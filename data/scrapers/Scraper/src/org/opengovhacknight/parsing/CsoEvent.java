package org.opengovhacknight.parsing;

import org.joda.time.DateTime;
import org.opengovhacknight.utils.DateTimeHelpers;

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
		
		this.startTime = DateTimeHelpers.createDateTime(date, startTime);
		this.endTime = DateTimeHelpers.createDateTime(date, endTime);

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
				";" + DateTimeHelpers.getTimeAsHoursMins(startTime.toLocalTime()) + ";" +
				DateTimeHelpers.getTimeAsHoursMins(endTime.toLocalTime()) + ";" +
				secondsBetweenStartAndEnd;
	}

	public String getSqlInsertSqlite() {
		long millisBetweenStartAndEnd = endTime.getMillis() - startTime.getMillis();
		String secondsBetweenStartAndEnd = Long.toString(millisBetweenStartAndEnd / 60000);

		return "INSERT INTO CSOs (id,Location,Segment,Date,StartTime,EndTime,Duration) " +
				"VALUES (NULL, '" + outfallLocation +
				"'," + waterwaySegment + ",'" + startTime.toLocalDate().toString() + "','" + DateTimeHelpers.getTimeAsHoursMins(startTime.toLocalTime()) +
				"','" + DateTimeHelpers.getTimeAsHoursMins(endTime.toLocalTime()) + "'," +
				secondsBetweenStartAndEnd + ")";

	}
	
	public String getSqlRemove() {
		return "DELETE FROM CSOs WHERE id=" + id;
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
	
	public boolean sameEvent(CsoEvent otherEvent) {
		if (false == startTime.toString().equals(otherEvent.getStartTime().toString())) {
			return false;
		}
		
		if (false == endTime.toString().equals(otherEvent.getEndTime().toString())) {
			return false;
		}
		
		return true;
	}
	
	public boolean overlap(CsoEvent otherEvent) {
		
		if (startTime.isAfter(otherEvent.getEndTime())) {
			return false;
		}
		
		if (endTime.isBefore(otherEvent.getStartTime())) {
			return false;
		}
		
		return true;
		

	}

}