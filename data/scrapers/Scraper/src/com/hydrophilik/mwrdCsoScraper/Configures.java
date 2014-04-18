package com.hydrophilik.mwrdCsoScraper;

import com.hydrophilik.mwrdCsoScraper.utils.FileManager;

public class Configures {

	private String dbPath = null;
	
	private static String configFileName = "mwrdCsoScraperConfig.txt";
	
	public Configures(String workingDir) throws Exception {

		dbPath = FileManager.readOneLineFile(workingDir + "/" + configFileName);
		if (null == dbPath) {
			throw new Exception("Unable to find or read configuration file");
		}
		
	}
	
	public String getDbPath() {
		return dbPath;
	}
	
	
}
