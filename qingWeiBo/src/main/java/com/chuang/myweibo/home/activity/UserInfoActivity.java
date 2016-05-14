package com.chuang.myweibo.home.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chuang.myweibo.R;
import com.chuang.myweibo.home.fragment.home.MainFragment;
import com.chuang.myweibo.utils.DateUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.models.User;

/**
 * 用户信息详情页面
 * Created by Chuang on 4-20.
 */
public class UserInfoActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "UserInfoActivity";
    private Context mContext;

    private long uid;

    //
    private DisplayImageOptions options;

    //用户信息展示
    private ImageView user_background;
    private ImageView user_img;
    private ImageView user_verified_symbol;
    private TextView user_name;
    private ImageView user_gender;
    private TextView user_follow;
    private TextView user_fans;
    private TextView user_describe;
    private TextView user_weihao;
    private TextView user_location;
    private TextView user_create_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏 一些手机如果有虚拟键盘的话,虚拟键盘就会变成透明的,挡住底部按钮点击事件所以,最好不要用
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Explode());//淡出离开
        setContentView(R.layout.activity_user_main_layout);

        //获取要显示信息的用户的uid
        uid = getIntent().getLongExtra("UID", 0);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.avator_default)
                .showImageForEmptyUri(R.drawable.avator_default)
                .showImageOnFail(R.drawable.avator_default)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new CircleBitmapDisplayer(14671839, 1))
                .build();
        initUser();
        setupListener();
    }

    private void setupListener() {
        findViewById(R.id.toolbar_back).setOnClickListener(this);
        findViewById(R.id.toolbar_more).setOnClickListener(this);
    }

    private void initUser() {
        user_background = (ImageView) findViewById(R.id.user_background);
        user_img = (ImageView) findViewById(R.id.user_img);
        user_verified_symbol = (ImageView) findViewById(R.id.user_verified_symbol);
        user_name = (TextView) findViewById(R.id.user_name);
        user_gender = (ImageView) findViewById(R.id.user_gender);
        user_follow = (TextView) findViewById(R.id.user_follow);
        user_fans = (TextView) findViewById(R.id.user_fans);
        user_describe = (TextView) findViewById(R.id.user_describe);
        user_weihao = (TextView) findViewById(R.id.user_weihao);
        user_location = (TextView) findViewById(R.id.user_location);
        user_create_time = (TextView) findViewById(R.id.user_create_time);

        MainFragment.mUsersAPI.show(uid, new RequestListener() {
            @Override
            public void onComplete(String response) {
                if (!TextUtils.isEmpty(response)) {
                    // 调用 User#parse 将JSON串解析成User对象
                    Log.d(TAG, "onComplete: " + response);
                    User user = User.parse(response);
                    if (user != null) {
                        //加载背景图
                        if (user.cover_image_phone != null && user.cover_image_phone.length() > 0) {
                            ImageLoader.getInstance().displayImage(user.cover_image_phone, user_background);
                        } else if (user.cover_image != null && user.cover_image.length() > 0) {
                            ImageLoader.getInstance().displayImage(user.cover_image, user_background);
                        } else {
                            user_background.setImageResource(R.drawable.userinfo_background);
                        }
                        //加载头像
                        ImageLoader.getInstance().displayImage(
                                user.avatar_hd,
                                user_img,
                                options
                        );
                        if (user.verified) {
                            if (user.verified_type == -1) {//普通用户
                                user_verified_symbol.setVisibility(View.GONE);
                            } else if (user.verified_type == 0) {//黄V
                                user_verified_symbol.setImageResource(R.drawable.avatar_vip);
                            } else if (user.verified_type == 1//蓝V
                                    || user.verified_type == 2
                                    || user.verified_type == 3) {
                                user_verified_symbol.setImageResource(R.drawable.avatar_enterprise_vip);
                            } else if (user.verified_type == 220) {//达人
                                user_verified_symbol.setImageResource(R.drawable.avatar_grassroot);
                            } else {
                                Log.d(TAG, "新的认证类型" + user.verified_type);
                            }
                        }
                        user_name.setText(user.name);
                        user_describe.setText("简介: " + user.description);
                        user_follow.setText("关注  " + user.friends_count + "");
                        user_fans.setText("粉丝  " + user.followers_count + "");
                        user_weihao.setText(user.weihao);
                        user_location.setText(user.location);
                        user_create_time.setText(DateUtils.translateDateHaveYear(user.created_at));
                        if (user.gender.equals("m")) {
                            user_gender.setImageResource(R.drawable.userinfo_icon_male);
                        } else if (user.gender.equals("f")) {
                            user_gender.setImageResource(R.drawable.userinfo_icon_female);
                        } else {
                            user_gender.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_back:
                onBackPressed();
                break;
            case R.id.toolbar_more:
                Toast.makeText(mContext, "更多", Toast.LENGTH_SHORT).show();
        }
    }
}
