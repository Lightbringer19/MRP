package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

public class CheckDate {

    public static boolean checkPostsReset(String unlockTime) {
        LocalTime time = LocalTime.parse(getNowTime());
        String[] unlock = unlockTime.split(":");
        int unlockHour = Integer.parseInt(unlock[0]);
        return isBetween(time, LocalTime.of(unlockHour, 0), LocalTime.of(unlockHour, 59));
    }

    public static String getDateForFTP() {
        return new SimpleDateFormat("MMdd").format(new Date());
    }

    public static String getTimeForArt() {
        return new SimpleDateFormat("MMddHHmmss-").format(new Date());
    }

    public static String getNowTime() {
        return new SimpleDateFormat("HH:mm").format(new Date());
    }

    public static String getPlusOneHour(String time) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        Date d = df.parse(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MINUTE, 60);
        return df.format(cal.getTime());
    }

    static String getTimeForLog() {
        return new SimpleDateFormat("MM-dd-yy HH:mm:ss ").format(new Date());
    }

    public static String getTodayDate() {
        return new SimpleDateFormat("MM-dd-yy").format(new Date());
    }

    public static String getTodayForPost() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public static String getTomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat("MM-dd-yy").format(cal.getTime());
    }

    public static String getTwoDaysBack() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -2);
        return new SimpleDateFormat("MM-dd-yy").format(cal.getTime());
    }

    private static boolean isBetween(LocalTime candidate, LocalTime start, LocalTime end) {
        return !candidate.isBefore(start) && !candidate.isAfter(end); // Inclusive.
    }

    public static void main(String[] args) {
    }

}
