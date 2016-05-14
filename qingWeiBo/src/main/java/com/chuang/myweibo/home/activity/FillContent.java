package com.chuang.myweibo.home.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chuang.myweibo.R;
import com.chuang.myweibo.utils.DateUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用于填充信息内容的工具类
 * Created by Chuang on 5-6.
 */
public class FillContent {
    //设置图片的默认属性
    private static DisplayImageOptions mAvatorOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.avator_default)
            .showImageForEmptyUri(R.drawable.avator_default)
            .showImageOnFail(R.drawable.avator_default)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .displayer(new CircleBitmapDisplayer(14671839, 1))
            .build();


    /**
     * 填充《微博item》的顶部微博用户信息数据
     */
    public static void fillTitleBar(
            Context context,
            Status status,
            ImageView profile_img,
            ImageView profile_verified,
            TextView profile_name,
            TextView profile_time,
            TextView weibo_comefrom) {

        fillProfileImg(context, status.user, profile_img, profile_verified);
        profile_name.setText(status.user.name);
        setWeiBoTime(profile_time, status.created_at);
        setWeiBoComeFrom(weibo_comefrom, status.source);
    }

    /**
     * 填充《评论item》顶部微博用户信息数据
     */
    public static void fillTitleBar(
            Context context,
            Comment comment,
            ImageView profile_img,
            ImageView profile_verified,
            TextView profile_name,
            TextView profile_time,
            TextView weibo_comefrom) {
        FillContent.fillProfileImg(context, comment.user, profile_img, profile_verified);
        profile_name.setText(comment.user.name);
        FillContent.setWeiBoTime(profile_time, comment.created_at);
        FillContent.setWeiBoComeFrom(weibo_comefrom, comment.source);
    }

    public static void setWeiBoTime(TextView textView, String created_at) {
        Date data = DateUtils.parseDate(created_at, DateUtils.WeiBo_ITEM_DATE_FORMAT);
        SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
        String time = df.format(data);
        textView.setText(time + "   ");
    }

    public static void setWeiBoComeFrom(TextView textView, String content) {
        if (content != null && content.length() > 0) {
            textView.setText("来自 " + Html.fromHtml(content));
        } else {
            textView.setText("");
        }
    }

    /**
     * 填充微博文字内容
     */
    public static void fillWeiBoContent(String text, TextView weibo_content) {
        weibo_content.setText(text);
    }

    /**
     * 填充“提到我的”和“评论我的”中间文本内容
     *
     * @param status
     * @param profile_img
     * @param profile_name
     * @param content
     */
    public static void FillCenterContent(
            Status status,
            ImageView profile_img,
            TextView profile_name,
            TextView content) {
        if (status.pic_urls_bmiddle == null || status.pic_urls_bmiddle.size() == 0) {//没图就填充头像
            ImageLoader.getInstance().displayImage(status.user.avatar_hd, profile_img);
        } else {//有图就填充第一张图片
            ImageLoader.getInstance().displayImage(status.pic_urls_bmiddle.get(0), profile_img);
        }
        profile_name.setText(status.user.name);
        content.setText(status.text);
    }

    /**
     * 填充微博转发，评论，赞的数量
     */
    public static void fillBottomBar(Status status, TextView redirect, TextView comment, TextView feedlike) {
        redirect.setText(status.reposts_count + "");
        comment.setText(status.comments_count + "");
        feedlike.setText(status.attitudes_count + "");
    }


    /**
     * 填充粉丝的内容
     *
     * @param user
     * @param followerImg
     * @param followerVerf
     * @param followerName
     * @param content
     * @param profileComefrom
     * @param follwerRelation
     */
    public static void fillFollowContent(Context context,
                                         User user,
                                         ImageView followerImg,
                                         ImageView followerVerf,
                                         TextView followerName,
                                         TextView content,
                                         TextView profileComefrom,
                                         ImageView follwerRelation) {

        FillContent.fillProfileImg(context, user, followerImg, followerVerf);
        followerName.setText(user.name);
        if (user.description != null && user.description.length() > 0) {
            content.setText(user.description);
        } else {
            content.setText("这个粉丝很懒，没有写个人简介");
        }
        // TODO: 5-7 把来源删除，换成关注数和粉丝数
        if (user.status != null) {//有些人不发微博
            profileComefrom.setText(Html.fromHtml(user.status.source));
        } else {
            profileComefrom.setText("");
        }
        if (user.following == true) {
            follwerRelation.setImageResource(R.drawable.card_icon_arrow);
        } else {
            follwerRelation.setImageResource(R.drawable.card_icon_addattention);
        }
    }

    /**
     * 填充关注的内容
     *
     * @param user
     * @param followerImg
     * @param followerVerf
     * @param followerName
     * @param content
     * @param profileComefrom
     * @param friendRelation
     */
    public static void fillFriendContent(Context context,
                                         User user,
                                         ImageView followerImg,
                                         ImageView followerVerf,
                                         TextView followerName,
                                         TextView content,
                                         TextView profileComefrom,
                                         ImageView friendRelation) {

        FillContent.fillProfileImg(context, user, followerImg, followerVerf);
        followerName.setText(user.name);
        if (user.description != null && user.description.length() > 0) {
            content.setText(user.description);
        } else {
            content.setText("这个朋友很懒，没有写个人简介");
        }

        if (user.status != null) {//有些人不发微博
            profileComefrom.setText(Html.fromHtml(user.status.source));
        } else {
            profileComefrom.setText("");
        }
        if (user.follow_me) {
            friendRelation.setImageResource(R.drawable.card_icon_arrow);
        } else {
            friendRelation.setImageResource(R.drawable.card_icon_attention);
        }
    }

    /**
     * 设置头像的认证icon，记住要手动刷新icon，不然icon会被recycleriview重用，导致显示出错
     *
     * @param user
     * @param profile_img
     * @param profile_verified
     */
    public static void fillProfileImg(final Context context,
                                      final User user,
                                      final ImageView profile_img,
                                      final ImageView profile_verified) {

        profile_verified.setVisibility(View.GONE);
        profile_verified.setVisibility(View.VISIBLE);

        if (user.verified) {
            if (user.verified_type == 0) {//黄V
                profile_verified.setImageResource(R.drawable.avatar_vip);
            } else if (user.verified_type == 1//政府
                    || user.verified_type == 2//企业
                    || user.verified_type == 3//媒体
                    || user.verified_type == 4//校园
                    || user.verified_type == 5//网站
                    || user.verified_type == 6//应用
                    ) {
                profile_verified.setImageResource(R.drawable.avatar_enterprise_vip);
            } else if (user.verified_type == 220) {//达人
                profile_verified.setImageResource(R.drawable.avatar_grassroot);
            } else {
                Log.d("verify", "新的认证类型" + user.verified_type);
            }
        } else {//未认证
            profile_verified.setVisibility(View.INVISIBLE);
        }

        ImageLoader.getInstance().displayImage(user.avatar_hd, profile_img, mAvatorOptions);

        profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserInfoActivity.class);
//                intent.putExtra("user", user);
                // TODO: 5-6 可将user序列化传递intent，以获取完整信息
                intent.putExtra("UID", Long.parseLong(user.id));
                context.startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation((Activity) context).toBundle());

            }
        });


    }
}
