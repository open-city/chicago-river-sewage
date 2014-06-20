package com.hydrophilik.mwrdCsoScraper.executables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;

import com.hydrophilik.mwrdCsoScraper.parsing.CsoEvent;
import com.hydrophilik.mwrdCsoScraper.utils.DateTimeUtils;
import com.hydrophilik.mwrdCsoScraper.utils.FileManager;
import com.hydrophilik.mwrdCsoScraper.utils.LogLogger;

public class SnapshotToSqlite {
	
	public static String sqliteDbName = "cso-data.db";

	public static void main(String[] args) {

		String workingDir = null;
		String databasePath = null;


		// arg0 => workingDir
		// arg1 => directory to database file
		try {
			System.out.println("Starting " + (new DateTime(DateTimeUtils.chiTimeZone)).toString());
			workingDir = args[0];
			databasePath = args[1] + "/" + "cso-data.db";
			
			FileManager.setWorkingDirectory(workingDir);
			
			List<CsoEvent> allCsoEvents = readEventsFromCsv(workingDir);

			deleteAndRecreateSqliteDb(databasePath);
			
			writeToSqlite(databasePath, allCsoEvents);
			
			System.out.println("Ending  " + (new DateTime(DateTimeUtils.chiTimeZone)).toString());
		}
		catch (Exception e) {
			LogLogger.logError(ExceptionUtils.getStackTrace(e));
			return;
		}
		finally {
			try {

				System.out.println("Ending finally");
			}
			catch (Exception e) {LogLogger.logError(e);}
		}
	}
	
	private static void deleteAndRecreateSqliteDb(String databasePath) throws Exception {
		Runtime.getRuntime().exec("rm -rf " + databasePath);
		Runtime.getRuntime().exec("sqlite3 " + databasePath);
		
	}
	
	private static void writeToSqlite(String databasePath, List<CsoEvent> allCsoEvents) {
		Statement stmt = null;
		
		Connection sqlLiteConn = null;
		
		try {
	    	Class.forName("org.sqlite.JDBC");
	    	sqlLiteConn = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
	    	
	    	stmt = sqlLiteConn.createStatement();
			//String sql = "CREATE TABLE CsoEvents (Id int(11), Date text, OutfallLocation text, WaterwaySegment int(11), StartTime text, EndTime text, Duration int(11));";
			String sql = "CREATE TABLE CSOs (Id INTEGER PRIMARY KEY, Location text, Segment int(11), Date text, StartTime text, EndTime text, Duration int(11));";
			
			stmt.executeUpdate(sql);
    	
			for (CsoEvent csoEvent : allCsoEvents) {
				stmt = sqlLiteConn.createStatement();
				sql = csoEvent.getSqlInsertSqlite();
				System.out.println(sql);
				
				try {
				
					stmt.executeUpdate(sql);
					
				}
				catch (Exception e) {
					System.out.println("Unable to add\n" + sql);
					continue;
				}

			}
		}
		catch (Exception e) {
			LogLogger.logError(e);
		}
		finally {
			try {
				if (null != sqlLiteConn)
					sqlLiteConn.close();
				if (null != stmt) {
					stmt.close();
				}
			} catch (Exception e) {LogLogger.logError(e);}
		}
		
	}
	
	private static List<CsoEvent> readEventsFromCsv(String workingDir) {
		List<CsoEvent> retVal = new ArrayList<CsoEvent>();
		
		File csvFile = new File(workingDir + "/csoEvents.csv");
		
		if (false == csvFile.exists())
			return null;
		
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(csvFile));
			
			// Get rid of first line which contains 
			sCurrentLine = br.readLine();
 
			while ((sCurrentLine = br.readLine()) != null) {
				String [] split = sCurrentLine.split(";");
				DateTime startTime = DateTimeUtils.createDateTime(split[1], split[4]);
				DateTime endTime = DateTimeUtils.createDateTime(split[1], split[5]);
				CsoEvent event = new CsoEvent(startTime, endTime, split[2], Integer.parseInt(split[3]));
				retVal.add(event);
				
			}
 
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return retVal;
	}

}
