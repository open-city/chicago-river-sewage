package com.hydrophilik.cso.parseHtml;

import com.hydrophilik.cso.utils.BezTime;

public class CsoEvent {
	private BezTime startTime = null;
	private BezTime endTime = null;
	private String outfallLocation = null;
	private int waterwaySegment;

	public CsoEvent(BezTime startTime, BezTime endTime,
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
