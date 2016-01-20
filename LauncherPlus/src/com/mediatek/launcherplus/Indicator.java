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

package com.mediatek.launcherplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Indicator extends View {
    public static final int ORIENTATION_VERTICAL = 0;
    public static final int ORIENTATION_HORIZONTAL = 1;

    private int mXPos = 0;

    private int mYPos = 0;

    private int mOrientation;

    private Bitmap mIndicatorBitmap;
    
    private Bitmap mIndicatorBigBitmap;

    private int mIndicatorWidth = 0;

    private int mIndicatorHeight = 0;

    private float mIndicatorOffset;
    
    private Launcher mLauncher;


    public Indicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Indicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Indicator, defStyle, 0);
        mOrientation = a.getInt(R.styleable.Indicator_direction, ORIENTATION_HORIZONTAL);
        a.recycle();

        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
        op.inScaled = false;
        mIndicatorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.indicator, op);
        mIndicatorBigBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.indicator_big, op);
    }
    
    void setLaucher(Launcher launcher){
    	this.mLauncher = launcher;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int height = getHeight();
        mIndicatorWidth = mIndicatorBitmap.getWidth(); 
        mIndicatorHeight = mIndicatorBitmap.getHeight(); 
        mXPos = 0;
        mYPos = (height - mIndicatorHeight)/2;
		if (DisplayMetrics.DENSITY_DEVICE > 160) {
			mYPos -= 2;
        }
    }

    @Override
	protected void onDraw(Canvas canvas) {
		if (mOrientation != ORIENTATION_HORIZONTAL) { // Portrait
			Paint p = new Paint();
			p.setColor(Color.DKGRAY);
			canvas.drawRect(0, 0, getWidth(), getHeight(), p);
		}
		Bitmap indicatorBitmap = (mLauncher != null && mLauncher.getWorkspace().getChildCount() == 5) ? 
				mIndicatorBigBitmap : mIndicatorBitmap;

		canvas.drawBitmap(indicatorBitmap, mXPos, mYPos, null);
	}

    public void setPosition(int scrollX) {
    	final int width = getWidth();
    	final int screen_count = mLauncher == null ? Launcher.SCREEN_COUNT : mLauncher.getWorkspace().getChildCount();
        if (mOrientation == ORIENTATION_HORIZONTAL) {    // Portrait
            final int padding = (width - mIndicatorWidth * screen_count) / (screen_count);
            mIndicatorOffset = (mIndicatorWidth + padding) / (float) ((View) getParent()).getWidth();
        } else {    // Landscape
            final int padding = (width - mIndicatorWidth * screen_count) / (screen_count);
            mIndicatorOffset = (mIndicatorWidth + padding) / (float) ((View) getParent()).getWidth();
        }
        mXPos = (int) (scrollX * mIndicatorOffset);
    }
    
    public int getOrientation(){
    	return mOrientation;
    }
}
