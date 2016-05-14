package com.chuang.myweibo.home.fragment.message.mention;

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
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

/**
 * Created by Chuang on 5-6.
 */
public class MentionMeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<Status> mDatas;
    private View mView;

    public MentionMeAdapter(Context context, ArrayList<Status> datas) {
        this.mDatas = datas;
        this.mContext = context;
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
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(mContext).inflate(R.layout.activity_message_mentionlist_item, parent, false);
        MentionViewHolder mentionViewHolder = new MentionViewHolder(mView);
        return mentionViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //顶部用户信息
        FillContent.fillTitleBar(
                mContext,
                mDatas.get(position),
                ((MentionViewHolder) holder).profile_img,
                ((MentionViewHolder) holder).profile_verified,
                ((MentionViewHolder) holder).profile_name,
                ((MentionViewHolder) holder).profile_time,
                ((MentionViewHolder) holder).weibo_comefrom);
        //微博内容
        FillContent.fillWeiBoContent(
                mDatas.get(position).text,
                ((MentionViewHolder) holder).mention_content);
        //中心内容
        FillContent.FillCenterContent(
                mDatas.get(position).retweeted_status,
                ((MentionViewHolder) holder).mentionitem_img,
                ((MentionViewHolder) holder).mentionitem_name,
                ((MentionViewHolder) holder).mentionitem_content);
        //转评赞
        FillContent.fillBottomBar(
                mDatas.get(position),
                ((MentionViewHolder) holder).redirect,
                ((MentionViewHolder) holder).comment,
                ((MentionViewHolder) holder).feedlike
        );
    }

    @Override
    public int getItemCount() {
        if (mDatas != null) {
            return mDatas.size();
        } else {
            return 0;
        }
    }

    public void setData(ArrayList<Status> data) {
        this.mDatas = data;
    }

    public class MentionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //titleBar
        public ImageView profile_img;
        public ImageView profile_verified;
        public TextView profile_name;
        public TextView profile_time;
        public TextView weibo_comefrom;
        //提到我的内容
        public TextView mention_content;

        //长方形内的内容
        public LinearLayout message_center_content;
        public ImageView mentionitem_img;
        public TextView mentionitem_name;
        public TextView mentionitem_content;

        //转评赞
        public TextView redirect;
        public TextView comment;
        public TextView feedlike;

        public MentionViewHolder(View v) {
            super(v);

            profile_img = (ImageView) v.findViewById(R.id.profile_img);
            profile_verified = (ImageView) v.findViewById(R.id.profile_verified_symbol);
            profile_name = (TextView) v.findViewById(R.id.profile_name);
            profile_time = (TextView) v.findViewById(R.id.profile_time);
            weibo_comefrom = (TextView) v.findViewById(R.id.weiboComeFrom);

            mention_content = (TextView) v.findViewById(R.id.mention_content);

            message_center_content = (LinearLayout) v.findViewById(R.id.message_center_content);
            mentionitem_img = (ImageView) v.findViewById(R.id.mentionitem_img);
            mentionitem_name = (TextView) v.findViewById(R.id.mentionitem_name);
            mentionitem_content = (TextView) v.findViewById(R.id.mentionitem_content);

            redirect = (TextView) v.findViewById(R.id.redirect);
            comment = (TextView) v.findViewById(R.id.comment);
            feedlike = (TextView) v.findViewById(R.id.feedlike);

            message_center_content.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, getLayoutPosition());
            }
        }
    }
}
