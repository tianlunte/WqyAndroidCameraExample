package com.tianlunte.wangqytest.fragments;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.tianlunte.wangqytest.MainActivity;
import com.tianlunte.wangqytest.R;
import com.tianlunte.wangqytest.models.WCameraWrapper;
import com.tianlunte.wangqytest.utils.CommUtils;
import com.tianlunte.wangqytest.utils.LocalCameraUtils;
import com.tianlunte.wangqytest.views.MyCameraPreView;
import com.tianlunte.wangqytest.views.TouchableImageView;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by wangqingyun on 5/17/16.
 */
public class FragmentTakePhoto extends Fragment implements View.OnClickListener, TouchableImageView.ITouchableImageViewDelegate {

    private MyCameraPreView mCameraPreView;
    private WCameraWrapper mCameraWrapper;

    private TouchableImageView mResultPicView;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPaint;
    private Path mPath;
    private Button mBtnSendPic;

    // eraser : xFermode or Paint.Shader

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_take_photo, container, false);

        mCameraPreView = (MyCameraPreView)rootView.findViewById(R.id.take_photo_camera_view);

        rootView.findViewById(R.id.take_photo_btn_take).setOnClickListener(this);

        mResultPicView = (TouchableImageView)rootView.findViewById(R.id.take_photo_result_pic);
        mResultPicView.setupDelegate(this);

        mBtnSendPic = (Button)rootView.findViewById(R.id.take_photo_send_photo);
        mBtnSendPic.setOnClickListener(this);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.parseColor("#ffff00"));

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_photo_btn_take: {
                mCameraPreView.takePicture(mJpegCallback);
            }
            break;
            case R.id.take_photo_send_photo: {
                ((MainActivity)getActivity()).sendImageMessage(mBitmap);
            }
            break;
        }
    }

    @Override
    public void onEventDown(float x, float y) {
        if(mCanvas != null) {
            mPath = new Path();
            mPath.moveTo(x * mBitmap.getWidth(), y * mBitmap.getHeight());
        }
    }

    @Override
    public void onEventMove(float x, float y) {
        if(mCanvas != null) {
            mPath.lineTo(x * mBitmap.getWidth(), y * mBitmap.getHeight());

            mCanvas.drawPath(mPath, mPaint);
            mResultPicView.setImageBitmap(mBitmap);
        }
    }

    public void show() {
        initAndRefresh();
    }

    public void hide() {
        mResultPicView.setVisibility(View.GONE);
        mBtnSendPic.setVisibility(View.GONE);

        mCameraPreView.clearCamera();
    }

    private void initAndRefresh() {
        mCameraPreView.clearCamera();
        mCameraWrapper = LocalCameraUtils.getRearCamera();
        if(mCameraWrapper != null) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraWrapper.pCameraId, cameraInfo);
            int degrees = LocalCameraUtils.getCameraDisplayOrientation(getActivity(), cameraInfo);
            mCameraWrapper.pCamera.setDisplayOrientation(degrees);
            mCameraPreView.setupCamera(mCameraWrapper.pCamera);

            int bestUI[] = new int[2];
            int bestCamera[] = new int[2];
            getBestSize(mCameraWrapper.pCamera, bestUI, bestCamera, true);
            /*
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)mCameraPreView.getLayoutParams();
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                params.width = bestUI[1];
                params.height = bestUI[0];
            } else {
                params.width = bestUI[0];
                params.height = bestUI[1];
            }
            mCameraPreView.setLayoutParams(params);
            */
            Camera.Parameters parameters = mCameraWrapper.pCamera.getParameters();
            parameters.setPreviewSize(bestCamera[0], bestCamera[1]);
            parameters.setRotation(degrees);
            getBestSize(mCameraWrapper.pCamera, bestUI, bestCamera, false);
            parameters.setPictureSize(bestCamera[0], bestCamera[1]);
            if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "portrait");
            } else {
                parameters.set("orientation", "landscape");
            }
            mCameraWrapper.pCamera.setParameters(parameters);
        }
    }

    private void getBestSize(Camera camera, int[] bestUI, int[] bestCamera, boolean previewSize) {
        int screenW = CommUtils.getScreenWidth(getActivity());
        int screenH = CommUtils.getScreenHeight(getActivity());

        bestUI[0] = screenW;
        bestUI[1] = screenH;
        int www = 0;
        int hhh = 0;

        List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
        if(!previewSize) {
            sizeList = camera.getParameters().getSupportedPictureSizes();
        }
        for(Camera.Size size : sizeList) {
            int min = Math.min(size.width, size.height);
            int max = Math.max(size.width, size.height);
            if(min <= Math.min(screenW, screenH) && max <= Math.max(screenW, screenH)) {
                if(min * max > www * hhh) {
                    www = min;
                    hhh = max;

                    bestCamera[0] = size.width;
                    bestCamera[1] = size.height;
                }
            }
        }

        if(www != 0) {
            bestUI[0] = www;
            bestUI[1] = hhh;
        }
    }

    private Bitmap getHorizontalReversePhoto(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Matrix m = new Matrix();
        m.postScale(-1, 1);
        Bitmap new2 = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
        newb.recycle();
        return new2;
    }

    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mCameraPreView.clearCamera();

            data = LocalCameraUtils.cutOutCorrectPhoto(getActivity(), data,
                    mCameraPreView.getWidth(),
                    mCameraPreView.getHeight(),
                    false, getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);

            mBitmap = bm.copy(Bitmap.Config.ARGB_8888, true);
            mCanvas = new Canvas(mBitmap);
            //bm = getHorizontalReversePhoto(bm);

            mResultPicView.setVisibility(View.VISIBLE);
            mResultPicView.setImageBitmap(mBitmap);

            mBtnSendPic.setVisibility(View.VISIBLE);
        }
    };

}