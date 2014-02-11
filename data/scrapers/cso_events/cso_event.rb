require 'rubygems'
require 'chronic'
require 'chronic_duration'

class CSOEvent
  OUTFALL_LOCATION_COLUMN = 0
  WATERWAY_SEGMENT_COLUMN = 1
  START_TIME_COLUMN = 2
  TOTAL_TIME_COLUMN = 4
  ZERO_SECONDS_STRING = ":00"

  attr_accessor :outfall_location, :waterway_segment, :start_time, :end_time

  def initialize row, date
    @outfall_location = row.children[OUTFALL_LOCATION_COLUMN].text
    @waterway_segment = row.children[WATERWAY_SEGMENT_COLUMN].text
    @start_time = Chronic.parse(date.strftime("%m/%d/%Y ") + row.children[START_TIME_COLUMN].text)
    total_time = ChronicDuration.parse(row.children[TOTAL_TIME_COLUMN].text + ZERO_SECONDS_STRING) #gem doesn't have a format option, assumes the string starts with minutes
    @end_time = total_time.nil? ? @start_time + 59 : @start_time + total_time
  end

  def to_csv
    "#{@outfall_location},#{@waterway_segment},#{@start_time.to_s},#{@end_time.to_s}"
  end
end
