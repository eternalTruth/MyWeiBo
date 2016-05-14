package com.chuang.myweibo.utils;

import android.util.Log;

import com.sina.weibo.sdk.exception.WeiboException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Http请求的工具类
 * Created by wenmingvs on 15/12/27.
 */
public class HttpUtil {

    private static final String TAG = "HttpUtil";
    private static final int TIMEOUT_IN_MILLIONS = 5000;

    public interface CallBack {
//        void onPostComplete(String result);
//
//        void onGetComplete(byte[] result);

        void onComplete(String response);

        void onError(Exception e);
    }


    /**
     * 异步的Get请求
     *
     * @param urlStr
     * @param callBack
     */
    public static void doGetAsyn(final String urlStr, final CallBack callBack) {
        new Thread() {
            public void run() {
                try {
//                    byte[] result = doGet(urlStr);
                    String result = doGet(urlStr);
                    if (callBack != null) {
//                        callBack.onGetComplete(result);
                        callBack.onComplete(result);
                    }

                } catch (WeiboException e) {
//                    e.printStackTrace();
                    callBack.onError(e);
                }

            }

            ;
        }.start();
    }

    /**
     * 异步的Post请求
     *
     * @param urlStr
     * @param params
     * @param callBack
     * @throws Exception
     */
    public static void doPostAsyn(final String urlStr,
                                  final String params,
                                  final CallBack callBack) throws Exception {
        new Thread() {
            @Override
            public void run() {
                try {
                    String result = doPost(urlStr, params);
                    if (callBack != null) {
//                        callBack.onPostComplete(result);
                        callBack.onComplete(result);
                    }
                } catch (WeiboException e) {
                    Log.d(TAG, "run: 以下错误由httputil报出");
//                    e.printStackTrace();
                    callBack.onError(e);
                }
            }
        }.start();
    }

    /**
     * Get请求，获得返回数据
     *
     * @param urlStr
     * @return
     * @throws Exception
     */
    public static String doGet(String urlStr) {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIMEOUT_IN_MILLIONS);
            conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];

                while ((len = is.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                return baos.toByteArray().toString();
            } else {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
            }
            conn.disconnect();
        }
        return null;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     * @throws Exception
     */
    public static String doPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl
                    .openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setUseCaches(false);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(TIMEOUT_IN_MILLIONS);
            conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);

            if (param != null && !param.trim().equals("")) {
                // 获取URLConnection对象对应的输出流
                out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(param);
                // flush输出流的缓冲
                out.flush();
            }
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 以Post方式访问一个URL
     *
     * @param url        要访问的URL
     * @param parameters URL后面“？”后面跟着的参数
     */
    public static void postUrl(String url, String parameters, CallBack callBack) {
        Log.d(TAG, "postUrl: " + "开始执行");
        String result = "";
        try {
            Log.d(TAG, "postUrl:发送了 " + url + parameters);
//            URLConnection conn = new URL(url).openConnection();
//            conn.setDoOutput(true);// 这里是关键，表示我们要向链接里注入的参数
//            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());// 获得连接输出流
//            out.write(parameters);
//            out.flush();
//            out.close();
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            Log.d(TAG, "postUrl: " + "=================================");
            out.writeBytes(parameters);
            Log.d(TAG, "postUrl: " + "完成");
            // 到这里已经完成了，开始打印返回的HTML代码
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                result += line;
            }
            callBack.onComplete(result);
            Log.d(TAG, "postUrl: " + result);
        } catch (Exception e) {
//            e.printStackTrace();
            callBack.onError(e);
        }
    }
}
