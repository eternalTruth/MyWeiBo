package com.chuang.myweibo.home.fragment.discovery;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chuang.myweibo.R;
import com.chuang.myweibo.home.ImageSliderView;
import com.chuang.myweibo.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chuang on 4-4.
 */
public class DiscoverFragment extends Fragment {
    private Context mContext;
    private Activity mActivity;
    private View mView;
    //轮播图容器
    private ImageSliderView myPager;
    //图片组
    private List<View> listViews;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mActivity = getActivity();
        mView = inflater.inflate(R.layout.fragment_discovery_layout, null);
        myPager = (ImageSliderView) mView.findViewById(R.id.vp_slider);
        myPager.setVisibility(View.GONE);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                initViewPager();
                myPager.start(
                        mActivity,
                        listViews,
                        3000,
                        null,
                        0,
                        0,
                        0,
                        0);
                myPager.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initViewPager() {
        listViews = new ArrayList<View>();
        int[] imageResId = new int[]{
                R.drawable.a,
                R.drawable.b,
                R.drawable.c,
                R.drawable.d,
                R.drawable.e
        };
        for (int id : imageResId) {
            ImageView imageView = new ImageView(mContext);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtil.showShort(mContext, "点击了" + myPager.getCurIndex());
                }
            });
            imageView.setImageResource(id);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            listViews.add(imageView);
        }

    }
}
