package com.pproduct.datasource.recyclerview;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pproduct.datasource.core.LogUtils;
import com.pproduct.datasource.core.data_structure.DataStructure;
import com.pproduct.datasource.core.listeners.DataObject;

/**
 * Created by haritonbatkov on 6/20/16.
 */
public class SimpleRecyclerFragment<T extends DataObject> extends BaseFragment<T> {
    @Override
    protected RecyclerViewAdapter.AdapterDelegate getAdapterDelegate() {
        return new RecyclerViewAdapter.AdapterDelegate() {
            @Override
            public int getViewType(DataStructure.IndexPath ip) {
                return 0;
            }

            @Override
            public BaseRecyclerViewHolder createViewForViewType(ViewGroup parent, int type) {
                View row = View.inflate(getContext(), R.layout.base_recycler_row, parent);
                BaseRecyclerViewHolder holder = new BaseRecyclerViewHolder(row);
                return holder;
            }

            @Override
            public void customizeViewFor(DataStructure.IndexPath ip, BaseRecyclerViewHolder holder) {
                T object = mDatasource.getDataStructure().getItemForIndexPath(ip);
                TextView textView = (TextView) holder.itemView.findViewById(R.id.textView);
                textView.setText(object.getObjectId());

            }

            @Override
            public void cellSelected(DataStructure.IndexPath ip) {
                T object = mDatasource.getDataStructure().getItemForIndexPath(ip);
                LogUtils.logi("Cell selected: " + object.getObjectId());
            }
        };
    }

    @Override
    protected int getLayoutId() {
        return R.layout.baselayout;
    }
}
