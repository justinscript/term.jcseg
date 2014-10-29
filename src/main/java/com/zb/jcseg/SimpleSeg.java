/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg;

import java.io.IOException;
import java.io.Reader;

import com.zb.jcseg.core.JcsegDictionary;
import com.zb.jcseg.core.IChunk;
import com.zb.jcseg.core.IWord;
import com.zb.jcseg.core.JcsegTaskConfig;

/**
 * simplex segment for JCSeg, has extend from ASegment. <br />
 * 
 * @author zxc Sep 3, 2014 2:25:43 PM
 */
public class SimpleSeg extends ASegment {

    public SimpleSeg(JcsegTaskConfig config, JcsegDictionary dic) throws IOException {
        super(config, dic);
    }

    public SimpleSeg(Reader input, JcsegTaskConfig config, JcsegDictionary dic) throws IOException {
        super(input, config, dic);
    }

    /**
     * @see ASegment#getBestCJKChunk(char[], int)
     */
    @Override
    public IChunk getBestCJKChunk(char[] chars, int index) {
        IWord[] words = getNextMatch(chars, index);
        return new Chunk(new IWord[] { words[words.length - 1] });
    }
}
