require 'rubygems'
require 'bundler/setup'
require 'nokogiri'
require 'open-uri'
require './outflow'

def parse_page_for url
  outflows = []
  doc = Nokogiri::HTML(open(url))
  rows = doc.css('tr').reject {|row| row.css('td').size != 4 || row.css('td')[0].text == "Discharge No."}
  rows.map {|row| Outflow.new(row) }
end

outflows = []
urls = ['http://www.mwrd.org/irj/go/km/docs/documents/MWRD/internet/protecting_the_environment/Combined_Sewer_Overflows/htm/Northside_WRP_CSO_Monitoring_and_Reporting_Plan.htm',
        'https://www.mwrd.org/irj/go/km/docs/documents/MWRD/internet/protecting_the_environment/Combined_Sewer_Overflows/htm/Stickney_WRP_CSO_Monitoring_and_Reporting_Plan.htm',
        'https://www.mwrd.org/irj/go/km/docs/documents/MWRD/internet/protecting_the_environment/Combined_Sewer_Overflows/htm/Calumet_WRP_CSO_Monitoring_and_Reporting_Plan.htm']

urls.each do |url|
  outflows += parse_page_for url
end

File.open('./outflow_locations_mwrd_monitoring_plans.csv', 'a') do |file|
  outflows.each {|outflow| file.puts outflow.to_csv }
end

