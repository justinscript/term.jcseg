/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.core;

/**
 * JCSeg Dictionary configuration exception class
 * 
 * @author zxc Sep 3, 2014 2:15:09 PM
 */
public class LexiconException extends Exception {

    private static final long serialVersionUID = 3794928123652720865L;

    public LexiconException(String info) {
        super(info);
    }

    public LexiconException(Throwable res) {
        super(res);
    }

    public LexiconException(String info, Throwable res) {
        super(info, res);
    }
}
