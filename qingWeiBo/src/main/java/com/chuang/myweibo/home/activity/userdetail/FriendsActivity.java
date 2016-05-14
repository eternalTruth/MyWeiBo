package com.chuang.myweibo.home.activity.userdetail;

import android.app.Activity;
import android.content.Context;
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
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.openapi.models.UserList;

import java.util.ArrayList;

/**
 * 展示用户的关注列表
 * Created by Chuang on 5-6.
 */
public class FriendsActivity extends Activity implements View.OnClickListener {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FriendsAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<User> mDatas;
    private boolean mNoMoreData;
    private int mNext_cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_friends);

        initRefreshLayout();
        initRecyclerView();
        setupToolBarListener();
    }

    public void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_friends);
        mAdapter = new FriendsAdapter(mDatas, mContext);
//        mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        //RecyclerViewUtils.setHeaderView(mRecyclerView, new SeachHeadView(mContext));
        //mRecyclerView.addItemDecoration(new WeiboItemSapce((int) mContext.getResources().getDimension(R.dimen.home_weiboitem_space)));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
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
        mNext_cursor = 0;
        MainFragment.mFriendshipsAPI.friends(
                Long.parseLong(MainFragment.mAccessToken.getUid()),
                50,//单页返回的记录数
                mNext_cursor,
                false,
                new RequestListener() {
                    @Override
                    public void onComplete(String response) {
                        //短时间内疯狂请求数据，服务器会返回数据，但是是空数据。为了防止这种情况出现，要在这里要判空
                        if (!TextUtils.isEmpty(response)) {
                            if (NewFeature.CACHE_WEIBOLIST) {
                                SDCardUtil.put(mContext, SDCardUtil.getSDCardPath() + "/qingwb/", "我的关注列表缓存.txt", response);
                            }
                            mNext_cursor = Integer.valueOf(UserList.parse(response).next_cursor);
                            mDatas = UserList.parse(response).usersList;
                            updateList();
                        } else {
                            ToastUtil.showShort(mContext, "网络请求太快，服务器返回空数据，请注意请求频率");
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onWeiboException(WeiboException e) {
                        if (NewFeature.CACHE_MESSAGE_COMMENT) {
                            String response = SDCardUtil.get(mContext, SDCardUtil.getSDCardPath() + "/qingwb/", "我的关注列表缓存.txt");
                            mDatas = UserList.parse(response).usersList;
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
}
