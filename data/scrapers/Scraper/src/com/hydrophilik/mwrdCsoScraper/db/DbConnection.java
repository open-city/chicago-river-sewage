package com.hydrophilik.mwrdCsoScraper.db;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.hydrophilik.mwrdCsoScraper.parsing.CsoEvent;
import com.hydrophilik.mwrdCsoScraper.utils.LogLogger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class DbConnection {
	
	Connection connection = null;
	
	public DbConnection(String databasePath) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
		}
		catch (Exception e) {
			releaseConnection();
		}
	}
	
	public void releaseConnection() {
		try {
			if (null != connection)
				connection.close();
			}
		catch (Exception e) {}
	}
	
	public void executeUpdate(String statementString) {

		PreparedStatement preparedStatement = null;
	      try {
		      preparedStatement = connection.prepareStatement(statementString);
		      preparedStatement.executeUpdate();
	      }
	      catch (Exception e) {
	    	  LogLogger.logError(e);
	      }
	      finally {
	    	  try {preparedStatement.close();} catch(Exception e) {}
	      }
	}
	
	public List<CsoEvent> getCsosAtDatePlace(String date, String outfallLocation) {
		
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(
					"SELECT * FROM CSOs WHERE Date=? AND Location=?");
			preparedStatement.setString(1, date);
			preparedStatement.setString(2, outfallLocation);
			
			resultSet = preparedStatement.executeQuery();
			
			List<CsoEvent> retVal = new ArrayList<CsoEvent>();
			
			while (resultSet.next()) {
				Integer id = resultSet.getInt("Id");
				int waterwaySeg = resultSet.getInt("Segment");
				String startTime = resultSet.getString("StartTime");
				String endTime = resultSet.getString("EndTime");
				CsoEvent thisEvent = new CsoEvent(id, date, startTime, endTime,
						outfallLocation, waterwaySeg);
	
				retVal.add(thisEvent);
			}
			
			return retVal;
		}
		catch (Exception e) {
			LogLogger.logError(e);
			return null;
		}
		finally {
			try {
				if (null != preparedStatement)
					preparedStatement.close();
				if (null != resultSet)
					resultSet.close();
			} catch (Exception e) {}
		}

	}
	
	public List<CsoEvent> getAllEventsInDb() throws Exception {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM CSOs");
		
		ResultSet resultSet = preparedStatement.executeQuery();
		
		List<CsoEvent> retVal = new ArrayList<CsoEvent>();
		
		while (resultSet.next()) {
			Integer id = resultSet.getInt("Id");
			String date = resultSet.getString("Date");
			int waterwaySeg = resultSet.getInt("WaterwaySegment");
			String outfallLocation = resultSet.getString("OutfallLocation");
			String startTime = resultSet.getString("StartTime");
			String endTime = resultSet.getString("EndTime");
			CsoEvent thisEvent = new CsoEvent(id, date, startTime, endTime,
					outfallLocation, waterwaySeg);

			retVal.add(thisEvent);
		}
		
		return retVal;
		
	}
}
