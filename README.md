# NiceRecyclerViewAdapter
一个可支持添加首尾view的基类Adapter

ListView中可以通过以下这两个方法设置头部和尾部的view，我们研究listView的源码来为rv添加同样的方法。
  ```
listView.addHeaderView();
 listView.addFooterView();
  ```
**添加头部**
  ```
 public void addHeaderView(View v, Object data, boolean isSelectable) {
        if (v.getParent() != null && v.getParent() != this) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "The specified child already has a parent. "
                           + "You must call removeView() on the child's parent first.");
            }
        }
        final FixedViewInfo info = new FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        //1.添加到头部集合
        mHeaderViewInfos.add(info);
        mAreAllItemsSelectable &= isSelectable;

        // Wrap the adapter if it wasn't already wrapped.
        //2.适配器不为null
        if (mAdapter != null) {
            if (!(mAdapter instanceof HeaderViewListAdapter)) {
                //3.在原来的Adapter上进行一层包装
                wrapHeaderListAdapterInternal();
            }

            // In the case of re-adding a header view, or adding one later on,
            // we need to notify the observer.
            if (mDataSetObserver != null) {
                mDataSetObserver.onChanged();
            }
        }
    }

 protected HeaderViewListAdapter wrapHeaderListAdapterInternal(
            ArrayList<ListView.FixedViewInfo> headerViewInfos,
            ArrayList<ListView.FixedViewInfo> footerViewInfos,
            ListAdapter adapter) {
        return new HeaderViewListAdapter(headerViewInfos, footerViewInfos, adapter);
    }

    /** @hide */
    protected void wrapHeaderListAdapterInternal() {
        mAdapter = wrapHeaderListAdapterInternal(mHeaderViewInfos, mFooterViewInfos, mAdapter);
    }

 public void addFooterView(View v, Object data, boolean isSelectable) {
        if (v.getParent() != null && v.getParent() != this) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "The specified child already has a parent. "
                           + "You must call removeView() on the child's parent first.");
            }
        }

        final FixedViewInfo info = new FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
      //1. 添加到尾部集合
        mFooterViewInfos.add(info);
        mAreAllItemsSelectable &= isSelectable;

        // Wrap the adapter if it wasn't already wrapped.
        //2.适配器不为null
        if (mAdapter != null) {
            if (!(mAdapter instanceof HeaderViewListAdapter)) {
               //3.在原来的Adapter上进行一层包装
                wrapHeaderListAdapterInternal();
            }

            // In the case of re-adding a footer view, or adding one later on,
            // we need to notify the observer.
            if (mDataSetObserver != null) {
                mDataSetObserver.onChanged();
            }
        }
    }

  ```
**封装的适配器**
  ```
public class HeaderViewListAdapter implements WrapperListAdapter, Filterable {

    @UnsupportedAppUsage
    private final ListAdapter mAdapter;

    // These two ArrayList are assumed to NOT be null.
    // They are indeed created when declared in ListView and then shared.
    @UnsupportedAppUsage
    ArrayList<ListView.FixedViewInfo> mHeaderViewInfos;
    @UnsupportedAppUsage
    ArrayList<ListView.FixedViewInfo> mFooterViewInfos;

    // Used as a placeholder in case the provided info views are indeed null.
    // Currently only used by some CTS tests, which may be removed.
    static final ArrayList<ListView.FixedViewInfo> EMPTY_INFO_LIST =
        new ArrayList<ListView.FixedViewInfo>();

    boolean mAreAllFixedViewsSelectable;

    private final boolean mIsFilterable;

    public HeaderViewListAdapter(ArrayList<ListView.FixedViewInfo> headerViewInfos,
                                 ArrayList<ListView.FixedViewInfo> footerViewInfos,
                                 ListAdapter adapter) {
        mAdapter = adapter;
        mIsFilterable = adapter instanceof Filterable;

        if (headerViewInfos == null) {
            mHeaderViewInfos = EMPTY_INFO_LIST;
        } else {
            mHeaderViewInfos = headerViewInfos;
        }

        if (footerViewInfos == null) {
            mFooterViewInfos = EMPTY_INFO_LIST;
        } else {
            mFooterViewInfos = footerViewInfos;
        }

        mAreAllFixedViewsSelectable =
                areAllListInfosSelectable(mHeaderViewInfos)
                && areAllListInfosSelectable(mFooterViewInfos);
    }

    public int getHeadersCount() {
        return mHeaderViewInfos.size();
    }

    public int getFootersCount() {
        return mFooterViewInfos.size();
    }

    public boolean isEmpty() {
        return mAdapter == null || mAdapter.isEmpty();
    }

    private boolean areAllListInfosSelectable(ArrayList<ListView.FixedViewInfo> infos) {
        if (infos != null) {
            for (ListView.FixedViewInfo info : infos) {
                if (!info.isSelectable) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean removeHeader(View v) {
        for (int i = 0; i < mHeaderViewInfos.size(); i++) {
            ListView.FixedViewInfo info = mHeaderViewInfos.get(i);
            if (info.view == v) {
                mHeaderViewInfos.remove(i);

                mAreAllFixedViewsSelectable =
                        areAllListInfosSelectable(mHeaderViewInfos)
                        && areAllListInfosSelectable(mFooterViewInfos);

                return true;
            }
        }

        return false;
    }

    public boolean removeFooter(View v) {
        for (int i = 0; i < mFooterViewInfos.size(); i++) {
            ListView.FixedViewInfo info = mFooterViewInfos.get(i);
            if (info.view == v) {
                mFooterViewInfos.remove(i);

                mAreAllFixedViewsSelectable =
                        areAllListInfosSelectable(mHeaderViewInfos)
                        && areAllListInfosSelectable(mFooterViewInfos);

                return true;
            }
        }

        return false;
    }

    public int getCount() {
        if (mAdapter != null) {
            return getFootersCount() + getHeadersCount() + mAdapter.getCount();
        } else {
            return getFootersCount() + getHeadersCount();
        }
    }

    public boolean areAllItemsEnabled() {
        if (mAdapter != null) {
            return mAreAllFixedViewsSelectable && mAdapter.areAllItemsEnabled();
        } else {
            return true;
        }
    }

    public boolean isEnabled(int position) {
        // Header (negative positions will throw an IndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return mHeaderViewInfos.get(position).isSelectable;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.isEnabled(adjPosition);
            }
        }

        // Footer (off-limits positions will throw an IndexOutOfBoundsException)
        return mFooterViewInfos.get(adjPosition - adapterCount).isSelectable;
    }

    public Object getItem(int position) {
        // Header (negative positions will throw an IndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return mHeaderViewInfos.get(position).data;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItem(adjPosition);
            }
        }

        // Footer (off-limits positions will throw an IndexOutOfBoundsException)
        return mFooterViewInfos.get(adjPosition - adapterCount).data;
    }

    public long getItemId(int position) {
        int numHeaders = getHeadersCount();
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemId(adjPosition);
            }
        }
        return -1;
    }

    public boolean hasStableIds() {
        if (mAdapter != null) {
            return mAdapter.hasStableIds();
        }
        return false;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Header (negative positions will throw an IndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return mHeaderViewInfos.get(position).view;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getView(adjPosition, convertView, parent);
            }
        }

        // Footer (off-limits positions will throw an IndexOutOfBoundsException)
        return mFooterViewInfos.get(adjPosition - adapterCount).view;
    }

    public int getItemViewType(int position) {
        int numHeaders = getHeadersCount();
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemViewType(adjPosition);
            }
        }

        return AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
    }

    public int getViewTypeCount() {
        if (mAdapter != null) {
            return mAdapter.getViewTypeCount();
        }
        return 1;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(observer);
        }
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(observer);
        }
    }

    public Filter getFilter() {
        if (mIsFilterable) {
            return ((Filterable) mAdapter).getFilter();
        }
        return null;
    }
    
    public ListAdapter getWrappedAdapter() {
        return mAdapter;
    }
}
  ```
**大致原理**:在原来的mAdapter的基础上，分别向头部和尾部添加view集合，然后重新计算和返回位置和大小等。

### 2.实现可添加首尾部的基类Adapter
**思路：**在基类的adapter中增加HeaderAndFooterAdapter属性，如果用户想添加首尾view则设置为HeaderAndFooterAdapter，如果不添加则直接设置为Adapter。

  ```
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
  ```
### 可添加首尾view的Adapter
  ```
public class HeaderAndFooterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int BASE_ITEM_TYPE_HEADER = 2048;
    private static final int BASE_ITEM_TYPE_FOOTER = 4096;

    private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFooterViews = new SparseArrayCompat<>();
    private RecyclerView.Adapter mAdapter;

    public HeaderAndFooterAdapter(@NonNull RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mHeaderViews.get(viewType) != null) {
            return new RecyclerView.ViewHolder(mHeaderViews.get(viewType)) {
            };
        } else if (mFooterViews.get(viewType) != null) {
            return new RecyclerView.ViewHolder(mFooterViews.get(viewType)) {
            };
        } else {
            return mAdapter.onCreateViewHolder(parent, viewType);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //如果是header 或 footer就不绑定数据
        if (isHeaderViewOrFooterView(position)) {
            return;
        }

        mAdapter.onBindViewHolder(holder, getRealItemPosition(position));
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderView(position)) {
            return mHeaderViews.keyAt(position);
        } else if (isFooterView(position)) {
            return mFooterViews.keyAt(position - getHeadersCount() - getRealItemCount());
        } else {
            return mAdapter.getItemViewType(getRealItemPosition(position));
        }
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + getFootersCount() + getRealItemCount();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (isHeaderViewOrFooterView(position)) {
                        return gridLayoutManager.getSpanCount();
                    } else {
                        if (spanSizeLookup != null) {
                            return spanSizeLookup.getSpanSize(position);
                        }
                    }
                    return 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        mAdapter.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        if (isHeaderViewOrFooterView(position)) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    private boolean isHeaderViewOrFooterView(int position) {
        return isHeaderView(position) || isFooterView(position);
    }

    private int getRealItemPosition(int position) {
        return position - getHeadersCount();
    }

    public boolean isHeaderView(int position) {
        return position < getHeadersCount();
    }

    public boolean isFooterView(int position) {
        return position >= getHeadersCount() + getRealItemCount();
    }

    public void addHeaderView(View view) {
        mHeaderViews.put(mHeaderViews.size() + BASE_ITEM_TYPE_HEADER, view);
    }

    public void addFooterView(View view) {
        mFooterViews.put(mFooterViews.size() + BASE_ITEM_TYPE_FOOTER, view);
    }

    public int getRealItemCount() {
        return mAdapter.getItemCount();
    }

    public int getHeadersCount() {
        return mHeaderViews.size();
    }

    public int getFootersCount() {
        return mFooterViews.size();
    }
}
  ```
####使用
  ```
public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RvAdapter mAdapter;
    private List<String> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < 23; i++) {
            mData.add("" + i);
        }
        mRecyclerView = findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RvAdapter(this);
        View header = LayoutInflater.from(this).inflate(R.layout.header_layout,mRecyclerView,false);
        View footer = LayoutInflater.from(this).inflate(R.layout.footer_layout,mRecyclerView,false);
        mAdapter.setData(mData);
        mAdapter.addHeaderView(header);
       // mAdapter.addHeaderView(footer);
        mAdapter.addFooterView(footer);

        //如果添加了首尾view，则设置这个adapter
        mRecyclerView.setAdapter(mAdapter.getHeaderAndFooterAdapter());

        // 没有添加首尾view，则直接adapter
        // mRecyclerView.setAdapter(mAdapter);

    }
}
  ```

### 效果
![image.png](https://upload-images.jianshu.io/upload_images/22650779-296aa9214eb29dd2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/200)

![image.png](https://upload-images.jianshu.io/upload_images/22650779-1d6627e1f26d6311.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/200)





