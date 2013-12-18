csvcut -l -c "Location" mwrd-tarp-connection-database.csv > mwrd-location.csv

csvcut -l -c "PIPE_DESC" SSMMA_Combined_Sewer_Overflow_locations.csv > SSMM-pipe-description.csv 

csvstack -g mwrd,ssmm -n source mwrd-location.csv SSMM-pipe-description.csv | csvdedupe --config mwrd-ssmm_config.json -v > output.csv
