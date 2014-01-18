require 'rubygems'
require 'bundler/setup'
require 'nokogiri'
require 'open-uri'
require 'chronic'
require './cso_event'

START_DATE = '01/01/2007'
SECONDS_IN_ONE_DAY = 24*60*60

def cso_events_exist_in doc
  doc.css('table').size > 1
end

def parse_page_for date
  cso_events = []
  formatted_date = date.strftime("%m/%d/%Y")
  doc = Nokogiri::HTML(open("http://apps.mwrd.org/CSO/CSOEventSynopsisReport.aspx?passdate=" + formatted_date))
  if (cso_events_exist_in doc)
    rows = doc.css('table').last.css('tr')[1..-1]
    cso_events = rows.map {|row| CSOEvent.new row, date}
    print '.'
  end
  cso_events
end

date_to_scrape = Chronic.parse START_DATE
cso_events = []

while date_to_scrape < Time.now
  cso_events += parse_page_for date_to_scrape
  date_to_scrape += SECONDS_IN_ONE_DAY
end

File.open('./cso_events.csv', 'a') do |file|
  cso_events.each {|cso_event| file.puts cso_event.to_csv }
end

