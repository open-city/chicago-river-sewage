### Source
http://chicagoriverviewer.roosdesignconsulting.com/

The source url is: http://www.arcgis.com/sharing/rest/content/items/756e5356baff4a74898e97f571e9d58c/data?f=json

My understanding is @mapmeld inspected the network traffic when the page loaded. Looks like you can take the unique identifier (in this case its 756e5356baff4a74898e97f571e9d58c) for any arcgis.com map and plug it in to ttp://www.arcgis.com/sharing/rest/content/items/{UNIQUE_ID}/data?f=json

### Extracted CSO layer and converted from `esrijson` to `geojson`
`convert.py`

### Reproject EPSG:3857 to EPSG:4326 
```bash
> ogr2ogr -f "GeoJSON" cso_outfalls.geojson cso_outfalls_pseudo_mercator.geojson -s_srs EPSG:3857 -t_srs EPSG:4326 
> ogr2ogr -f CSV cso_outfalls.csv cso_outfalls.geojson -lco GEOMETRY=AS_X
Y
```
