package com.chuang.myweibo.home.activity.weibodetail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chuang.myweibo.NewFeature;
import com.chuang.myweibo.R;
import com.chuang.myweibo.home.ActivityCollector;
import com.chuang.myweibo.home.fragment.home.MainFragment;
import com.chuang.myweibo.utils.NetUtil;
import com.chuang.myweibo.utils.ToastUtil;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.CommentList;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

/**
 * 微博详情，可查看评论
 * Created by Chuang on 4-9.
 */
public class WeiBoDetails extends Activity implements View.OnClickListener {
    //环境
    private Context mContext;
    private static final String TAG = "WeiBoDetails";

    private int clickedWeiBoPosition;
    public static int weiboType;
    //weibo相关类
    public static Status weiboDetail;
    //评论相关类
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CommentAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private ArrayList<Comment> mCommentDatas;//weibo数据集
    private ArrayList<Comment> mCommentCache;//weibo本地缓存
    private int mLastVisibleItemPositon;//最后一个可见item的位置，count from 1
    private long lastCommentID; //最后一条评论的id
    //--------------------------------------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo_detail_layout);
        ActivityCollector.addActivity(this);

        //获取传递过来的微博位置和微博类型
        Intent intent = getIntent();
        clickedWeiBoPosition = intent.getIntExtra("clicked_position", -1);
        weiboType = intent.getIntExtra("weibo_type", -1);
        weiboDetail = MainFragment.mDatas.get(clickedWeiBoPosition);

        initRecyclerView();
        initRefreshLayout();
        setupListener();

    }

    private void initRecyclerView() {
        mCommentDatas = new ArrayList<Comment>();
        mCommentCache = new ArrayList<Comment>();
        mRecyclerView = (RecyclerView) findViewById(R.id.commentRecyclerView);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(5));
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CommentAdapter(mCommentDatas, mContext);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && mLastVisibleItemPositon + 1 == mAdapter.getItemCount()) {

                    Log.d("12345", "mLastVisibleItemPositon  是" + mLastVisibleItemPositon);
                    Log.d("12345", "mAdapter.getItemCount() 是" + mAdapter.getItemCount());
                    Log.d("12345", "mCommentDatas.size() 是" + mCommentDatas.size());
                    Log.d("12345", "mCommentCache.size()  是" + mCommentCache.size());

                    if (mCommentDatas.size() - 1 < mCommentCache.size() && mCommentDatas.size() != 0) {
                        //读取Cache缓存
                        Log.d(TAG, "onScrollStateChanged: 开始读取Cache缓存");
                        addDataFromCache(mLastVisibleItemPositon - 1);
                        mAdapter.setCommentData(mCommentDatas);
                        mAdapter.notifyDataSetChanged();
                    } else {//从网络获取
                        lastCommentID = Long.parseLong(mCommentDatas.get(mCommentDatas.size() - 1).id);
                        ToastUtil.showShort(mContext, "本地数据已经被读取完，开始进行网络请求");
                        Log.d(TAG, "onScrollStateChanged: 开始从网络获取评论");
                        pullToLoadMoreDataFromURL();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mLastVisibleItemPositon = mLayoutManager.findLastVisibleItemPosition();
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
        //监听下拉动作
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefreshData();
            }
        });
        //初始化时即刷新获取数据
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                pullToRefreshData();
            }
        });

    }

    /**
     * 从网络获取更多评论 （从lastCommentid开始获取
     */
    private void pullToLoadMoreDataFromURL() {
        if (NetUtil.isConnected(mContext)) {//如果网络已连接
            if (MainFragment.mAccessToken != null && MainFragment.mAccessToken.isSessionValid()) {//AccessToken非空且有效
                MainFragment.mCommentsAPI.show(
                        Long.parseLong(weiboDetail.id),//long id
                        0,//long since_id
                        lastCommentID,//long max_id
                        NewFeature.GET_COMMENT_NUMS,//int count
                        1,//int page
                        0,//int authorType
                        new RequestListener() {//RequestListener listener
                            @Override
                            public void onComplete(String response) {
                                ArrayList<Comment> comment = CommentList.parse(response).commentList;
                                comment.remove(0);
                                mCommentCache.addAll(comment);
                                addDataFromCache(mLastVisibleItemPositon - 1);
                                mAdapter.setCommentData(mCommentDatas);
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onWeiboException(WeiboException e) {
                                ToastUtil.showShort(mContext, "服务器出现问题！");
                            }
                        });
            } else {
                ToastUtil.showShort(mContext, "登录信息失效，请重新登录");
            }
        } else {//网络未连接
            ToastUtil.showShort(mContext, "网络请求失败，没有网络");
        }
    }

    private void pullToRefreshData() {
        if (NetUtil.isConnected(mContext)) {
            if (MainFragment.mAccessToken != null && MainFragment.mAccessToken.isSessionValid()) {//若Token有效
                MainFragment.mCommentsAPI.show(
                        Long.parseLong(weiboDetail.id),//long id
                        0,//long since_id
                        0,//long max_id
                        NewFeature.GET_COMMENT_NUMS,//int count
                        1,//int page
                        0,//int authorType
                        new RequestListener() {//RequestListener listener
                            @Override
                            public void onComplete(String response) {
                                if (!TextUtils.isEmpty(response)) {//返回不为空
                                    getCommentCache(response);
                                    Log.d("api", "onComplete: " + response);

                                } else {
                                    ToastUtil.showShort(mContext, "网络请求太快，服务器返回空数据，请注意请求频率");
                                }
                                mSwipeRefreshLayout.setRefreshing(false);//停止刷新动画
                            }

                            @Override
                            public void onWeiboException(WeiboException e) {
                                ErrorInfo info = ErrorInfo.parse(e.getMessage());
                                ToastUtil.showShort(mContext, info.toString());
                                mSwipeRefreshLayout.setRefreshing(false);//不显示刷新动画
                            }
                        }
                );
            }
        } else {

        }
    }

    /**
     * 网络->mCommentCache->mCommentDatas
     *
     * @param response
     */
    private void getCommentCache(String response) {
        mCommentCache.clear();
        mCommentDatas.clear();
        if (response.startsWith("{\"comments\"")) {
            mCommentCache = CommentList.parse(response).commentList;
            mCommentDatas.add(0, new Comment());
            addDataFromCache(0);//从mCommentCache的start标记开始向mCommentData添加数据
        }
        mAdapter.setCommentData(mCommentDatas);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 从mCommentCache的start标记开始向mCommentData添加数据
     * mCommentCache->mCommentData
     */
    private void addDataFromCache(int start) {
        int count = 0;
        for (int i = start; i < mCommentCache.size(); i++) {
            if (start == mCommentCache.size()) {
                ToastUtil.showShort(mContext, "CommentCache已经读取完！");
                break;
            }
            if (count == NewFeature.LOAD_COMMENT_ITEM) {//如果count=20
                break;
            }
            mCommentDatas.add(mCommentCache.get(i));
            count++;
        }
    }


    /**
     * item之间的分隔线
     */
    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.right = space;
            outRect.top = space;
        }
    }

    private void setupListener() {

        findViewById(R.id.toolbar_back).setOnClickListener(this);
        findViewById(R.id.toolbar_more).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_back:
                onBackPressed();
                break;
            case R.id.toolbar_more:
                Toast.makeText(mContext, "更多", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
