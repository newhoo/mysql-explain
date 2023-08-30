package io.github.newhoo.mysql.util;

import java.lang.reflect.Array;

/**
 * StringUtils
 *
 * @author huzunrong
 * @since 1.0
 */
public class StringUtils {

    public static <T extends CharSequence> T defaultIfEmpty(final T str, final T defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean containsAny(CharSequence cs, CharSequence... searchCharSequences) {
        if (isEmpty(cs) || isEmptyArray(searchCharSequences)) {
            return false;
        }
        for (CharSequence searchCharSequence : searchCharSequences) {
            if (contains(cs, searchCharSequence)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(final CharSequence seq, final CharSequence searchSeq) {
        if (seq == null || searchSeq == null) {
            return false;
        }
        return indexOf(seq, searchSeq, 0) >= 0;
    }

    public static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
        return cs.toString().indexOf(searchChar.toString(), start);
    }

    public static boolean isEmptyArray(final Object[] array) {
        if (array == null) {
            return true;
        }
        return Array.getLength(array) == 0;
    }

    /*public static String base64Decode(String str) {
        if (isEmpty(str)) {
            return str;
        }
        try {
            return new String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return str;
        }
    }

    private static String base64Encode(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }*/
}