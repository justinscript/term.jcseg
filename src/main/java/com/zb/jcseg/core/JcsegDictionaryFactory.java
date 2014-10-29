/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.core;

import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * Jcseg Dictionary Factory to create Dictionary instance . <br />
 * a path of the class that has extends the ADictionary class must be given first. <br />
 * 
 * @author zxc Sep 3, 2014 2:15:09 PM
 */
public class JcsegDictionaryFactory {

    private JcsegDictionaryFactory() {

    }

    /**
     * create a new ADictionary instance . <br />
     * 
     * @param __dicClass
     * @return ADictionary
     */
    public static JcsegDictionary createDictionary(String __dicClass, Class<?>[] paramType, Object[] args) {
        try {
            Class<?> _class = Class.forName(__dicClass);
            Constructor<?> cons = _class.getConstructor(paramType);
            return ((JcsegDictionary) cons.newInstance(args));
        } catch (Exception e) {
            System.err.println("can't create the ADictionary instance " + "with classpath [" + __dicClass + "]");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * create a default ADictionary instance of class com.zb.jcseg.Dictionary . <br />
     * 
     * @see Dictionary
     * @return ADictionary
     */
    public static JcsegDictionary createDefaultDictionary(JcsegTaskConfig config, boolean sync) {
        JcsegDictionary dic = createDictionary("com.zb.jcseg.Dictionary", new Class[] { JcsegTaskConfig.class,
                Boolean.class }, new Object[] { config, sync });
        try {
            dic.loadFromLexiconDirectory(config.getLexiconPath());
            if (dic.getConfig().isAutoload()) dic.startAutoload();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dic;
    }

    public static JcsegDictionary createDefaultDictionary(JcsegTaskConfig config) {
        return createDefaultDictionary(config, config.isAutoload());
    }
}
