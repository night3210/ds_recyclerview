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
public class RecyclerViewAdapter<H extends DataObject, T extends BaseRecyclerViewHolder> extends RecyclerView.Adapter<T> {
    protected WeakReference<DataStructure<H>> mData;
    protected AdapterDelegate<T> footerHolder;
    public static final int FOOTER_VIEW_TYPE = 1515;

    private AdapterDelegate<T> mAdapterDelegate;

    public interface AdapterDelegate<T extends BaseRecyclerViewHolder> {
        int getViewType(DataStructure.IndexPath ip);
        T createViewForViewType(ViewGroup parent, int type);
        void customizeViewFor(DataStructure.IndexPath ip, T holder);
        void cellSelected(DataStructure.IndexPath ip);
    }

    public RecyclerViewAdapter(AdapterDelegate<T> adapterDelegate) {
        setHasStableIds(true);
        if(adapterDelegate==null)
            throw new IllegalArgumentException("Empty adapterDelegate");
        mAdapterDelegate = adapterDelegate;
    }

    public void setFooterViewAdapterDelegate(AdapterDelegate<T> delegate) {
        footerHolder = delegate;
    }

    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==FOOTER_VIEW_TYPE) {
            return footerHolder.createViewForViewType(parent, viewType);
        }
        return mAdapterDelegate.createViewForViewType(parent, viewType);
    }

    @Override
    public void onBindViewHolder(T holder, int position) {
        if(isFooterPosition(position)) {
            footerHolder.customizeViewFor(null ,holder);
            return;
        }
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
        if(mData==null)
            return 0;
        DataStructure<H> dataStructure =  mData.get();
        if(dataStructure==null)
            return 0;
        return dataStructure.dataSize() + (footerHolder!=null?1:0);
    }

    @Override
    public int getItemViewType(int position) {
        if(mData==null || mData.get()==null)
            return 0;
        if(isFooterPosition(position))
            return FOOTER_VIEW_TYPE;
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

    public void setDataSource(ListDataSource<H> dataSource) {
        mData=new WeakReference<>(dataSource.getDataStructure());
    }

    public H getItem(int position) {
        if(isFooterPosition(position))
            return null;
        if(mData==null)
            return null;
        DataStructure<H> dataStructure =  mData.get();
        if(dataStructure==null)
            return null;
        DataStructure.IndexPath indexPath = getIndexPathForPosition(position);
        if (indexPath == null) {
            return null;
        }
        return dataStructure.getItemForIndexPath(indexPath);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public boolean isFooterPosition(int position) {
        if (footerHolder==null) {
            return false;
        }
        if (position < (getItemCount() - 1)) {
            return false;
        }
        return true;
    }
}
