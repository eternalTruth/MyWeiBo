package com.chuang.myweibo.home.activity;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chuang.myweibo.AccessTokenKeeper;
import com.chuang.myweibo.NewFeature;
import com.chuang.myweibo.R;
import com.chuang.myweibo.home.ActivityCollector;
import com.chuang.myweibo.home.fragment.DiscoverFragment;
import com.chuang.myweibo.home.fragment.MainFragment;
import com.chuang.myweibo.home.fragment.MessageFragment;
import com.chuang.myweibo.home.fragment.PostFragment;
import com.chuang.myweibo.home.fragment.ProfileFragment;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private Context mContext;
    private String TAG = "mainActivity";

    public static String weiboUserName;


    private Oauth2AccessToken mAccessToken;

    //碎片及碎片管理器
    private FragmentManager mFragmentManager;

    private MainFragment mMainFragment;
    private MessageFragment mMessageFragment;
    private PostFragment mPostFragment;
    private DiscoverFragment mDiscoverFragment;
    private ProfileFragment mProfileFragment;

    //顶部toolbar
    private LinearLayout mToolBar;
    private TextView mUserName;

    //底部导航
    private static final int HOME = 0;
    private static final int MESSAGE = 1;
    private static final int POST = 2;
    private static final int DISCOVERY = 3;
    private static final int PROFILE = 4;
    private RelativeLayout mHomeTab, mMessageTab, mPostTab, mDiscoveryTab, mProfileTab;
    //--------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_layout);

        mContext = this;

        mHomeTab = (RelativeLayout) findViewById(R.id.tab_home);
        mMessageTab = (RelativeLayout) findViewById(R.id.tab_message);
        mPostTab = (RelativeLayout) findViewById(R.id.tab_post);
        mDiscoveryTab = (RelativeLayout) findViewById(R.id.tab_discovery);
        mProfileTab = (RelativeLayout) findViewById(R.id.tab_profile);


        ActivityCollector.addActivity(this);
        checkSessionStatus();//检查登陆状态，即是否已经获得了AccessToken,设置LOGIN_STATUS

        mFragmentManager = getFragmentManager();
        setTabFragment(HOME);//默认展示主页碎片

        setupListener();//设置TAB事件监听

    }
    private void setTabFragment(int index) {
        setToolBar(index);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        hideAllFragments(transaction);
        //设置fragment的淡入淡出效果
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        switch (index) {
            case HOME://首页
                mHomeTab.setSelected(true);

                if (mMainFragment == null) {
                    mMainFragment = new MainFragment();
                    transaction.add(R.id.contentLayout, mMainFragment);
                } else {
                    transaction.show(mMainFragment);
                }
                break;
            case MESSAGE://消息
                mMessageTab.setSelected(true);
                if (mMessageFragment == null) {
                    mMessageFragment = new MessageFragment();
                    transaction.add(R.id.contentLayout, mMessageFragment);
                } else {
                    transaction.show(mMessageFragment);
                }
                break;
            case POST://发微博
                mPostTab.setSelected(true);
                if (mPostFragment == null) {
                    mPostFragment = new PostFragment();
                    transaction.add(R.id.contentLayout, mPostFragment);
                } else {
                    transaction.show(mPostFragment);
                }
                break;
            case DISCOVERY://发现
                mDiscoveryTab.setSelected(true);
                if (mDiscoverFragment == null) {
                    mDiscoverFragment = new DiscoverFragment();
                    transaction.add(R.id.contentLayout, mDiscoverFragment);
                } else {
                    transaction.show(mDiscoverFragment);
                }
                break;
            case PROFILE://个人简介
                mProfileTab.setSelected(true);
                if (mProfileFragment == null) {
                    mProfileFragment = new ProfileFragment();
                    transaction.add(R.id.contentLayout, mProfileFragment);
                } else {
                    transaction.show(mProfileFragment);
                }
                break;
        }

        transaction.commit();

    }

    /**
     * 设置ToolBar
     */
    private void setToolBar(int index) {
        mToolBar = (LinearLayout) findViewById(R.id.toolbar_layout);
        switch (index) {
            case HOME:
                mToolBar.removeAllViews();
                if (NewFeature.LOGIN_STATUS == true) {//已登录
                    LayoutInflater.from(mContext).inflate(R.layout.toolbar_home_login, mToolBar);
                    mUserName = (TextView) mToolBar.findViewById(R.id.toolbar_username);
                    mUserName.setText(weiboUserName);

                } else {//未登录
                    LayoutInflater.from(mContext).inflate(R.layout.toolbar_home_unlogin, mToolBar);
                }
                break;
            case MESSAGE:
                mToolBar.removeAllViews();
                LayoutInflater.from(mContext).inflate(R.layout.toolbar_message, mToolBar);
                break;
            case POST:
                mToolBar.removeAllViews();
                LayoutInflater.from(mContext).inflate(R.layout.toolbar_post, mToolBar);
                mUserName = (TextView) mToolBar.findViewById(R.id.toolbar_username);
                if (NewFeature.LOGIN_STATUS == true) {
                    mUserName.setText(weiboUserName);
                } else {
                    mUserName.setText("");
                }
                break;
            case DISCOVERY:
                mToolBar.removeAllViews();
                LayoutInflater.from(mContext).inflate(R.layout.toolbar_discovery, mToolBar);
                break;
            case PROFILE:
                mToolBar.removeAllViews();
                LayoutInflater.from(mContext).inflate(R.layout.toolbar_profile, mToolBar);
                break;

        }
    }


    /**
     * 隐藏所有的碎片
     */
    private void hideAllFragments(FragmentTransaction transaction) {
        if (mMainFragment != null) {
            transaction.hide(mMainFragment);
        }
        if (mMessageFragment != null) {
            transaction.hide(mMessageFragment);
        }
        if (mPostFragment != null) {
            transaction.hide(mPostFragment);
        }
        if (mDiscoverFragment != null) {
            transaction.hide(mDiscoverFragment);
        }
        if (mProfileFragment != null) {
            transaction.hide(mProfileFragment);
        }
        mHomeTab.setSelected(false);
        mMessageTab.setSelected(false);
        mPostTab.setSelected(false);
        mDiscoveryTab.setSelected(false);
        mProfileTab.setSelected(false);
    }

    /**
     * 检查登陆状态，即是否已经获得了AccessToken,并设置LOGIN_STATUS的状态
     * 是则NewFeature.LOGIN_STATUS = true;
     * 否则NewFeature.LOGIN_STATUS = false;
     */
    private void checkSessionStatus() {
        mAccessToken = AccessTokenKeeper.readAccessToken(mContext);
        if (mAccessToken.isSessionValid()) {//如果已经获取到了AccessToken
            NewFeature.LOGIN_STATUS = true;//登陆状态为true
            AccessTokenKeeper.readUserName(mContext);
        } else {
            NewFeature.LOGIN_STATUS = false;
        }
    }

    private void setupListener() {
        mHomeTab.setOnClickListener(this);
        mMessageTab.setOnClickListener(this);
        mPostTab.setOnClickListener(this);
        mDiscoveryTab.setOnClickListener(this);
        mProfileTab.setOnClickListener(this);

    }

    /**
     * 处理底部导航栏的监听事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tab_home:
                if (!mHomeTab.isSelected()) {//如果未选中，就执行
                    setTabFragment(HOME);
                }
                break;
            case R.id.tab_message:
                if (!mMessageTab.isSelected()) {
                    setTabFragment(MESSAGE);
                }
                break;
            case R.id.tab_post:
                if (!mPostTab.isSelected()) {
                    setTabFragment(POST);
                }
                break;
            case R.id.tab_discovery:
                if (!mDiscoveryTab.isSelected()) {
                    setTabFragment(DISCOVERY);
                }
                break;
            case R.id.tab_profile:
                if (!mProfileTab.isSelected()) {
                    setTabFragment(PROFILE);
                }
                break;

        }
    }

}
