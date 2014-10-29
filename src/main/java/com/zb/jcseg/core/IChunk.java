/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.core;

/**
 * chunk interface for JCSeg. the most important concept for the mmseg chinese segment alogorithm.
 * 
 * @author zxc Sep 3, 2014 2:15:09 PM
 */
public interface IChunk {

    /**
     * get the all the words in the chunk.
     * 
     * @return IWord[]
     */
    public IWord[] getWords();

    /**
     * return the average word length for all the chunks.
     * 
     * @return double
     */
    public double getAverageWordsLength();

    /**
     * return the variance of all the words in all the chunks.
     * 
     * @return double
     */
    public double getWordsVariance();

    /**
     * return the degree of morphemic freedom for all the single words.
     * 
     * @return double
     */
    public double getSingleWordsMorphemicFreedom();

    /**
     * return the length of the chunk(the number of the word)
     * 
     * @return int
     */
    public int getLength();
}
