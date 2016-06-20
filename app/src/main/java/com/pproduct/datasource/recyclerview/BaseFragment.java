package com.pproduct.datasource.recyclerview;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import com.pproduct.datasource.core.DataSource;
import com.pproduct.datasource.core.ListDataSource;
import com.pproduct.datasource.core.listeners.DataObject;
import com.pproduct.datasource.core.listeners.DataSourceStateListener;
import com.pproduct.datasource.core.listeners.Fetch;



/**
 * Created by Developer on 2/11/2016.
 */
public abstract class BaseFragment extends Fragment {
    protected View mRootView;
    protected WeakReference<Activity> mBaseLayoutActivity;
    protected RecyclerView mRecyclerView;
    protected RefreshLayout mSwipeRefreshLayout;
    protected LinearLayoutManager mLayoutManager;
    protected ListDataSource<DataObject> mDatasource;
    protected RecyclerViewAdapter mAdapter;

    protected View footerLayout;
    private ProgressView progressBar;
    private TextView textMore;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaseLayoutActivity = new WeakReference<Activity>((Activity) getContext());
    }

    public boolean isAlive() {
        return !((getActivity() == null) || !isVisible() || isDetached());
    }

    public void initDataSource(Fetch fetch) {
        mRootView = getLayoutInflater(getArguments()).inflate(R.layout.baselayout, null);
        mRecyclerView = (RecyclerView) mRootView.findViewById(getRecyclerViewID());
        mRecyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout = (RefreshLayout) mRootView.findViewById(getSwipeLayoutID());
        mSwipeRefreshLayout.setChildView(mRecyclerView);
        mSwipeRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.start();
                mDatasource.startContentRefreshing();
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mDatasource.startContentRefreshing();
            }
        });
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RecyclerViewAdapter(getAdapterDelegate());
        mRecyclerView.setAdapter(mAdapter);
        mDatasource = new ListDataSource<>(fetch);
        mDatasource.setStateListener(new DataSourceStateListener() {
            @Override
            public void dataSourceChangedState(DataSource dataSource, DataSource.State newState) {
                mAdapter.setDataSource(mDatasource);
                mAdapter.notifyDataSetChanged();
                if(newState== DataSource.State.CONTENT) {
                    progressBar.stop();
                    mSwipeRefreshLayout.setRefreshing(false);
                    mSwipeRefreshLayout.setLoading(false);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        mDatasource.startContentLoading();
        //footerLayout = getLayoutInflater(getArguments()).inflate(R.layout.footer_layout, null);
        textMore = (TextView) mRootView.findViewById(R.id.text_more);
        progressBar = (ProgressView) mRootView.findViewById(R.id.load_progress_bar);
        textMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //simulateLoadingData();
            }
        });
    }
    protected abstract RecyclerViewAdapter.AdapterDelegate getAdapterDelegate();


    protected int getRecyclerViewID() {
        return R.id.ds_recycler_view;
    }
    protected int getSwipeLayoutID() {
        return R.id.ds_refresh_layout;
    }
}
