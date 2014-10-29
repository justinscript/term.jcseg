/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.util;

import java.io.File;

/**
 * static method for jcseg.
 * 
 * @author zxc Sep 3, 2014 2:19:43 PM
 */
public class JcsegUtil {

    /**
     * get the absolute parent path for the jar file.
     * 
     * @param o
     * @return String
     */
    public static String getJarHome(Object o) {
        String path = o.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File jarFile = new File(path);
        return jarFile.getParentFile().getAbsolutePath();
    }
}
