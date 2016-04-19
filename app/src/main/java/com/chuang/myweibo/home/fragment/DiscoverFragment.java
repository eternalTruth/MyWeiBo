package com.chuang.myweibo.home.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chuang.myweibo.R;

/**
 * Created by Chuang on 4-4.
 */
public class DiscoverFragment extends Fragment {
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_discovery_layout, null);

        return mView;
    }



}
