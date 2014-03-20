from flask import Flask, request, make_response, render_template
from flask_sqlalchemy import SQLAlchemy
from datetime import date, datetime, timedelta
from sqlalchemy import Table, func, distinct, Column, create_engine, MetaData
from sqlalchemy.orm import sessionmaker, scoped_session
from sqlalchemy.exc import NoSuchTableError
import json
import requests
import re
import os
import operator

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///data/processed_data/cso-data.db'
db = SQLAlchemy(app)

class CSOEvent(db.Model):
    __tablename__ = 'CSOs'
    __table_args__ = (db.UniqueConstraint('location', 'date', 'starttime'),)
    id = db.Column(db.Integer, primary_key=True)
    location = db.Column(db.String)
    segment = db.Column(db.Integer)
    date = db.Column(db.Date)
    starttime = db.Column(db.String)
    endtime = db.Column(db.String)
    duration = db.Column(db.Integer)

    def __repr__(self):
        t = self.date.strftime('%Y/%m/%d')
        return '<CSOEvent %r %r %r>' % (self.location, t, self.starttime)

    def as_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}

dthandler = lambda obj: obj.isoformat() if isinstance(obj, datetime) or isinstance(obj, date) else None

waterway_segments = [
  {"riverway": "North Shore Channel", "description": "Lake Michigan to North Side Water Reclamation Plant"},
  {"riverway": "North Shore Channel", "description": "North Side Water Reclamation Plant to the confluence with the North Branch of the Chicago River"},
  {"riverway": "North Branch of Chicago River", "description": "Confluence with the North Shore Channel to Wolf Point"},
  {"riverway": "North Branch of Chicago River", "description": "Beckwith Road and West Fork to confluence with the North Shore Channel"},
  {"riverway": "Chicago River", "description": "Wolf Point to Chicago River Controlling Works"},
  {"riverway": "South Branch of Chicago River", "description": "Wolf Point to Damen Avenue"},
  {"riverway": "South Fork of SBCR (Bubbly Creek)", "description": ""},
  {"riverway": "Chicago Sanitary and Ship Canal", "description": "Damen Avenue to the Stickney Water Reclamation Plant"},
  {"riverway": "Chicago Sanitary and Ship Canal", "description": "Stickney Water Reclamation Plant to the confluence with the Calumet-Sag Channel"},
  {"riverway": "Chicago Sanitary and Ship Canal", "description": "From the confluence with the Calumet-Sag Channel to the Lemont Water Reclamation Plant"},
  {"riverway": "Chicago Sanitary and Ship Canal", "description": "Lemont Water Reclamation Plant to Lockport Lock & Dam"},
  {"riverway": "Weller Creek", "description": ""},
  {"riverway": "Des Plaines River", "description": "Weller Creek to Willow-Higgins Creek"},
  {"riverway": "Des Plaines River", "description": "Willow-Higgins Creek to the confluence with Salt Creek"},
  {"riverway": "Des Plaines River", "description": "The confluence with Salt Creek to the confluence with the CSSC"},
  {"riverway": "Salt Creek", "description": "From Addison Creek to the confluence with the Des Plaines River"},
  {"riverway": "Calumet River", "description": "O'Brien Locks to Lake Michigan"},
  {"riverway": "Grand Calumet River", "description": "From confluence with the Little Calumet River to the Indiana state line"},
  {"riverway": "Little Calumet River", "description": "O'Brien Locks to the Calumet-Sag Channel"},
  {"riverway": "Little Calumet River", "description": "Indiane state line to the Calumet-Sag Channel"},
  {"riverway": "Calumet-Sag Channel", "description": ""},
  {"riverway": "Calumet Union Drainage Ditch", "description": ""},
  {"riverway": "Addison Creek", "description": ""}
]

# ROUTES
@app.route('/cso-status/')
def cso_status():
    lookup_date = datetime.today().strftime("%m/%d/%Y")
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
            segment_detail = {}
            segment_detail["segment-id"] = int(w)
            segment_detail["riverway"] = (waterway_segments[int(w) - 1])["riverway"]
            segment_detail["description"] = (waterway_segments[int(w) - 1])["description"]
            water_segment_details.append(segment_detail)

        water_segment_details_sorted = sorted(water_segment_details, key=lambda k: k['riverway']) 
        
        if len(water_segments) == 0:
            water_resp['cso-events'] = []
            water_resp['is-there-sewage'] = 'no'
        else:
            water_resp['cso-events'] = water_segment_details_sorted
            water_resp['is-there-sewage'] = 'yes'

            chicago_riverways = json.load(open('static/data/chicago_riverways.json', 'rb'))
            riverway_features_all = chicago_riverways['features']

            riverway_features_to_show = []
            for segment in water_segment_details:
              for f in riverway_features_all:
                if f['properties']['SEGMENT_ID'] == segment['segment-id']:
                  f['properties']['riverway'] = waterway_segments[f['properties']['SEGMENT_ID']-1]['riverway']
                  f['properties']['description'] = waterway_segments[f['properties']['SEGMENT_ID']-1]['description']
                  riverway_features_to_show.append(f)

            chicago_riverways['features'] = riverway_features_to_show
            water_resp['riverway-geojson'] = chicago_riverways
 
        resp = make_response(json.dumps(water_resp))
    else: 
        resp = make_response(json.dumps({'status': 'error', 'message': 'Something went wrong while performing your query. Try again'}), 500)
 
    resp.headers['Content-Type'] = 'application/json'
    return resp

# API Routes
@app.route('/cso-events/')
def cso_events():
    offset = request.args.get('offset', 0)
    limit = request.args.get('limit', 100)
    base_query = CSOEvent.query.order_by(CSOEvent.date.desc())\
        .offset(int(offset)).limit(int(limit))
    data = [r.as_dict() for r in base_query.all()]
    resp = make_response(json.dumps(data, default=dthandler))
    resp.headers['Content-Type'] = 'application/json'
    return resp

@app.route('/csos-by-waterway/')
def csos_by_waterway():
    base_query = db.session.query(CSOEvent.segment,
        func.count(CSOEvent.duration))\
        .group_by(CSOEvent.segment)\
        .order_by(func.count(CSOEvent.duration).desc())
    resp = [{'segment': s, 'count': c} for s,c in base_query.all()]
    resp = make_response(json.dumps(resp))
    resp.headers['Content-Type'] = 'application/json'
    return resp

@app.route('/history/')
def history():
    cso_dates = db.session.query(CSOEvent.date,
                func.count(distinct(CSOEvent.segment)),
                func.count(CSOEvent.id))\
                .group_by(CSOEvent.date)\
                .order_by(CSOEvent.date.desc()).all()

    return render_app_template('history.html', cso_dates=cso_dates)

@app.route('/espanol/')
def index_es():
    return render_app_template('index.es.html')

@app.route('/')
def index():
    today = datetime.today()
    request_date = request.args.get('date', today.strftime("%m/%d/%Y"))
    today_flag = True

    try:
        request_date = datetime.strptime(request_date, "%m/%d/%Y")

        if request_date.strftime("%m/%d/%Y") != today.strftime("%m/%d/%Y"):
          today_flag = False
    except ValueError:
        print "Error parsing date", request_date
        request_date = today

    return render_app_template('index.html', date=request_date, today_flag=today_flag)

# UTILITY
def render_app_template(template, **kwargs):
    '''Add some goodies to all templates.'''

    if 'config' not in kwargs:
        kwargs['config'] = app.config
    return render_template(template, **kwargs)

# INIT
if __name__ == "__main__":
    app.run(debug=True, port=9999)
