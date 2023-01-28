package ru.nightmirror.atlas.misc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

    private final static SimpleDateFormat FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    public static String getFormattedTime(long ms) {
        return FORMAT.format(new Date(ms));
    }

    public static String getFormattedTime() {
        return getFormattedTime(System.currentTimeMillis());
    }
}
