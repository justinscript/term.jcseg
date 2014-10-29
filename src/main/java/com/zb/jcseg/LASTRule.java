/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg;

import com.zb.jcseg.core.IChunk;
import com.zb.jcseg.core.ILastRule;
import com.zb.jcseg.core.IRule;

/**
 * the last rule. -clear the ambiguity after the four rule.
 * 
 * @author zxc Sep 3, 2014 2:25:43 PM
 */
public class LASTRule implements ILastRule {

    /**
     * maxmum match rule instance.
     */
    private static LASTRule __instance = null;

    private LASTRule() {

    }

    /**
     * return the quote to the maximum match instance.
     * 
     * @return MMRule
     */
    public static LASTRule createRule() {
        if (__instance == null) __instance = new LASTRule();
        return __instance;
    }

    /**
     * last rule interface. here we simply return the first chunk.
     * 
     * @see IRule#call(IChunk[])
     */
    @Override
    public IChunk call(IChunk[] chunks) {
        return chunks[0];
    }
}
