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
import com.chuang.myweibo.myapi.RepostAPI;
import com.chuang.myweibo.utils.HttpUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Chuang on 4-14.
 */
public class MyRepostActivity extends Activity implements View.OnClickListener {
    //环境
    private Context mContext;
    private EditText editText;
    private String weiboId;
    private static final String TAG = "repost";
    //toolbar
    private TextView username;

    //---------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = MyRepostActivity.this;
        setContentView(R.layout.activity_repost_layout);
        weiboId = getIntent().getStringExtra("clicked_weiboID");
        ActivityCollector.addActivity(this);
        username = (TextView) findViewById(R.id.toolbar_username);
        username.setText(MainActivity.weiboUserName);
        setupListener();

        //自动弹出键盘
        editText = (EditText) findViewById(R.id.repost_weibo_content);
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

    // TODO: 4-18 传入参数，同时评论
    private void repostWeibo() {

        String repostWeibo = editText.getText().toString();
        if (repostWeibo.length() < 140) {
            if (repostWeibo.length() == 0) {
                repostWeibo = "转发微博";
            }
            RepostAPI.doRepost(
                    MainFragment.mAccessToken.getToken(),
                    weiboId,
                    repostWeibo,
                    new HttpUtil.CallBack() {
                        @Override
                        public void onComplete(String response) {
                            if (!TextUtils.isEmpty(response)) {
                                if (response.startsWith("{\"created_at\"")) {
                                    //在主线程中操作
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MyRepostActivity.this, "转发成功", Toast.LENGTH_SHORT).show();
                                            onBackPressed();
                                        }
                                    });
                                } else {
                                    Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onError(Exception e) {
//                            e.printStackTrace();
                        }
                    }
            );
        } else {
            Toast.makeText(mContext, "字数超过140限制", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_back:
                onBackPressed();
                break;
            case R.id.fab_post:
                repostWeibo();
                break;
        }
    }
}
