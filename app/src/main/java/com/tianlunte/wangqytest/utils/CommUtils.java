package com.tianlunte.wangqytest.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by wangqingyun on 5/18/16.
 */
public class CommUtils {

    public static int getScreenHeight(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);
        return size.y;
    }

    public static int getScreenWidth(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);
        return size.x;
    }

    public static boolean isCameraPermissionActive(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNullOrEmpty(String text) {
        return text == null || text.length() == 0;
    }

}