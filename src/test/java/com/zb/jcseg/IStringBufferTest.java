/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg;

import com.zb.jcseg.util.IStringBuffer;

/**
 * @author zxc Sep 3, 2014 2:20:51 PM
 */
public class IStringBufferTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        IStringBuffer isb = new IStringBuffer();

        long s = System.currentTimeMillis();
        for (int j = 0; j < 10; j++) {
            isb.append(j + "");
            isb.append(',');
        }
        long e = System.currentTimeMillis();
        System.out.println("Done, cost:" + (e - s) + "msec, " + isb.toString());

        isb.clear();
        s = System.currentTimeMillis();
        for (int j = 0; j < 10; j++) {
            isb.append("" + j);
            isb.append(',');
        }
        e = System.currentTimeMillis();
        System.out.println("charAt(4)=" + isb.charAt(4));
        isb.deleteCharAt(4);
        System.out.println("Done, cost:" + (e - s) + "msec, " + isb.toString());
    }
}
