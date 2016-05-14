package com.chuang.myweibo.home.fragment.message;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chuang.myweibo.NewFeature;
import com.chuang.myweibo.R;
import com.chuang.myweibo.home.fragment.message.comment.MyCommentActivity;
import com.chuang.myweibo.home.fragment.message.mention.MentionMeActivity;
import com.chuang.myweibo.utils.ToastUtil;

import java.util.ArrayList;


/**
 * Created by Chuang on 4-4.
 */
public class MessageFragment extends Fragment implements MessageAdapter.OnItemClickListener {
    //环境
    private Context mContext;
    private Activity mActivity;
    private View mView;
    private Intent intent;
    //
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLinearLayoutManager;// TODO: 4-13 使用RecyclerView.LayoutManager
    MessageAdapter mMessageAdapter;
    //item的数据
    ArrayList<Integer> mData;
    ArrayList<String> mText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (NewFeature.LOGIN_STATUS == true) {
            mView = inflater.inflate(R.layout.fragment_message_layout, null);

            mContext = getActivity();
            mActivity = getActivity();

            mText = new ArrayList<String>();
            mText.add("@我的");
            mText.add("评论");
            mText.add("赞");
            mText.add("订阅消息");
            mText.add("未关注的消息");

            mData = new ArrayList<Integer>();
            mData.add(R.drawable.messagescenter_at);
            mData.add(R.drawable.messagescenter_comments);
            mData.add(R.drawable.messagescenter_good);
            mData.add(R.drawable.messagescenter_subscription);
            mData.add(R.drawable.messagescenter_messagebox);

            initRefreshLayout();
            initRecyclerView();

            return mView;
        } else {
            mView = inflater.inflate(R.layout.fragment_message_layout_unlogin, null);
            return mView;
        }
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.messageList);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mMessageAdapter = new MessageAdapter(mContext, mData, mText);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mMessageAdapter);
        // 给Adapter添加点击事件
        mMessageAdapter.setOnItemClickListener(this);

    }

    private void initRefreshLayout() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.message_pulltorefresh);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 5000);//下拉刷新等待5秒钟，5秒内再次下拉无效
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (position){
            case 0:
                ToastUtil.showShort(mContext,"搜索");
                break;
            case 1:
                intent = new Intent(mActivity, MentionMeActivity.class);
                startActivity(intent);
                break;
            case 2:
                intent = new Intent(mActivity, MyCommentActivity.class);
                startActivity(intent);
                break;
            case 3:
                ToastUtil.showShort(mContext,"API暂未支持");
                break;
            case 4:
                ToastUtil.showShort(mContext,"订阅消息");
                break;
            case 5:
                ToastUtil.showShort(mContext,"未关注的消息");
                break;
            default:
                break;
        }
    }
}
