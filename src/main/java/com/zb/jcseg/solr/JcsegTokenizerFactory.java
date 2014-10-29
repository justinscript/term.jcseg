/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.solr;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;

import com.zb.jcseg.core.JcsegDictionary;
import com.zb.jcseg.core.JcsegDictionaryFactory;
import com.zb.jcseg.core.JcsegException;
import com.zb.jcseg.core.JcsegTaskConfig;

/**
 * jcseg solr tokenizer factory class . <br />
 * 
 * @author zxc Sep 3, 2014 2:19:43 PM
 */
public class JcsegTokenizerFactory extends TokenizerFactory {

    private int             mode;
    private JcsegTaskConfig config = null;
    private JcsegDictionary dic    = null;

    /**
     * set the mode arguments in the schema.xml configuration file to change the segment mode for jcseg . <br />
     * 
     * @see TokenizerFactory#TokenizerFactory(Map<String, String)
     */
    public JcsegTokenizerFactory(Map<String, String> args) {
        super(args);

        String _mode = getOriginalArgs().get("mode");
        String dicPath = getOriginalArgs().get("dicPath");
        if (_mode == null) {
            mode = JcsegTaskConfig.COMPLEX_MODE;
        } else {
            _mode = _mode.toLowerCase();
            if ("simple".equals(_mode)) {
                mode = JcsegTaskConfig.SIMPLE_MODE;
            } else mode = JcsegTaskConfig.COMPLEX_MODE;
        }

        // initialize the task config and the dictionary
        config = new JcsegTaskConfig();
        if (StringUtils.isNotEmpty(dicPath)) {
            config.setLexPath(dicPath);
        }
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
    public Tokenizer create(AttributeFactory factory, Reader input) {
        try {
            return new JcsegTokenizer(input, mode, config, dic);
        } catch (JcsegException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
