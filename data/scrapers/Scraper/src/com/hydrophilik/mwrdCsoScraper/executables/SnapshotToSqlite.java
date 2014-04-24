package com.hydrophilik.mwrdCsoScraper.executables;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;

import com.hydrophilik.mwrdCsoScraper.Configures;
import com.hydrophilik.mwrdCsoScraper.db.DbConnection;
import com.hydrophilik.mwrdCsoScraper.parsing.CsoEvent;
import com.hydrophilik.mwrdCsoScraper.utils.DateTimeUtils;
import com.hydrophilik.mwrdCsoScraper.utils.FileManager;
import com.hydrophilik.mwrdCsoScraper.utils.LogLogger;

public class SnapshotToSqlite {
	
	public static String sqliteDbName = "cso-data.db";

	public static void main(String[] args) {

		Configures configuration = null;
		String workingDir = null;
		String databasePath = null;


		// arg0 => workingDir
		// arg1 => directory to database file
		try {
			System.out.println("Starting " + (new DateTime(DateTimeUtils.chiTimeZone)).toString());
			workingDir = args[0];
			databasePath = args[1] + "/" + "cso-data.db";
			
			FileManager.setWorkingDirectory(workingDir);
			configuration = new Configures(workingDir);
			
			List<CsoEvent> allCsoEvents = getCsoEventsFromRds(configuration);

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
	
	private static List<CsoEvent> getCsoEventsFromRds(Configures configuration) {
		
		DbConnection dbConn = null;

		List<CsoEvent> allCsoEvents = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = new DbConnection(configuration);
			allCsoEvents = dbConn.getAllEventsInDb();
			
			return allCsoEvents;

		}
		catch(Exception e) {
			
		}
		finally {
			if (null != dbConn)
				dbConn.releaseConnection();
		}

		
		return null;
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
			String sql = "CREATE TABLE CsoEvents (Id int(11), Date text, OutfallLocation text, WaterwaySegment int(11), StartTime text, EndTime text, Duration int(11));";
			
			stmt.executeUpdate(sql);
    	
			for (CsoEvent csoEvent : allCsoEvents) {
				stmt = sqlLiteConn.createStatement();
				sql = csoEvent.getSqlInsert();
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

}
