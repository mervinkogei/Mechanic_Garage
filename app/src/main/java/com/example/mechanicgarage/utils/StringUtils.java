package com.example.mechanicgarage.utils;

import android.text.TextUtils;

public class StringUtils {
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static String getValueOrEmpty(String string) {
        return (string != null) ? string : "";
    }
}
