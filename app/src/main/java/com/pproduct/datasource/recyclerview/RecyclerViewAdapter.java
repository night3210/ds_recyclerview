package com.pproduct.datasource.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.night3210.datasource.core.ListDataSource;
import com.night3210.datasource.core.data_structure.DataStructure;

import java.lang.ref.WeakReference;

/**
 * Created by Developer on 2/10/2016.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<BaseRecyclerViewHolder> {
    WeakReference<DataStructure> mData;


    public interface AdapterDelegate {
        int getViewType(DataStructure.IndexPath ip);
        BaseRecyclerViewHolder createViewForViewType(ViewGroup parent, int type);
        void customizeViewFor(DataStructure.IndexPath ip, BaseRecyclerViewHolder holder);
        void cellSelected(DataStructure.IndexPath ip);
    }
    private AdapterDelegate mAdapterDelegate;
    public RecyclerViewAdapter(AdapterDelegate adapterDelegate) {
//        if(str==null)
//            throw new IllegalArgumentException("Empty datastructure");
        if(adapterDelegate==null)
            throw new IllegalArgumentException("Empty adapterDelegate");
        //mData=new WeakReference<>(str);
        mAdapterDelegate = adapterDelegate;
    }
    @Override
    public BaseRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mAdapterDelegate.createViewForViewType(parent, viewType);
    }
    @Override
    public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
        mAdapterDelegate.customizeViewFor(getIndexPathForPosition(position), holder);
    }
    @Override
    public int getItemCount() {
        if(mData==null ||
                mData.get()==null)
            return 0;
        return mData.get().dataSize();
    }
    @Override
    public int getItemViewType(int position) {
        if(mData==null ||
                mData.get()==null)
            return 0;
        return getIndexPathForPosition(position).getSection();
    }
    public DataStructure.IndexPath getIndexPathForPosition(int position) {
        int row = position;
        DataStructure dataStructure = mData.get();
        if(dataStructure==null)
            throw new IllegalStateException("DataStructure == null");
        int section = 0;
        for(int i=0;i<dataStructure.getSectionsCount();i++) {
            int sectionCount = dataStructure.itemsCountForSection(i);
            if (row < sectionCount) {
                break;
            }
            row -= sectionCount;
            section++;
        }
        return new DataStructure.IndexPath(row, section);
    }
    public int getPositionForIndexPath(DataStructure.IndexPath path) {
        int position = path.getRow();
        DataStructure dataStructure = mData.get();
        if(dataStructure == null)
            throw new IllegalStateException("DataStructure == null");
        for(int i=0; i<dataStructure.getSectionsCount(); i++) {
            if(path.getSection() == i)
                break;
            position += dataStructure.itemsCountForSection(i);
        }
        return position;
    }
    public void setDataSource(ListDataSource dataSource) {
        mData=new WeakReference<DataStructure>(dataSource.getDataStructure());
    }

}
