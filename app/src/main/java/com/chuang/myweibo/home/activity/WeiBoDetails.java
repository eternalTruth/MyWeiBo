package com.chuang.myweibo.home.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chuang.myweibo.NewFeature;
import com.chuang.myweibo.R;
import com.chuang.myweibo.home.ActivityCollector;
import com.chuang.myweibo.home.adapter.CommentAdapter;
import com.chuang.myweibo.home.adapter.ImageAdapter;
import com.chuang.myweibo.home.fragment.MainFragment;
import com.chuang.myweibo.utils.DateUtils;
import com.chuang.myweibo.utils.DensityUtil;
import com.chuang.myweibo.utils.NetUtil;
import com.chuang.myweibo.utils.ToastUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.CommentList;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

/**
 * Created by Chuang on 4-9.
 */
public class WeiBoDetails extends Activity implements View.OnClickListener {
    //环境
    private Context mContext;
    private static final String TAG = "123";

    private int clickedWeiBoPosition;
    private int weiboType;
    private RelativeLayout weiboView;
    //weibo相关类
    private Status weiboDetail;
    //评论相关类
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CommentAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private ArrayList<Comment> mCommentDatas;//weibo数据集
    private ArrayList<Comment> mCommentCache;//weibo本地缓存
    private int mLastVisibleItemPositon;//最后一个可见item的位置，count from 1
    private long lastCommentID; //最后一条评论的id
    //图片相关类
    private DisplayImageOptions options;
    private ArrayList<String> mImageDatas;
    private GridLayoutManager gridLayoutManager;
    private ImageAdapter imageAdapter;
    private LinearLayout.LayoutParams mParams;
    //--------------------------------------------------------------------------

    //构造方法
    public WeiBoDetails() {
        mContext = this;
        //设置图片的默认属性
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.avator_default)
                .showImageForEmptyUri(R.drawable.avator_default)
                .showImageOnFail(R.drawable.avator_default)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new CircleBitmapDisplayer(14671839, 1))//加载圆形图片14671839半透明灰色
                .build();
    }

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
        setWeiBoDetail(weiboType);

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


    //根据微博类型显示相应布局
    private void setWeiBoDetail(int weibotype) {
        weiboView = (RelativeLayout) findViewById(R.id.detail_weibo_layout);
        switch (weibotype) {
            case 0://转发微博
                LayoutInflater.from(this).inflate(R.layout.fragment_main_weiboitem_retweet, weiboView);

                //头像
                ImageLoader.getInstance().displayImage(
                        weiboDetail.user.avatar_hd,
                        (ImageView) findViewById(R.id.profile_img),
                        options
                );
                //昵称
                ((TextView) findViewById(R.id.profile_name)).setText(weiboDetail.user.name);
                //时间
                ((TextView) findViewById(R.id.profile_time)).setText(DateUtils.translateDate(weiboDetail.created_at) + "   ");
                //来源
                ((TextView) findViewById(R.id.weiboComeFrom)).setText("来自 " + Html.fromHtml(weiboDetail.source));

                //微博文字内容
                ((TextView) findViewById(R.id.retweet_content)).setText(weiboDetail.text);

                //微博转发，评论，赞的数量
                ((TextView) findViewById(R.id.comment)).setText(weiboDetail.comments_count + "");
                ((TextView) findViewById(R.id.redirect)).setText(weiboDetail.reposts_count + "");
                ((TextView) findViewById(R.id.feedlike)).setText(weiboDetail.attitudes_count + "");

                //转发的原微博文字
                StringBuffer retweetcontent_buffer = new StringBuffer();
                retweetcontent_buffer.setLength(0);
                retweetcontent_buffer.append("@");
                retweetcontent_buffer.append(weiboDetail.retweeted_status.user.name);
                retweetcontent_buffer.append(" :  ");
                retweetcontent_buffer.append(weiboDetail.retweeted_status.text);
                //@昵称为蓝色
                SpannableString sp = new SpannableString(retweetcontent_buffer);
                sp.setSpan(
                        new ForegroundColorSpan(mContext.getResources().getColor(R.color.com_sina_weibo_sdk_blue)),//颜色
                        0,//从下标0开始
                        weiboDetail.retweeted_status.user.name.length() + 2,//结束,包含@号
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                );
                ((TextView) findViewById(R.id.origin_nameAndcontent)).setText(sp);

                //转发的图片

                ((RecyclerView) findViewById(R.id.origin_imageList)).setVisibility(View.GONE);
                ((RecyclerView) findViewById(R.id.origin_imageList)).setVisibility(View.VISIBLE);

                mImageDatas = weiboDetail.retweeted_status.pic_urls_bmiddle;
                gridLayoutManager = new GridLayoutManager(mContext, 3);
                imageAdapter = new ImageAdapter(mImageDatas, mContext);
                ((RecyclerView) findViewById(R.id.origin_imageList)).setHasFixedSize(true);
                ((RecyclerView) findViewById(R.id.origin_imageList)).setAdapter(imageAdapter);
                ((RecyclerView) findViewById(R.id.origin_imageList)).setLayoutManager(gridLayoutManager);
                imageAdapter.setData(mImageDatas);
                ((RecyclerView) findViewById(R.id.origin_imageList)).requestLayout();

                if (mImageDatas != null && mImageDatas.size() != 0) {
                    mParams = (LinearLayout.LayoutParams) ((RecyclerView) findViewById(R.id.origin_imageList)).getLayoutParams();
                    mParams.width =
                            (DensityUtil.dp2px(mContext, 110f)) * 3 + (DensityUtil.dp2px(mContext, 5f)) * 2;
                    mParams.height =
                            (DensityUtil.dp2px(mContext, 110f)) * getImgLineCount(mImageDatas) +
                                    (DensityUtil.dp2px(mContext, 5f)) * getImgLineCount(mImageDatas);
                    mParams.setMargins(// TODO: 4-6
                            DensityUtil.dp2px(mContext, 8),//left
                            DensityUtil.dp2px(mContext, -5),//top
                            DensityUtil.dp2px(mContext, 8),//right
                            DensityUtil.dp2px(mContext, 8)//bottom
                    );
                    ((RecyclerView) findViewById(R.id.origin_imageList)).setLayoutParams(mParams);
                    imageAdapter.notifyDataSetChanged();

                } else {
                    ((RecyclerView) findViewById(R.id.origin_imageList)).setVisibility(View.GONE);
                }

                break;
            case 1://原创微博
                LayoutInflater.from(this).inflate(R.layout.fragment_main_weiboitem_original, weiboView);

                //头像
                ImageLoader.getInstance().displayImage(
                        weiboDetail.user.avatar_hd,
                        (ImageView) findViewById(R.id.profile_img),
                        options
                );
                //昵称
                ((TextView) findViewById(R.id.profile_name)).setText(weiboDetail.user.name);
                //时间
                ((TextView) findViewById(R.id.profile_time)).setText(DateUtils.translateDate(weiboDetail.created_at) + "   ");
                //微博来源
                ((TextView) findViewById(R.id.weiboComeFrom)).setText("来自 " + Html.fromHtml(weiboDetail.source));
                //微博文字内容
                ((TextView) findViewById(R.id.weibo_Content)).setText(weiboDetail.text);
                //微博图片内容
                ((RecyclerView) findViewById(R.id.weibo_image)).setVisibility(View.GONE);// TODO: 4-5 此行删除是否可行
                ((RecyclerView) findViewById(R.id.weibo_image)).setVisibility(View.VISIBLE);
                gridLayoutManager = new GridLayoutManager(mContext, 3);//3列
                mImageDatas = weiboDetail.pic_urls_bmiddle;
                imageAdapter = new ImageAdapter(mImageDatas, mContext);
                ((RecyclerView) findViewById(R.id.weibo_image)).setHasFixedSize(true);// TODO: 4-6
                ((RecyclerView) findViewById(R.id.weibo_image)).setAdapter(imageAdapter);
                ((RecyclerView) findViewById(R.id.weibo_image)).setLayoutManager(gridLayoutManager);// TODO: 4-6
                imageAdapter.setData(mImageDatas);
                ((RecyclerView) findViewById(R.id.weibo_image)).requestLayout();

                if (mImageDatas != null && mImageDatas.size() != 0) {
                    mParams = (LinearLayout.LayoutParams) ((RecyclerView) findViewById(R.id.weibo_image)).getLayoutParams();
                    mParams.height =
                            (DensityUtil.dp2px(mContext, 110f)) * getImgLineCount(mImageDatas) +
                                    (DensityUtil.dp2px(mContext, 5f)) * getImgLineCount(mImageDatas);
                    mParams.width =
                            (DensityUtil.dp2px(mContext, 110f)) * 3 + (DensityUtil.dp2px(mContext, 5f)) * 2;
                    mParams.setMargins(// TODO: 4-6
                            DensityUtil.dp2px(mContext, 8),//left
                            DensityUtil.dp2px(mContext, -5),//top
                            DensityUtil.dp2px(mContext, 8),//right
                            DensityUtil.dp2px(mContext, 8)//bottom
                    );
                    ((RecyclerView) findViewById(R.id.weibo_image)).setLayoutParams(mParams);
                    imageAdapter.notifyDataSetChanged();
                } else {//微博里没有图片
                    ((RecyclerView) findViewById(R.id.weibo_image)).setVisibility(View.GONE);
                }
                //微博转发，评论，赞的数量
                ((TextView) findViewById(R.id.comment)).setText(weiboDetail.comments_count + "");
                ((TextView) findViewById(R.id.redirect)).setText(weiboDetail.reposts_count + "");
                ((TextView) findViewById(R.id.feedlike)).setText(weiboDetail.attitudes_count + "");


                break;
            case 2://转发的原始微博
                LayoutInflater.from(this).inflate(R.layout.fragment_main_weiboitem_original, weiboView);


                //头像
                ImageLoader.getInstance().displayImage(
                        weiboDetail.retweeted_status.user.avatar_hd,
                        (ImageView) findViewById(R.id.profile_img),
                        options
                );
                //昵称
                ((TextView) findViewById(R.id.profile_name)).setText(weiboDetail.retweeted_status.user.name);
                //时间
                ((TextView) findViewById(R.id.profile_time)).setText(DateUtils.translateDate(weiboDetail.retweeted_status.created_at) + "   ");
                //微博来源
                ((TextView) findViewById(R.id.weiboComeFrom)).setText("来自 " + Html.fromHtml(weiboDetail.retweeted_status.source));
                //微博文字内容
                ((TextView) findViewById(R.id.weibo_Content)).setText(weiboDetail.retweeted_status.text);
                //微博图片内容
                ((RecyclerView) findViewById(R.id.weibo_image)).setVisibility(View.GONE);// TODO: 4-5 此行删除是否可行
                ((RecyclerView) findViewById(R.id.weibo_image)).setVisibility(View.VISIBLE);
                gridLayoutManager = new GridLayoutManager(mContext, 3);//3列

                mImageDatas = weiboDetail.retweeted_status.pic_urls_bmiddle;
                imageAdapter = new ImageAdapter(mImageDatas, mContext);
                ((RecyclerView) findViewById(R.id.weibo_image)).setHasFixedSize(true);// TODO: 4-6
                ((RecyclerView) findViewById(R.id.weibo_image)).setAdapter(imageAdapter);
                ((RecyclerView) findViewById(R.id.weibo_image)).setLayoutManager(gridLayoutManager);// TODO: 4-6
                imageAdapter.setData(mImageDatas);
                ((RecyclerView) findViewById(R.id.weibo_image)).requestLayout();

                if (mImageDatas != null && mImageDatas.size() != 0) {
                    mParams = (LinearLayout.LayoutParams) ((RecyclerView) findViewById(R.id.weibo_image)).getLayoutParams();
                    mParams.height =
                            (DensityUtil.dp2px(mContext, 110f)) * getImgLineCount(mImageDatas) +
                                    (DensityUtil.dp2px(mContext, 5f)) * getImgLineCount(mImageDatas);
                    mParams.width =
                            (DensityUtil.dp2px(mContext, 110f)) * 3 + (DensityUtil.dp2px(mContext, 5f)) * 2;
                    mParams.setMargins(// TODO: 4-6
                            DensityUtil.dp2px(mContext, 8),//left
                            DensityUtil.dp2px(mContext, -5),//top
                            DensityUtil.dp2px(mContext, 8),//right
                            DensityUtil.dp2px(mContext, 8)//bottom
                    );
                    ((RecyclerView) findViewById(R.id.weibo_image)).setLayoutParams(mParams);
                    imageAdapter.notifyDataSetChanged();
                } else {//微博里没有图片
                    ((RecyclerView) findViewById(R.id.weibo_image)).setVisibility(View.GONE);
                }
                //微博转发，评论，赞的数量
                ((TextView) findViewById(R.id.comment)).setText(weiboDetail.retweeted_status.comments_count + "");
                ((TextView) findViewById(R.id.redirect)).setText(weiboDetail.retweeted_status.reposts_count + "");
                ((TextView) findViewById(R.id.feedlike)).setText(weiboDetail.retweeted_status.attitudes_count + "");

                break;

            default://未获取到或错误的微博类型
                break;

        }
    }

    private int getImgLineCount(ArrayList<String> mImageDatas) {
        int count = mImageDatas.size();
        if (count <= 3) {
            return 1;
        } else if (count <= 6) {
            return 2;
        } else if (count <= 9) {
            return 3;
        }
        return 0;
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
