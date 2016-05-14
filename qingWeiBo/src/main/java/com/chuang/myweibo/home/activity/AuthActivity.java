package com.chuang.myweibo.home.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chuang.myweibo.AccessTokenKeeper;
import com.chuang.myweibo.Constants;
import com.chuang.myweibo.R;
import com.chuang.myweibo.home.ActivityCollector;
import com.chuang.myweibo.utils.ToastUtil;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.User;

/**
 * 授权页面
 */
public class AuthActivity extends AppCompatActivity implements View.OnClickListener {
    //环境
    private Context mContext;
    private String weiboUserName;
    //微博授权类
    private AuthInfo mAuthInfo;
    private SsoHandler mSsoHandler;
    /**
     * 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能
     */
    private Oauth2AccessToken mAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.activity_auth);
        ActivityCollector.addActivity(this);

        //创建授权认证信息
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URI, Constants.SCOPE);
        mSsoHandler = new SsoHandler(AuthActivity.this, mAuthInfo);
        Log.d("222", "已经初始化");

        setupListener();
    }


    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     * 该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {
        AuthListener() {
            Log.d("222", "AuthListener: ");
        }

        @Override
        public void onComplete(Bundle values) {
            Log.d("222", "74");
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            //从这里获取用户输入的 电话号码信息
            String phoneNum = mAccessToken.getPhoneNum();
            if (mAccessToken.isSessionValid()) {
                Log.d("222", "79");

                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(AuthActivity.this, mAccessToken);
                Log.d("222", "84");
//                Toast.makeText(AuthActivity.this, "成功获取token", Toast.LENGTH_SHORT).show();

                getWeiboUserName();//获取当前登录的用户名

            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
//                String message = getString(R.string.weibosdk_demo_toast_auth_failed);
//                if (!TextUtils.isEmpty(code)) {
//                    message = message + "\nObtained the code: " + code;
//                }
//                Toast.makeText(AuthActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(AuthActivity.this,
                    "取消授权", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(AuthActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    /**
     * 获取微博用户名
     */
    private void getWeiboUserName() {

        UsersAPI mUsersAPI = new UsersAPI(mContext, Constants.APP_KEY, mAccessToken);
        long uid = Long.parseLong(mAccessToken.getUid());
        mUsersAPI.show(uid, new RequestListener() {
            @Override
            public void onComplete(String response) {
                // 调用 User#parse 将JSON串解析成User对象
                User user = User.parse(response);
                if (user != null) {
//                    mUserName.setText(user.name);
                    weiboUserName = user.name;
                    //把用户名保存到SP
                    AccessTokenKeeper.writeUserName(AuthActivity.this, weiboUserName);
                    //跳转到MainAcivity
                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                    startActivity(intent);
                    //防止跳转后返回授权界面
                    ActivityCollector.removeActivity(AuthActivity.this);

                }
            }

            @Override
            public void onWeiboException(WeiboException e) {
                ErrorInfo info = ErrorInfo.parse(e.getMessage());
                ToastUtil.showShort(mContext, info.toString());
            }
        });
    }

    private void setupListener() {
        findViewById(R.id.toolbar_back).setOnClickListener(this);
        findViewById(R.id.btn_auth).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_back:
                onBackPressed();
                break;
            case R.id.btn_auth:
                mSsoHandler.authorizeWeb(new AuthListener());
//        Log.d("222", mAuthInfo.getAppKey());
//        Log.d("222",mAuthInfo.getKeyHash());
                break;
            case R.id.btn_sso:
                break;
            default:
                break;
        }

    }
}
