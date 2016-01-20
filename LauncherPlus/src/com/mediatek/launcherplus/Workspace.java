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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.mediatek.launcherplus.CellLayout.LayoutParams;

import android.app.Dialog;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ScreenStateChangeListener;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.IMTKWidget;
import android.widget.Toast;

/**
 * The workspace is a wide area with a wallpaper and a finite number of screens.
 * Each screen contains a number of icons, folders or widgets the user can
 * interact with. A workspace is meant to be used with a fixed width only.
 */
public class Workspace extends ViewGroup implements DropTarget, DragSource, DragScroller {
    @SuppressWarnings( {
        "UnusedDeclaration"
    })
    private static final String TAG = "LauncherPlus.Workspace";

    private static final boolean DEBUG = false;

    private static int COUNT_UPDATE_WALLPAPER = 0;

    private static final boolean ENABLE_GOOGLE_SMOOTH = false;

    private static final int INVALID_SCREEN = -1;
    
    private static final int CELL_MOVE_UP = -1;
    private static final int CELL_MOVE_DOWN = 1;

    /**
     * The velocity at which a fling gesture will cause us to snap to the next
     * screen
     */
    private static final int SNAP_VELOCITY = 256;

    private final WallpaperManager mWallpaperManager;

    private int mDefaultScreen;

    private boolean mFirstLayout = true;

    private int mCurrentScreen;

    private int mNextScreen = INVALID_SCREEN;

    private Scroller mScroller;

    private VelocityTracker mVelocityTracker;

    /**
     * CellInfo for the cell that is currently being dragged
     */
    private CellLayout.CellInfo mDragInfo;

    /**
     * Target drop area calculated during last acceptDrop call.
     */
    private int[] mTargetCell = null;

    private float mLastMotionX;

    private float mLastMotionY;

    private final static int TOUCH_STATE_REST = 0;

    private final static int TOUCH_STATE_SCROLLING = 1;

    private int mTouchState = TOUCH_STATE_REST;

    private OnLongClickListener mLongClickListener;

    private Launcher mLauncher;

    private IconCache mIconCache;

    private DragController mDragController;

    /**
     * Cache of vacant cells, used during drag events and invalidated as needed.
     */
    private CellLayout.CellInfo mVacantCache = null;

    private int[] mTempCell = new int[2];

    private int[] mTempEstimate = new int[2];

    private Rect mTempRect = new Rect();

    private boolean mAllowLongPress = true;

    private int mTouchSlop;

    private int mMaximumVelocity;
    
    private int mOverscrollDistance;

    private static final int INVALID_POINTER = -1;

    private int mActivePointerId = INVALID_POINTER;

    /* for google design to draw smoothly ,but effect is not good ,skip it */
    private static final float NANOTIME_DIV = 1000000000.0f;

    private static final float SMOOTHING_SPEED = 0.75f;

    private static final float SMOOTHING_CONSTANT = (float) (0.016 / Math.log(SMOOTHING_SPEED));

    private float mSmoothingTime;

    private float mTouchX;

    private WorkspaceOvershootInterpolator mScrollInterpolator;

    private static final float BASELINE_FLING_VELOCITY = 2500.f;

    private static final float FLING_VELOCITY_INFLUENCE = 0.06f;

    private Rect mTargetCellForShowGreen;

    private static final Paint mGrayAreaPaint = new Paint();
    static {
        mGrayAreaPaint.setStyle(Style.FILL);
        //mGrayAreaPaint.setAlpha(0xb0);
        mGrayAreaPaint.setAlpha(0x00);
        mGrayAreaPaint.setColorFilter(new PorterDuffColorFilter(/*Color.BLACK*/0x5f5f5f, PorterDuff.Mode.SRC_ATOP));
    }

    private boolean mDragging = false;

    private static final int PADDING = 20;

    private Paint mPaint;
    boolean mLiveWallpaperSupport = true;
    private int mWallpaperWidth;
    private int mWallpaperHeight;
    private float mWallpaperOffset;
    private boolean mWallpaperLoaded;
    BitmapDrawable mWallpaperDrawable;

    private DragLayer mDragLayer;
    private Toast mNoSpaceToast;
    
    private int mDropCellX = -1;
    private int mDropCellY = -1;
    private long mDragMoveingTime = 0;
    private final int DropReorderTime  = 600;

	private static boolean canSendMessage = true;
    
    private static class WorkspaceOvershootInterpolator implements Interpolator {
        private static final float DEFAULT_TENSION = 1.3f;

        private float mTension;

        public WorkspaceOvershootInterpolator() {
            mTension = DEFAULT_TENSION;
        }

        public void setDistance(int distance) {
            mTension = distance > 0 ? DEFAULT_TENSION / distance : DEFAULT_TENSION;
        }

        public void disableSettle() {
            mTension = 0.f;
        }

        public float getInterpolation(float t) {
            // _o(t) = t * t * ((tension + 1) * t + tension)
            // o(t) = _o(t - 1) + 1
            t -= 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        }
    }

    /**
     * Used to inflate the Workspace from XML.
     * 
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization
     *            values.
     */
    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Used to inflate the Workspace from XML.
     * 
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization
     *            values.
     * @param defStyle Unused.
     */
    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mWallpaperManager = WallpaperManager.getInstance(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Workspace, defStyle, 0);
        mDefaultScreen = a.getInt(R.styleable.Workspace_defaultScreen, 0);
        a.recycle();

        setHapticFeedbackEnabled(false);
        initWorkspace();
    }

    /**
     * Initializes various states for this workspace.
     */
    private void initWorkspace() {
        Context context = getContext();
        mScrollInterpolator = new WorkspaceOvershootInterpolator();
        mScroller = new Scroller(context, mScrollInterpolator);
        mCurrentScreen = mDefaultScreen;
        Launcher.setScreen(mCurrentScreen);
        LauncherApplication app = (LauncherApplication) context.getApplicationContext();
        mIconCache = app.getIconCache();

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mOverscrollDistance = configuration.getScaledOverscrollDistance();

        mPaint=new Paint();
        mPaint.setDither(false);

        mNoSpaceToast = Toast.makeText(context, context.getString(R.string.out_of_space), Toast.LENGTH_SHORT);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, index, params);
    }

    @Override
    public void addView(View child) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, width, height);
    }

    @Override
    public void addView(View child, LayoutParams params) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, params);
    }

    /**
     * @return The open folder on the current screen, or null if there is none
     */
    Folder getOpenFolder() {
        CellLayout currentScreen = (CellLayout) getChildAt(mCurrentScreen);
        int count = currentScreen.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = currentScreen.getChildAt(i);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
            if (lp.cellHSpan == 4 && lp.cellVSpan == 4 && child instanceof Folder) {
                return (Folder) child;
            }
        }
        return null;
    }

    Folder getOpenFolder(CellLayout cellLayout) {
        int count = cellLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = cellLayout.getChildAt(i);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
            if (lp.cellHSpan == 4 && lp.cellVSpan == 4 && child instanceof Folder) {
                return (Folder) child;
            }
        }
        return null;
    }

    ArrayList<Folder> getOpenFolders() {
        final int screens = getChildCount();
        ArrayList<Folder> folders = new ArrayList<Folder>(screens);

        for (int screen = 0; screen < screens; screen++) {
            CellLayout currentScreen = (CellLayout) getChildAt(screen);
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
                if (lp.cellHSpan == 4 && lp.cellVSpan == 4 && child instanceof Folder) {
                    folders.add((Folder) child);
                    break;
                }
            }
        }

        return folders;
    }

    boolean isDefaultScreenShowing() {
        return mCurrentScreen == mDefaultScreen;
    }

    /**
     * Returns the index of the currently displayed screen.
     * 
     * @return The index of the currently displayed screen.
     */
    int getCurrentScreen() {
        return mCurrentScreen;
    }

    /**
     * Returns how many screens there are.
     */
    int getScreenCount() {
        return getChildCount();
    }

     /* Sets the current screen.
     * 
     * @param currentScreen
     */
    void setCurrentScreen(int currentScreen) {
        if (!mScroller.isFinished()){
            mScroller.abortAnimation();            
        }
        clearVacantCache();
        mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
        scrollTo(mCurrentScreen * getWidth(), 0);
        if (mLiveWallpaperSupport) {
            updateWallpaperOffset();
        }
        invalidate();
    }

    /**
     * Adds the specified child in the current screen. The position and
     * dimension of the child are defined by x, y, spanX and spanY.
     * 
     * @param child The child to add in one of the workspace's screens.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    void addInCurrentScreen(View child, int x, int y, int spanX, int spanY) {
        addInScreen(child, mCurrentScreen, x, y, spanX, spanY, false);
    }

    /**
     * Adds the specified child in the current screen. The position and
     * dimension of the child are defined by x, y, spanX and spanY.
     * 
     * @param child The child to add in one of the workspace's screens.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the
     *            children list.
     */
    void addInCurrentScreen(View child, int x, int y, int spanX, int spanY, boolean insert) {
        addInScreen(child, mCurrentScreen, x, y, spanX, spanY, insert);
    }

    /**
     * Adds the specified child in the specified screen. The position and
     * dimension of the child are defined by x, y, spanX and spanY.
     * 
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    void addInScreen(View child, int screen, int x, int y, int spanX, int spanY) {
        addInScreen(child, screen, x, y, spanX, spanY, false);
    }

    /**
     * Adds the specified child in the specified screen. The position and
     * dimension of the child are defined by x, y, spanX and spanY.
     * 
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the
     *            children list.
     */
    void addInScreen(View child, int screen, int x, int y, int spanX, int spanY, boolean insert) {
        if (screen < 0 || screen >= getChildCount()) {
            Log.e(TAG, "The screen must be >= 0 and < " + getChildCount() + " (was " + screen
                    + "); skipping child");
            return;
        }

        clearVacantCache();

        final CellLayout group = (CellLayout) getChildAt(screen);
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
        } else {
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }
        group.addView(child, insert ? 0 : -1, lp);
        if (child instanceof ImageView )
        	return;
        if (!(child instanceof Folder)) {
            child.setHapticFeedbackEnabled(false);
            child.setOnLongClickListener(mLongClickListener);
        }
        if (child instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget) child);
        }
        if (!mLauncher.getPaused() && !mLauncher.isAllAppsVisible() && getCurrentScreen() == screen) {
            notifyScreenStateChanged(child, screen, ScreenStateChangeListener.SCREEN_ENTER);
        }
    }

    CellLayout.CellInfo findAllVacantCells(boolean[] occupied) {
        CellLayout group = (CellLayout) getChildAt(mCurrentScreen);
        if (group != null) {
            return group.findAllVacantCells(occupied, null);
        }
        return null;
    }

    private void clearVacantCache() {
        if (mVacantCache != null) {
            mVacantCache.clearVacantCells();
            mVacantCache = null;
        }
    }

    /**
     * Registers the specified listener on each screen contained in this
     * workspace.
     * 
     * @param l The listener used to respond to long clicks.
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListener = l;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setOnLongClickListener(l);
        }
    }

    private void updateWallpaperOffset() {
        if (DEBUG) {
            COUNT_UPDATE_WALLPAPER++;
        }

        updateWallpaperOffset(getChildAt(getChildCount() - 1).getRight() - (mRight - mLeft));
    }

    private void updateWallpaperOffset(int scrollRange) {
        IBinder token = getWindowToken();
        if (token != null) {
            mWallpaperManager.setWallpaperOffsetSteps(1.0f / (getChildCount() - 1), 0);
            mWallpaperManager.setWallpaperOffsets(getWindowToken(), Math.max(0.f, Math.min(mScrollX
                    / (float) scrollRange, 1.f)), 0);
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        if (ENABLE_GOOGLE_SMOOTH) {
            mTouchX = x;
            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (ENABLE_GOOGLE_SMOOTH) {
                mTouchX = mScrollX = mScroller.getCurrX();
                mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
            } else {
                mScrollX = mScroller.getCurrX();
            }
            mScrollY = mScroller.getCurrY();
            if (mLiveWallpaperSupport) {
                updateWallpaperOffset();
            }
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
			View whichHostView = getChildAt(mNextScreen);
			View whichMtkWidgetView = searchIMTKWidget(whichHostView);
			if (whichMtkWidgetView != null) {
				((IMTKWidget) whichMtkWidgetView).moveIn(mNextScreen);
				canSendMessage = true;
				if (Launcher.DEBUG_SURFACEWIDGET) Log.e(Launcher.TAG_SURFACEWIDGET, "moveIn");
			}
            int destination = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            boolean notify = mCurrentScreen != destination;
            if (notify) {
                notifyScreenStateChanged(getChildAt(mCurrentScreen), mCurrentScreen, ScreenStateChangeListener.SCREEN_LEAVE);
            }
            mCurrentScreen = destination;
            Launcher.setScreen(mCurrentScreen);
            mNextScreen = INVALID_SCREEN;
            clearChildrenCache();
            if (notify) {
                notifyScreenStateChanged(getChildAt(mCurrentScreen), mCurrentScreen, ScreenStateChangeListener.SCREEN_ENTER);
            }
            if (!(mTouchState == TOUCH_STATE_SCROLLING)) {
                // mDragLayer.setScrolling(false);
                if (mDragging) {
                    boolean acceptFlag = acceptDrop(null, 0, 0, 0, 0, null, mDragController.getDragInfo());
                    if (!acceptFlag) {
                        mNoSpaceToast.show();
                    } else {
                        mNoSpaceToast.cancel();
                    }
                }
            }
        } else if (ENABLE_GOOGLE_SMOOTH && mTouchState == TOUCH_STATE_SCROLLING) {
            final float now = System.nanoTime() / NANOTIME_DIV;
            final float e = (float) Math.exp((now - mSmoothingTime) / SMOOTHING_CONSTANT);
            final float dx = mTouchX - mScrollX;
            mScrollX += dx * e;
            mSmoothingTime = now;

            // Keep generating points as long as we're more than 1px away from
            // the target
            if (dx > 1.f || dx < -1.f) {
                if (mLiveWallpaperSupport) {
                    updateWallpaperOffset();
                }
                postInvalidate();
            }
        }
		mLauncher.updateIndicatorPosition();
    }

    private void notifyScreenStateChanged(View view, int screen, int state) {
        if (view instanceof ScreenStateChangeListener) {
            ((ScreenStateChangeListener) view).onScreenStateChanged(screen, state);
        } else {
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int index = 0; index < group.getChildCount(); ++index) {
                    notifyScreenStateChanged(group.getChildAt(index), screen, state);
                }
            }
        }
    }

    void notifyScreenStateChanged(View view, CellLayout cellLayout, int state) {
        for (int index = 0; index < getScreenCount(); ++index) {
            if (getChildAt(index) == cellLayout) {
                if (index == getCurrentScreen()) {
                    notifyScreenStateChanged(view, index, state);
                }
                return;
            }
        }
    }

    void notifyCurrentScreenEnter() {
        notifyScreenStateChanged(getChildAt(mCurrentScreen), mCurrentScreen, ScreenStateChangeListener.SCREEN_ENTER);
    }
    
    void notifyCurrentScreenLeave() {
        notifyScreenStateChanged(getChildAt(mCurrentScreen), mCurrentScreen, ScreenStateChangeListener.SCREEN_LEAVE);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        boolean restore = false;
        int restoreCount = 0;
        if (!mLiveWallpaperSupport && mWallpaperDrawable != null) {
            float x = getScrollX() * mWallpaperOffset;
            if (x + mWallpaperWidth < getRight() - getLeft()) {
                x = getRight() - getLeft() - mWallpaperWidth;
            }
            // Added tweaks for when scrolling "beyond bounce limits" :P
            if (mScrollX < 0) {
                x = mScrollX;
            }
            if (mScrollX > getChildAt(getChildCount() - 1).getRight() - (mRight - mLeft)) {
                x = (mScrollX - mWallpaperWidth + (mRight - mLeft));
            }
            // lets center the wallpaper when there's only one screen...
            if (getChildCount() == 1) {
                x = (getScrollX() - (mWallpaperWidth / 2) + (getRight() / 2));
            }
            canvas.drawBitmap(mWallpaperDrawable.getBitmap(), x, getBottom() - mWallpaperHeight, mPaint);
        }

        // ViewGroup.dispatchDraw() supports many features we don't need:
        // clip to padding, layout animation, animation listener, disappearing
        // children, etc. The following implementation attempts to fast-track
        // the drawing dispatch by drawing only what we know needs to be drawn.

        boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING && mNextScreen == INVALID_SCREEN;
        // If we are not scrolling or flinging, draw only the current screen
        if (fastDraw) {
            drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
            if (mDragging) {
                // draw gray area for current screen
                drawGrayAreaForItem(canvas, (CellLayout) getChildAt(mCurrentScreen), true);
            }
        } else {
            final long drawingTime = getDrawingTime();
            final float scrollPos = (float) mScrollX / getWidth();
            final int leftScreen = (int) scrollPos;
            final int rightScreen = leftScreen + 1;
            if (leftScreen >= 0) {
                drawChild(canvas, getChildAt(leftScreen), drawingTime);
            }
            if (scrollPos != leftScreen && rightScreen < getChildCount()) {
                drawChild(canvas, getChildAt(rightScreen), drawingTime);
            }
        }

        if (restore) {
            canvas.restoreToCount(restoreCount);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        computeScroll();
        mDragController.setWindowToken(getWindowToken());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        // The children are given the same width and height as the workspace
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        if (!mLiveWallpaperSupport) {
            if (mWallpaperLoaded) {
                mWallpaperLoaded = false;
                mWallpaperWidth = mWallpaperDrawable.getIntrinsicWidth();
                mWallpaperHeight = mWallpaperDrawable.getIntrinsicHeight();
            }

            final int wallpaperWidth = mWallpaperWidth;
            mWallpaperOffset = wallpaperWidth > width ? (count * width - wallpaperWidth) / ((count - 1) * (float) width) : 1.0f;
        }

        if (mFirstLayout) {
            setHorizontalScrollBarEnabled(false);
            scrollTo(mCurrentScreen * width, 0);
            setHorizontalScrollBarEnabled(true);
            if (mLiveWallpaperSupport) {
                updateWallpaperOffset(width * (getChildCount() - 1));
            }
            mFirstLayout = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        int screen = indexOfChild(child);
        if (screen != mCurrentScreen || !mScroller.isFinished()) {
            if (!mLauncher.isWorkspaceLocked()) {
                snapToScreen(screen);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if (!mLauncher.isAllAppsVisible()) {
            final Folder openFolder = getOpenFolder();
            if (openFolder != null) {
                return openFolder.requestFocus(direction, previouslyFocusedRect);
            } else {
                int focusableScreen;
                if (mNextScreen != INVALID_SCREEN) {
                    focusableScreen = mNextScreen;
                } else {
                    focusableScreen = mCurrentScreen;
                }
                getChildAt(focusableScreen).requestFocus(direction, previouslyFocusedRect);
            }
        }
        return false;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (direction == View.FOCUS_LEFT) {
            if (getCurrentScreen() > 0) {
                snapToScreen(getCurrentScreen() - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (getCurrentScreen() < getChildCount() - 1) {
                snapToScreen(getCurrentScreen() + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (!mLauncher.isAllAppsVisible()) {
            final Folder openFolder = getOpenFolder();
            if (openFolder == null) {
                getChildAt(mCurrentScreen).addFocusables(views, direction);
                if (direction == View.FOCUS_LEFT) {
                    if (mCurrentScreen > 0) {
                        getChildAt(mCurrentScreen - 1).addFocusables(views, direction);
                    }
                } else if (direction == View.FOCUS_RIGHT) {
                    if (mCurrentScreen < getChildCount() - 1) {
                        getChildAt(mCurrentScreen + 1).addFocusables(views, direction);
                    }
                }
            } else {
                openFolder.addFocusables(views, direction);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			if (mLauncher.isFolderOpen()) {
				mLauncher.closeFolder();
			}
            if (mLauncher.isWorkspaceLocked() || mLauncher.isAllAppsVisible()) {
                return false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final boolean workspaceLocked = mLauncher.isWorkspaceLocked();
        final boolean allAppsVisible = mLauncher.isAllAppsVisible();
        if (workspaceLocked || allAppsVisible) {
            return false; // We don't want the events. Let them fall through to
            // the all apps view.
        }

        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging state
         * and he is moving his finger. We want to intercept this motion.
         */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have
                 * caught it. Check whether the user has moved far enough from
                 * his original down touch.
                 */

                /*
                 * Locally do absolute value. mLastMotionX is set to the y value
                 * of the down event.
                 */
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);

                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > touchSlop;
                boolean yMoved = yDiff > touchSlop;

                if (xMoved || yMoved) {
				if (Launcher.isInEditMode()) {
					CellLayout cellLayout = (CellLayout) getChildAt(mCurrentScreen);
					Folder folder = getOpenFolder(cellLayout);
					if (folder == null) {
						int[] cellXY = new int[2];
						cellLayout.pointToCellExact((int) mLastMotionX,	(int) mLastMotionY, cellXY);
						View v = getChildAtPosition(mCurrentScreen, cellXY);

						if (v != null) {
							Object tag = v.getTag();

							if (tag instanceof ShortcutInfo) {
								if (ShortcutInfo.isMoreApp((ShortcutInfo) tag)) {
									return false;
								}
							}

							if (!mLauncher.isClickOnKillRect(v)) {
								CellLayout.CellInfo cellInfo = new CellLayout.CellInfo();
								cellInfo.cell = v;
								cellInfo.cellX = cellXY[0];
								cellInfo.cellY = cellXY[1];
								cellInfo.spanX = 1;
								cellInfo.spanY = 1;
								startDrag(cellInfo);
								return false;
							}
						}
					}
				}
                    if (xDiff > yDiff) {
                        // Scroll if the user moved far enough along the X axis
                        mTouchState = TOUCH_STATE_SCROLLING;
                        if (ENABLE_GOOGLE_SMOOTH) {
                            mTouchX = mScrollX;
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                        }
                        enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
                        //mLastMotionX = x -mLastMotionX > 0 ? x - 1 : x + 1;
                        // mDragLayer.setScrolling(true);
                        // Avoid this event losing 
                        onTouchEvent(ev);
                    }
                    // Either way, cancel any pending longpress
                    if (mAllowLongPress) {
                        mAllowLongPress = false;
                        // Try canceling the long press. It could also have been
                        // scheduled
                        // by a distant descendant, so use the mAllowLongPress
                        // flag to block
                        // everything
                        final View currentScreen = getChildAt(mCurrentScreen);
                        currentScreen.cancelLongPress();
                    }
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                // Remember location of down touch
                mLastMotionX = x;
                mLastMotionY = y;
                mActivePointerId = ev.getPointerId(0);
                mAllowLongPress = true;

                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't. mScroller.isFinished should be false when
                 * being flinged.
                 */
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                if (mTouchState != TOUCH_STATE_SCROLLING) {
                    final CellLayout currentScreen = (CellLayout) getChildAt(mCurrentScreen);
                    if (!currentScreen.lastDownOnOccupiedCell()) {
                        getLocationOnScreen(mTempCell);
                        // Send a tap to the wallpaper if the last down was on
                        // empty space
                        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                        if (mLiveWallpaperSupport) { 
                            mWallpaperManager.sendWallpaperCommand(getWindowToken(),
                                    "android.wallpaper.tap",
                                    mTempCell[0] + (int) ev.getX(pointerIndex),
                                    mTempCell[1] + (int) ev.getY(pointerIndex), 0, null);
                        }
                    }
                }
                // Release the drag
                clearChildrenCache();
                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                mAllowLongPress = false;

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mTouchState != TOUCH_STATE_REST;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = ev.getX(newPointerIndex);
            mLastMotionY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    /**
     * If one of our descendant views decides that it could be focused now, only
     * pass that along if it's on the current screen. This happens when live
     * folders requery, and if they're off screen, they end up calling
     * requestFocus, which pulls it on screen.
     */
    @Override
    public void focusableViewAvailable(View focused) {
        View current = getChildAt(mCurrentScreen);
        View v = focused;
        while (true) {
            if (v == current) {
                super.focusableViewAvailable(focused);
                return;
            }
            if (v == this) {
                return;
            }
            ViewParent parent = v.getParent();
            if (parent instanceof View) {
                v = (View) v.getParent();
            } else {
                return;
            }
        }
    }

    public void hideWallpaper(boolean hide) {
        IBinder windowToken = getWindowToken();
        if (windowToken != null) {
            if (hide) {
                mWallpaperManager.sendWallpaperCommand(windowToken, "hide", 0, 0, 0, null);

            } else {
                mWallpaperManager.sendWallpaperCommand(windowToken, "show", 0, 0, 0, null);
            }
        }
    }

    /**
     * Make a local copy of wallpaper bitmap to use instead wallpapermanager
     * only when detected not being a Live Wallpaper
     */
    public void setWallpaper(boolean fromIntentReceiver) {
        // Just for Live Wallpaper
        if (mWallpaperManager.getWallpaperInfo() != null) {
            mWallpaperDrawable = null;
            mWallpaperLoaded = false;
            mLiveWallpaperSupport = true;
        } else {
            /*if (fromIntentReceiver || mWallpaperDrawable == null) {
                final Drawable drawable = mWallpaperManager.getDrawable();
                mWallpaperDrawable = (BitmapDrawable) drawable;
                mWallpaperLoaded = true;
            }*/
            mLiveWallpaperSupport = true;
        }
        mLauncher.setWindowBackground(mLiveWallpaperSupport);
        invalidate();
        requestLayout();
    }

    void enableChildrenCache(int fromScreen, int toScreen) {
        if (fromScreen > toScreen) {
            final int temp = fromScreen;
            fromScreen = toScreen;
            toScreen = temp;
        }

        final int count = getChildCount();

        fromScreen = Math.max(fromScreen, 0);
        toScreen = Math.min(toScreen, count - 1);

        for (int i = fromScreen; i <= toScreen; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(true);
            layout.setChildrenDrawingCacheEnabled(true);
        }
    }

    void clearChildrenCache() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (mLauncher.isWorkspaceLocked()) {
            return false; // We don't want the events. Let them fall through to
            // the all apps view.
        }
        if (mLauncher.isAllAppsVisible()) {
            // Cancel any scrolling that is in progress.
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            snapToScreen(mCurrentScreen);
            return false; // We don't want the events. Let them fall through to
            // the all apps view.
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                // Remember where the motion event started
                mLastMotionX = ev.getX();
                mActivePointerId = ev.getPointerId(0);
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
                }
                break;
            case MotionEvent.ACTION_MOVE:
				if (mTouchState == TOUCH_STATE_SCROLLING) {
                    // Scroll to follow the motion event
                    final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    final float x = ev.getX(pointerIndex);
                    final float deltaX = mLastMotionX - x;
                    mLastMotionX = x;

					if (canSendMessage && ! mDragController.isDraging() ) {
	                    View currentHostView = getChildAt(mCurrentScreen);
	                    View currentMtkWidgetView = searchIMTKWidget(currentHostView);
	                    
	                    if (currentMtkWidgetView != null) {
	                        boolean result = ((IMTKWidget) currentMtkWidgetView).moveOut(deltaX > 0 ? 1 : -1);
	                        if (result == false) {
	                            if (Launcher.DEBUG_SURFACEWIDGET) Log.e(Launcher.TAG_SURFACEWIDGET, "moveOut false");
	                            return true;
	                        }
	                        canSendMessage = false;
	                        if (Launcher.DEBUG_SURFACEWIDGET) Log.e(Launcher.TAG_SURFACEWIDGET, "moveOut true");
	                    }
	                }

                    if (deltaX < 0) {
                        if (ENABLE_GOOGLE_SMOOTH) {
                            if (mTouchX > -mOverscrollDistance) {
                                mTouchX += Math.max(-mOverscrollDistance, deltaX);
                                mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                                invalidate();
                            }
                        } else {
							if (mScrollX > -mOverscrollDistance) {
								scrollBy((int) Math.max(-mOverscrollDistance, deltaX), 0);
								if (mLiveWallpaperSupport) {
									updateWallpaperOffset();
								}
							}
                        }
                    } else if (deltaX > 0) {
                        final int availableToScroll = getChildAt(getChildCount() - 1).getRight()
                                - mScrollX - getWidth() + mOverscrollDistance;
                        if (availableToScroll >  0) {
                            if (ENABLE_GOOGLE_SMOOTH) {
                                mTouchX += Math.min(mOverscrollDistance, deltaX);
                                mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                                invalidate();
                            } else {
                                scrollBy((int) Math.min(mOverscrollDistance, deltaX), 0);
                                if (mLiveWallpaperSupport) {
                                    updateWallpaperOffset();
                                }
                            }
                        }
                    } else {
                        if (ENABLE_GOOGLE_SMOOTH) {
                            awakenScrollBars();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    final int velocityX = (int) velocityTracker.getXVelocity(mActivePointerId);

                    final int screenWidth = getWidth();
                    final int whichScreen = (mScrollX + (screenWidth / 2)) / screenWidth;
                    final float scrolledPos = (float) mScrollX / screenWidth;
                    if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                        // Fling hard enough to move left.
                        // Don't fling across more than one screen at a time.
                        final int bound = scrolledPos < whichScreen ? mCurrentScreen - 1
                                : mCurrentScreen;
                        snapToScreen(Math.min(whichScreen, bound), velocityX, false);
                    } else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
                        // Fling hard enough to move right
                        // Don't fling across more than one screen at a time.
                        final int bound = scrolledPos > whichScreen ? mCurrentScreen + 1
                                : mCurrentScreen;
                        snapToScreen(Math.max(whichScreen, bound), velocityX, false);
                    } else {
                        snapToScreen(whichScreen, 0, false);
                    }

                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }
                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        return true;
    }

    void snapToScreen(int whichScreen) {
        snapToScreen(whichScreen, 0, false);
    }

    private void snapToScreen(int whichScreen, int velocity, boolean settle) {
        if (DEBUG) {
            Log.d(TAG, "snapToScreen COUNT_UPDATE_WALLPAPER == " + COUNT_UPDATE_WALLPAPER);
            COUNT_UPDATE_WALLPAPER = 0;
        }
		// if (!mDragLayer.getScrolling()) {
		// mDragLayer.setScrolling(true);
		// }

        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
             
        clearVacantCache();
        enableChildrenCache(mCurrentScreen, whichScreen);

        mNextScreen = whichScreen;

        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichScreen != mCurrentScreen
                && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }

        final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
        final int newX = whichScreen * getWidth();
        final int delta = newX - mScrollX;
        int duration = (screenDelta + 1) * 100;

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        } 

        if (settle) {
            mScrollInterpolator.setDistance(screenDelta);
        } else {
            mScrollInterpolator.disableSettle();
        }

        velocity = Math.abs(velocity);
		if (mScrollX < 0 || mScrollX + getWidth() - getChildAt(getChildCount() - 1).getRight() > 0) {
			// bounce time
			duration = 450;
		} else if (velocity > 0) {
            duration += (duration / (velocity / BASELINE_FLING_VELOCITY)) * FLING_VELOCITY_INFLUENCE;
        } else {
            duration += 32;
        }
        
        if (Launcher.isShortcutScreen()) {
			mLauncher.mPageClassification.setText(Launcher.pageClassification[mNextScreen]);
        }

		controlMTKWidget(whichScreen);

        if (ENABLE_GOOGLE_SMOOTH) {
            awakenScrollBars(duration);
        }
        
        mScroller.startScroll(mScrollX, 0, delta, 0, duration);
        invalidate();
    }

    void startDrag(CellLayout.CellInfo cellInfo) {
        View child = cellInfo.cell;

        // Make sure the drag was started by a long press as opposed to a long
        // click.
        if (!child.isInTouchMode()) {
            return;
        }

        mDragInfo = cellInfo;
        mDragInfo.screen = mCurrentScreen;

        CellLayout current = ((CellLayout) getChildAt(mCurrentScreen));

        current.onDragChild(child);
        mDragController.startDrag(child, this, child.getTag(), DragController.DRAG_ACTION_MOVE);
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final SavedState state = new SavedState(super.onSaveInstanceState());
        state.currentScreen = mCurrentScreen;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.currentScreen != -1) {
            if(mLauncher.localeChanged() == false) {
                mCurrentScreen = savedState.currentScreen;
            } else {
                mCurrentScreen = mDefaultScreen;
            }
            Launcher.setScreen(mCurrentScreen);
        }
    }

    void addApplicationShortcut(ShortcutInfo info, CellLayout.CellInfo cellInfo) {
        addApplicationShortcut(info, cellInfo, false);
    }

    void addApplicationShortcut(ShortcutInfo info, CellLayout.CellInfo cellInfo,
            boolean insertAtFirst) {
    	final CellLayout layout = (CellLayout) getChildAt(cellInfo.screen);
	    final int[] cellXY = new int[2];
	
	    layout.cellToPoint(cellInfo.cellX, cellInfo.cellY, cellXY);
	    onDropExternal(cellXY[0], cellXY[1], 0, 0, info, layout, insertAtFirst);
    }

    private View getChildAtPosition(int screen, int[] cellXY) {
    	Workspace workspace = mLauncher.getShortcutWorkspace();
    	final CellLayout cellLayout = (CellLayout) workspace.getChildAt( screen );
    	
    	final int count = cellLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = (View) cellLayout.getChildAt(i);
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
			if (cellXY[0] == lp.cellX && cellXY[1] == lp.cellY) {
				return child;
			}
		}
    	return null;
    }
    
    private boolean createMoreFolder(View cell, int screen, int x, int y, Object dragInfo ) {
    	Workspace workspace = mLauncher.getShortcutWorkspace();
    	final CellLayout cellLayout = (CellLayout)workspace.getChildAt(screen);
    	int count = cellLayout.getChildCount();
    	
		if (dragInfo instanceof ShortcutInfo) {
			final ShortcutInfo info = (ShortcutInfo) dragInfo;
			if (mTargetCell == null) {
				int[] cellXY = new int[2];
				cellLayout.pointToCellExact(x, y, cellXY);
				View targetView = getChildAtPosition(screen,cellXY);
				if (targetView != null && targetView != cell  && (targetView.getTag() instanceof ShortcutInfo)) {
					ShortcutInfo targetInfo = (ShortcutInfo) targetView.getTag();
					if (!ShortcutInfo.isMoreApp(targetInfo)) {
						if (screen != info.screen) {
		                    final CellLayout originalCellLayout = (CellLayout) getChildAt(info.screen);
		                    originalCellLayout.removeView(cell);
		                } else {
		                	cellLayout.removeView(cell);
		                }
						cellLayout.removeView(targetView);
						
						FolderIcon folderIcon = mLauncher.addFolder(screen,targetInfo.cellX, targetInfo.cellY);
						folderIcon.onDrop(null, 0, 0, 0, 0, null, info);
						folderIcon.onDrop(null, 0, 0, 0, 0, null, targetInfo);
						
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				if (count == Launcher.CELL_COUNT && !mLauncher.hasMoreFolder(screen)) {
					cellLayout.removeView(cell);
				
					FolderIcon folderIcon = mLauncher.addFolder(screen,mTargetCell[0], mTargetCell[1]);
					folderIcon.onDrop(null, 0, 0, 0, 0, null, info);
				
					return false;
				}
			}
    	}  else if(dragInfo instanceof UserFolderInfo){
    		final UserFolderInfo info = (UserFolderInfo) dragInfo;
    		if ( mTargetCell == null ) return false;
    		if (count == Launcher.CELL_COUNT && !mLauncher.hasMoreFolder(screen)) {
					mLauncher.changeFolderName( (UserFolderInfo)dragInfo, Launcher.MORE_FOLDER);
				}
    			return true;
    	}
		return true;
    }
    
    public boolean onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        final CellLayout cellLayout = getCurrentDropLayout();
        if (source != this) {
            return onDropExternal(x , y , xOffset, yOffset, dragInfo, cellLayout);
        } else {
            // Move internally
            if (mDragInfo != null) {
                final View cell = mDragInfo.cell;
                mTargetCell = estimateDropCell(x - xOffset, y - yOffset, mDragInfo.spanX,
                        mDragInfo.spanY, cell, cellLayout, mTargetCell);
                
				boolean isDrop = true;
				int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;
                				
				if (mTargetCell == null) {
                	if ( mLauncher.isDefStyle() || !mLauncher.isShortcutScreen()) {
                		return false;
                	} else {
                		return createMoreFolder(cell, index, x, y, dragInfo);
                	}
                }
				
                if (index != mDragInfo.screen) {
					if (cell.getLayoutParams() instanceof CellLayout.LayoutParams) {
						final CellLayout originalCellLayout = (CellLayout) getChildAt(mDragInfo.screen);
						originalCellLayout.removeView(cell);
						cellLayout.addView(cell);
                    } else {
						// If no room to place a widget on HomeScreen when adding, it will startDrag and the
                    	// widget contains a WM.LayoutParams. Here needs to reset it with a CellLayout.LayoutParams.
						CellLayout.LayoutParams lp = new CellLayout.LayoutParams(cell.getLayoutParams());
						cell.setLayoutParams(lp);
						mLauncher.addInScreen(cell);
                    }
                }

				View mtkWidgetView = searchIMTKWidget(cell);
                if (mtkWidgetView != null) {
                	((IMTKWidget)mtkWidgetView).setScreen(index);
                	((IMTKWidget)mtkWidgetView).stopDrag();
                    if (Launcher.DEBUG_SURFACEWIDGET) Log.e(Launcher.TAG_SURFACEWIDGET, "stopDrag");
                }
                
                if (mLauncher.isShortcutScreen()) {
                	int count = cellLayout.getChildCount();
                	if ( count == Launcher.CELL_COUNT ) {
                		isDrop = createMoreFolder(cell, index, x, y, dragInfo);
                	}
                }
                if (isDrop) {
                	cellLayout.onDropChild(cell, mTargetCell);

                	final ItemInfo info = (ItemInfo) cell.getTag();
                	CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                	if (mDragController.getDragActionMode() == DragController.DRAG_ACTION_ADD) {
                		LauncherModel.addItemToDatabase(mLauncher, info, LauncherSettings.Favorites.CONTAINER_DESKTOP, 
                				mCurrentScreen, lp.cellX, lp.cellY, false);
                		LauncherModel model = mLauncher.getModel();
                        if (info instanceof LauncherAppWidgetInfo) {
                            model.addDesktopAppWidget((LauncherAppWidgetInfo) info);
                        }
                		return true;
                	}

                	if (mDragInfo.cellX != lp.cellX || mDragInfo.cellY != lp.cellY
                			|| mDragInfo.screen != mCurrentScreen) {
                		LauncherModel.moveItemInDatabase(mLauncher, info, LauncherSettings.Favorites.CONTAINER_DESKTOP, 
                				index, lp.cellX, lp.cellY);
                	}
                }
            }
        }

        return true;
    }

    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        clearVacantCache();
    }

    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }

    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        clearVacantCache();
        ((CellLayout) getChildAt(mCurrentScreen)).setCellForGreenRect(null);
    }

    private boolean onDropExternal(int x, int y, int xOffset, int yOffset, Object dragInfo, CellLayout cellLayout) {
        return onDropExternal(x, y, xOffset, yOffset, dragInfo, cellLayout, false);
    }

    private boolean onDropExternal(int x, int y, int xOffset, int yOffset, Object dragInfo, CellLayout cellLayout,
            boolean insertAtFirst) {
        // Drag from somewhere else
        ItemInfo info = (ItemInfo) dragInfo;

        View view;

        switch (info.itemType) {
            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                if (info.container == NO_ID && info instanceof ApplicationInfo) {
                    // Came from all apps -- make a copy
                    info = new ShortcutInfo((ApplicationInfo) info);
                }
                view = mLauncher.createShortcut(R.layout.application, cellLayout,
                        (ShortcutInfo) info);
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
                view = FolderIcon.fromXml(R.layout.folder_icon, mLauncher,
                        (ViewGroup) getChildAt(mCurrentScreen), ((UserFolderInfo) info));
                break;
            default:
                throw new IllegalStateException("Unknown item type: " + info.itemType);
        }
        
        mTargetCell = estimateDropCell(x-xOffset, y-yOffset, 1, 1, view, cellLayout, mTargetCell);
        if(mTargetCell == null){
        	if (mLauncher.isDefStyle()) {
        		return false;
        	} else {
        		if ( view != null && info instanceof ShortcutInfo) {
        			return createMoreFolder(view, insertAtFirst ? info.screen : mCurrentScreen, x, y, dragInfo);
        		}
        	}
        }
        
        if (view == null) {
            // do not add the view if view is null.
            return true;
        }
        
        cellLayout.addView(view, insertAtFirst ? 0 : -1);
        view.setHapticFeedbackEnabled(false);
        view.setOnLongClickListener(mLongClickListener);
        if (view instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget) view);
        }
        
        boolean isDrop = true;
        
        if ( !mLauncher.isDefStyle() ) { 
        	int count = cellLayout.getChildCount();
        	if ( count == Launcher.CELL_COUNT )
        		isDrop = createMoreFolder(view, insertAtFirst ? info.screen : mCurrentScreen, mTargetCell[0], mTargetCell[1], dragInfo);
		}
        
        if (isDrop) {
        	cellLayout.onDropChild(view, mTargetCell);
        	CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();

        	LauncherModel.addOrMoveItemInDatabase(mLauncher, info,
        			LauncherSettings.Favorites.CONTAINER_DESKTOP, mCurrentScreen, lp.cellX, lp.cellY);
        }
        
        return true;
    }

    /**
     * Return the current {@link CellLayout}, correctly picking the destination
     * screen while a scroll is in progress.
     */
    private CellLayout getCurrentDropLayout() {
        int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;
        return (CellLayout) getChildAt(index);
    }

	/**
	 * Return the screen, correctly picking the destination screen while drop a
	 * item is in progress.
	 */
	public int getDropScreen() {
		return mScroller.isFinished() ? mCurrentScreen : mNextScreen;
	}

    /**
     * {@inheritDoc}
     */
    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
		final CellLayout layout = getCurrentDropLayout();
		final CellLayout.CellInfo cellInfo = mDragInfo;
		final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
		final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
    	if ( mLauncher.isDefStyle() || !mLauncher.isShortcutScreen()) {
    		if (mVacantCache == null) {
    			final View ignoreView = cellInfo == null ? null : cellInfo.cell;
    			mVacantCache = layout.findAllVacantCells(null, ignoreView);
    		}

    		return mVacantCache.findCellForSpan(mTempEstimate, spanX, spanY, false);
    	} else {
    		return true;//(mLauncher.findIdleCell(cellInfo) || (mLauncher.findMoreFolder(Launcher.MORE_FOLDER, cellInfo.screen)) != null);
    	}
    }

    /**
     * {@inheritDoc}
     */
    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo, Rect recycle) {
        final CellLayout layout = getCurrentDropLayout();

        final CellLayout.CellInfo cellInfo = mDragInfo;
        final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
        final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
        final View ignoreView = cellInfo == null ? null : cellInfo.cell;

        final Rect location = recycle != null ? recycle : new Rect();

        // Find drop cell and convert into rectangle
        int[] dropCell = estimateDropCell(x - xOffset, y - yOffset, spanX, spanY, ignoreView,
                layout, mTempCell);

        if (dropCell == null) {
            return null;
        }

        layout.cellToPoint(dropCell[0], dropCell[1], mTempEstimate);
        location.left = mTempEstimate[0];
        location.top = mTempEstimate[1];

        layout.cellToPoint(dropCell[0] + spanX, dropCell[1] + spanY, mTempEstimate);
        location.right = mTempEstimate[0];
        location.bottom = mTempEstimate[1];

        return location;
    }

    /**
     * Calculate the nearest cell where the given object would be dropped.
     */
    private int[] estimateDropCell(int pixelX, int pixelY, int spanX, int spanY, View ignoreView,
            CellLayout layout, int[] recycle) {
        // Create vacant cell cache if none exists
        if (mVacantCache == null) {
            mVacantCache = layout.findAllVacantCells(null, ignoreView);
        }

        // Find the best target drop location
        return layout.findNearestVacantArea(pixelX, pixelY, spanX, spanY, mVacantCache, recycle);
    }

    void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    void setDragLayer(DragLayer dragLayer) {
        mDragLayer = dragLayer;
    }

    public void setDragController(DragController dragController) {
        mDragController = dragController;
    }

    public void onDropCompleted(View target, boolean success) {
        clearVacantCache();

        if (success) {
            if (target != this && mDragInfo != null) {
                final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
                cellLayout.removeView(mDragInfo.cell);
                if (mDragInfo.cell instanceof DropTarget) {
                    mDragController.removeDropTarget((DropTarget) mDragInfo.cell);
                }
                // final Object tag = mDragInfo.cell.getTag();
            }
        } else {
            if (mDragInfo != null) {
            	if (Launcher.isShortcutScreen()) {
					int cellXY[] = new int[] { mDragInfo.cellX, mDragInfo.cellY };
					if (getChildAtPosition(mDragInfo.screen, cellXY) != null) {
						View cell = mDragInfo.cell;
						CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
						final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
						cellLayout.removeView(mDragInfo.cell);
						CellLayout.CellInfo cellInfo = mLauncher.findIdleCell();
						lp.cellX = cellInfo.cellX;
						lp.cellY = cellInfo.cellY;
						cellLayout.addView(cell);

						final ItemInfo info = (ItemInfo) cell.getTag();
						if (mDragInfo.cellX != lp.cellX	|| mDragInfo.cellY != lp.cellY || mDragInfo.screen != mCurrentScreen) {
							LauncherModel.moveItemInDatabase(mLauncher,	info, LauncherSettings.Favorites.CONTAINER_DESKTOP,
											mDragInfo.screen, lp.cellX,	lp.cellY);
						}
					}
            	}
            	final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
                cellLayout.onDropAborted(mDragInfo.cell);
            }
        }

        mDragInfo = null;
        mDragging = false;
    }

	protected void controlMTKWidget(int whichScreen) {
		if (canSendMessage && ! mDragController.isDraging() ) {
			View currentHostView = getChildAt(mCurrentScreen);
			View currentMtkWidgetView = searchIMTKWidget(currentHostView);
			if (currentMtkWidgetView != null) {
				int screen = ((IMTKWidget) currentMtkWidgetView).getScreen();
				((IMTKWidget) currentMtkWidgetView).moveOut((whichScreen - screen) > 0 ? 1 : -1);
				canSendMessage = false;
				if (Launcher.DEBUG_SURFACEWIDGET) Log.e(Launcher.TAG_SURFACEWIDGET, "moveOut");
			}
		}
    }

    public void scrollLeft() {
        clearVacantCache();
//        if (mScroller.isFinished()) {
//            if (mCurrentScreen > 0){
//                snapToScreen(mCurrentScreen - 1);
//            }
//        } else {
//            if (mNextScreen > 0){
//                snapToScreen(mNextScreen - 1);
//            }
//        }
		if (mCurrentScreen > 0) {
			snapToScreen(mCurrentScreen - 1);
		}
        mDragMoveingTime = System.currentTimeMillis();
    }

    public void scrollRight() {
        clearVacantCache();
//        if (mScroller.isFinished()) {
//            if (mCurrentScreen < getChildCount() - 1){
//                snapToScreen(mCurrentScreen + 1);
//            }
//        } else {
//            if (mNextScreen < getChildCount() - 1){
//                snapToScreen(mNextScreen + 1);
//            }
//        }
		if (mCurrentScreen < getChildCount() - 1) {
			snapToScreen(mCurrentScreen + 1);
		}
        mDragMoveingTime = System.currentTimeMillis();
    }

    public int getScreenForView(View v) {
        int result = -1;
        if (v != null) {
            ViewParent vp = v.getParent();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (vp == getChildAt(i)) {
                    return i;
                }
            }
        }
        return result;
    }

    public Folder getFolderForTag(Object tag) {
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            CellLayout currentScreen = ((CellLayout) getChildAt(screen));
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
                if (lp.cellHSpan == 4 && lp.cellVSpan == 4 && child instanceof Folder) {
                    Folder f = (Folder) child;
                    if (f.getInfo() == tag) {
                        return f;
                    }
                }
            }
        }
        return null;
    }

    public View getViewForTag(Object tag) {
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            CellLayout currentScreen = ((CellLayout) getChildAt(screen));
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                if (child.getTag() == tag) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * @return True is long presses are still allowed for the current touch
     */
    public boolean allowLongPress() {
        return mAllowLongPress;
    }

    /**
     * Set true to allow long-press events to be triggered, usually checked by
     * {@link Launcher} to accept or block dpad-initiated long-presses.
     */
    public void setAllowLongPress(boolean allowLongPress) {
        mAllowLongPress = allowLongPress;
    }

    void removeItems(final ArrayList<ApplicationInfo> apps) {
        final int count = getChildCount();
        final PackageManager manager = getContext().getPackageManager();
        final AppWidgetManager widgets = AppWidgetManager.getInstance(getContext());

        final HashSet<String> packageNames = new HashSet<String>();
        final int appCount = apps.size();
        for (int i = 0; i < appCount; i++) {
            packageNames.add(apps.get(i).componentName.getPackageName());
        }

        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);

            // Avoid ANRs by treating each screen separately
            post(new Runnable() {
                public void run() {
                    final ArrayList<View> childrenToRemove = new ArrayList<View>();
                    childrenToRemove.clear();

                    int childCount = layout.getChildCount();
                    for (int j = 0; j < childCount; j++) {
                        final View view = layout.getChildAt(j);
                        Object tag = view.getTag();

                        if (tag instanceof ShortcutInfo) {
                            final ShortcutInfo info = (ShortcutInfo) tag;
                            final Intent intent = info.intent;
                            final ComponentName name = intent.getComponent();

                            if (Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                                for (String packageName : packageNames) {
                                    if (packageName.equals(name.getPackageName())) {
                                        // TODO: This should probably be done on
                                        // a worker thread
                                        LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                        childrenToRemove.add(view);
                                    }
                                }
                            }
                        } else if (tag instanceof UserFolderInfo) {
                            final UserFolderInfo info = (UserFolderInfo) tag;
                            final ArrayList<ShortcutInfo> contents = info.contents;
                            final ArrayList<ShortcutInfo> toRemove = new ArrayList<ShortcutInfo>(1);
                            final int contentsCount = contents.size();
                            boolean removedFromFolder = false;

                            for (int k = 0; k < contentsCount; k++) {
                                final ShortcutInfo appInfo = contents.get(k);
                                final Intent intent = appInfo.intent;
                                final ComponentName name = intent.getComponent();

                                if (Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                                    for (String packageName : packageNames) {
                                        if (packageName.equals(name.getPackageName())) {
                                            toRemove.add(appInfo);
                                            // TODO: This should probably be
                                            // done on a worker thread
                                            LauncherModel.deleteItemFromDatabase(mLauncher, appInfo);
                                            removedFromFolder = true;
                                        }
                                    }
                                }
                            }

                            contents.removeAll(toRemove);
                            if (removedFromFolder) {
                                final Folder folder = getOpenFolder();
                                if (folder != null)
                                    folder.notifyDataSetChanged();
                            }
                        } else if (tag instanceof LiveFolderInfo) {
                            final LiveFolderInfo info = (LiveFolderInfo) tag;
                            final Uri uri = info.uri;
                            final ProviderInfo providerInfo = manager.resolveContentProvider(uri
                                    .getAuthority(), 0);

                            if (providerInfo != null) {
                                for (String packageName : packageNames) {
                                    if (packageName.equals(providerInfo.packageName)) {
                                        // TODO: This should probably be done on
                                        // a worker thread
                                        LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                        childrenToRemove.add(view);
                                    }
                                }
                            }
                        } else if (tag instanceof LauncherAppWidgetInfo) {
                            final LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) tag;
                            final AppWidgetProviderInfo provider = widgets
                                    .getAppWidgetInfo(info.appWidgetId);
                            if (provider != null) {
                                for (String packageName : packageNames) {
                                    if (packageName.equals(provider.provider.getPackageName())) {
                                        // TODO: This should probably be done on
                                        // a worker thread
                                        LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                        childrenToRemove.add(view);
                                    }
                                }
                            }
                        }
                    }

                    childCount = childrenToRemove.size();
                    for (int j = 0; j < childCount; j++) {
                        View child = childrenToRemove.get(j);
                        layout.removeViewInLayout(child);
                        if (child instanceof DropTarget) {
                            mDragController.removeDropTarget((DropTarget) child);
                        }
                    }

                    if (childCount > 0) {
                        layout.requestLayout();
                        layout.invalidate();
                    }
                }
            });
        }
    }

    void updateShortcuts(ArrayList<ApplicationInfo> apps) {
        final PackageManager pm = mLauncher.getPackageManager();

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            int childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                final View view = layout.getChildAt(j);
                Object tag = view.getTag();
                if (tag instanceof ShortcutInfo) {
                    ShortcutInfo info = (ShortcutInfo) tag;
                    // We need to check for ACTION_MAIN otherwise getComponent()
                    // might
                    // return null for some shortcuts (for instance, for
                    // shortcuts to
                    // web pages.)
                    final Intent intent = info.intent;
                    final ComponentName name = intent.getComponent();
                    if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
                            && Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                        final int appCount = apps.size();
                        for (int k = 0; k < appCount; k++) {
                            ApplicationInfo app = apps.get(k);
                            if (app.componentName.equals(name)) {
                                info.setIcon(mIconCache.getIcon(info.intent));
                                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(null,
                                        new FastBitmapDrawable(info.getIcon(mIconCache)), null,
                                        null);
                            }
                        }
                    }
                }
            }
        }
    }

	public View searchIMTKWidget(View hostView ) {
		if (getId() == R.id.shortcut_workspace) {
			// shortcut workspace doesn't contain widget.
			return null;
		} else if (hostView instanceof IMTKWidget) {
			return hostView;
		} else if (hostView instanceof ViewGroup) {
			int childCount = ((ViewGroup) hostView).getChildCount();
			for (int i = 0; i < childCount; i++) {
				View mtkWidgetView = searchIMTKWidget(((ViewGroup) hostView).getChildAt(i));
				if (mtkWidgetView != null)
					return mtkWidgetView;
			}
		}
		return null;
    }

    void moveToDefaultScreen(boolean animate) {
        if (animate) {
            snapToScreen(mDefaultScreen);
        } else {
            setCurrentScreen(mDefaultScreen);
		}
		if (Launcher.isShortcutScreen()) {
			mLauncher.mPageClassification
					.setText(Launcher.pageClassification[mDefaultScreen]);
		}
		getChildAt(mDefaultScreen).requestFocus();
    }

    public static class SavedState extends BaseSavedState {
        int currentScreen = -1;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentScreen = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentScreen);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public void setDragging(boolean dragging) {
        if (dragging) {
            notifyCurrentScreenLeave();
        } 

        mDragging = dragging;
    }

    /*
     * @Param useWsCanvas, this function who invoked 
     */
    private void drawGrayAreaForItem(Canvas canvas, CellLayout cellLayout, boolean useWsCanvas) {
        final Folder openFolder = getOpenFolder(cellLayout);
        if (openFolder != null) {
            if (openFolder instanceof DropTarget && 
                    ((DropTarget) openFolder).acceptDrop(null, 0, 0, 0, 0, null, mDragController.getDragInfo())) {
                if (!useWsCanvas) {
                    cellLayout.dispatchDraw(canvas);
                }
                return;
            } else {
                mLauncher.closeFolder(openFolder);
            }
        }

        if (!useWsCanvas) {
            cellLayout.dispatchDraw(canvas);
        }

        Rect rect = mTempRect;
        final int count = cellLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View tmpView = (View) cellLayout.getChildAt(i);
            if (tmpView.getVisibility() == View.VISIBLE) {
                if ((tmpView instanceof DropTarget)
                        && ((DropTarget) tmpView).acceptDrop(null, 0, 0, 0, 0, null,
                                mDragController.getDragInfo())) {
                    // Nothing to do
                } else {
                    rect.set(0, 0, tmpView.getWidth(), tmpView.getHeight());
                    rect.offset(tmpView.getLeft(), tmpView.getTop());
                    if (useWsCanvas) { 
                        rect.offset(cellLayout.getLeft(), 0);
                    }

                    canvas.drawRect(rect, mGrayAreaPaint);
                }
            }
        }
    }

    public void resetTargetCellForShowGreen() {
        mTargetCellForShowGreen = null;
    }
 
    public void getEstimateDropCellForShowGreen(int x, int y, int xOffset, int yOffset, Object dragInfo) {
    	if(Launcher.getCurrentScene().equals(Launcher.shortcutTheme)){
    		reorderCellLayout(x, y, xOffset, yOffset, dragInfo);
    	}
        // If item which is in grea area, green outline will not show
        CellLayout cellLayout = (CellLayout) getChildAt(mCurrentScreen);
        mTargetCellForShowGreen = estimateDropLocation(null, x, y, xOffset, yOffset, null,
                dragInfo, null);
        cellLayout.setCellForGreenRect(mTargetCellForShowGreen);
    }
    
    public void reorderCellLayout(int x, int y, int xOffset, int yOffset, Object dragInfo){
    	if(!Launcher.isDefStyle()){
	    	final boolean xMove = (int) Math.abs(x - mDropCellX)>mTouchSlop;
	        final boolean yMove = (int) Math.abs(y - mDropCellY)>mTouchSlop;
	    	if(xMove || yMove){
	    		mDragMoveingTime = System.currentTimeMillis();
	    		mDropCellX = x;
	        	mDropCellY = y;
	    	} else {
	    		if(System.currentTimeMillis() - mDragMoveingTime < DropReorderTime){
	    			mDropCellX = x;
	    			mDropCellY = y;
	    		} else {
	    			reorderCellLayout(x, y, dragInfo);
	    		}
	    	}
    	}
    }
    
    /**
     * 
     * @param x,y	dragInfo screen position
     * @param dragInfo	the child is being dragged.
     */
	private void reorderCellLayout(int x, int y, Object dragInfo) {
		int[] cellXY = new int[2];
		final CellLayout cellLayout = (CellLayout) getChildAt(mCurrentScreen);
		cellLayout.pointToCellExact(x, y, cellXY);
		final int countX = cellLayout.getCountX();
		final int countY = cellLayout.getCountY();
		
		// Make sure the view on the position is not the app manager or more app.
		if (cellXY[1] * countX + cellXY[0] != countX * countY - 1) {
			View targetView = getChildAtPosition(mCurrentScreen,cellXY);
			// If the target position don't have a view, do nothing. Else move the view.
			if (targetView != null) {	
				ItemInfo info = (ItemInfo)dragInfo;
				final View[] childs = getOrderChildsFromCellLayout(cellLayout, info);
				
				int pos = cellXY[1] * countX + cellXY[0];
				if (childs[pos] != null) {
					int upCount = getMoveUpCount(childs, pos-1);
					int downCount = getMoveDownCount(childs, pos +1);

					if (upCount + downCount != childs.length-1) { // full return create a floder
						// Clear FolderIcon open state to close. 
						if(targetView instanceof DropTarget){
							((DropTarget) targetView).onDragExit(this, cellXY[0], cellXY[1], 1, 1, null, dragInfo);
						}
						moveViews(cellLayout, childs, upCount, downCount, pos);
					}
				}
			}
		}
	}
	
	private View[] getOrderChildsFromCellLayout(CellLayout cellLayout, ItemInfo info){
		View[] childs = new View[cellLayout.getCountX() * cellLayout.getCountY() - 1];
		for (int i = 0; i < childs.length; i++) {
			childs[i] = null;
		}
		int count = cellLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = (View) cellLayout.getChildAt(i);
//			if (child.getTag() instanceof ShortcutInfo) {
//				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
//				int pos = lp.cellY * cellLayout.getCountX() + lp.cellX;
//				if (child.getTag() != info && pos < childs.length) {
//					childs[pos] = child;
//				}
//			} else if (child.getTag() instanceof UserFolderInfo) {
				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
				int pos = lp.cellY * cellLayout.getCountX() + lp.cellX;
				if (child.getTag() != info && pos < childs.length) {
					childs[pos] = child;
				}
//			}
		}
		return childs;
	}
    
    private int getMoveUpCount(View[] childs, int start){
    	int size = 0;
		for (int i = start; i >= 0; i--) {
			if (childs[i] == null) {
				break;
			}
			size++;
		}
		return size;
    }
    
    private int getMoveDownCount(View[] childs, int start){
    	int size = 0;
		for (int i = start; i < childs.length; i++) {
			if (childs[i] == null) {
				break;
			}
			size++;
		}
		return size;
    }
    
	private void moveViews(CellLayout cellLayout, View[] childs, int up, int down, int pos) {
		if (up + down != childs.length - 1) { // full return create a floder
			if (down == childs.length - 1 - pos || (up != pos && up <= down)) {// up
				for (int i = pos; i >= 0; i--) {
					if (childs[i] != null) {
						moveView(childs[i], CELL_MOVE_UP);
					} else {
						break;
					}
				}
//			} else if (up == pos || (up > down && down != childs.length - 1 - pos)) {// down
			} else {
				for (int i = pos; i < childs.length; i++) {
					if (childs[i] != null) {
						moveView(childs[i], CELL_MOVE_DOWN);
					} else {
						break;
					}
				}
				// cellLayout.invalidate();
			}
			cellLayout.invalidate();
		}
	}
    
	private void moveView(View child, int dir) {
		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
		int pos = lp.cellY * 4 + lp.cellX + dir;
		lp.cellX = pos % 4;
		lp.cellY = pos / 4;
		child.requestLayout();
		LauncherModel.moveItemInDatabase(mLauncher, (ItemInfo)child.getTag(),
				LauncherSettings.Favorites.CONTAINER_DESKTOP, mCurrentScreen, lp.cellX, lp.cellY);
	}

    public void setDragInfo(CellLayout.CellInfo cellInfo) {
        mDragInfo = cellInfo;
    }

    public boolean isFinished() {
        return mScroller.isFinished();
    }
    
    public void hideWorkspace() {
    	setVisibility(View.GONE);
    	int count = getChildCount();
    	for( int i = 0; i < count; i++) {
			final CellLayout cellLayout = (CellLayout) getChildAt(i);
			cellLayout.setVisibility(View.GONE);
			int childCount = cellLayout.getChildCount();

			for (int j = 0; j < childCount; j++) {
				View child = (View) cellLayout.getChildAt(j);
				child.setVisibility(View.GONE);
			}
		}
    }
    
    public void showWorkspace( ) {
    	setVisibility(View.VISIBLE);
    	int count = getChildCount();
    	for( int i = 0; i < count; i++) {
			final CellLayout cellLayout = (CellLayout) getChildAt(i);
			cellLayout.setVisibility(View.VISIBLE);
			int childCount = cellLayout.getChildCount();

			for (int j = 0; j < childCount; j++) {
				View child = (View) cellLayout.getChildAt(j);
				child.setVisibility(View.VISIBLE);
			}
		}
	updateWallpaperOffset();  			
    }
    
    public void removeViews() {
    	int count = getChildCount();
    	for (int i = 0; i < count; i++) {
            final CellLayout cellLayout = (CellLayout) getChildAt(i);
            cellLayout.removeAllViews();
        }
    }
    
    public int getX() {
    	return (int)mLastMotionX;
    }
    
    public int getY() {
    	return (int)mLastMotionY;
    }
    
    public FolderIcon getFolder(Object tag) {
    	CellLayout cellLayout = (CellLayout)getChildAt(mCurrentScreen);
    	int count = cellLayout.getChildCount();
    	for (int i=0; i<count; i++) {
    		View child = cellLayout.getChildAt(i);
    		if (child instanceof DropTarget) {
    			if (child.getTag() == tag) {
    				return (FolderIcon)child;
    			}	
    		}
    	}
    	return null;
    }
}
