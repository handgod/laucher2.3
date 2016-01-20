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

package com.android.quicksearchbox.google;

import java.util.ArrayList;
import java.util.List;

import com.android.quicksearchbox.QsbApplication;
import com.android.quicksearchbox.SearchSettings;
import com.android.quicksearchbox.SearchWidgetProvider;

import android.app.SearchEngineInfo;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.CheckBox;

import com.android.quicksearchbox.R;


public class SearchEngineSettings extends PreferenceActivity implements OnPreferenceClickListener {

	private static final boolean DBG = false;
	private static final String TAG = "SearchEngineSettings";
    public static final String GOOGLE = "google";
    private static final String PREF_SYNC_SEARCH_ENGINE = "syc_search_engine";
    
    // intent action used to notify Browser that user has has changed search engine setting
    private static final String ACTION_BROWSER_SEARCH_ENGINE_CHANGED = 
    		"com.android.browser.SEARCH_ENGINE_CHANGED";

    public static final String PREF_SEARCH_ENGINE = "search_engine";
	
	private List<RadioPreference> mRadioPrefs;
	private CheckBoxPreference mCheckBoxPref;
	private SharedPreferences mPrefs;
    
    private String[] mEntryValues;
    private String[] mEntries;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRadioPrefs = new ArrayList<RadioPreference>();
        mPrefs = SearchSettings.getSearchPreferences(this);
        
        int selectedItem = -1;
        String searchEngineName = mPrefs.getString(PREF_SEARCH_ENGINE, GOOGLE);
        List<SearchEngineInfo> searchEngines = getSearchEngineInfos(this);
    	int len = searchEngines.size();
    	mEntryValues = new String[len];
    	mEntries = new String[len];
    	
    	for (int i = 0; i < len; i++) {
    		mEntryValues[i] = searchEngines.get(i).getName();
    		mEntries[i] = searchEngines.get(i).getLabel();
			if (mEntryValues[i].equals(searchEngineName)) {
				selectedItem = i;
			}
		}
    	setPreferenceScreen(createPreferenceHierarchy());
    	if (selectedItem!= -1) {
    		mRadioPrefs.get(selectedItem).setChecked(true);
    	}
    }
    
    public static List<SearchEngineInfo> getSearchEngineInfos(Context context) {
        SearchManager searchManager = (SearchManager)context.getSystemService(Context.SEARCH_SERVICE);
        return searchManager.getSearchEngineInfos();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean(PREF_SYNC_SEARCH_ENGINE, mCheckBoxPref.isChecked());
		editor.commit();
		broadcastSearchEngineChangedInternal(this);
		if (mCheckBoxPref.isChecked()) {
			broadcastSearchEngineChangedExternal(this);
		}
    }
    
    private void broadcastSearchEngineChangedInternal(Context context) {
    	Intent intent = new Intent(SearchWidgetProvider.ACTION_SEARCH_ENGINE_CHANGED);
    	intent.setPackage(getPackageName());
    	intent.putExtra(PREF_SEARCH_ENGINE, mPrefs.getString(PREF_SEARCH_ENGINE, GOOGLE));
        
    	if (DBG) {
    		Log.i(TAG, "Broadcasting: " + intent);
    	}
        context.sendBroadcast(intent);
    }
    
    private void broadcastSearchEngineChangedExternal(Context context) {
    	Intent intent = new Intent(ACTION_BROWSER_SEARCH_ENGINE_CHANGED);
    	intent.setPackage("com.android.browser");
    	intent.putExtra(PREF_SEARCH_ENGINE, mPrefs.getString(PREF_SEARCH_ENGINE, GOOGLE));
        
    	if (DBG) {
    		Log.i(TAG, "Broadcasting: " + intent);
    	}
        context.sendBroadcast(intent);
    }

    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
        
        PreferenceCategory consistencyPref = new PreferenceCategory(this);
        consistencyPref.setTitle(R.string.pref_content_search_engine_consistency);
        root.addPreference(consistencyPref);
        
        // Toggle preference
        mCheckBoxPref = new CheckBoxPreference(this);
        mCheckBoxPref.setKey("toggle_consistency");
        mCheckBoxPref.setTitle(R.string.pref_search_engine_unify);
        mCheckBoxPref.setSummaryOn(R.string.pref_search_engine_unify_summary);
        mCheckBoxPref.setSummaryOff(R.string.pref_search_engine_unify_summary);
        consistencyPref.addPreference(mCheckBoxPref);
        boolean syncSearchEngine = mPrefs.getBoolean(PREF_SYNC_SEARCH_ENGINE, true);
        mCheckBoxPref.setChecked(syncSearchEngine);
        
        PreferenceCategory searchEnginesPref = new PreferenceCategory(this);
        searchEnginesPref.setTitle(R.string.pref_content_search_engine);
        root.addPreference(searchEnginesPref);
        
        for(int i = 0; i < mEntries.length; i++) {
        	RadioPreference radioPref = new RadioPreference(this);
        	radioPref.setWidgetLayoutResource(R.layout.radio_preference);
        	radioPref.setTitle(mEntries[i]);
        	radioPref.setOrder(i);
        	radioPref.setOnPreferenceClickListener(this);
        	searchEnginesPref.addPreference(radioPref);
        	mRadioPrefs.add(radioPref);
        }
        
        return root;
    }

	public boolean onPreferenceClick(Preference preference) {
		for(RadioPreference radioPref: mRadioPrefs) {
			radioPref.setChecked(false);
		}
		
		((RadioPreference)preference).setChecked(true);
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString(PREF_SEARCH_ENGINE, mEntryValues[preference.getOrder()]);
		editor.commit();
		return true;
	}
}
