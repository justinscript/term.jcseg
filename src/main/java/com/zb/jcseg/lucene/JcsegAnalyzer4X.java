/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

import com.zb.jcseg.core.JcsegDictionary;
import com.zb.jcseg.core.JcsegDictionaryFactory;
import com.zb.jcseg.core.JcsegTaskConfig;

/**
 * jcseg analyzer for lucene.
 * 
 * @author zxc Sep 3, 2014 2:19:43 PM
 */
public class JcsegAnalyzer4X extends Analyzer {

    private int             mode;
    private JcsegTaskConfig config = null;
    private JcsegDictionary     dic    = null;

    public JcsegAnalyzer4X(int mode) {
        this.mode = mode;

        // initialize the task config and the dictionary
        config = new JcsegTaskConfig();
        dic = JcsegDictionaryFactory.createDefaultDictionary(config);
    }

    public void setConfig(JcsegTaskConfig config) {
        this.config = config;
    }

    public void setDict(JcsegDictionary dic) {
        this.dic = dic;
    }

    public JcsegTaskConfig getTaskConfig() {
        return config;
    }

    public JcsegDictionary getDict() {
        return dic;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        try {
            Tokenizer source = new JcsegTokenizer(reader, mode, config, dic);
            return new TokenStreamComponents(source, new JcsegFilter(source));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
