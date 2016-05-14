package com.chuang.myweibo.home.activity.userdetail;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chuang.myweibo.R;
import com.chuang.myweibo.home.activity.FillContent;
import com.sina.weibo.sdk.openapi.models.User;

import java.util.ArrayList;

/**
 * 粉丝列表适配器
 * Created by Chuang on 5-6.
 */
public class FollowerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<User> mDatas = new ArrayList<User>();
    private Context mContext;
    private View mView;

    public FollowerAdapter(ArrayList<User> datas, Context context) {
        this.mDatas = datas;
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(mContext).inflate(R.layout.user_followers_list_item, parent, false);
        FollowerViewHolder viewHolder = new FollowerViewHolder(mView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FillContent.fillFollowContent(mContext,
                mDatas.get(position),
                ((FollowerViewHolder) holder).followerImg,
                ((FollowerViewHolder) holder).followerVerf,
                ((FollowerViewHolder) holder).followerName,
                ((FollowerViewHolder) holder).follower_describe,
                ((FollowerViewHolder) holder).profile_comefrom,
                ((FollowerViewHolder) holder).followerRelation);
    }

    @Override
    public int getItemCount() {
        if (mDatas != null) {
            return mDatas.size();
        } else {
            return 0;
        }
    }


    public void setData(ArrayList<User> data) {
        this.mDatas = data;
    }

    protected class FollowerViewHolder extends RecyclerView.ViewHolder {
        public ImageView followerImg;
        public ImageView followerVerf;
        public TextView followerName;
        public TextView follower_describe;
        public TextView profile_comefrom;
        public ImageView followerRelation;

        public FollowerViewHolder(View view) {
            super(view);
            followerImg = (ImageView) view.findViewById(R.id.follower_img);
            followerVerf = (ImageView) view.findViewById(R.id.follower_verified);
            followerName = (TextView) view.findViewById(R.id.follower_name);
            follower_describe = (TextView) view.findViewById(R.id.follower_describe);
            profile_comefrom = (TextView) view.findViewById(R.id.profile_comefrom);
            followerRelation = (ImageView) view.findViewById(R.id.follower_relation);
        }
    }
}
