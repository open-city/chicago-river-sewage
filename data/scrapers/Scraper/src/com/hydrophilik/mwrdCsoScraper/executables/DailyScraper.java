package com.hydrophilik.mwrdCsoScraper.executables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.LocalDate;

import com.hydrophilik.mwrdCsoScraper.Configures;
import com.hydrophilik.mwrdCsoScraper.db.DbConnection;
import com.hydrophilik.mwrdCsoScraper.parsing.CsoEvent;
import com.hydrophilik.mwrdCsoScraper.scrapeSite.Scraper;
import com.hydrophilik.mwrdCsoScraper.utils.DateTimeUtils;
import com.hydrophilik.mwrdCsoScraper.utils.LogLogger;
import com.hydrophilik.mwrdCsoScraper.utils.FileManager;

public class DailyScraper {
	
	public static DbConnection dbConn = null;

	public static void main(String[] args) {

		String workingDir;
		Configures configuration = null;
		// arg0 => working directory
		try {
			workingDir = args[0];
			FileManager.setWorkingDirectory(workingDir);
			configuration = new Configures(workingDir);
			dbConn = new DbConnection(configuration);
			LogLogger.log("Running scraper on: " + (new LocalDate()).toString());
		}
		catch (Exception e) {
			LogLogger.logError(ExceptionUtils.getStackTrace(e));
			return;
		}
		
		scrapeLastThirtyDays();
		if (null != dbConn)
			dbConn.releaseConnection();
	}
	
	private static void scrapeLastThirtyDays() {
		
		//int DAYS_AWAY = 1;
		
		//LocalDate today = new LocalDate(2014, 4, 10);
		LocalDate today = new LocalDate(DateTimeUtils.chiTimeZone);
		LocalDate date = new LocalDate(2007,1,1);

		//LocalDate date = today.minusDays(DAYS_AWAY);
		
		while (false == DateTimeUtils.isSameDay(today, date)) {
			
			Map<String, List<CsoEvent>> csoEvents = null;
			try {
				csoEvents = Scraper.grabEventFromSite(date);
				List<String> sqlCommands = convertEventsToSql(csoEvents);
				for (String sqlCommand : sqlCommands) {
					dbConn.executeUpdate(sqlCommand);
				}
			}
			catch (Exception e) {
				LogLogger.logError(e);
			}
			
			date = date.plusDays(1);
		}
	}
	
	private static List<String> convertEventsToSql(Map<String, List<CsoEvent>> csoEvents) {
		
		List<String> retVal = new ArrayList<String>();

		for (String key : csoEvents.keySet()) {
			String [] keySplit = key.split(CsoEvent.seperator);
			String date = keySplit[0];
			String outfallLocation = keySplit[1];
			
			List<CsoEvent> csoEventsFromDb = null;
			try {
				csoEventsFromDb = dbConn.getCsosAtDatePlace(date, outfallLocation);
			}
			catch (Exception e) {
				LogLogger.logError(e);
				continue;
			}
			
			List<CsoEvent> scrapedEvents = csoEvents.get(key);
			if ((null == csoEventsFromDb) || (0 == csoEventsFromDb.size())) {
				for (CsoEvent scrapedEvent : scrapedEvents) {
					retVal.add(scrapedEvent.getSqlInsert());
				}
			}
			else {
				for (CsoEvent scrapedEvent : scrapedEvents) {
					
					for (CsoEvent dbEvent : csoEventsFromDb) {
						if (sameEvent(scrapedEvent, dbEvent)) {
							continue;
						}
						if (overlap(scrapedEvent, dbEvent)) {
							LogLogger.log("The website updated event: " + scrapedEvent.getKey());
							String sqlCmd = dbEvent.getSqlRemove();
							retVal.add(sqlCmd);
							
						}
						retVal.add(scrapedEvent.getSqlInsert());
					}
				}
			}
		}
		
		return retVal;
	}
	
	private static boolean overlap(CsoEvent event1, CsoEvent event2) {
		if (event1.getStartTime().isBefore(event2.getStartTime())) {
			if (false == event1.getEndTime().isBefore(event2.getStartTime())) {
				return true;
			}
		}
		else {
			if (false == event2.getEndTime().isBefore(event1.getStartTime())) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean sameEvent(CsoEvent event1, CsoEvent event2) {
		if (false == event1.getStartTime().toString().equals(event2.getStartTime().toString())) {
			return false;
		}
		
		if (false == event1.getEndTime().toString().equals(event2.getEndTime().toString())) {
			return false;
		}
		
		return true;
	}
}