/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * token filter class , according the to lucene API, this will be remove in in Lucene 5.0 . <br />
 * 
 * @author zxc Sep 3, 2014 2:19:43 PM
 */
public class JcsegFilter extends TokenFilter {

    // private CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    protected JcsegFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            // char text[] = termAtt.buffer();
            // int termLength = termAtt.length();

            return true;
        }
        return false;
    }
}
