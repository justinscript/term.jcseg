/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.solr;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import com.zb.jcseg.core.JcsegDictionary;
import com.zb.jcseg.core.ISegment;
import com.zb.jcseg.core.IWord;
import com.zb.jcseg.core.JcsegException;
import com.zb.jcseg.core.JcsegTaskConfig;
import com.zb.jcseg.core.SegmentFactory;

/**
 * jcsge tokennizer for lucene.
 * 
 * @author zxc Sep 3, 2014 2:19:43 PM
 */
public class JcsegTokenizer extends Tokenizer {

    private ISegment          segmentor;

    private CharTermAttribute termAtt;
    private OffsetAttribute   offsetAtt;

    public JcsegTokenizer(Reader input, int mode, JcsegTaskConfig config, JcsegDictionary dic) throws JcsegException,
                                                                                          IOException {
        super(input);

        segmentor = SegmentFactory.createJcseg(mode, new Object[] { config, dic });
        segmentor.reset(input);
        termAtt = addAttribute(CharTermAttribute.class);
        offsetAtt = addAttribute(OffsetAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();
        IWord word = segmentor.next();
        if (word != null) {
            termAtt.append(word.getValue());
            // termAtt.copyBuffer(word.getValue(), 0, word.getValue().length);
            termAtt.setLength(word.getLength());
            offsetAtt.setOffset(word.getPosition(), word.getPosition() + word.getLength());
            return true;
        } else {
            end();
            return false;
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        segmentor.reset(input);
    }
}
