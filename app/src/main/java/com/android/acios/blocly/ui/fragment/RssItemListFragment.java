package com.android.acios.blocly.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.acios.blocly.BloclyApplication;
import com.android.acios.blocly.R;
import com.android.acios.blocly.api.DataSource;
import com.android.acios.blocly.api.model.RssFeed;
import com.android.acios.blocly.api.model.RssItem;
import com.android.acios.blocly.ui.adapter.ItemAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by christophepouliot on 15-07-19.
 */
public class RssItemListFragment extends Fragment implements ItemAdapter.DataSource, ItemAdapter.ItemAdapterDelegate {

    private static final String BUNDLE_EXTRA_RSS_FEED = RssItemListFragment.class.getCanonicalName().concat(".EXTRA_RSS_FEED");

    public static RssItemListFragment fragmentForRssFeed(RssFeed rssFeed) {
        Bundle arguments = new Bundle();
        arguments.putLong(BUNDLE_EXTRA_RSS_FEED, rssFeed.getRowId());
        RssItemListFragment rssItemListFragment = new RssItemListFragment();
        rssItemListFragment.setArguments(arguments);
        return rssItemListFragment;
    }

    public interface Delegate {
         void onItemExpanded(RssItemListFragment rssItemListFragment, RssItem rssItem);
         void onItemContracted(RssItemListFragment rssItemListFragment, RssItem rssItem);
         void onItemVisitClicked(RssItemListFragment rssItemListFragment, RssItem rssItem);
    }

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;

    private RssFeed currentFeed;
    private List<RssItem> currentItems = new ArrayList<>();

    private WeakReference<Delegate> delegate;

    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);
        delegate = new WeakReference<>((Delegate) activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemAdapter = new ItemAdapter();
        itemAdapter.setDataSource(this);
        itemAdapter.setDelegate(this);



        Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }
        long feedRowId = arguments.getLong(BUNDLE_EXTRA_RSS_FEED);
        BloclyApplication.getSharedDataSource().fetchFeedWithId(feedRowId, new DataSource.Callback<RssFeed>() {
            @Override
            public void onSuccess(RssFeed rssFeed) {
                currentFeed = rssFeed;
            }

            @Override
            public void onError(String errorMessage) {}
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_rss_list, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) inflate.findViewById(R.id.srl_fragment_rss_list);
        recyclerView = (RecyclerView) inflate.findViewById(R.id.rv_fragment_rss_list);
        return inflate;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                BloclyApplication.getSharedDataSource().fetchNewItemsForFeed(currentFeed,
                        new DataSource.Callback<List<RssItem>>() {
                            @Override
                            public void onSuccess(List<RssItem> rssItems) {

                                if (getActivity() == null) {
                                    return;
                                }
                                if (!rssItems.isEmpty()) {

                                    currentItems.addAll(0, rssItems);

                                    itemAdapter.notifyItemRangeInserted(0, rssItems.size());
                                    //itemAdapter.notifyDataSetChanged();
                                }
                                swipeRefreshLayout.setRefreshing(false);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(itemAdapter);
    }

    /*
     * ItemAdapter.DataSource
     */

    @Override
    public RssItem getRssItem(ItemAdapter itemAdapter, int position){
        return currentItems.get(position);
    }

    @Override
    public RssFeed getRssFeed(ItemAdapter itemAdapter, int position) {
        return currentFeed;
    }

    @Override
    public int getItemCount(ItemAdapter itemAdapter) {
        return currentItems.size();
    }

    /*
     * ItemAdapter.ItemAdapterDelegate
     */

    @Override
    public void onItemClicked(ItemAdapter itemAdapter, RssItem rssItem) {
        int positionToExpand = -1;
        int positionToContract = -1;
        if (itemAdapter.getExpandedItem() != null) {
            positionToContract = currentItems.indexOf(itemAdapter.getExpandedItem());
            View viewToContract = recyclerView.getLayoutManager().findViewByPosition(positionToContract);
            if (viewToContract == null) {
                positionToContract = -1;
            }
        }
        if (itemAdapter.getExpandedItem() != rssItem) {
            positionToExpand = currentItems.indexOf(rssItem);
            itemAdapter.setExpandedItem(rssItem);
        } else {
            itemAdapter.setExpandedItem(null);
        }
        if (positionToContract > -1) {
            itemAdapter.notifyItemChanged(positionToContract);
        }
        if (positionToExpand > -1) {
            itemAdapter.notifyItemChanged(positionToExpand);
            delegate.get().onItemExpanded(this, itemAdapter.getExpandedItem());
        } else {
            delegate.get().onItemContracted(this, rssItem);
            return;
        }

    }

    @Override
    public void onVisitClicked(ItemAdapter itemAdapter, RssItem rssItem) {
        delegate.get().onItemVisitClicked(this, rssItem);
    }

    @Override
    public void didFavorite(View view, boolean isChecked, RssItem rssItem) {

    }

    @Override
    public void didArchive(View view, boolean isChecked, RssItem rssItem) {

    }
}
