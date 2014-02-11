require 'rubygems'

class RainGaugeMeasurement
  GAUGE_NUMBER_COLUMN = 0
  RAIN_INCHES_COLUMN = 2
  GAUGE_NAME_COLUMN = 4
  ADDRESS_COLUMN = 6

  def initialize row, date
    @rain_inches = clean(row.children[RAIN_INCHES_COLUMN].text).to_f
    @gauge_name = row.children[GAUGE_NAME_COLUMN].text.strip.gsub(",","")
    @gauge_number = gauge_number_from row
    address_row = row.children[ADDRESS_COLUMN]
    @address = address_row.nil? ? "" : address_row.text.strip.gsub(",","")
    @date = date
  end

  def to_csv
    "#{@gauge_number},#{@rain_inches},#{@gauge_name},#{@address},#{@date}"
  end

  private
  def clean input
    input.gsub(/\s+/, '').gsub(/^\u00A0/, "").strip
  end

  def gauge_number_from row
    number = clean(row.children[GAUGE_NUMBER_COLUMN].text)
    combined_number_description = number + " - " + @gauge_name.gsub("Average", "")
    @gauge_number = number.match(/Avg/) ? combined_number_description.strip : number
  end
end
