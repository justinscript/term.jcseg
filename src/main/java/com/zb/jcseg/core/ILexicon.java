/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.core;

/**
 * lexicon configuration class.
 * 
 * @author zxc Sep 3, 2014 2:15:09 PM
 */
public interface ILexicon {

    public static final int T_LEN            = 11;

    /**
     * China,JPanese,Korean words
     */
    public static final int CJK_WORDS        = 0;

    /**
     * chinese single units
     */
    public static final int CJK_UNITS        = 1;

    /**
     * chinese and english mix word. like B超,SIM卡.
     */
    public static final int EC_MIXED_WORD    = 2;

    /**
     * chinese last name.
     */
    public static final int CN_LNAME         = 3;

    /**
     * chinese single name.
     */
    public static final int CN_SNAME         = 4;

    /**
     * fisrt word of chinese double name.
     */
    public static final int CN_DNAME_1       = 5;

    /**
     * sencond word of chinese double name.
     */
    public static final int CN_DNAME_2       = 6;

    /**
     * the adorn(修饰) char before the last name.
     */
    public static final int CN_LNAME_ADORN   = 7;
    public static final int EN_PUN_WORDS     = 8;
    public static final int STOP_WORD        = 9;
    public static final int CE_MIXED_WORD    = 10;

    /**
     * unmatched word
     */
    public static final int UNMATCH_CJK_WORD = 10;
}
