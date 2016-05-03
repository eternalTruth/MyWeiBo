package com.chuang.myweibo.home.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chuang.myweibo.AccessTokenKeeper;
import com.chuang.myweibo.Constants;
import com.chuang.myweibo.NewFeature;
import com.chuang.myweibo.R;
import com.chuang.myweibo.home.activity.AuthActivity;
import com.chuang.myweibo.home.activity.CommentActivity;
import com.chuang.myweibo.home.activity.RepostActivity;
import com.chuang.myweibo.home.activity.WeiBoDetails;
import com.chuang.myweibo.home.adapter.WeiboAdapter;
import com.chuang.myweibo.utils.NetUtil;
import com.chuang.myweibo.utils.SharedPreferencesUtil;
import com.chuang.myweibo.utils.ToastUtil;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;

import java.util.ArrayList;


/**
 * Created by Chuang on 4-4.
 */
public class MainFragment extends Fragment implements WeiboAdapter.OnItemClickListener, View.OnClickListener {
    //环境
    private Context mContext;
    private Activity mActivity;
    private View mView;
    private Intent intent;

    //API相关类
    public static Oauth2AccessToken mAccessToken;
    public static StatusesAPI mStatusesAPI;//微博信息API
    public static UsersAPI mUsersAPI;//用户信息API
    public static CommentsAPI mCommentsAPI;//评论信息API

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        mContext = getActivity();

        if (NewFeature.LOGIN_STATUS == true) {//如果登陆状态是true
            mView = inflater.inflate(R.layout.fragment_main_layout, container, false);

            initAccessToken();//实例化AuthInfo,SsoHandler,AccessToken,StatusesAPI,UserAPI对象
            initRefreshLayout();
            initRecyclerView();

            return mView;
        } else {//未登录
            mView = inflater.inflate(R.layout.fragment_main_layout_unlogin, container, false);
            setupToolBarListener();
            return mView;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (NewFeature.LOGIN_STATUS == false) {//如果未登录，show的时候注册toolbar的监听事件
                setupToolBarListener();
            }
        }
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {

        mDatas = new ArrayList<Status>();
        mWeiBoCache = new ArrayList<Status>();

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.weiboRecyclerView);
//        if (mFirstLoad == true) {
//            //设置item的间隔
//            mRecyclerView.addItemDecoration(new SpaceItemDecoration(DensityUtil.dp2px(mContext, 8)));
//        }
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(30));// TODO: 4-6 dp2px
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
                        Log.d("118", "开始读取本地缓存");
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
        mSwipeRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_refresh_widget);
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
     * 从网络获取更多微博 （从lastWeiboid开始获取
     */
    private void pullToLoadMoreDataFromURL() {
        Log.d("171", "从网路获取更多微博");
        if (NetUtil.isConnected(mContext)) {//如果网络已连接
            if (mAccessToken != null && mAccessToken.isSessionValid()) {//AccessToken非空且有效
                //调用API获取好友微博
                Log.d("175", "调用API获取好友微博");
                mStatusesAPI.friendsTimeline(
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
     * 下拉刷新 （从0开始获取
     */
    private void pullToRefreshData() {
        if (NetUtil.isConnected(mContext)) {//若已联网
            if (mAccessToken != null && mAccessToken.isSessionValid()) {//若Token有效
                mStatusesAPI.friendsTimeline(
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
                                if (!TextUtils.isEmpty(response)) {
                                    SharedPreferencesUtil.put(mContext, "weibo", response);
                                    Log.d("213", "从SP向weibocache添加数据");
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
                Log.d("228", "mAccessToken为空或无效");
            }
        } else {
            ToastUtil.showShort(mContext, "没有网络,读取本地缓存");
            getWeiBoCache();//从SP的第0条开始添加->weiboCache
            mSwipeRefreshLayout.setRefreshing(false);//不显示刷新动画
        }
    }

    /**
     * 实例化AuthInfo,SsoHandler,AccessToken,StatusesAPI,UserAPI对象
     */
    private void initAccessToken() {
//        mAuthInfo = new AuthInfo(mContext, Constants.APP_KEY, Constants.REDIRECT_URI, Constants.SCOPE);
//        mSsoHandler = new SsoHandler(mActivity, mAuthInfo);
        mAccessToken = AccessTokenKeeper.readAccessToken(mContext);
        mStatusesAPI = new StatusesAPI(mContext, Constants.APP_KEY, mAccessToken);
        mUsersAPI = new UsersAPI(mContext, Constants.APP_KEY, mAccessToken);
        mCommentsAPI = new CommentsAPI(mContext, Constants.APP_KEY, mAccessToken);

    }

    /**
     * 从本地缓存SP中拿到数据并且解析到mWeiBoCache,并添加到mData
     * SP->mWeiBoCache->mData
     */
    private void getWeiBoCache() {

        String response = (String) SharedPreferencesUtil.get(mContext, "weibo", new String());
        if (response.startsWith("{\"statuses\"")) {
            mWeiBoCache.clear();
            mDatas.clear();
            mWeiBoCache = StatusList.parse(response).statusList;
            mDatas.add(0, new Status());//将索引0的位置占据
            addDataFromCache(0);

            mAdapter.setData(mDatas);
            mAdapter.notifyDataSetChanged();
        }else{
            Toast.makeText(mContext,"SP数据错误",Toast.LENGTH_SHORT).show();
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
    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
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
        mActivity.findViewById(R.id.toolbar_register).setOnClickListener(this);
        mActivity.findViewById(R.id.toolbar_login).setOnClickListener(this);
    }

    /**
     * 处理Adapter点击事件
     *
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            //-------转评赞---------------------------------------
            case R.id.click_redirect:
                intent = new Intent(mActivity, RepostActivity.class);
                intent.putExtra("clicked_weiboID", mDatas.get(position).id);
                startActivity(intent);
                break;
            case R.id.click_comment:

//                if(((TextView)mActivity.findViewById(R.id.comment)).getText().equals("0")){
//                    intent = new Intent(mActivity,WeiBoDetails.class);
//                    intent.putExtra("clicked_")
//                }else {
                intent = new Intent(mActivity, CommentActivity.class);
                intent.putExtra("clicked_weiboID", mDatas.get(position).id);
//                }
                startActivity(intent);
                break;
            case R.id.click_feedlike:
                Toast.makeText(mContext, "赞", Toast.LENGTH_SHORT).show();
                break;
            //-------顶部搜索--------------------------------------
            case R.id.searchview:
                Toast.makeText(mContext, "搜索", Toast.LENGTH_SHORT).show();
                break;
            //-------原创微博的内容---------------------------------
            case R.id.click_original_content:
                Toast.makeText(mContext, "原创微博内容" + position, Toast.LENGTH_SHORT).show();
                intent = new Intent(mActivity, WeiBoDetails.class);
                intent.putExtra("clicked_position", position);
                intent.putExtra("weibo_type", 1);

                startActivity(intent);
                break;
            //-------转发微博的内容----------------------------------
            case R.id.click_retweet_content:
                Toast.makeText(mContext, "转发微博内容" + position, Toast.LENGTH_SHORT).show();
                intent = new Intent(mActivity, WeiBoDetails.class);
                intent.putExtra("clicked_position", position);
                intent.putExtra("weibo_type", 0);

                startActivity(intent);
                break;
            //-------转发微博的原微博内容----------------------------
            case R.id.click_retweet_original_content:
                Toast.makeText(mContext, "转发的原始微博内容" + position, Toast.LENGTH_SHORT).show();
                intent = new Intent(mActivity, WeiBoDetails.class);
                intent.putExtra("clicked_position", position);
                intent.putExtra("weibo_type", 2);

                startActivity(intent);
                break;

        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //-------toolbar--------------------------------------
            case R.id.toolbar_register:
                Toast.makeText(mContext, "注册", Toast.LENGTH_SHORT).show();
                break;
            case R.id.toolbar_login:
                startActivity(new Intent(mContext, AuthActivity.class));
                break;
        }
    }


}