package com.haiming.baserecyclerviewadapterproject.base;

import android.content.Context;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public abstract class RecyclerViewAdapter<M, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected List<M> mDataList = new ArrayList<>();
    private HeaderAndFooterAdapter mHeaderAndFooterAdapter;
    private Context mContext;

    public RecyclerViewAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    /**
     * @return 获取指定索引位置的数据模型
     */
    public M getItem(int position) {
        if (mDataList != null && position >= 0 && mDataList.size() > position) {
            return mDataList.get(position);
        } else {
            return null;
        }
    }

    /**
     * @return 获取数据集合
     */
    public List<M> getDataList() {
        return mDataList;
    }

    /**
     * @return 数据列表是否为空
     */
    public boolean isDataListEmpty() {
        return (mDataList == null || mDataList.size() <= 0);
    }

    /**
     * @param data 在指定位置添加新的数据
     */
    public void addData(M data, int position) {
        if (data != null && mDataList.size() >= position) {
            mDataList.add(position, data);
            notifyDataSetChangedWrapper();
        }
    }

    /**
     * @param data 在集合尾部添加新的数据
     */
    public void addMoreData(M data) {
        if (data != null) {
            mDataList.add(data);
            notifyDataSetChangedWrapper();
        }
    }

    /**
     * @param list 在集合尾部添加更多数据集合
     */
    public void addMoreData(List<M> list) {
        if (list != null && list.size() >= 0) {
            int positionStart = mDataList.size();
            mDataList.addAll(positionStart, list);
            notifyItemRangeInsertedWrapper(positionStart, list.size());
        }
    }

    /**
     * @param list 在集合头部部添加更多数据集合
     */
    public void addHeadData(List<M> list) {
        if (list != null && list.size() >= 0) {
            mDataList.addAll(0, list);
            notifyItemRangeInsertedWrapper(0, list.size());
        }
    }

    /**
     * @param data 设置全新的数据集合，如果传入空（null或空表），则清空数据列表
     *             （第一次从服务器加载数据，或者下拉刷新当前界面数据表）
     */
    public void setData(List<M> data) {
        mDataList.clear();
        if (data != null && data.size() >= 0) {
            mDataList.addAll(data);
        }
        notifyDataSetChangedWrapper();
    }

    /**
     * 清空数据列表
     */

    public void clear() {
        mDataList.clear();
        notifyDataSetChangedWrapper();
    }

    public Context getContext() {
        return mContext;
    }

    private void notifyItemRangeInsertedWrapper(int positionStart, int itemCount) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemRangeInserted(positionStart, itemCount);
        } else {
            mHeaderAndFooterAdapter.notifyItemRangeInserted(mHeaderAndFooterAdapter.getHeadersCount() + positionStart, itemCount);
        }
    }

    private void notifyItemRangeChangedWrapper(int positionStart, int itemCount) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemRangeChanged(positionStart, itemCount);
        } else {
            mHeaderAndFooterAdapter.notifyItemRangeChanged(mHeaderAndFooterAdapter.getHeadersCount() + positionStart, itemCount);
        }
    }

    //刷新数据区域，即不刷Header和Footer
    protected void notifyDataItemsChanged() {
        try {
            notifyItemRangeChangedWrapper(0, getItemCount());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void notifyDataSetChangedWrapper() {
        //如果没有头尾view的Adapter，则直接更新
        if (mHeaderAndFooterAdapter == null) {
            notifyDataSetChanged();
        } else {
            //否则，要通知头尾view的Adapter更新
            mHeaderAndFooterAdapter.notifyDataSetChanged();
        }
    }

    public final void notifyItemChangedWrapper(int position) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemChanged(position);
        } else {
            mHeaderAndFooterAdapter.notifyItemChanged(mHeaderAndFooterAdapter.getHeadersCount() + position);
        }
    }

    public void addHeaderView(View headerView) {
        getHeaderAndFooterAdapter().addHeaderView(headerView);
    }

    public void addFooterView(View footerView) {
        getHeaderAndFooterAdapter().addFooterView(footerView);
    }

    public int getHeadersCount() {
        return mHeaderAndFooterAdapter == null ? 0 : mHeaderAndFooterAdapter.getHeadersCount();
    }

    public int getFootersCount() {
        return mHeaderAndFooterAdapter == null ? 0 : mHeaderAndFooterAdapter.getFootersCount();
    }

    public HeaderAndFooterAdapter getHeaderAndFooterAdapter() {
        if (mHeaderAndFooterAdapter == null) {
            synchronized (RecyclerViewAdapter.this) {
                if (mHeaderAndFooterAdapter == null) {
                    mHeaderAndFooterAdapter = new HeaderAndFooterAdapter(this);
                }
            }
        }
        return mHeaderAndFooterAdapter;
    }
}
