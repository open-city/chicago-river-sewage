package com.hydrophilik.mwrdCsoScraper.db;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.hydrophilik.mwrdCsoScraper.Configures;
import com.hydrophilik.mwrdCsoScraper.parsing.CsoEvent;
import com.hydrophilik.mwrdCsoScraper.utils.LogLogger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


public class DbConnection {
	
	Connection connection = null;

	public DbConnection(Configures configuration) throws Exception {
	    try {
	        // this will load the MySQL driver, each DB has its own driver
	        Class.forName("com.mysql.jdbc.Driver");
	        // setup the connection with the DB.
	        connection = DriverManager.getConnection(configuration.getDbPath());
	    }
	    catch (Exception e) {
	    	releaseConnection();
	    	throw new Exception(e);
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

	      try {
		      PreparedStatement preparedStatement = connection.prepareStatement(statementString);
		      preparedStatement.executeUpdate();
	      }
	      catch (Exception e) {
	    	  LogLogger.logError(e);
	      }

	}
	
	public List<CsoEvent> getCsosAtDatePlace(String date, String outfallLocation) throws Exception {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM CsoEvents WHERE Date=? AND OutfallLocation=?");
		preparedStatement.setString(1, date);
		preparedStatement.setString(2, outfallLocation);
		
		ResultSet resultSet = preparedStatement.executeQuery();
		
		List<CsoEvent> retVal = new ArrayList<CsoEvent>();
		
		while (resultSet.next()) {
			Integer id = resultSet.getInt("id");
			int waterwaySeg = resultSet.getInt("WaterwaySegment");
			String startTime = resultSet.getString("StartTime");
			String endTime = resultSet.getString("EndTime");
			CsoEvent thisEvent = new CsoEvent(id, date, startTime, endTime,
					outfallLocation, waterwaySeg);

			retVal.add(thisEvent);
		}
		
		return retVal;

	}
}
