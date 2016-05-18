package com.tianlunte.wangqytest;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by wangqingyun on 5/17/16.
 */
public class WangQyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }

}