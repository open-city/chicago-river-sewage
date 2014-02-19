import json
import esri_to_geo

with open('arcgis_layers.json', 'r') as f :
    arcgis_layers = json.load(f)

for layer in arcgis_layers["operationalLayers"] :
    if layer["id"] == 'cso_chicago_caws_active_4354' :
        esri_json = layer["featureCollection"]["layers"][0]["featureSet"]
        with open('cso_outfalls_state_plane.geojson', 'w') as f :
            json.dump(esri_to_geo.esri_to_geo(esri_json), f)
