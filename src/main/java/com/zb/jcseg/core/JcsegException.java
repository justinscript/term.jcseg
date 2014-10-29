/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.core;

/**
 * JCSeg exception class
 * 
 * @author zxc Sep 3, 2014 2:15:09 PM
 */
public class JcsegException extends Exception {

    private static final long serialVersionUID = 4495714680349884838L;

    public JcsegException(String info) {
        super(info);
    }

    public JcsegException(Throwable res) {
        super(res);
    }

    public JcsegException(String info, Throwable res) {
        super(info, res);
    }
}
