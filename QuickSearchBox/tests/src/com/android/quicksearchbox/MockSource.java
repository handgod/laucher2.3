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
 */

/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.quicksearchbox;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

/**
 * Mock implementation of {@link Source}.
 *
 */
public class MockSource implements Source {

    public static final MockSource SOURCE_1 = new MockSource("SOURCE_1");

    public static final MockSource SOURCE_2 = new MockSource("SOURCE_2");

    public static final MockSource SOURCE_3 = new MockSource("SOURCE_3");

    public static final MockSource WEB_SOURCE = new MockSource("WEB") {
        @Override
        public boolean isWebSuggestionSource() {
            return true;
        }
    };

    private final String mName;

    private final int mVersionCode;

    public MockSource(String name) {
        this(name, 0);
    }

    public MockSource(String name, int versionCode) {
        mName = name;
        mVersionCode = versionCode;
    }

    public ComponentName getIntentComponent() {
        // Not an activity, but no code should treat it as one.
        return new ComponentName("com.android.quicksearchbox",
                getClass().getName() + "." + mName);
    }

    public int getVersionCode() {
        return mVersionCode;
    }

    public boolean isVersionCodeCompatible(int version) {
        return version == mVersionCode;
    }

    public String getName() {
        return getIntentComponent().flattenToShortString();
    }

    public String getDefaultIntentAction() {
        return Intent.ACTION_SEARCH;
    }

    public String getDefaultIntentData() {
        return null;
    }

    public Drawable getIcon(String drawableId) {
        return null;
    }

    public Uri getIconUri(String drawableId) {
        return null;
    }

    public String getLabel() {
        return "MockSource " + mName;
    }

    public int getQueryThreshold() {
        return 0;
    }

    public CharSequence getHint() {
        return null;
    }

    public String getSettingsDescription() {
        return "Suggestions from MockSource " + mName;
    }

    public Drawable getSourceIcon() {
        return null;
    }

    public Uri getSourceIconUri() {
        return null;
    }

    public boolean canRead() {
        return true;
    }

    public boolean isLocationAware() {
        return false;
    }

    public SourceResult getSuggestions(String query, int queryLimit, boolean onlySource) {
        if (query.length() == 0) {
            return null;
        }
        ListSuggestionCursor cursor = new ListSuggestionCursor(query);
        cursor.add(createSuggestion(query + "_1"));
        cursor.add(createSuggestion(query + "_2"));
        return new Result(query, cursor);
    }

    public Suggestion createSuggestion(String query) {
        Uri data = new Uri.Builder().scheme("content").authority(mName).path(query).build();
        return new SuggestionData(this)
                .setText1(query)
                .setIntentAction(Intent.ACTION_VIEW)
                .setIntentData(data.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o.getClass().equals(this.getClass())) {
            MockSource s = (MockSource) o;
            return s.mName.equals(mName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @Override
    public String toString() {
        return getName() + ":" + getVersionCode();
    }

    private class Result extends SuggestionCursorWrapper implements SourceResult {

        public Result(String userQuery, SuggestionCursor cursor) {
            super(userQuery, cursor);
        }

        public Source getSource() {
            return MockSource.this;
        }

    }

    public SuggestionCursor refreshShortcut(String shortcutId, String extraData) {
        return null;
    }

    public boolean isExternal() {
        return false;
    }

    public boolean isWebSuggestionSource() {
        return false;
    }

    public boolean queryAfterZeroResults() {
        return false;
    }

    public Intent createSearchIntent(String query, Bundle appData) {
        return null;
    }

    public SuggestionData createSearchShortcut(String query) {
        return null;
    }

    public Intent createVoiceSearchIntent(Bundle appData) {
        return null;
    }

    public boolean voiceSearchEnabled() {
        return false;
    }

}
