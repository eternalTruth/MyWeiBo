package com.chuang.myweibo.home.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chuang.myweibo.R;
import com.chuang.myweibo.utils.DateUtils;
import com.chuang.myweibo.utils.DensityUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.sina.weibo.sdk.openapi.models.Comment;

import java.util.ArrayList;

/**
 * Created by Chuang on 4-15.
 */
public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //环境
    private Context mContext;
    private Activity mActivty;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_COMMENT = 2;

    //传递给ViewHolder的view对象
    private View mView;
    //“加载中”动画
    protected AnimationDrawable mFooterImag;
    //图片相关类
    private DisplayImageOptions options;
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
            mView = LayoutInflater.from(mContext).inflate(R.layout.headerview_search, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(mContext, 0));
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
            Log.d("id", "onBindViewHolder: " + position + "    " + mCommentDatas.get(position).id);

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

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private String comment_id;
        private ImageView comment_profile_img;
        private TextView comment_profile_name;
        private TextView comment_profile_time;
        private TextView comment_comefrom;
        private TextView comment_likes;
        private TextView comment_content;
//        ImageView comment_img;//评论中的图片，暂不支持

        public CommentViewHolder(View itemView) {
            super(itemView);
            comment_id = null;
            comment_profile_img = (ImageView) itemView.findViewById(R.id.comment_profile_img);
            comment_profile_name = (TextView) itemView.findViewById(R.id.comment_profile_name);
            comment_profile_time = (TextView) itemView.findViewById(R.id.comment_profile_time);
            comment_comefrom = (TextView) itemView.findViewById(R.id.comment_comefrom);
            comment_likes = (TextView) itemView.findViewById(R.id.comment_likes);
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


}



