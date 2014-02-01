package com.hydrophilik.cso;

import java.io.File;

public class Configuration {
	public static String readDirectory = "/Users/scottbeslow/Documents/Orange Wall/Hydrophilik/csohno/scrapedTest/";		
	public static String writeDirectory = "/Users/scottbeslow/Documents/Orange Wall/Hydrophilik/csohno/Data/";
	public static String eventsSuffix = "2007-2013";
	
	
	public static boolean setup() {
		
		File directory = new File(readDirectory);
		
		if (false == directory.exists()) {
			System.out.println(readDirectory + " does not exist.  There is nothing to read.  Please put stuff in there or change the read directory");
			return false;
		}
		
		directory = new File(writeDirectory);
		if (false == directory.exists()) {
			System.out.println(writeDirectory + " does not exist.  Please create it before running this script or change the write directory");
			return false;
		}
		
		return true;
		
	}
}
