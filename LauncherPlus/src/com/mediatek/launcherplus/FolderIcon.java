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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.mediatek.launcherplus.R;

/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends BubbleTextView implements DropTarget {
    protected FolderInfo mInfo;
    protected Launcher mLauncher;
    private Drawable mCloseIcon;
    private Drawable mOpenIcon;
    protected  Bitmap mIconBackground;
    private Drawable mFolderIcon;
    private Bitmap mFolderIconBitmap;
    private Bitmap mFolderIconEditBitmap;
    private Bitmap mIconBitmap;
    private Bitmap mIconEditBitmap;
    
    private final int PADDING = 6;
    private final int ICON_NUM_PER_COLUMNS = 3;

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        initIconBackground();       
    }

    public FolderIcon(Context context) {
        super(context);
        initIconBackground();
    }
	
    protected void initIconBackground() {
		// TODO Auto-generated method stub
    	//yellow image
    	if (!Launcher.isDefStyle()) {
    		if (Launcher.isDefaultSkin()) {
    			int random = (int) (Math.random() * (Launcher.mIconBackgroundBitmap.length - 1));
    			mIconBackground = Launcher.mIconBackgroundBitmap[random];
    		} else {
    			mIconBackground = Launcher.mThemeIconBackgroundBitmap;
    		}
    	}
	}

    static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
            UserFolderInfo folderInfo) {

        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);

        final Resources resources = launcher.getResources();
        if (Launcher.isDefStyle()) {
        	Drawable d = resources.getDrawable(R.drawable.ic_launcher_folder);
        	icon.mCloseIcon = d;
        	icon.mOpenIcon = resources.getDrawable(R.drawable.ic_launcher_folder_open);
			icon.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
        } else {
        	Drawable d = resources.getDrawable(R.drawable.launcher_folder);
        	icon.mFolderIcon = d;
        	icon.mFolderIconBitmap = icon.mIconBitmap = ((BitmapDrawable)icon.mFolderIcon).getBitmap();
        	icon.mIconEditBitmap = IconCache.createCompoundBitmap(icon.getContext(), icon.mIconBitmap, launcher.mIconEditBg, false);
        	icon.mFolderIconEditBitmap = icon.mIconEditBitmap = IconCache.createCompoundBitmap(icon.getContext(), icon.mIconEditBitmap, launcher.mKillBitmap, true);
        
        	if (Launcher.isInEditMode()) {
        		icon.setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(icon.mIconEditBitmap), null, null);
        		icon.setPadding(icon.getPaddingLeft(), icon.getPaddingTop()-Launcher.mKillBitmap.getHeight()/2, icon.getPaddingRight(), icon.getPaddingBottom());
        		icon.setCompoundDrawablePadding(icon.getCompoundDrawablePadding()-Launcher.mKillBitmap.getHeight()/2+3);
        	} else {
                if (Launcher.isQVGAMode()) {
                    Bitmap mTempIconBitmap = IconCache.createCompoundBitmap(
                            icon.getContext(), icon.mIconBitmap, icon.mIconBitmap, false);
                    icon.setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mTempIconBitmap), null, null);
                } else {
        		icon.setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(icon.mIconBitmap), null, null);
                }
        	}
        }
        icon.setText(folderInfo.title);
        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        
        return icon;
    }

    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        final ItemInfo item = (ItemInfo) dragInfo;
        final int itemType = item.itemType;
        return (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT);
//                && item.container != mInfo.id;
    }

    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo, Rect recycle) {
        return null;
    }

    public void getEstimateDropCellForShowGreen(int x, int y, int xOffset, int yOffset, Object dragInfo) {
    	if(Launcher.getCurrentScene().equals(Launcher.shortcutTheme)){
    		mLauncher.getWorkspace().reorderCellLayout(x, y, xOffset, yOffset, dragInfo);
    	}
    }

    public boolean onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        ShortcutInfo item = null;
        if (dragInfo instanceof ApplicationInfo) {
            // Came from all apps -- make a copy
            item = ((ApplicationInfo)dragInfo).makeShortcut();
        } else if (dragInfo instanceof ShortcutInfo) {
            item = (ShortcutInfo)dragInfo;
        }
        
        if (item != null && (mInfo instanceof UserFolderInfo)){
        	((UserFolderInfo)mInfo).add(item);
        	LauncherModel.addOrMoveItemInDatabase(mLauncher, item, mInfo.id, 0, 0, 0);
        }
        else 
        {
        	return false;
        }
        
        if (Launcher.isShortcutScreen()) {
        	updateAppIconThumbails();
        }
        
        return true;
    }
    
    public void updateAppIconThumbails() {
    	final UserFolderInfo info = (UserFolderInfo) mInfo;
        final ArrayList<ShortcutInfo> contents = info.contents;
        //if (contents.size() <= 9 ) {
        	final int contentsCount = contents.size() < 9 ? contents.size() : 9;
        	Bitmap compoundBitmap = Bitmap.createBitmap(mFolderIconBitmap.getWidth(), mFolderIconBitmap.getHeight(), Config.ARGB_8888);;
        	Canvas canvas = new Canvas(compoundBitmap);
        	canvas.drawBitmap(mFolderIconBitmap, 0, 0, null);
        	for (int k=0; k<contentsCount; k++) {
        		final ShortcutInfo appInfo = contents.get(k);
        		Bitmap appIcon = appInfo.getAppIcon(((LauncherApplication)mLauncher.getApplication()).getIconCache());
        		Bitmap appIconThumbails = IconCache.scaleBitmap(appIcon, 0.3f, 0.3f);
        		int appIconWidth = appIconThumbails.getWidth();
        		int appIconHeight = appIconThumbails.getHeight();
        		int i = k / ICON_NUM_PER_COLUMNS;
        		int j = k % ICON_NUM_PER_COLUMNS;
        		int xPos = PADDING*(j+1) + j*appIconWidth;
        		int yPos = PADDING*(i+1) + i*appIconHeight;
        		canvas.drawBitmap(appIconThumbails, xPos, yPos, null);
        	}
        	mIconBitmap = compoundBitmap;
        	mIconEditBitmap = IconCache.createCompoundBitmap(getContext(), mIconBitmap, mLauncher.mIconEditBg, false);
        	mIconEditBitmap = IconCache.createCompoundBitmap(getContext(), mIconEditBitmap, mLauncher.mKillBitmap, true);
        	if (Launcher.isInEditMode()) {
        		setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mIconEditBitmap), null, null);
        	} else {
            if (Launcher.isQVGAMode()) {
                Bitmap mTempIconBitmap = IconCache.createCompoundBitmap(
                        getContext(), mIconBitmap, mIconBitmap, false);
                setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mTempIconBitmap), null, null);
            } else {
        		setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mIconBitmap), null, null);
            }
        	}
        //}
    }

    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    	/*if (Launcher.isInEditMode()) {
        	setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mOpenEditBitmap), null, null);
        } else {
        	setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mOpenBitmap), null, null);
        }*/
    	if (Launcher.isDefStyle()) {
        	setCompoundDrawablesWithIntrinsicBounds(null, mOpenIcon, null, null);
    	}
    }

    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }

    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    	/*if (Launcher.isInEditMode()) {
        	setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mCloseEditBitmap), null, null);
        } else {
        	setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mCloseBitmap), null, null);
        }*/
    	if (Launcher.isDefStyle()) {
        	setCompoundDrawablesWithIntrinsicBounds(null, mCloseIcon, null, null);
    	}
    }
    
    public FolderInfo getInfo () {
    	return mInfo;
    }
    
    public Drawable getOpenIcon() {
    	return mOpenIcon;
    }
    
    public Drawable getIcon() {
    	return mFolderIcon;
    }
    
    public Bitmap getIconBitmap() {
    	return mIconBitmap;
    }
    
    public Bitmap getIconEditBitmap() {
    	return mIconEditBitmap;
    }
}
