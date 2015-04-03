package org.opengovhacknight.utils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public abstract class DateTimeHelpers {

    // Argument dateStr expected in format MM/DD/YYYY
    public static LocalDate convertDateString(String dateStr) throws Exception {

        String [] dateSplit = dateStr.split("/");
        return new LocalDate(Integer.parseInt(dateSplit[2]), Integer.parseInt(dateSplit[0]),
                Integer.parseInt(dateSplit[1]));

    }

    public static DateTime createDateTime(String dateStr, String timeStr) {
        String [] dateArray = dateStr.split("/");

        String [] timeArray = timeStr.split(":");

        if (3 == dateArray.length) {

            return new DateTime(Integer.parseInt(dateArray[2]),
                    Integer.parseInt(dateArray[0]),
                    Integer.parseInt(dateArray[1]),
                    Integer.parseInt(timeArray[0]),
                    Integer.parseInt(timeArray[1]));
        }

        dateArray = dateStr.split("-");

        if (3 != dateArray.length)
            return null;

        return new DateTime(Integer.parseInt(dateArray[0]),
                Integer.parseInt(dateArray[1]),
                Integer.parseInt(dateArray[2]),
                Integer.parseInt(timeArray[0]),
                Integer.parseInt(timeArray[1]));

    }

    public static int daylightSavingsDay(LocalDate localDate) {
        // (int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour, int secondOfMinute, DateTimeZone zone)
        DateTime start = new DateTime(localDate.year().get(), localDate.monthOfYear().get(), localDate.dayOfMonth().get(), 0, 0, 0);

        DateTime end = start.plusHours(3);
        if (4 == end.hourOfDay().get()) {
            return -1;
        }
        if (2 == end.hourOfDay().get()) {
            return 1;
        }

        return 0;
    }

    public static boolean timeInInterval(DateTime start,
                                         DateTime end, String timeArg) {
        String [] timeSplit = timeArg.split(":");
        if (2 != timeSplit.length) {
            return false;
        }

        DateTime time = new DateTime(start.year().get(), start.monthOfYear().get(), start.dayOfMonth().get(),
                Integer.parseInt(timeSplit[0]), Integer.parseInt(timeSplit[1]), 0);


        if ((time.isAfter(start)) && (time.isBefore(end)))
            return true;

        return false;

    }
    
	public static String getTimeAsHoursMins(LocalTime localTime) {
		String [] timeArray = localTime.toString().split(":");
		return timeArray[0] + ":" + timeArray[1];
	}

}
