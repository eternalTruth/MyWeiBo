package com.chuang.myweibo.home.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.chuang.myweibo.AccessTokenKeeper;
import com.chuang.myweibo.R;
import com.chuang.myweibo.home.ActivityCollector;

/**
 * Created by Chuang on 4-14.
 */
public class SettingsActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_layout);
        ActivityCollector.addActivity(this);
        setupListener();
    }

    private void setupListener() {
        findViewById(R.id.toolbar_back).setOnClickListener(this);
        findViewById(R.id.exit_weibo).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_back:
                onBackPressed();
                break;
            case R.id.exit_weibo:

                AccessTokenKeeper.clear(this);
                ActivityCollector.finishAll();

        }
    }
}
