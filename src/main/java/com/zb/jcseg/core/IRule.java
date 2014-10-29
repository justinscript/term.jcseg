/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.core;

/**
 * filter rule interface. the most important concept for mmseg chinese segment algorithm.
 * 
 * @author zxc Sep 3, 2014 2:15:09 PM
 */
public interface IRule {

    /**
     * do the filter work
     * 
     * @param chunks
     * @return IChunk[]
     */
    public IChunk[] call(IChunk[] chunks);
}
