package org.itheima.recycler.widget;

import android.support.v4.widget.SwipeRefreshLayout;

import com.itheima.retrofitutils.ItheimaHttp;
import com.itheima.retrofitutils.Request;
import com.itheima.retrofitutils.listener.HttpResponseListener;

import org.itheima.recycler.R;
import org.itheima.recycler.adapter.BaseLoadMoreRecyclerAdapter;
import org.itheima.recycler.bean.BasePageBean;
import org.itheima.recycler.listener.PullToMoreListener;
import org.itheima.recycler.viewholder.BaseRecyclerViewHolder;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by lyl on 2016/10/7.
 */

public abstract class PullToLoadMoreRecyclerView<HttpResponseBean extends BasePageBean> implements PullToMoreListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ItheimaRecyclerView mRecyclerView;
    private Class<? extends BaseRecyclerViewHolder> mViewHolderClazz;

    public PullToLoadMoreRecyclerView(SwipeRefreshLayout swipeRefreshLayout, ItheimaRecyclerView recyclerView, Class<? extends BaseRecyclerViewHolder> viewHolderClazz) {
        mSwipeRefreshLayout = swipeRefreshLayout;
        mRecyclerView = recyclerView;
        mViewHolderClazz = viewHolderClazz;
        initView();
        initData();
    }

    private void initView() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(getSwipeColorSchemeResources());
            mSwipeRefreshLayout.setOnRefreshListener(this);
        }
    }

    public void setSpanCount(int spanCount) {
        mRecyclerView.setSpanCount(spanCount);
    }


    protected void initData() {
        mLoadMoreRecyclerViewAdapter = new BaseLoadMoreRecyclerAdapter(mRecyclerView, mViewHolderClazz, getItemResId(), null);
        mLoadMoreRecyclerViewAdapter.setPullAndMoreListener(this);
    }


    public int mCurPage = 1;
    public int mPageSize = 20;

    public int mTotalPage = 0;

    public Call mCall;


    public String mCurPageKey = "curPage";
    public String mPageSizeKey = "pageSize";

    private BaseLoadMoreRecyclerAdapter mLoadMoreRecyclerViewAdapter;

    public abstract int getItemResId();

    public abstract String getApi();

    public int[] getSwipeColorSchemeResources() {
        return new int[]{R.color.colorPrimary};
    }


    public Map<String, Object> putParam(String key, Object value) {
        mParamMap.put(key, value);
        return mParamMap;
    }


    @Override
    public void onRefresh() {
        requestData();
    }

    @Override
    public void onRefreshLoadMore(BaseLoadMoreRecyclerAdapter.LoadMoreViewHolder holder) {
        if (mCurPage <= mTotalPage) {
            holder.loading(null);
            requestData(true);
        } else {
            holder.loadingFinish(null);
            pullLoadFinish();
        }
    }

    public void requestData() {
        mCurPage = 1;
        requestData(false);
    }

    Map<String, Object> mParamMap = new HashMap<>();

    private void requestData(final boolean isLoadMore) {
        mParamMap.put(mCurPageKey, String.valueOf(mCurPage));
        mParamMap.put(mPageSizeKey, String.valueOf(mPageSize));
        Request request = ItheimaHttp.newGetRequest(getApi());
        request.putParamsMap(mParamMap);
        mCall = ItheimaHttp.send(request, new HttpResponseListener<HttpResponseBean>() {
            @Override
            public void onResponse(HttpResponseBean responseBean) {
                mTotalPage = responseBean.getTotalPage();
                mCurPage++;
                mLoadMoreRecyclerViewAdapter.addDatas(isLoadMore, responseBean.getItemDatas());
                pullLoadFinish();
                if (mHttpResponseCall != null) {
                    mHttpResponseCall.onResponse(responseBean);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable e) {
                super.onFailure(call, e);
                pullLoadFinish();
                if (mHttpResponseCall != null) {
                    mHttpResponseCall.onFailure(call, e);
                }

            }


            @Override
            public Class getClazz() {
                return PullToLoadMoreRecyclerView.this.getClass();
            }
        });

    }

    private HttpResponseListener<HttpResponseBean> mHttpResponseCall;

    public void setHttpResponseListener(HttpResponseListener<HttpResponseBean> call) {
        mHttpResponseCall = call;
    }

    public void pullLoadFinish() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void free() {
        mRecyclerView = null;
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout = null;

        }
        if (mCall != null) {
            mCall.cancel();
            mCall = null;
        }
        mLoadMoreRecyclerViewAdapter.setPullAndMoreListener(null);
        mLoadMoreRecyclerViewAdapter = null;

        mHttpResponseCall = null;
    }
    /*|||||||||||||||||||||||||||||||||||||||||||||||||||||*/

    public void setCurPage(int curPage) {
        mCurPage = curPage;
    }

    public void setPageSize(int pageSize) {
        mPageSize = pageSize;
    }

    public void setTotalPage(int totalPage) {
        mTotalPage = totalPage;
    }

    public void setCurPageKey(String curPageKey) {
        mCurPageKey = curPageKey;
    }

    public void setPageSizeKey(String pageSizeKey) {
        mPageSizeKey = pageSizeKey;
    }
}