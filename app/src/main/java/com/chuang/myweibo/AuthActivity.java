package com.chuang.myweibo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

public class AuthActivity extends AppCompatActivity {

    private Button mbtnAuth;
    private AuthInfo mAuthInfo;//微博实例
    private SsoHandler mSsoHandler;
    /**
     * 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能
     */
    private Oauth2AccessToken mAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mbtnAuth = (Button) findViewById(R.id.btn_auth);

        // 初始化微博对象
        mAuthInfo = new AuthInfo(this, Contants.APP_KEY, Contants.REDIRECT_URI, Contants.SCOPE);
        mSsoHandler = new SsoHandler(AuthActivity.this, mAuthInfo);
    }

    public void oauthClicked(View v) {

        mSsoHandler.authorizeWeb(new AuthListener());
        Log.d("222", mAuthInfo.getAppKey());
        Log.d("222",mAuthInfo.getKeyHash());

    }

    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     * 该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            //从这里获取用户输入的 电话号码信息
            String phoneNum = mAccessToken.getPhoneNum();
            if (mAccessToken.isSessionValid()) {
                // 显示 Token
//                updateTokenView(false);

                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(AuthActivity.this, mAccessToken);
                Toast.makeText(AuthActivity.this,
                        "成功获取token", Toast.LENGTH_SHORT).show();

//                Intent intent = new Intent("com.chuang.myweibo.MainActivity");
//                Bundle bundle = values;
//                intent.putExtras(bundle);
//                startActivity(intent);
                startActivity(new Intent(AuthActivity.this,MainActivity.class));
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
}
