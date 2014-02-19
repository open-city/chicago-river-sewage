from flask import Flask, request, make_response, render_template
from datetime import date, datetime, timedelta
from sqlalchemy import Table, func, distinct, Column, create_engine, MetaData
from sqlalchemy.orm import sessionmaker, scoped_session
from sqlalchemy.exc import NoSuchTableError
import json
import requests
import re

app = Flask(__name__)

engine = create_engine('sqlite:///data/processed_data/cso-data.db')
metadata = MetaData()
session = scoped_session(sessionmaker(autocommit=False,
                                      autoflush=False,
                                      bind=engine))

csos_table = Table('CSOs', metadata,
        autoload=True, autoload_with=engine, extend_existing=True)

waterway_segments = [
  "North Shore Channel: Lake Michigan to North Side WRP",
  "North Shore Channel: North Side WRP to the confluence with the North Branch of the Chicago River (NBCR)",
  "NBCR: confluence with the North Shore Channel to Wolf Point, including the North Branch Canal east of Goose Island.",
  "NBCR: Beckwith Road and West Fork to confluence with the North Shore Channel",
  "Chicago River: Wolf Point to Chicago River Controlling Works (CRCW)",
  "South Branch of Chicago River: Wolf Point to Damen Avenue",
  "South Fork of SBCR (Bubbly Creek)",
  "Chicago Sanitary and Ship Canal (CSSC): Damen Avenue to the Stickney WRP",
  "CSSC: Stickney WRP to the confluence with the Calumet-Sag Channel",
  "CSSC: from the confluence with the Calumet-Sag Channel to the Lemont WRP",
  "CSSC: Lemont WRP to Lockport Lock & Dam",
  "Weller Creek",
  "Des Plaines River: Weller Creek to Willow-Higgins Creek",
  "Des Plaines River: Willow-Higgins Creek to the confluence with Salt Creek",
  "Des Plaines River: the confluence with Salt Creek to the confluence with the CSSC",
  "Salt Creek: from Addison Creek to the confluence with the Des Plaines River",
  "Calumet River: O'Brien Locks to Lake Michigan",
  "Grand Calumet River: from confluence with the Little Calumet River to the Indiana state line",
  "Little Calumet River: O'Brien Locks to the Calumet-Sag Channel",
  "Little Calumet River: Indiane state line to the Calumet-Sag Channel",
  "Calumet-Sag Channel",
  "Calumet Union Drainage Ditch",
  "Addison Creek"
]

# ROUTES
@app.route('/water-status/')
def water_status():
  lookup_date = datetime.today().strftime("%m/%d/%y")
  request_date = request.args.get('date')
  if request_date:
    lookup_date = request_date
    water_page = requests.get('http://apps.mwrd.org/CSO/display_all.aspx?link_date=%s' % lookup_date)
  else: # if no date given, MWRD has a different page for the current status :P
    water_page = requests.get('http://apps.mwrd.org/CSO/cso_quick_view.aspx')

  if water_page.status_code is 200:
      water_resp = {'date': lookup_date}
      water_segments = set(re.findall('images\/(\d+).GIF"', water_page.content))
      water_segment_details = []
      for w in water_segments:
        water_segment_details.append([int(w), waterway_segments[int(w) - 1]])

      if len(water_segments) == 0:
        water_resp['cso-events'] = []
        water_resp['is-there-sewage'] = 'no'
      else:
        water_resp['cso-events'] = water_segment_details
        water_resp['is-there-sewage'] = 'yes'

      resp = make_response(json.dumps(water_resp))
  else: 
      resp = make_response(json.dumps({'status': 'error', 'message': 'Something went wrong while performing your query. Try again'}), 500)

  resp.headers['Content-Type'] = 'application/json'
  return resp

@app.route('/cso-history/')
def cso_history():

  resp = []
  table_keys = csos_table.columns.keys()
  query = list(
    session.query(csos_table.c.Segment, func.sum(csos_table.c.Duration))\
    .group_by(csos_table.c.Segment)\
    .order_by(func.count(csos_table.c.Duration).desc())\
    .all()
  )

  for value in query:
      d = {}
      for k, v in zip(['segment','count'], value):
        d[k] = v
      resp.append(d)

  # for value in query:
  #   d = {}
  #   for k, v in zip(table_keys, value):
  #     d[k] = v
  #   resp.append(d)

  resp = make_response(json.dumps(resp))
  resp.headers['Content-Type'] = 'application/json'

  return resp

@app.route('/espanol/')
def index_es():
  return render_app_template('index.es.html')

@app.route('/')
def index():
  return render_app_template('index.html')

# UTILITY
def render_app_template(template, **kwargs):
    '''Add some goodies to all templates.'''

    if 'config' not in kwargs:
        kwargs['config'] = app.config
    return render_template(template, **kwargs)

# INIT
if __name__ == "__main__":
    app.run(debug=True, port=9999)
