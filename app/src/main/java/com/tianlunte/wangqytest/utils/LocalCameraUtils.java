package com.tianlunte.wangqytest.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.view.Surface;

import com.tianlunte.wangqytest.models.WCameraWrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wangqingyun on 5/18/16.
 */
public class LocalCameraUtils {

    public static WCameraWrapper getFrontCamera() {
        for(int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    return new WCameraWrapper(i, Camera.open(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static WCameraWrapper getRearCamera() {
        for(int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    return new WCameraWrapper(i, Camera.open(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static int getCameraDisplayOrientation(Activity activity, @SuppressWarnings("deprecation") Camera.CameraInfo cameraInfo) {
        int degrees = getCameraDisplayOrientation_1(activity, cameraInfo);

        String MANUFACTURER = android.os.Build.MANUFACTURER;
        int GINGERBREAD_MR1 = android.os.Build.VERSION_CODES.GINGERBREAD_MR1;
        String MODEL = android.os.Build.MODEL;
        String RELEASE = Build.VERSION.RELEASE;

        //noinspection deprecation
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            if ((MANUFACTURER.toLowerCase().equals("HTC".toLowerCase()) && MODEL.toLowerCase().equals("HTC Salsa C510e".toLowerCase()))
                    || (MANUFACTURER.toLowerCase().equals("samsung".toLowerCase()) && MODEL.toLowerCase().equals("Galaxy Y Duos".toLowerCase()))
                    || (MANUFACTURER.toLowerCase().equals("LGE".toLowerCase()) && MODEL.toLowerCase().equals("LG-P500".toLowerCase()))) {
                degrees = (degrees + 90 + 360) % 360;
            }
        } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            if ((MANUFACTURER.toLowerCase().equals("samsung".toLowerCase()) && MODEL.toLowerCase().equals("Galaxy Y".toLowerCase()))
                    || (MANUFACTURER.toLowerCase().equals("samsung".toLowerCase()) && MODEL.toLowerCase().equals("Galaxy Y Duos".toLowerCase()))
                    || (MANUFACTURER.toLowerCase().equals("LGE".toLowerCase()) && MODEL.toLowerCase().equals("LG-P500".toLowerCase()))
                    || (MANUFACTURER.toLowerCase().equals("samsung".toLowerCase()) && MODEL.toLowerCase().equals("Galaxy Young Pro".toLowerCase()))
                    || (MANUFACTURER.toLowerCase().equals("samsung".toLowerCase()) && MODEL.toLowerCase().equals("GT-S5360".toLowerCase()))
                    || (MANUFACTURER.toLowerCase().equals("samsung".toLowerCase()) && MODEL.toLowerCase().equals("Galaxy Ace 2".toLowerCase()))) {
                degrees = (degrees + 90 + 360) % 360;
            }
        }
        return degrees;
    }

    private static int getCameraDisplayOrientation_1(Activity activity, Camera.CameraInfo cameraInfo) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
            }
            break;
            case Surface.ROTATION_90: {
                degrees = 90;
            }
            break;
            case Surface.ROTATION_180: {
                degrees = 180;
            }
            break;
            case Surface.ROTATION_270: {
                degrees = 270;
            }
            break;
            default:
                break;
        }

        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }

        return result;
    }

    public static byte[] cutOutCorrectPhoto(Context context, byte[] data, int containerWidth, int containerHeight, boolean isFrontCamera,
                                            boolean isScreenPortrait) {
        int _rotate = 0;

        if (context != null) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, opts);

            String mimeType = opts.outMimeType;


            if (mimeType != null && mimeType.toLowerCase().contains("jp")) {
                // simply jpg || jpeg

                String path = getPhotoSavePath(context);

                writeBin(path, data);

                try {
                    ExifInterface _exif = new ExifInterface(path);
                    _rotate = exifOrientationToDegree(_exif);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                deleteFileOrFolder(path);
            }
        }

        Bitmap sourceBitmap = loadBitmap(data, 1280);
        if (sourceBitmap == null) {
            return null;
        }

        int pictureWidth = sourceBitmap.getWidth();
        int pictureHeight = sourceBitmap.getHeight();

        int x;
        int y;
        int height;
        int width;

        if (_rotate == 90 || _rotate == 270) {
            double scaleWidth = (double) pictureWidth / (double) containerHeight;
            double scaleHeight = (double) pictureHeight / (double) containerWidth;

            double targetProportion = (double) containerHeight / (double) containerWidth;

            if (scaleWidth > scaleHeight) {
                height = pictureHeight;
                width = (int) ((double) pictureHeight * targetProportion);

                y = 0;
                x = (pictureWidth - width) / 2;
            } else {
                width = pictureWidth;
                height = (int) ((double) pictureWidth / targetProportion);

                x = 0;
                y = (pictureHeight - height) / 2;
            }

        } else {
            double scaleWidth = (double) pictureWidth / (double) containerWidth;
            double scaleHeight = (double) pictureHeight / (double) containerHeight;

            double targetProportion = (double) containerWidth / (double) containerHeight;


            if (scaleWidth > scaleHeight) {
                height = pictureHeight;
                width = (int) ((double) pictureHeight * targetProportion);

                y = 0;
                x = (pictureWidth - width) / 2;
            } else {
                width = pictureWidth;
                height = (int) ((double) pictureWidth / targetProportion);

                x = 0;
                y = (pictureHeight - height) / 2;
            }
        }

        Matrix matrix;
        if (isFrontCamera && isScreenPortrait) {
            matrix = new Matrix();
            matrix.setRotate(_rotate + 180);
        } else {
            if (_rotate != 0) {
                matrix = new Matrix();

                matrix.setRotate(_rotate);
            } else {
                matrix = null;
            }
        }


        Bitmap targetBitmap = Bitmap.createBitmap(sourceBitmap, x, y, width, height, matrix, false);
        sourceBitmap.recycle();

        return compressBitmap(targetBitmap, 100);
    }

    public static byte[] compressBitmap(final Bitmap bmp, final int quality) {
        if (bmp == null)
            return null;

        byte[] buffer;
        ByteArrayOutputStream bytesWriter = new ByteArrayOutputStream();

        bmp.compress(Bitmap.CompressFormat.JPEG, quality, bytesWriter);

        buffer = bytesWriter.toByteArray();

        try {
            bytesWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer;
    }

    public static Bitmap loadBitmap(byte[] data, final int maxOutWidth) {

        if (data == null || maxOutWidth < 1) {
            return null;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inPurgeable = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, opts);

        int inSampleWidth = Math.round((float) opts.outWidth / (float) maxOutWidth);
        if (inSampleWidth < 1) {
            inSampleWidth = 1;
        }

        opts.inJustDecodeBounds = false;
        opts.inSampleSize = inSampleWidth;
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, opts);

        if (bmp != null && bmp.getWidth() <= maxOutWidth) {
            return bmp;
        }

        Bitmap bitmapTmp = bmp;
        bmp = createScaledBitmap(bitmapTmp, maxOutWidth, false);
        if (bitmapTmp != null) {
            bitmapTmp.recycle();
        }

        return bmp;
    }

    public static Bitmap createScaledBitmap(final Bitmap org, final int outWidth, boolean filter) {
        if (org == null || outWidth < 1)
            return null;

        final int outHeight = (int) ((double) org.getHeight() * (double) outWidth / (double) org.getWidth());
        return createScaledBitmap(org, outWidth, outHeight, filter);
    }

    private static Bitmap createScaledBitmap(final Bitmap org, final int outWidth, final int outHeight, boolean filter) {
        if (org == null || outWidth < 1 || outHeight < 1)
            return null;

        return Bitmap.createScaledBitmap(org, outWidth, outHeight, filter);
    }

    public static boolean deleteFileOrFolder(String path) {
        return deleteFileOrFolder(new File(path));
    }

    public static boolean deleteFileOrFolder(File file) {
        if (file == null) {
            return false;
        }

        if (!file.exists()) {
            return true;
        }

        if (file.isFile()) {
            return file.delete();
        }

        return file.isDirectory() && deleteFolder(file, true);
    }

    private static boolean deleteFolder(File file, boolean isDeleteFolder) {
        if (file == null) {
            return false;
        }

        if (!file.isDirectory()) {
            return false;
        }

        File[] childFiles = file.listFiles();
        if (childFiles == null || childFiles.length == 0) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            return true;
        }

        for (File childFile : childFiles) {
            deleteFolder(childFile, true);
        }

        if (isDeleteFolder) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        return true;
    }

    private static String getPhotoSavePath(Context context) {
        boolean isHasSDCard = isHasSDCard(context);

        String cachePath;

        if (isHasSDCard) {
            String externalCachePath = (context.getExternalCacheDir().getAbsolutePath() + File.separatorChar);
            File file = new File(externalCachePath);
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();

            cachePath = externalCachePath;
        } else {
            String systemCachePath = (context.getCacheDir().getAbsolutePath() + File.separatorChar);
            File file = new File(systemCachePath);
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();

            cachePath = systemCachePath;
        }

        cachePath += (File.separatorChar + "javacvPhoto");
        File file = new File(cachePath);
        //noinspection ResultOfMethodCallIgnored
        file.mkdirs();


        return cachePath + File.separatorChar + "photo.jpg";
    }

    private static boolean isHasSDCard(Context context) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File fileExternalStorageDirectory = Environment.getExternalStorageDirectory();
            File fileExternalCacheDir = context.getExternalCacheDir();
            if (null == fileExternalStorageDirectory || fileExternalCacheDir == null) {
                return false;
            }

            if (fileExternalStorageDirectory.exists() && fileExternalCacheDir.exists()) {
                return !CommUtils.isNullOrEmpty(fileExternalStorageDirectory.getAbsolutePath()) && !CommUtils.isNullOrEmpty(fileExternalCacheDir.getAbsolutePath());
            }
        }
        return false;
    }

    public static boolean writeBin(String fileWay, byte[] data) {
        return !CommUtils.isNullOrEmpty(fileWay) && writeBin(new File(fileWay), data);
    }

    public static boolean writeBin(File f, byte[] data) {
        return f != null && writeBin(f, data, false);
    }


    private static boolean writeBin(File f, byte[] data, boolean append) {
        if (f == null) {
            return false;
        }

        FileOutputStream fileOutputStream = null;
        try {
            if (data != null) {
                fileOutputStream = new FileOutputStream(f, append);
                fileOutputStream.write(data);
            }
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    public static int exifOrientationToDegree(ExifInterface exifInterface) {
        if (exifInterface == null) {
            return 0;
        }

        int exifRotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        int rotation;
        switch (exifRotation) {
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;

            case ExifInterface.ORIENTATION_UNDEFINED:
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotation = 0;
                break;

        }

        return rotation;
    }

}