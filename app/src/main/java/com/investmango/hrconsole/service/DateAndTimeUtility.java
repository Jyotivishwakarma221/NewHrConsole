package com.investmango.hrconsole.service;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DateAndTimeUtility {
    public static String dateAndTimeFormat = "dd-MM-yyyy hh:mm a";
    private static String dateFormat = "dd-MM-yyyy";
    private static String timeFormat = "hh:mm a";
    public static DateFormat dateAndTimeFormatter = new SimpleDateFormat(dateAndTimeFormat);
    private static DateFormat dateFormatter = new SimpleDateFormat(dateFormat);
    private static DateFormat timeFormatter = new SimpleDateFormat(timeFormat);

    /**
     * Get date and time in string format from long milliseconds
     *
     * @param milliseconds
     * @return
     */
    public static String getDateAndTimeFromLong(Long milliseconds) {
        long length = (long) (Math.log10(milliseconds) + 1);
        Date d;
        if (length == 10) {
            length = milliseconds * 1000l;
            d = new Date(length);
        } else {
            d = new Date(milliseconds);
        }
        return dateAndTimeFormatter.format(d);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long monthYearToEpoch(int month, int year) {
        // Create a LocalDate object for the first day of the given month and year
        LocalDate date = null;
        date = LocalDate.of(year, month, 1);

        // Convert the LocalDate object to epoch time (seconds since 1970-01-01T00:00:00Z)
        return date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
    }

    public static boolean compareInTime(long inTime) {
        try {
            Date date = new Date(inTime);
            Date date1 = timeFormatter.parse("10:15 am");
            if (date.after(date1)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean compareOutTime(long outTime) {
        try {
            Date date = new Date(outTime);
            Date date1 = timeFormatter.parse("06:30 pm");
            if (date.after(date1)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static long convertTimeStringToLong(String timeString) throws Exception {
        String pattern = "HH:mm ";

        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Date date = dateFormat.parse(timeString);
        return date.getTime();
    }

    public static boolean twoTimeDiff(String inTime, String outTime) {
        try {
            final int MILLI_TO_HOUR = 1000 * 60 * 60;
            Date in = timeFormatter.parse(inTime);
            Date out = timeFormatter.parse(outTime);

            float diff = (Objects.requireNonNull(out).getTime() - Objects.requireNonNull(in).getTime()) / MILLI_TO_HOUR;
            if (diff > 8.5) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get current date and time in string format
     *
     * @return
     */
    public static String getCurrentDateAndTime() {
        return dateAndTimeFormatter.format(new Date());
    }

    /**
     * Get long milliseconds from String date and time
     *
     * @param dateAndTime
     * @return
     */
    public static Long getLongFromStringDateFormat(String dateAndTime) {
        Date date = null;
        try {
            date = (Date) dateAndTimeFormatter.parse(dateAndTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Objects.requireNonNull(date).getTime();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long convertToEpochMillis(String dateStr, String time24Hour) {
        DateTimeFormatter[] dateFormatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd/M/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("d/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("dd-M-yyyy"),
                DateTimeFormatter.ofPattern("d-MM-yyyy"),
                DateTimeFormatter.ofPattern("d-M-yyyy"),
        };
        DateTimeFormatter[] timeFormatters = {DateTimeFormatter.ofPattern("HH:mm"), DateTimeFormatter.ofPattern("hh:mm a"), DateTimeFormatter.ofPattern("hh:mm a"),};
        dateStr = dateStr.trim();
        time24Hour = time24Hour.trim();

        LocalDate date = null;
        for (DateTimeFormatter dateFormatter : dateFormatters) {
            try {
                date = LocalDate.parse(dateStr, dateFormatter);
                break; // If parsing is successful, exit the loop
            } catch (DateTimeParseException e) {
                // Continue to the next formatter
            }
        }

        if (date == null) {
            Log.e("TAG", "convertToEpochMillis: " + new IllegalArgumentException("Invalid date format: " + dateStr));
        }

        // Parse the time
        LocalTime time = null;
        for (DateTimeFormatter timeFormatter : timeFormatters) {
            try {
                time = LocalTime.parse(time24Hour, timeFormatter);
                break; // If parsing is successful, exit the loop
            } catch (DateTimeParseException e) {
                // Continue to the next formatter
            }
        }
        if (time == null) {
            Log.e("TAG", "convertToEpochMillis: " + time);
        }
        // Combine date and time to create LocalDateTime
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        // Convert LocalDateTime to epoch milliseconds
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long convertToEpochMillis(String dateStr) {
        DateTimeFormatter[] dateFormatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd/M/yyyy"),
                DateTimeFormatter.ofPattern("d/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("dd-M-yyyy"),
                DateTimeFormatter.ofPattern("d-MM-yyyy"),
                DateTimeFormatter.ofPattern("d-M-yyyy"),
        };

        LocalDate date = null;
        for (DateTimeFormatter dateFormatter : dateFormatters) {
            try {
                date = LocalDate.parse(dateStr, dateFormatter);
                break; // If parsing is successful, exit the loop
            } catch (DateTimeParseException e) {
                // Continue to the next formatter
                Log.e("expction", "Failed to parse with pattern " + dateFormatter + ": " + e.getMessage());
            }
        }

        if (date == null) {
            System.err.println("Invalid date format. Please use one of the supported formats.");
            return -1;
        }

        // Convert LocalDate to LocalDateTime
        LocalDateTime dateTime = date.atStartOfDay();

        // Convert LocalDateTime to Instant
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();

        // Convert Instant to epoch milliseconds
        return instant.toEpochMilli();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long getCurrentEpochTime() {
        // Get the current date and time this is not in miliseconds
        ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
        // Convert the current date and time to epoch time (seconds since 1970-01-01T00:00:00Z)
        return currentDateTime.toEpochSecond();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long getCurrentinMili() {
        long epochTimeInMilliseconds = Instant.now().toEpochMilli();
        return epochTimeInMilliseconds;
    }

    public static Long getLongFromDate(String date) throws ParseException {
        Date dt = (Date) dateFormatter.parse(date);
        return Objects.requireNonNull(dt).getTime();
    }

    public static int getDifferenceOfTwoDateInHour(Long startDate, Long endDate) {
        long diff = endDate - startDate;
        int timeInHour = (int) (diff / (3600000));
        return timeInHour;
    }

    public static int getDifferenceOfTwoDateInHour(String startDate, String endDate) {

        long diff = 0;
        diff = DateAndTimeUtility.getLongFromStringDateFormat(endDate) - DateAndTimeUtility.getLongFromStringDateFormat(startDate);
        int timeInHour = (int) (diff / (3600000));
        return timeInHour;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public static long dateToEpoch(String dateString) {
        try {
            String format = "d/M/yyyy";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDate localDate = LocalDate.parse(dateString, formatter);
            return localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public static long getDateFromLong(Long milliseconds) {
        String date = new SimpleDateFormat("dd").format(new Date(milliseconds));
        return Long.parseLong(date);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getRelativeTime(Long epochTime) {

        Instant epochInstant = Instant.ofEpochMilli(epochTime);

        // Get the current time as an Instant
        Instant now = Instant.now();

        // Calculate the duration between the epoch time and now
        Duration duration = Duration.between(epochInstant, now);

        // Get the relative time in days, hours, minutes, and seconds
        long totalSeconds = duration.getSeconds();
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        // Determine the appropriate time unit to display
        if (days > 0) {
            return days + " days ago";
        } else if (hours > 0) {
            return hours + " hours ago";
        } else if (minutes > 0) {
            return minutes + " mins ago";
        } else {
            return seconds + " seconds ago";
        }
    }

    public static String getDateMonthFromLong(Long milliseconds) {
        String date = new SimpleDateFormat("dd LLL").format(new Date(milliseconds));
        return date;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Integer getMonthNumber(Long milliseconds) {
        LocalDate localDate = Instant.ofEpochMilli(milliseconds)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Get month (January is 1, February is 2, etc.)
        int month = localDate.getMonthValue();
        return month;
    }

    public static String convertEpochToTime(Long epoch) {
        Date date = new Date(epoch); // Convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("hh a", Locale.getDefault());
        return sdf.format(date);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getDateeFromLong(Long milliseconds) {
        Instant instant = Instant.ofEpochMilli(milliseconds);

        // Convert Instant to ZonedDateTime using the system default time zone
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

        // Extract the LocalDate from ZonedDateTime
        return String.valueOf(zonedDateTime.toLocalDate());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getDATEFromLong(Long milliseconds) {
        if (milliseconds!=0) {
            Instant instant = Instant.ofEpochMilli(milliseconds);

            // Convert the Instant to a ZonedDateTime
            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());

            // Define the desired date format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            // Format the ZonedDateTime
            String formattedDate = zdt.format(formatter);
            return formattedDate;
        }else return "--";
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getTimeInHourFromLong(Long milliseconds) {
        Instant instant = Instant.ofEpochMilli(milliseconds);

        // Convert the Instant to a ZonedDateTime
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formattedtime = zdt.format(formatter);

        return formattedtime;
    }

    public static String getTimeIn24HourFromLong(Long milliseconds) {
        return new SimpleDateFormat("HH:MM ").format(new Date(milliseconds));
    }

    public static String getDate(String inTime) throws ParseException {
        return dateFormatter.format(Objects.requireNonNull(dateAndTimeFormatter.parse(inTime)));
    }

    public static String getDate() {
        String date = null;
        try {
            date = dateFormatter.format(Objects.requireNonNull(dateAndTimeFormatter.parse(getCurrentDateAndTime())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


}
