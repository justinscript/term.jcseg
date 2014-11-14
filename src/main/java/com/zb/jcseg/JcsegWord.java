/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg;

import com.zb.jcseg.core.IWord;

/**
 * word class for jcseg has implements IWord interface
 * 
 * @author zxc Sep 3, 2014 2:25:43 PM
 */
public class JcsegWord implements IWord, Cloneable {

    private String   value;
    private int      fre        = 0;
    private int      type;
    private int      position;
    private String   pinyin     = null;
    private String[] partspeech = null;
    private String[] syn        = null;

    public JcsegWord(String value, int type) {
        this.value = value;
        this.type = type;
    }

    public JcsegWord(String value, int fre, int type) {
        this.value = value;
        this.fre = fre;
        this.type = type;
    }

    /**
     * @see IWord#getValue()
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * @see IWord#getLength()
     */
    @Override
    public int getLength() {
        return value.length();
    }

    /**
     * @see IWord#getFrequency()
     */
    @Override
    public int getFrequency() {
        return fre;
    }

    /**
     * @see IWord#getType()
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * @see IWord#setPosition(int)
     */
    @Override
    public void setPosition(int pos) {
        position = pos;
    }

    /**
     * @see IWord#getPosition()
     */
    public int getPosition() {
        return position;
    }

    /**
     * @see IWord#getPinying()
     */
    @Override
    public String getPinyin() {
        return pinyin;
    }

    /**
     * @see IWord#getSyn()
     */
    @Override
    public String[] getSyn() {
        return syn;
    }

    @Override
    public void setSyn(String[] syn) {
        this.syn = syn;
    }

    /**
     * @see IWord#getPartSpeech()
     */
    @Override
    public String[] getPartSpeech() {
        return partspeech;
    }

    @Override
    public void setPartSpeech(String[] partspeech) {
        this.partspeech = partspeech;
    }

    /**
     * @see IWord#setPinying(String)
     */
    public void setPinyin(String py) {
        pinyin = py;
    }

    /**
     * @see IWord#addPartSpeech(String );
     */
    @Override
    public void addPartSpeech(String ps) {
        if (partspeech == null) {
            partspeech = new String[1];
            partspeech[0] = ps;
        } else {
            String[] bak = partspeech;
            partspeech = new String[partspeech.length + 1];
            int j;
            for (j = 0; j < bak.length; j++)
                partspeech[j] = bak[j];
            partspeech[j] = ps;
            bak = null;
        }
    }

    /**
     * @see IWord#addSyn(String)
     */
    @Override
    public void addSyn(String s) {
        if (syn == null) {
            syn = new String[1];
            syn[0] = s;
        } else {
            String[] tycA = syn;
            syn = new String[syn.length + 1];
            int j;
            for (j = 0; j < tycA.length; j++)
                syn[j] = tycA[j];
            syn[j] = s;
            tycA = null;
        }
    }

    /**
     * @see Object#equals(Object)
     * @see IWord#equals(Object)
     */
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof IWord) {
            IWord word = (IWord) o;
            boolean bool = word.getValue().equalsIgnoreCase(this.getValue());
            /*
             * value equals and the type of the word must be equals too, for there is many words in different lexicon
             * with a same value but in different use.
             */
            return (bool && (word.getType() == this.getType()));
        }

        return false;
    }

    /**
     * Interface to clone the current object
     * 
     * @return IWord
     */
    @Override
    public IWord clone() {
        IWord w = null;
        try {
            w = (IWord) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return w;
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(value);
        sb.append('/');
        // append the cx
        if (partspeech != null) {
            for (int j = 0; j < partspeech.length; j++) {
                if (j == 0) sb.append(partspeech[j]);
                else {
                    sb.append(',');
                    sb.append(partspeech[j]);
                }
            }
        } else sb.append("null");
        sb.append('/');
        sb.append(pinyin);
        sb.append('/');
        // append the tyc
        if (syn != null) {
            for (int j = 0; j < syn.length; j++) {
                if (j == 0) sb.append(syn[j]);
                else {
                    sb.append(',');
                    sb.append(syn[j]);
                }
            }
        } else sb.append("null");

        if (value.length() == 1) {
            sb.append('/');
            sb.append(fre);
        }

        return sb.toString();
    }
}
