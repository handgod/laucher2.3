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

import com.android.quicksearchbox.util.MockDataSetObserver;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Tests for {@link Suggestions}.
 *
 */
@SmallTest
public class SuggestionsTest extends AndroidTestCase {

    private Suggestions mSuggestions;
    private MockDataSetObserver mObserver;
    private List<Corpus> mExpectedCorpora;

    @Override
    protected void setUp() throws Exception {
        mExpectedCorpora = Arrays.asList(new Corpus[]{null,null});
        mSuggestions = new Suggestions(null, 0, "foo", mExpectedCorpora);
        mObserver = new MockDataSetObserver();
        mSuggestions.registerDataSetObserver(mObserver);
    }

    @Override
    protected void tearDown() throws Exception {
        mSuggestions.close();
        mSuggestions = null;
    }

    public void testGetExpectedResultCount() {
        assertEquals(mExpectedCorpora.size(), mSuggestions.getExpectedResultCount());
    }

    public void testGetExpectedCorpora() {
        List<Corpus> expectedCorpora = mSuggestions.getExpectedCorpora();
        assertEquals(mExpectedCorpora.size(), expectedCorpora.size());
        for (int i=0; i<mExpectedCorpora.size(); ++i) {
            assertEquals(mExpectedCorpora.get(i), expectedCorpora.get(i));
        }
    }

    public void testGetUserQuery() {
        assertEquals("foo", mSuggestions.getQuery());
    }

    public void testGetIncludedCorpora() {
        Corpus corpus = MockCorpus.CORPUS_1;
        mSuggestions.addCorpusResults(
                Collections.singletonList(corpus.getSuggestions("foo", 50, true)));
        Set<Corpus> includedCorpora = mSuggestions.getIncludedCorpora();
        assertEquals(includedCorpora.size(), 1);
        assertTrue(includedCorpora.contains(corpus));
    }

    public void testObserverNotified() {
        Corpus corpus = MockCorpus.CORPUS_1;
        mObserver.assertNotChanged();
        mObserver.assertNotInvalidated();
        mSuggestions.addCorpusResults(
                Collections.singletonList(corpus.getSuggestions("foo", 50, true)));
        mObserver.assertChanged();
        mObserver.assertNotInvalidated();
    }

}
