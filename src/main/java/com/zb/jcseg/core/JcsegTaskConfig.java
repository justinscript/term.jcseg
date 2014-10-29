/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import sun.security.krb5.Config;

import com.zb.jcseg.util.JcsegUtil;

/**
 * Jcseg segmentation task config class . <br />
 * 
 * @see Config
 * @author zxc Sep 3, 2014 2:15:09 PM
 */
public class JcsegTaskConfig {

    static final Logger        logger                = Logger.getLogger(JcsegTaskConfig.class.getName());

    /** jar home directory. */
    public static String       JAR_HOME              = System.getProperty("user.dir");
    /** default lexicon property file name */
    public static final String LEX_PROPERTY_FILE     = "jcseg.properties";
    /** simple algorithm or complex algorithm */
    public static final int    SIMPLE_MODE           = 1;
    public static final int    COMPLEX_MODE          = 2;

    /** maximum length for maximum match(5-7) */
    public int                 MAX_LENGTH            = 5;

    /**
     * maximum length for the chinese words after the LATIN word. use to match chinese and english mix word, like
     * 'B超,AA制...'
     */
    public int                 MIX_CN_LENGTH         = 2;

    /** identify the chinese name? */
    public boolean             I_CN_NAME             = false;

    /** the max length for the adron of the chinese last name.like 老陈 “老” */
    public int                 MAX_CN_LNADRON        = 1;

    /** wether to load the pinying of the CJK_WORDS */
    public boolean             LOAD_CJK_PINYIN       = false;

    /** append the pinying to the splited IWord */
    public boolean             APPEND_CJK_PINYIN     = false;

    /** wether to load the syn word of the CJK_WORDS. */
    public boolean             LOAD_CJK_SYN          = false;

    /** append the syn word to the splited IWord. */
    public boolean             APPEND_CJK_SYN        = true;

    /** wether to load the word's part of speech */
    public boolean             LOAD_CJK_POS          = false;

    /**
     * the threshold of the single word that is a single word when it and the last char of the name make up a word.
     */
    public int                 NAME_SINGLE_THRESHOLD = 1000000;

    /** the maxinum length for the text bettween the pair punctution. */
    public int                 PPT_MAX_LENGTH        = 15;

    /** clear away the stopword. */
    public boolean             CLEAR_STOPWORD        = false;

    /** chinese numeric to Arabic . */
    public boolean             CNNUM_TO_ARABIC       = true;

    /** chinese fraction to arabic fraction . */
    public boolean             CNFRA_TO_ARABIC       = true;

    private String             prefix                = "lex";
    private String             suffix                = "lex";
    protected String           lexPath               = null;
    private boolean            lexAutoload           = false;
    private int                polltime              = 10;
    // 智能合并但在分
    private boolean            wiselyUnionWord       = false;

    public JcsegTaskConfig() {
        this(null);
    }

    public JcsegTaskConfig(String proFile) {
        JAR_HOME = JcsegUtil.getJarHome(this);
        try {
            resetFromPropertyFile(proFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // public File stream2file(InputStream in) throws IOException {
    // final File tempFile = File.createTempFile("stream2file", ".tmp");
    // tempFile.deleteOnExit();
    // try {
    // FileOutputStream out = new FileOutputStream(tempFile);
    // IOUtils.copy(in, out);
    // } catch (Exception e) {
    //
    // }
    // return tempFile;
    // }

    // 解压jar包中的文件到toDir目录
    public static void unJar(File jarFile, File toDir) throws IOException {
        unJar(new JarFile(jarFile), toDir);
    }

    public static void unJar(JarFile jar, File toDir) throws IOException {
        try {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (!entry.isDirectory()) {
                    InputStream in = jar.getInputStream(entry);
                    try {
                        File file = new File(toDir, entry.getName());
                        if (!file.getParentFile().mkdirs()) {
                            if (!file.getParentFile().isDirectory()) {
                                throw new IOException("Mkdirs failed to create " + file.getParentFile().toString());
                            }
                        }
                        OutputStream out = new FileOutputStream(file);
                        try {
                            byte[] buffer = new byte[8192];
                            int i;
                            while ((i = in.read(buffer)) != -1) {
                                out.write(buffer, 0, i);
                            }
                        } finally {
                            out.close();
                        }
                    } finally {
                        in.close();
                    }
                }
            }
        } finally {
            jar.close();
        }
    }

    /**
     * reset the value of its options from a propertie file . <br />
     * 
     * @param proFile path of jcseg.properties file. when null is givend, jcseg will look up the default
     * jcseg.properties file. <br />
     * @throws IOException
     */
    public void resetFromPropertyFile(String proFile) throws IOException {
        Properties lexPro = new Properties();
        String jarPath = null;
        /* load the mapping from the default property file. */
        if (proFile == null) {
            /**
             * <pre>
             *      0.load the jcseg.properties from the current data.
             *      1.load the jcseg.properties located with the jar file. 
             *      2.load the jcseg.properties from the classpath. 
             *      3.load the jcseg.properties from the user.home.
             * </pre>
             */
            boolean jcseg_properties = false;
            File pro_file = null;
            String fileName = System.getProperty("jcseg.properties.path");
            if (StringUtils.isNotEmpty(fileName)) {
                pro_file = new File(fileName);
                if (pro_file.exists()) {
                    lexPro.load(new FileReader(pro_file));
                    jcseg_properties = true;
                }
            }

            // File pro_file = stream2file(this.getClass().getResourceAsStream("/data/" + LEX_PROPERTY_FILE));
            // File pro_file = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
            // InputStream in = getClass().getResourceAsStream("/data/" + LEX_PROPERTY_FILE);

            if (!jcseg_properties) {
                lexPro.load(this.getClass().getResourceAsStream("/data/" + LEX_PROPERTY_FILE));
                if (!lexPro.isEmpty()) {
                    jcseg_properties = true;
                    jarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
                    logger.info("resetFromPropertyFile jarPath :" + jarPath);
                    // URL jarUrl = new URL(jarPath);
                    // JarURLConnection jarCon = (JarURLConnection) jarUrl.openConnection();
                    // jarCon.getJarFile();
                    jarPath = StringUtils.remove(jarPath, ".jar");
                    File workDir = new File(jarPath);
                    // File.createTempFile("", "", new File(StringUtils.remove(jarPath, ".jar")));
                    workDir.delete();
                    workDir.mkdirs();
                    if (!workDir.isDirectory()) {
                        logger.info("Mkdirs failed to create " + workDir);
                        throw new IOException("Mkdirs failed to create " + workDir);
                    }
                    logger.info("resetFromPropertyFile workDir :" + workDir.getPath());
                    unJar(new JarFile(jarPath + ".jar"), workDir);
                }
            }
            if (!jcseg_properties) {
                pro_file = new File(JAR_HOME + "/" + LEX_PROPERTY_FILE);
                if (pro_file.exists()) {
                    lexPro.load(new FileReader(pro_file));
                    jcseg_properties = true;
                }
            }
            if (!jcseg_properties) {
                InputStream is = JcsegDictionaryFactory.class.getResourceAsStream("/" + LEX_PROPERTY_FILE);
                if (is != null) {
                    lexPro.load(new BufferedInputStream(is));
                    jcseg_properties = true;
                }
            }
            if (!jcseg_properties) {
                pro_file = new File(System.getProperty("user.home") + "/" + LEX_PROPERTY_FILE);
                if (pro_file.exists()) {
                    lexPro.load(new FileReader(pro_file));
                    jcseg_properties = true;
                }
            }

            /*
             * jcseg properties file loading status report, show the crorrent properties file location information . <br
             * />
             * @date 2014-09-06
             */
            if (!jcseg_properties) {
                String _report = "jcseg properties[jcseg.properties] file loading error: \n";
                _report += "try the follwing ways to solve the problem: \n";
                _report += "1. put jcseg.properties into the classpath.\n";
                _report += "2. put jcseg.properties together with the jcseg-core-{version}.jar file.\n";
                _report += "3. put jcseg.properties in directory " + System.getProperty("user.home") + "\n\n";
                throw new IOException(_report);
            }
        }
        /* load the mapping from the specified property file. */
        else {
            File pro_file = new File(proFile);
            if (!pro_file.exists()) throw new IOException("property file [" + proFile + "] not found!");
            lexPro.load(new FileReader(pro_file));
        }

        /* about the lexicon */
        // the lexicon path
        logger.info("jcseg.properties :" + lexPro.values());
        lexPath = (StringUtils.isNotEmpty(jarPath) ? jarPath : StringUtils.EMPTY) + lexPro.getProperty("lexicon.path");
        logger.info("################ jarPath : " + jarPath);
        logger.info("################ jcseg.properties lexicon.path : " + lexPro.getProperty("lexicon.path"));
        logger.info("################ lexPath : " + lexPath);
        // lexPath = this.getClass().getResource(lexPath).getFile();
        if (lexPath == null) {
            throw new IOException("lexicon.path property not find in jcseg.properties file!!!");
        }
        if (lexPath.indexOf("{jar.dir}") > -1) lexPath = lexPath.replace("{jar.dir}", JAR_HOME);
        // System.out.println("path: "+lexPath);

        // the lexicon file prefix and suffix
        if (lexPro.getProperty("lexicon.suffix") != null) suffix = lexPro.getProperty("lexicon.suffix");
        if (lexPro.getProperty("lexicon.prefix") != null) prefix = lexPro.getProperty("lexicon.prefix");

        // reset all the options
        if (lexPro.getProperty("jcseg.maxlen") != null) MAX_LENGTH = Integer.parseInt(lexPro.getProperty("jcseg.maxlen"));
        if (lexPro.getProperty("jcseg.mixcnlen") != null) MIX_CN_LENGTH = Integer.parseInt(lexPro.getProperty("jcseg.mixcnlen"));
        if (lexPro.getProperty("jcseg.icnname") != null && lexPro.getProperty("jcseg.icnname").equals("1")) I_CN_NAME = true;
        if (lexPro.getProperty("jcseg.cnmaxlnadron") != null) MAX_CN_LNADRON = Integer.parseInt(lexPro.getProperty("jcseg.cnmaxlnadron"));
        if (lexPro.getProperty("jcseg.nsthreshold") != null) NAME_SINGLE_THRESHOLD = Integer.parseInt(lexPro.getProperty("jcseg.nsthreshold"));
        if (lexPro.getProperty("jcseg.pptmaxlen") != null) PPT_MAX_LENGTH = Integer.parseInt(lexPro.getProperty("jcseg.pptmaxlen"));
        if (lexPro.getProperty("jcseg.loadpinyin") != null && lexPro.getProperty("jcseg.loadpinyin").equals("1")) LOAD_CJK_PINYIN = true;
        if (lexPro.getProperty("jcseg.loadsyn") != null && lexPro.getProperty("jcseg.loadsyn").equals("1")) LOAD_CJK_SYN = true;
        if (lexPro.getProperty("jcseg.loadpos") != null && lexPro.getProperty("jcseg.loadpos").equals("1")) LOAD_CJK_POS = true;
        if (lexPro.getProperty("jcseg.clearstopword") != null && lexPro.getProperty("jcseg.clearstopword").equals("1")) CLEAR_STOPWORD = true;
        if (lexPro.getProperty("jcseg.cnnumtoarabic") != null && lexPro.getProperty("jcseg.cnnumtoarabic").equals("0")) CNNUM_TO_ARABIC = false;
        if (lexPro.getProperty("jcseg.cnfratoarabic") != null && lexPro.getProperty("jcseg.cnfratoarabic").equals("0")) CNFRA_TO_ARABIC = false;
        if (lexPro.getProperty("lexicon.autoload") != null && lexPro.getProperty("lexicon.autoload").equals("1")) lexAutoload = true;
        if (lexPro.getProperty("lexicon.polltime") != null) polltime = Integer.parseInt(lexPro.getProperty("lexicon.polltime"));
        if (lexPro.getProperty("wiselyUnionWord") != null && lexPro.getProperty("wiselyUnionWord").equals("1")) wiselyUnionWord = true;
    }

    /** property about lexicon file. */
    public String getLexiconFilePrefix() {
        return prefix;
    }

    public String getLexiconFileSuffix() {
        return suffix;
    }

    /** return the lexicon directory path */
    public String getLexiconPath() {
        return lexPath;
    }

    public void setLexPath(String lexPath) {
        this.lexPath = lexPath;
    }

    /** about lexicon autoload */
    public boolean isAutoload() {
        return lexAutoload;
    }

    public void setAutoload(boolean autoload) {
        lexAutoload = autoload;
    }

    public int getPollTime() {
        return polltime;
    }

    public void setPollTime(int polltime) {
        this.polltime = polltime;
    }

    public int getMaxLength() {
        return MAX_LENGTH;
    }

    public void setMaxLength(int maxLength) {
        MAX_LENGTH = maxLength;
    }

    public int getMixCnLength() {
        return MIX_CN_LENGTH;
    }

    public void setMixCnLength(int mixCnLength) {
        MIX_CN_LENGTH = mixCnLength;
    }

    public boolean identifyCnName() {
        return I_CN_NAME;
    }

    public void setICnName(boolean iCnName) {
        I_CN_NAME = iCnName;
    }

    public int getMaxCnLnadron() {
        return MAX_CN_LNADRON;
    }

    public void setMaxCnLnadron(int maxCnLnadron) {
        MAX_CN_LNADRON = maxCnLnadron;
    }

    public boolean loadCJKPinyin() {
        return LOAD_CJK_PINYIN;
    }

    public void setLoadCJKPinyin(boolean loadCJKPinyin) {
        LOAD_CJK_PINYIN = loadCJKPinyin;
    }

    public boolean appendCJKPinyin() {
        return APPEND_CJK_PINYIN;
    }

    public void setAppendCJKPinyin(boolean appendCJKPinyin) {
        APPEND_CJK_PINYIN = appendCJKPinyin;
    }

    public boolean loadCJKSyn() {
        return LOAD_CJK_SYN;
    }

    public void setLoadCJKSyn(boolean loadCJKSyn) {
        LOAD_CJK_SYN = loadCJKSyn;
    }

    public boolean appendCJKSyn() {
        return APPEND_CJK_SYN;
    }

    public void setAppendCJKSyn(boolean appendCJKPinyin) {
        APPEND_CJK_SYN = appendCJKPinyin;
    }

    public boolean ladCJKPos() {
        return LOAD_CJK_POS;
    }

    public void setLoadCJKPos(boolean loadCJKPos) {
        LOAD_CJK_POS = loadCJKPos;
    }

    public int getNameSingleThreshold() {
        return NAME_SINGLE_THRESHOLD;
    }

    public void setNameSingleThreshold(int thresold) {
        NAME_SINGLE_THRESHOLD = thresold;
    }

    public int getPPTMaxLength() {
        return PPT_MAX_LENGTH;
    }

    public void setPPT_MAX_LENGTH(int pptMaxLength) {
        PPT_MAX_LENGTH = pptMaxLength;
    }

    public boolean clearStopwords() {
        return CLEAR_STOPWORD;
    }

    public void setClearStopwords(boolean clearstopwords) {
        CLEAR_STOPWORD = clearstopwords;
    }

    public boolean cnNumToArabic() {
        return CNNUM_TO_ARABIC;
    }

    public void setCnNumToArabic(boolean cnNumToArabic) {
        CNNUM_TO_ARABIC = cnNumToArabic;
    }

    public boolean cnFractionToArabic() {
        return CNFRA_TO_ARABIC;
    }

    public void setCnFactionToArabic(boolean cnFractionToArabic) {
        CNFRA_TO_ARABIC = cnFractionToArabic;
    }

    public boolean isWiselyUnionWord() {
        return wiselyUnionWord;
    }

    public void setWiselyUnionWord(boolean wiselyUnionWord) {
        this.wiselyUnionWord = wiselyUnionWord;
    }
}
