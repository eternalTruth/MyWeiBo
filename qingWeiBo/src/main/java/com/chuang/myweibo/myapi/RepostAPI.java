package com.chuang.myweibo.myapi;

import android.util.Log;

import com.chuang.myweibo.utils.HttpUtil;

/**
 * Created by Chuang on 4-22.
 */
public class RepostAPI {
    private static String TAG = "RepostAPI";

    /**
     * @param accessToken
     * @param id          转发的微博id
     * @param text        转发的文字内容
     */
    public static void doRepost(String accessToken, String id, String text, HttpUtil.CallBack callBack) {
        String requestURL = "https://api.weibo.com/2/statuses/repost.json";
        String params = "&access_token=" + accessToken + "&id=" + id + "&status=" + text;
//        try {
//            new Thread(
//                    new Runnable() {
//                        @Override
//                        public void run() {
//                            HttpUtil.postUrl(requestURL, params, callBack);
//                        }
//                    }).start();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        try {
            HttpUtil.doPostAsyn(requestURL, params, callBack);
        } catch (Exception e) {
            Log.d(TAG, "doRepost: 以下错误由RepostAPI报出");
            e.printStackTrace();
        }
    }
}
