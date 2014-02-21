import json
import esri_to_geo
import subprocess

with open('arcgis_layers.json', 'r') as f :
    arcgis_layers = json.load(f)

for layer in arcgis_layers["operationalLayers"] :
    layer_name = layer["featureCollection"]["layers"][0]["layerDefinition"]["name"]

    esri_json = layer["featureCollection"]["layers"][0]["featureSet"]
    file_name = layer_name + '_pseudo_mercator.geojson'

    with open(file_name, 'w') as f :
        json.dump(esri_to_geo.esri_to_geo(esri_json), f)

    command = 'ogr2ogr -f "GeoJSON" %s.geojson %s -s_srs EPSG:3857 -t_srs EPSG:4326' % (layer_name, file_name)
    print command
    subprocess.call(command, shell=True)


