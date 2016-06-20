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
public abstract class BaseFragment<T extends DataObject> extends Fragment {
    protected View mRootView;
    protected WeakReference<Activity> mBaseLayoutActivity;
    protected RecyclerView mRecyclerView;
    protected RefreshLayout mSwipeRefreshLayout;
    protected LinearLayoutManager mLayoutManager;
    protected ListDataSource<T> mDatasource;
    protected RecyclerViewAdapter mAdapter;

    protected View footerLayout;
    private ProgressView progressBar;
    private TextView textMore;

    protected Fetch mFetch;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaseLayoutActivity = new WeakReference<Activity>((Activity) getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        initDataSource();
    }

    public boolean isAlive() {
        return !((getActivity() == null) || !isVisible() || isDetached());
    }

    protected ListDataSource<T> createDataSource() {
        if (mFetch == null) {
            throw new IllegalStateException("You need to provide fetch");
        }
        return new ListDataSource<>(mFetch);
    }

    protected void initRecyclerView() {
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
    public void initDataSource() {
        mDatasource = createDataSource();
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
    }
    protected abstract RecyclerViewAdapter.AdapterDelegate getAdapterDelegate();


    protected int getRecyclerViewID() {
        return R.id.ds_recycler_view;
    }
    protected int getSwipeLayoutID() {
        return R.id.ds_refresh_layout;
    }

    public Fetch getFetch() {
        return mFetch;
    }

    public void setFetch(Fetch mFetch) {
        this.mFetch = mFetch;
    }
}
