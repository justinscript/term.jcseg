/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.util;

/**
 * int first in first out queue base on single link.
 * 
 * @author zxc Nov 14, 2014 1:10:02 PM
 */
public class IIntFIFO {

    // size of the queue
    private int   size;

    // head entry of the queue
    private Entry head;

    public IIntFIFO() {
        size = 0;
        head = new Entry(-1, null);
    }

    /**
     * add a new item to the queue
     * 
     * @param data
     * @return boolean
     */
    public boolean enQueue(int data) {
        Entry o = new Entry(data, head.next);
        head.next = o;
        size++;

        return true;
    }

    /**
     * remove the first item from the queue
     * 
     * @return int (It not good to return int)
     */
    public int deQueue() {
        if (size == 0) return -1;
        Entry o = head.next;
        head.next = o.next;

        int v = o.data; // backup the data
        o = null; // Let gc do its work
        size--;

        return v;
    }

    /**
     * get the size of the queue
     * 
     * @return int
     */
    public int size() {
        return size;
    }

    /**
     * Item Entry inner class
     */
    public static class Entry {

        public int   data; // entry data
        public Entry next; // next item

        public Entry(int data, Entry next) {
            this.data = data;
            this.next = next;
        }
    }
}
