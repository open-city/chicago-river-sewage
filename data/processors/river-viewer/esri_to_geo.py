"""
This script will convert an EsriJSON
dictionary to a GeoJSON dictionary

send a GeoJSON feature:
feature = json.loads(esri_input)
result = esri_to_geo(feature)
optional: response = json.dumps(result)

Still in the works:
- parse all geometry types
"""

def esri_to_geo(esrijson):
  geojson = {}
  # first, grab the properties
  features = esrijson["features"]
  esri_geom_type = esrijson["geometryType"]
  count = len(features)
  geojson["type"] = "FeatureCollection"
  """
  # Not sure how to distinguish a single
  # Feature from an array of a single Feature
  # Or do I even have to?
  if count > 1:
    geojson["type"] = "FeatureCollection"
  else:
    geojson["type"] = "Feature"
  """

  feats = []
  for feat in features:
    feats.append(extract(feat, esri_geom_type))

  geojson["features"] = feats

  return geojson

def extract(feature, esri_geom_type):
  item = {}
  item["type"] = "Feature"
  # use the esri geometryType
  # to determine how the coordinates array
  # will be defined
  geom = feature["geometry"]
  geometry = {}
  geometry["type"] = get_geom_type(esri_geom_type)
  geometry["coordinates"] = get_coordinates(geom, geometry["type"])
  item["geometry"] = geometry
  # can just make a direct
  # copy of the attributes to properties
  item["properties"] = feature["attributes"]

  return item

def get_geom_type(esri_type):
  if esri_type == "esriGeometryPoint":
    return "Point"
  elif esri_type == "esriGeometryMultiPoint":
    return "MultiPoint"
  elif esri_type == "esriGeometryPolyline":
    return "LineString"
  elif esri_type == "esriGeometryPolygon":
    return "Polygon"
  else:
    return "unknown"

def get_coordinates(geom, geom_type):
  if geom_type == "Polygon":
    return geom["rings"]
  elif geom_type == "LineString":
    return geom["paths"]
  elif geom_type == "Point":
    return [ geom["x"], geom["y"] ]
  else:
    return []
