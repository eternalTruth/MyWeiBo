package com.chuang.myweibo.reveal.effect;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.View;

import com.chuang.myweibo.R;

/**
 * Created by Chuang on 4-20.
 */
public class MyReveal extends Activity {
    public RevealView mLayoutRevealView;
    boolean isRevealOpen = false;
    /* 动画 */
    int DURATION = 500;//动画持续时间
    int RADIUS = 10;


    /**
     * 仅绘制水波
     *
     * @param p 水波中心坐标
     */
    public void showAlbumRevealColorView(Point p) {
        mLayoutRevealView.reveal(p.x, p.y, getResources().getColor(R.color.colorPrimaryDark), RADIUS, DURATION, null);
        isRevealOpen = true;
    }


    /**
     * 可实现完成事件的水波
     *
     * @param listener
     */
    public void showLayoutRevealColorView(Point p, RevealView.RevealAnimationListener listener) {
        mLayoutRevealView.reveal(p.x, p.y, getThemeColor(), RADIUS, DURATION, listener);
        isRevealOpen = true;
    }

    /**
     * 关闭水波回退到一点
     *
     * @param p
     */
    private void closeLayoutRevealColorView(Point p) {
//        Point p = getLocationInView(mLayoutRevealView, mFloatingView);
        mLayoutRevealView.hide(p.x, p.y, Color.TRANSPARENT, RADIUS, DURATION, null);
        isRevealOpen = false;
    }

    /**
     * 获取主题Theme颜色
     *
     * @return
     */
    public int getThemeColor() {
        TypedValue typedValue = new TypedValue();
        int[] colorAttr = new int[]{R.attr.colorPrimary};
        int indexOfAttrColor = 0;
        TypedArray a = obtainStyledAttributes(typedValue.data, colorAttr);
        int color = a.getColor(indexOfAttrColor, -1);
        a.recycle();
        return color;
    }


    /**
     * 计算view控件的坐标
     *
     * @param src    控件来源（父容器）
     * @param target view对象
     * @return point对象。point.x和point.y为横纵坐标
     */
    public Point getLocationInView(View src, View target) {
        final int[] l0 = new int[2];
        src.getLocationOnScreen(l0);

        final int[] l1 = new int[2];
        target.getLocationOnScreen(l1);

        l1[0] = l1[0] - l0[0] + target.getWidth() / 2;
        l1[1] = l1[1] - l0[1] + target.getHeight() / 2;

        return new Point(l1[0], l1[1]);
    }

}
