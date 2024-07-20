package com.investmango.hrconsole.service;

public class StringUtils {

    public static boolean isNotEmpty(String str) {
        if (str != null) {
            return !str.trim().isEmpty();
        }
        return false;
    }

    public static boolean isEmpty(String str) {
        if (str != null) {
            return str.trim().isEmpty();
        }
        return true;
    }
}
