/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.launcherplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.WindowManagerImpl;

public class DragView extends View implements TweenCallback {
    // Number of pixels to add to the dragged item for scaling. Should be even
    // for pixel alignment.
    private static final int DRAG_SCALE = 24;

    private Bitmap mBitmap;

    private Paint mPaint;

    private int mRegistrationX;

    private int mRegistrationY;

    SymmetricalLinearTween mTween;

    private float mScale;

    private float mAnimationScale = 1.0f;

    private WindowManager.LayoutParams mLayoutParams;

    private WindowManager mWindowManager;

    private int mBitmapWidth;

    private int mBitmapHeight;

    private float mPaddingHorizontal;

    private float mPaddingVertical;

    private DragController mDragController = null;

    private boolean mDCHandleTouchEvent = false;

    private final int[] mCoordinatesTemp = new int[2];

    private static final int REMOVE_CONTENT_VIEW = 0x00000001;

    // Adjust TouchEvent will by handled by DragController or Launcher
    private View mContentView;

    private int mScreenWidth;

    private int mScreenHeight;

    private int mCellHeight;

    private int mStatusBarHeight;

    /**
     * Construct the drag view.
     * <p>
     * The registration point is the point inside our view that the touch events
     * should be centered upon.
     * 
     * @param context A context
     * @param bitmap The view that we're dragging around. We scale it up when we
     *            draw it.
     * @param registrationX The x coordinate of the registration point.
     * @param registrationY The y coordinate of the registration point.
     */
    public DragView(Context context, Bitmap bitmap, int registrationX, int registrationY, int left,
            int top, int width, int height) {
        super(context);

        mWindowManager = WindowManagerImpl.getDefault();
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        mWindowManager = WindowManagerImpl.getDefault();

        mTween = new SymmetricalLinearTween(false, 110 /* ms duration */, this);

        Matrix scale = new Matrix();
        float scaleFactor = width;
        scaleFactor = mScale = (scaleFactor + DRAG_SCALE) / scaleFactor;
        if (Launcher.isInEditMode()) {
        	scaleFactor = 1;
        }
        scale.setScale(scaleFactor, scaleFactor);

        mBitmap = Bitmap.createBitmap(bitmap, left, top, width, height, scale, true);

        // The point in our scaled bitmap that the touch events are located
        mRegistrationX = registrationX + (DRAG_SCALE / 2);
        mRegistrationY = registrationY + (DRAG_SCALE / 2);

        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        mPaddingHorizontal = (mBitmapWidth - bitmap.getWidth()) / 2;
        mPaddingVertical = (mBitmapHeight - bitmap.getHeight()) / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (false) {
            // for debugging
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xaaffffff);
            canvas.drawRect(0, 0, getWidth(), getHeight(), p);
        }
        float scale = mAnimationScale;
        if (scale < 0.999f) { // allow for some float error
            float width = mBitmap.getWidth();
            float offset = (width - (width * scale)) / 2;
            canvas.translate(offset, offset);
            canvas.scale(scale, scale);
        }
        if (mPaint == null) {
            Paint ditherPaint = new Paint();
            ditherPaint.setDither(true);
            canvas.drawBitmap(mBitmap, 0.0f, 0.0f, ditherPaint);
        } else {
            mPaint.setDither(true);
            canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBitmap.recycle();
        mBitmap = null;
        System.gc();
    }

    public void onTweenValueChanged(float value, float oldValue) {
        mAnimationScale = (1.0f + ((mScale - 1.0f) * value)) / mScale;
        invalidate();
    }

    public void onTweenStarted() {
    }

    public void onTweenFinished() {
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
        invalidate();
    }

    /**
     * Create a window containing this view and show it.
     * 
     * @param windowToken obtained from v.getWindowToken() from one of your
     *            views
     * @param touchX the x coordinate the user touched in screen coordinates
     * @param touchY the y coordinate the user touched in screen coordinates
     */
    public void show(IBinder windowToken, int touchX, int touchY) {
        WindowManager.LayoutParams lp;
        int pixelFormat;

        pixelFormat = PixelFormat.TRANSLUCENT;

        lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, touchX - mRegistrationX, touchY
                        - mRegistrationY, WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                /* | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM */, pixelFormat);
        // lp.token = mStatusBarView.getWindowToken();
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        lp.token = windowToken;
        lp.setTitle("DragView");
        mLayoutParams = lp;

        mWindowManager.addView(this, lp);

        mAnimationScale = 1.0f / mScale;
        mTween.start(true);
    }

    /**
     * Move the window containing this view.
     * 
     * @param touchX the x coordinate the user touched in screen coordinates
     * @param touchY the y coordinate the user touched in screen coordinates
     */
    void move(int touchX, int touchY) {
        WindowManager.LayoutParams lp = mLayoutParams;
        lp.x = touchX - mRegistrationX;
        lp.y = touchY - mRegistrationY;
        restraintToScreen(touchX, touchY, lp);

        mWindowManager.updateViewLayout(this, lp);
    }

    void remove() {
        mWindowManager.removeView(this);
    }

    private boolean contains(int x, int y) {
        return x >= 0 && y >= 0 && x <= mBitmapWidth && y <= mBitmapHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (contains(x, y)) {
                    mRegistrationX = x;
                    mRegistrationY = y;
                    float touchOffsetX = ev.getX() - mPaddingHorizontal > 0 ? ev.getX()
                            - mPaddingHorizontal : 0;
                    float touchOffsetY = ev.getY() - mPaddingVertical > 0 ? ev.getY()
                            - mPaddingVertical : 0;
                    mDragController.setTouchOffset(touchOffsetX, touchOffsetY);
                    mDCHandleTouchEvent = true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mDCHandleTouchEvent) {
                    mDCHandleTouchEvent = false;
                    return mDragController.onTouchEvent(ev);
                }
                break;
        }

        if (mDCHandleTouchEvent) {
            return mDragController.onTouchEvent(ev);
        } else {
            mDragController.handleTouchEvent(ev);
            return true;
        }
    }

    public void setDragController(DragController dragController) {
        mDragController = dragController;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mDragController.cancelDrag();
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Draw the view into a bitmap.
     */
    private Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    public void show(IBinder windowToken, Workspace workspace, int spanX, int spanY, int cellWidth,
            int cellHeight) {
        WindowManager.LayoutParams lp;
        int pixelFormat = PixelFormat.TRANSLUCENT;

        int viewWidth = spanX * cellWidth;
        int viewHeight = spanY * cellHeight;
        lp = new WindowManager.LayoutParams(viewWidth, viewHeight,
                (workspace.getWidth() - viewWidth) / 2, (workspace.getHeight() - viewHeight) / 2,
                WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, pixelFormat);
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        lp.token = windowToken;
        lp.setTitle("DragView_ContentView");
        mLayoutParams = lp;

        mWindowManager.addView(mContentView, lp);
        mContentView.setVisibility(View.INVISIBLE);
        mHandler.sendMessage(mHandler.obtainMessage(REMOVE_CONTENT_VIEW));
    }

    public DragView(Context context, View view) {
        super(context);

        mContentView = view;
        mWindowManager = WindowManagerImpl.getDefault();
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == REMOVE_CONTENT_VIEW) {
                Bitmap bitmap = getViewBitmap(mContentView);
                Matrix scale = new Matrix();
                float scaleFactor = bitmap.getWidth();
                scaleFactor = mScale = (scaleFactor + DRAG_SCALE) / scaleFactor;
                scale.setScale(scaleFactor, scaleFactor);
                mBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        scale, true);
                bitmap.recycle();
                bitmap = null;
                System.gc();
                // The point in our scaled bitmap that the touch events are
                // located
                mBitmapWidth = mBitmap.getWidth();
                mBitmapHeight = mBitmap.getHeight();
                mRegistrationX = mBitmapWidth / 2;
                mRegistrationY = mBitmapHeight / 2;
                mLayoutParams.x -= DRAG_SCALE / 2;
                mLayoutParams.y -= DRAG_SCALE / 2;
                mWindowManager.addView(DragView.this, mLayoutParams);
                mWindowManager.removeView(mContentView);
            }
        }
    };

    public void setCellHeight(int cellHeight) {
        mCellHeight = cellHeight;
    }

    public void setStatusBarHeight(int statusBarHeight) {
        mStatusBarHeight = statusBarHeight;
    }

    private void restraintToScreen(int touchX, int touchY, WindowManager.LayoutParams lp) {
        boolean needReCompute = false;
        if (lp.y + getHeight() - mStatusBarHeight < mCellHeight) {
            lp.y = mCellHeight + mStatusBarHeight - getHeight();
            needReCompute = true;
        }

        if (lp.y + (int) (mBitmapHeight / 3) > mScreenHeight) {
            lp.y = mScreenHeight - (int) (mBitmapHeight / 3);
            needReCompute = true;
        }

        if (lp.x + (int) (mBitmapWidth / 3 * 2) < 0) {
            lp.x = (int) (mBitmapWidth / 3 * 2) * (-1);
            needReCompute = true;
        }

        if (lp.x + (int) (mBitmapWidth / 3) > mScreenWidth) {
            lp.x = mScreenWidth - (int) (mBitmapWidth / 3);
            needReCompute = true;
        }

        if (needReCompute) {
            reComputeTouchOffset(touchX, touchY);
        }
    }

    private void reComputeTouchOffset(int touchX, int touchY) {
        int[] loc = mCoordinatesTemp;
        getLocationOnScreen(loc);
        int screenX = loc[0];
        int screenY = loc[1];
        mRegistrationX = touchX - screenX;
        mRegistrationY = touchY - screenY;
        float touchOffsetX = mRegistrationX - mPaddingHorizontal > 0 ? mRegistrationX
                - mPaddingHorizontal : 0;
        float touchOffsetY = mRegistrationY - mPaddingVertical > 0 ? mRegistrationY
                - mPaddingVertical : 0;
        mDragController.setTouchOffset(touchOffsetX, touchOffsetY);
    }
}
