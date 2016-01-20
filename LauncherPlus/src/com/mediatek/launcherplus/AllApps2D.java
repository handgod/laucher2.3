/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
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

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import com.mediatek.featureoption.FeatureOption;
import com.mediatek.launcherplus.R;

public class AllApps2D
        extends RelativeLayout
        implements AllAppsView,
                   AdapterView.OnItemClickListener,
                   AdapterView.OnItemLongClickListener,
                   View.OnKeyListener,
                   DragSource {

    private static final String TAG = "LauncherPlus.AllApps2D";
    private static final boolean DEBUG = false;

    private Launcher mLauncher;
    private DragController mDragController;

    private GridView mGrid;

    private ArrayList<ApplicationInfo> mAllAppsList = new ArrayList<ApplicationInfo>();

    // preserve compatibility with 3D all apps:
    //    0.0 -> hidden
    //    1.0 -> shown and opaque
    //    intermediate values -> partially shown & partially opaque
    private float mZoom;

    private AppsAdapter mAppsAdapter;

    private static final int DRAW_NORMAL = 0;

    private static final int DRAW_OPEN = 1;

    private static final int DRAW_CLOSE = 2;

    private int mDrawMode = DRAW_NORMAL;

    private long mStartTime;

    private int mDuration = 200;
    // ------------------------------------------------------------
    
    public static class HomeButton extends ImageButton {
        public HomeButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        @Override
        public View focusSearch(int direction) {
            if (direction == FOCUS_UP) return super.focusSearch(direction);
            return null;
        }
    }

    public class AppsAdapter extends ArrayAdapter<ApplicationInfo> {
        private final LayoutInflater mInflater;

        public AppsAdapter(Context context, ArrayList<ApplicationInfo> apps) {
            super(context, 0, apps);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ApplicationInfo info = getItem(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.application_boxed, parent, false);
            }

//            if (!info.filtered) {
//                info.icon = Utilities.createIconThumbnail(info.icon, getContext());
//                info.filtered = true;
//            }

            final TextView textView = (TextView) convertView;
            if (DEBUG) {
                Log.d(TAG, "icon bitmap = " + info.iconBitmap 
                    + " density = " + info.iconBitmap.getDensity());
            }
            info.iconBitmap.setDensity(Bitmap.DENSITY_NONE);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(info.iconBitmap), null, null);
            textView.setText(info.title);

	    	return convertView;
        }
    }

    public AllApps2D(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(View.GONE);
        setSoundEffectsEnabled(false);

        mAppsAdapter = new AppsAdapter(getContext(), mAllAppsList);
        mAppsAdapter.setNotifyOnChange(false);
    }

    @Override
    protected void onFinishInflate() {
        setBackgroundColor(Color.BLACK);

        try {
            mGrid = (GridView)findViewWithTag("all_apps_2d_grid");
            if (mGrid == null) throw new Resources.NotFoundException();
            mGrid.setOnItemClickListener(this);
            mGrid.setOnItemLongClickListener(this);
            mGrid.setBackgroundColor(Color.BLACK);
            mGrid.setCacheColorHint(Color.BLACK);
            
            ImageButton homeButton = (ImageButton) findViewWithTag("all_apps_2d_home");
            if (homeButton == null) throw new Resources.NotFoundException();
            homeButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (!isAnimationRunning()) {
                            mLauncher.closeAllApps(true);
                        }
                    }
                });
            
            mGrid.setAdapter(mAppsAdapter);
            
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find necessary layout elements for AllApps2D");
        }

        setOnKeyListener(this);
    }

    public AllApps2D(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (!isVisible()) return false;

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                mLauncher.closeAllApps(true);
                break;
            default:
                return false;
        }

        return true;
    }

    public void onItemClick(AdapterView parent, View v, int position, long id) {
        ApplicationInfo app = (ApplicationInfo) parent.getItemAtPosition(position);
        mLauncher.startActivitySafely(app.intent, app);
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!view.isInTouchMode()) {
            return false;
        }

        ApplicationInfo app = (ApplicationInfo) parent.getItemAtPosition(position);
        app = new ApplicationInfo(app);

        mDragController.startDrag(view, this, app, DragController.DRAG_ACTION_COPY);
        mLauncher.closeAllApps(true);

        return true;
    }

    protected void onFocusChanged(boolean gainFocus, int direction, android.graphics.Rect prev) {
        if (gainFocus) {
            mGrid.requestFocus();
        }
    }

    public void setDragController(DragController dragger) {
        mDragController = dragger;
    }

    public void onDropCompleted(View target, boolean success) {
    }

    @Override
    public void draw(Canvas canvas) {
        if (mDrawMode == DRAW_NORMAL) {
            super.draw(canvas);
        } else if (mDrawMode == DRAW_OPEN) {
            int alpha = (int) (255 * ((System.currentTimeMillis() - mStartTime) / (float) mDuration));
            if (alpha >= 255) {
                alpha = 255;
            }
            
            int restoreTo = canvas.saveLayerAlpha(getLeft(), getTop(), getRight(), getBottom(), alpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
            getBackground().draw(canvas);
            dispatchDraw(canvas);
            canvas.restoreToCount(restoreTo);
            
            if (isAnimationEnd()) {
                onAnimationEnd();
            }
            invalidate();
        } else if (mDrawMode == DRAW_CLOSE) {
            int alpha = 255 + (int) ((0 - 255) * ((System.currentTimeMillis() - mStartTime) / (float) mDuration));
            if (alpha <= 0) {
                alpha = 0;
            }
            
            int restoreTo = canvas.saveLayerAlpha(getLeft(), getTop(), getRight(), getBottom(), alpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
            getBackground().draw(canvas);
            dispatchDraw(canvas);
            canvas.restoreToCount(restoreTo);
            
            if (isAnimationEnd()) {
                onAnimationEnd();
            }
            invalidate();
        }
    }

    private boolean isAnimationEnd() {
        return (mStartTime != 0) && (mStartTime + mDuration <= System.currentTimeMillis());
    }
    
    public boolean isAnimationRunning() {
        return (mStartTime != 0) && (mStartTime + mDuration >= System.currentTimeMillis());
    }

    /**
     * Zoom to the specifed level.
     *
     * @param zoom [0..1] 0 is hidden, 1 is open
     */
    public void zoom(float zoom, boolean animate) {
//        Log.d(TAG, "zooming " + ((zoom == 1.0) ? "open" : "closed"));
        cancelLongPress();
        mZoom = zoom;

        if (isVisible()) {
            getParent().bringChildToFront(this);
            setVisibility(View.VISIBLE);
            if (animate) {
                mStartTime = System.currentTimeMillis();
                mDrawMode = DRAW_OPEN;
                invalidate();
            } else {
                onAnimationEnd();
            }
        } else {
            if (animate) {
                mDrawMode = DRAW_CLOSE;
                mStartTime = System.currentTimeMillis();
                invalidate();
            } else {
                onAnimationEnd();
            }
        }
    }

    protected void onAnimationEnd() {
        if (isVisible()) {
            mZoom = 1.0f;
        } else {
            setVisibility(GONE);
            mZoom = 0.0f;
        }
        mLauncher.zoomed(mZoom);
        mDrawMode = DRAW_NORMAL;
        mStartTime = 0;
    }

    public boolean isVisible() {
        return mZoom > 0.001f;
    }

    @Override
    public boolean isOpaque() {
        return mZoom > 0.999f;
    }

    public void setApps(ArrayList<ApplicationInfo> list) {
        mAllAppsList.clear();
        addApps(list);
    }
    
    public void reorderApps(){
        if (AllAppsList.mTopPackages == null || mAllAppsList == null 
        		|| AllAppsList.mTopPackages.isEmpty() || mAllAppsList.isEmpty()) {
        	return ;
        } 
        
        ArrayList<ApplicationInfo> appListClone = (ArrayList<ApplicationInfo>) mAllAppsList.clone();
        ArrayList<ApplicationInfo> dataReorder =
            new ArrayList<ApplicationInfo>(AllAppsList.DEFAULT_APPLICATIONS_NUMBER); 
        
        for (AllAppsList.TopPackage tp : AllAppsList.mTopPackages) { 
        	int loop = 0;
        	for (ApplicationInfo ai : appListClone) {         		
        		if (ai.componentName.getPackageName().equals(tp.mPackageName) 
        				&& ai.componentName.getClassName().equals(tp.mClassName)) {
						mAllAppsList.remove(ai);							
										
						dataReorder.add(ai);
						break;
        			}
        		loop++;
        	}                	                
        } 
        
        for (AllAppsList.TopPackage tp : AllAppsList.mTopPackages) { 
        	int newIndex = 0;       	
        	for (ApplicationInfo ai : dataReorder) {         		
        		if (ai.componentName.getPackageName().equals(tp.mPackageName) 
        				&& ai.componentName.getClassName().equals(tp.mClassName)) {        			
        			newIndex = Math.min(Math.max(tp.mOrder, 0), appListClone.size());				
					mAllAppsList.add(newIndex, ai);									
					
					break;
        		}
        	}                	                
        }   
    }

    public void addApps(ArrayList<ApplicationInfo> list) {
//        Log.d(TAG, "addApps: " + list.size() + " apps: " + list.toString());

        final int N = list.size();

        for (int i=0; i<N; i++) {
            final ApplicationInfo item = list.get(i);
            int index = Collections.binarySearch(mAllAppsList, item,
                    LauncherModel.APP_NAME_COMPARATOR);
            if (index < 0) {
                index = -(index+1);
            }
            mAllAppsList.add(index, item);
        }
        if (FeatureOption.MTK_YMCAPROP_SUPPORT) {
        	reorderApps();
        }
        mAppsAdapter.notifyDataSetChanged();
    }

    public void removeApps(ArrayList<ApplicationInfo> list) {
        final int N = list.size();
        for (int i=0; i<N; i++) {
            final ApplicationInfo item = list.get(i);
            int index = findAppByComponent(mAllAppsList, item);
            if (index >= 0) {
                mAllAppsList.remove(index);
            } else {
                Log.w(TAG, "couldn't find a match for item \"" + item + "\"");
                // Try to recover.  This should keep us from crashing for now.
            }
        }
        mAppsAdapter.notifyDataSetChanged();
        if (FeatureOption.MTK_YMCAPROP_SUPPORT) {
        	reorderApps();
        }
    }

    public void updateApps(ArrayList<ApplicationInfo> list) {
        // Just remove and add, because they may need to be re-sorted.
        removeApps(list);
        addApps(list);
    }

    private static int findAppByComponent(ArrayList<ApplicationInfo> list, ApplicationInfo item) {
        ComponentName component = item.intent.getComponent();
        final int N = list.size();
        for (int i=0; i<N; i++) {
            ApplicationInfo x = list.get(i);
            if (x.intent.getComponent().equals(component)) {
                return i;
            }
        }
        return -1;
    }

    public void dumpState() {
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList", mAllAppsList);
    }
    
    public void surrender() {
    }
}


