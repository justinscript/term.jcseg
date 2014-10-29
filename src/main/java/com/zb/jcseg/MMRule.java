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
 * the first filter rule - the maxmum match rule for JCSeg. this rule will return the chunks that own the largest word
 * length.
 * 
 * @author zxc Sep 3, 2014 2:25:43 PM
 */
public class MMRule implements IRule {

    /**
     * maxmum match rule instance.
     */
    private static MMRule __instance = null;

    private MMRule() {

    }

    /**
     * return the quote to the maximum match instance.
     * 
     * @return MMRule
     */
    public static MMRule createRule() {
        if (__instance == null) __instance = new MMRule();
        return __instance;
    }

    /**
     * interface for maximum match rule.
     * 
     * @see IRule#call(IChunk[])
     */
    @Override
    public IChunk[] call(IChunk[] chunks) {
        int maxLength = chunks[0].getLength();
        int j;
        // find the maximum word length
        for (j = 1; j < chunks.length; j++) {
            if (chunks[j].getLength() > maxLength) maxLength = chunks[j].getLength();
        }

        // get the items that the word length equals to
        // the max's length.
        ArrayList<IChunk> chunkArr = new ArrayList<IChunk>(chunks.length);
        for (j = 0; j < chunks.length; j++) {
            if (chunks[j].getLength() == maxLength) chunkArr.add(chunks[j]);
        }

        IChunk[] lchunk = new IChunk[chunkArr.size()];
        chunkArr.toArray(lchunk);
        chunkArr.clear();

        return lchunk;
    }
}
