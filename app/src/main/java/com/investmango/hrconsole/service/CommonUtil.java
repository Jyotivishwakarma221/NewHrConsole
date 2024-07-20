package com.investmango.hrconsole.service;

import android.content.Context;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class CommonUtil {
    private static final Logger logger = Logger.getLogger(CommonUtil.class.getName());

    public static String dateAndTimeFormat = "dd-MM-yyyy hh:mm a";
    private static final String dateFormat = "dd-MM-yyyy";
    private static final String timeFormat = "hh:mm a";

    private static final DateFormat dateAndTimeFormatter = new SimpleDateFormat(dateAndTimeFormat);
    private static final DateFormat dateFormatter = new SimpleDateFormat(dateFormat);
    private static final DateFormat timeFormatter = new SimpleDateFormat(timeFormat);


//    public static long getLongFromStringDateFormat(String dateAndTime) {
//        if (StringUtils.isNotEmpty(dateAndTime)) {
//            try {
//                Date date = dateAndTimeFormatter.parse(dateAndTime);
//                if (date != null) {
//                    return date.getTime();
//                } else {
//                    return 0;
//                }
//            } catch (ParseException e) {
//                logger.warning(e.getMessage());
//                return 0;
//            }
//        } else {
//            return 0;
//        }
//
//    }
//
//    public static String getStringDate(long milliseconds) {
//        if (milliseconds > 0) {
//            return dateAndTimeFormatter.format(new Date(milliseconds));
//        }
//        return "";
//    }
//
//    public static float twoTimeDiff(long inTime, long outTime) {
//        final long MILLI_TO_HOUR = 1000 * 60 * 60;
//        Date in = new Date(inTime);
//        Date out = new Date(outTime);
//
//        long inLong = in.getTime();
//        long outLong = out.getTime();
//
//        float diff = (float) ((outLong - inLong) / MILLI_TO_HOUR);
//        return diff;
//    }

    public static String getDate(long inTime) {
        return dateFormatter.format(new Date(inTime));
    }

    public static String getDate() {
        return dateFormatter.format(new Date());
    }

    public static String getTime(long inTime) {
        return timeFormatter.format(new Date(inTime));
    }

    public static String getTime() {
        return timeFormatter.format(new Date());
    }

//    public static boolean compareInTime(long inTime) {
//        try {
//            Date date = new Date(inTime);
//            Date date1 = timeFormatter.parse("10:00 am");
//
//            if (date.after(date1)) {
//                return true;
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    public static boolean compareOutTime(long outTime) {
//        try {
//            Date date = new Date(outTime);
//            Date date1 = timeFormatter.parse("06:30 pm");
//
//            if (date.after(date1)) {
//                return true;
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    //    public static boolean isAttendanceOnTime(long timestamp, String compareWith, String inOutString) {
//        try {
//            if (timestamp > 0) {
//                String time = timeFormatter.format(new Date(timestamp));
//                Date date1 = timeFormatter.parse(time);
//                Date date2 = timeFormatter.parse(compareWith);
//                if (inOutString.equalsIgnoreCase("IN_TIME")) {
//                    if (Objects.requireNonNull(date1).getTime() <= Objects.requireNonNull(date2).getTime()) {
//                        return true;
//                    }
//                } else if (inOutString.equalsIgnoreCase("OUT_TIME")) {
//                    if (Objects.requireNonNull(date1).getTime() >= Objects.requireNonNull(date2).getTime()) {
//                        return true;
//                    }
//                }
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static boolean isAttendanceOnTime(long timestamp, String compareWith, String inOutString) {
        try {
            if (timestamp > 0) {
                String time = timeFormatter.format(new Date(timestamp));
                Date date1 = timeFormatter.parse(time);
                Date date2 = timeFormatter.parse(compareWith);
                if (inOutString.equalsIgnoreCase("IN_TIME")) {
                    if (date1.getTime() <= date2.getTime()) {
                        return true;
                    }
                } else if (inOutString.equalsIgnoreCase("OUT_TIME")) {
                    if (date1.getTime() >= date2.getTime()) {
                        return true;
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}

