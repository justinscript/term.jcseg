/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.core;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;

import com.zb.jcseg.ComplexSeg;
import com.zb.jcseg.SimpleSeg;

/**
 * Segment factory to create singleton ISegment object. a path of the class that has implemented the ISegment interface
 * must be given first.
 * 
 * @author zxc Sep 3, 2014 2:15:09 PM
 */
public class SegmentFactory {

    /**
     * load the ISegment class with the given path
     * 
     * @param __segClass
     * @return ISegment
     */
    public static ISegment createSegment(String __segClass, Class<?> paramtypes[], Object args[]) {
        ISegment seg = null;
        try {
            Class<?> _class = Class.forName(__segClass);
            Constructor<?> cons = _class.getConstructor(paramtypes);
            seg = (ISegment) cons.newInstance(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("can't load the ISegment implements class " + "with path [" + __segClass + "] ");
        }
        return seg;
    }

    public static ISegment createJcseg(JcsegTaskConfig config, JcsegDictionary dic, int mode) throws JcsegException {
        ISegment seg = null;
        if (mode == JcsegTaskConfig.SIMPLE_MODE) {
            try {
                seg = new SimpleSeg(config, dic);
            } catch (IOException e) {
                e.printStackTrace();
                throw new JcsegException("创建简单mode分词器失败");
            }
        } else if (mode == JcsegTaskConfig.COMPLEX_MODE) {
            try {
                seg = new ComplexSeg(config, dic);
            } catch (IOException e) {
                e.printStackTrace();
                throw new JcsegException("创建复杂mode分词器失败");
            }
        } else {
            throw new JcsegException("No Such Algorithm Excpetion");
        }
        return seg;
    }

    /**
     * create the specified mode jcseg instance . <br />
     * 
     * @param mode
     * @return ISegment
     * @throws JcsegException
     */
    public static ISegment createJcseg(int mode, Object... args) throws JcsegException {
        String __segClass;
        if (mode == JcsegTaskConfig.SIMPLE_MODE) __segClass = "com.zb.jcseg.SimpleSeg";
        else if (mode == JcsegTaskConfig.COMPLEX_MODE) __segClass = "com.zb.jcseg.ComplexSeg";
        else throw new JcsegException("No Such Algorithm Excpetion");

        Class<?>[] _paramtype = null;
        if (args.length == 2) {
            _paramtype = new Class[] { JcsegTaskConfig.class, JcsegDictionary.class };
        } else if (args.length == 3) {
            _paramtype = new Class[] { Reader.class, JcsegTaskConfig.class, JcsegDictionary.class };
        } else {
            throw new JcsegException("length of the arguments should be 2 or 3");
        }

        return createSegment(__segClass, _paramtype, args);
    }

}
