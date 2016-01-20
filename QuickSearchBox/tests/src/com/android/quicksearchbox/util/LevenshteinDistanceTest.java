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

package com.android.quicksearchbox.util;

import com.android.quicksearchbox.util.LevenshteinDistance.EditOperation;
import com.android.quicksearchbox.util.LevenshteinDistance.Token;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * Tests for class {@link LevenshteinDistance}.
 */
@SmallTest
public class LevenshteinDistanceTest extends AndroidTestCase {
    // to make the tests less verbose:
    private static final int INSERT = LevenshteinDistance.EDIT_INSERT;
    private static final int DELETE = LevenshteinDistance.EDIT_DELETE;
    private static final int REPLACE = LevenshteinDistance.EDIT_REPLACE;
    private static final int UNCHANGED = LevenshteinDistance.EDIT_UNCHANGED;

    private void verifyTargetOperations(String[] source, String[] target, int[] expectedOps,
            int expectedDistance) {

        Token[] sourceTokens = makeTokens(source);
        Token[] targetTokens = makeTokens(target);

        assertEquals("test error", target.length, expectedOps.length);
        LevenshteinDistance distance = new LevenshteinDistance(sourceTokens, targetTokens);

        assertEquals(expectedDistance, distance.calculate());
        EditOperation[] ops = distance.getTargetOperations();
        assertEquals(expectedOps.length, ops.length);
        for (int i = 0; i < ops.length; ++i) {
            assertEquals("Token " + i + " '" + target[i] + "' has wrong operation",
                    expectedOps[i], ops[i].getType());
            if (expectedOps[i] == UNCHANGED) {
                assertEquals(source[ops[i].getPosition()], target[i]);
            } else if (expectedOps[i] == REPLACE) {
                assertFalse(source[ops[i].getPosition()].equals(target[i]));
            }
        }
    }

    private Token[] makeTokens(String[] strings) {
        Token[] tokens = new Token[strings.length];
        for (int i = 0; i < strings.length; i++) {
            String str = strings[i];
            tokens[i] = new Token(str.toCharArray(), 0, str.length());
        }
        return tokens;
    }

    public void testGetTargetOperationsEmptySource() {
        verifyTargetOperations(
                new String[]{},
                new String[]{},
                new int[]{},
                0);

        verifyTargetOperations(
                new String[]{},
                new String[]{"goo", "ball"},
                new int[]{INSERT, INSERT},
                2);
    }

    public void testGetTargetOperationsEmptyTarget() {
        verifyTargetOperations(
                new String[]{"delete"},
                new String[]{},
                new int[]   {},
                1);

        verifyTargetOperations(
                new String[]{"delete", "me"},
                new String[]{},
                new int[]   {},
                2);
    }

    public void testGetTargetOperationsReplacement() {
        verifyTargetOperations(
                new String[]{"dennis"},
                new String[]{"gnasher"},
                new int[]   {REPLACE},
                1);

        verifyTargetOperations(
                new String[]{"angry", "viking"},
                new String[]{"happy", "kitten"},
                new int[]   {REPLACE, REPLACE},
                2);
    }

    public void testGetTargetOperationsUnchanged() {
        verifyTargetOperations(
                new String[]{"tweedledee"},
                new String[]{"tweedledee"},
                new int[]   {UNCHANGED},
                0);

        verifyTargetOperations(
                new String[]{"tweedledee", "tweedledum"},
                new String[]{"tweedledee", "tweedledum"},
                new int[]   {UNCHANGED,     UNCHANGED},
                0);
    }

    public void testGetTargetOperationsDuplicateTokens() {
        String rhubarb = "rhubarb";
        verifyTargetOperations(
                new String[]{rhubarb},
                new String[]{rhubarb,   rhubarb},
                new int[]   {UNCHANGED, INSERT},
                1);

        verifyTargetOperations(
                new String[]{rhubarb,   rhubarb},
                new String[]{rhubarb,   rhubarb},
                new int[]   {UNCHANGED, UNCHANGED},
                0);

        verifyTargetOperations(
                new String[]{rhubarb,   rhubarb},
                new String[]{rhubarb,   rhubarb,   rhubarb},
                new int[]   {UNCHANGED, UNCHANGED, INSERT},
                1);
    }

}
