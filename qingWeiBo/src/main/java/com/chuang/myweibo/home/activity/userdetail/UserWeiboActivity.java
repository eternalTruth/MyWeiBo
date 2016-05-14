package com.chuang.myweibo.home.activity.userdetail;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.chuang.myweibo.NewFeature;
import com.chuang.myweibo.R;
import com.chuang.myweibo.home.adapter.WeiboAdapter;
import com.chuang.myweibo.home.fragment.home.MainFragment;
import com.chuang.myweibo.utils.NetUtil;
import com.chuang.myweibo.utils.SharedPreferencesUtil;
import com.chuang.myweibo.utils.ToastUtil;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;

import java.util.ArrayList;

/**
 * 用户微博列表
 * Created by Chuang on 5-7.
 */
public class UserWeiboActivity extends Activity implements WeiboAdapter.OnItemClickListener, View.OnClickListener {
    //环境
    private Context mContext;
    private Activity mActivity;
    //weibo相关类
    public static ArrayList<Status> mDatas; //weibo数据集
    private ArrayList<Status> mWeiBoCache;//weibo本地缓存
    private int mLastVisibleItemPositon;//最后一个可见item的位置，count from 1
    private long lastWeiboID; //最后一条微博的id
    //布局适配器相关类
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private WeiboAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        mActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_weibo);

        initRecyclerView();
        initRefreshLayout();
        setupToolBarListener();
    }

    private void initRecyclerView() {

        mDatas = new ArrayList<Status>();
        mWeiBoCache = new ArrayList<Status>();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_weibo);
//        if (mFirstLoad == true) {
//            //设置item的间隔
//            mRecyclerView.addItemDecoration(new SpaceItemDecoration(DensityUtil.dp2px(mContext, 8)));
//        }
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(30));
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);//设置布局管理器
        mAdapter = new WeiboAdapter(mDatas, mContext);
        mRecyclerView.setAdapter(mAdapter);//设置适配器
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());//设置item添加删除的动画

        // 给Adapter添加点击事件
        mAdapter.setOnItemClickListener(this);

        //RecyclerView添加滑动监听
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                /**
                 * 空闲状态不滑动SCROLL_STATE_IDLE 、触摸滑动状态SCROLL_STATE_DRAGGING和惯性滑动SCROLL_STATE_SETTLING
                 */
                //如果新状态是 停止滑动&&最后一个可见item为适配器的最后一个                          //itemCount=mData.size()+1
                if (newState == RecyclerView.SCROLL_STATE_IDLE && mLastVisibleItemPositon + 1 == mAdapter.getItemCount()) {
                    if (mDatas.size() - 1 < mWeiBoCache.size() && mDatas.size() != 0) {//读取本地缓存数据
                        //-1是因为第一个被空数据占据
                        addDataFromCache(mLastVisibleItemPositon - 1);
                        mAdapter.setData(mDatas);
                        mAdapter.notifyDataSetChanged();
                    } else {//从网络获取微博
                        lastWeiboID = Long.parseLong(mDatas.get(mDatas.size() - 1).id);
                        ToastUtil.showShort(mContext, "本地数据已经被读取完，开始进行网络请求");//上滑刷新
                        pullToLoadMoreDataFromURL();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mLastVisibleItemPositon = mLayoutManager.findLastVisibleItemPosition();
//                LogUtil.d("mLastVisibleItemPositon = " + mLastVisibleItemPositon);
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

    /**
     * 下拉刷新 （从0开始获取
     */
    private void pullToRefreshData() {
        if (NetUtil.isConnected(mContext)) {//若已联网
            if (MainFragment.mAccessToken != null && MainFragment.mAccessToken.isSessionValid()) {//若Token有效
                MainFragment.mStatusesAPI.userTimeline(
                        0,//long since_id 若指定，则返回比指定since_id大（晚）的微博，
                        0,//long max_id 若指定，则返回比max_id小等（早）的微博
                        NewFeature.GET_WEIBO_NUMS,//int count 返回微博条数
                        1,//int page
                        false,//boolean base_app
                        NewFeature.WEIBO_TYPE,//int featureType
                        false,//boolean trim_user
                        new RequestListener() {
                            @Override
                            public void onComplete(String response) {
                                //短时间内疯狂请求数据，服务器会暂时返回空数据，所以在这里要判空
//                                Log.d("6666", "onComplete: "+response);
                                if (!TextUtils.isEmpty(response)) {
                                    SharedPreferencesUtil.put(mContext, "user_weibo", response);
                                    getWeiBoCache();//从SP的第0条开始添加->weiboCache // TODO: 4-6 getWeiBoCache(0)
                                } else {
                                    ToastUtil.showShort(mContext, "网络请求太快，服务器返回空数据，请注意请求频率");
                                }
                                mSwipeRefreshLayout.setRefreshing(false);//不显示刷新动画
                            }

                            @Override
                            public void onWeiboException(WeiboException e) {
                                ErrorInfo info = ErrorInfo.parse(e.getMessage());
                                ToastUtil.showShort(mContext, info.toString());
                                mSwipeRefreshLayout.setRefreshing(false);//不显示刷新动画
                            }
                        });
            } else {
                ToastUtil.showShort(mContext,"登录信息过期，请重新登录");
            }
        } else {
            ToastUtil.showShort(mContext, "没有网络,读取本地缓存");
            getWeiBoCache();//从SP的第0条开始添加->weiboCache
            mSwipeRefreshLayout.setRefreshing(false);//不显示刷新动画
        }
    }

    /**
     * 从网络获取更多微博 （从lastWeiboid开始获取
     */
    private void pullToLoadMoreDataFromURL() {
        if (NetUtil.isConnected(mContext)) {//如果网络已连接
            if (MainFragment.mAccessToken != null && MainFragment.mAccessToken.isSessionValid()) {//AccessToken非空且有效
                //调用API获取好友微博
                MainFragment.mStatusesAPI.userTimeline(
                        0,//long since_id
                        lastWeiboID,//long max_id
                        NewFeature.GET_WEIBO_NUMS,//int count
                        1,//int page
                        false,//boolean base_app
                        NewFeature.WEIBO_TYPE, //int feature_type
                        false,//boolean trim_user
                        new RequestListener() {//RequestListener listener
                            @Override
                            public void onComplete(String response) {
                                ArrayList<Status> status = StatusList.parse(response).statusList;
                                status.remove(0);// TODO: 3-24  需要了解ArrayList，StatusList的结构，
                                mWeiBoCache.addAll(status);
                                addDataFromCache(mLastVisibleItemPositon - 1);
                                mAdapter.setData(mDatas);
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onWeiboException(WeiboException e) {
                                ToastUtil.showShort(mContext, "服务器出现问题！");
                            }
                        });
            }
        } else {//网络未连接
            ToastUtil.showShort(mContext, "请求失败，没有网络");
        }
    }


    /**
     * 从本地缓存SP中拿到数据并且解析到mWeiBoCache,并添加到mData
     * SP->mWeiBoCache->mData
     */
    private void getWeiBoCache() {

        String response = (String) SharedPreferencesUtil.get(mContext, "user_weibo", new String());
        if (response.startsWith("{\"statuses\"")) {
            mWeiBoCache.clear();
            mDatas.clear();
            mWeiBoCache = StatusList.parse(response).statusList;
            mDatas.add(0, new Status());//将索引0的位置占据
            addDataFromCache(0);

            mAdapter.setData(mDatas);
            mAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(mContext, "SP数据错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从mWeiBoCache的start标记开始向mData添加数据
     * mWeiBoCache->mData
     */
    private void addDataFromCache(int start) {
        int count = 0;
        for (int i = start; i < mWeiBoCache.size(); i++) {
            if (start == mWeiBoCache.size()) {//开始位置超过Cache的容量
                ToastUtil.showShort(mContext, "本地缓存已经读取完！");
                break;
            }
            if (count == NewFeature.LOAD_WEIBO_ITEM) {//从start开始添加10条微博即结束
                break;
            }
            mDatas.add(mWeiBoCache.get(i));//添加到mData的末尾
            count++;
        }
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
    public void onItemClick(View view, int position) {

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
