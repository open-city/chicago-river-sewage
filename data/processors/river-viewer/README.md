### Source
http://chicagoriverviewer.roosdesignconsulting.com/

### Extracted CSO layer and converted from `esrijson` to `geojson`
`convert.py`

### Reproject EPSG:3857 to EPSG:4326 
```bash
ogr2ogr -f "GeoJSON" cso_outfalls.geojson cso_outfalls_pseudo_mercator.geojson -s_srs EPSG:3857 -t_srs EPSG:4326 
```
