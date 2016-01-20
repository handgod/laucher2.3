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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.HashMap;

/**
 * Cache of application icons.  Icons can be made from any thread.
 */
public class IconCache {
    private static final String TAG = "LauncherPlus.IconCache";

    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;

    private static class CacheEntry {
    	public Bitmap appIcon;
        public Bitmap icon;
        public String title;
        public Bitmap titleBitmap;
        public Bitmap iconBackground;
        public Bitmap iconInEditMode;
    }

    private final Bitmap mDefaultIcon;
    private final LauncherApplication mContext;
    private final PackageManager mPackageManager;
    private final Utilities.BubbleText mBubble;
    private final HashMap<ComponentName, CacheEntry> mCache =
            new HashMap<ComponentName, CacheEntry>(INITIAL_ICON_CACHE_CAPACITY);

    public IconCache(LauncherApplication context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mBubble = new Utilities.BubbleText(context);
        mDefaultIcon = makeDefaultIcon();
    }

    private Bitmap makeDefaultIcon() {
        Drawable d = mPackageManager.getDefaultActivityIcon();
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        return b;
    }

    /**
     * Remove any records for the supplied ComponentName.
     */
    public void remove(ComponentName componentName) {
        synchronized (mCache) {
            mCache.remove(componentName);
        }
    }

    /**
     * Empty out the cache.
     */
    public void flush() {
        synchronized (mCache) {
            mCache.clear();
        }
    }

    /**
     * Fill in "application" with the icon and label for "info."
     */
    public void getTitleAndIcon(ApplicationInfo application, ResolveInfo info) {
        synchronized (mCache) {
            CacheEntry entry = cacheLocked(application.componentName, info);
            if (entry.titleBitmap == null) {
                entry.titleBitmap = mBubble.createTextBitmap(entry.title);
            }

            application.title = entry.title;
            application.titleBitmap = entry.titleBitmap;
            application.iconBitmap = entry.icon;
        }
    }
    
    public Bitmap getIconInEditMode(Intent intent) {
    	synchronized (mCache) {
            final ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
            ComponentName component = intent.getComponent();

            if (resolveInfo == null || component == null) {
            	int random = (int) (Math.random() * (Launcher.mIconBackgroundBitmap.length - 1));
                return Launcher.mIconBackgroundBitmap[random];
            }

            CacheEntry entry = cacheLocked(component, resolveInfo);
            return entry.iconInEditMode;
        }
    }
    
    public Bitmap getIcon(Intent intent) {
        synchronized (mCache) {
            final ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
            ComponentName component = intent.getComponent();

            if (resolveInfo == null || component == null) {
                return mDefaultIcon;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo);
            return entry.icon;
        }
    }
    
    public Bitmap getAppIcon(Intent intent) {
    	synchronized (mCache) {
            final ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
            ComponentName component = intent.getComponent();

            if (resolveInfo == null || component == null) {
                return mDefaultIcon;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo);
            return entry.appIcon;
        }
    }

    public Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo) {
        synchronized (mCache) {
            if (resolveInfo == null || component == null) {
                return null;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo);
            return entry.icon;
        }
    }
    
    private CacheEntry cacheLocked(ComponentName componentName, ResolveInfo info) {
        CacheEntry entry = mCache.get(componentName);
        if (entry == null) {
            entry = new CacheEntry();

            mCache.put(componentName, entry);

            entry.title = info.loadLabel(mPackageManager).toString();
            if (entry.title == null) {
                entry.title = info.activityInfo.name;
            }
            entry.icon = entry.appIcon = Utilities.createIconBitmap(
                    info.activityInfo.loadIcon(mPackageManager), mContext);
            
            if (!Launcher.isDefStyle()) {
            	if (Launcher.isDefaultSkin()) {
            		int random = (int) (Math.random() * (Launcher.mIconBackgroundBitmap.length - 1));
            		entry.iconBackground = Launcher.mIconBackgroundBitmap[random];
            	} else {
            		entry.iconBackground = Launcher.mThemeIconBackgroundBitmap;
            	}
            	
                entry.icon = createCompoundBitmap(mContext, entry.iconBackground, entry.appIcon , false);
                entry.iconInEditMode = createCompoundBitmap(mContext, entry.icon, Launcher.mIconEditBg, false);
                
                if (!isSystemApp(info)) {
                	entry.iconInEditMode = createCompoundBitmap(mContext, entry.iconInEditMode, Launcher.mKillBitmap,true);            	
                }
            }		           
        }
        return entry;
    }
    
	public static Bitmap createCompoundBitmap(Context context, Bitmap bg, Bitmap icon, boolean isEditMode) {
    	bg = scaleBitmap(context, bg);
    	icon = scaleBitmap(context, icon);
    	
    	final int bgWidth = bg.getWidth();
    	final int bgHeight = bg.getHeight();
    	final int iconWidth = icon.getWidth();
		final int iconHeight = icon.getHeight();

		Bitmap compoundBitmap = null;
		if (isEditMode) {
			compoundBitmap = Bitmap.createBitmap(bgWidth +iconWidth, bgHeight+bgHeight/2, Config.ARGB_8888);
		} else {
			compoundBitmap = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
		}
		Canvas canvas = new Canvas(compoundBitmap);
		if (isEditMode) {
			canvas.drawBitmap(bg, iconWidth/2, iconHeight/2, null);
			canvas.drawBitmap(icon, bgWidth, 0, null);
		} else {
			canvas.drawBitmap(bg, 0, 0, null);
			canvas.drawBitmap(icon, (bgWidth - iconWidth) / 2, (bgHeight - iconHeight) / 2, null);
		}
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		
		return compoundBitmap;
    }
    
    /**
     * scale a bitmap
     * @param context
     * @param bm
     * @return The sacled bitmap
     */
	static Bitmap scaleBitmap(Context context, Bitmap bm) {
		float sx = 1.0f;
    	float sy = 1.0f;
    	
    	if (Launcher.isDefStyle()) {
    		sx = 0.95f;
    		sy = 0.95f;
    	}

		if (DisplayMetrics.DENSITY_DEVICE <= 120) {
			sx = 0.86f;
			sy = 0.86f;
		} else if (DisplayMetrics.DENSITY_DEVICE <= 160){
			sx = 0.95f;
    		sy = 0.95f;
		}
        
        if (Launcher.isQVGAMode()) {
            sx = 0.73f;
            sy = 0.73f;
        }
        return scaleBitmap(bm, sx, sy);		
	}
	
	
	static Bitmap scaleBitmap(Bitmap bm, float sx, float sy) {
		if (sx == 1.0f && sy == 1.0f) {
			return bm;
		}
		
		Matrix matrix = new Matrix();
		matrix.postScale(sx, sy);
		return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
	}
    
    public boolean isSystemApp(ResolveInfo resolveInfo) {
    	if (resolveInfo != null && (resolveInfo.activityInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
            return true;
        }

        return false;
    }
}
