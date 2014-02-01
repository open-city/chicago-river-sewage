package com.hydrophilik.cso.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class BezTime {
	
	private Calendar dateTime;
	String debugString = null; //TODO: For debugging, remove this
	
	public BezTime(String date, String time) {
		this(date);
		String [] timeParts = time.split(":");
		dateTime.set(Calendar.HOUR, Integer.parseInt(timeParts[0]));
		dateTime.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
		debugString = parseDate() + " " + parseTime();
		
	}
	
	public BezTime(String date) {
		super();
		String [] dateParts = date.split("/");
		dateTime = new GregorianCalendar(Integer.parseInt(dateParts[2]),
				Integer.parseInt(dateParts[0]) - 1, // month is zero-based
				Integer.parseInt(dateParts[1]));
		
		dateTime.setTimeZone(TimeZone.getTimeZone("CST"));
		
		debugString = parseDate() + " " + parseTime();

	}
	
	public String parseTime() {
		return Integer.toString(dateTime.get(Calendar.HOUR_OF_DAY)) + ":" +
				Integer.toString(dateTime.get(Calendar.MINUTE));
	}
	
	public String parseDate() {
		return Integer.toString(dateTime.get(Calendar.MONTH) + 1) + "/" +
				Integer.toString(dateTime.get(Calendar.DAY_OF_MONTH)) + "/" +
				Integer.toString(dateTime.get(Calendar.YEAR));
						
	}
	
	public void addMinutes(int minutes) {
		dateTime.add(Calendar.MINUTE, minutes);
		debugString = parseDate() + " " + parseTime();
	}
	
	public boolean sameDayAs(BezTime otherTime) {
		if (parseDate().equals(otherTime.parseDate())) {
			return true;
		}
		
		return false;
		
	}
	
	public long getTimeInMillis() {
		return dateTime.getTimeInMillis();
	}
	
	public int getMinutesUntil(BezTime laterTime) {
		long timeInMillis = laterTime.getTimeInMillis() - getTimeInMillis();
		return (int) timeInMillis / 60000;
	}
	
	public void incrementDay() {
		dateTime.add(Calendar.DAY_OF_MONTH, 1);
		debugString = parseDate() + " " + parseTime();
	}

}
