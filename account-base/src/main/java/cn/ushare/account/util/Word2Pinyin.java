package cn.ushare.account.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Word2Pinyin {

    public static String toPinyin(String str) {
        String convert = "";
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < str.length(); i++) {
            char word = str.charAt(i);
            String[] pinyinArray = null;
            try {
                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word, format);
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
            if (pinyinArray != null) {
                // convert += pinyinArray[0].charAt(0);// 首字母
                convert += pinyinArray[0].toString();
            } else {
                convert += word;
            }
        }
        return convert;
    }

    public static boolean checkChar(String str) {
        for (int i = 0; i < str.length(); i++) {
            char word = str.charAt(i);
            if ((word + "").getBytes().length > 1) {
                return true;// 中文
            }
        }
        return false;
    }

}
