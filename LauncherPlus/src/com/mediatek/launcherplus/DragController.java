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
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.IBinder;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.VelocityTracker;
import android.view.View;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Class for initiating a drag within a view or across multiple views.
 */
public class DragController {
    @SuppressWarnings( {
        "UnusedDeclaration"
    })
    private static final String TAG = "Launcher.DragController";

    /** Indicates the drag is a move. */
    public static int DRAG_ACTION_MOVE = 0;

    /** Indicates the drag is a copy. */
    public static int DRAG_ACTION_COPY = 1;

    /** Indicates the drag is a ADD. */
    public static int DRAG_ACTION_ADD = 2;

    /** Indicates the drag is a ADD. */
    public static int DRAG_ACTION_NONE = -1;

    /** Indicates the drag MODE. */
    private int mDragActionMode = DRAG_ACTION_NONE;

    private static final int SCROLL_DELAY = 600;

    private static final int SCROLL_ZONE = 20;

    private static final int VIBRATE_DURATION = 35;

    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;

    private static final int SCROLL_OUTSIDE_ZONE = 0;

    private static final int SCROLL_WAITING_IN_ZONE = 1;

    private static final int SCROLL_LEFT = 0;

    private static final int SCROLL_RIGHT = 1;

    private Context mContext;

    private Handler mHandler;

    private final Vibrator mVibrator = new Vibrator();

    // temporaries to avoid gc thrash
    private Rect mRectTemp = new Rect();

    private final int[] mCoordinatesTemp = new int[2];

    /** Whether or not we're dragging. */
    private boolean mDragging;
    
    /** Whether or not show the alert dialog while dragging. */
    private boolean mDialogShown;

    /** X coordinate of the down event. */
    private float mMotionDownX;

    /** Y coordinate of the down event. */
    private float mMotionDownY;

    /** Info about the screen for clamping. */
    private DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    /** Original view that is being dragged. */
    private View mOriginator;

    /** X offset from the upper-left corner of the cell to where we touched. */
    private float mTouchOffsetX;

    /** Y offset from the upper-left corner of the cell to where we touched. */
    private float mTouchOffsetY;

    /** Where the drag originated */
    private DragSource mDragSource;

    /** The data associated with the object being dragged */
    private Object mDragInfo;

    /** The view that moves around while you drag. */
    private DragView mDragView;

    /** Who can receive drop events */
    private ArrayList<DropTarget> mDropTargets = new ArrayList<DropTarget>();

    private DragListener mListener;

    /** The window token used as the parent for the DragView. */
    private IBinder mWindowToken;

    private Launcher mLauncher;

    private final int mStatusBarHeight;

    private final int[] mDragViewCoordinates = new int[2];

    private Rect mDragViewRect = new Rect();

    private static final int PADDING_FOR_BORDER = 12;

    private VelocityTracker mVelocityTracker;

    private static final int SNAP_VELOCITY = 150;

    private int mMaximumVelocity;

    /**
     * The view that will be scrolled when dragging to the left and right edges
     * of the screen.
     */
    private View mScrollView;

    private View mMoveTarget;

    private DragScroller mDragScroller;

    private int mScrollState = SCROLL_OUTSIDE_ZONE;

    private ScrollRunnable mScrollRunnable = new ScrollRunnable();

    private RectF mDeleteRegion;

    private DropTarget mLastDropTarget;

    private InputMethodManager mInputMethodManager;


    /**
     * Interface to receive notifications when a drag starts or stops
     */
    interface DragListener {

        /**
         * A drag has begun
         * 
         * @param source An object representing where the drag originated
         * @param info The data associated with the object that is being dragged
         * @param dragAction The drag action: either
         *            {@link DragController#DRAG_ACTION_MOVE} or
         *            {@link DragController#DRAG_ACTION_COPY}
         */
        void onDragStart(DragSource source, Object info, int dragAction);

        /**
         * The drag has eneded
         */
        void onDragEnd();
    }

    /**
     * Used to create a new DragLayer from XML.
     * 
     * @param context The application's context.
     */
    public DragController(Context context) {
        mContext = context;
        mHandler = new Handler();

        recordScreenSize();

        mLauncher = (Launcher) mContext;

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mStatusBarHeight = (int) (25 * mDisplayMetrics.density);

    }

    /**
     * Starts a drag.
     * 
     * @param v The view that is being dragged
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *            {@link #DRAG_ACTION_COPY}
     */
    public void startDrag(View v, DragSource source, Object dragInfo, int dragAction) {
        mDragActionMode = dragAction;
        mOriginator = v;

        Bitmap b = getViewBitmap(v);

        if (b == null) {
            // out of memory?
            return;
        }

        int[] loc = mCoordinatesTemp;
        v.getLocationOnScreen(loc);
        int screenX = loc[0];
        int screenY = loc[1];

        startDrag(b, screenX, screenY, 0, 0, b.getWidth(), b.getHeight(), source, dragInfo,
                dragAction);

        b.recycle();

        if (dragAction == DRAG_ACTION_MOVE) {
            v.setVisibility(View.GONE);
        }
    }

    /**
     * Starts a drag.
     * 
     * @param b The bitmap to display as the drag image. It will be re-scaled to
     *            the enlarged size.
     * @param screenX The x position on screen of the left-top of the bitmap.
     * @param screenY The y position on screen of the left-top of the bitmap.
     * @param textureLeft The left edge of the region inside b to use.
     * @param textureTop The top edge of the region inside b to use.
     * @param textureWidth The width of the region inside b to use.
     * @param textureHeight The height of the region inside b to use.
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *            {@link #DRAG_ACTION_COPY}
     */
    public void startDrag(Bitmap b, int screenX, int screenY, int textureLeft, int textureTop,
            int textureWidth, int textureHeight, DragSource source, Object dragInfo, int dragAction) {
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        // Hide soft keyboard, if visible
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager) mContext
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(mWindowToken, 0);

        if (mListener != null) {
            mListener.onDragStart(source, dragInfo, dragAction);
        }

        int registrationX = ((int) mMotionDownX) - screenX;
        int registrationY = ((int) mMotionDownY) - screenY;

        mTouchOffsetX = mMotionDownX - screenX;
        mTouchOffsetY = mMotionDownY - screenY;

        mDragging = true;
        willDrawChild(mDragging);
        mDragSource = source;
        mDragInfo = dragInfo;

        mVibrator.vibrate(VIBRATE_DURATION);
        
        Workspace workSpace = mLauncher.getWorkspace();
        int cellHeight = ((CellLayout) workSpace.getChildAt(workSpace.getCurrentScreen()))
                .getCellHeight();

        DragView dragView = mDragView = new DragView(mContext, b, registrationX, registrationY,
                textureLeft, textureTop, textureWidth, textureHeight);
        dragView.setDragController(this);
        dragView.setCellHeight(cellHeight);
        dragView.setStatusBarHeight(mStatusBarHeight);
        dragView.show(mWindowToken, (int) mMotionDownX, (int) mMotionDownY);

        if (source instanceof AllAppsView) {
            if (!mLauncher.getWorkspace().acceptDrop(source, 0, 0, 0, 0, mDragView, dragInfo)) {
                Toast.makeText(mLauncher, mLauncher.getString(R.string.out_of_space),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // draw Green Outline and Gray Area
        final int[] coordinates = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget(screenX, screenY, coordinates);
        
        workSpace.setDragging(mDragging);
        if (dropTarget != null) {
            dropTarget.getEstimateDropCellForShowGreen((int) mMotionDownX, (int) mMotionDownY - mStatusBarHeight,
                    (int) mTouchOffsetX, (int) mTouchOffsetY, dragInfo);
        }
    }

    private void willDrawChild(boolean dragging) {
        Workspace workspace = mLauncher.getWorkspace();
        int count = workspace.getChildCount();
        for (int i = 0; i < count; i++) {
            ((CellLayout) workspace.getChildAt(i)).setWillNotDraw(!dragging); 
            ((CellLayout) workspace.getChildAt(i)).setDragging(dragging); 
        }
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
        if (cacheBitmap == null) {
            Log.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    /**
     * Call this from a drag source view like this:
     * 
     * <pre>
     *  @Override
     *  public boolean dispatchKeyEvent(KeyEvent event) {
     *      return mDragController.dispatchKeyEvent(this, event)
     *              || super.dispatchKeyEvent(event);
     * </pre>
     */
    @SuppressWarnings( {
        "UnusedDeclaration"
    })
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging;
    }

    /**
     * Stop dragging without dropping.
     */
    public void cancelDrag() {
        if (mDragActionMode == DRAG_ACTION_ADD) {
            Workspace workspace = mLauncher.getWorkspace();
            workspace.setDragInfo(null);
            final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) mDragInfo;
            final CellLayout cellLayout = (CellLayout) workspace.getChildAt(launcherAppWidgetInfo.screen);
            cellLayout.removeView(mOriginator);
            mOriginator = null;
            mLauncher.removeAppWidget(launcherAppWidgetInfo);
            final LauncherAppWidgetHost appWidgetHost = mLauncher.getAppWidgetHost();
            if (appWidgetHost != null) {
                appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
            }
        }

        endDrag();
    }

    public void endDrag() {
        Workspace workSpace = mLauncher.getWorkspace();
        workSpace.setDragging(false);
        workSpace.resetTargetCellForShowGreen();
        workSpace.invalidate();
        mDragActionMode = DRAG_ACTION_NONE;
        mDialogShown = false;
        
        if (mDragging) {
            mDragging = false;
            willDrawChild(mDragging);
            if (mOriginator != null) {
                mOriginator.setVisibility(View.VISIBLE);
            }
            if (mListener != null) {
                mListener.onDragEnd();
            }
            if (mDragView != null) {
                mDragView.remove();
                mDragView = null;
            }
            if (!mLauncher.getPaused()) {
                workSpace.notifyCurrentScreenEnter();
            }
        }
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (false) {
            Log.d(Launcher.TAG, "DragController.onInterceptTouchEvent " + ev + " mDragging="
                    + mDragging);
        }
        final int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            recordScreenSize();
        }

        final int screenX = clamp((int) ev.getRawX(), 0, mDisplayMetrics.widthPixels);
        final int screenY = clamp((int) ev.getRawY(), 0, mDisplayMetrics.heightPixels);

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mMotionDownX = screenX;
                mMotionDownY = screenY;
                mLastDropTarget = null;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            	if (!mDialogShown) {
					if (mDragging && drop(screenX, screenY)) {
						endDrag();
						return true;
					}
            	}
                break;
        }

        return mDragging;
    }

    /**
     * Sets the view that should handle move events.
     */
    void setMoveTarget(View view) {
        mMoveTarget = view;
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return mMoveTarget != null && mMoveTarget.dispatchUnhandledMove(focused, direction);
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onTouchEvent(MotionEvent ev) {
        View scrollView = mScrollView;

        if (!mDragging) {
            return false;
        }

        final int action = ev.getAction();
        final int screenX = clamp((int) ev.getRawX(), 0, mDisplayMetrics.widthPixels);
        final int screenY = clamp((int) ev.getRawY(), 0, mDisplayMetrics.heightPixels);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mMotionDownX = screenX;
                mMotionDownY = screenY;

                if ((screenX < SCROLL_ZONE) || (screenX > scrollView.getWidth() - SCROLL_ZONE)) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
                } else {
                    mScrollState = SCROLL_OUTSIDE_ZONE;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                // Update the drag view. Don't use the clamped pos here so the
                // dragging looks
                // like it goes off screen a little, intead of bumping up
                // against the edge.
                mDragView.move((int) ev.getRawX(), (int) ev.getRawY());

                // Drop on someone?
                final int[] coordinates = mCoordinatesTemp;
                DropTarget dropTarget = findDropTarget(screenX, screenY, coordinates);
                if (dropTarget != null) {
                    if (mLastDropTarget == dropTarget) {
                        dropTarget.onDragOver(mDragSource, coordinates[0], coordinates[1],
                                (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                    } else {
                        if (mLastDropTarget != null) {
                            mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                                    (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                        }
                        dropTarget.onDragEnter(mDragSource, coordinates[0], coordinates[1],
                                (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                    }
                } else {
                    if (mLastDropTarget != null) {
                        mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                                (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                    }
                }
                mLastDropTarget = dropTarget;
                if (mLastDropTarget != null) {
                    mLastDropTarget.getEstimateDropCellForShowGreen(screenX, screenY
                            - mStatusBarHeight, (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                }

                // Scroll, maybe, but not if we're in the delete region.
                boolean inDeleteRegion = false;
                if (mDeleteRegion != null) {
                    inDeleteRegion = mDeleteRegion.contains(screenX, screenY);
                }
                
                if (!inDeleteRegion && screenX < SCROLL_ZONE) {
                    if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                        mScrollState = SCROLL_WAITING_IN_ZONE;
                        mScrollRunnable.setDirection(SCROLL_LEFT);
                        mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
                    }
                } else if (!inDeleteRegion && screenX > scrollView.getWidth() - SCROLL_ZONE) {
                    if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                        mScrollState = SCROLL_WAITING_IN_ZONE;
                        mScrollRunnable.setDirection(SCROLL_RIGHT);
                        mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
                    }
                } else {
                    if (mScrollState == SCROLL_WAITING_IN_ZONE) {
                        mScrollState = SCROLL_OUTSIDE_ZONE;
                        mScrollRunnable.setDirection(SCROLL_RIGHT);
                        mHandler.removeCallbacks(mScrollRunnable);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                mHandler.removeCallbacks(mScrollRunnable);
				if (!mDialogShown) {
					if (mDragging && drop(screenX, screenY)) {
						endDrag();
					}
				}
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelDrag();
        }

        return true;
    }

    public boolean isSystemApp(ShortcutInfo info) {
    	final ResolveInfo resolveInfo = mLauncher.getPackageManager().resolveActivity(info.intent, 0);
    	if ((resolveInfo.activityInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
            return true;
        }

        return false;
    }
    
    private boolean drop(float x, float y) {
        final int[] cellXY = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget((int) x, (int) y, cellXY);
        
        boolean dropFlag = false;
        if (dropTarget != null) {
        	dropTarget.onDragExit(mDragSource, cellXY[0], cellXY[1], (int) mTouchOffsetX,
                    (int) mTouchOffsetY, mDragView, mDragInfo);
            if (dropTarget.acceptDrop(mDragSource, cellXY[0], cellXY[1],
                    (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo)) {
            	
            	dropFlag = dropTarget.onDrop(mDragSource, cellXY[0], cellXY[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
            	if ( mLauncher.isDefStyle()) {
            		if (!dropFlag)
						return false;
					mDragSource.onDropCompleted((View) dropTarget, true);
            	} else {
					if (dropFlag && dropTarget instanceof DeleteZone) {
						// remove the icon form homescreen
						if (mDragInfo instanceof ShortcutInfo) {
							// ShortcutInfo is System APP
							mDragSource.onDropCompleted((View) dropTarget,false);
						} else if (mDragInfo instanceof UserFolderInfo) {
							if (!((UserFolderInfo) mDragInfo).contents.isEmpty()) {
								mDragSource.onDropCompleted((View) dropTarget,false);
							} else {
								mDragSource.onDropCompleted((View) dropTarget,true);
							}
						} else {
							mDragSource.onDropCompleted((View) dropTarget, true);
						}
					} else if (!dropFlag && dropTarget instanceof DeleteZone) {
						if (mDragInfo instanceof ShortcutInfo) {
							// ShortcutInfo is not System APP, shown alert
							// dialog to delete the APP.
							mDialogShown = true;
							mDragSource.onDropCompleted((View) dropTarget,false);
							return true;
						} else if (mDragInfo instanceof UserFolderInfo) {
							// Folder is not empty and not all APPs in the
							// folder are SystemApp.
							if (!((UserFolderInfo) mDragInfo).contents.isEmpty()) {
								mDialogShown = true;
								mDragSource.onDropCompleted((View) dropTarget,false);
								return true;
							}
						}
					} else if (dropFlag) {
						mDragSource.onDropCompleted((View) dropTarget, true);
					}
            	}
            } else {
                // workaround for FolderIcon
                // if(!(dropTarget instanceof Workspace) ){
                if (dropTarget == mOriginator) {
                    mDragSource.onDropCompleted((View) dropTarget, false);
                    dropFlag = true;
                }
            }
        }
        return dropFlag;
    }
    
    public FolderIcon findDropTarget(String folderName, int screen) { 
    	final ArrayList<DropTarget> dropTargets = mDropTargets;
        final int count = dropTargets.size();
        for (int i = count - 1; i >= 0; i--) {
            DropTarget target = dropTargets.get(i);
            if ( target instanceof FolderIcon ) {
            	FolderIcon folderIcon = (FolderIcon)target;
            	String tmpName = folderIcon.getInfo().title.toString();
            	if ( tmpName.equalsIgnoreCase(folderName) && folderIcon.getInfo().screen == screen ) 
            		return folderIcon;
            }
        }
        return null;
    }
    
    private DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        final Rect r = mRectTemp;
        
        final ArrayList<DropTarget> dropTargets = mDropTargets;
        final int count = dropTargets.size();
        for (int i = count - 1; i >= 0; i--) {
            final DropTarget target = dropTargets.get(i);
            target.getHitRect(r);
            target.getLocationOnScreen(dropCoordinates);
            r.offset(dropCoordinates[0] - target.getLeft(), dropCoordinates[1] - target.getTop());
            if (target instanceof DeleteZone && mDragView != null) {
                mDragView.getLocationOnScreen(mDragViewCoordinates);
                mDragViewRect.set(mDragViewCoordinates[0], mDragViewCoordinates[1],
                        mDragViewCoordinates[0] + mDragView.getWidth(), mDragViewCoordinates[1]
                                + mDragView.getHeight());
                // Considering transparent border, we decrease the rect
                int padding = (int) (mDisplayMetrics.density * PADDING_FOR_BORDER);
                mDragViewRect.inset(padding, 0);
                if (Rect.intersects(mDragViewRect, r)) {
                    if (mLauncher.getPortrait()) {
                        if (mDragViewRect.bottom - r.top > mDragViewRect.height() * 0.4f) {
                            return target;
                        }
                    } else {
                        if (mDragViewRect.right - r.left > mDragViewRect.width() * 0.4f) {
                            return target;
                        }
                    }
                }
            }

            if (r.contains(x, y) && (target != mOriginator)) {
                dropCoordinates[0] = x - dropCoordinates[0];
                dropCoordinates[1] = y - dropCoordinates[1];
                return target;
            }
        }
        return null;
    }
    
    public DropTarget getDropTarget(int x, int y){
    	return findDropTarget(x, y, mCoordinatesTemp);
    }

    /**
     * Get the screen size so we can clamp events to the screen size so even if
     * you drag off the edge of the screen, we find something.
     */
    private void recordScreenSize() {
        ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(mDisplayMetrics);
    }
    
    public DisplayMetrics getDisplayMetrics() {
        return mDisplayMetrics;
    }

    /**
     * Clamp val to be &gt;= min and &lt; max.
     */
    private static int clamp(int val, int min, int max) {
        if (val < min) {
            return min;
        } else if (val >= max) {
            return max - 1;
        } else {
            return val;
        }
    }

    public void setDragScoller(DragScroller scroller) {
        mDragScroller = scroller;
    }

    public void setWindowToken(IBinder token) {
        mWindowToken = token;
    }

    /**
     * Sets the drag listner which will be notified when a drag starts or ends.
     */
    public void setDragListener(DragListener l) {
        mListener = l;
    }

    /**
     * Remove a previously installed drag listener.
     */
    public void removeDragListener(DragListener l) {
        mListener = null;
    }

    /**
     * Add a DropTarget to the list of potential places to receive drop events.
     */
    public void addDropTarget(DropTarget target) {
        mDropTargets.add(target);
    }

    /**
     * Don't send drop events to <em>target</em> any more.
     */
    public void removeDropTarget(DropTarget target) {
        mDropTargets.remove(target);
    }
    
    public void removeAllDropTargets() {
    	mDropTargets.clear();
    }
    
    public void updateDropTargets(DropTarget target) {
    	mDropTargets.set(0, target); //only update Workspace
    }

    /**
     * Set which view scrolls for touch events near the edge of the screen.
     */
    public void setScrollView(View v) {
        mScrollView = v;
    }

    /**
     * Specifies the delete region. We won't scroll on touch events over the
     * delete region.
     * 
     * @param region The rectangle in screen coordinates of the delete region.
     */
    void setDeleteRegion(RectF region) {
        mDeleteRegion = region;
    }

    private class ScrollRunnable implements Runnable {
        private int mDirection;

        ScrollRunnable() {
        }

        public void run() {
            if (mDragScroller != null) {
                if (mDirection == SCROLL_LEFT) {
                    mDragScroller.scrollLeft();
                } else {
                    mDragScroller.scrollRight();
                }
                mScrollState = SCROLL_OUTSIDE_ZONE;
            }
        }

        void setDirection(int direction) {
            mDirection = direction;
        }
    }

    boolean isDraging() {
        return mDragging;
    }

    public Object getDragInfo() {
        return mDragInfo;
    }

    public void handleTouchEvent(MotionEvent ev) {
        if (mDragView != null) {
            final int action = ev.getAction();
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);
            switch (action) {
                case MotionEvent.ACTION_UP:
                    Workspace workspace = mLauncher.getWorkspace();
                    if (workspace.isFinished()) {
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        int velocityX = (int) velocityTracker.getXVelocity();
                        int velocityY = (int) velocityTracker.getYVelocity();

                        int currentScreen = workspace.getCurrentScreen();
                        if (velocityX > SNAP_VELOCITY && currentScreen > 0
                                && Math.abs(velocityX) > Math.abs(velocityY)) {
                            // Fling hard enough to move left
                            workspace.snapToScreen(currentScreen - 1);
                        } else if (velocityX < -SNAP_VELOCITY
                                && currentScreen < workspace.getChildCount() - 1
                                && Math.abs(velocityX) > Math.abs(velocityY)) {
                            // Fling hard enough to move right
                            workspace.snapToScreen(currentScreen + 1);
                        }

                        if (mVelocityTracker != null) {
                            mVelocityTracker.recycle();
                            mVelocityTracker = null;
                        }
                    }
            }
        }
    }

    public void setTouchOffset(float xOffset, float yOffset) {
        mTouchOffsetX = xOffset;
        mTouchOffsetY = yOffset;
    }

    public void startDrag(View view, Object dragInfo, int spanX, int spanY, int dragAction) {
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        // Hide soft keyboard, if visible
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager) mContext
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(mWindowToken, 0);

        mDragActionMode = dragAction;
        mOriginator = view;
        Workspace workspace = mLauncher.getWorkspace();
        // Maybe we need to define the DragSource
        if (mListener != null) {
            mListener.onDragStart(workspace, dragInfo, dragAction);
        }

        int cellWidth = ((CellLayout) workspace.getChildAt(workspace.getCurrentScreen()))
                .getCellWidth();
        int cellHeight = ((CellLayout) workspace.getChildAt(workspace.getCurrentScreen()))
                .getCellHeight();
        mTouchOffsetX = cellWidth * spanX / 2;
        mTouchOffsetY = cellHeight * spanY / 2;
        // No Motion Event, we fabricate it
        mMotionDownX = 160;
        mMotionDownY = 240;

        mDragging = true;
        willDrawChild(mDragging);
        mDragSource = workspace;
        mDragInfo = dragInfo;

        mVibrator.vibrate(VIBRATE_DURATION);

        // Set DragInfo for workspace
        CellLayout.CellInfo cellInfo = new CellLayout.CellInfo();
        cellInfo.cell = view;
        cellInfo.spanX = spanX;
        cellInfo.spanY = spanY;
        cellInfo.screen = ((LauncherAppWidgetInfo) dragInfo).screen;
        workspace.setDragInfo(cellInfo);
        workspace.setDragging(mDragging);

        // DragView
        DragView dragView = mDragView = new DragView(mContext, view);
        dragView.setDragController(this);
        dragView.setCellHeight(cellHeight);
        dragView.setStatusBarHeight(mStatusBarHeight);
        dragView.show(mWindowToken, workspace, spanX, spanY, cellWidth, cellHeight);
    }

    public int getDragActionMode() {
        return mDragActionMode;
    }
}
