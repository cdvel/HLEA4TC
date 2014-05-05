/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cdario.hlea4tc;

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
