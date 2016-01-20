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

import com.android.quicksearchbox.util.MockNamedTaskExecutor;

import android.os.Handler;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

/**
 * Tests for {@link SuggestionsProviderImpl}.
 */
@MediumTest
public class SuggestionsProviderImplTest extends AndroidTestCase {

    private MockCorpora mCorpora;
    private MockNamedTaskExecutor mTaskExecutor;
    private SuggestionsProviderImpl mProvider;

    @Override
    protected void setUp() throws Exception {
        Config config = new Config(getContext());
        mTaskExecutor = new MockNamedTaskExecutor();
        Handler publishThread = new MockHandler();
        ShortcutRepository shortcutRepo = new MockShortcutRepository();
        mCorpora = new MockCorpora();
        mCorpora.addCorpus(MockCorpus.CORPUS_1);
        mCorpora.addCorpus(MockCorpus.CORPUS_2);
        CorpusRanker corpusRanker = new LexicographicalCorpusRanker(mCorpora);
        Logger logger = new NoLogger();
        mProvider = new SuggestionsProviderImpl(config,
                mTaskExecutor,
                publishThread,
                shortcutRepo,
                mCorpora,
                corpusRanker,
                logger);
        mProvider.setAllPromoter(new ConcatPromoter());
        mProvider.setSingleCorpusPromoter(new ConcatPromoter());
    }

    public void testSingleCorpus() {
        Suggestions suggestions = mProvider.getSuggestions("foo", MockCorpus.CORPUS_1, 3);
        try {
            assertEquals(1, suggestions.getExpectedResultCount());
            assertEquals(0, suggestions.getResultCount());
            assertEquals(0, suggestions.getPromoted().getCount());
            assertTrue(mTaskExecutor.runNext());
            assertEquals(1, suggestions.getExpectedResultCount());
            assertEquals(1, suggestions.getResultCount());
            assertEquals(MockCorpus.CORPUS_1.getSuggestions("foo", 3, true).getCount(),
                    suggestions.getPromoted().getCount());
            mTaskExecutor.assertDone();
        } finally {
            if (suggestions != null) suggestions.close();
        }
    }

    public void testMultipleCorpora() {
        Suggestions suggestions = mProvider.getSuggestions("foo", null, 6);
        try {
            int corpus1Count = MockCorpus.CORPUS_1.getSuggestions("foo", 3, true).getCount();
            int corpus2Count = MockCorpus.CORPUS_2.getSuggestions("foo", 3, true).getCount();
            assertEquals(mCorpora.getEnabledCorpora().size(), suggestions.getExpectedResultCount());
            assertEquals(0, suggestions.getResultCount());
            assertEquals(0, suggestions.getPromoted().getCount());
            assertTrue(mTaskExecutor.runNext());
            assertEquals(1, suggestions.getResultCount());
            assertEquals("Incorrect promoted: " + suggestions.getPromoted(),
                    corpus1Count, suggestions.getPromoted().getCount());
            assertTrue(mTaskExecutor.runNext());
            assertEquals(2, suggestions.getResultCount());
            assertEquals("Incorrect promoted: " + suggestions.getPromoted(),
                    corpus1Count + corpus2Count, suggestions.getPromoted().getCount());
            mTaskExecutor.assertDone();
        } finally {
            if (suggestions != null) suggestions.close();
        }
    }

}
