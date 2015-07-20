package com.android.acios.blocly.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.acios.blocly.R;
import com.android.acios.blocly.api.model.RssFeed;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by christophepouliot on 15-07-02.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder>{

    public enum NavigationOption {
        NAVIGATION_OPTION_INBOX,
        NAVIGATION_OPTION_FAVORITES,
        NAVIGATION_OPTION_ARCHIVED
    }

    public static interface NavigationDrawerAdapterDataSource {
        public List<RssFeed> getFeeds(NavigationDrawerAdapter adapter);
    }


    public static interface NavigationDrawerAdapterDelegate {
        public void didSelectNavigationOption(NavigationDrawerAdapter adapter, NavigationOption navigationOption);
        public void didSelectFeed(NavigationDrawerAdapter adapter, RssFeed rssFeed);
    }

    WeakReference<NavigationDrawerAdapterDelegate> delegate;
    WeakReference<NavigationDrawerAdapterDataSource> dataSource;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.navigation_item, viewGroup, false);
        return new ViewHolder(inflate);
        }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        RssFeed rssFeed = null;

        if (position >= NavigationOption.values().length) {
            int feedPosition = position - NavigationOption.values().length;
            rssFeed = getDataSource().getFeeds(this).get(feedPosition);
        }
        viewHolder.update(position, rssFeed);

    }

    @Override
    public int getItemCount() {
        return NavigationOption.values().length + getDataSource().getFeeds(this).size();
    }

    public NavigationDrawerAdapterDelegate getDelegate() {
        if (delegate == null) {
            return null;
        }
        return delegate.get();
    }

    public void setDelegate(NavigationDrawerAdapterDelegate delegate) {
        this.delegate = new WeakReference<>(delegate);
    }

    public NavigationDrawerAdapterDataSource getDataSource() {
        if (dataSource == null) {
            return null;
        }
        return dataSource.get();
    }

    public void setDataSource(NavigationDrawerAdapterDataSource dataSource) {
        this.dataSource = new WeakReference<>(dataSource);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View topPadding;
        TextView title;
        View bottomPadding;
        View divider;

        int position;
        RssFeed rssFeed;

        public ViewHolder(View itemView) {
            super(itemView);
            topPadding = itemView.findViewById(R.id.v_nav_item_top_padding);
            title = (TextView) itemView.findViewById(R.id.tv_nav_item_title);
            bottomPadding = itemView.findViewById(R.id.v_nav_item_bottom_padding);
            divider = itemView.findViewById(R.id.v_nav_item_divider);
            itemView.setOnClickListener(this);
        }

        public void update(int position, RssFeed rssFeed) {
            this.position = position;
            this.rssFeed = rssFeed;

            boolean shouldShowTopPadding = position == NavigationOption.NAVIGATION_OPTION_INBOX.ordinal()
                    || position == NavigationOption.values().length;
            topPadding.setVisibility(shouldShowTopPadding ? View.VISIBLE : View.GONE);

            boolean shouldShowBottomPadding = position == NavigationOption.NAVIGATION_OPTION_ARCHIVED.ordinal()
                    || position == getItemCount()-1;
            bottomPadding.setVisibility(shouldShowBottomPadding ? View.VISIBLE : View.GONE);

            divider.setVisibility(position == NavigationOption.NAVIGATION_OPTION_ARCHIVED.ordinal()
                    ? View.VISIBLE : View.GONE);

            if (position < NavigationOption.values().length) {
                int[] titleTexts = new int[] {R.string.navigation_option_inbox,
                        R.string.navigation_option_favorites,
                        R.string.navigation_option_archived};
                title.setText(titleTexts[position]);
            } else {
                if (rssFeed != null) {
                    title.setText(rssFeed.getTitle());
                } else {
                    title.setText("NULL");
                }
            }

        }

        @Override
        public void onClick(View v) {
            if (getDelegate() == null) {
                return;
            }

            if (position < NavigationOption.values().length) {
                getDelegate().didSelectNavigationOption(NavigationDrawerAdapter.this, NavigationOption.values()[position]);

            } else {
                getDelegate().didSelectFeed(NavigationDrawerAdapter.this, rssFeed);
            }
        }

    }



}
