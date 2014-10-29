/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.zb.jcseg.core.JcsegDictionary;
import com.zb.jcseg.core.IChunk;
import com.zb.jcseg.core.ILexicon;
import com.zb.jcseg.core.ISegment;
import com.zb.jcseg.core.IWord;
import com.zb.jcseg.core.JcsegTaskConfig;
import com.zb.jcseg.filter.CNNMFilter;
import com.zb.jcseg.filter.ENSCFilter;
import com.zb.jcseg.filter.PPTFilter;
import com.zb.jcseg.util.IStringBuffer;
import com.zb.jcseg.util.WordUnionUtils;

/**
 * abstract segment class, implemented ISegment interface <br />
 * implements all the common method that <br />
 * simple segment and Complex segment algorithm both share. <br />
 * 
 * @author zxc Sep 3, 2014 2:25:43 PM
 */
public abstract class ASegment implements ISegment {

    /* current position for the given stream. */
    protected int               idx;
    protected PushbackReader    reader      = null;
    /* CJK word cache poll */
    protected LinkedList<IWord> wordPool    = new LinkedList<IWord>();
    // 预处理CJK词的缓冲池
    protected LinkedList<IWord> preWordPool = new LinkedList<IWord>();
    protected IStringBuffer     isb;
    protected boolean           checkCE     = false;

    /* the dictionary and task config */
    protected JcsegDictionary       dic;
    protected JcsegTaskConfig   config;

    public ASegment(JcsegTaskConfig config, JcsegDictionary dic) throws IOException {
        this(null, config, dic);
    }

    public ASegment(Reader input, JcsegTaskConfig config, JcsegDictionary dic) throws IOException {
        this.config = config;
        this.dic = dic;
        isb = new IStringBuffer(64);
        reset(input);
    }

    /**
     * stream/reader reset.
     * 
     * @param input
     * @throws IOException
     */
    public void reset(Reader input) throws IOException {
        if (input != null) {
            reader = new PushbackReader(new BufferedReader(input), 60);
        }
        idx = -1;
        initilized = false;
    }

    /**
     * read the next char from the current position
     * 
     * @throws IOException
     */
    protected int readNext() throws IOException {
        int c = reader.read();
        if (c != -1) idx++;
        return c;
    }

    /**
     * push back the data to the stream.
     * 
     * @param data
     * @throws IOException
     */
    protected void pushBack(int data) throws IOException {
        reader.unread(data);
        idx--;
    }

    protected void pushBack(char[] data) throws IOException {
        for (int j = data.length - 1; j >= 0; j--) {
            reader.unread(data[j]);
            idx--;
        }
    }

    /**
     * push back string back to stream
     * 
     * @param data
     */
    protected void pushBack(String data) throws IOException {
        if (StringUtils.isBlank(data)) {
            return;
        }
        for (int i = data.length() - 1; i >= 0; i--) {
            reader.unread((int) data.charAt(i));
            idx--;
        }
    }

    @Override
    public int getStreamPosition() {
        return idx + 1;
    }

    /**
     * set the dictionary of the current segmentor. <br />
     * 
     * @param dic
     */
    public void setDict(JcsegDictionary dic) {
        this.dic = dic;
    }

    /**
     * get the current dictionary instance . <br />
     * 
     * @return ADictionary
     */
    public JcsegDictionary getDict() {
        return dic;
    }

    /**
     * set the current task config . <br />
     * 
     * @param config
     */
    public void setConfig(JcsegTaskConfig config) {
        this.config = config;
    }

    /**
     * get the current task config instance. <br />
     * 
     * @param JcsegTaskConfig
     */
    public JcsegTaskConfig getConfig() {
        return config;
    }

    private boolean         initilized = false;
    private Iterator<IWord> wordIterator;

    /**
     * 神医：增加对单字符的合并
     */
    @SuppressWarnings("rawtypes")
    @Override
    public IWord next() throws IOException {
        // 启动智能合并
        if (config.isWiselyUnionWord()) {
            if (!initilized) {
                List<String> wordList = new ArrayList<String>();
                IWord tmp;
                while ((tmp = _next()) != null) {
                    wordList.add(tmp.getValue());
                }
                List<String> tmpList = WordUnionUtils.wiselyCombineWords(wordList);
                List<IWord> tmpWordList = new ArrayList<IWord>(tmpList.size());
                for (Iterator iterator = tmpList.iterator(); iterator.hasNext();) {
                    String string = (String) iterator.next();
                    tmpWordList.add(new JcsegWord(string, IWord.T_CJK_WORD));
                }
                wordIterator = tmpWordList.iterator();
                initilized = true;
            }
            if (wordIterator != null && wordIterator.hasNext()) {
                return wordIterator.next();
            } else {
                return null;
            }
        } else {
            return _next();
        }
    }

    /**
     * @see ISegment#next()
     */
    @SuppressWarnings("unused")
    public IWord _next() throws IOException {
        if (wordPool.size() > 0) {
            return wordPool.removeFirst();
        }
        int c, pos;
        char ch;
        while ((c = readNext()) != -1) {
            ch = (char) c;
            if (ENSCFilter.isWhitespace(c)) {
                continue;
            }
            pos = idx;
            // System.out.println((char)c);
            // 是否为双中日韩字符
            if (isCJKChar(c)) {
                // 处理中日韩字符
                handleCJKword(c, wordPool);
                if (wordPool.size() == 0) {
                    continue;
                }
                return wordPool.removeFirst();
            } else if (!ENSCFilter.isPunctuation(c) && isLetterOrDigit(c)) {
                IWord w = nextLetterOrDigit(c);
                // if ( w == null ) continue;
                // clear the stopwords
                if (config.CLEAR_STOPWORD && dic.match(ILexicon.STOP_WORD, w.getValue())) {
                    continue;
                }
                w.setPosition(pos);
                return w;
            } else if (isLetterNumber(c)) {
                IWord w = new JcsegWord(nextLetterNumber(c), IWord.T_OTHER_NUMBER);
                // clear the stopwords
                if (config.CLEAR_STOPWORD && dic.match(ILexicon.STOP_WORD, w.getValue())) {
                    continue;
                }
                w.setPosition(pos);
                return w;
            } else if (isOtherNumber(c)) {
                IWord w = new JcsegWord(nextOtherNumber(c), IWord.T_OTHER_NUMBER);
                // clear the stopwords
                if (config.CLEAR_STOPWORD && dic.match(ILexicon.STOP_WORD, w.getValue())) {
                    continue;
                }
                return w;
            } else if (PPTFilter.isPairPunctuation((char) c)) {
                String text = getPairPunctuationText(c);
                if (text == null) {
                    continue;
                }
                // clear the stopwords
                if (config.CLEAR_STOPWORD && dic.match(ILexicon.STOP_WORD, text)) {
                    continue;
                }
                IWord w = new JcsegWord(text, ILexicon.CJK_WORDS);
                return w;
            }
        }
        return null;
    }

    //
    // // 处理中日韩字符
    // private void handleCJKword(int c, int pos) throws IOException {
    // // 读取下一段中文字符
    // char[] chars = nextCJKSentence(c);
    // parseChars(pos, chars, wordPool);
    // }

    // 处理中日韩字符
    private void handleCJKword(int c, LinkedList<IWord> myWordPool) throws IOException {
        // 读取下一段中文字符
        char[] chars = nextCJKSentence(c);
        parseChars(chars, myWordPool, true);
    }

    //
    private void parseChars(char[] chars, LinkedList<IWord> myWordPool, boolean canRead/* 是否允许在方法磊调用readCh方法 */)
                                                                                                                throws IOException {
        // 首字符的流位置
        int pos = idx;
        int cjkidx = 0;
        IWord w = null;
        while (cjkidx < chars.length) {
            /*
             * find the next CJK word. the process will be different with the different algorithm
             * @see getBestCJKChunk() from SimpleSeg or ComplexSeg.
             */
            w = null;
            // check if there is chinese numeric
            if (CNNMFilter.isCNNumeric(chars[cjkidx]) > -1 && cjkidx + 1 < chars.length) {
                // get the chinese numeric chars
                String num = nextCNNumeric(chars, cjkidx);
                if (cjkidx + 3 < chars.length && chars[cjkidx + 1] == '分' && chars[cjkidx + 2] == '之'
                    && CNNMFilter.isCNNumeric(chars[cjkidx + 3]) > -1) {
                    w = new JcsegWord(num, IWord.T_CN_NUMERIC);
                    w.setPosition(pos + cjkidx);
                    myWordPool.add(w);

                    /*
                     * Here: Convert the chinese fraction to arabic fraction, if the Config.CNFRA_TO_ARABIC is true.
                     */
                    if (config.CNFRA_TO_ARABIC) {
                        String[] split = num.split("分之");
                        IWord wd = new JcsegWord(CNNMFilter.cnNumericToArabic(split[1], true) + "/"
                                            + CNNMFilter.cnNumericToArabic(split[0], true), IWord.T_CN_NUMERIC);
                        wd.setPosition(w.getPosition());
                        myWordPool.add(wd);
                    }
                } else if (CNNMFilter.isCNNumeric(chars[cjkidx + 1]) > -1
                           || dic.match(ILexicon.CJK_UNITS, chars[cjkidx + 1] + "")) {

                    StringBuilder sb = new StringBuilder();
                    String temp = null;
                    sb.append(num);
                    boolean matched = false;
                    int j;

                    // find the word that made up with the numeric
                    // like: 五四运动
                    for (j = num.length(); (cjkidx + j) < chars.length && j < config.MAX_LENGTH; j++) {
                        sb.append(chars[cjkidx + j]);
                        temp = sb.toString();
                        if (dic.match(ILexicon.CJK_WORDS, temp)) {
                            w = dic.get(ILexicon.CJK_WORDS, temp);
                            num = temp;
                            matched = true;
                        }
                    }

                    IWord wd = null;
                    // find the numeric units
                    if (matched == false && config.CNNUM_TO_ARABIC) {
                        // get the numeric'a arabic
                        String arbic = CNNMFilter.cnNumericToArabic(num, true) + "";

                        if ((cjkidx + num.length()) < chars.length
                            && dic.match(ILexicon.CJK_UNITS, chars[cjkidx + num.length()] + "")) {
                            char units = chars[cjkidx + num.length()];
                            num += units;
                            arbic += units;
                        }

                        wd = new JcsegWord(arbic, IWord.T_CN_NUMERIC);
                        wd.setPosition(pos + cjkidx);
                    }
                    // clear the stop words
                    if (dic.match(ILexicon.STOP_WORD, num)) {
                        cjkidx += num.length();
                        continue;
                    }

                    if (w == null) w = new JcsegWord(num, IWord.T_CN_NUMERIC);
                    w.setPosition(pos + cjkidx);
                    myWordPool.add(w);
                    if (wd != null) myWordPool.add(wd);
                }

                if (w != null) {
                    cjkidx += w.getLength();
                    // add the pinyin to the poll
                    if (config.APPEND_CJK_PINYIN && config.LOAD_CJK_PINYIN && w.getPinyin() != null) {
                        IWord wd = new JcsegWord(w.getPinyin(), IWord.T_CJK_PINYIN);
                        wd.setPosition(w.getPosition());
                        myWordPool.add(wd);
                    }
                    // add the syn words to the poll
                    if (config.APPEND_CJK_SYN && config.LOAD_CJK_SYN && w.getSyn() != null) {
                        IWord wd;
                        for (int j = 0; j < w.getSyn().length; j++) {
                            wd = new JcsegWord(w.getSyn()[j], w.getType());
                            wd.setPosition(w.getPosition());
                            myWordPool.add(wd);
                        }
                    }
                    continue;
                }
            }

            IChunk chunk = getBestCJKChunk(chars, cjkidx);
            // System.out.println(chunk+"\n");
            // w = new Word(chunk.getWords()[0].getValue(), IWord.T_CJK_WORD);
            w = chunk.getWords()[0];

            /* find the chinese name. */
            int T = -1;
            if (config.I_CN_NAME && w.getLength() <= 2 && chunk.getWords().length > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append(w.getValue());
                String str = null;

                // the w is a Chinese last name.
                if (dic.match(ILexicon.CN_LNAME, w.getValue()) && (str = findCHName(chars, 0, chunk)) != null) {
                    T = IWord.T_CN_NAME;
                    sb.append(str);
                }
                // the w is Chinese last name adorn
                else if (dic.match(ILexicon.CN_LNAME_ADORN, w.getValue()) && chunk.getWords()[1].getLength() <= 2
                         && dic.match(ILexicon.CN_LNAME, chunk.getWords()[1].getValue())) {
                    T = IWord.T_CN_NICKNAME;
                    sb.append(chunk.getWords()[1].getValue());
                }
                /*
                 * the length of the w is 2: the last name and the first char make up a word for the double name.
                 */
                /*
                 * else if ( w.getLength() > 1 && findCHName( w, chunk )) { T = IWord.T_CN_NAME;
                 * sb.append(chunk.getWords()[1].getValue().charAt(0)); }
                 */

                if (T != -1) w = new JcsegWord(sb.toString(), T);
            }

            // check the stopwords(clear it when Config.CLEAR_STOPWORD is true)
            if (T == -1 && config.CLEAR_STOPWORD && dic.match(ILexicon.STOP_WORD, w.getValue())) {
                cjkidx += w.getLength();
                continue;
            }

            // reach the end of the chars - the last word
            // check the existence of the chinese and english mixed word
            IWord enAfter = null, ce = null;
            if (cjkidx + w.getLength() >= chars.length && checkCE && canRead) {
                // System.out.println("CE-Word"+w.getValue());
                enAfter = nextLetterOrDigit(readNext());
                if (enAfter.getType() == IWord.T_BASIC_LATIN) {
                    String cestr = w.getValue() + enAfter.getValue();
                    if (dic.match(ILexicon.CE_MIXED_WORD, cestr)) {
                        ce = dic.get(ILexicon.CE_MIXED_WORD, cestr);
                        ce.setPosition(pos + cjkidx);
                        myWordPool.add(ce);
                        cjkidx += w.getLength();
                        enAfter = null;
                    }
                }
            }

            // no ce word found, store the english word
            if (ce == null) {
                w.setPosition(pos + cjkidx);
                myWordPool.add(w);
                cjkidx += w.getLength();
            } else {
                w = ce;
            }

            // add the pinyin to the pool
            if (T == -1 && config.APPEND_CJK_PINYIN && config.LOAD_CJK_PINYIN && w.getPinyin() != null) {
                IWord wd = new JcsegWord(w.getPinyin(), IWord.T_CJK_PINYIN);
                wd.setPosition(w.getPosition());
                myWordPool.add(wd);
            }
            // add the syn words to the pool
            if (T == -1 && config.APPEND_CJK_SYN && config.LOAD_CJK_SYN && w.getSyn() != null) {
                IWord wd;
                for (int j = 0; j < w.getSyn().length; j++) {
                    wd = new JcsegWord(w.getSyn()[j], w.getType());
                    wd.setPosition(w.getPosition());
                    myWordPool.add(wd);
                }
            }

            // handle the after english word
            if (enAfter != null && !(config.CLEAR_STOPWORD && dic.match(ILexicon.STOP_WORD, enAfter.getValue()))) {
                enAfter.setPosition(chars.length);
                myWordPool.add(enAfter);
            }
        }
    }

    /**
     * check the specified char is CJK,Thai... char true will be return if it is, or return false.
     * 
     * @param c
     * @return boolean
     */
    static boolean isCJKChar(int c) {
        if (Character.getType(c) == Character.OTHER_LETTER) return true;
        return false;
    }

    /**
     * check the specified char is a basic latin and russia and greece letter true will be return if it is, or return
     * false.<br />
     * this method can recognize full-width char and letter.<br />
     * 
     * @param c
     * @return boolean
     */
    static boolean isLetterOrDigit(int c) {
        /*
         * int type = Character.getType(c); Character.UnicodeBlock cu = Character.UnicodeBlock.of(c); if ( !
         * Character.isWhitespace(c) && (cu == Character.UnicodeBlock.BASIC_LATIN || type ==
         * Character.DECIMAL_DIGIT_NUMBER || type == Character.LOWERCASE_LETTER || type == Character.UPPERCASE_LETTER ||
         * type == Character.TITLECASE_LETTER || type == Character.MODIFIER_LETTER)) return true; return false;
         */
        return (ENSCFilter.isHalfWidthChar(c) || ENSCFilter.isFullWidthChar(c));
    }

    /**
     * check the specified char is a digit or not. true will return if it is , or return false this method can recognize
     * full-with char.
     * 
     * @param c
     * @return boolean
     */
    static boolean isDigit(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches("-?[0-9]+.?[0-9]+");
        /*
         * for (int j = 0; j < str.length(); j++) { char c = str.charAt(j); if (Character.getType(c) !=
         * Character.DECIMAL_DIGIT_NUMBER) return false; } return true;
         */
    }

    /**
     * check the specified char is Letter number like 'ⅠⅡ' true will be return if it is, or return false. <br />
     * 
     * @param c
     * @return boolean
     */
    static boolean isLetterNumber(int c) {
        if (Character.getType(c) == Character.LETTER_NUMBER) return true;
        return false;
    }

    /**
     * check the specified char is other number like '①⑩⑽㈩' true will be return if it is, or return false. <br />
     * 
     * @param c
     * @return boolean
     */
    static boolean isOtherNumber(int c) {
        if (Character.getType(c) == Character.OTHER_NUMBER) return true;
        return false;
    }

    /**
     * match the next CJK word in the dictionary. <br />
     * 
     * @param chars
     * @param index
     * @return IWord[]
     */
    protected IWord[] getNextMatch(char[] chars, int index) {

        ArrayList<IWord> mList = new ArrayList<IWord>(8);
        // StringBuilder isb = new StringBuilder();
        isb.clear();

        char c = chars[index];
        isb.append(c);
        String temp = isb.toString();
        if (dic.match(ILexicon.CJK_WORDS, temp)) {
            mList.add(dic.get(ILexicon.CJK_WORDS, temp));
        }

        String _key = null;
        for (int j = 1; j < config.MAX_LENGTH && ((j + index) < chars.length); j++) {
            isb.append(chars[j + index]);
            _key = isb.toString();
            if (dic.match(ILexicon.CJK_WORDS, _key)) {
                mList.add(dic.get(ILexicon.CJK_WORDS, _key));
            }
        }

        /*
         * if match no words from the current position to idx+Config.MAX_LENGTH, just return the Word with a value of
         * temp as a unrecognited word.
         */
        if (mList.isEmpty()) {
            mList.add(new JcsegWord(temp, ILexicon.UNMATCH_CJK_WORD));
        }

        /*
         * for ( int j = 0; j < mList.size(); j++ ) { System.out.println(mList.get(j)); }
         */

        IWord[] words = new IWord[mList.size()];
        mList.toArray(words);
        mList.clear();

        return words;
    }

    /**
     * find the chinese name from the position of the given word.
     * 
     * @param chars
     * @param index
     * @param chunk
     * @return IWord
     */
    protected String findCHName(char[] chars, int index, IChunk chunk) {
        StringBuilder isb = new StringBuilder();
        // isb.clear();
        /* there is only two IWords in the chunk. */
        if (chunk.getWords().length == 2) {
            IWord w = chunk.getWords()[1];
            switch (w.getLength()) {
                case 1:
                    if (dic.match(ILexicon.CN_SNAME, w.getValue())) {
                        isb.append(w.getValue());
                        return isb.toString();
                    }
                    return null;
                case 2:
                case 3:
                    /*
                     * there is only two IWords in the chunk. case 2: like: 这本书是陈高的, chunk: 陈_高的 more: 瓜子和坚果,chunk: 和_坚果
                     * (1.6.8前版本有歧义) case 3: 1.double name: the two chars and char after it make up a word. like:
                     * 这本书是陈美丽的, chunk: 陈_美丽的 2.single name: the char and the two chars after it make up a word. -ignore
                     */
                    String d1 = new String(w.getValue().charAt(0) + "");
                    String d2 = new String(w.getValue().charAt(1) + "");
                    if (dic.match(ILexicon.CN_DNAME_1, d1) && dic.match(ILexicon.CN_DNAME_2, d2)) {
                        isb.append(d1);
                        isb.append(d2);
                        return isb.toString();
                    }
                    /*
                     * the name char of the single name and the char after it make up a word.
                     */
                    else if (dic.match(ILexicon.CN_SNAME, d1)) {
                        IWord iw = dic.get(ILexicon.CJK_WORDS, d2);
                        if (iw != null && iw.getFrequency() >= config.NAME_SINGLE_THRESHOLD) {
                            isb.append(d1);
                            return isb.toString();
                        }
                    }
                    return null;
            }
        }
        /* three IWords in the chunk */
        else {
            IWord w1 = chunk.getWords()[1];
            IWord w2 = chunk.getWords()[2];
            switch (w1.getLength()) {
                case 1:
                    /* check if it is a double name first. */
                    if (dic.match(ILexicon.CN_DNAME_1, w1.getValue())) {
                        if (w2.getLength() == 1) {
                            /* real double name? */
                            if (dic.match(ILexicon.CN_DNAME_2, w2.getValue())) {
                                isb.append(w1.getValue());
                                isb.append(w2.getValue());
                                return isb.toString();
                            }
                            /* not a real double name, check if it is a single name. */
                            else if (dic.match(ILexicon.CN_SNAME, w1.getValue())) {
                                isb.append(w1.getValue());
                                return isb.toString();
                            }
                        }
                        /*
                         * double name: char 2 and the char after it make up a word. like: 陈志高兴奋极了, chunk:陈_志_高兴
                         * (兴和后面成词) like: 陈志高的, chunk:陈_志_高的 ("的"的阕值Config.SINGLE_THRESHOLD) like: 陈高兴奋极了, chunk:陈_高_兴奋
                         * (single name)
                         */
                        else {
                            String d1 = new String(w2.getValue().charAt(0) + "");
                            int index_ = index + chunk.getWords()[0].getLength() + 2;
                            IWord[] ws = getNextMatch(chars, index_);
                            // System.out.println("index:"+index+":"+chars[index]+", "+ws[0]);
                            /* is it a double name? */
                            if (dic.match(ILexicon.CN_DNAME_2, d1)
                                && (ws.length > 1 || ws[0].getFrequency() >= config.NAME_SINGLE_THRESHOLD)) {
                                isb.append(w1.getValue());
                                isb.append(d1);
                                return isb.toString();
                            }
                            /* check if it is a single name */
                            else if (dic.match(ILexicon.CN_SNAME, w1.getValue())) {
                                isb.append(w1.getValue());
                                return isb.toString();
                            }
                        }
                    }
                    /* check if it is a single name. */
                    else if (dic.match(ILexicon.CN_SNAME, w1.getValue())) {
                        isb.append(w1.getValue());
                        return isb.toString();
                    }
                    return null;
                case 2:
                    String d1 = new String(w1.getValue().charAt(0) + "");
                    String d2 = new String(w1.getValue().charAt(1) + "");
                    /*
                     * it is a double name and char 1, char 2 make up a word. like: 陈美丽是对的, chunk: 陈_美丽_是 more: 都成为高速公路,
                     * chunk:都_成为_高速公路 (1.6.8以前的有歧义)
                     */
                    if (dic.match(ILexicon.CN_DNAME_1, d1) && dic.match(ILexicon.CN_DNAME_2, d2)) {
                        isb.append(w1.getValue());
                        return isb.toString();
                    }
                    /*
                     * it is a single name, char 1 and the char after it make up a word.
                     */
                    else if (dic.match(ILexicon.CN_SNAME, d1)) {
                        IWord iw = dic.get(ILexicon.CJK_WORDS, d2);
                        if (iw != null && iw.getFrequency() >= config.NAME_SINGLE_THRESHOLD) {
                            isb.append(d1);
                            return isb.toString();
                        }
                    }
                    return null;
                case 3:
                    /*
                     * singe name: - ignore mean the char and the two chars after it make up a word. it is a double
                     * name. like: 陈美丽的人生， chunk: 陈_美丽的_人生
                     */
                    String c1 = new String(w1.getValue().charAt(0) + "");
                    String c2 = new String(w1.getValue().charAt(1) + "");
                    IWord w3 = dic.get(ILexicon.CJK_WORDS, w1.getValue().charAt(2) + "");
                    if (dic.match(ILexicon.CN_DNAME_1, c1) && dic.match(ILexicon.CN_DNAME_2, c2)
                        && (w3 == null || w3.getFrequency() >= config.NAME_SINGLE_THRESHOLD)) {
                        isb.append(c1);
                        isb.append(c2);
                        return isb.toString();
                    }
                    return null;
            }
        }
        return null;
    }

    /**
     * find the Chinese double name: when the last name and the first char of the name make up a word.
     * 
     * @param chunk the best chunk.
     * @return boolean
     */
    public boolean findCHName(IWord w, IChunk chunk) {
        String s1 = new String(w.getValue().charAt(0) + "");
        String s2 = new String(w.getValue().charAt(1) + "");

        if (dic.match(ILexicon.CN_LNAME, s1) && dic.match(ILexicon.CN_DNAME_1, s2)) {
            IWord sec = chunk.getWords()[1];
            switch (sec.getLength()) {
                case 1:
                    if (dic.match(ILexicon.CN_DNAME_2, sec.getValue())) return true;
                case 2:
                    String d1 = new String(sec.getValue().charAt(0) + "");
                    IWord _w = dic.get(ILexicon.CJK_WORDS, sec.getValue().charAt(1) + "");
                    // System.out.println(_w);
                    if (dic.match(ILexicon.CN_DNAME_2, d1)
                        && (_w == null || _w.getFrequency() >= config.NAME_SINGLE_THRESHOLD)) return true;
            }
        }

        return false;
    }

    /**
     * load a CJK char list from the stream start from the current position. till the char is not a CJK char.<br />
     * 
     * @param c <br />
     * @return char[] <br />
     * @throws IOException
     */
    protected char[] nextCJKSentence(int c) throws IOException {
        // StringBuilder isb = new StringBuilder();
        isb.clear();
        int ch;
        isb.append((char) c);

        checkCE = false;
        while ((ch = readNext()) != -1) {
            if (ENSCFilter.isWhitespace(ch)) break;
            if (!isCJKChar(ch)) {
                pushBack(ch);
                /* check chinese english mixed word */
                if (ENSCFilter.isEnLetter(ch)) checkCE = true;
                break;
            }
            isb.append((char) ch);
        }
        return isb.toString().toCharArray();
    }

    /**
     * find the letter or digit word from the current position.<br />
     * count until the char is whitespace or not letter_digit.
     * 
     * @param c
     * @return IWord
     * @throws IOException
     */
    @SuppressWarnings("unused")
    protected IWord nextLetterOrDigit(int c) throws IOException {
        // StringBuilder isb = new StringBuilder();
        isb.clear();
        if (ENSCFilter.isFullWidthChar(c)) c = c - 65248;
        if (ENSCFilter.isUpperCaseLetter(c)) c = c + 32;
        isb.append((char) c);
        int ch;
        boolean check = false;
        while ((ch = readNext()) != -1) {
            if (ENSCFilter.isWhitespace(ch)) {
                check = true;
                break;
            }
            if (ENSCFilter.isPunctuation(ch) && !ENSCFilter.isENKeepChar((char) ch)) break;
            if (!isLetterOrDigit(ch)) {
                pushBack(ch);
                check = true;
                break;
            }

            // turn the full-width char to half-width char.
            if (ENSCFilter.isFullWidthChar(ch)) ch = ch - 65248;
            // turn the lower case letter to upper case.
            if (ENSCFilter.isUpperCaseLetter(ch)) ch = ch + 32;

            isb.append((char) ch);
        }

        String __str = isb.toString();
        if (!dic.match(ILexicon.EN_PUN_WORDS, __str)) {
            // delete the useless english punctuations.
            int t = isb.length() - 1;
            boolean _switch = false;
            for (; t > 0 && isb.charAt(t) != '%' && ENSCFilter.isPunctuation(isb.charAt(t)); t--) {
                _switch = true;
                isb.deleteCharAt(t);
            }
            if (_switch) __str = isb.toString();
        }

        // System.out.println(sb.toString()+", "+ch);
        if ((check && !isCJKChar(ch)) || ch == -1) {
            // english stop word
            // if ( config.CLEAR_STOPWORD
            // && dic.match(ILexicon.STOP_WORD, __str) ) return null;
            return new JcsegWord(__str, IWord.T_BASIC_LATIN);
        }

        /*
         * get english and chinese mix word like 'BB机,B超...'
         */
        IWord w = new JcsegWord(__str, IWord.T_BASIC_LATIN);
        StringBuilder mixWord = new StringBuilder();
        mixWord.append(__str);
        String _temp = null;
        String _word = __str;
        int mc = 0, j = 0; // the number of char that readed from the stream.
        ArrayList<Integer> chArr = new ArrayList<Integer>(config.MIX_CN_LENGTH);

        /*
         * Attension: make sure that (ch = readNext()) is after j < Config.MIX_CN_LENGTH. or it cause the miss of the
         * next char.
         */
        for (; j < config.MIX_CN_LENGTH && (ch = readNext()) != -1; j++) {
            if (ENSCFilter.isWhitespace(ch)) break;
            mixWord.append((char) ch);
            // System.out.print((char)ch+",");
            chArr.add(ch);
            _temp = mixWord.toString();
            // System.out.println((j+1)+": "+_temp);
            if (dic.match(ILexicon.EC_MIXED_WORD, _temp)) {
                _word = _temp;
                mc = j + 1;
            }
        }

        if (mc > 0) {
            for (int i = j - 1; i >= mc; i--) {
                pushBack(chArr.get(i).intValue());
            }
            chArr.clear();
            chArr = null;
            w = new JcsegWord(_word, IWord.T_MIXED_WORD);
        } else {
            // StringBuilder sb = new StringBuilder();
            // for (int tl : chArr) {
            // sb.append((char) tl);
            // }
            // String suffixWord = sb.toString();
            for (int i = j - 1; i >= 0; i--) {
                pushBack(chArr.get(i).intValue());
            }
            chArr.clear();
            chArr = null;
            // check if there is a units for the digit.
            if (isDigit(_word)) {
                ch = readNext();
                if (dic.match(ILexicon.CJK_UNITS, ((char) ch) + "")) {
                    // 神医:还需要检查后面的分词是否合理，如果不合理，则不能当作单位来处理
                    int nextc = readNext();
                    char nextChar = (char) nextc;
                    if (ENSCFilter.isWhitespace(nextChar) || !isCJKChar(nextc)) {
                        pushBack(nextc);
                        w = new JcsegWord(new String(_word + ((char) ch)), IWord.T_MIXED_WORD);
                    }
                    // 下一个字符还是中文字符
                    else {
                        char[] nextChars = preParseWordFrom(nextc);
                        if (preWordPool.size() > 0) {
                            IWord iWord = preWordPool.get(0);
                            // 是单字，尝试将单位放到后面去组合
                            if (iWord.getLength() == 1) {
                                pushBack(nextChars);
                                char[] retryChars = preParseWordFrom(ch);
                                if (preWordPool.size() > 0) {
                                    // iWord = preWordPool.get(0);
                                    int size = preWordPool.size();
                                    int firstWordLen = preWordPool.get(0).getLength();
                                    // int secondWordLen = 0;
                                    // if (size == 2) {
                                    // secondWordLen = preWordPool.get(1).getLength();
                                    // }
                                    // 单位放在后面更合理
                                    if (firstWordLen > 1) {
                                        pushBack(retryChars);
                                    } else {
                                        pushBack(nextChars);
                                        w = new JcsegWord(new String(_word + ((char) ch)), IWord.T_MIXED_WORD);
                                    }
                                }
                            } else {
                                pushBack(nextChars);
                                w = new JcsegWord(new String(_word + ((char) ch)), IWord.T_MIXED_WORD);
                            }
                        }/*
                          * else { pushBack(chars); w = new Word(new String(_word + ((char) ch)), IWord.T_MIXED_WORD); }
                          */
                    }
                } else {
                    pushBack(ch);
                }
            }

            /*
             * here: when the english near the chinese word, i think it mean something. so , clear it or not ? is up to
             * you. ^_^
             */
            /*
             * else if ( config.CLEAR_STOPWORD && dic.match(ILexicon.STOP_WORD, _word) ) { return null; }
             */
        }

        return w;
    }

    /**
     * 预处理CJK分词
     * 
     * @param c
     * @return
     * @throws IOException
     */
    private char[] preParseWordFrom(int c) throws IOException {
        char[] chars = nextCJKSentence(c);
        preWordPool.clear();
        parseChars(chars, preWordPool, false);
        return chars;
    }

    /**
     * find the next other letter from the current position. find the letter number from the current position. count
     * until the char in the specified position is not a letter number or whitespace. <br />
     * 
     * @param c
     * @return String
     * @throws IOException
     */
    protected String nextLetterNumber(int c) throws IOException {
        StringBuilder isb = new StringBuilder();
        isb.append((char) c);
        int ch;
        while ((ch = readNext()) != -1) {
            if (ENSCFilter.isWhitespace(ch)) break;
            if (!isLetterNumber(ch)) {
                pushBack(ch);
                break;
            }
            isb.append((char) ch);
        }

        return isb.toString();
    }

    /**
     * find the other number from the current position. <br />
     * count until the char in the specified position is not a orther number or whitespace. <br />
     * 
     * @param c
     * @return String
     * @throws IOException
     */
    protected String nextOtherNumber(int c) throws IOException {
        // StringBuilder isb = new StringBuilder();
        isb.clear();
        isb.append((char) c);
        int ch;
        while ((ch = readNext()) != -1) {
            if (ENSCFilter.isWhitespace(ch)) break;
            if (!isOtherNumber(ch)) {
                pushBack(ch);
                break;
            }
            isb.append((char) ch);
        }

        return isb.toString();
    }

    /**
     * find the chinese number from the current position. <br />
     * count until the char in the specified position is not a orther number or whitespace. <br />
     * 
     * @param chars char array of CJK items. <br />
     * @param index
     * @return String[]
     */
    protected String nextCNNumeric(char[] chars, int index) throws IOException {
        // StringBuilder isb = new StringBuilder();
        isb.clear();
        isb.append(chars[index]);
        for (int j = index + 1; j < chars.length; j++) {
            // System.out.println("cn:"+chars[j]);
            if (CNNMFilter.isCNNumeric(chars[j]) == -1) {
                // deal with “分之”
                if (j + 2 < chars.length && chars[j] == '分' && chars[j + 1] == '之') {
                    isb.append(chars[j++]);
                    isb.append(chars[j]);
                    continue;
                } else break;
            }
            isb.append(chars[j]);
        }
        return isb.toString();
    }

    /**
     * find pair punctuation of the given punctuation char. the purpose is to get the text bettween them. <br />
     * 
     * @param c
     * @throws IOException
     */
    protected String getPairPunctuationText(int c) throws IOException {
        // StringBuilder isb = new StringBuilder();
        isb.clear();
        char echar = PPTFilter.getPunctuationPair((char) c);
        boolean matched = false;
        int j, ch;
        ArrayList<Integer> chArr = new ArrayList<Integer>(config.PPT_MAX_LENGTH);
        for (j = 0; j < config.PPT_MAX_LENGTH; j++) {
            ch = readNext();
            if (ch == -1) break;
            if (ch == echar) {
                matched = true;
                break;
            }
            isb.append((char) ch);
            chArr.add(ch);
        }

        if (matched == false) {
            for (int i = j - 1; i >= 0; i--)
                pushBack(chArr.get(i).intValue());
            return null;
        }

        return isb.toString();
    }

    /**
     * an abstract method to gain a CJK word from the current position. simpleSeg and ComplexSeg is different to deal
     * this, so make it a abstract method here.
     * 
     * @param chars
     * @param index
     * @return IChunk
     * @throws IOException
     */
    protected abstract IChunk getBestCJKChunk(char chars[], int index) throws IOException;

}
