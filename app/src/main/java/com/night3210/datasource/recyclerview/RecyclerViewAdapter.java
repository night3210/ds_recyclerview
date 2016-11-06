package com.night3210.datasource.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.night3210.datasource.core.ListDataSource;
import com.night3210.datasource.core.data_structure.DataStructure;
import com.night3210.datasource.core.listeners.DataObject;

import java.lang.ref.WeakReference;

/**
 * Created by Ivan on 2/10/2016.
 */
public class RecyclerViewAdapter<T extends BaseRecyclerViewHolder, H extends DataObject> extends RecyclerView.Adapter<T> {
    protected WeakReference<DataStructure<H>> mData;

    public interface AdapterDelegate<T extends BaseRecyclerViewHolder> {
        int getViewType(DataStructure.IndexPath ip);
        T createViewForViewType(ViewGroup parent, int type);
        void customizeViewFor(DataStructure.IndexPath ip, BaseRecyclerViewHolder holder);
        void cellSelected(DataStructure.IndexPath ip);
    }

    private AdapterDelegate mAdapterDelegate;
    public RecyclerViewAdapter(AdapterDelegate adapterDelegate) {
        setHasStableIds(true);
        if(adapterDelegate==null)
            throw new IllegalArgumentException("Empty adapterDelegate");
        mAdapterDelegate = adapterDelegate;
    }
    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        return (T) mAdapterDelegate.createViewForViewType(parent, viewType);
    }
    @Override
    public void onBindViewHolder(T holder, int position) {
        final DataStructure.IndexPath indexPath = getIndexPathForPosition(position);
        mAdapterDelegate.customizeViewFor(indexPath, holder);
        if (holder.itemView != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAdapterDelegate.cellSelected(indexPath);
                }
            });
        }
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
        if(mData==null || mData.get()==null)
            return 0;
        return getIndexPathForPosition(position).getSection();
    }
    public DataStructure.IndexPath getIndexPathForPosition(int position) {
        int row = position;
        DataStructure<H> dataStructure = mData.get();
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
        DataStructure<H> dataStructure = mData.get();
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
        mData=new WeakReference<DataStructure<H>>(dataSource.getDataStructure());
    }
    public H getItem(int position) {
        if(mData==null || mData.get()==null)
            return null;
        return mData.get().getItemForIndexPath(getIndexPathForPosition(position));
    }
    public void insertItem(H item) {
        //mData.get().add
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
