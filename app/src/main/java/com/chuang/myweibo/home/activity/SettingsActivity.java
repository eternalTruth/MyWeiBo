package com.chuang.myweibo.home.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.chuang.myweibo.AccessTokenKeeper;
import com.chuang.myweibo.R;
import com.chuang.myweibo.home.ActivityCollector;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Chuang on 4-14.
 */
public class SettingsActivity extends Activity implements View.OnClickListener {
    //环境
    Context mContext;
    //MD风格对话框
    private MaterialDialog mMaterialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
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
                mMaterialDialog = new MaterialDialog(this);
                mMaterialDialog
                        .setTitle("注意")
                        .setMessage("确认退出微博并清除登录信息？")
                        //可以设置背景mMaterialDialog.setBackgroundResource(R.drawable.background);
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AccessTokenKeeper.clear(mContext);
                                ActivityCollector.finishAll();
                            }
                        })
                        .setNegativeButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMaterialDialog.dismiss();
                            }
                        })
                        //是否在触摸Dialog之外区域时关闭Dialog
                        .setCanceledOnTouchOutside(true)
                        .setOnDismissListener(
                                new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
//                                        Toast.makeText(mContext, "onDismiss",Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .show();


        }
    }
}
