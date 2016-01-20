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

import com.android.common.Search;
import com.mediatek.launcherplus.AddAdapter;
import com.mediatek.launcherplus.AddAdapter.ListItem;
import com.mediatek.launcherplus.CellLayout.CellInfo;
import com.mediatek.launcherplus.CellLayout.LayoutParams;
import com.mediatek.launcherplus.LauncherProvider.DatabaseHelper;
import com.mediatek.launcherplus.LauncherSettings.Favorites;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.StatusBarManager;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.LiveFolders;
import android.provider.Settings;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;
import android.widget.ImageView.ScaleType;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.DataInputStream;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;

import android.widget.IMTKWidget;
import com.mediatek.featureoption.FeatureOption;

/**
 * Default launcher application.
 */
public final class Launcher extends Activity implements View.OnClickListener, OnLongClickListener,
        LauncherModel.Callbacks, AllAppsView.Watcher{
    static final String TAG = "LauncherPlus.Launcher";

	static final String TAG_SURFACEWIDGET = "mtkWidgetView";

	static final boolean DEBUG_SURFACEWIDGET = true;
    
    static final String MORE_FOLDER = "More";
    
    static final String CURRENT_SCENE = "Current_scene";
    
    static final String CURRENT_STYLE = "Current_style";
    
    static final String CURRENT_SKIN = "Current_skin";

    static final boolean LOGD = false;

    static final boolean PROFILE_STARTUP = false;

    static final boolean DEBUG_WIDGETS = false;

    static final boolean DEBUG_USER_INTERFACE = false;

    private static final int WALLPAPER_SCREENS_SPAN = 2;
    
    private static final int MENU_GROUP_ADD = 1;

    private static final int MENU_GROUP_WALLPAPER = MENU_GROUP_ADD + 1;
    
    private static final int MENU_GROUP_SWITCH_STYLE = MENU_GROUP_WALLPAPER + 1;
    
    private static final int MENU_GROUP_ENTER_EDIT = MENU_GROUP_SWITCH_STYLE + 1;
    
    private static final int MENU_GROUP_NOTIFICATIONS = MENU_GROUP_ENTER_EDIT + 1;
    
    private static final int MENU_GROUP_SEARCH = MENU_GROUP_NOTIFICATIONS + 1;
    
    private static final int MENU_GROUP_SETTINGS = MENU_GROUP_SEARCH + 1;
    
    private static final int MENU_GROUP_EDIT = MENU_GROUP_SETTINGS + 1;

    private static final int MENU_ADD = Menu.FIRST + 1;

    private static final int MENU_WALLPAPER_SETTINGS = MENU_ADD + 1;

    private static final int MENU_SEARCH = MENU_WALLPAPER_SETTINGS + 1;

    private static final int MENU_NOTIFICATIONS = MENU_SEARCH + 1;
    
    private static final int MENU_EDIT = MENU_NOTIFICATIONS + 1;

    //private static final int MENU_SETTINGS = MENU_NOTIFICATIONS + 1;
    
    private static final int MENU_SETTINGS = MENU_EDIT + 1;
    
    private static final int MENU_SWITCH_STYLE = MENU_SETTINGS + 1;
    
    private static final int MENU_EDIT_SAVE = MENU_SWITCH_STYLE + 1;
    
    private static final int MENU_EDIT_CANCEL = MENU_EDIT_SAVE + 1;

    private static final int REQUEST_CREATE_SHORTCUT = 1;

    private static final int REQUEST_CREATE_LIVE_FOLDER = 4;

    private static final int REQUEST_CREATE_APPWIDGET = 5;

    private static final int REQUEST_PICK_APPLICATION = 6;

    private static final int REQUEST_PICK_SHORTCUT = 7;

    private static final int REQUEST_PICK_LIVE_FOLDER = 8;

    private static final int REQUEST_PICK_APPWIDGET = 9;

    private static final int REQUEST_PICK_WALLPAPER = 10;
    
    private static final int REQUEST_SET_THEME = 11;
    
    static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    static final int SCREEN_COUNT = 8;

    static final int DEFAULT_SCREEN = 0;

    static final int NUMBER_CELLS_X = 4;

    static final int NUMBER_CELLS_Y = 4;

    static final int DIALOG_CREATE_SHORTCUT = 1;

    static final int DIALOG_RENAME_FOLDER = 2;
    
    static final float PREVIEW_SCALE = 100.0f / 320;

    static final int PREVIEW_PADDING = 4;

    private static final String PREFERENCES = "launcher.preferences";

    // Type: int
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    
    // Type: int
    private static final String RUNTIME_STATE_CURRENT_INDICATOR = "launcher.current_indicator";

    // Type: boolean
    private static final String RUNTIME_STATE_ALL_APPS_FOLDER = "launcher.all_apps_folder";

    // Type: long
    private static final String RUNTIME_STATE_USER_FOLDERS = "launcher.user_folder";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cellX";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cellY";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_spanX";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_spanY";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_COUNT_X = "launcher.add_countX";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_COUNT_Y = "launcher.add_countY";

    // Type: int[]
    private static final String RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS = "launcher.add_occupied_cells";

    // Type: boolean
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";

    // Type: long
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
    // video live wallpaper
    private static final String VIDEO_LIVE_WALLPAPER_PKG = "com.mediatek.vlw";
    private static final String VIDEO_LIVE_WALLPAPER_CLS = "com.mediatek.vlw.VideoEditor";
	private static final String VIDEO_WIDGET = "VideoWidgetProvider";
	
	private static final String STK_PACKAGE = "com.android.stk";
    
    private static final String STK2_PACKAGE = "com.android.stk2";    

	static final int APPWIDGET_HOST_ID = 1024;

    private static final Object sLock = new Object();

    private static int sScreen = DEFAULT_SCREEN;

    private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver();

    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

    private LayoutInflater mInflater;

    private DragController mDragController;

    private Workspace mWorkspace;
    
    private Workspace mShortcutWorkspace;
    
    private Workspace mWidgetWorkspace;
    
    private Workspace mDefaultWorkspace;

    private AppWidgetManager mAppWidgetManager;

    private LauncherAppWidgetHost mAppWidgetHost;

    private CellLayout.CellInfo mAddItemCellInfo;

    private CellLayout.CellInfo mMenuAddInfo;

    private final int[] mCellCoordinates = new int[2];

    private FolderInfo mFolderInfo;

    private DeleteZone mDeleteZone;
    
    private HandleView mHandleView;
    
    private AllAppsView mAllAppsGrid;

    private Bundle mSavedState;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mWorkspaceLoading = true;

    private boolean mPaused = true;

    private boolean mRestoring;

    private boolean mWaitingForResult;

    private Bundle mSavedInstanceState;

    private LauncherModel mModel;

    private IconCache mIconCache;

    private ArrayList<ItemInfo> mShortcutDesktopItems = new ArrayList<ItemInfo>();
    
    private ArrayList<ItemInfo> mAppNotInDesktopItems = new ArrayList<ItemInfo>(); 
    
    private ArrayList<ItemInfo> mWidgetDesktopItems = new ArrayList<ItemInfo>();

    private static HashMap<Long, FolderInfo> mFolders = new HashMap<Long, FolderInfo>();
    
    private static String mCurrentStyle = "Default";
    
    final static String defStyle = "Default";
    
    final static String enhanceStyle = "Enhance";

    private static String mCurrentTheme = "Shortcut";
    
    public final static String widgetTheme = "Widget";
    
    public final static String shortcutTheme = "Shortcut";
    
    public static String mCurrentSkin;
    
    private ImageView mPhoneShortcut;

    private ImageView mMessageShortcut;
    
    private ImageView mSwitchShortcutAndWidget;
    
    private Animation mOutAnimation;
    
    private Animation mInAnimation;

    private RelativeLayout mFolderOpenRelativeLayout;

	private Folder mOpenFolder;
    
    private Animation mFolderOpenAnimation;
    
    private static WallpaperIntentReceiver sWallpaperReceiver;
    
    private Dialog mProgressDialog = null;

    private Indicator mIndicator;
    
    private Bitmap mThumbnail = null;

    private ImageView mPreview;

    private RelativeLayout mBottomLayer;
    
    private RelativeLayout mTopLayer;

    private boolean mPortrait = true;
    
    private DragLayer mDragLayer;
    
    private static boolean mIsDefStyle = true;
    
    private static boolean mIsShortcutWorkspace = true;
    
    private boolean mLocaleChanged;
    
    static final int CELL_COUNT = NUMBER_CELLS_X * NUMBER_CELLS_X ;
    
    private static boolean[] mIdleCell = new boolean[CELL_COUNT];
    
    static String[] pageClassification;
    
    static final int INITIAL_ICON_BACKGROUND_CAPACITY = 7;
    
    static Bitmap[] mIconBackgroundBitmap = new Bitmap[INITIAL_ICON_BACKGROUND_CAPACITY];
    
    static Bitmap mThemeIconBackgroundBitmap = null;
    
    static Bitmap mKillBitmap = null;
    
    static Bitmap mIconEditBg = null;
    
    public TextView mPageClassification;
    
    private ImageView mSwitch;

	private static boolean mIsInEditMode = false;

	private DatabaseHelper mTmpHelper;
    
    private static final String TABLE_TEMP = "temp";
    
    private static final int QVGA_WIDTH = 240;
    private static final int QVGA_HEIGHT = 320;
    private static boolean isQVGAMode = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(null);
        getWindow().setFormat(PixelFormat.OPAQUE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        super.onCreate(savedInstanceState);
        
        isQVGAMode = isSpecialWidthAndHeight(QVGA_WIDTH, QVGA_HEIGHT);
        mIsInEditMode = false;
        
    	mTmpHelper = new DatabaseHelper(this);
        
        mCurrentStyle = getSharedPreferences(CURRENT_STYLE, 0).getString(CURRENT_STYLE, defStyle);
                 
        mIsDefStyle = (defStyle.equalsIgnoreCase(mCurrentStyle));
        
        if ( !mIsDefStyle ) {
			mCurrentTheme = getSharedPreferences(CURRENT_SCENE, 0).getString(CURRENT_SCENE, shortcutTheme);
			
			mIsShortcutWorkspace = (shortcutTheme.equalsIgnoreCase(mCurrentTheme));
        }
        
        mCurrentSkin = getSharedPreferences(CURRENT_SKIN, 0).getString(CURRENT_SKIN, Configuration.SKIN_UNDEFINED);
        
        Resources res = getResources();
		pageClassification = res.getStringArray(R.array.classification);
		
		LauncherApplication app = ((LauncherApplication) getApplication());
        mModel = app.setLauncher(this);
        mModel.setLauncher(this);
        mIconCache = app.getIconCache();
        mDragController = new DragController(this);
        mInflater = getLayoutInflater();

        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetHost.setLauncher(this);
        mAppWidgetHost.startListening();

        if (PROFILE_STARTUP) {
            android.os.Debug.startMethodTracing("/sdcard/launcher");
        }

        checkForLocaleChange();
        setWallpaperDimension();

        setContentView(R.layout.launcher);
        showHomeLoadingDialog();
        setupViews();

        registerWallpaperReceivers();
        registerContentObservers();

        lockAllApps();
        
        initialIconBackgroundBitmap();

        mSavedState = savedInstanceState;
        restoreState(mSavedState);
        
        showTopLayer( isShortcutScreen());

        if (PROFILE_STARTUP) {
            android.os.Debug.stopMethodTracing();
        }

        if (!mRestoring) {
            mModel.setAllAppsDirty();
            mModel.startLoader(this, true);
        }

        // For handling default keys
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);

        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mCloseSystemDialogsReceiver, filter);  
    }
    
    private void checkForLocaleChange() {
        final LocaleConfiguration localeConfiguration = new LocaleConfiguration();
        readConfiguration(this, localeConfiguration);

        final Configuration configuration = getResources().getConfiguration();

        mPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT ? true : false;

        final String previousLocale = localeConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = localeConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = localeConfiguration.mnc;
        final int mnc = configuration.mnc;

        mLocaleChanged = !locale.equals(previousLocale) || mcc != previousMcc
                || mnc != previousMnc;

        if (mLocaleChanged) {
            localeConfiguration.locale = locale;
            localeConfiguration.mcc = mcc;
            localeConfiguration.mnc = mnc;

            writeConfiguration(this, localeConfiguration);
            mIconCache.flush();
        }

		final String previousSkin = readSkinSetting();
        final String skin = configuration.skin;
        boolean skinChanged = !skin.equalsIgnoreCase(previousSkin);
        mCurrentSkin = skin;
        
        if (skinChanged) {
        	writeSkinSetting(this, skin);
        	mIconCache.flush();
        }
    }

	private String readSkinSetting() {
    	String skin = Configuration.SKIN_UNDEFINED;
    	skin = getSharedPreferences(CURRENT_SKIN, 0).getString(CURRENT_SKIN, null);
        return skin;
    }
    
    private void writeSkinSetting(Context context, String skin) {
        SharedPreferences settings = context.getSharedPreferences(CURRENT_SKIN, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(CURRENT_SKIN, skin);

        // Don't forget to commit your edits!!!
        editor.commit();
    }
    
    public static boolean isDefaultSkin() {
    	return mCurrentSkin.equals(Configuration.SKIN_UNDEFINED);
    }

    private static class LocaleConfiguration {
        public String locale;

        public int mcc = -1;

        public int mnc = -1;
    }

    public boolean localeChanged() {
        return mLocaleChanged;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // Ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // noinspection ResultOfMethodCallIgnored
            context.getFileStreamPath(PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    static int getScreen() {
        synchronized (sLock) {
            return sScreen;
        }
    }

    static void setScreen(int screen) {
        synchronized (sLock) {
            sScreen = screen;
        }
    }

    private void setWallpaperDimension() {
        WallpaperManager wpm = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);

        Display display = getWindowManager().getDefaultDisplay();
        boolean isPortrait = display.getWidth() < display.getHeight();

        final int width = isPortrait ? display.getWidth() : display.getHeight();
        final int height = isPortrait ? display.getHeight() : display.getWidth();
        wpm.suggestDesiredDimensions(width * WALLPAPER_SCREENS_SPAN, height);
    }

    /**
     * Wallpaper intent receiver for proper trackicng of wallpaper changes
     */
    private static class WallpaperIntentReceiver extends BroadcastReceiver {
        private WeakReference<Launcher> mLauncher;
        WallpaperIntentReceiver(Application application, Launcher launcher) {
            setLauncher(launcher);
        }

        void setLauncher(Launcher launcher) {
            mLauncher = new WeakReference<Launcher>(launcher);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mLauncher != null) {
                final Launcher launcher = mLauncher.get();
                if (launcher != null) {
                    final Workspace workspace = launcher.getWorkspace();
                    if(workspace != null) {
                        workspace.setWallpaper(true);
                    }
                }
            }
        }
    }

    public void setWindowBackground(boolean liveWallpaper) {
        if (!liveWallpaper) {
            getWindow().setBackgroundDrawable(null);
            getWindow().setFormat(PixelFormat.OPAQUE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        } else {
            getWindow().setBackgroundDrawable(new ColorDrawable(0));
            getWindow().setFormat(PixelFormat.TRANSPARENT);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mWaitingForResult = false;

        // The pattern used here is that a user PICKs a specific application,
        // which, depending on the target, might need to CREATE the actual
        // target.

        // For example, the user would PICK_SHORTCUT for "Music playlist", and
        // we
        // launch over to the Music app to actually CREATE_SHORTCUT.

        if (resultCode == RESULT_OK && mAddItemCellInfo != null) {
            switch (requestCode) {
                case REQUEST_PICK_APPLICATION:
                    completeAddApplication(this, data, mWorkspace.getCurrentScreen());
                    break;
                case REQUEST_PICK_SHORTCUT:
                    processShortcut(data);
                    break;
                case REQUEST_CREATE_SHORTCUT:
                    completeAddShortcut(data, mAddItemCellInfo);
                    break;
                case REQUEST_PICK_LIVE_FOLDER:
                    addLiveFolder(data);
                    break;
                case REQUEST_CREATE_LIVE_FOLDER:
                    completeAddLiveFolder(data, mAddItemCellInfo);
                    break;
                case REQUEST_PICK_APPWIDGET:
                    addAppWidget(data);
                    break;
                case REQUEST_CREATE_APPWIDGET:
                    completeAddAppWidget(data, mAddItemCellInfo);
                    break;
                case REQUEST_PICK_WALLPAPER:
                    // We just wanted the activity result here so we can clear
                    // mWaitingForResult
                    break;
                case REQUEST_SET_THEME:
                    break;
            }
        } else if ((requestCode == REQUEST_PICK_APPWIDGET || requestCode == REQUEST_CREATE_APPWIDGET)
                && resultCode == RESULT_CANCELED && data != null) {
            // Clean up the appWidgetId if we canceled
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

		View hostView = mWorkspace.getChildAt(mWorkspace.getCurrentScreen());
		View mtkWidgetView = mWorkspace.searchIMTKWidget(hostView);
		if (mtkWidgetView != null) {
		    ((IMTKWidget)mtkWidgetView).onResumeWhenShown(mWorkspace.getCurrentScreen());
		    if (DEBUG_SURFACEWIDGET) Log.e(TAG_SURFACEWIDGET, "onResumeWhenShown");
		}

        mPaused = false;

        mWorkspace.setWallpaper(false);
        if (mRestoring) {
            mWorkspaceLoading = true;
            mModel.startLoader(this, true);
            mRestoring = false;
        }

        if (!isAllAppsVisible()) {
            mWorkspace.notifyCurrentScreenEnter();
        }

		if (mIndicator != null) {
            //mIndicator.reset();
            if (mPreview != null && mPreview.getParent() != null) {
                WindowManagerImpl.getDefault().removeView(mPreview);
            }
            mIndicator.postInvalidate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPaused = true;

		View hostView = mWorkspace.getChildAt(mWorkspace.getCurrentScreen());
		    View mtkWidgetView = mWorkspace.searchIMTKWidget(hostView);
		    if (mtkWidgetView != null) {
			      ((IMTKWidget)mtkWidgetView).onPauseWhenShown(mWorkspace.getCurrentScreen());
			      if (DEBUG_SURFACEWIDGET) Log.e(TAG_SURFACEWIDGET, "onPauseWhenShown");
		    }

        if (mWorkspace != null) {
            mWorkspace.setCurrentScreen(getCurrentWorkspaceScreen());
        }

        mDragController.cancelDrag();
        if (!isAllAppsVisible()) {
            mWorkspace.notifyCurrentScreenLeave();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        // Flag the loader to stop early before switching
        mModel.stopLoader();
        mAllAppsGrid.surrender();
        return Boolean.TRUE;
    }

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);
        if (!handled && acceptFilter() && keyCode != KeyEvent.KEYCODE_ENTER) {
            boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb,
                    keyCode, event);
            if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
                // something usable has been typed - start a search
                // the typed text will be retrieved and cleared by
                // showSearchDialog()
                // If there are multiple keystrokes before the search dialog
                // takes focus,
                // onSearchRequested() will be called for every keystroke,
                // but it is idempotent, so it's fine.
                return onSearchRequested();
            }
        }

        // Eat the long press event so the keyboard doesn't come up.
        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            return true;
        }

        return handled;
    }

    private String getTypedText() {
        return mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    /**
     * Restores the previous state, if it exists.
     * 
     * @param savedState The previous state.
     */
    private void restoreState(Bundle savedState) {
        if (savedState == null) {
            return;
        }

        final boolean allApps = savedState.getBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, false);
        if (allApps) {
            showAllApps(false);
        }

        final int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN, -1);
        if (currentScreen > -1) {
            
            if(mLocaleChanged) {
                mWorkspace.setCurrentScreen(DEFAULT_SCREEN);
            } else { 
                mWorkspace.setCurrentScreen(currentScreen);
            }
            
        }

        final int addScreen = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);
        if (addScreen > -1) {
            mAddItemCellInfo = new CellLayout.CellInfo();
            final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
            addItemCellInfo.valid = true;
            addItemCellInfo.screen = addScreen;
            addItemCellInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
            addItemCellInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
            addItemCellInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
            addItemCellInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
            addItemCellInfo.findVacantCellsFromOccupied(savedState
                    .getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS), savedState
                    .getInt(RUNTIME_STATE_PENDING_ADD_COUNT_X), savedState
                    .getInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y));
            mRestoring = true;
        }

        boolean renameFolder = savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
        if (renameFolder) {
            long id = savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
            mFolderInfo = mModel.getFolderById(this, mFolders, id);
            mRestoring = true;
        }
    }

    private AllAppsView getAllAppsGrid(DragLayer dragLayer) {
        // Used for debug for LauncherPlus
        String allAppsGrid = SystemProperties.get("launcherplus.allappsgrid", "2d");
        ViewStub stub;
        if (allAppsGrid.equals("2d")) {
            stub = (ViewStub) dragLayer.findViewById(R.id.stub_all_apps_2d);
        } else if (allAppsGrid.equals("3d_11")) {
            stub = (ViewStub) dragLayer.findViewById(R.id.stub_all_apps_3d_11);
        } else if (allAppsGrid.equals("3d_20")) {
            stub = (ViewStub) dragLayer.findViewById(R.id.stub_all_apps_3d_20);
        } else {
            stub = (ViewStub) dragLayer.findViewById(R.id.all_apps_view_2d);
        }

        return (AllAppsView) stub.inflate();
    }

	private Workspace getWorkspace(DragLayer dragLayer) {
    	Workspace workspace;
    	mDefaultWorkspace = (Workspace)dragLayer.findViewById(R.id.default_workspace);

		mDefaultWorkspace.setOnLongClickListener(this); 
		mDefaultWorkspace.setDragController(mDragController);
		mDefaultWorkspace.setLauncher(this);
		mDefaultWorkspace.setDragLayer(dragLayer);
		
		mShortcutWorkspace = (Workspace) dragLayer.findViewById(R.id.shortcut_workspace);
		mWidgetWorkspace = (Workspace) dragLayer.findViewById(R.id.widget_workspace);
			
		mShortcutWorkspace.setOnLongClickListener(this);
		mShortcutWorkspace.setDragController(mDragController);
		mShortcutWorkspace.setLauncher(this);
		mShortcutWorkspace.setDragLayer(dragLayer);

		mWidgetWorkspace.setOnLongClickListener(this);
		mWidgetWorkspace.setDragController(mDragController);
		mWidgetWorkspace.setLauncher(this);
		mWidgetWorkspace.setDragLayer(dragLayer);
		
		if ( mIsDefStyle ) {
			mShortcutWorkspace.hideWorkspace();
			mWidgetWorkspace.hideWorkspace();
    		workspace = mDefaultWorkspace;
    	} else {
    		mDefaultWorkspace.hideWorkspace();
    		if (isShortcutScreen()) {
    			mWidgetWorkspace.hideWorkspace();
				workspace = mShortcutWorkspace;
			} else {
				mShortcutWorkspace.hideWorkspace();
				workspace = mWidgetWorkspace;
			}
    	}
		return workspace;
    }

    /**
     * Finds all the views we need and configure them properly.
     */
    private void setupViews() {
        DragController dragController = mDragController;

        DragLayer dragLayer = (DragLayer) findViewById(R.id.drag_layer);
        dragLayer.setDragController(dragController);
        mDragLayer = dragLayer;

        mAllAppsGrid = getAllAppsGrid(dragLayer);

        mAllAppsGrid.setLauncher(this);
        mAllAppsGrid.setDragController(dragController);
        ((View) mAllAppsGrid).setWillNotDraw(false); // We don't want a hole
        // punched in our window.
        // Manage focusability manually since this thing is always visible
        ((View) mAllAppsGrid).setFocusable(false);

        mWorkspace = getWorkspace(dragLayer);
        
        mIndicator = (Indicator) dragLayer.findViewById(R.id.indicator);
        mIndicator.setLaucher(this);
        mBottomLayer = (RelativeLayout) dragLayer.findViewById(R.id.bottom_layer);
        dragLayer.setBottomLayer(mBottomLayer);
        mBottomLayer.setOnLongClickListener(null);

        mPhoneShortcut = (ImageView) mBottomLayer.findViewById(R.id.phone); 
        mMessageShortcut = (ImageView) mBottomLayer.findViewById(R.id.message); 

        DeleteZone deleteZone = (DeleteZone) dragLayer.findViewById(R.id.delete_zone);
        mDeleteZone = deleteZone;

        mHandleView = (HandleView) findViewById(R.id.all_apps_button);
        mHandleView.setLauncher(this);
        mHandleView.setOnClickListener(this);
        
        mSwitchShortcutAndWidget = (ImageView)mBottomLayer.findViewById(R.id.switch_shortcut_widget);
        if (mIsShortcutWorkspace) {
			mSwitchShortcutAndWidget.setImageDrawable(getResources().getDrawable(R.drawable.apps_button));
		} else {
			mSwitchShortcutAndWidget.setImageDrawable(getResources().getDrawable(R.drawable.widget_button));
		}
        mSwitchShortcutAndWidget.setOnClickListener(this);
        
        showHandleViewButton( mIsDefStyle );      
        
        final Workspace workspace = mWorkspace;
        workspace.setHapticFeedbackEnabled(false);

        deleteZone.setLauncher(this);
        deleteZone.setDragController(dragController);
        RelativeLayout relativeLayout = (RelativeLayout) mBottomLayer.findViewById(R.id.relative_layout_bar); 
        deleteZone.setHandle(relativeLayout);

        dragController.setDragScoller(workspace);
        dragController.setDragListener(deleteZone);
        dragController.setScrollView(dragLayer);
        dragController.setMoveTarget(workspace);

        // The order here is bottom to top.
        dragController.addDropTarget(workspace);
        dragController.addDropTarget(deleteZone);
        
        mTopLayer = (RelativeLayout) dragLayer.findViewById(R.id.top_layer);
    	dragLayer.setTopLayer(mTopLayer);
    	//mTopLayer.setOnLongClickListener(null);
    	mSwitch = (ImageView)mTopLayer.findViewById(R.id.title_bg);
    	mPageClassification = (TextView)mTopLayer.findViewById(R.id.page_classification);
    }

    /**
     * Creates a view representing a shortcut.
     * 
     * @param info The data structure describing the shortcut.
     * @return A View inflated from R.layout.application.
     */
    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.application, (ViewGroup) mWorkspace.getChildAt(mWorkspace
                .getCurrentScreen()), info);
    }

    /**
     * Creates a view representing a shortcut inflated from the specified
     * resource.
     * 
     * @param layoutResId The id of the XML layout used to create the shortcut.
     * @param parent The group the shortcut belongs to.
     * @param info The data structure describing the shortcut.
     * @return A View inflated from layoutResId.
     */
    View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
    	TextView favorite = (TextView) mInflater.inflate(layoutResId, parent, false);    	
        
    	Bitmap icon = info.getIcon(mIconCache);
    	Bitmap iconEditMode = info.getIconInEditMode(mIconCache);
    	
    	favorite.setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mIsInEditMode ? iconEditMode : icon), null, null);
    	favorite.setText(info.title);
        favorite.setTag(info);
        favorite.setOnClickListener(this);
        if (mIsInEditMode && !info.isSystemApp()) {
        	adjustTextViewTopPaddingInEditMode(favorite, icon.getHeight(), true);
        }
        
        return favorite;        
    }
    
    private void clearIdleCell( CellLayout.CellInfo cellInfo ) {
    	Workspace workspace;
    	if ( mIsDefStyle ) {
    		workspace = mDefaultWorkspace;
    	} else {
    		workspace = mShortcutWorkspace;
    	}
    	for( int i = 0 ; i < CELL_COUNT - 1; i++ ) {
    		mIdleCell[i] = true;
    	}
    	
    	mIdleCell[CELL_COUNT -1 ] = false; //View More
    	
    	final CellLayout cellLayout = (CellLayout) workspace.getChildAt(cellInfo.screen);
    	int count = cellLayout.getChildCount();
    	
    	for( int i = 0; i < count; i++) {
    		View child = (View) cellLayout.getChildAt(i); 
    		if ( child instanceof Folder ) 
    			continue;
    		LayoutParams lp = (LayoutParams) child.getLayoutParams();
    		mIdleCell[ lp.cellY * Launcher.NUMBER_CELLS_Y + lp.cellX ] = false;
    	}
    }
    
    private FolderIcon findMoreFolder(String folderName, int screen ){
    	DropTarget target = mDragController.findDropTarget(folderName, screen);
    	
    	FolderIcon folderIcon = null;
		if ( target != null ) {
    		folderIcon = (FolderIcon)target; 
    	}
		
    	return folderIcon;
    }
    
    public boolean hasMoreFolder(int screen) {
    	final CellLayout cellLayout = (CellLayout) mShortcutWorkspace.getChildAt(screen);
    	int count = cellLayout.getChildCount();
    	
    	for( int i = 0; i < count; i++) {
    		View child = (View) cellLayout.getChildAt(i); 
    		if ( child.getTag() instanceof UserFolderInfo ) {
    			final UserFolderInfo userFolderInfo = (UserFolderInfo)child.getTag();
    			if ( userFolderInfo.title.toString().equalsIgnoreCase(Launcher.MORE_FOLDER))
    				return true;
    		}    			
    	}
    	
    	return false;
    }
    
    private boolean findIdleCell(CellLayout.CellInfo cellInfo){
    	clearIdleCell( cellInfo );
    	
    	for( int i = 0; i < CELL_COUNT; i++ ) {
    		if ( mIdleCell[i] ) {
    			cellInfo.cellY = i / Launcher.NUMBER_CELLS_Y ;
    			cellInfo.cellX = i - cellInfo.cellY * NUMBER_CELLS_Y;
    			cellInfo.valid = true;
    			return true;
    		}
    	}
    	return false;
    }
    
    CellLayout.CellInfo findIdleCell() {
    	CellLayout.CellInfo cellInfo = new CellInfo();
		cellInfo.screen = mWorkspace.getCurrentScreen();
		
    	clearIdleCell( cellInfo );
    	
    	for( int i = 0; i < CELL_COUNT; i++ ) {
    		if ( mIdleCell[i] ) {
    			cellInfo.cellY = i / Launcher.NUMBER_CELLS_Y ;
    			cellInfo.cellX = i - cellInfo.cellY * NUMBER_CELLS_Y;
    			cellInfo.valid = true;
    			return cellInfo;
    		}
    	}
    	return null;
    }
    
    void changeFolderName( UserFolderInfo userFolderInfo, String folderName) {
        if (!TextUtils.isEmpty(folderName)) {
        	mFolderInfo = userFolderInfo;
            // Make sure we have the right folder info
            mFolderInfo = mFolders.get(mFolderInfo.id);
            mFolderInfo.title = folderName;
            LauncherModel.updateItemInDatabase(Launcher.this, mFolderInfo);
 
            if (mWorkspaceLoading) {
                lockAllApps();
                mModel.startLoader(Launcher.this, false);
            } else {
                final FolderIcon folderIcon = (FolderIcon) mWorkspace.getViewForTag(mFolderInfo);
                if (folderIcon != null) {
                    folderIcon.setText(folderName);
                    getWorkspace().requestLayout();
                } else {
                    lockAllApps();
                    mWorkspaceLoading = true;
                    mModel.startLoader(Launcher.this, false);
                }
            }
        }
    }
    
    private boolean findCell(CellLayout.CellInfo cellInfo) {
    	if ( mIsDefStyle) {
    		return findSingleSlot(cellInfo);
    	} else {
    		return findIdleCell(cellInfo);
    	}
    }
    
	void completeAddApplication(Context context, Intent data, int screen) {
		Workspace workspace;
		if ( mIsDefStyle ) {
			workspace = mDefaultWorkspace;
		} else {
			workspace = mShortcutWorkspace;
		}
		FolderIcon moreFolder = null;
		CellLayout.CellInfo cellInfo = new CellInfo();
		cellInfo.screen = screen;
		if (!findCell(cellInfo)) {
			if ( !mIsDefStyle ) {
				if ((moreFolder=findMoreFolder(Launcher.MORE_FOLDER, cellInfo.screen)) == null) {
					return;
				} 
			} else {
				return;
			}
		}

		final ShortcutInfo info = mModel.getShortcutInfo(context.getPackageManager(), data, context);
		
		//This should be App workspace CellLayout
		CellLayout cellLayout = (CellLayout) workspace.getChildAt(cellInfo.screen);
		int count = cellLayout.getChildCount();

		if (info != null) {
			info.setActivity(data.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			info.container = ItemInfo.NO_ID;
			info.screen = cellInfo.screen;

			if (count == CELL_COUNT && moreFolder != null) {
				moreFolder.onDrop(null, 0, 0, 0, 0, null, info);
			} else {
				workspace.addApplicationShortcut(info, cellInfo, isWorkspaceLocked());
			}
		} else {
			Log.e(TAG, "Couldn't find ActivityInfo for selected application: "
					+ data);
		}
	}
    
    private void changeFolderName() {
		// TODO Auto-generated method stub
		
	}

	/**
     * Add an application shortcut to the workspace.
     * 
     * @param data The intent describing the application.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    void completeAddApplication(Context context, Intent data, CellLayout.CellInfo cellInfo) {
    	Workspace workspace;
		if ( mIsDefStyle ) {
			workspace = mDefaultWorkspace;
		} else {
			workspace = mShortcutWorkspace;
		}
        cellInfo.screen = workspace.getCurrentScreen();
        if (!findCell(cellInfo))
            return;

        final ShortcutInfo info = mModel
                .getShortcutInfo(context.getPackageManager(), data, context);

        if (info != null) {
            info.setActivity(data.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            info.container = ItemInfo.NO_ID;
            workspace.addApplicationShortcut(info, cellInfo, isWorkspaceLocked());
        } else {
            Log.e(TAG, "Couldn't find ActivityInfo for selected application: " + data);
        }
    }

    /**
     * Add a shortcut to the workspace.
     * 
     * @param data The intent describing the shortcut.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    private void completeAddShortcut(Intent data, CellLayout.CellInfo cellInfo) {
    	Workspace workspace;
		if ( mIsDefStyle ) {
			workspace = mDefaultWorkspace;
		} else {
			workspace = mShortcutWorkspace;
		}
        cellInfo.screen = workspace.getCurrentScreen();
        if (!findCell(cellInfo))
            return;
        
		final ShortcutInfo info = mModel.addShortcut(this, data, cellInfo, false);

		if (!mRestoring ) {
			final View view = createShortcut(info);
			workspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY,
					1, 1, isWorkspaceLocked());
		}
    }

    /**
     * Add a widget to the workspace.
     * 
     * @param data The intent describing the appWidgetId.
     * @param cellInfo The position on screen where to create the widget.
     */
    private void completeAddAppWidget(Intent data, CellLayout.CellInfo cellInfo) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        if (LOGD)
            Log.d(TAG, "dumping extras content=" + extras.toString());

        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        // Calculate the grid spans needed to fit this widget
        CellLayout layout = (CellLayout) mWorkspace.getChildAt(cellInfo.screen);
        int width = 0;
        int height = 0;
        int[] spans;
        if (data.hasCategory("MTK_settingsWidget")) {
            width = (Integer) data.getExtra("width_cell");
            height = (Integer) data.getExtra("height_cell");
            // spans = layout.rectToCell(width, height);
            spans = new int[] {
                    width, height
            };
        } else {
            spans = layout.rectToCell(appWidgetInfo.minWidth, appWidgetInfo.minHeight);
        }

        // Try finding open space on Launcher screen
        final int[] xy = mCellCoordinates;
        if (!findSlot(cellInfo, xy, spans[0], spans[1])) {
            Log.e(TAG, "No Room for widget ................");
            handleNoRoomForWidget(spans, appWidgetId);
            return;
        }

        // Build Launcher-specific widget info and save to database
        LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId);
        launcherInfo.spanX = spans[0];
        launcherInfo.spanY = spans[1];

        LauncherModel.addItemToDatabase(this, launcherInfo,
                LauncherSettings.Favorites.CONTAINER_DESKTOP, mWorkspace.getCurrentScreen(), xy[0],
                xy[1], false);

        if (!mRestoring) {
            mWidgetDesktopItems.add(launcherInfo);
            mModel.addDesktopAppWidget(launcherInfo);

            // Perform actual inflation because we're live
            launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);

            launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            launcherInfo.hostView.setTag(launcherInfo);

            mWorkspace.addInCurrentScreen(launcherInfo.hostView, xy[0], xy[1], launcherInfo.spanX,
                    launcherInfo.spanY, isWorkspaceLocked());
	}

	View mtkWidgetView = mWorkspace.searchIMTKWidget(launcherInfo.hostView);
        if ( mtkWidgetView != null) {
        	((IMTKWidget)mtkWidgetView).setScreen(mWorkspace.getCurrentScreen());
        	((IMTKWidget)mtkWidgetView).setWidgetId(appWidgetId);
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
        mWidgetDesktopItems.remove(launcherInfo);
        launcherInfo.hostView = null;
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    void closeSystemDialogs() {
        getWindow().closeAllPanels();

        try {
            dismissDialog(DIALOG_CREATE_SHORTCUT);
            // Unlock the workspace if the dialog was showing
        } catch (Exception e) {
            // An exception is thrown if the dialog is not visible, which is
            // fine
        }

        try {
            dismissDialog(DIALOG_RENAME_FOLDER);
            // Unlock the workspace if the dialog was showing
        } catch (Exception e) {
            // An exception is thrown if the dialog is not visible, which is
            // fine
        }

        // Whatever we were doing is hereby canceled.
        mWaitingForResult = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            // also will cancel mWaitingForResult.
            closeSystemDialogs();

            boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            boolean allAppsVisible = isAllAppsVisible();
            if (!mWorkspace.isDefaultScreenShowing()) {
                final boolean animate = alreadyOnHome && !allAppsVisible;
				mWorkspace.controlMTKWidget( mWorkspace.getCurrentScreen() );
                mWorkspace.moveToDefaultScreen(animate);
                if (!animate) {
                    mIndicator.setPosition(mWorkspace.getScrollX());
                }
            }
            closeAllApps(alreadyOnHome && allAppsVisible);

            final View v = getWindow().peekDecorView();
            if (v != null && v.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    public void updateIndicatorPosition(){
    	mIndicator.setPosition(mWorkspace.getScrollX());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Do not call super here
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(RUNTIME_STATE_CURRENT_SCREEN, mWorkspace.getCurrentScreen());

        final ArrayList<Folder> folders = mWorkspace.getOpenFolders();
        if (folders.size() > 0) {
            final int count = folders.size();
            long[] ids = new long[count];
            for (int i = 0; i < count; i++) {
                final FolderInfo info = folders.get(i).getInfo();
                ids[i] = info.id;
            }
            outState.putLongArray(RUNTIME_STATE_USER_FOLDERS, ids);
        } else {
            super.onSaveInstanceState(outState);
        }

        // TODO should not do this if the drawer is currently closing.
        if (isAllAppsVisible()) {
            outState.putBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, true);
        }

        if (mAddItemCellInfo != null && mAddItemCellInfo.valid && mWaitingForResult) {
            final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
            final CellLayout layout = (CellLayout) mWorkspace.getChildAt(addItemCellInfo.screen);

            outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN, addItemCellInfo.screen);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, addItemCellInfo.cellX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, addItemCellInfo.cellY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, addItemCellInfo.spanX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, addItemCellInfo.spanY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_X, layout.getCountX());
            outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y, layout.getCountY());
            outState.putBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS, layout
                    .getOccupiedCells());
        }

        if (mFolderInfo != null && mWaitingForResult) {
            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID, mFolderInfo.id);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }

        TextKeyListener.getInstance().release();

        mModel.unbind();
        mModel.stopLoader();

        unbindShortcutDesktopItems();
        unbindWidgetDesktopItems();
        unbindAppNotInDesktopItems();

        getContentResolver().unregisterContentObserver(mWidgetObserver);

        unregisterReceiver(mCloseSystemDialogsReceiver);
        if (sWallpaperReceiver != null) {
            final Application application = getApplication();
            application.unregisterReceiver(sWallpaperReceiver);
            sWallpaperReceiver = null;
        }

        System.gc();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0)
            mWaitingForResult = true;
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
            boolean globalSearch) {

        closeAllApps(true);

        if (initialQuery == null) {
            // Use any text typed in the launcher as the initial query
            initialQuery = getTypedText();
            clearTypedText();
        }
        if (appSearchData == null) {
            appSearchData = new Bundle();
            appSearchData.putString(Search.SOURCE, "launcher-search");
        }

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchManager.startSearch(initialQuery, selectInitialQuery, getComponentName(),
                appSearchData, globalSearch);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isWorkspaceLocked()) {
            return false;
        }

        super.onCreateOptionsMenu(menu);

        menu.add(MENU_GROUP_ADD, MENU_ADD, 0, R.string.menu_add).setIcon(
                android.R.drawable.ic_menu_add).setAlphabeticShortcut('A');
        menu.add(MENU_GROUP_WALLPAPER, MENU_WALLPAPER_SETTINGS, 0, R.string.menu_wallpaper)
                .setIcon(android.R.drawable.ic_menu_gallery).setAlphabeticShortcut('W');
        menu.add(MENU_GROUP_SEARCH, MENU_SEARCH, 0, R.string.menu_search).setIcon(
                android.R.drawable.ic_search_category_default).setAlphabeticShortcut(
                SearchManager.MENU_KEY);
        menu.add(MENU_GROUP_ENTER_EDIT, MENU_EDIT, 0, R.string.menu_edit).setIcon(
                R.drawable.ic_menu_edit).setAlphabeticShortcut('E');
        menu.add(MENU_GROUP_NOTIFICATIONS, MENU_NOTIFICATIONS, 0, R.string.menu_notifications).setIcon(
        		com.android.internal.R.drawable.ic_menu_notifications).setAlphabeticShortcut('N');
        
        menu.add(MENU_GROUP_EDIT, MENU_EDIT_SAVE, 0, R.string.menu_edit_save).setIcon(
                R.drawable.ic_menu_save).setAlphabeticShortcut('S');
        
        menu.add(MENU_GROUP_EDIT, MENU_EDIT_CANCEL, 0, R.string.menu_edit_cancel).setIcon(
                R.drawable.ic_menu_close_clear_cancel).setAlphabeticShortcut('C');
        
        final Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        menu.add(MENU_GROUP_SETTINGS, MENU_SETTINGS, 0, R.string.menu_settings).setIcon(
                android.R.drawable.ic_menu_preferences).setAlphabeticShortcut('P').setIntent(settings);
        
        menu.add(MENU_GROUP_SWITCH_STYLE, MENU_SWITCH_STYLE, 0, R.string.menu_switch_style).setIcon(
                R.drawable.ic_menu_switch_style).setAlphabeticShortcut('S');
        
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If all apps is animating, don't show the menu, because we don't know
        // which one to show.
        if (mAllAppsGrid.isVisible() && !mAllAppsGrid.isOpaque()) {
            return false;
        }

        if (mDragController.isDraging()) {
            return false;
        }

        // Only show the add and wallpaper options when we're not in all apps.
        boolean visible = !mAllAppsGrid.isOpaque();
        menu.setGroupVisible(MENU_GROUP_ADD, visible);
        menu.setGroupVisible(MENU_GROUP_WALLPAPER, visible);
        menu.setGroupVisible(MENU_GROUP_SWITCH_STYLE, visible);
        
        if (isShortcutScreen()) {
        	menu.setGroupVisible(MENU_GROUP_NOTIFICATIONS, false);
        	
        	boolean editMode = mIsInEditMode;
			menu.setGroupVisible(MENU_GROUP_ADD, !editMode);
			menu.setGroupVisible(MENU_GROUP_WALLPAPER, !editMode);
			menu.setGroupVisible(MENU_GROUP_SWITCH_STYLE, !editMode);
			menu.setGroupVisible(MENU_GROUP_SEARCH, !editMode);
			menu.setGroupVisible(MENU_GROUP_SETTINGS, !editMode);
			menu.setGroupVisible(MENU_GROUP_ENTER_EDIT, !editMode);
			menu.setGroupVisible(MENU_GROUP_EDIT, editMode);
        } else {
        	menu.setGroupVisible(MENU_GROUP_ENTER_EDIT, false);
        	menu.setGroupVisible(MENU_GROUP_EDIT, false);
        	menu.setGroupVisible(MENU_GROUP_NOTIFICATIONS, true);
        }

        // Disable add if the workspace is full.
        if (visible) {
        	if ( isShortcutScreen() ) {
        		mMenuAddInfo = findIdleCell();
        	} else {
        		mMenuAddInfo = mWorkspace.findAllVacantCells(null);
        	}
            menu.setGroupEnabled(MENU_GROUP_ADD, mMenuAddInfo != null && mMenuAddInfo.valid);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD:
                addItems();
                return true;
            case MENU_WALLPAPER_SETTINGS:
                startWallpaper();
                return true;
            case MENU_SEARCH:
                onSearchRequested();
                return true;
            case MENU_NOTIFICATIONS:
                showNotifications();
                return true;
            case MENU_EDIT:
            	enterEditMode();
            	return true;
            case MENU_SWITCH_STYLE:
            	clearAndSwitchStyle();
            	return true;
            case MENU_EDIT_SAVE:
            	saveEditOperations();
            	return true;
            case MENU_EDIT_CANCEL:
            	cancelEditOperations();
            	return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private void enterEditMode() {
    	mIsInEditMode = true;
		changeIconToEditModeOrNot(true);
		new Thread("createTempTable") {
    		public void run() {
    			createTempTable();
    		}
    	}.start();			
    }
    
    private void exitEditMode() {
    	mIsInEditMode = false;
    	changeIconToEditModeOrNot(false);
    }
    
    private void changeIconToEditModeOrNot(boolean isInEditMode) {
    	int count = mShortcutWorkspace.getChildCount();
		for (int i = 0; i < count; i++) {
			final CellLayout cellLayout = (CellLayout) mShortcutWorkspace.getChildAt(i);
			int childCount = cellLayout.getChildCount();
			for (int j = 0; j < childCount; j++) {
				View child = (View) cellLayout.getChildAt(j);
				Object tag = child.getTag();
				if (tag instanceof ShortcutInfo) {
					ShortcutInfo shortcutInfo = (ShortcutInfo) tag;
					Bitmap icon = shortcutInfo.getIcon(mIconCache);
					Bitmap iconEditMode = shortcutInfo.getIconInEditMode(mIconCache);
					((TextView)child).setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(isInEditMode ? iconEditMode : icon), null, null);
					if (!shortcutInfo.isSystemApp()) {
						adjustTextViewTopPaddingInEditMode((TextView)child, icon.getHeight(), isInEditMode);
					}
				} else if (tag instanceof UserFolderInfo) {
					FolderIcon folderIcon = (FolderIcon)child;
					Bitmap icon = folderIcon.getIconBitmap();
					Bitmap iconEditMode = folderIcon.getIconEditBitmap();		
					folderIcon.setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(isInEditMode ? iconEditMode : icon), null, null);
					adjustTextViewTopPaddingInEditMode((TextView)child, icon.getHeight(), isInEditMode);					
				} else if(tag instanceof LiveFolderInfo) {
					LiveFolderIcon liveFolderIcon = (LiveFolderIcon)child;
					Bitmap icon = liveFolderIcon.getIconBitmap();
					Bitmap iconEditMode = liveFolderIcon.getIconEditBitmap();
					liveFolderIcon.setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(isInEditMode ? iconEditMode : icon), null, null);
					//adjustTextViewTopPaddingInEditMode((TextView)child, icon.getHeight(), isInEditMode, true);
				}
			}
		}
		mShortcutWorkspace.invalidate();
    }
    
    private void adjustTextViewTopPaddingInEditMode(TextView view, int iconHeight, boolean isInEditMode) {
    	if (isInEditMode) {
    		view.setPadding(view.getPaddingLeft(), view.getPaddingTop()-mKillBitmap.getHeight()/2, view.getPaddingRight(), view.getPaddingBottom());
    		view.setCompoundDrawablePadding(view.getCompoundDrawablePadding()-mKillBitmap.getHeight()/2+3);
    	} else {
    		view.setPadding(view.getPaddingLeft(), view.getPaddingTop()+mKillBitmap.getHeight()/2, view.getPaddingRight(), view.getPaddingBottom());
    		view.setCompoundDrawablePadding(view.getCompoundDrawablePadding()+mKillBitmap.getHeight()/2-3);
    	}
    }
    
    public void saveEditOperations() {
    	mIsInEditMode = false;
    	exitEditMode();
    	new Thread("dropTempTable") {
    		public void run() {
    	    	dropTempTable();
    		}
    	}.start();
    }
    
    public void cancelEditOperations() {
    	mIsInEditMode = false;
    	exitEditMode();    	
    	restoreFavorites();
    	new Thread("dropTempTable") {
    		public void run() {
    	    	dropTempTable();
    		}
    	}.start();
    	
    	mDragController.removeAllDropTargets();
    	mDragController.addDropTarget(mWorkspace);
    	mDragController.addDropTarget(mDeleteZone);
    	
    	mWorkspaceLoading = true;
    	mModel.startLoader(this, true);
        mRestoring = false;
    }
    
    private void createTempTable() {
    	SQLiteDatabase db = mTmpHelper.getWritableDatabase();
    	String sql = "CREATE TABLE " + TABLE_TEMP + " (" +
        "_id INTEGER PRIMARY KEY," +
        "title TEXT," +
        "intent TEXT," +
        "container INTEGER," +
        "screen INTEGER," +
        "cellX INTEGER," +
        "cellY INTEGER," +
        "itemType INTEGER," +
        "originalID INTEGER" +
        ");";
    	Log.e(TAG, "Create Temp DB");
    	try {
    		db.execSQL("DROP TABLE IF EXISTS temp");
    		db.execSQL(sql);
    	} catch(SQLException e) {
    		Log.e(TAG, "Create Temp DB Failed");
    	}
    		
    }
    
    private void restoreFavorites() {
    	final ContentResolver cr = getContentResolver();
    	Cursor c = cr.query(LauncherSettings.Favorites.TEMP_CONTENT_URI, null, null, null, null);
    	ArrayList<Long> needToDelete = new ArrayList<Long>(1);
    	try {
			final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
			final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
			final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
			final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
			final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
			final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ORIGINAL_ID);
			final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);

			Intent intent;
			long container;
			int screen;
			int cellX;
			int cellY;
			int originalId;
			int itemType;
			
			c.moveToLast();
			
			do {
				container = c.getLong(containerIndex);
				screen = c.getInt(screenIndex);
				cellX = c.getInt(cellXIndex);
				cellY = c.getInt(cellYIndex);
				originalId = c.getInt(idIndex);
				itemType = c.getInt(itemTypeIndex);
				Log.e(TAG, "id= " + c.getInt(c.getColumnIndexOrThrow(Favorites._ID)));

				if (itemType == LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER || 
						itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER) {
					cr.delete(Favorites.getContentUri(originalId, false), null, null);
				} else {
					ContentValues values = new ContentValues();
					values.put(LauncherSettings.Favorites.CONTAINER, container);
					values.put(LauncherSettings.Favorites.SCREEN, screen);
					values.put(LauncherSettings.Favorites.CELLX, cellX);
					values.put(LauncherSettings.Favorites.CELLY, cellY);
				
					cr.update(Favorites.getContentUri(originalId, false), values, null, null);
				}
			} while (c.moveToPrevious());
    	} catch (Exception e) {
            return;
        } finally {
            c.close();
        }
    }
    
    private void dropTempTable() {
    	SQLiteDatabase db = mTmpHelper.getWritableDatabase();
    	String sql="drop table " + TABLE_TEMP;
    	try {
    		db.execSQL(sql);
    		Log.e(TAG, "Drop temp table success");
    	} catch (SQLException e) {
    		Log.e(TAG, "Drop temp table failed");
    	}
    }
    
    public static boolean isInEditMode() {
    	return mIsInEditMode;
    }

    /**
     * Indicates that we want global search for this activity by setting the
     * globalSearch argument for {@link #startSearch} to true.
     */

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, true);
        return true;
    }

    public boolean isWorkspaceLocked() {
        return mWorkspaceLoading || mWaitingForResult || isOnAppWidgetAnimation();
    }

	private boolean isOnAppWidgetAnimation() {
		if (mInAnimation == null || mOutAnimation == null)
			return false;
		boolean onInAni = mInAnimation.hasStarted() && !mInAnimation.hasEnded();
		boolean onOutAni = mOutAnimation.hasStarted() && !mOutAnimation.hasEnded();
		if(DEBUG_USER_INTERFACE) 
			Log.w(TAG, "isOnAppWidgetAnimation() = "+(onOutAni || onInAni));
		return onOutAni || onInAni;
	}

    private void addItems() {
        closeAllApps(true);
        showAddDialog(mMenuAddInfo);
    }

    void addAppWidget(Intent data) {
        // TODO: catch bad widget exception when sent
        int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidget = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        if (appWidget.provider.getClassName().contains(VIDEO_WIDGET) && mWorkspace.searchIMTKWidget(mWorkspace) != null) {
            if (mAppWidgetHost != null) {
            	mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            	Toast.makeText(this, R.string.one_video_widget, Toast.LENGTH_LONG).show();
            	return;
            }
        }
        
        if (appWidget.configure != null) {
            // Launch over to configure widget, if needed
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidget.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            // Otherwise just add it
            onActivityResult(REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK, data);
        }
    }

    void processShortcut(Intent intent) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            startActivityForResult(pickIntent, REQUEST_PICK_APPLICATION);
        } else {
            startActivityForResult(intent, REQUEST_CREATE_SHORTCUT);
        }
    }

    void addLiveFolder(Intent intent) {
    	Workspace workspace;
    	if ( mIsDefStyle ) {
    		workspace = mDefaultWorkspace;
    	} else {
    		workspace = mShortcutWorkspace;
    	}
        // Handle case where user selected "Folder"
        String folderName = getResources().getString(R.string.group_folder);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        int screen = intent.getIntExtra("screen", workspace.getCurrentScreen());
        if (folderName != null && folderName.equals(shortcutName)) {
            addFolder(screen);
        } else {
            startActivityForResult(intent, REQUEST_CREATE_LIVE_FOLDER);
        }
    }

    FolderIcon addFolder(int screen, int cellX, int cellY ) {
    	Workspace workspace;
    	if ( mIsDefStyle ) {
    		workspace = mDefaultWorkspace;
    	} else {
    		workspace = mShortcutWorkspace;
    	}
        UserFolderInfo folderInfo = new UserFolderInfo();
        
        CellLayout cellLayout = (CellLayout)workspace.getChildAt(screen);
        int count = cellLayout.getChildCount();
		if ( !mIsDefStyle && count == Launcher.CELL_COUNT - 1 && !hasMoreFolder(screen) ) {
			folderInfo.title = Launcher.MORE_FOLDER;
		} else {
			folderInfo.title = getText(R.string.folder_name);
		}
        

        // Update the model
        LauncherModel.addItemToDatabase(this, folderInfo,
                LauncherSettings.Favorites.CONTAINER_DESKTOP, screen,
                cellX, cellY, false);
        mFolders.put(folderInfo.id, folderInfo);

        // Create the view
        FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
                (ViewGroup) workspace.getChildAt(screen), folderInfo);
        
        workspace.addInScreen(newFolder, screen, cellX, cellY, 1, 1, isWorkspaceLocked());
        
        return newFolder;
    }
    
    FolderIcon addFolder(int screen) {
    	Workspace workspace;
    	if ( mIsDefStyle ) {
    		workspace = mDefaultWorkspace;
    	} else {
    		workspace = mShortcutWorkspace;
    	}
    	
        UserFolderInfo folderInfo = new UserFolderInfo();
        
        CellLayout.CellInfo cellInfo = new CellInfo();;
        cellInfo.screen = screen;
        if (!findCell(cellInfo))
            return null;
        
        CellLayout cellLayout = (CellLayout)workspace.getChildAt(screen);
        int count = cellLayout.getChildCount();
		if ( !mIsDefStyle && count == Launcher.CELL_COUNT - 1 && !hasMoreFolder(screen) ) {
			folderInfo.title = Launcher.MORE_FOLDER;
		} else {
			folderInfo.title = getText(R.string.folder_name);
		}

        // Update the model
        LauncherModel.addItemToDatabase(this, folderInfo,
                LauncherSettings.Favorites.CONTAINER_DESKTOP, screen,
                cellInfo.cellX, cellInfo.cellY, false);
        mFolders.put(folderInfo.id, folderInfo);

        // Create the view
        FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
                (ViewGroup) workspace.getChildAt(screen), folderInfo);
        
        workspace.addInScreen(newFolder, screen, cellInfo.cellX, cellInfo.cellY, 1, 1,
                isWorkspaceLocked());
        
        return newFolder;
    }

    void removeFolder(FolderInfo folder) {
        mFolders.remove(folder.id);
        LauncherModel.deleteItemFromDatabase(this, folder);
    }
    
    void removeFolder(long id) {
        mFolders.remove(id);
        LauncherModel.deleteItemFromDatabase(this, id);
    }

    private void completeAddLiveFolder(Intent data, CellLayout.CellInfo cellInfo) {
        cellInfo.screen = mWorkspace.getCurrentScreen();
        //if (!findSingleSlot(cellInfo))
        if ( !findCell(cellInfo))
            return;

        final LiveFolderInfo info = addLiveFolder(this, data, cellInfo, false);

        if (!mRestoring) {
            final View view = LiveFolderIcon.fromXml(R.layout.live_folder_icon, this,
                    (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), info);
            mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY, 1, 1,
                    isWorkspaceLocked());
        }
    }

    static LiveFolderInfo addLiveFolder(Context context, Intent data, CellLayout.CellInfo cellInfo,
            boolean notify) {

        Intent baseIntent = data.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT);
        String name = data.getStringExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME);

        Drawable icon = null;
        Intent.ShortcutIconResource iconResource = null;

        Parcelable extra = data.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON);
        if (extra != null && extra instanceof Intent.ShortcutIconResource) {
            try {
                iconResource = (Intent.ShortcutIconResource) extra;
                final PackageManager packageManager = context.getPackageManager();
                Resources resources = packageManager
                        .getResourcesForApplication(iconResource.packageName);
                final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                icon = resources.getDrawable(id);
            } catch (Exception e) {
                Log.w(TAG, "Could not load live folder icon: " + extra);
            }
        }

        if (icon == null) {
            icon = context.getResources().getDrawable(R.drawable.ic_launcher_folder);
        }

        final LiveFolderInfo info = new LiveFolderInfo();
        info.icon = Utilities.createIconBitmap(icon, context);
        info.title = name;
        info.iconResource = iconResource;
        info.uri = data.getData();
        info.baseIntent = baseIntent;
        info.displayMode = data.getIntExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
                LiveFolders.DISPLAY_MODE_GRID);

        LauncherModel.addItemToDatabase(context, info,
                LauncherSettings.Favorites.CONTAINER_DESKTOP, cellInfo.screen, cellInfo.cellX,
                cellInfo.cellY, notify);
        mFolders.put(info.id, info);

        return info;
    }

    private boolean findSingleSlot(CellLayout.CellInfo cellInfo) {
        final int[] xy = new int[2];
        if (findSlot(cellInfo, xy, 1, 1)) {
            cellInfo.cellX = xy[0];
            cellInfo.cellY = xy[1];
            return true;
        }
        return false;
    }

    private boolean findSlot(CellLayout.CellInfo cellInfo, int[] xy, int spanX, int spanY) {
        if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
            boolean[] occupied = mSavedState != null ? mSavedState
                    .getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS) : null;
            cellInfo = mWorkspace.findAllVacantCells(occupied);
            if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
                Toast.makeText(this, getString(R.string.out_of_space), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void showNotifications() {
        final StatusBarManager statusBar = (StatusBarManager) getSystemService(STATUS_BAR_SERVICE);
        if (statusBar != null) {
            statusBar.expand();
        }
    }

    private void startWallpaper() {
        closeAllApps(true);
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        pickWallpaper.putExtra("LauncherPlus", "true");
        Intent chooser = Intent.createChooser(pickWallpaper, getText(R.string.chooser_wallpaper));
        // NOTE: Adds a configure option to the chooser if the wallpaper
        // supports it
        // Removed in Eclair MR1
        // WallpaperManager wm = (WallpaperManager)
        // getSystemService(Context.WALLPAPER_SERVICE);
        // WallpaperInfo wi = wm.getWallpaperInfo();
        // if (wi != null && wi.getSettingsActivity() != null) {
        // LabeledIntent li = new LabeledIntent(getPackageName(),
        // R.string.configure_wallpaper, 0);
        // li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
        // chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
        // }
        startActivityForResult(chooser, REQUEST_PICK_WALLPAPER);
    }
    private void startVideoWallpaper() {
        mWorkspace.hideWallpaper(true);
        Intent intent = new Intent();
        ComponentName cpn = new ComponentName(VIDEO_LIVE_WALLPAPER_PKG, VIDEO_LIVE_WALLPAPER_CLS);
        intent.setComponent(cpn);
        startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
    }
    /**
     * Registers various intent receivers. The current implementation registers
     * only a wallpaper intent receiver to let other applications change the
     * wallpaper.
     */
    private void registerWallpaperReceivers() {
        if (sWallpaperReceiver == null) {
            final Application application = getApplication();
            sWallpaperReceiver = new WallpaperIntentReceiver(application, this);
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);
            application.registerReceiver(sWallpaperReceiver, intentFilter);
        } else {
            sWallpaperReceiver.setLauncher(this);
        }
    }

    /**
     * Registers various content observers. The current implementation registers
     * only a favorites observer to keep track of the favorites applications.
     */
    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI, true,
                mWidgetObserver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (SystemProperties.getInt("debug.launcherplus.dumpstate", 0) != 0) {
                        dumpState();
                        return true;
                    }
                    break;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
            }
        }

        if (!isAllAppsVisible()) {
            int keyCode = event.getKeyCode();
            if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode || KeyEvent.KEYCODE_DPAD_UP == keyCode) {
                return true;
            }

            if (event.getAction() == KeyEvent.ACTION_DOWN) { 
                if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode) {
                    mWorkspace.scrollLeft();
                    return true;
                } else if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode) { 
                    mWorkspace.scrollRight();
                    return true;
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
    	if (mIsInEditMode) {
        	if (mOpenFolder == null) {
				saveEditOperations();
			}
        }
    	
        if (isAllAppsVisible()) {
            closeAllApps(true);
        } else {
            closeFolder();
        }
    }

    public void closeFolder() {
		if (isFolderOpen()) {
			closeFolder(mOpenFolder);
			mOpenFolder = null;
		}
    }

    void closeFolder(Folder folder) {
        folder.getInfo().opened = false;
		mWaitingForResult = false;
        ViewGroup parent = (ViewGroup) folder.getParent();
        if (parent != null) {
            parent.removeView(folder);
            if (folder instanceof DropTarget) {
                // Live folders aren't DropTargets.
                mDragController.removeDropTarget((DropTarget) folder);
            }
            if(parent == mFolderOpenRelativeLayout){
            	parent.setVisibility(View.GONE);
            }
        }
        folder.onClose();
    }

    /**
     * Re-listen when widgets are reset.
     */
    private void onAppWidgetReset() {
        mAppWidgetHost.startListening();
    }

    /**
     * Go through the and disconnect any of the callbacks in the drawables and
     * the views or we leak the previous Home screen on orientation change.
     */
    private void unbindShortcutDesktopItems() {
        for (ItemInfo item : mShortcutDesktopItems) {
            item.unbind();
        }
    }
    
    private void unbindWidgetDesktopItems() {
        for (ItemInfo item : mWidgetDesktopItems) {
            item.unbind();
        }
    }
    
    private void unbindAppNotInDesktopItems() {
    	for (ItemInfo item : mAppNotInDesktopItems) {
            item.unbind();
        }
    }

    /**
     * Launches the intent referred by the clicked shortcut.
     * 
     * @param v The view representing the clicked shortcut.
     */
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
			ShortcutInfo shortcutInfo = (ShortcutInfo) tag;
			Intent intent = ((ShortcutInfo) tag).intent;
			if (ShortcutInfo.isMoreApp(shortcutInfo)) {
				int screenType = mWorkspace.getCurrentScreen() - 1;
				intent.putExtra("pageType", screenType);
			}
			int[] pos = new int[2];
			v.getLocationOnScreen(pos);
			if (mIsInEditMode) {
				if (!shortcutInfo.isSystemApp() && isClickOnKillRect(v)) {
					mDeleteZone.deleteShortcutDialog(mShortcutWorkspace, shortcutInfo);
				}				
			} else {
				intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0]
						+ v.getWidth(), pos[1] + v.getHeight()));
				startActivitySafely(intent, tag);
			}
		} else if (tag instanceof FolderInfo) {
			if (mIsInEditMode && tag instanceof UserFolderInfo	&& isClickOnKillRect(v)) {
				mDeleteZone.deleteFolderDialog(mShortcutWorkspace, (UserFolderInfo) tag);
			} else {
				handleFolderClick((FolderInfo) tag);
			}
		} else if (mIsDefStyle) {
			if (v == mHandleView) {
				if (isAllAppsVisible()) {
					closeAllApps(true);
				} else {
					showAllApps(true);
				}
			}
		} else {
			if (v == mSwitchShortcutAndWidget) {
				switchShortcutAndWidget();
			}
		}
    }
    
    public boolean isClickOnKillRect(View v) {
    	int[] pos = new int[2];
        v.getLocationOnScreen(pos);
        
    	int[] workspacePos = new int[2];
    	mShortcutWorkspace.getLocationOnScreen(workspacePos);
    	
    	int[] clickPos = new int[2];
    	clickPos[0] = mShortcutWorkspace.getX() + workspacePos[0];
    	clickPos[1] = mShortcutWorkspace.getY() + workspacePos[1];
    	
    	Rect killRect = new Rect(pos[0] + v.getWidth() - mKillBitmap.getWidth(), pos[1],
    			pos[0] + v.getWidth(), pos[1] + mKillBitmap.getHeight());
    	if (killRect.contains(clickPos[0],clickPos[1])) {
    		return true;
    	}
    	return false;
    }

    void startActivitySafely(Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
        	if (FeatureOption.MTK_VLW_APP) {
        	    String pkg = null;
        	    ComponentName cpn = intent.getComponent();
        	    if (cpn != null) {
        		    pkg = cpn.getPackageName();
        	    }
        	    if (VIDEO_LIVE_WALLPAPER_PKG.equals(pkg)) {
        		    startVideoWallpaper();
        	    } else {
        	        startActivity(intent);
        	    }
            } else {
        		startActivity(intent);
        	}
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent
                    + ". Make sure to create a MAIN intent-filter for the corresponding activity "
                    + "or use the exported attribute for this activity. " + "tag=" + tag
                    + " intent=" + intent, e);
        }
    }

    void startActivityForResultSafely(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent
                    + ". Make sure to create a MAIN intent-filter for the corresponding activity "
                    + "or use the exported attribute for this activity.", e);
        }
    }

    private void handleFolderClick(FolderInfo folderInfo) {
        if (!folderInfo.opened) {
            // Close any open folder
            closeFolder();
            // Open the requested folder
            openFolder(folderInfo);
        } else {
            // Find the open folder...
            Folder openFolder = mWorkspace.getFolderForTag(folderInfo);
            int folderScreen;
            if (openFolder != null) {
                folderScreen = mWorkspace.getScreenForView(openFolder);
                // .. and close it
                closeFolder(openFolder);
                if (folderScreen != mWorkspace.getCurrentScreen()) {
                    // Close any folder open on the current screen
                    closeFolder();
                    // Pull the folder onto this screen
                    openFolder(folderInfo);
                }
            }
        }
    }

	public boolean isFolderOpen() {
		return mOpenFolder != null;
	}

    /**
     * Opens the user fodler described by the specified tag. The opening of the
     * folder is animated relative to the specified View. If the View is null,
     * no animation is played.
     * 
     * @param folderInfo The FolderInfo describing the folder to open.
     */
    public void openFolder(FolderInfo folderInfo) {
        if (folderInfo instanceof UserFolderInfo) {
        	mOpenFolder = UserFolder.fromXml(this);
        } else if (folderInfo instanceof LiveFolderInfo) {
        	mOpenFolder = com.mediatek.launcherplus.LiveFolder.fromXml(this, folderInfo);
        } else {
            return;
        }

        mOpenFolder.setDragController(mDragController);
        mOpenFolder.setLauncher(this);

        mOpenFolder.bind(folderInfo);
        folderInfo.opened = true;
        mWaitingForResult = true;
        
        int keyHeight = ((CellLayout)mWorkspace.getChildAt(0)).getCellHeight()*NUMBER_CELLS_Y;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, keyHeight);
        if(mFolderOpenAnimation == null || mFolderOpenRelativeLayout == null){
        	initFolderLayout();
        }
        mFolderOpenRelativeLayout.addView(mOpenFolder, lp);
        mOpenFolder.setHapticFeedbackEnabled(false);
        mOpenFolder.setOnLongClickListener(this);
        mOpenFolder.onOpen();
        mFolderOpenRelativeLayout.setVisibility(View.VISIBLE);
        mFolderOpenRelativeLayout.startAnimation(mFolderOpenAnimation);
    }

    private void initFolderLayout(){
    	mFolderOpenRelativeLayout = (RelativeLayout)findViewById(R.id.folder_topen);
    	mFolderOpenAnimation = AnimationUtils.loadAnimation(this, R.anim.folder_open);
    }

    public boolean onLongClick(View v) {
        if (isWorkspaceLocked()) {
            return false;
        }

        if (!(v instanceof CellLayout)) {
            v = (View) v.getParent();
        }

        CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) v.getTag();

        // This happens when long clicking an item with the dpad/trackball
        if (cellInfo == null) {
            return true;
        }

        if (mWorkspace.allowLongPress()) {
            if (cellInfo.cell == null) {
            	if (!mIsInEditMode) {
            		if (cellInfo.valid) {
            			// User long pressed on empty space
            			mWorkspace.setAllowLongPress(false);
            			mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
            					HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
            			removeDialog(DIALOG_CREATE_SHORTCUT);
            			showAddDialog(cellInfo);
            		}
            	}
            } else {
                if (!(cellInfo.cell instanceof Folder)) {
                    // User long pressed on an item
                	View mtkWidgetView = mWorkspace.searchIMTKWidget(cellInfo.cell);
                	if (mtkWidgetView != null) {
                		((IMTKWidget)mtkWidgetView).startDrag();
            			if (DEBUG_SURFACEWIDGET) Log.e(TAG_SURFACEWIDGET, "startDrag");
            		}
                	if (cellInfo.cell.getTag() instanceof ShortcutInfo) {
                		ShortcutInfo shortcutInfo = (ShortcutInfo) cellInfo.cell.getTag();
                		if (ShortcutInfo.isMoreApp(shortcutInfo)) {
                			return true;
                		}
                    }
                	mWorkspace.startDrag(cellInfo);
                }
            }
        }

        return true;
    }

    Workspace getWorkspace() {
        return mWorkspace;
    }
    
    LauncherModel getModel() {
    	return mModel;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CREATE_SHORTCUT:
                return new CreateShortcut().createDialog();
            case DIALOG_RENAME_FOLDER:
                return new RenameFolder().createDialog();
        }

        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_CREATE_SHORTCUT:
                break;
            case DIALOG_RENAME_FOLDER:
                if (mFolderInfo != null) {
                    EditText input = (EditText) dialog.findViewById(R.id.folder_name);
                    final CharSequence text = mFolderInfo.title;
                    input.setText(text);
                    input.setSelection(0, text.length());
                    input.addTextChangedListener(new AddDialogTextWatcher(dialog));
                    ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(
                            input.length() == 0 ? false : true);
                }
                break;
        }
    }

    private class AddDialogTextWatcher implements TextWatcher {
        private Dialog dlg;

        AddDialogTextWatcher(Dialog dialog) {
            dlg = dialog;
        }

        public void afterTextChanged(Editable s) {
            ((AlertDialog) dlg).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(
                    s.length() == 0 ? false : true);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    void showRenameDialog(FolderInfo info) {
        mFolderInfo = info;
        mWaitingForResult = true;
        showDialog(DIALOG_RENAME_FOLDER);
    }

    private void showAddDialog(CellLayout.CellInfo cellInfo) {
        mAddItemCellInfo = cellInfo;
        mWaitingForResult = true;
        removeDialog(DIALOG_CREATE_SHORTCUT);
        showDialog(DIALOG_CREATE_SHORTCUT);
    }

    private void pickShortcut() {
        Bundle bundle = new Bundle();

        ArrayList<String> shortcutNames = new ArrayList<String>();
        shortcutNames.add(getString(R.string.group_applications));
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
        shortcutIcons.add(ShortcutIconResource.fromContext(Launcher.this,
                R.drawable.ic_launcher_application));
        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.title_select_shortcut));
        pickIntent.putExtras(bundle);

        startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
    }

    private class RenameFolder {
        private EditText mInput;

        Dialog createDialog() {
            final View layout = View.inflate(Launcher.this, R.layout.rename_folder, null);
            mInput = (EditText) layout.findViewById(R.id.folder_name);

            AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
            builder.setIcon(0);
            builder.setTitle(getString(R.string.rename_folder_title));
            builder.setCancelable(true);
            builder.setOnCancelListener(new Dialog.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    cleanup();
                }
            });
            builder.setNegativeButton(getString(R.string.cancel_action),
                    new Dialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            cleanup();
                        }
                    });
            builder.setPositiveButton(getString(R.string.rename_action),
                    new Dialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            changeFolderName();
                        }
                    });
            builder.setView(layout);

            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                public void onShow(DialogInterface dialog) {
                    mWaitingForResult = true;
                    mInput.requestFocus();
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(mInput, 0);
                }
            });

            return dialog;
        }

        private void changeFolderName() {
            final String name = mInput.getText().toString();
            if (!TextUtils.isEmpty(name)) {
                // Make sure we have the right folder info
                mFolderInfo = mFolders.get(mFolderInfo.id);
                mFolderInfo.title = name;
                LauncherModel.updateItemInDatabase(Launcher.this, mFolderInfo);

                if (mWorkspaceLoading) {
                    lockAllApps();
                    mModel.startLoader(Launcher.this, false);
                } else {
                    final FolderIcon folderIcon = (FolderIcon) mWorkspace
                            .getViewForTag(mFolderInfo);
                    if (folderIcon != null) {
                        folderIcon.setText(name);
                        getWorkspace().requestLayout();
                    } else {
                        lockAllApps();
                        mWorkspaceLoading = true;
                        mModel.startLoader(Launcher.this, false);
                    }
                }
            }
            cleanup();
        }

        private void cleanup() {
            dismissDialog(DIALOG_RENAME_FOLDER);
            mWaitingForResult = false;
            mFolderInfo = null;
        }
    }

    // Now a part of LauncherModel.Callbacks. Used to reorder loading steps.
    public boolean isAllAppsVisible() {
        return (mAllAppsGrid != null) ? mAllAppsGrid.isVisible() : false;
    }

    // AllAppsView.Watcher
    public void zoomed(float zoom) {
        if (zoom == 1.0f) {
            mWorkspace.setVisibility(View.GONE);
        }
    }

    private void showDragLayerElement(boolean isShow) {
    	int visibilityParam = isShow ? View.VISIBLE : View.GONE;
        mIndicator.setVisibility(visibilityParam);
        mBottomLayer.setVisibility(visibilityParam);
    }

    void showAllApps(boolean animated) {
		View hostView = mWorkspace.getChildAt(mWorkspace.getCurrentScreen());
    	View mtkWidgetView = mWorkspace.searchIMTKWidget(hostView);
    	if ( mtkWidgetView != null) {
    		((IMTKWidget)mtkWidgetView).startCovered(mWorkspace.getCurrentScreen());
    		if (DEBUG_SURFACEWIDGET) Log.e(TAG_SURFACEWIDGET, "startCovered");
    	}
        mWorkspace.hideWallpaper(true);
        mWorkspace.notifyCurrentScreenLeave();
        mAllAppsGrid.zoom(1.0f, animated);

        ((View) mAllAppsGrid).setFocusable(true);
        ((View) mAllAppsGrid).requestFocus();

        mDeleteZone.setVisibility(View.GONE);
        showDragLayerElement(false);
    }

    /**
     * Things to test when changing this code. - Home from workspace - from
     * center screen - from other screens - Home from all apps - from center
     * screen - from other screens - Back from all apps - from center screen -
     * from other screens - Launch app from workspace and quit - with back -
     * with home - Launch app from all apps and quit - with back - with home -
     * Go to a screen that's not the default, then all apps, and launch and app,
     * and go back - with back -with home - On workspace, long press power and
     * go back - with back - with home - On all apps, long press power and go
     * back - with back - with home - On workspace, power off - On all apps,
     * power off - Launch an app and turn off the screen while in that app - Go
     * back with home key - Go back with back key TODO: make this not go to
     * workspace - From all apps - From workspace - Enter and exit car mode
     * (becuase it causes an extra configuration changed) - From all apps - From
     * the center workspace - From another workspace
     */
    void closeAllApps(boolean animated) {
        if (mAllAppsGrid.isVisible()) {
			View hostView = mWorkspace.getChildAt(mWorkspace.getCurrentScreen());
        	View mtkWidgetView = mWorkspace.searchIMTKWidget(hostView);
        	if ( mtkWidgetView != null ) {
        		((IMTKWidget)mtkWidgetView).stopCovered(mWorkspace.getCurrentScreen());
        		if (DEBUG_SURFACEWIDGET) Log.e(TAG_SURFACEWIDGET, "stopCovered");
        	}
            showDragLayerElement(true);

            mWorkspace.hideWallpaper(false);
            mWorkspace.setVisibility(View.VISIBLE);
            mAllAppsGrid.zoom(0.0f, animated);
            ((View) mAllAppsGrid).setFocusable(false);
            mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
            mWorkspace.notifyCurrentScreenEnter();
        }
    }

    void lockAllApps() {
        // TODO
    }

    void unlockAllApps() {
        // TODO
    }

    /**
     * Displays the shortcut creation dialog and launches, if necessary, the
     * appropriate activity.
     */
    private class CreateShortcut implements DialogInterface.OnClickListener,
            DialogInterface.OnCancelListener, DialogInterface.OnDismissListener,
            DialogInterface.OnShowListener {

        private AddAdapter mAdapter;

        Dialog createDialog() {
            mAdapter = new AddAdapter(Launcher.this);

            final AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
            builder.setTitle(getString(R.string.menu_item_add_item));
            builder.setAdapter(mAdapter, this);

            builder.setInverseBackgroundForced(true);

            AlertDialog dialog = builder.create();
            dialog.setOnCancelListener(this);
            dialog.setOnDismissListener(this);
            dialog.setOnShowListener(this);

            return dialog;
        }

        public void onCancel(DialogInterface dialog) {
            mWaitingForResult = false;
            cleanup();
        }

        public void onDismiss(DialogInterface dialog) {
        }

        private void cleanup() {
            try {
                dismissDialog(DIALOG_CREATE_SHORTCUT);
            } catch (Exception e) {
                // An exception is thrown if the dialog is not visible, which is
                // fine
            }
        }

        /**
         * Handle the action clicked in the "Add to home" dialog.
         */
        public void onClick(DialogInterface dialog, int which) {
            Resources res = getResources();
            cleanup();
            
            ListItem listItem = (ListItem) mAdapter.getItem(which);
            
            switch (listItem.actionTag) {
                case AddAdapter.ITEM_SHORTCUT: {
                    // Insert extra item to handle picking application
                    pickShortcut();
                    break;
                }

                case AddAdapter.ITEM_APPWIDGET: {
                    int appWidgetId = Launcher.this.mAppWidgetHost.allocateAppWidgetId();

                    Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
                    pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    pickIntent.putExtra("fromWhere", "from_launcher_plus");
                    // start the pick activity
                    startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
                    break;
                }

                case AddAdapter.ITEM_LIVE_FOLDER: {
                    // Insert extra item to handle inserting folder
                    Bundle bundle = new Bundle();

                    ArrayList<String> shortcutNames = new ArrayList<String>();
                    shortcutNames.add(res.getString(R.string.group_folder));
                    bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

                    ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
                    shortcutIcons.add(ShortcutIconResource.fromContext(Launcher.this,
                            R.drawable.ic_launcher_folder));
                    bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            shortcutIcons);

                    Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                    final CellLayout cellLayout = (CellLayout) mShortcutWorkspace.getChildAt(mShortcutWorkspace.getCurrentScreen());
                    int count = cellLayout.getChildCount();
                    if ( isShortcutScreen() && count == Launcher.CELL_COUNT - 1 && !hasMoreFolder(mShortcutWorkspace.getCurrentScreen()) ) {
                    	pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent());
                    } else {
                    	pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(
                    			LiveFolders.ACTION_CREATE_LIVE_FOLDER));
                    }                    
                    pickIntent.putExtra(Intent.EXTRA_TITLE,
                            getText(R.string.title_select_live_folder));
                    pickIntent.putExtras(bundle);

                    startActivityForResult(pickIntent, REQUEST_PICK_LIVE_FOLDER);
                    break;
                }
                
                case AddAdapter.ITEM_WALLPAPER: {
                    startWallpaper();
                    break;
                }
                
                case AddAdapter.ITEM_VIDEO_WALLPAPER: {
                	startVideoWallpaper();
                	break;
                }
            }
        }

        public void onShow(DialogInterface dialog) {
            mWaitingForResult = true;
        }
    }

    /**
     * Receives notifications when applications are added/removed.
     */
    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            closeSystemDialogs();
            String reason = intent.getStringExtra("reason");
            if (!"homekey".equals(reason)) {
                boolean animate = true;
                if (mPaused || "lock".equals(reason)) {
                    animate = false;
                }
                closeAllApps(animate);
            }
        }
    }

    /**
     * Receives notifications whenever the appwidgets are reset.
     */
    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            onAppWidgetReset();
        }
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public int getCurrentWorkspaceScreen() {
        if (mWorkspace != null) {
            return mWorkspace.getCurrentScreen();
        } else {
            return DEFAULT_SCREEN;
        }
    }

    /**
     * Refreshes the shortcuts shown on the workspace. Implementation of the
     * method from LauncherModel.Callbacks.
     */
    public void startBinding() {
        final Workspace workspace = mWorkspace;
        /*int count = workspace.getChildCount();
        for (int i = 0; i < count; i++) {
            // Use removeAllViewsInLayout() to avoid an extra requestLayout()
            // and invalidate().
            ((ViewGroup) workspace.getChildAt(i)).removeAllViewsInLayout();
        }*/
        if (mIsDefStyle) {
        	mDefaultWorkspace.removeViews();
        } else {
        	mShortcutWorkspace.removeViews();
        	mWidgetWorkspace.removeViews();
        }

        if (DEBUG_USER_INTERFACE) {
            android.widget.Button finishButton = new android.widget.Button(this);
            finishButton.setText("Finish");
            workspace.addInScreen(finishButton, 1, 0, 0, 1, 1);

            finishButton.setOnClickListener(new android.widget.Button.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    /**
     * Bind the items start-end from the list. Implementation of the method from
     * LauncherModel.Callbacks.
     */
    public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {

        Workspace workspace = null;
        if ( mIsDefStyle ) {
        	workspace= mDefaultWorkspace;
        } else {
        	workspace = mShortcutWorkspace;
        }

        synchronized (LauncherModel.mObject) {
            if (shortcuts.size() < end) {
                Log.w(TAG, "Launcher.bindItems exit without bind. because siez is "
                        + shortcuts.size() + ", and end is " + end);
                return;
            }

            for (int i = start; i < end; i++) {
                final ItemInfo item = shortcuts.get(i);
                mShortcutDesktopItems.add(item);
                switch (item.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                        if (item.container == LauncherSettings.Favorites.CONTAINER_PHONE) {
                            mPhoneShortcut.setTag(item);
                            mPhoneShortcut.setOnClickListener(this);
                        } else if (item.container == LauncherSettings.Favorites.CONTAINER_MESSAGE) {
                            mMessageShortcut.setTag(item);
                            mMessageShortcut.setOnClickListener(this);
                        } else {
                            final View shortcut = createShortcut((ShortcutInfo) item);
                            workspace.addInScreen(shortcut, item.screen, item.cellX, item.cellY, 1, 1,
                                    false);
                        }
                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
                        final FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
                                (ViewGroup) workspace.getChildAt(workspace.getCurrentScreen()),
                                (UserFolderInfo) item);
                        if (!mIsDefStyle) {
                        	newFolder.updateAppIconThumbails();
                        }
                        workspace.addInScreen(newFolder, item.screen, item.cellX, item.cellY, 1, 1,
                                false);
                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
                        final FolderIcon newLiveFolder = LiveFolderIcon.fromXml(
                                R.layout.live_folder_icon, this, (ViewGroup) workspace
                                        .getChildAt(workspace.getCurrentScreen()),
                                (LiveFolderInfo) item);
                        workspace.addInScreen(newLiveFolder, item.screen, item.cellX, item.cellY,
                                1, 1, false);
                        break;
                }
            }
        }

        workspace.requestLayout();
    }

	public void bindAppNotInScreen(ArrayList<ItemInfo> folderItems) {
		ArrayList<ItemInfo> appMayBeNotInScreen = new ArrayList<ItemInfo>(); 
		mAppNotInDesktopItems.clear();
    	AllAppsList allAppsList = mModel.getAllAppList();
        int allAppCount = allAppsList.size();
        for( int i = 0; i < allAppCount; i++ ) {
        	final int shortcutCount = mShortcutDesktopItems.size();
        	final ApplicationInfo app = (ApplicationInfo)allAppsList.get(i);
        	int j = 0;
        	for( ; j < shortcutCount; j++ ) {
        		final ItemInfo item = mShortcutDesktopItems.get(j);
        		if ( item instanceof ShortcutInfo ) {
        			ShortcutInfo shortcut = (ShortcutInfo)item;
        			if (app.intent.getComponent() != null ) {
        				if (app.intent.getComponent().equals(shortcut.intent.getComponent())) 
        						break;
        			}
        		}            		
        	}
        	if ( j == shortcutCount ) {
        		appMayBeNotInScreen.add(app);
        	}
        }
        
        int n=0;
        for (; n<appMayBeNotInScreen.size(); n++) {
			final ApplicationInfo app = (ApplicationInfo) appMayBeNotInScreen.get(n);
			int k = 0;
			final int folderItemsCount = folderItems.size();
			for (; k < folderItemsCount; k++) {
				final ShortcutInfo appInfo = (ShortcutInfo) folderItems.get(k);
				if (app.intent.getComponent() != null &&
					app.intent.getComponent().equals(appInfo.intent.getComponent()))
					break;
			}
			if (k==folderItemsCount) {
				mAppNotInDesktopItems.add(app);
			}
        }
        
        //bind apk installed before launcher
        int count = mAppNotInDesktopItems.size();
        for( int i = 0; i < count; i++ ) {
        	final ApplicationInfo item = (ApplicationInfo) mAppNotInDesktopItems.get(i);
        	Intent intent = item.intent;
        	completeAddApplication(getApplicationContext(), intent, mShortcutWorkspace.getChildCount() - 1);
        }
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindFolders(HashMap<Long, FolderInfo> folders) {
        mFolders.clear();
        mFolders.putAll(folders);
    }

    /**
     * Add the views for a widget to the workspace. Implementation of the method
     * from LauncherModel.Callbacks.
     */
    public void bindAppWidget(LauncherAppWidgetInfo item) {
        final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: " + item);
        }
        Workspace workspace = null;
        if ( mIsDefStyle ) {
        	workspace= mDefaultWorkspace;
        } else {
        	workspace = mWidgetWorkspace;
        }

        final int appWidgetId = item.appWidgetId;
        final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component "
                    + appWidgetInfo.provider);
        }

        item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);

        item.hostView.setAppWidget(appWidgetId, appWidgetInfo);
        item.hostView.setTag(item);

        workspace.addInScreen(item.hostView, item.screen, item.cellX, item.cellY, item.spanX,
                item.spanY, false);

        workspace.requestLayout();

        mWidgetDesktopItems.add(item);

        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bound widget id=" + item.appWidgetId + " in "
                    + (SystemClock.uptimeMillis() - start) + "ms");
        }
    }

    /**
     * Callback saying that there aren't any more items to bind. Implementation
     * of the method from LauncherModel.Callbacks.
     */
    public void finishBindingItems() {
        if (mSavedState != null) {
            //mWorkspace.snapToScreen(getCurrentWorkspaceScreen());
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
            }

            final long[] userFolders = mSavedState.getLongArray(RUNTIME_STATE_USER_FOLDERS);
            if (userFolders != null) {
                for (long folderId : userFolders) {
                    final FolderInfo info = mFolders.get(folderId);
                    if (info != null) {
                        openFolder(info);
                    }
                }
                final Folder openFolder = mWorkspace.getOpenFolder();
                if (openFolder != null) {
                    openFolder.requestFocus();
                }
            }

            mSavedState = null;
        }

        if (mSavedInstanceState != null) {
            super.onRestoreInstanceState(mSavedInstanceState);
            mSavedInstanceState = null;
        }

        mWorkspaceLoading = false;
        if (mProgressDialog != null) {
            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {
                // We catch exception here, because have no impact on user
                Log.e(TAG, "Exception when Dialog.dismiss()...");
            } finally {
                mProgressDialog = null;
            }
        }
    }

    /**
     * Add the icons for all apps. Implementation of the method from
     * LauncherModel.Callbacks.
     */
    public void bindAllApplications(ArrayList<ApplicationInfo> apps) {
        mAllAppsGrid.setApps(apps);
    }

    /**
     * A package was installed. Implementation of the method from
     * LauncherModel.Callbacks.
     */
    public void bindAppsAdded(ArrayList<ApplicationInfo> apps) {
        removeDialog(DIALOG_CREATE_SHORTCUT);
        mAllAppsGrid.addApps(apps);
    }

    /**
     * A package was updated. Implementation of the method from
     * LauncherModel.Callbacks.
     */
    public void bindAppsUpdated(ArrayList<ApplicationInfo> apps) {
        removeDialog(DIALOG_CREATE_SHORTCUT);
        mWorkspace.updateShortcuts(apps);
        mAllAppsGrid.updateApps(apps);
    }

    /**
     * A package was uninstalled. Implementation of the method from
     * LauncherModel.Callbacks.
     */
    public void bindAppsRemoved(ArrayList<ApplicationInfo> apps) {
        removeDialog(DIALOG_CREATE_SHORTCUT);
        if (mIsDefStyle) {
        	mDefaultWorkspace.removeItems(apps);
        } else {
        	mShortcutWorkspace.removeItems(apps);
        }
        mAllAppsGrid.removeApps(apps);
    }

    public void addInScreen(View view) {
		LauncherAppWidgetInfo launcherInfo = (LauncherAppWidgetInfo) view.getTag();
		mWidgetDesktopItems.add(launcherInfo);
		// Recreate hostView and put it in CellLayout.
		int appWidgetId = launcherInfo.appWidgetId;
		AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
		launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
		launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
		launcherInfo.hostView.setTag(launcherInfo);
		mWorkspace.addInScreen(launcherInfo.hostView, mWorkspace.getDropScreen(),
				launcherInfo.cellX, launcherInfo.cellY, launcherInfo.spanX, launcherInfo.spanY, false);
    }

    private void handleNoRoomForWidget(final int[] spans, final int appWidgetId) {
        LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId);
        launcherInfo.spanX = spans[0];
        launcherInfo.spanY = spans[1];
        launcherInfo.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
        launcherInfo.screen = mWorkspace.getCurrentScreen();
        launcherInfo.cellX = 0;
        launcherInfo.cellY = 0;

        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        launcherInfo.hostView = mAppWidgetHost.createView(Launcher.this, appWidgetId, appWidgetInfo);
        launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
        launcherInfo.hostView.setTag(launcherInfo);

        mDragController.startDrag(launcherInfo.hostView, launcherInfo, spans[0], spans[1],
                DragController.DRAG_ACTION_ADD);
    }

    private void showHomeLoadingDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new Dialog(this);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setContentView(R.layout.progressbar);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
    }

    public boolean getPortrait() {
        return mPortrait;
    }

    public boolean getPaused() {
        return mPaused;
    }
    
    public void setDesktopItems( CellLayout.CellInfo cellInfo ) {
    	mAddItemCellInfo = cellInfo;
    }
    
    private void initialIconBackgroundBitmap() {
    	int[] iconBgBimap = new int[] {R.drawable.icon_background_blue, R.drawable.icon_background_green, R.drawable.icon_background_indigo,
    			R.drawable.icon_background_orange, R.drawable.icon_background_purple, R.drawable.icon_background_red, R.drawable.icon_background_yellow};
    	
    	for( int i = 0; i < Launcher.INITIAL_ICON_BACKGROUND_CAPACITY; i++ ) {
    		Bitmap iconBgBitmap = BitmapFactory.decodeResource(getResources(), iconBgBimap[i]);
    		mIconBackgroundBitmap[i] = iconBgBitmap;
    	}
    	
    	mThemeIconBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_background_transparent);
    	
    	mKillBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.kill);
    	
    	mIconEditBg = BitmapFactory.decodeResource(getResources(), R.drawable.icon_edit_bg);
    }
    
    public static boolean isShortcutScreen() {
    	return ( enhanceStyle.equalsIgnoreCase(mCurrentStyle) && shortcutTheme.equalsIgnoreCase(mCurrentTheme));
    }
    
    public static boolean isDefStyle() {
    	return (defStyle.equalsIgnoreCase(mCurrentStyle));
    }
    
    public boolean isSpecialWidthAndHeight(int width, int height) {
        Display mDisplay = getWindowManager().getDefaultDisplay();
        final int mWidth = mDisplay.getWidth();
        final int mHeight = mDisplay.getHeight();

        return ((width == mWidth) && (height == mHeight));
    }

    public static boolean isQVGAMode() {
        return isQVGAMode;
    }

	private void initAppWidgetAniamtion() {
		mInAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_in);
		mOutAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_out);
		mOutAnimation.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation ani) {
				switchEnhancementWorkspace(mIsShortcutWorkspace);

				if (mIsShortcutWorkspace) {
					mShortcutWorkspace.getChildAt(mShortcutWorkspace.getCurrentScreen()).startAnimation(mInAnimation);
				} else {
					mWidgetWorkspace.getChildAt(mWidgetWorkspace.getCurrentScreen()).startAnimation(mInAnimation);
				}

				updateWorkspaceSwitchEnhancementWorkspace();
			}

			public void onAnimationRepeat(Animation ani) {

			}

			public void onAnimationStart(Animation ani) {

			}
		});
	}
    
    public void switchShortcutAndWidget() {
	if (isWorkspaceLocked() || mIsInEditMode) {
    		return;
    	}

    	mIsShortcutWorkspace = !mIsShortcutWorkspace;
    	
    	View hostView = mWidgetWorkspace.getChildAt(mWidgetWorkspace.getCurrentScreen());
    	View mtkWidgetView = mWidgetWorkspace.searchIMTKWidget(hostView);
    	
    	if (mIsShortcutWorkspace) {
    		if ( mtkWidgetView != null) {
        		((IMTKWidget)mtkWidgetView).moveOut(mWidgetWorkspace.getCurrentScreen());
        		if (DEBUG_SURFACEWIDGET) Log.e(TAG_SURFACEWIDGET, "moveOut");
        	}
    	} else {
    		if ( mtkWidgetView != null) {
        		((IMTKWidget)mtkWidgetView).moveIn(mWidgetWorkspace.getCurrentScreen());
        		if (DEBUG_SURFACEWIDGET) Log.e(TAG_SURFACEWIDGET, "moveIn");
        	}
    	}

    	if(mOutAnimation == null || mInAnimation == null){
    		initAppWidgetAniamtion();
    	}
    	if ( mIsShortcutWorkspace ) {
			//start shortcut out animation
    		mWidgetWorkspace.getChildAt(mWidgetWorkspace.getCurrentScreen()).startAnimation(mOutAnimation);
    	} else {
			// start widget page out animation
			mShortcutWorkspace.getChildAt(mShortcutWorkspace.getCurrentScreen()).startAnimation(mOutAnimation);
    	}
    	saveThemeSetting(this, mIsShortcutWorkspace ? shortcutTheme : widgetTheme);
    }

	private void showTopLayer(boolean isVisible) {
    	int visibilityParam = isVisible ? View.VISIBLE : View.INVISIBLE;
    	mTopLayer.setVisibility(visibilityParam);
		mPageClassification.setVisibility(visibilityParam);
		mSwitch.setVisibility(visibilityParam);
		if ( isVisible ) {
			mPageClassification.setText(pageClassification[mShortcutWorkspace.getCurrentScreen()]);
		} else {
			mPageClassification.setText(null);
		}
    }
    
    private void showHandleViewButton(boolean isVisible) {
		mHandleView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    	mSwitchShortcutAndWidget.setVisibility(isVisible ? View.INVISIBLE : View.VISIBLE);    		
    }
    
    private void updateWorkspaceSwitchEnhancementWorkspace() {
    	mDragController.updateDropTargets(mWorkspace);

		mDragController.setDragScoller(mWorkspace);
		mDragController.setMoveTarget(mWorkspace);

		mWorkspace.setWallpaper(true);
    }
    
    private void updateWorkspaceSwitchStyle() {
    	mDragController.removeAllDropTargets();
    	mDragController.addDropTarget(mWorkspace);
    	mDragController.addDropTarget(mDeleteZone);

		mDragController.setDragScoller(mWorkspace);
		mDragController.setMoveTarget(mWorkspace);

		mWorkspace.setWallpaper(true);
    }
    
    private void switchEnhancementWorkspace(boolean isShortcutWorkspace) {
    	if ( isShortcutWorkspace ) {
			mWidgetWorkspace.hideWorkspace();

			showTopLayer(true);

			mSwitchShortcutAndWidget.setImageDrawable(getResources().getDrawable(R.drawable.apps_button));

			mShortcutWorkspace.showWorkspace();
			mDragLayer.setWorkspace(mShortcutWorkspace);
			mWorkspace = mShortcutWorkspace;
    	} else {
    		mShortcutWorkspace.hideWorkspace();
			
			showTopLayer(false);
			
    		mSwitchShortcutAndWidget.setImageDrawable(getResources().getDrawable(R.drawable.widget_button));

    		//show widget workspace
    		mWidgetWorkspace.showWorkspace();
    		mDragLayer.setWorkspace( mWidgetWorkspace );
    		mWorkspace = mWidgetWorkspace;
    	}
    }
    
    private void switchDefaultStyleWorkspace(boolean isDefStyle) {
    	if ( isDefStyle ) {
    		mShortcutWorkspace.hideWorkspace();
    		mWidgetWorkspace.hideWorkspace();
    		mShortcutWorkspace.removeViews();
    		mWidgetWorkspace.removeViews();
    		
    		mDragLayer.setWorkspace( mDefaultWorkspace );
    		mDefaultWorkspace.showWorkspace();
    		mDefaultWorkspace.setCurrentScreen(DEFAULT_SCREEN);
    		mWorkspace = mDefaultWorkspace;
    	} else {
    		mDefaultWorkspace.hideWorkspace();
    		mDefaultWorkspace.removeViews();
    		
    		mShortcutWorkspace.setCurrentScreen(DEFAULT_SCREEN);
    		mWidgetWorkspace.setCurrentScreen(DEFAULT_SCREEN);
    		
    		switchEnhancementWorkspace(mIsShortcutWorkspace);
    	}
    }
    
    public void clearAndSwitchStyle() {
    	mIsDefStyle = !mIsDefStyle;
    	
    	switchDefaultStyleWorkspace( mIsDefStyle );
    	
    	if ( mIsDefStyle ) {
    		showTopLayer(false);    		
    		showHandleViewButton(true);    		
    	} else {
    		showHandleViewButton(false);    		
    	}
    	
    	updateWorkspaceSwitchStyle();
		
		saveStyleSetting(this, mIsDefStyle ? defStyle : enhanceStyle);
		
   	 	mIconCache.flush();
		
		mModel.unbind();
		
		switchStyle();
    }
    
    private void switchStyle() {
    	 showHomeLoadingDialog();
    	     	 
    	 mWorkspaceLoading = true;
    	 mModel.setAllAppsDirty();         
         mModel.startLoader(this, true);
         mRestoring = false;
    }
    
    public Workspace getShortcutWorkspace() {
    	return mShortcutWorkspace;
    }
    
    public Indicator getIndicator() {
        return mIndicator;
    }
    
    IBinder getWindowToken() {
        return mDragLayer.getWindowToken();
    }
    
    public Bitmap getKillBitmap() {
    	return mKillBitmap;
    }
    
    public boolean isScreenLandscape() {
        Configuration conf = getResources().getConfiguration();
        return conf.orientation == Configuration.ORIENTATION_LANDSCAPE ? true : false;
    }
    
    public static void saveStyleSetting(Context context, String style) {
    	SharedPreferences settings = context.getSharedPreferences(CURRENT_STYLE, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(CURRENT_STYLE, style);
        mCurrentStyle = style;

        // Don't forget to commit your edits!!!
        editor.commit();
    }
    
    public static void saveThemeSetting(Context context, String theme) {
        SharedPreferences settings = context.getSharedPreferences(CURRENT_SCENE, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(CURRENT_SCENE, theme);
        mCurrentTheme = theme;

        // Don't forget to commit your edits!!!
        editor.commit();
    }

    public static String restoreThemeSetting(Context context) {
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(CURRENT_SCENE, 0);
        String tmp = settings.getString(CURRENT_SCENE, null);
        mCurrentTheme = tmp;
        return tmp;
    }

    public static String getCurrentScene() {
        return mCurrentTheme;
    }
    
    public static String getCurrentStyle() {
    	return mCurrentStyle;
    }

	public DeleteZone getDeleteZone() {
    	return mDeleteZone;
    }

    /**
     * Prints out out state for debugging.
     */
    public void dumpState() {
        Log.d(TAG, "BEGIN launcherPlus dump state for launcher " + this);
        Log.d(TAG, "mSavedState=" + mSavedState);
        Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
        Log.d(TAG, "mRestoring=" + mRestoring);
        Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
        Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
        Log.d(TAG, "mShortcutDesktopItems.size=" + mShortcutDesktopItems.size());
        Log.d(TAG, "mWidgetDesktopItems.size=" + mWidgetDesktopItems.size());
        Log.d(TAG, "mFolders.size=" + mFolders.size());
        mModel.dumpState();
        mAllAppsGrid.dumpState();
        Log.d(TAG, "END launcherPlus dump state");
    }
}
