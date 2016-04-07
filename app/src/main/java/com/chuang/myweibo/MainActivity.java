package com.chuang.myweibo;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

public class MainActivity extends AppCompatActivity {
    private Context mContext;

    private Oauth2AccessToken mAccessToken;

    //碎片及碎片管理器
    private FragmentManager mFragmentManager;

    private MainFragment mMainFragment;
    private MessageFragment mMessageFragment;
    private PostFragment mPostFragment;
    private DiscoverFragment mDiscoverFragment;
    private ProfileFragment mProfileFragment;

    //底部导航
    private TextView mHomeTab, mMessageTab, mDiscoveryTab, mProfile;
    private FrameLayout mPostTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mainactivity_layout);
//        setContentView(R.layout.activity_main);

        mHomeTab = (TextView) findViewById(R.id.tv_home);
        mMessageTab = (TextView) findViewById(R.id.tv_message);
        mDiscoveryTab = (TextView) findViewById(R.id.tv_discovery);
        mProfile = (TextView) findViewById(R.id.tv_profile);
        mPostTab = (FrameLayout) findViewById(R.id.fl_post);

        mContext = this;
        ActivityCollector.addActivity(this);
        checkSessionStatus();//检查登陆状态，即是否已经获得了AccessToken
        mFragmentManager = getFragmentManager();
//        setTabFragment(0);//默认展示主页碎片

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
//        hideAllFragments(transaction);
        mMainFragment = new MainFragment();
        transaction.add(R.id.contentLayout, mMainFragment);
        transaction.commit();
//        setUpListener();//设置底部导航栏的监听事件


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
        } else {
            NewFeature.LOGIN_STATUS = false;
        }
    }

}
