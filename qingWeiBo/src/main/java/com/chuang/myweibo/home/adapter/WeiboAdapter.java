package com.chuang.myweibo.home.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chuang.myweibo.R;
import com.chuang.myweibo.home.activity.FillContent;
import com.chuang.myweibo.utils.DateUtils;
import com.chuang.myweibo.utils.DensityUtil;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

/**
 * 主页“MainFragment”的微博列表适配器
 * Created by Chuang on 4-5.
 */
public class WeiboAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Context mContext;

    //weibo的类型
    private static final int TYPE_ORINGIN_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_HEADER = 2;
    private static final int TYPE_RETWEET_ITEM = 3;
    private static final int TYPE_RETWEET_DELETE = 4;

    //用于传递给viewholder的view对象
    private View view;

    //“加载中”动画
    protected AnimationDrawable mFooterImag;

    //weibo相关类
    private ArrayList<Status> mData;
    private StringBuffer retweetcontent_buffer = new StringBuffer();

    //图片相关类
    private ArrayList<String> mImageDatas;
    private GridLayoutManager gridLayoutManager;
    private ImageAdapter imageAdapter;
    private LinearLayout.LayoutParams mParams;

    //WeiBoAdapter的构造方法
    public WeiboAdapter(ArrayList<Status> datas, Context context) {
        this.mData = datas;
        this.mContext = context;

        mImageDatas = new ArrayList<String>();
    }

    //------------------创建item的点击事件的接口------------
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
    //-----------------------------------------------------

    //将布局转化为View并传递给ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ORINGIN_ITEM) {//如果是原创类微博
            view = LayoutInflater.from(mContext).inflate(R.layout.fragment_main_weiboitem_original, parent, false);
            OriginViewHolder originViewHolder = new OriginViewHolder(view);
            originViewHolder.imageList.addItemDecoration(new SpaceItemDecoration(DensityUtil.dp2px(mContext, 5)));//设置原创微博图片的间隔
            return originViewHolder;
        } else if (viewType == TYPE_RETWEET_ITEM) {//如果是转发类的微博
            view = LayoutInflater.from(mContext).inflate(R.layout.fragment_main_weiboitem_retweet, parent, false);
            RetweetViewHolder retweetViewHolder = new RetweetViewHolder(view);
            retweetViewHolder.retweet_imageList.addItemDecoration(new SpaceItemDecoration(DensityUtil.dp2px(mContext, 5)));
            return retweetViewHolder;
        } else if (viewType == TYPE_FOOTER) {//如果是底部的“加载中”item
            // TODO: 4-5 试试像上面的方法一样写view
            view = LayoutInflater.from(mContext).inflate(R.layout.footerview_loading, null);
            //// TODO: 4-5 学习ViewGroup.LayoutParams的用法
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            FooterViewHolder footerViewHolder = new FooterViewHolder(view);
            ImageView waitingImg = (ImageView) view.findViewById(R.id.waiting_image);
            mFooterImag = (AnimationDrawable) waitingImg.getDrawable();
            mFooterImag.start();
            return footerViewHolder;
        } else if (viewType == TYPE_HEADER) {//顶部搜索item
            view = LayoutInflater.from(mContext).inflate(R.layout.headerview_search, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(mContext, 40));
            view.setLayoutParams(params);
            SearchViewHolder searchViewHoler = new SearchViewHolder(view);
            return searchViewHoler;
        }

        return null;
    }

    //建立起ViewHolder中数据与视图的关联
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (holder instanceof OriginViewHolder) {
            //uid
            ((OriginViewHolder) holder).uid = mData.get(position).user.idstr;
            //头像和认证
            FillContent.fillProfileImg(mContext,
                    mData.get(position).user,
                    ((OriginViewHolder) holder).profile_img,
                    ((OriginViewHolder) holder).profile_verified_symbol);
            //昵称
            ((OriginViewHolder) holder).profile_name.setText(mData.get(position).user.name);
            //时间
            ((OriginViewHolder) holder).profile_time.setText(DateUtils.translateDate(mData.get(position).created_at) + "    ");
            //微博来源
            ((OriginViewHolder) holder).weibo_comefrom.setText("来自 " + Html.fromHtml(mData.get(position).source));
            //微博文字内容
            ((OriginViewHolder) holder).weibo_content.setText(mData.get(position).text);
            //微博图片内容
            ((OriginViewHolder) holder).imageList.setVisibility(View.GONE);
            ((OriginViewHolder) holder).imageList.setVisibility(View.VISIBLE);
            gridLayoutManager = new GridLayoutManager(mContext, 3);//3列
            mImageDatas = mData.get(position).pic_urls_bmiddle;
            imageAdapter = new ImageAdapter(mImageDatas, mContext);
            ((OriginViewHolder) holder).imageList.setHasFixedSize(true);// TODO: 4-6  
            ((OriginViewHolder) holder).imageList.setAdapter(imageAdapter);
            ((OriginViewHolder) holder).imageList.setLayoutManager(gridLayoutManager);// TODO: 4-6
            imageAdapter.setData(mImageDatas);
            ((OriginViewHolder) holder).imageList.requestLayout();

            if (mImageDatas != null && mImageDatas.size() != 0) {
                mParams = (LinearLayout.LayoutParams) ((OriginViewHolder) holder).imageList.getLayoutParams();
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
                ((OriginViewHolder) holder).imageList.setLayoutParams(mParams);
                imageAdapter.notifyDataSetChanged();
            } else {//微博里没有图片
                ((OriginViewHolder) holder).imageList.setVisibility(View.GONE);
            }
            //微博转发，评论，赞的数量
            ((OriginViewHolder) holder).comment.setText(mData.get(position).comments_count + "");
            ((OriginViewHolder) holder).redirect.setText(mData.get(position).reposts_count + "");
            ((OriginViewHolder) holder).feedlike.setText(mData.get(position).attitudes_count + "");


        } else if (holder instanceof RetweetViewHolder) {
            //微博用户信息
            ((RetweetViewHolder) holder).uid = mData.get(position).user.idstr;
            //头像及认证
            FillContent.fillProfileImg(mContext,
                    mData.get(position).user,
                    ((RetweetViewHolder) holder).profile_img,
                    ((RetweetViewHolder) holder).profile_verified_symbol);

            ((RetweetViewHolder) holder).profile_name.setText(mData.get(position).user.name);
            ((RetweetViewHolder) holder).profile_time.setText(DateUtils.translateDate(mData.get(position).created_at) + "    ");
            ((RetweetViewHolder) holder).weibo_comefrom.setText("来自 " + Html.fromHtml(mData.get(position).source));

            //微博文字内容
            ((RetweetViewHolder) holder).retweet_content.setText(mData.get(position).text);

            //微博转发，评论，赞的数量
            ((RetweetViewHolder) holder).comment.setText(mData.get(position).comments_count + "");
            ((RetweetViewHolder) holder).redirect.setText(mData.get(position).reposts_count + "");
            ((RetweetViewHolder) holder).feedlike.setText(mData.get(position).attitudes_count + "");

            //转发的原微博文字
            // TODO: 4-7 尝试将转发的原博主昵称retweeted_status.user.name提取后蓝色显示出来---已解决
            // TODO: 4-7 尝试点击原博主昵称触发点击事件
            retweetcontent_buffer.setLength(0);
            retweetcontent_buffer.append("@");
            retweetcontent_buffer.append(mData.get(position).retweeted_status.user.name);
//            Log.d("193", mData.get(position).retweeted_status.user.name + " --" + mData.get(position).retweeted_status.user.name.length());
            retweetcontent_buffer.append(" :  ");
            retweetcontent_buffer.append(mData.get(position).retweeted_status.text);
//            ((RetweetViewHolder) holder).origin_nameAndcontent.setText(retweetcontent_buffer.toString());

            //不用buffer直接setText
//            ((RetweetViewHolder) holder).origin_nameAndcontent.setText(
//                    "@" + mData.get(position).retweeted_status.user.name + " :  " + mData.get(position).retweeted_status.text
//            );
            //@昵称为蓝色
            SpannableString sp = new SpannableString(retweetcontent_buffer);
            sp.setSpan(
                    new ForegroundColorSpan(mContext.getResources().getColor(R.color.com_sina_weibo_sdk_blue)),//颜色
                    0,//从下标0开始
                    mData.get(position).retweeted_status.user.name.length() + 2,//结束,包含@号
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            );
            ((RetweetViewHolder) holder).origin_nameAndcontent.setText(sp);

            //转发的图片

            ((RetweetViewHolder) holder).retweet_imageList.setVisibility(View.GONE);
            ((RetweetViewHolder) holder).retweet_imageList.setVisibility(View.VISIBLE);

            mImageDatas = mData.get(position).retweeted_status.pic_urls_bmiddle;
            gridLayoutManager = new GridLayoutManager(mContext, 3);
            imageAdapter = new ImageAdapter(mImageDatas, mContext);
            ((RetweetViewHolder) holder).retweet_imageList.setHasFixedSize(true);
            ((RetweetViewHolder) holder).retweet_imageList.setAdapter(imageAdapter);
            ((RetweetViewHolder) holder).retweet_imageList.setLayoutManager(gridLayoutManager);
            imageAdapter.setData(mImageDatas);
            ((RetweetViewHolder) holder).retweet_imageList.requestLayout();

            if (mImageDatas != null && mImageDatas.size() != 0) {
                mParams = (LinearLayout.LayoutParams) ((RetweetViewHolder) holder).retweet_imageList.getLayoutParams();
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
                ((RetweetViewHolder) holder).retweet_imageList.setLayoutParams(mParams);
                imageAdapter.notifyDataSetChanged();

            } else {
                ((RetweetViewHolder) holder).retweet_imageList.setVisibility(View.GONE);
            }
        } else if (holder instanceof FooterViewHolder) {

            if (getItemCount() == 1) {//adapter中的data set只有1个，则 “加载中 ”不可见
                ((FooterViewHolder) holder).linearLayout.setVisibility(View.GONE);
            } else {//可见
                ((FooterViewHolder) holder).linearLayout.setVisibility(View.VISIBLE);
            }
        } else if (holder instanceof SearchViewHolder) {

        }

    }

    //获取item的数目
    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else if (position == 0) {
            return TYPE_HEADER;
        } else {
            if (mData.get(position).retweeted_status != null && mData.get(position).retweeted_status.user.name != null) {
                return TYPE_RETWEET_ITEM;
            } else {
                return TYPE_ORINGIN_ITEM;
            }
        }
    }

    //-------------------------各种类型的微博ViewHolder--------------------------------------

    /**
     * 原创微博类
     */
    public class OriginViewHolder extends ViewHolder implements View.OnClickListener {
        private String uid;
        private ImageView profile_img;//头像
        private ImageView profile_verified_symbol;
        private TextView profile_name;//昵称
        private TextView profile_time;//发微博的时间
        private TextView weibo_comefrom;//微博来源
        private TextView weibo_content;//微博内容
        private TextView redirect;//转发
        private TextView comment;//评论
        private TextView feedlike;//赞
        private RecyclerView imageList;//图片列表

        public OriginViewHolder(View itemView) {//itemView是layoutInflater转化的布局
            super(itemView);

            profile_img = (ImageView) itemView.findViewById(R.id.profile_img);
            profile_verified_symbol = (ImageView) itemView.findViewById(R.id.profile_verified_symbol);
            profile_name = (TextView) itemView.findViewById(R.id.profile_name);
            profile_time = (TextView) itemView.findViewById(R.id.profile_time);
            weibo_content = (TextView) itemView.findViewById(R.id.weibo_Content);
            weibo_comefrom = (TextView) itemView.findViewById(R.id.weiboComeFrom);
            redirect = (TextView) itemView.findViewById(R.id.redirect);
            comment = (TextView) itemView.findViewById(R.id.comment);
            feedlike = (TextView) itemView.findViewById(R.id.feedlike);
            imageList = (RecyclerView) itemView.findViewById(R.id.weibo_image);

            //原创类微博的监听组件
            itemView.findViewById(R.id.profile_img).setOnClickListener(this);
            itemView.findViewById(R.id.click_original_content).setOnClickListener(this);

            itemView.findViewById(R.id.click_redirect).setOnClickListener(this);
            itemView.findViewById(R.id.click_comment).setOnClickListener(this);
            itemView.findViewById(R.id.click_feedlike).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, getLayoutPosition());
            }
        }
    }

    /**
     * 转发微博类
     */
    public class RetweetViewHolder extends ViewHolder implements View.OnClickListener {
        private String uid;
        private ImageView profile_img;
        private ImageView profile_verified_symbol;
        private TextView profile_name;
        private TextView profile_time;
        private TextView weibo_comefrom;
        private TextView retweet_content;
        private TextView redirect;
        private TextView comment;
        private TextView feedlike;
        private TextView origin_nameAndcontent;
        private RecyclerView retweet_imageList;

        public RetweetViewHolder(View itemView) {
            super(itemView);
            profile_img = (ImageView) itemView.findViewById(R.id.profile_img);
            profile_verified_symbol = (ImageView) itemView.findViewById(R.id.profile_verified_symbol);
            profile_name = (TextView) itemView.findViewById(R.id.profile_name);
            profile_time = (TextView) itemView.findViewById(R.id.profile_time);
            retweet_content = (TextView) itemView.findViewById(R.id.retweet_content);
            weibo_comefrom = (TextView) itemView.findViewById(R.id.weiboComeFrom);
            redirect = (TextView) itemView.findViewById(R.id.redirect);
            comment = (TextView) itemView.findViewById(R.id.comment);
            feedlike = (TextView) itemView.findViewById(R.id.feedlike);
            origin_nameAndcontent = (TextView) itemView.findViewById(R.id.origin_nameAndcontent);
            retweet_imageList = (RecyclerView) itemView.findViewById(R.id.origin_imageList);

            //转发类微博的监听组件
            itemView.findViewById(R.id.profile_img).setOnClickListener(this);
            itemView.findViewById(R.id.click_retweet_content).setOnClickListener(this);
            itemView.findViewById(R.id.click_retweet_original_content).setOnClickListener(this);

            itemView.findViewById(R.id.click_redirect).setOnClickListener(this);
            itemView.findViewById(R.id.click_comment).setOnClickListener(this);
            itemView.findViewById(R.id.click_feedlike).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, getLayoutPosition());
            }
        }
    }

    /**
     * 底部获取更多微博的ViewHolder，上滑出现，包含“加载中”动画
     */
    private class FooterViewHolder extends ViewHolder {
        private LinearLayout linearLayout;

        public FooterViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) view.findViewById(R.id.loadMoreLayout);
        }
    }

    /**
     * 顶部搜索item的ViewHolder
     */
    private class SearchViewHolder extends ViewHolder implements View.OnClickListener {

        public SearchViewHolder(View itemView) {
            super(itemView);
            itemView.findViewById(R.id.searchview).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, getLayoutPosition());
            }
        }
    }
    //----------------------------------------------------------------------------------------

    /**
     * 获得微博图片的行数
     *
     * @return 0，1，2，3
     */
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

    public void setData(ArrayList<Status> data) {
        this.mData = data;
        Log.d("283", "已向mData添加数据");
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
}
