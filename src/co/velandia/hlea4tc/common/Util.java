package co.velandia.hlea4tc.common;

import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

/**
 *
 * @author cesar
 */
public class Util {

    public static String timestamp() {
        StringBuilder sb = new StringBuilder();
        Formatter format = new Formatter(sb, Locale.ENGLISH);
        format.format("%tT ", Calendar.getInstance());
        return sb.toString();
    }
}
