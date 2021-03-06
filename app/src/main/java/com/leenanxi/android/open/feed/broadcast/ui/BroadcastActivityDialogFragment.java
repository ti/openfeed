package com.leenanxi.android.open.feed.broadcast.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import com.leenanxi.android.open.feed.R;
import com.leenanxi.android.open.feed.api.model.Broadcast;
import com.leenanxi.android.open.feed.eventbus.BroadcastUpdatedEvent;
import com.leenanxi.android.open.feed.widget.TabFragmentPagerAdapter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BroadcastActivityDialogFragment extends AppCompatDialogFragment {
    private static final String KEY_PREFIX = BroadcastActivityDialogFragment.class.getName() + '.';
    public static final String EXTRA_BROADCAST = KEY_PREFIX + "broadcast";
    TabLayout mTabLayout;
    ViewPager mViewPager;
    Button mPositiveButton;
    Button mNegativeButton;
    Button mNeutralButton;
    private TabFragmentPagerAdapter mTabAdapter;
    private Broadcast mBroadcast;
    /**
     * @deprecated Use {@link #newInstance(Broadcast)} instead.
     */
    public BroadcastActivityDialogFragment() {
    }

    public static BroadcastActivityDialogFragment newInstance(Broadcast broadcast) {
        //noinspection deprecation
        BroadcastActivityDialogFragment fragment = new BroadcastActivityDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(EXTRA_BROADCAST, broadcast);
        fragment.setArguments(arguments);
        return fragment;
    }

    public static void show(Broadcast broadcast, FragmentActivity activity) {
        BroadcastActivityDialogFragment.newInstance(broadcast)
                .show(activity.getSupportFragmentManager(), null);
    }

    private void initViews(View itemView) {
        mTabLayout = (TabLayout) itemView.findViewById(R.id.tab);
        mViewPager = (ViewPager) itemView.findViewById(R.id.viewPager);
        mPositiveButton = (Button) itemView.findViewById(android.R.id.button1);
        mNegativeButton = (Button) itemView.findViewById(android.R.id.button2);
        mNeutralButton = (Button) itemView.findViewById(android.R.id.button3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBroadcast = getArguments().getParcelable(EXTRA_BROADCAST);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppCompatDialog dialog = (AppCompatDialog) super.onCreateDialog(savedInstanceState);
        // We are using a custom title, as in AlertDialog.
        dialog.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.broadcast_activity_dialog_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTabAdapter = new TabFragmentPagerAdapter(getChildFragmentManager());
        mTabAdapter.addTab(BroadcastLikerListFragment.newInstance(mBroadcast), null);
        mTabAdapter.addTab(BroadcastRebroadcastersListFragment.newInstance(mBroadcast), null);
        updateTabTitle();
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(mTabAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mPositiveButton.setText(R.string.ok);
        mPositiveButton.setVisibility(View.VISIBLE);
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mNegativeButton.setVisibility(View.GONE);
        mNeutralButton.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BroadcastUpdatedEvent event) {
        Broadcast broadcast = event.broadcast;
        if (broadcast.id == mBroadcast.id) {
            mBroadcast = broadcast;
            updateTabTitle();
        }
    }

    private void updateTabTitle() {
        mTabAdapter.setPageTitle(mTabLayout, 0, getTabTitle(mBroadcast.likeCount,
                R.string.broadcast_likers_title_format, R.string.broadcast_likers_title_empty));
        mTabAdapter.setPageTitle(mTabLayout, 1, getTabTitle(mBroadcast.rebroadcastCount,
                R.string.broadcast_rebroadcasters_title_format,
                R.string.broadcast_rebroadcasters_title_empty));
    }

    private CharSequence getTabTitle(int count, int formatResId, int emptyResId) {
        return count > 0 ? getString(formatResId, count) : getString(emptyResId);
    }
}
