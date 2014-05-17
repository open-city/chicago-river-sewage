package com.hydrophilik.mwrdCsoScraper.parsing;

import org.joda.time.DateTime;

import com.hydrophilik.mwrdCsoScraper.utils.DateTimeUtils;

public class CsoEvent {
	
	public static final String seperator = ";;";
	
	private Integer id = null;
	
	private DateTime startTime = null;
	private DateTime endTime = null;
	private String outfallLocation = null;
	private int waterwaySegment;

	public CsoEvent(DateTime startTime, DateTime endTime,
			String outfallLocation, int waterwaySegment) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		this.outfallLocation = outfallLocation;
		this.waterwaySegment = waterwaySegment;
	}
	
	public CsoEvent(Integer id, String date, String startTime, String endTime,
			String outfallLocation, int waterwaySegment) {
		this.id = id;
		
		this.startTime = DateTimeUtils.createDateTime(date, startTime);
		this.endTime = DateTimeUtils.createDateTime(date, endTime);

		this.outfallLocation = outfallLocation;
		this.waterwaySegment = waterwaySegment;
	}

	public boolean isValid() {
		if (null == startTime)
			return false;
		return true;
	}
	
	public String parseToString() {
		
		long millisBetweenStartAndEnd = endTime.getMillis() - startTime.getMillis();
		String secondsBetweenStartAndEnd = Long.toString(millisBetweenStartAndEnd / 60000);

		return outfallLocation + ";" + waterwaySegment + ";" + startTime.toLocalDate().toString() +
				";" + DateTimeUtils.getTimeAsHoursMins(startTime.toLocalTime()) + ";" +
				DateTimeUtils.getTimeAsHoursMins(endTime.toLocalTime()) + ";" +
				secondsBetweenStartAndEnd;
	}
	
	public String getSqlInsert() {
		long millisBetweenStartAndEnd = endTime.getMillis() - startTime.getMillis();
		String secondsBetweenStartAndEnd = Long.toString(millisBetweenStartAndEnd / 60000);

		return "INSERT INTO CsoEvents (id,Date,OutfallLocation,WaterwaySegment,StartTime,EndTime,Duration) " +
				"VALUES (NULL, '" + startTime.toLocalDate().toString() + "','" + outfallLocation +
				"'," + waterwaySegment + ",'" + DateTimeUtils.getTimeAsHoursMins(startTime.toLocalTime()) +
				"','" + DateTimeUtils.getTimeAsHoursMins(endTime.toLocalTime()) + "'," +
				secondsBetweenStartAndEnd + ")";

	}
	
	public String getSqlRemove() {
		return "DELETE FROM CsoEvents WHERE id=" + id;
	}

	public String getKey() {
		return startTime.toLocalDate().toString() + seperator + outfallLocation;
	}
	
	public Integer getId(){
		return id;
	}

	public DateTime getStartTime() {
		return startTime;
	}

	public DateTime getEndTime() {
		return endTime;
	}
}