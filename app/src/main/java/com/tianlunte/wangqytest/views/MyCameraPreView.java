package com.tianlunte.wangqytest.views;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.TextureView;

import java.io.IOException;

/**
 * Created by wangqingyun on 5/17/16.
 */
public class MyCameraPreView extends TextureView implements TextureView.SurfaceTextureListener {
    private Camera mCamera;
    private SurfaceTexture mTexture;

    private boolean mCameraSetup = false;
    private boolean mSurfaceCreated = false;

    public MyCameraPreView(Context context) {
        super(context);

        initSetup();
    }

    public MyCameraPreView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initSetup();
    }

    protected void initSetup() {
        setSurfaceTextureListener(this);
    }

    public void setupCamera(Camera camera) {
        mCamera = camera;
        mCameraSetup = true;

        readyCamera();
    }

    public void clearCamera() {
        mCameraSetup = false;
        unReadyCamera();
    }

    public void takePicture(Camera.PictureCallback callback) {
        mCamera.takePicture(null, null, callback);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceCreated = true;
        mTexture = surface;

        readyCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            e.printStackTrace();
        }

        try {
            mCamera.startPreview();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurfaceCreated = false;
        unReadyCamera();

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void readyCamera() {
        if(mCameraSetup && mSurfaceCreated) {
            try {
                mCamera.setPreviewTexture(mTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mCamera.startPreview();
        }
    }

    private void unReadyCamera() {
        try {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}