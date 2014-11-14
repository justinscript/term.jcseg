/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import com.zb.jcseg.filter.ENSCFilter;

/**
 * Jcseg Dictionary abstract class. <br />
 * 
 * @author zxc Sep 3, 2014 2:15:09 PM
 */
public abstract class JcsegDictionary {

    static final Logger        logger         = Logger.getLogger(JcsegDictionary.class.getName());

    public static final String AL_TODO_FILE   = "lex-autoload.todo";

    protected JcsegTaskConfig  config;
    protected boolean          sync;

    /** autoload thread */
    private Thread             autoloadThread = null;

    public JcsegDictionary(JcsegTaskConfig config, Boolean sync) {
        this.config = config;
        this.sync = sync.booleanValue();
    }

    public JcsegTaskConfig getConfig() {
        return config;
    }

    public void setConfig(JcsegTaskConfig config) {
        this.config = config;
    }

    /**
     * load all the words from a specified lexicon file . <br />
     * 
     * @param config
     * @param file
     */
    public void loadFromLexiconFile(File file) {
        loadWordsFromFile(config, this, file, "UTF-8");
    }

    public void loadFromLexiconFile(String file) {
        loadWordsFromFile(config, this, new File(file), "UTF-8");
    }

    /**
     * load the all the words form all the files under a specified lexicon directionry . <br />
     * 
     * @param config
     * @param lexDir
     * @throws IOException
     */
    public void loadFromLexiconDirectory(String lexDir) throws IOException {

        File[] files = getLexiconFiles(lexDir, config.getLexiconFilePrefix(), config.getLexiconFileSuffix());
        if (files == null) {
            logger.warning("loadFromLexiconDirectory :  files is null!");
            return;
        }
        logger.info("loadFromLexiconDirectory :  files length " + files.length);

        for (int j = 0; j < files.length; j++) {
            loadWordsFromFile(config, this, files[j], "UTF-8");
        }
    }

    /** start the lexicon autoload thread . */
    public void startAutoload() {
        if (autoloadThread != null) return;
        autoloadThread = new Thread(new Runnable() {

            @Override
            public void run() {
                File todo = new File(config.getLexiconPath() + "/" + AL_TODO_FILE);
                long lastModified = todo.lastModified();
                while (true) {
                    // sleep for some time (seconds)
                    try {
                        Thread.sleep(config.getPollTime() * 1000);
                    } catch (InterruptedException e) {
                        break;
                    }

                    // check the update of the lex-autoload.todo
                    if (todo.lastModified() <= lastModified) continue;

                    // load words form the lexicon files
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(todo));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.indexOf('#') != -1) continue;
                            if ("".equals(line)) continue;
                            loadFromLexiconFile(config.getLexiconPath() + "/" + line);
                        }
                        reader.close();
                        FileWriter fw = new FileWriter(todo);
                        fw.write("");
                        fw.close();

                        lastModified = todo.lastModified();
                        // System.out.println("newly added words loaded.");
                    } catch (IOException e) {
                        break;
                    }
                }
                autoloadThread = null;
            }
        });
        autoloadThread.setDaemon(true);
        autoloadThread.start();
        // System.out.println("lexicon autoload thread started!!!");
    }

    public void stopAutoload() {
        if (autoloadThread != null) {
            autoloadThread.interrupt();
            autoloadThread = null;
        }
    }

    public boolean isSync() {
        return sync;
    }

    /**
     * loop up the dictionary, check the given key is in the dictionary or not. <br />
     * 
     * @param t
     * @param key
     * @return true for matched, false for not match.
     */
    public abstract boolean match(int t, String key);

    /**
     * add a new word to the dictionary. <br />
     * 
     * @param t
     * @param key
     * @param type
     */
    public abstract void add(int t, String key, int type);

    /**
     * add a new word to the dictionary with its statistics frequency. <br />
     * 
     * @param t
     * @param key
     * @param fre
     * @param type
     */
    public abstract void add(int t, String key, int fre, int type);

    /**
     * return the IWord asscociate with the given key. if there is not mapping for the key null will be return. <br />
     * 
     * @param t
     * @param key
     */
    public abstract IWord get(int t, String key);

    /**
     * remove the mapping associate with the given key. <br />
     * 
     * @param t
     * @param key
     */
    public abstract void remove(int t, String key);

    /**
     * return the size of the dictionary <br />
     * 
     * @param t
     * @return int
     */
    public abstract int size(int t);

    /**
     * get the key's type index located in ILexicon interface. <br />
     * 
     * @param key
     * @return int
     */
    public static int getIndex(String key) {
        if (key == null) return -1;
        key = key.toUpperCase();

        if (key.equals("CJK_WORDS")) return ILexicon.CJK_WORD;
        else if (key.equals("CJK_UNITS")) return ILexicon.CJK_UNITS;
        else if (key.equals("EC_MIXED_WORD")) return ILexicon.EC_MIXED_WORD;
        else if (key.equals("CE_MIXED_WORD")) return ILexicon.CE_MIXED_WORD;
        else if (key.equals("CN_LNAME")) return ILexicon.CN_LNAME;
        else if (key.equals("CN_SNAME")) return ILexicon.CN_SNAME;
        else if (key.equals("CN_DNAME_1")) return ILexicon.CN_DNAME_1;
        else if (key.equals("CN_DNAME_2")) return ILexicon.CN_DNAME_2;
        else if (key.equals("CN_LNAME_ADORN")) return ILexicon.CN_LNAME_ADORN;
        else if (key.equals("EN_PUN_WORDS")) return ILexicon.EN_PUN_WORD;
        else if (key.equals("STOP_WORDS")) return ILexicon.STOP_WORD;

        return ILexicon.CJK_WORD;
    }

    /**
     * get all the lexicon file under the specified path and meet the specified conditions . <br />
     * 
     * @throws IOException
     */
    public static File[] getLexiconFiles(String lexDir, String prefix, String suffix) throws IOException {

        logger.info("getLexiconFiles :  lexDir : " + lexDir + "prefix : " + prefix + "suffix : " + suffix);

        File path = new File(lexDir);
        if (path.exists() == false) throw new IOException("Lexicon directory [" + lexDir + "] does'n exists.");

        /*
         * load all the lexicon file under the lexicon path that start with __prefix and end with __suffix.
         */
        final String __suffix = suffix;
        final String __prefix = prefix;
        File[] files = path.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return (name.startsWith(__prefix) && name.endsWith(__suffix));
            }
        });

        return files;
    }

    /**
     * load all the words in the specified lexicon file into the dictionary. <br />
     * 
     * @param config
     * @param dic
     * @param file
     * @param charset
     */
    public static void loadWordsFromFile(JcsegTaskConfig config, JcsegDictionary dic, File file, String charset) {
        long s = System.currentTimeMillis();

        long lineNum = 0l;

        InputStreamReader ir = null;
        BufferedReader br = null;

        try {
            ir = new InputStreamReader(new FileInputStream(file), charset);
            br = new BufferedReader(ir);

            String line = null;
            boolean isFirstLine = true;
            int t = -1;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if ("".equals(line)) continue;
                // swept the notes
                if (line.indexOf('#') == 0) continue;
                // the first line fo the lexicon file.
                if (isFirstLine == true) {
                    t = JcsegDictionary.getIndex(line);
                    // System.out.println(line+", "+t);
                    isFirstLine = false;
                    if (t >= 0) continue;
                }

                // special lexicon
                if (line.indexOf('/') == -1) {
                    /*
                     * Here: 1. english stop words, 2. english and chinese mixed words, 3. chinese and english mixed
                     * words, 4. english punctuation words, don't have to limit its length.
                     */
                    boolean olen = (t == ILexicon.STOP_WORD && ENSCFilter.isHWEnChar(line.charAt(0)));
                    olen = olen || (t == ILexicon.EC_MIXED_WORD);
                    olen = olen || (t == ILexicon.CE_MIXED_WORD);
                    olen = olen || (t == ILexicon.EN_PUN_WORD);
                    if (olen || line.length() <= config.MAX_LENGTH) {
                        dic.add(t, line, IWord.T_CJK_WORD);
                    }
                }
                // normal words lexicon file
                else {
                    String[] wd = line.split("/");
                    if (wd.length > 4) dic.add(t, wd[0], Integer.parseInt(wd[4]), IWord.T_CJK_WORD);
                    else dic.add(t, wd[0], IWord.T_CJK_WORD);

                    IWord w = dic.get(t, wd[0]);
                    // set the pinying of the word.
                    if (config.LOAD_CJK_PINYIN && !"null".equals(wd[2])) {
                        w.setPinyin(wd[2]);
                    }
                    // set the syn words.
                    if (config.LOAD_CJK_SYN && !"null".equals(wd[3])) {
                        String[] syns = wd[3].split(",");
                        for (int j = 0; j < syns.length; j++) {
                            /*
                             * Here: filter the syn words that its length is greater than Config.MAX_LENGTH
                             */
                            if (syns[j].length() > config.MAX_LENGTH) continue;
                            w.addSyn(syns[j].trim());
                        }
                    }
                    // set the word's part of speech
                    if (config.LOAD_CJK_POS && !"null".equals(wd[1])) {
                        String[] pos = wd[1].split(",");
                        for (int j = 0; j < pos.length; j++)
                            w.addPartSpeech(pos[j].trim());
                    }
                    lineNum = w.getLength();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ir != null) ir.close();
                if (br != null) br.close();
            } catch (IOException e) {
            }
        }

        logger.info("jcseg words loaded time=" + (System.currentTimeMillis() - s) + "ms, line=" + lineNum
                    + ", on file=" + file);
    }
}
