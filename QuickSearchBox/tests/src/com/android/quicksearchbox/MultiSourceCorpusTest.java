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
 * Copyright (C) 2010 The Android Open Source Project
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

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;
import android.test.suitebuilder.annotation.LargeTest;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Tests for {@link MultiSourceCorpus}.
 */
@LargeTest
public class MultiSourceCorpusTest extends AndroidTestCase {

    protected MultiSourceCorpus mCorpus;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Config config = new Config(getContext());
        // Using a single thread to make the test deterministic
        Executor executor = Executors.newSingleThreadExecutor();
        mCorpus = new SkeletonMultiSourceCorpus(getContext(), config, executor,
                MockSource.SOURCE_1, MockSource.SOURCE_2);
    }

    public void testGetSources() {
        MoreAsserts.assertContentsInOrder(mCorpus.getSources(),
                MockSource.SOURCE_1, MockSource.SOURCE_2);
    }

    public void testGetSuggestions() {
        ListSuggestionCursor expected = concatSuggestionCursors("foo",
                MockSource.SOURCE_1.getSuggestions("foo", 50, false),
                MockSource.SOURCE_2.getSuggestions("foo", 50, false));
        CorpusResult observed = mCorpus.getSuggestions("foo", 50, false);
        SuggestionCursorUtil.assertSameSuggestions(expected, observed);
    }

    private static ListSuggestionCursor concatSuggestionCursors(String query,
            SuggestionCursor... cursors) {
        ListSuggestionCursor out = new ListSuggestionCursor("foo");
        for (SuggestionCursor cursor : cursors) {
            int count = cursor.getCount();
            for (int i = 0; i < count; i++) {
                out.add(new SuggestionPosition(cursor, i));
            }
        }
        return out;
    }

    private static class SkeletonMultiSourceCorpus extends MultiSourceCorpus {

        public SkeletonMultiSourceCorpus(Context context, Config config, Executor executor,
                Source... sources) {
            super(context, config, executor, sources);
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

        public Drawable getCorpusIcon() {
            return null;
        }

        public Uri getCorpusIconUri() {
            return null;
        }

        public CharSequence getHint() {
            return null;
        }

        public CharSequence getLabel() {
            return null;
        }

        @Override
        public int getQueryThreshold() {
            return 0;
        }

        public CharSequence getSettingsDescription() {
            return null;
        }

        public boolean isWebCorpus() {
            return false;
        }

        @Override
        public boolean queryAfterZeroResults() {
            return false;
        }

        @Override
        public boolean voiceSearchEnabled() {
            return false;
        }

        public String getName() {
            return "SkeletonMultiSourceCorpus";
        }

    }
}
