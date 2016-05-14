package com.chuang.myweibo.home.fragment.message.mention;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.chuang.myweibo.NewFeature;
import com.chuang.myweibo.R;
import com.chuang.myweibo.home.fragment.home.MainFragment;
import com.chuang.myweibo.utils.SDCardUtil;
import com.chuang.myweibo.utils.ToastUtil;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;

import java.util.ArrayList;

/**
 * Created by Chuang on 5-6.
 */
public class MentionMeActivity extends Activity implements View.OnClickListener, MentionMeAdapter.OnItemClickListener {
    private Context mContext;
    private ArrayList<Status> mDatas;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private MentionMeAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private int lastVisibleItemPosition;
    private int currentScrollState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_mentionlist_layout);

        initRecyclerView();
        initRefreshLayout();
        setupToolBarListener();
    }

    public void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_mention);
        mAdapter = new MentionMeAdapter(mContext, mDatas);
        mAdapter.setOnItemClickListener(this);
//        mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        //RecyclerViewUtils.setHeaderView(mRecyclerView, new SeachHeadView(mContext));
        //mRecyclerView.addItemDecoration(new WeiboItemSapce((int) mContext.getResources().getDimension(R.dimen.home_weiboitem_space)));
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(30));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                currentScrollState = newState;
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                if ((visibleItemCount > 0 && currentScrollState == RecyclerView.SCROLL_STATE_IDLE && (lastVisibleItemPosition) >= totalItemCount - 1)) {
//                    onLoadNextPage(recyclerView);
                    // TODO: 5-8 请求更多数据
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

            }
        });
    }

    /**
     * 初始化下拉刷新的RefreshLayout
     */
    private void initRefreshLayout() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //初始化时获取微博数据
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                pullToRefreshData();
            }
        });

        //监听刷新动作
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefreshData();
            }
        });
    }

    public void pullToRefreshData() {
        mSwipeRefreshLayout.setRefreshing(true);
//        mNext_cursor = 0;
        MainFragment.mStatusesAPI.mentions(
                0L,//since_id
                0L,//max_id
                20,//count 单页返回的记录数
                1,//page
                0,//authorType
                0,//sourceType
                0,//filterType
                true,//boolean trim_user false返回完整user字段，true只返回user_id
                new RequestListener() {
                    @Override
                    public void onComplete(String response) {
                        //短时间内疯狂请求数据，服务器会返回数据，但是是空数据。为了防止这种情况出现，要在这里要判空
                        if (!TextUtils.isEmpty(response)) {
                            if (NewFeature.CACHE_MESSAGE_MENTION) {
                                SDCardUtil.put(mContext, SDCardUtil.getSDCardPath() + "/qingwb/", "message_mention.txt", response);
                            }
//                            mNext_cursor = Integer.valueOf(UserList.parse(response).next_cursor);
                            mDatas = StatusList.parse(response).statusList;
                            updateList();
                        } else {
                            ToastUtil.showShort(mContext, "网络请求太快，服务器返回空数据，请注意请求频率");
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onWeiboException(WeiboException e) {
                        if (NewFeature.CACHE_MESSAGE_COMMENT) {
                            String response = SDCardUtil.get(mContext, SDCardUtil.getSDCardPath() + "/qingwb/", "message_mention    .txt");
                            mDatas = StatusList.parse(response).statusList;
                            updateList();
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    public void updateList() {
//        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mAdapter.setData(mDatas);
//        mHeaderAndFooterRecyclerViewAdapter.notifyDataSetChanged();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 设置item的间隔及样式
     */
    private class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.bottom = space;
        }

    }

    private void setupToolBarListener() {
        findViewById(R.id.toolbar_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            case R.id.message_center_content:
                ToastUtil.showShort(mContext, "跳转");
                break;
            default:
                break;
        }
    }
}
