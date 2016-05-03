package com.chuang.myweibo.home.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.chuang.myweibo.R;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;

/**
 * Created by Chuang on 4-4.
 */
public class PostFragment extends Fragment implements View.OnClickListener {
    //环境
    private Context mContext;
    private Activity mActivity;
    private View mView;
    //输入的微博文本
    private EditText editText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mActivity = getActivity();
        mView = inflater.inflate(R.layout.fragment_post_layout, null);
        //设置fab的监听
        mView.findViewById(R.id.fab_post).setOnClickListener(this);

        return mView;
    }

    private void postWeibo() {
        editText = (EditText) mActivity.findViewById(R.id.post_weibo_content);
        String postContent = editText.getText().toString();
        if (!postContent.equals("")&&postContent.length()<140) {
            MainFragment.mStatusesAPI.update(
                    postContent,//微博内容
                    null,//纬度
                    null,//经度
                    new RequestListener() {
                        @Override
                        public void onComplete(String response) {
                            if (!TextUtils.isEmpty(response)) {
                                if (response.startsWith("{\"created_at\"")) {
//                                    Status status = Status.parse(response);
                                    Toast.makeText(mContext, "微博发送成功", Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onWeiboException(WeiboException e) {
                            ErrorInfo info = ErrorInfo.parse(e.getMessage());
                            Toast.makeText(mContext, info.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } else {
            Toast.makeText(mContext, "微博内容不能为空", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_post:
                postWeibo();
//                Toast.makeText(mContext, "发送微博", Toast.LENGTH_SHORT).show();
        }
    }
}
