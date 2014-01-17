require 'rubygems'
require 'bundler/setup'
require 'nokogiri'
require 'open-uri'
require 'chronic'
require './rain_gauge_measurement'

START_DATE = '08/01/2006' # Start of visible data is 7/31, but I chose to start at a reasonable day
SECONDS_IN_ONE_DAY = 24*60*60

def parse_page_for date
  measurements = []
  formatted_date = date.strftime("%m/%d/%Y")
  doc = Nokogiri::HTML(open("http://apps.mwrd.org/CSO/displayrain.aspx?passdate=" + formatted_date))
  rows = doc.css('table').last.css('tr')[2..-1] # first two rows are unnecesary
  measurements = rows.map {|row| RainGaugeMeasurement.new row, date}
  print '.'
  measurements
end

date_to_scrape = Chronic.parse START_DATE
rain_measurements = []

while date_to_scrape < Time.now
  rain_measurements += parse_page_for date_to_scrape
  date_to_scrape += SECONDS_IN_ONE_DAY
end

File.open('./mwrd_rain_measurements.csv', 'a') do |file|
  file.puts "Gauge Number, Rain (inches), Gauge Name, Address, Date"
  rain_measurements.each {|measurement| file.puts measurement.to_csv }
end

