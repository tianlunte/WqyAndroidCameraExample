package com.tianlunte.wangqytest.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by wangqingyun on 5/18/16.
 */
public class TouchableImageView extends ImageView {

    public interface ITouchableImageViewDelegate {
        void onEventDown(float x, float y);
        void onEventMove(float x, float y);
    }

    private ITouchableImageViewDelegate mDelegate;

    public TouchableImageView(Context context) {
        super(context);
    }

    public TouchableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setupDelegate(ITouchableImageViewDelegate delegate) {
        mDelegate = delegate;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mDelegate.onEventDown(event.getRawX() / getWidth(), event.getRawY() / getHeight());
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                mDelegate.onEventMove(event.getRawX() / getWidth(), event.getRawY() / getHeight());
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {

            }
            break;
        }

        return true;
    }

}