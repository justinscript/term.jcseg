/*
 * Copyright 2011-2016 ZuoBian.com All right reserved. This software is the confidential and proprietary information of
 * ZuoBian.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with ZuoBian.com.
 */
package com.zb.jcseg.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 智能合并词
 * 
 * @author zxc Sep 3, 2014 2:11:34 PM
 */
public class WordUnionUtils {

    // 后缀词
    static String[]               SUFFIX_LETTERS      = new String[] { "周", "蜜", "片", "起", "效", "型", "版", "汁", "籽",
            "式", "装", "峡", "级", "段", "叶", "水", "草", "乳", "素", "霜", "油", "类", "液", "湾", "贴", "茶", "牌", "款", "套", "杯" };

    // 前缀词
    static String[]               PREFIX_LETTERS      = new String[] { "纯", "送", "赠", "超", /* "长", "短", */"带", "淡",/*
                                                                                                                    * "大",
                                                                                                                    * "小"
                                                                                                                    * ,
                                                                                                                    */
                                                      "不", "祛", "护", "防", "无", "控", /* "新", */"满", "转", "全", "去", "高",
            "低", "含", "配", "老", "加", "可", "仿"        };

    // static String[] Single_Words = new String[] { "男", "女", "春", "夏", "秋", "冬", "童", "布", "鞋",
    // "潮", "帽", "超", "老", "表", "软", "硬", "软", "大", "小", "新", "旧", "长", "短", "好", "轻", "细", "粗", "薄", "厚", "真",
    // "内", "外", "凉", "热", "冷", "胖", "廋", "松", "紧", "宽", "窄", "皮", "棉", "单"
    //
    static String[]               Single_Words        = new String[] { "刀", "厚", "大", "架", "热", "车", "真", "紧", "男",
            "老", "冷", "茶", "长", "袜", "乳", "春", "灯", "软", "冬", "棉", "门", "包", "书", "袋", "轻", "膏", "硬", "桌", "巾", "廋",
            "篮", "粗", "秋", "靴", "枕", "新", "桶", "夏", "水", "纸", "镜", "油", "皮", "外", "宽", "刷", "童", "内", "潮", "椅", "松",
            "单", "壳", "霜", "凳", "壶", "裙", "超", "细", "杯", "胖", "女", "笔", "鞋", "蜡", "盒", "盐", "布", "床", "好", "窄", "薄",
            "露", "帽", "柜", "胶", "小", "裤", "凉", "旧", "套", "伞", "表", "箱", "垫", "短", "衣"
                                                      // 颜色
                                                      /* "红", "橙", "黄", "绿", "蓝", "青", "紫", "黑", "白", "银", "灰" */

                                                      // 名词

                                                      };

    // 女包邮： 将这样的词处理为女包和包邮两个词
    static List<AbbreviationWord> abreviationWordList = new ArrayList<AbbreviationWord>();
    static {
        abreviationWordList.add(new AbbreviationWord("包", new String[] { "男", "女" }, new String[] { "邮" }));
    }

    // 单位
    static HashSet<String>        unitSet             = new HashSet<String>();
    static {
        String[] units = { "米", "寸", "尺", "丈", "里", "年", "月", "日", "时", "秒", "元", "角", "升", "斗", "石", "瓶", "袋", "盒",
            "吨", "克", "斤", "两", "担", "亩", "顷", "折", "件", "番", "世", "乘", "井", "亿", "仙", "代", "份", "位", "例", "倍", "兆",
            "册", "出", "分", "划", "列", "刻", "区", "千", "卷", "厅", "厨", "口", "句", "台", "号", "员", "品", "回", "团", "国", "圆",
            "圈", "场", "堆", "声", "壶", "处", "夜", "天", "头", /* "女","男", */"室", /* "家", */"尾", "局", "层", "届", "岁", "市", /*
                                                                                                                     * "带",
                                                                                                                     */
            "幅", "幕", "床", "度", "座", "弄", "式", "快", "批", "抽", "捧", "撮", "支", "曲", "期", "本", "朵", "束", "条", "杯", "柜",
            "栋", "样", "桌", "桶", "次", "步", "毫", "洞", "派", "滴", "点", "片", "环", "盏", "盘", "种", "站", "笔", "等", "箱", "粒",
            "级", "线", "缸", "群", "翻", "艘", "节", "行", "课", "车", "轮", "辆", "辈", "辑", "道", "部", "重", "针", "门", "间", "阶",
            "集", "面", "页", "颗", "款", "码" };
        for (String c : units) {
            unitSet.add(c);
        }
    }

    /**
     * 智能合并分词后的单个字符
     * 
     * @param segWords
     * @return
     */
    @SuppressWarnings("unused")
    public static List<String> wiselyCombineWords(List<String> segWords) {
        if (segWords.isEmpty()) {
            return segWords;
        }
        List<String> splitWordsList = new ArrayList<String>(segWords.size());
        StringBuilder sb = new StringBuilder();
        String candidate = "";
        String space = " ";
        // 智能合并算法--缺省向前合并
        for (int i = 0, size = segWords.size(); i < size; i++) {
            String word = segWords.get(i);
            // }
            // for (String word : segWords) {
            // 当前词的长度大于1
            if (StringUtils.length(word) > 1) {
                // 有剩余字符
                if (sb.length() > 0) {
                    // 向前合并
                    String tmpStr = sb.toString();
                    if (sb.length() == 1) {
                        // 单字也可以作为一个词
                        if (isSingleWord(tmpStr)) {
                            addStr2List(splitWordsList, candidate);
                            addStr2List(splitWordsList, tmpStr);
                        }
                        // 是否要往后合并
                        else if (isPrefixLetter(tmpStr)) {
                            if (StringUtils.isNotBlank(candidate)) {
                                addStr2List(splitWordsList, candidate);
                            }
                            // 往后加
                            word = tmpStr + word;
                        } else {
                            if (isEndWithDigit(candidate)) {
                                // 往前合并
                                if (unitSet.contains(tmpStr) || StringUtils.length(word) > 1) {
                                    candidate = candidate + tmpStr;
                                    addStr2List(splitWordsList, candidate);
                                } else {
                                    if (StringUtils.isNotBlank(candidate)) {
                                        addStr2List(splitWordsList, candidate);
                                    }
                                    // 往后合并
                                    word = tmpStr + word;
                                }
                            } else {
                                handleSingleWord(splitWordsList, candidate, tmpStr);
                            }
                        }
                    } else {
                        addStr2List(splitWordsList, candidate);
                        addStr2List(splitWordsList, tmpStr);
                    }
                    sb.setLength(0);
                } else {
                    if (StringUtils.isNotBlank(candidate)) {
                        addStr2List(splitWordsList, candidate);
                    }
                }
                candidate = word;
            } else {
                {
                    int k = i + 1;
                    // 当前字可以作为单字使用
                    int singleWordCount = 1;
                    for (int j = k; j < size; j++) {
                        if (StringUtils.length(segWords.get(j)) != 1) {
                            break;
                        }
                        singleWordCount++;
                    }
                    if (singleWordCount > 1) {
                        int exIndex = i;
                        boolean canAllBeTreatedAsSingle = true;
                        for (int j = i, stop = i + singleWordCount; j < stop; j++) {
                            if (!isSingleWord(segWords.get(j))) {
                                exIndex = j;
                                canAllBeTreatedAsSingle = false;
                                break;
                            }
                        }
                        // 所有词都作为单字处理
                        if (canAllBeTreatedAsSingle) {
                            addStr2List(splitWordsList, candidate);
                            candidate = "";
                            for (int stop = i + singleWordCount; i < stop; i++) {
                                addStr2List(splitWordsList, segWords.get(i));
                            }
                            i--;
                        } else {
                            // TODO zxc:需要更智能判断
                            for (int stop = i + singleWordCount; i < stop; i++) {
                                sb.append(segWords.get(i));
                            }
                            i--;
                            addStr2List(splitWordsList, candidate);
                            addStr2List(splitWordsList, sb.toString());
                            sb.setLength(0);
                            candidate = "";
                        }
                        continue;
                    }
                }

                if (StringUtils.equals(word, space)) {
                    String tmpStr = sb.toString();
                    if (sb.length() == 1) {
                        candidate = candidate + tmpStr;
                        addStr2List(splitWordsList, candidate);
                    } else {
                        if (StringUtils.isNotBlank(candidate)) {
                            addStr2List(splitWordsList, candidate);
                        }
                        if (sb.length() > 1) {
                            addStr2List(splitWordsList, tmpStr);
                        }
                    }
                    splitWordsList.add(space);
                    sb.setLength(0);
                    candidate = "";
                } else {
                    // 新款鞋女2013
                    // if (index + 1 < size) {
                    // nextWordIsSingle = StringUtils.length(segWords.get(index + 1)) == 1;
                    // } else {
                    // nextWordIsSingle = false;
                    // }
                    if (sb.length() > 0) {
                        String header = sb.substring(0, 1);
                        // 个性烫钻小钻铅笔裤显瘦牛仔裤女
                        if (isSingleWord(header)) {
                            addStr2List(splitWordsList, candidate);
                            candidate = "";
                            addStr2List(splitWordsList, header);
                            sb.deleteCharAt(0);
                        }
                    }
                    sb.append(word);
                }
            }
        }
        boolean isCandidateNotNull = StringUtils.length(candidate) > 0;
        boolean isSbNotNull = sb.length() > 0;
        if (isCandidateNotNull) {
            if (isSbNotNull) {
                String w = sb.toString();
                if (isSingleWord(w)) {
                    addStr2List(splitWordsList, candidate);
                    addStr2List(splitWordsList, w);
                } else {
                    handleSingleWord(splitWordsList, candidate, w);
                }
            } else {
                addStr2List(splitWordsList, candidate);
            }
        } else if (isSbNotNull) {
            addStr2List(splitWordsList, sb.toString());
        }
        return splitWordsList;
    }

    private static void handleSingleWord(List<String> splitWordsList, String candidate, String singleWord) {
        MagicWordResult result = isNeedAddDualWord(candidate, singleWord);
        if (result.isSuccess()) {
            addStr2List(splitWordsList, candidate);
            addStr2List(splitWordsList, result.getWord());
        } else {
            if (StringUtils.length(singleWord) <= 1) {
                // TODO zxc：合并也会出错，尽可能不合并
                addStr2List(splitWordsList, candidate);
                addStr2List(splitWordsList, singleWord);
                // candidate = candidate + singleWord;
                // addStr2List(splitWordsList, candidate);
            } else {
                addStr2List(splitWordsList, candidate);
                addStr2List(splitWordsList, singleWord);
            }
        }
    }

    /**
     * 该方法允许加空格
     * 
     * @param splitWordsList
     * @param candidate
     */
    public static void addStr2List(List<String> splitWordsList, String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return;
        }
        // 不能去重复词
        // for (String word : splitWordsList) {
        // if (word.contains(candidate)) {
        // return;
        // }
        // }
        splitWordsList.add(candidate);
    }

    /**
     * 判断单字是否要向后合并
     * 
     * @param c
     * @return
     */
    private static boolean isPrefixLetter(String c) {
        for (String word : PREFIX_LETTERS) {
            if (StringUtils.equals(word, c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 最后一个字符是否为数字 -- M7手机壳
     * 
     * @param c
     * @return
     */
    public static boolean isEndWithAsciiAlphanumeric(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        return CharUtils.isAsciiAlphanumeric(str.charAt(str.length() - 1));
    }

    private static boolean isEndWithDigit(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        return Character.isDigit(str.charAt(str.length() - 1));
    }

    public static boolean isStartWithDigit(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        return Character.isDigit(str.charAt(0));
    }

    public static boolean isEndWithDigital(String str, String c) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        return CharUtils.isAsciiNumeric(str.charAt(str.length() - 1)) && unitSet.contains(c);
    }

    public static boolean isSuffix(String c) {
        for (String word : SUFFIX_LETTERS) {
            if (StringUtils.equals(word, c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 单字也可以组成词
     * 
     * @param c
     * @return
     */
    private static boolean isSingleWord(String c) {
        for (String word : Single_Words) {
            if (StringUtils.equals(word, c)) {
                return true;
            }
        }
        return false;
    }

    // 包含单个字符
    public static boolean isContainSingleWord(String w) {
        if (StringUtils.length(w) <= 0) {
            return false;
        }
        for (String s : Single_Words) {
            if (StringUtils.contains(w, s)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否要填补词
    public static MagicWordResult isNeedAddDualWord(String candidate, String lastWord) {
        for (AbbreviationWord word : abreviationWordList) {
            MagicWordResult result = word.isAccept(candidate, lastWord);
            if (result.isSuccess()) {
                return result;
            }
        }
        return MagicWordResult.failResult;
    }

    static class AbbreviationWord {

        private String   dualWord;
        private String[] prefixes;
        private String[] suffixes;

        public AbbreviationWord(String dualWord, String[] prefixes, String[] suffixes) {
            this.dualWord = dualWord;
            this.prefixes = prefixes;
            this.suffixes = suffixes;
        }

        public MagicWordResult isAccept(String candidate, String lastWord) {
            if (!StringUtils.endsWith(candidate, dualWord)) {
                return MagicWordResult.failResult;
            }
            String endWord = null;
            boolean isFind = false;
            for (String s : suffixes) {
                if (StringUtils.endsWith(lastWord, s)) {
                    isFind = true;
                    endWord = s;
                    break;
                }
            }
            if (!isFind) {
                return MagicWordResult.failResult;
            }
            String tmp = candidate.substring(0, candidate.length() - 1);
            for (String s : prefixes) {
                if (StringUtils.endsWith(tmp, s)) {
                    return new MagicWordResult(dualWord + endWord, true);
                }
            }

            return MagicWordResult.failResult;
        }
    }

    static class MagicWordResult {

        public static final MagicWordResult failResult = new MagicWordResult(null, false);

        MagicWordResult(String word, boolean success) {
            this.word = word;
            this.success = success;
        }

        private boolean success;
        private String  word;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }
    }
}
