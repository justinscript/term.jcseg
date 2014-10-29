/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg;

import java.util.HashMap;
// import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zb.jcseg.core.JcsegDictionary;
import com.zb.jcseg.core.ILexicon;
import com.zb.jcseg.core.IWord;
// import com.zb.jcseg.core.JHashMap;
import com.zb.jcseg.core.JcsegTaskConfig;

/**
 * Dictionary class. <br />
 * 
 * @author zxc Sep 3, 2014 2:25:43 PM
 */
public class Dictionary extends JcsegDictionary {

    /** hash table for the words */
    private Map<String, IWord>[] dics = null;

    @SuppressWarnings("unchecked")
    public Dictionary(JcsegTaskConfig config, Boolean sync) {
        super(config, sync);
        dics = new Map[ILexicon.T_LEN];
        if (this.sync) {
            for (int j = 0; j < ILexicon.T_LEN; j++)
                dics[j] = new ConcurrentHashMap<String, IWord>(16, 0.80F);
        } else {
            for (int j = 0; j < ILexicon.T_LEN; j++)
                dics[j] = new HashMap<String, IWord>(16, 0.80F);
        }
    }

    /**
     * @see JcsegDictionary#match(int, String)
     */
    @Override
    public boolean match(int t, String key) {
        if (t < 0 || t >= ILexicon.T_LEN) return false;
        return dics[t].containsKey(key);
    }

    /**
     * @see JcsegDictionary#add(int, String, int)
     */
    @Override
    public void add(int t, String key, int type) {
        if (t < 0 || t >= ILexicon.T_LEN) return;
        if (dics[t].get(key) == null) dics[t].put(key, new JcsegWord(key, type));
    }

    /**
     * @see JcsegDictionary#add(int, String, int, int)
     */
    @Override
    public void add(int t, String key, int fre, int type) {
        if (t < 0 || t >= ILexicon.T_LEN) return;
        if (dics[t].get(key) == null) dics[t].put(key, new JcsegWord(key, fre, type));
    }

    /**
     * @see JcsegDictionary#get(int, String)
     */
    @Override
    public IWord get(int t, String key) {
        if (t < 0 || t >= ILexicon.T_LEN) return null;
        return dics[t].get(key);
    }

    /**
     * @see JcsegDictionary#remove(int, String)
     */
    @Override
    public void remove(int t, String key) {
        if (t < 0 || t >= ILexicon.T_LEN) return;
        dics[t].remove(key);
    }

    /**
     * @see JcsegDictionary#size(int)
     */
    @Override
    public int size(int t) {
        if (t < 0 || t >= ILexicon.T_LEN) return 0;
        return dics[t].size();
    }
}
