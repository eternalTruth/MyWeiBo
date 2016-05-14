package com.chuang.myweibo.home.activity.weibodetail;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chuang.myweibo.R;
import com.chuang.myweibo.home.adapter.ImageAdapter;
import com.chuang.myweibo.utils.DateUtils;
import com.chuang.myweibo.utils.DensityUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.sina.weibo.sdk.openapi.models.Comment;

import java.util.ArrayList;

/**
 * 在WeiBoDetails里展示评论列表的适配器
 * Created by Chuang on 4-15.
 */
public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //环境
    private Context mContext;
    private Activity mActivty;
    //评论的item类型
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_COMMENT = 2;

    //传递给ViewHolder的view对象
    private View mView;
    private RelativeLayout weiboView;
    //“加载中”动画
    protected AnimationDrawable mFooterImag;
    //图片相关类
    private DisplayImageOptions options;
    private ArrayList<String> mImageDatas;
    private GridLayoutManager gridLayoutManager;
    private ImageAdapter imageAdapter;
    private LinearLayout.LayoutParams mParams;
    //评论相关类
    private ArrayList<Comment> mCommentDatas;

    //构造方法
    public CommentAdapter(ArrayList<Comment> datas, Context context) {
        this.mCommentDatas = datas;
        this.mContext = context;
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_COMMENT) {
            mView = LayoutInflater.from(mContext).inflate(R.layout.comment_item_layout, parent, false);
            CommentViewHolder viewHolder = new CommentViewHolder(mView);
            return viewHolder;
        } else if (viewType == TYPE_FOOTER) {
            mView = LayoutInflater.from(mContext).inflate(R.layout.footerview_loading, null);
            mView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            FooterViewHolder footerViewHolder = new FooterViewHolder(mView);
            ImageView waitingImg = (ImageView) mView.findViewById(R.id.waiting_image);
            mFooterImag = (AnimationDrawable) waitingImg.getDrawable();
            mFooterImag.start();
            return footerViewHolder;
        } else if (viewType == TYPE_HEADER) {

            if (WeiBoDetails.weiboType == 0) {//转发
                mView = LayoutInflater.from(mContext).inflate(R.layout.weibodetail_header_retweet, null);
                LayoutInflater.from(mContext).inflate(R.layout.fragment_main_weiboitem_retweet, weiboView);
            } else if (WeiBoDetails.weiboType == 1) {//原创
                mView = LayoutInflater.from(mContext).inflate(R.layout.weibodetail_header_origin, null);
            } else if (WeiBoDetails.weiboType == 2) {//转发的原始微博
                mView = LayoutInflater.from(mContext).inflate(R.layout.weibodetail_header_origin, null);
            } else {

            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mView.setLayoutParams(params);
            HeaderViewHolder headerViewHolder = new HeaderViewHolder(mView);
            return headerViewHolder;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).comment_id = mCommentDatas.get(position).id;//获取评论的id

            ImageLoader.getInstance().displayImage(
                    mCommentDatas.get(position).user.avatar_hd,
                    ((CommentViewHolder) holder).comment_profile_img,
                    options
            );
            ((CommentViewHolder) holder).comment_profile_name.setText(mCommentDatas.get(position).user.name);
            ((CommentViewHolder) holder).comment_profile_time.setText(DateUtils.translateDate(mCommentDatas.get(position).created_at) + "   ");
            ((CommentViewHolder) holder).comment_comefrom.setText("来自  " + Html.fromHtml(mCommentDatas.get(position).source));
//            ((CommentViewHolder) holder).comment_likes.setText(mCommentDatas.get(position).user.online_status);//评论点赞数官方暂未提供API
            ((CommentViewHolder) holder).comment_content.setText(mCommentDatas.get(position).text);

        } else if (holder instanceof FooterViewHolder) {
            if (getItemCount() == 1) {//adapter中的data set只有1个，则 “加载中 ”不可见
                ((FooterViewHolder) holder).linearLayout.setVisibility(View.GONE);
            } else {//可见
                ((FooterViewHolder) holder).linearLayout.setVisibility(View.VISIBLE);
            }
        } else if (holder instanceof HeaderViewHolder) {
            if (WeiBoDetails.weiboType == 0) {//转发
                //头像
                ImageLoader.getInstance().displayImage(
                        WeiBoDetails.weiboDetail.user.avatar_hd,
                        (ImageView) mView.findViewById(R.id.profile_img),
                        options
                );
                //昵称
                ((TextView) mView.findViewById(R.id.profile_name)).setText(WeiBoDetails.weiboDetail.user.name);
                //时间
                ((TextView) mView.findViewById(R.id.profile_time)).setText(DateUtils.translateDate(WeiBoDetails.weiboDetail.created_at) + "   ");
                //来源
                ((TextView) mView.findViewById(R.id.weiboComeFrom)).setText("来自 " + Html.fromHtml(WeiBoDetails.weiboDetail.source));


                //微博文字内容
                ((TextView) mView.findViewById(R.id.retweet_content)).setText(WeiBoDetails.weiboDetail.text);

                //微博转发，评论，赞的数量
                ((TextView) mView.findViewById(R.id.comment)).setText(WeiBoDetails.weiboDetail.comments_count + "");
                ((TextView) mView.findViewById(R.id.redirect)).setText(WeiBoDetails.weiboDetail.reposts_count + "");
                ((TextView) mView.findViewById(R.id.feedlike)).setText(WeiBoDetails.weiboDetail.attitudes_count + "");

                //转发的原微博文字
                StringBuffer retweetcontent_buffer = new StringBuffer();
                retweetcontent_buffer.setLength(0);
                retweetcontent_buffer.append("@");
                retweetcontent_buffer.append(WeiBoDetails.weiboDetail.retweeted_status.user.name);
                retweetcontent_buffer.append(" :  ");
                retweetcontent_buffer.append(WeiBoDetails.weiboDetail.retweeted_status.text);
                //@昵称为蓝色
                SpannableString sp = new SpannableString(retweetcontent_buffer);
                sp.setSpan(
                        new ForegroundColorSpan(mContext.getResources().getColor(R.color.com_sina_weibo_sdk_blue)),//颜色
                        0,//从下标0开始
                        WeiBoDetails.weiboDetail.retweeted_status.user.name.length() + 2,//结束,包含@号
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                );
                ((TextView) mView.findViewById(R.id.origin_nameAndcontent)).setText(sp);

                //转发的图片

                ((RecyclerView) mView.findViewById(R.id.origin_imageList)).setVisibility(View.GONE);
                ((RecyclerView) mView.findViewById(R.id.origin_imageList)).setVisibility(View.VISIBLE);

                mImageDatas = WeiBoDetails.weiboDetail.retweeted_status.pic_urls_bmiddle;
                gridLayoutManager = new GridLayoutManager(mContext, 3);
                imageAdapter = new ImageAdapter(mImageDatas, mContext);
                ((RecyclerView) mView.findViewById(R.id.origin_imageList)).setHasFixedSize(true);
                ((RecyclerView) mView.findViewById(R.id.origin_imageList)).setAdapter(imageAdapter);
                ((RecyclerView) mView.findViewById(R.id.origin_imageList)).setLayoutManager(gridLayoutManager);
                imageAdapter.setData(mImageDatas);
                ((RecyclerView) mView.findViewById(R.id.origin_imageList)).requestLayout();

                if (mImageDatas != null && mImageDatas.size() != 0) {
                    mParams = (LinearLayout.LayoutParams) ((RecyclerView) mView.findViewById(R.id.origin_imageList)).getLayoutParams();
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
                    ((RecyclerView) mView.findViewById(R.id.origin_imageList)).setLayoutParams(mParams);
                    imageAdapter.notifyDataSetChanged();

                } else {
                    ((RecyclerView) mView.findViewById(R.id.origin_imageList)).setVisibility(View.GONE);
                }
            } else if (WeiBoDetails.weiboType == 1) {//原创
                LayoutInflater.from(mContext).inflate(R.layout.fragment_main_weiboitem_original, weiboView);

                //头像
                ImageLoader.getInstance().displayImage(
                        WeiBoDetails.weiboDetail.user.avatar_hd,
                        (ImageView) mView.findViewById(R.id.profile_img),
                        options
                );
                //昵称
                ((TextView) mView.findViewById(R.id.profile_name)).setText(WeiBoDetails.weiboDetail.user.name);
                //时间
                ((TextView) mView.findViewById(R.id.profile_time)).setText(DateUtils.translateDate(WeiBoDetails.weiboDetail.created_at) + "   ");
                //微博来源
                ((TextView) mView.findViewById(R.id.weiboComeFrom)).setText("来自 " + Html.fromHtml(WeiBoDetails.weiboDetail.source));
                //微博文字内容
                ((TextView) mView.findViewById(R.id.weibo_Content)).setText(WeiBoDetails.weiboDetail.text);
                //微博图片内容
                ((RecyclerView) mView.findViewById(R.id.weibo_image)).setVisibility(View.GONE);// TODO: 4-5 此行删除是否可行
                ((RecyclerView) mView.findViewById(R.id.weibo_image)).setVisibility(View.VISIBLE);
                gridLayoutManager = new GridLayoutManager(mContext, 3);//3列
                mImageDatas = WeiBoDetails.weiboDetail.pic_urls_bmiddle;
                imageAdapter = new ImageAdapter(mImageDatas, mContext);
                ((RecyclerView) mView.findViewById(R.id.weibo_image)).setHasFixedSize(true);// TODO: 4-6
                ((RecyclerView) mView.findViewById(R.id.weibo_image)).setAdapter(imageAdapter);
                ((RecyclerView) mView.findViewById(R.id.weibo_image)).setLayoutManager(gridLayoutManager);// TODO: 4-6
                imageAdapter.setData(mImageDatas);
                ((RecyclerView) mView.findViewById(R.id.weibo_image)).requestLayout();

                if (mImageDatas != null && mImageDatas.size() != 0) {
                    mParams = (LinearLayout.LayoutParams) ((RecyclerView) mView.findViewById(R.id.weibo_image)).getLayoutParams();
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
                    ((RecyclerView) mView.findViewById(R.id.weibo_image)).setLayoutParams(mParams);
                    imageAdapter.notifyDataSetChanged();
                } else {//微博里没有图片
                    ((RecyclerView) mView.findViewById(R.id.weibo_image)).setVisibility(View.GONE);
                }
                //微博转发，评论，赞的数量
                ((TextView) mView.findViewById(R.id.comment)).setText(WeiBoDetails.weiboDetail.comments_count + "");
                ((TextView) mView.findViewById(R.id.redirect)).setText(WeiBoDetails.weiboDetail.reposts_count + "");
                ((TextView) mView.findViewById(R.id.feedlike)).setText(WeiBoDetails.weiboDetail.attitudes_count + "");

            } else if (WeiBoDetails.weiboType == 2) {//转发的原始微博
                //头像
                ImageLoader.getInstance().displayImage(
                        WeiBoDetails.weiboDetail.retweeted_status.user.avatar_hd,
                        (ImageView) mView.findViewById(R.id.profile_img),
                        options
                );
                //昵称
                ((TextView) mView.findViewById(R.id.profile_name)).setText(WeiBoDetails.weiboDetail.retweeted_status.user.name);
                //时间
                ((TextView) mView.findViewById(R.id.profile_time)).setText(DateUtils.translateDate(WeiBoDetails.weiboDetail.retweeted_status.created_at) + "   ");
                //微博来源
                ((TextView) mView.findViewById(R.id.weiboComeFrom)).setText("来自 " + Html.fromHtml(WeiBoDetails.weiboDetail.retweeted_status.source));
                //微博文字内容
                ((TextView) mView.findViewById(R.id.weibo_Content)).setText(WeiBoDetails.weiboDetail.retweeted_status.text);
                //微博图片内容
                ((RecyclerView) mView.findViewById(R.id.weibo_image)).setVisibility(View.GONE);// TODO: 4-5 此行删除是否可行
                ((RecyclerView) mView.findViewById(R.id.weibo_image)).setVisibility(View.VISIBLE);
                gridLayoutManager = new GridLayoutManager(mContext, 3);//3列

                mImageDatas = WeiBoDetails.weiboDetail.retweeted_status.pic_urls_bmiddle;
                imageAdapter = new ImageAdapter(mImageDatas, mContext);
                ((RecyclerView) mView.findViewById(R.id.weibo_image)).setHasFixedSize(true);// TODO: 4-6
                ((RecyclerView) mView.findViewById(R.id.weibo_image)).setAdapter(imageAdapter);
                ((RecyclerView) mView.findViewById(R.id.weibo_image)).setLayoutManager(gridLayoutManager);// TODO: 4-6
                imageAdapter.setData(mImageDatas);
                ((RecyclerView) mView.findViewById(R.id.weibo_image)).requestLayout();

                if (mImageDatas != null && mImageDatas.size() != 0) {
                    mParams = (LinearLayout.LayoutParams) ((RecyclerView) mView.findViewById(R.id.weibo_image)).getLayoutParams();
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
                    ((RecyclerView) mView.findViewById(R.id.weibo_image)).setLayoutParams(mParams);
                    imageAdapter.notifyDataSetChanged();
                } else {//微博里没有图片
                    ((RecyclerView) mView.findViewById(R.id.weibo_image)).setVisibility(View.GONE);
                }
                //微博转发，评论，赞的数量
                ((TextView) mView.findViewById(R.id.comment)).setText(WeiBoDetails.weiboDetail.retweeted_status.comments_count + "");
                ((TextView) mView.findViewById(R.id.redirect)).setText(WeiBoDetails.weiboDetail.retweeted_status.reposts_count + "");
                ((TextView) mView.findViewById(R.id.feedlike)).setText(WeiBoDetails.weiboDetail.retweeted_status.attitudes_count + "");
            }
        }

    }

    @Override
    public int getItemCount() {
        return mCommentDatas.size() + 1;
    }


    public void setCommentData(ArrayList<Comment> data) {
        this.mCommentDatas = data;
        Log.d("84", "已向mData添加数据");
    }

    //===========================================================================
    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private String comment_id;
        private ImageView comment_profile_img;
        private TextView comment_profile_name;
        private TextView comment_profile_time;
        private TextView comment_comefrom;
        private TextView comment_content;
//        ImageView comment_img;//评论中的图片，暂不支持

        public CommentViewHolder(View itemView) {
            super(itemView);
            comment_id = null;
            comment_profile_img = (ImageView) itemView.findViewById(R.id.comment_profile_img);
            comment_profile_name = (TextView) itemView.findViewById(R.id.comment_profile_name);
            comment_profile_time = (TextView) itemView.findViewById(R.id.comment_profile_time);
            comment_comefrom = (TextView) itemView.findViewById(R.id.comment_comefrom);
            comment_content = (TextView) itemView.findViewById(R.id.comment_content);
//            comment_img
        }

    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayout;

        public FooterViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) mView.findViewById(R.id.loadMoreLayout);
        }
    }

    //===========================================================================

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_COMMENT;
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

}



