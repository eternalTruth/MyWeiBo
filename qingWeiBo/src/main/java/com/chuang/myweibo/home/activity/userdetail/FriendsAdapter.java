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
 * Created by Chuang on 5-6.
 */
public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<User> mDatas = new ArrayList<User>();
    private Context mContext;
    private View mView;

    public FriendsAdapter(ArrayList<User> datas, Context context) {
        this.mDatas = datas;
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(mContext).inflate(R.layout.user_friends_list_item, parent, false);
        FriendViewHolder viewHolder = new FriendViewHolder(mView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FillContent.fillFriendContent(mContext,
                mDatas.get(position),
                ((FriendViewHolder) holder).friendImg,
                ((FriendViewHolder) holder).friendVerf,
                ((FriendViewHolder) holder).friendName,
                ((FriendViewHolder) holder).friend_describe,
                ((FriendViewHolder) holder).profile_comefrom,
                ((FriendViewHolder) holder).friendRelation);
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

    protected class FriendViewHolder extends RecyclerView.ViewHolder {
        public ImageView friendImg;
        public ImageView friendVerf;
        public TextView friendName;
        public TextView friend_describe;
        public TextView profile_comefrom;
        public ImageView friendRelation;

        public FriendViewHolder(View view) {
            super(view);
            friendImg = (ImageView) view.findViewById(R.id.friend_img);
            friendVerf = (ImageView) view.findViewById(R.id.friend_verified);
            friendName = (TextView) view.findViewById(R.id.friend_name);
            friend_describe = (TextView) view.findViewById(R.id.friend_describe);
            profile_comefrom = (TextView) view.findViewById(R.id.profile_comefrom);
            friendRelation = (ImageView) view.findViewById(R.id.friend_relation);
        }
    }
}
