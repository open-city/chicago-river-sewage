package com.hydrophilik.mwrdCsoScraper.parsing;

import com.orangewall.bezutils.beztime.BezCal;

public class CsoEvent {
	private BezCal startTime = null;
	private BezCal endTime = null;
	private String outfallLocation = null;
	private int waterwaySegment;

	public CsoEvent(BezCal startTime, BezCal endTime,
			String outfallLocation, int waterwaySegment) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		this.outfallLocation = outfallLocation;
		this.waterwaySegment = waterwaySegment;
	}

	public boolean isValid() {
		if (null == startTime)
			return false;
		return true;
	}
	
	public String parseToString() {

		return outfallLocation + ";" + waterwaySegment + ";" + startTime.parseDate() +
				";" + startTime.parseTime() + ";" + endTime.parseTime() + ";" +
				startTime.getMinutesUntil(endTime);

	}
	
}
