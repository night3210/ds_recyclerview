package com.night3210.datasource.recyclerview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.night3210.datasource.core.DataSource;
import com.night3210.datasource.core.ListDataSource;
import com.night3210.datasource.core.LogUtils;
import com.night3210.datasource.core.listeners.ChangedCallback;
import com.night3210.datasource.core.listeners.DataObject;
import com.night3210.datasource.core.listeners.DataSourceStateListener;
import com.night3210.datasource.core.listeners.Fetch;

import java.lang.ref.WeakReference;

/**
 * Created by Developer on 2/11/2016.
 */
public abstract class BaseFragment<T extends DataObject, H extends BaseRecyclerViewHolder> extends Fragment {
    protected View mRootView;
    protected WeakReference<AppCompatActivity> mBaseLayoutActivity;
    protected RecyclerView mRecyclerView;
    protected RefreshLayout mSwipeRefreshLayout;
    protected LinearLayoutManager mLayoutManager;
    protected ListDataSource<T> mDataSource;
    protected RecyclerViewAdapter<T, H> mAdapter;

    protected ProgressView progressBar;
    protected TextView textMore;

    protected Fetch mFetch;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaseLayoutActivity = new WeakReference<>((AppCompatActivity) getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(getLayoutId(), container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
        initRecyclerView();
        initDataSource();
    }

    public boolean isAlive() {
        return !((getActivity() == null) || isDetached());
    }

    protected ListDataSource<T> createDataSource() {
        if (mFetch == null) {
            throw new IllegalStateException("You need to provide fetch before createDataSource called");
        }
        return new ListDataSource<>(mFetch);
    }
    public void setRecyclerSizeFree(){
        mRecyclerView.setHasFixedSize(false);
    }

    private void findViews() {
        findRecyclerView();
        findSwipeRefreshLayout();
        textMore = (TextView) mRootView.findViewById(R.id.text_more);
        progressBar = (ProgressView) mRootView.findViewById(R.id.load_progress_bar);
    }
    protected void findRecyclerView() {
        mRecyclerView = (RecyclerView) mRootView.findViewById(getRecyclerViewID());
    }
    protected void findSwipeRefreshLayout() {
        mSwipeRefreshLayout = (RefreshLayout) mRootView.findViewById(getSwipeLayoutID());
    }
    protected void initRecyclerView() {
        if(mRecyclerView==null) {
            LogUtils.logi("No recyclerview, skip datasource creation");
            return;
        }
        mRecyclerView.setHasFixedSize(true);
        setupRefreshLayout();
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RecyclerViewAdapter<T,H>(getAdapterDelegate());
        mRecyclerView.setAdapter(mAdapter);
        //footerLayout = getLayoutInflater(getArguments()).inflate(R.layout.footer_layout, null);
        if(textMore!=null)
            textMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDataSource != null) {
                        mDataSource.loadMoreIfPossible();
                    }
                }
            });
    }
    public void initDataSource() {
        mDataSource = createDataSource();
        if(mDataSource ==null) {
            LogUtils.logi("No dataSource or fetch, skip datasource creation. ds/fetch = "+ mFetch);
            return;
        }
        mDataSource.setStateListener(new DataSourceStateListener() {
            @Override
            public void dataSourceChangedState(DataSource dataSource, DataSource.State newState) {
                if(mAdapter==null)
                    throw new RuntimeException("no adapter inside datasource state listener");
                if(!isAlive())
                    return;
                if(newState!= DataSource.State.REFRESH_CONTENT) {
                    mAdapter.setDataSource(mDataSource);
                    mAdapter.notifyDataSetChanged();
                    hideProgressBar();
                }
                if(newState == DataSource.State.CONTENT) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mSwipeRefreshLayout.setLoading(false);
                    hideProgressBar();
                }else if(newState == DataSource.State.NO_CONTENT) {
                    hideProgressBar();
                }
            }
        });
        mDataSource.setChangedListener(new ChangedCallback() {
            @Override
            public void changed() {
                if(!isAlive())
                    return;
                mAdapter.notifyDataSetChanged();
            }
        });
        mDataSource.startContentLoading();
    }

    protected void hideProgressBar() {
        progressBar.stop();
        progressBar.setVisibility(View.GONE);
    }

    protected void showProgressBar() {
        progressBar.start();
        progressBar.setVisibility(View.VISIBLE);
    }

    protected abstract RecyclerViewAdapter.AdapterDelegate<H> getAdapterDelegate();


    protected int getRecyclerViewID() {
        return R.id.ds_recycler_view;
    }
    protected int getSwipeLayoutID() {
        return R.id.ds_refresh_layout;
    }
    protected abstract int getLayoutId();

    public void setFetch(Fetch fetch) {
        mFetch = fetch;
    }

    protected void setupRefreshLayout() {
        mSwipeRefreshLayout.setChildView(mRecyclerView);
        mSwipeRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                if (!isInvertedRefreshActions()) {
                    loadMoreTriggered();
                } else {
                    refreshTriggered();
                }
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isInvertedRefreshActions()) {
                    loadMoreTriggered();
                } else {
                    refreshTriggered();
                }
            }
        });
        mSwipeRefreshLayout.setEnabled(isRefreshAvailable());
    }

    protected void refreshTriggered() {
        showProgressBar();
        mDataSource.startContentRefreshing();
    }

    protected void loadMoreTriggered() {
        showProgressBar();
        mDataSource.refreshContentIfPossible();
    }

    protected boolean isInvertedRefreshActions() {
        return false;
    }

    protected boolean isRefreshAvailable() {
        return true;
    }
}
