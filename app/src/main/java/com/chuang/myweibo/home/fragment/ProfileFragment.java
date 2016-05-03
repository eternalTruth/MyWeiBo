package com.chuang.myweibo.home.fragment;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chuang.myweibo.NewFeature;
import com.chuang.myweibo.R;
import com.chuang.myweibo.home.activity.SettingsActivity;
import com.chuang.myweibo.home.activity.UserInfoActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.models.User;

/**
 * Created by Chuang on 4-4.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    //环境
    private Context mContext;
    private Activity mActivity;
    private View mView;

    private Point p;
    //图片处理
    private DisplayImageOptions options;
    //微博工具类
//    private AuthInfo mAuthInfo;
//    private SsoHandler mSsoHandler;
//    private Oauth2AccessToken mAccessToken;
//    private UsersAPI mUsersAPI;
    //我的信息
    private ImageView mProfile_myimg;
    private TextView mProfile_mydescribe;
    private TextView mProfile_myname;
    private TextView mStatuses_count;
    private TextView mFriends_count;
    private TextView mFollowers_count;

    private Intent intent;

    String TAG = "123";

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach:context ");
        super.onAttach(context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach: activity");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mContext = getActivity();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.avator_default)
                .showImageForEmptyUri(R.drawable.avator_default)
                .showImageOnFail(R.drawable.avator_default)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new CircleBitmapDisplayer(14671839, 1))
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        if (NewFeature.LOGIN_STATUS == true) {
            mView = inflater.inflate(R.layout.fragment_profile_layout, null);
//            initAccessToken();
            initUser();
            setupListener();
            setupToolBarListener();

        } else {// TODO: 4-11 未登录时显示未登陆的界面
            setupToolBarListener();
        }
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: ");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged: ");
        if (!hidden) {
            setupToolBarListener();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint: ");
    }

//    private void initAccessToken() {
//        mAuthInfo = new AuthInfo(mContext, Constants.APP_KEY,
//                Constants.REDIRECT_URI, Constants.SCOPE);
//        mSsoHandler = new SsoHandler(mActivity, mAuthInfo);
//        mAccessToken = AccessTokenKeeper.readAccessToken(mContext);
//        mUsersAPI = new UsersAPI(mContext, Constants.APP_KEY, mAccessToken);
//    }

    private void initUser() {

        mProfile_myimg = (ImageView) mView.findViewById(R.id.profile_myimg);
        mProfile_myname = (TextView) mView.findViewById(R.id.profile_myname);
        mProfile_mydescribe = (TextView) mView.findViewById(R.id.profile_mydescribe);
        mStatuses_count = (TextView) mView.findViewById(R.id.profile_mystatuses_count);
        mFollowers_count = (TextView) mView.findViewById(R.id.profile_myfollowers_count);
        mFriends_count = (TextView) mView.findViewById(R.id.profile_myfriends_count);

        long uid = Long.parseLong(MainFragment.mAccessToken.getUid());
        MainFragment.mUsersAPI.show(uid, new RequestListener() {
            @Override
            public void onComplete(String response) {
                if (!TextUtils.isEmpty(response)) {
                    // 调用 User#parse 将JSON串解析成User对象
                    User user = User.parse(response);
                    if (user != null) {
                        ImageLoader.getInstance().displayImage(
                                user.avatar_hd,
                                mProfile_myimg,
                                options
                        );
                        mProfile_myname.setText(user.name);
                        mProfile_mydescribe.setText("简介:" + user.description);
                        mStatuses_count.setText(user.statuses_count + "");
                        mFriends_count.setText(user.friends_count + "");
                        mFollowers_count.setText(user.followers_count + "");
                    }
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {

            }
        });
    }


    private void setupListener() {
        mView.findViewById(R.id.click_mycard).setOnClickListener(this);
        mView.findViewById(R.id.click_mystatus).setOnClickListener(this);
        mView.findViewById(R.id.click_myfriends).setOnClickListener(this);
        mView.findViewById(R.id.click_myfollowers).setOnClickListener(this);

    }

    private void setupToolBarListener() {
        mActivity.findViewById(R.id.toolbar_settings).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        View view = mActivity.findViewById(R.id.profile_myimg);
        switch (v.getId()) {
            case R.id.click_mycard:
                Toast.makeText(mContext, "我的信息", Toast.LENGTH_SHORT).show();
                intent = new Intent(mActivity, UserInfoActivity.class);
//                startActivity(intent);
//                startActivity(
//                        intent,
//                        ActivityOptions.makeSceneTransitionAnimation(
//                                mActivity,
//                                Pair.create(view, "user_img")
//                        ).toBundle()
//                );
                startActivity(
                        intent,
                        ActivityOptions.makeSceneTransitionAnimation(mActivity).toBundle()
                );
                break;
            case R.id.click_mystatus:
                Toast.makeText(mContext, "我的微博", Toast.LENGTH_SHORT).show();
                break;
            case R.id.click_myfriends:
                Toast.makeText(mContext, "我的关注", Toast.LENGTH_SHORT).show();
                break;
            case R.id.click_myfollowers:
                Toast.makeText(mContext, "我的粉丝", Toast.LENGTH_SHORT).show();
                break;
            case R.id.toolbar_settings:
                startActivity(new Intent(mActivity, SettingsActivity.class));

                break;
        }
    }
}
