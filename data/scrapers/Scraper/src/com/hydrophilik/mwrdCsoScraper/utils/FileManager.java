package com.hydrophilik.mwrdCsoScraper.utils;

import org.opengovhacknight.utils.LogLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileManager {
	
	public static void setWorkingDirectory(String workingDirName) throws Exception {
		File workingDir = new File(workingDirName);
		if (false == workingDir.exists()) {
			throw (new Exception("Working directory does not exist"));
		}
		
	}
	
	public static void createDir(String directoryName) throws Exception {
		File directory = new File(directoryName);
		if (false == directory.exists()) {
			directory.mkdir();
		}
	}
	
	public static String readOneLineFile(String filePath) {
		
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(filePath));
			sCurrentLine = br.readLine();
			return sCurrentLine;
 
 
		} catch (IOException e) {
			LogLogger.logError(e);
			return null;
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {}
		}
	}

}
