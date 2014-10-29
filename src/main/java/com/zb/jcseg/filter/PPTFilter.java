/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * a class to deal with the text bettween the pair punctuations. <br />
 * 
 * @author zxc Sep 3, 2014 2:19:43 PM
 */
public class PPTFilter {

    private static final Character[]         PAIR_PUNCTUATION = {
                                                              /* '“', '”', '‘', '’', */'《', '》', '『', '』', '【', '】' };

    private static Map<Character, Character> pairPunctuation  = null;

    static {
        pairPunctuation = new HashMap<Character, Character>((int) (PAIR_PUNCTUATION.length / 1.7) + 1, 0.85f);
        for (int j = 0; j < PAIR_PUNCTUATION.length; j += 2)
            pairPunctuation.put(PAIR_PUNCTUATION[j], PAIR_PUNCTUATION[j + 1]);
    }

    /**
     * check the given char is pair punctuation or not. <br />
     * 
     * @param c <br />
     * @param boolean true for it is and false for not.
     */
    public static boolean isPairPunctuation(char c) {
        return pairPunctuation.containsKey(c);
    }

    /**
     * get the pair punctuation' pair.
     * 
     * @param c
     * @return char
     */
    public static char getPunctuationPair(char c) {
        return pairPunctuation.get(c);
    }
}
