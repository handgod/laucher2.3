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

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for RankAwarePromoter
 */
@SmallTest
public class RankAwarePromoterTest extends AndroidTestCase {
    public static final int NUM_SUGGESTIONS_ABOVE_KEYBOARD = 4;
    public static final int MAX_PROMOTED_CORPORA = 3;
    public static final int MAX_PROMOTED_SUGGESTIONS = 8;
    public static final String TEST_QUERY = "query";

    private CorpusRanker mRanker;
    private RankAwarePromoter mPromoter;

    @Override
    public void setUp() {
        Corpora corpora = createMockCorpora(5, MAX_PROMOTED_CORPORA);
        mRanker = new LexicographicalCorpusRanker(corpora);
        mPromoter = new RankAwarePromoter(new Config(mContext){
            @Override
            public int getNumSuggestionsAboveKeyboard() {
                return NUM_SUGGESTIONS_ABOVE_KEYBOARD;
            }
        }, corpora);
    }

    public void testPromotesExpectedSuggestions() {
        ArrayList<CorpusResult> suggestions = getSuggestions(TEST_QUERY);
        ListSuggestionCursor promoted = new ListSuggestionCursor(TEST_QUERY);
        mPromoter.pickPromoted(null, suggestions, MAX_PROMOTED_SUGGESTIONS, promoted);

        assertEquals(MAX_PROMOTED_SUGGESTIONS, promoted.getCount());

        int[] expectedSource = {0, 1, 2, 0, 1, 2, 3, 4};
        int[] expectedSuggestion = {1, 1, 1, 2, 2, 2, 1, 1};

        for (int i = 0; i < promoted.getCount(); i++) {
            promoted.moveTo(i);
            assertEquals("Source in position " + i,
                    "MockSource Source" + expectedSource[i],
                    promoted.getSuggestionSource().getLabel());
            assertEquals("Suggestion in position " + i,
                    TEST_QUERY + "_" + expectedSuggestion[i],
                    promoted.getSuggestionText1());
        }
    }

    private List<Corpus> getRankedCorpora() {
        return mRanker.getRankedCorpora();
    }

    private ArrayList<CorpusResult> getSuggestions(String query) {
        ArrayList<CorpusResult> suggestions = new ArrayList<CorpusResult>();
        for (Corpus corpus : getRankedCorpora()) {
            suggestions.add(corpus.getSuggestions(query, 10, false));
        }
        return suggestions;
    }

    private static MockCorpora createMockCorpora(int count, int defaultCount) {
        MockCorpora corpora = new MockCorpora();
        for (int i = 0; i < count; i++) {
            Source mockSource = new MockSource("Source" + i);
            Corpus mockCorpus = new MockCorpus(mockSource, i < defaultCount);
            corpora.addCorpus(mockCorpus);
        }
        return corpora;
    }
}
