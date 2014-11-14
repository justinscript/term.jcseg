/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.core;

import java.io.IOException;
import java.io.Reader;

/**
 * Jcseg segment interface
 * 
 * @author zxc Sep 3, 2014 2:15:09 PM
 */
public interface ISegment {

    // Wether to check the chinese and english mixed word.
    public static final int CHECK_CE_MASk = 1 << 0;
    // Wether to check the chinese fraction.
    public static final int CHECK_CF_MASK = 1 << 1;
    // Wether to start the latin secondary segmentation.
    public static final int START_SS_MASK = 1 << 2;

    /**
     * reset the reader
     * 
     * @param input
     */
    public void reset(Reader input) throws IOException;

    /**
     * get the current length of the stream
     * 
     * @return int
     */
    public int getStreamPosition();

    /**
     * segment a word from a char array from a specified position.
     * 
     * @return IWord
     */
    public IWord next() throws IOException;
}
