package com.chuang.myweibo.home.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chuang.myweibo.R;
import com.chuang.myweibo.home.ActivityCollector;
import com.chuang.myweibo.home.fragment.home.MainFragment;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Chuang on 4-14.
 */
public class CommentActivity extends Activity implements View.OnClickListener {
    //环境
    private Context mContext;
    //文本框
    private EditText editText;
    private String weiboId;
    //
    private TextView username;

    //--------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_comment_layout);
        weiboId = getIntent().getStringExtra("clicked_weiboID");
        ActivityCollector.addActivity(this);
        username = (TextView) findViewById(R.id.toolbar_username);
        username.setText(MainActivity.weiboUserName);
        setupListener();

        editText = (EditText) findViewById(R.id.post_comment_content);
        // 获取编辑框焦点
//        editText.setFocusable(true);
        //打开软键盘
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//        关闭软键盘
//        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
                           public void run() {
                               InputMethodManager inputManager =
                                       (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                               inputManager.showSoftInput(editText, 0);
                           }
                       },
                500);
    }

    private void setupListener() {
        findViewById(R.id.toolbar_back).setOnClickListener(this);
        findViewById(R.id.fab_post).setOnClickListener(this);
    }

    // TODO: 4-18 传入参数，同时转发
    private void postComment() {

        String myComment = editText.getText().toString();
        if (!myComment.equals("") && myComment.length() < 140) {
            MainFragment.mCommentsAPI.create(
                    myComment,//String 评论内容
                    Long.parseLong(weiboId),//long id
                    false,//boolean 当评论转发的微博时，是否评论给原微博
                    new RequestListener() {
                        @Override
                        public void onComplete(String response) {
                            if (!TextUtils.isEmpty(response)) {
                                if (response.startsWith("{\"created_at\"")) {
                                    Toast.makeText(mContext, "评论发送成功", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
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
            Toast.makeText(mContext, "评论不能为空", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_back:
                onBackPressed();
                break;
            case R.id.fab_post:
                postComment();
                break;
        }

    }
}
