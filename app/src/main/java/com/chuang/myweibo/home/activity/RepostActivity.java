package com.chuang.myweibo.home.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.chuang.myweibo.R;
import com.chuang.myweibo.home.ActivityCollector;

/**
 * Created by Chuang on 4-14.
 */
public class RepostActivity extends Activity implements View.OnClickListener {
    //环境
    private Context mContext;
    private EditText editText;
    private String weiboId;

    //---------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repost_layout);
        weiboId = getIntent().getStringExtra("clicked_weiboId");
        ActivityCollector.addActivity(this);
        setupListener();
    }

    private void setupListener() {
        findViewById(R.id.toolbar_back).setOnClickListener(this);
        findViewById(R.id.fab_post).setOnClickListener(this);
    }

    // TODO: 4-18 传入参数，同时评论
    private void repostWeibo() {
        editText = (EditText) findViewById(R.id.repost_weibo_content);
        String repostWeibo = editText.getText().toString();
        if (repostWeibo!=null&& repostWeibo.length() < 140){
//            MainFragment.mStatusesAPI.

        }else{

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_back:
                onBackPressed();
                break;
            case R.id.fab_post:
                break;
        }
    }
}
