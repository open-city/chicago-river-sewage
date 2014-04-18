package com.hydrophilik.mwrdCsoScraper.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.LocalDate;

import com.hydrophilik.mwrdCsoScraper.executables.DailyScraper;

public class LogLogger {
	
	public static void logError(String message) {
		if (null == DailyScraper.dbConn)
			return;
		DailyScraper.dbConn.executeUpdate("INSERT INTO Logs (Id, Date, Type, Message) VALUES ("
				+ "NULL, '" + (new LocalDate(DateTimeUtils.chiTimeZone)).toString() + "',1,'" + message + "')");
	}
	
	public static void logError(Exception e) {
		logError(ExceptionUtils.getStackTrace(e));
	}
	
	public static void log(String message) {
		DailyScraper.dbConn.executeUpdate("INSERT INTO Logs (Id, Date, Type, Message) VALUES ("
				+ "NULL, '" + (new LocalDate(DateTimeUtils.chiTimeZone)).toString() + "',0,'" + message + "')");
		
	}

}
