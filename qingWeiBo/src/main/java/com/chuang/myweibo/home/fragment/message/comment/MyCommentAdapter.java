package com.chuang.myweibo.home.fragment.message.comment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chuang.myweibo.R;
import com.chuang.myweibo.home.activity.FillContent;
import com.sina.weibo.sdk.openapi.models.Comment;

import java.util.ArrayList;


/**
 * 消息里的评论适配器
 * Created by Chuang on 5-6.
 */
public class MyCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<Comment> mDatas;
    private View mView;

    public MyCommentAdapter(Context context, ArrayList<Comment> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    //------------------创建item的点击事件的接口------------
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(mContext).inflate(R.layout.activity_message_commentlist_item, parent, false);
        CommentViewHolder commentViewHolder = new CommentViewHolder(mView);
        return commentViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FillContent.fillTitleBar(
                mContext,
                mDatas.get(position),
                ((CommentViewHolder) holder).profile_img,
                ((CommentViewHolder) holder).profile_verified,
                ((CommentViewHolder) holder).profile_name,
                ((CommentViewHolder) holder).profile_time,
                ((CommentViewHolder) holder).weibo_comefrom);
        //微博内容
        FillContent.fillWeiBoContent(
                mDatas.get(position).text,
                ((CommentViewHolder) holder).mention_content);
        FillContent.FillCenterContent(mDatas.get(position).status,
                ((CommentViewHolder) holder).mentionitem_img,
                ((CommentViewHolder) holder).mentionitem_name,
                ((CommentViewHolder) holder).mentionitem_content);
    }

    @Override
    public int getItemCount() {
        if (mDatas != null) {
            return mDatas.size();
        } else {
            return 0;
        }
    }

    public void setData(ArrayList<Comment> data) {
        this.mDatas = data;
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout comment_layout;
        public ImageView profile_img;
        public ImageView profile_verified;
        public TextView profile_name;
        public TextView profile_time;
        public TextView weibo_comefrom;
        public ImageView popover_arrow;

        public TextView mention_content;

        //长方形内的内容
        public ImageView mentionitem_img;
        public TextView mentionitem_name;
        public TextView mentionitem_content;

        public CommentViewHolder(View v) {
            super(v);
//            comment_layout = (LinearLayout) v.findViewById(R.id.comment_layout);
            profile_img = (ImageView) v.findViewById(R.id.profile_img);
            profile_verified = (ImageView) v.findViewById(R.id.profile_verified_symbol);
            profile_name = (TextView) v.findViewById(R.id.profile_name);
            profile_time = (TextView) v.findViewById(R.id.profile_time);
            weibo_comefrom = (TextView) v.findViewById(R.id.weiboComeFrom);
//            popover_arrow = (ImageView) v.findViewById(R.id.popover_arrow);

            mention_content = (TextView) v.findViewById(R.id.mention_content);

            mentionitem_img = (ImageView) v.findViewById(R.id.mentionitem_img);
            mentionitem_name = (TextView) v.findViewById(R.id.mentionitem_name);
            mentionitem_content = (TextView) v.findViewById(R.id.mentionitem_content);
        }
    }
}
