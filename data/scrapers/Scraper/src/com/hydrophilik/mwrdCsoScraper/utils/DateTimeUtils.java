package com.hydrophilik.mwrdCsoScraper.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

public abstract class DateTimeUtils {
	
	public static DateTimeZone chiTimeZone = DateTimeZone.forID("America/Chicago");
	
	
	public static DateTime createDateTime(String dateStr, String timeStr) {
		String [] dateArray = dateStr.split("/");
		String [] timeArray = timeStr.split(":");
		
		return new DateTime(Integer.parseInt(dateArray[2]),
				Integer.parseInt(dateArray[0]),
				Integer.parseInt(dateArray[1]),
				Integer.parseInt(timeArray[0]),
				Integer.parseInt(timeArray[1]),
				chiTimeZone);
	}
	
	public static boolean isSameDay(DateTime firstDay, DateTime secondDay) {
		if (firstDay.toLocalDate().toString().equals(secondDay.toLocalDate().toString()))
				return true;
		return false;
	}
	
	public static String getTimeAsHoursMins(LocalTime localTime) {
		String [] timeArray = localTime.toString().split(":");
		return timeArray[0] + ":" + timeArray[1];
	}
}
