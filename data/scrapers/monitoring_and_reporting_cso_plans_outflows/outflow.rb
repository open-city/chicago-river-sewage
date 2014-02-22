require 'rubygems'

class Outflow
  DISCHARGE_NUMBER_COLUMN = 0
  TARP_STRUCTURE_COLUMN = 1
  OUTFALL_LOCATION_COLUMN = 2
  CSO_OUTFALL_OWNER_COLUMN = 3

  attr_accessor :discharge_number,
                :tarp_structure,
                :outfall_location,
                :cso_outfall_owner

  def initialize row
    @discharge_number = clean(row.css('td')[DISCHARGE_NUMBER_COLUMN].text)
    @tarp_structure = clean(row.css('td')[TARP_STRUCTURE_COLUMN].text)
    @outfall_location = clean(row.css('td')[OUTFALL_LOCATION_COLUMN].text)
    @cso_outfall_owner = clean(row.css('td')[CSO_OUTFALL_OWNER_COLUMN].text)
  end

  def to_csv
    "#{@discharge_number},#{@tarp_structure},#{@outfall_location},#{@cso_outfall_owner}"
  end

  def clean string
    string.gsub!(/\n/, " ")
    string.gsub!(/\s+/, ' ')
    string.gsub!(/,/, '/')
    string.strip!
    string
  end
end
