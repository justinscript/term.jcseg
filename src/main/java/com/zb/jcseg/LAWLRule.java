/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg;

import java.util.ArrayList;

import com.zb.jcseg.core.IChunk;
import com.zb.jcseg.core.IRule;

/**
 * the second filter rule - largest average word length. this rule will return the chunks that own the largest average
 * word length.
 * 
 * @author zxc Sep 3, 2014 2:25:43 PM
 */
public class LAWLRule implements IRule {

    /**
     * maxmum match rule instance.
     */
    private static LAWLRule __instance = null;

    /**
     * return the quote to the maximum match instance.
     * 
     * @return MMRule
     */
    public static LAWLRule createRule() {
        if (__instance == null) __instance = new LAWLRule();
        return __instance;
    }

    private LAWLRule() {
    }

    /**
     * interface for largest average word length.
     * 
     * @see IRule#call(IChunk[])
     */
    @Override
    public IChunk[] call(IChunk[] chunks) {

        double largetAverage = chunks[0].getAverageWordsLength();
        int j;

        // find the largest average word length
        for (j = 1; j < chunks.length; j++) {
            if (chunks[j].getAverageWordsLength() > largetAverage) largetAverage = chunks[j].getAverageWordsLength();
        }

        // get the items that the average word length equals to
        // the max's.
        ArrayList<IChunk> chunkArr = new ArrayList<IChunk>(chunks.length);
        for (j = 0; j < chunks.length; j++) {
            if (chunks[j].getAverageWordsLength() == largetAverage) chunkArr.add(chunks[j]);
        }

        IChunk[] lchunk = new IChunk[chunkArr.size()];
        chunkArr.toArray(lchunk);
        chunkArr.clear();

        return lchunk;
    }
}
