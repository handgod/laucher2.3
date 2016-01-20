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

package com.android.quicksearchbox.ui;

import com.android.quicksearchbox.Corpus;
import com.android.quicksearchbox.CorpusRanker;

import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for showing a list of sources in the source selection activity.
 */
public class CorporaAdapter extends BaseAdapter {

    private static final String TAG = "CorporaAdapter";
    private static final boolean DBG = false;

    private final CorpusViewFactory mViewFactory;

    private final CorpusRanker mRanker;

    private final DataSetObserver mCorporaObserver = new CorporaObserver();

    private List<Corpus> mRankedEnabledCorpora;

    private boolean mGridView;

    private CorporaAdapter(CorpusViewFactory viewFactory,
            CorpusRanker ranker, boolean gridView) {
        mViewFactory = viewFactory;
        mRanker = ranker;
        mGridView = gridView;
        mRanker.registerDataSetObserver(mCorporaObserver);
        updateCorpora();
    }

    public static CorporaAdapter createListAdapter(CorpusViewFactory viewFactory,
            CorpusRanker ranker) {
        return new CorporaAdapter(viewFactory, ranker, false);
    }

    public static CorporaAdapter createGridAdapter(CorpusViewFactory viewFactory,
            CorpusRanker ranker) {
        return new CorporaAdapter(viewFactory, ranker, true);
    }

    private void updateCorpora() {
        mRankedEnabledCorpora = new ArrayList<Corpus>();
        for (Corpus corpus : mRanker.getRankedCorpora()) {
            if (!corpus.isCorpusHidden()) {
                mRankedEnabledCorpora.add(corpus);
            }
        }
        notifyDataSetChanged();
    }

    public void close() {
        mRanker.unregisterDataSetObserver(mCorporaObserver);
    }

    public int getCount() {
        return 1 + mRankedEnabledCorpora.size();
    }

    public Corpus getItem(int position) {
        if (position == 0) {
            return null;
        } else {
            return mRankedEnabledCorpora.get(position - 1);
        }
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * Gets the position of the given corpus.
     */
    public int getCorpusPosition(Corpus corpus) {
        if (corpus == null) {
            return 0;
        }
        int count = getCount();
        for (int i = 0; i < count; i++) {
            if (corpus.equals(getItem(i))) {
                return i;
            }
        }
        Log.w(TAG, "Corpus not in adapter: " + corpus);
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CorpusView view = (CorpusView) convertView;
        if (view == null) {
            view = createView(parent);
        }
        Corpus corpus = getItem(position);
        Drawable icon;
        CharSequence label;
        if (corpus == null) {
            icon = mViewFactory.getGlobalSearchIcon();
            label = mViewFactory.getGlobalSearchLabel();
        } else {
            icon = corpus.getCorpusIcon();
            label = corpus.getLabel();
        }
        if (DBG) Log.d(TAG, "Binding " + position + ", label=" + label);
        view.setIcon(icon);
        view.setLabel(label);
        return view;
    }

    protected CorpusView createView(ViewGroup parent) {
        if (mGridView) {
            return mViewFactory.createGridCorpusView(parent);
        } else {
            return mViewFactory.createListCorpusView(parent);
        }
    }

    private class CorporaObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            updateCorpora();
        }

        @Override
        public void onInvalidated() {
            updateCorpora();
        }
    }

}
