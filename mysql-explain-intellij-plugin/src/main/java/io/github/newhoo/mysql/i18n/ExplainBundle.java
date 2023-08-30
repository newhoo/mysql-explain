package io.github.newhoo.mysql.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * i18n
 *
 * @author huzunrong
 * @since 1.0.5
 */
public class ExplainBundle {

    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages.explain", getLocale());

    private static Locale getLocale() {
        String lang = Locale.getDefault().getLanguage();
        if (lang.equals(Locale.ENGLISH.getLanguage()) || lang.equals(Locale.CHINESE.getLanguage())) {
            return Locale.getDefault();
        }
        return Locale.ENGLISH;
    }

    public static String getMessage(String key) {
        return resourceBundle.getString(key).trim();
    }

//    public static String message(String key, Object... params) {
//        return CommonBundle.message(resourceBundle, key, params).trim();
//    }
}
