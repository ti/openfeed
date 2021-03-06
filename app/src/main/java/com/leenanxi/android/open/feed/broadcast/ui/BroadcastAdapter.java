package com.leenanxi.android.open.feed.broadcast.ui;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.leenanxi.android.open.feed.R;
import com.leenanxi.android.open.feed.api.model.Broadcast;
import com.leenanxi.android.open.feed.util.RecyclerViewUtils;
import com.leenanxi.android.open.feed.util.ViewUtils;
import com.leenanxi.android.open.feed.widget.SimpleAdapter;

import java.util.List;

public class BroadcastAdapter extends SimpleAdapter<Broadcast, BroadcastAdapter.ViewHolder> {
    private Listener mListener;

    public BroadcastAdapter(List<Broadcast> broadcastList, Listener listener) {
        super(broadcastList);
        mListener = listener;
        setHasStableIds(true);
    }

    private static View getSharedView(ViewHolder holder) {
        Context context = holder.itemView.getContext();
        // HACK: Transition is so hard to work with, but this gives a better effect.
        View view = ViewUtils.hasSw600dp(context) ? holder.cardView : holder.broadcastLayout;
        ViewCompat.setTransitionName(view, context.getString(R.string.transition_name_broadcast));
        return view;
    }

    public Broadcast findBroadcastById(long broadcastId) {
        int count = getItemCount();
        for (int i = 0; i < count; ++i) {
            Broadcast broadcast = getItem(i);
            if (broadcast.id == broadcastId) {
                return broadcast;
            } else if (broadcast.rebroadcastedBroadcast != null
                    && broadcast.rebroadcastedBroadcast.id == broadcastId) {
                return broadcast.rebroadcastedBroadcast;
            }
        }
        return null;
    }

    public void updateBroadcast(Broadcast updatedBroadcast) {
        int count = getItemCount();
        for (int i = 0; i < count; ++i) {
            Broadcast broadcast = getItem(i);
            if (broadcast.id == updatedBroadcast.id) {
                set(i, updatedBroadcast);
            } else if (broadcast.rebroadcastedBroadcast != null
                    && broadcast.rebroadcastedBroadcast.id == updatedBroadcast.id) {
                broadcast.rebroadcastedBroadcast = updatedBroadcast;
                notifyItemChanged(i);
            }
        }
    }

    public void removeBroadcastById(long broadcastId) {
        int count = getItemCount();
        for (int i = 0; i < count; ) {
            Broadcast broadcast = getItem(i);
            if (broadcast.id == broadcastId
                    || (broadcast.rebroadcastedBroadcast != null
                    && broadcast.rebroadcastedBroadcast.id == broadcastId)) {
                remove(i);
                --count;
            } else {
                ++i;
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return getList().get(position).id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ViewUtils.inflate(R.layout.broadcast_item, parent));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Broadcast originalBroadcast = getItem(position);
        holder.rebroadcastedByText.setText(originalBroadcast.getRebroadcastedBy(
                RecyclerViewUtils.getContext(holder)));
        final Broadcast broadcast = originalBroadcast.rebroadcastedBroadcast != null ?
                originalBroadcast.rebroadcastedBroadcast : originalBroadcast;
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onOpenBroadcast(broadcast, getSharedView(holder));
            }
        });
        holder.broadcastLayout.bindBroadcast(broadcast);
        holder.broadcastLayout.mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener.onLikeBroadcast(originalBroadcast.id, broadcast, !broadcast.liked)) {
                    holder.broadcastLayout.mLikeButton.setEnabled(false);
                }
            }
        });
        holder.broadcastLayout.mRebroadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener.onRebroadcastBroadcast(originalBroadcast.id, broadcast,
                        !broadcast.isRebroadcasted())) {
                    holder.broadcastLayout.mRebroadcastButton.setEnabled(false);
                }
            }
        });
        holder.broadcastLayout.mCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Not setting button disabled because we are using enabled state for indeterminate
                // state due to network, and we want the click to always open the broadcast for our
                // user.
                mListener.onCommentBroadcast(broadcast, getSharedView(holder));
            }
        });
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.broadcastLayout.releaseBroadcast();
    }

    public interface Listener {
        boolean onLikeBroadcast(long itemId, Broadcast broadcast, boolean like);

        boolean onRebroadcastBroadcast(long itemId, Broadcast broadcast, boolean rebroadcast);

        void onCommentBroadcast(Broadcast broadcast, View sharedView);

        void onOpenBroadcast(Broadcast broadcast, View sharedView);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView rebroadcastedByText;
        public CardView cardView;
        public BroadcastLayout broadcastLayout;


        public ViewHolder(View itemView) {
            super(itemView);
            rebroadcastedByText = (TextView) itemView.findViewById(R.id.rebroadcasted_by);
            cardView = (CardView) itemView.findViewById(R.id.card);
            broadcastLayout = (BroadcastLayout) itemView.findViewById(R.id.broadcast);
        }
    }
}
