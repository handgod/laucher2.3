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

import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AccelerateInterpolator;
import android.graphics.RectF;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import  android.content.pm.IPackageDeleteObserver;

import com.mediatek.launcherplus.R;

public class DeleteZone extends ImageView implements DropTarget, DragController.DragListener {
    private static final int ORIENTATION_HORIZONTAL = 1;
    private static final int TRANSITION_DURATION = 250;
    private static final int ANIMATION_DURATION = 200;

    private final int[] mLocation = new int[2];
    
    private Launcher mLauncher;
    private boolean mTrashMode;

    private AnimationSet mInAnimation;
    private AnimationSet mOutAnimation;
    private Animation mHandleInAnimation;
    private Animation mHandleOutAnimation;

    private int mOrientation;
    private DragController mDragController;

    private final RectF mRegion = new RectF();
    private TransitionDrawable mTransition;
    private View mHandle;
    private final Paint mTrashPaint = new Paint();

    public DeleteZone(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeleteZone(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final int srcColor = context.getResources().getColor(R.color.delete_color_filter);
        mTrashPaint.setColorFilter(new PorterDuffColorFilter(srcColor, PorterDuff.Mode.SRC_ATOP));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DeleteZone, defStyle, 0);
        mOrientation = a.getInt(R.styleable.DeleteZone_direction, ORIENTATION_HORIZONTAL);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTransition = (TransitionDrawable) getDrawable();
    }

    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        return true;
    }
    
    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo, Rect recycle) {
        return null;
    }

    public void getEstimateDropCellForShowGreen(int x, int y, int xOffset, int yOffset, Object dragInfo) {
    }

    private void uninstallPkg(String packageName) {
        // Create new intent to launch Uninstaller activity
       Uri packageURI = Uri.parse("package:"+packageName);
       Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
       mLauncher.startActivity(uninstallIntent);
   }
    
    public boolean onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        final ItemInfo item = (ItemInfo) dragInfo;

        if (item.container == -1) return true;
        
        boolean isDrop = true;
        
        if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
            if (item instanceof LauncherAppWidgetInfo) {
                mLauncher.removeAppWidget((LauncherAppWidgetInfo) item);
            }
        } else {
            if (source instanceof UserFolder) {
                final UserFolder userFolder = (UserFolder) source;
                final UserFolderInfo userFolderInfo = (UserFolderInfo) userFolder.getInfo();
                // Item must be a ShortcutInfo otherwise it couldn't have been in the folder
                // in the first place.
                if(item instanceof ShortcutInfo && !(((ShortcutInfo)item).isSystemApp())){
                	//userFolderInfo.remove((ShortcutInfo)item);
                }                
            }
        }
        if (item instanceof UserFolderInfo) {
        	if ( mLauncher.isShortcutScreen() ) {
        		if(!((UserFolderInfo)item).contents.isEmpty()){
        		return !deleteFolderDialog(source, (UserFolderInfo) item);
			}
        	} else {
        		final UserFolderInfo userFolderInfo = (UserFolderInfo)item;
                LauncherModel.deleteUserFolderContentsFromDatabase(mLauncher, userFolderInfo);
                mLauncher.removeFolder(userFolderInfo);
        	}
		} else if ( mLauncher.isShortcutScreen() && item instanceof ShortcutInfo) {
			if (((ShortcutInfo)item).isSystemApp()) {
				Toast.makeText(mLauncher.getApplicationContext(), R.string.system_app_delete, Toast.LENGTH_SHORT).show();
				isDrop = false;
			} else {
				return !deleteShortcutDialog(source, (ShortcutInfo) item);
			}
			isDrop = false;
		} else if (item instanceof LauncherAppWidgetInfo) {
			final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) item;
			final LauncherAppWidgetHost appWidgetHost = mLauncher
					.getAppWidgetHost();
			if (appWidgetHost != null) {
                final int appWidgetId = launcherAppWidgetInfo.appWidgetId;
                // Deleting an app widget ID is a void call but writes to disk before returning
                // to the caller...
                new Thread("deleteAppWidgetId") {
                    public void run() {
                        appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
                    }
                }.start();
            }
		}

	if(isDrop){
        	LauncherModel.deleteItemFromDatabase(mLauncher, item);
        }
        
        if (mLauncher.isInEditMode()) {
        	mLauncher.saveEditOperations();
        }
        
        return true;
    }
    
	public boolean deleteShortcutDialog(final DragSource source, final ShortcutInfo dragInfo) {
		if (dragInfo.isSystemApp()) {
			Toast.makeText(mLauncher.getApplicationContext(),
					R.string.system_app_delete, Toast.LENGTH_SHORT).show();
			return false;
		} else {
			AlertDialog	deleteShortcutDialog = new AlertDialog.Builder(mLauncher)
						.setTitle(R.string.remove_dialog_title)
						.setPositiveButton(R.string.remove_dialog_positive, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int which) {
								deletePackage(dragInfo);
								source.onDropCompleted(DeleteZone.this, true);
								mDragController.endDrag();
								if (mLauncher.isInEditMode()) {
									mLauncher.saveEditOperations();
								}
							}
						})
						.setNegativeButton(R.string.remove_dialog_negative, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								mDragController.cancelDrag();
							}
						}).create();
			
			final Resources res = mLauncher.getResources();
			StringBuilder sb = new StringBuilder();
			sb.append(dragInfo.title).append(" ").append(res.getString(R.string.remove_app_info));
			deleteShortcutDialog.setMessage(sb.toString());
			deleteShortcutDialog.show();
			return true;
		}
	}
    
	public boolean deleteFolderDialog(final DragSource source, final UserFolderInfo userFolderInfo) {
		AlertDialog	deleteFolderDialog = new AlertDialog.Builder(mLauncher).setTitle(R.string.remove_dialog_title)
					.setPositiveButton(R.string.remove_dialog_positive,new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							deleteUserFolderInfo(userFolderInfo);
							if(userFolderInfo.contents.isEmpty()){
								source.onDropCompleted(DeleteZone.this, true);
							} else {
								source.onDropCompleted(DeleteZone.this, false);
							}
							mDragController.endDrag();
							if (mLauncher.isInEditMode()) {
								mLauncher.saveEditOperations();
							}
						}
					})
					.setNegativeButton(R.string.remove_dialog_negative,	new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mDragController.cancelDrag();
						}
					}).create();
		
		final Resources res = mLauncher.getResources();
		StringBuilder sb = new StringBuilder();
		sb.append(res.getString(R.string.remove_folder_title));
		sb.append(" '" + userFolderInfo.title + "' ");
		sb.append(res.getString(R.string.remove_folder_text));
		if (!userFolderInfo.contents.isEmpty()) {
			boolean allSystemApps = true;
			for (int i = 0; i < userFolderInfo.contents.size(); i++) {
				final ShortcutInfo shortcutInfo = userFolderInfo.contents.get(i);
				if (!shortcutInfo.isSystemApp()) {
					if (allSystemApps) {
						sb.append(res.getString(R.string.remove_folder_info));
						allSystemApps = false;
					}
					sb.append("\n" + shortcutInfo.title);
//					shortcuts.add(shortcutInfo);
				}
			}
			
			if (allSystemApps) {
				// If all apps in the folder are system apps, we won't show the
				// dialog.
				return false;
			}
		}

		deleteFolderDialog.setMessage(sb.toString());
		deleteFolderDialog.show();
		return true;
	}
    
    private void deleteUserFolderInfo(UserFolderInfo userFolderInfo){
    	boolean containSystemApp = false;
    	if(!userFolderInfo.contents.isEmpty()){
    		for(int i=0;i<userFolderInfo.contents.size();i++){
    			final ShortcutInfo shortcutInfo = userFolderInfo.contents.get(i);
				if (shortcutInfo.isSystemApp()) {
					containSystemApp = true;
				} else {
					deletePackage(shortcutInfo);
				}    			
    		}
    	}
    	
    	if(!containSystemApp){
    		LauncherModel.deleteUserFolderContentsFromDatabase(mLauncher, userFolderInfo);
        	mLauncher.removeFolder(userFolderInfo);
        	Workspace workspace = mLauncher.getWorkspace();
        	//Folder folder = workspace.getFolderForTag(userFolderInfo);
        	FolderIcon  folder = workspace.getFolder(userFolderInfo);
        	mDragController.removeDropTarget((DropTarget) folder);
        	if (mLauncher.isInEditMode()) {
        		final FolderIcon folderIcon = (FolderIcon) workspace.getViewForTag(userFolderInfo);
        		((CellLayout)workspace.getChildAt(workspace.getCurrentScreen())).removeViewInLayout(folderIcon);
        	}
    	} 
    }
    
    private void deletePackage(ShortcutInfo shortcutInfo){
    	if (!shortcutInfo.isSystemApp()) {
    		final String packageName = shortcutInfo.intent.getComponent().getPackageName();
    		deletePackage(shortcutInfo.id, shortcutInfo.title, packageName);
    	}
    }
    
    private void deletePackage(long id, CharSequence title, String packageName){
		try {
			mLauncher.getPackageManager().deletePackage(packageName,
					new PackageDeleteObserver(id, title.toString()), 0);
		} catch (Exception e) {
			Log.e("DeleteZone", e.toString());
			Toast.makeText(mLauncher.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
    }
    
    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        mTransition.startTransition(TRANSITION_DURATION);
        dragView.setPaint(mTrashPaint);
    }

    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }

    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        mTransition.resetTransition();
        dragView.setPaint(null);
    }

    public void onDragStart(DragSource source, Object info, int dragAction) {
        final ItemInfo item = (ItemInfo) info;
        if (item != null) {
            mTrashMode = true;
            createAnimations();
            final int[] location = mLocation;
            getLocationOnScreen(location);
            mRegion.set(location[0], location[1], location[0] + mRight - mLeft,
                    location[1] + mBottom - mTop);
            mDragController.setDeleteRegion(mRegion);
            mTransition.resetTransition();
            startAnimation(mInAnimation);
            mHandle.startAnimation(mHandleOutAnimation);
            setVisibility(VISIBLE);
        }
    }

    public void onDragEnd() {
        if (mTrashMode) {
            mTrashMode = false;
            mDragController.setDeleteRegion(null);
            startAnimation(mOutAnimation);
            mHandle.startAnimation(mHandleInAnimation);
            setVisibility(GONE);
        }
    }

    private void createAnimations() {
        if (mInAnimation == null) {
            mInAnimation = new FastAnimationSet();
            final AnimationSet animationSet = mInAnimation;
            animationSet.setInterpolator(new AccelerateInterpolator());
            animationSet.addAnimation(new AlphaAnimation(0.0f, 1.0f));
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                animationSet.addAnimation(new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f));
            } else {
                animationSet.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                        1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f));
            }
            animationSet.setDuration(ANIMATION_DURATION);
        }
        if (mHandleInAnimation == null) {
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                mHandleInAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f);
            } else {
                mHandleInAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f);
            }
            mHandleInAnimation.setDuration(ANIMATION_DURATION);
        }
        if (mOutAnimation == null) {
            mOutAnimation = new FastAnimationSet();
            final AnimationSet animationSet = mOutAnimation;
            animationSet.setInterpolator(new AccelerateInterpolator());
            animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                animationSet.addAnimation(new FastTranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f));
            } else {
                animationSet.addAnimation(new FastTranslateAnimation(Animation.RELATIVE_TO_SELF,
                        0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f));
            }
            animationSet.setDuration(ANIMATION_DURATION);
        }
        if (mHandleOutAnimation == null) {
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                mHandleOutAnimation = new FastTranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f);
            } else {
                mHandleOutAnimation = new FastTranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f, Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f);
            }
            mHandleOutAnimation.setFillAfter(true);
            mHandleOutAnimation.setDuration(ANIMATION_DURATION);
        }
    }

    void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    void setDragController(DragController dragController) {
        mDragController = dragController;
    }

    void setHandle(View view) {
        mHandle = view;
    }

    private static class FastTranslateAnimation extends TranslateAnimation {
        public FastTranslateAnimation(int fromXType, float fromXValue, int toXType, float toXValue,
                int fromYType, float fromYValue, int toYType, float toYValue) {
            super(fromXType, fromXValue, toXType, toXValue,
                    fromYType, fromYValue, toYType, toYValue);
        }

        @Override
        public boolean willChangeTransformationMatrix() {
            return true;
        }

        @Override
        public boolean willChangeBounds() {
            return false;
        }
    }

    private static class FastAnimationSet extends AnimationSet {
        FastAnimationSet() {
            super(false);
        }

        @Override
        public boolean willChangeTransformationMatrix() {
            return true;
        }

        @Override
        public boolean willChangeBounds() {
            return false;
        }
	}
    
    private final static int UNINSTALL_MESSAGE = 1;
    private final static int UNINSTALL_FAILURE = 0;
    private final static int UNINSTALL_SUCCESS = 1;
    
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == UNINSTALL_MESSAGE){
				final Resources res = mLauncher.getResources();
				StringBuilder sb = new StringBuilder();
				switch(msg.arg1){
				case UNINSTALL_FAILURE:
					sb.append(res.getString(R.string.remove_app_failure));
					sb.append(" '"+msg.getData().getCharSequence("title")+"'");
					break;
				case UNINSTALL_SUCCESS:
					sb.append("'"+msg.getData().getCharSequence("title")+"' ");
					sb.append(res.getString(R.string.remove_app_success));
					long id = msg.getData().getLong("id");
					LauncherModel.deleteItemFromDatabase(mLauncher, id);
					break;
				default:
				}
				Toast.makeText(mLauncher.getApplicationContext(), sb.toString(), Toast.LENGTH_SHORT).show();
			}
		}
	};
    
    class  PackageDeleteObserver  extends  IPackageDeleteObserver.Stub {
    	private long id;
    	private String title = null;
    	
    	public PackageDeleteObserver(long id, String title){
    		this.id = id;
    		this.title = title;
    	}
    	
        public  void  packageDeleted(boolean  succeeded) {
                Message msg = mHandler.obtainMessage(UNINSTALL_MESSAGE);
                msg.arg1 = succeeded? UNINSTALL_SUCCESS:UNINSTALL_FAILURE;
                Bundle data = new Bundle();
                data.putLong("id", id);
                data.putCharSequence("title", title);
                msg.setData(data);
                mHandler.sendMessage(msg);
        }
    }
}
