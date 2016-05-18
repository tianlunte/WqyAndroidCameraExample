package com.tianlunte.wangqytest.models;

import android.hardware.Camera;

/**
 * Created by wangqingyun on 5/18/16.
 */
public class WCameraWrapper {

    public int pCameraId;
    public Camera pCamera;

    public WCameraWrapper(int id, Camera camera) {
        this.pCameraId = id;
        this.pCamera = camera;
    }

}