package org.opengovhacknight.utils;

import com.hydrophilik.mwrdCsoScraper.utils.DateTimeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.LocalDate;

import org.opengovhacknight.db.DbConnection;
import org.opengovhacknight.parsing.Scrape;


public class LogLogger {
	
	public static void logError(String message) {
        DbConnection dbConn = Scrape.getDbConn();
		if (null == dbConn)
			return;
		dbConn.executeUpdate("INSERT INTO Logs (Id, Date, Type, Message) VALUES ("
				+ "NULL, '" + (new LocalDate(DateTimeUtils.chiTimeZone)).toString() + "',1,'" + message + "')");
	}
	
	public static void logError(Exception e) {
		logError(ExceptionUtils.getStackTrace(e));
	}
	
	public static void log(String message) {
        DbConnection dbConn = Scrape.getDbConn();
		dbConn.executeUpdate("INSERT INTO Logs (Id, Date, Type, Message) VALUES ("
				+ "NULL, '" + (new LocalDate(DateTimeUtils.chiTimeZone)).toString() + "',0,'" + message + "')");
		
	}
}