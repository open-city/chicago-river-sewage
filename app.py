from flask import Flask, request, make_response, render_template
from flask_sqlalchemy import SQLAlchemy
from flask.ext.cache import Cache
from functools import update_wrapper
from datetime import date, datetime, timedelta
from sqlalchemy import Table, func, distinct, Column, create_engine, MetaData
from sqlalchemy.orm import sessionmaker, scoped_session
from sqlalchemy.exc import NoSuchTableError
from operator import itemgetter
import json
import requests
import re
import os
import operator

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///data/processed_data/cso-data.db'
db = SQLAlchemy(app)
cache = Cache(config={'CACHE_TYPE': 'simple'})
cache.init_app(app)
CACHE_TIMEOUT = 60*5 # 5 minutes

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

def crossdomain(origin=None, methods=None, headers=None,
                max_age=21600, attach_to_all=True,
                automatic_options=True): # pragma: no cover
    if methods is not None:
        methods = ', '.join(sorted(x.upper() for x in methods))
    if headers is not None and not isinstance(headers, basestring):
        headers = ', '.join(x.upper() for x in headers)
    if not isinstance(origin, basestring):
        origin = ', '.join(origin)
    if isinstance(max_age, timedelta):
        max_age = max_age.total_seconds()

    def get_methods():
        if methods is not None:
            return methods

        options_resp = app.make_default_options_response()
        return options_resp.headers['allow']

    def decorator(f):
        def wrapped_function(*args, **kwargs):
            if automatic_options and request.method == 'OPTIONS':
                resp = current_app.make_default_options_response()
            else:
                resp = make_response(f(*args, **kwargs))
            if not attach_to_all and request.method != 'OPTIONS':
                return resp

            h = resp.headers

            h['Access-Control-Allow-Origin'] = origin
            h['Access-Control-Allow-Methods'] = get_methods()
            h['Access-Control-Max-Age'] = str(max_age)
            if headers is not None:
                h['Access-Control-Allow-Headers'] = headers
            return resp

        f.provide_automatic_options = False
        return update_wrapper(wrapped_function, f)
    return decorator

def make_cache_key(*args, **kwargs):
    path = request.path
    args = str(hash(frozenset(request.args.items())))
    # print 'cache_key:', (path+args)
    print(  (path + args).encode('utf-8') )
    return (path + args).encode('utf-8')

def get_waterway_segment(segment):
  waterway_segments = [
    {"segment": 1 , "riverway": "North Shore Channel", "description": "Lake Michigan to North Side Water Reclamation Plant"},
    {"segment": 2 , "riverway": "North Shore Channel", "description": "North Side Water Reclamation Plant to the confluence with the North Branch of the Chicago River"},
    {"segment": 3 , "riverway": "North Branch of Chicago River", "description": "Confluence with the North Shore Channel to Wolf Point"},
    {"segment": 4 , "riverway": "North Branch of Chicago River", "description": "Beckwith Road and West Fork to confluence with the North Shore Channel"},
    {"segment": 5 , "riverway": "Chicago River", "description": "Wolf Point to Chicago River Controlling Works"},
    {"segment": 6 , "riverway": "South Branch of Chicago River", "description": "Wolf Point to Damen Avenue"},
    {"segment": 7 , "riverway": "South Fork of SBCR (Bubbly Creek)", "description": ""},
    {"segment": 8 , "riverway": "Chicago Sanitary and Ship Canal", "description": "Damen Avenue to the Stickney Water Reclamation Plant"},
    {"segment": 9 , "riverway": "Chicago Sanitary and Ship Canal", "description": "Stickney Water Reclamation Plant to the confluence with the Calumet-Sag Channel"},
    {"segment": 10, "riverway": "Chicago Sanitary and Ship Canal", "description": "From the confluence with the Calumet-Sag Channel to the Lemont Water Reclamation Plant"},
    {"segment": 11, "riverway": "Chicago Sanitary and Ship Canal", "description": "Lemont Water Reclamation Plant to Lockport Lock & Dam"},
    {"segment": 12, "riverway": "Weller Creek", "description": ""},
    {"segment": 13, "riverway": "Des Plaines River", "description": "Weller Creek to Willow-Higgins Creek"},
    {"segment": 14, "riverway": "Des Plaines River", "description": "Willow-Higgins Creek to the confluence with Salt Creek"},
    {"segment": 15, "riverway": "Des Plaines River", "description": "The confluence with Salt Creek to the confluence with the CSSC"},
    {"segment": 16, "riverway": "Salt Creek", "description": "From Addison Creek to the confluence with the Des Plaines River"},
    {"segment": 17, "riverway": "Calumet River", "description": "O'Brien Locks to Lake Michigan"},
    {"segment": 18, "riverway": "Grand Calumet River", "description": "From confluence with the Little Calumet River to the Indiana state line"},
    {"segment": 19, "riverway": "Little Calumet River", "description": "O'Brien Locks to the Calumet-Sag Channel"},
    {"segment": 20, "riverway": "Little Calumet River", "description": "Indiana state line to the Calumet-Sag Channel"},
    {"segment": 21, "riverway": "Calumet-Sag Channel", "description": ""},
    {"segment": 22, "riverway": "Calumet Union Drainage Ditch", "description": ""},
    {"segment": 23, "riverway": "Addison Creek", "description": ""},
    {"segment": 30, "riverway": "Wilmette", "description": "Discharge to Lake Michigan"},
    {"segment": 31, "riverway": "Chicago River Controlling Works", "description": "Discharge to Lake Michigan"},
    {"segment": 32, "riverway": "O'Brien", "description": "Discharge to Lake Michigan"},
  ]

  return [s for s in waterway_segments if s['segment'] == segment][0]

def get_riverway_geojson(segments):
  chicago_riverways = json.load(open('static/data/chicago_riverways.json', 'rb'))
  riverway_features_all = chicago_riverways['features']

  riverway_features_to_show = []
  for segment in segments:
    for f in riverway_features_all:
      if f['properties']['SEGMENT_ID'] == segment['segment']:
        f['properties']['riverway'] = get_waterway_segment(f['properties']['SEGMENT_ID'])['riverway']
        f['properties']['description'] = get_waterway_segment(f['properties']['SEGMENT_ID'])['description']
        riverway_features_to_show.append(f)

  chicago_riverways['features'] = riverway_features_to_show
  return chicago_riverways

def get_day_count():
    return db.session.query(func.count(distinct(CSOEvent.date))).all()[0][0]

# API Routes
@app.route('/flush-cache')
@crossdomain(origin="*")
def flush_cache():
    cache.clear()
    resp = make_response(json.dumps({'status' : 'ok', 'message' : 'cache flushed!'}))
    resp.headers['Content-Type'] = 'application/json'
    return resp

@app.route('/cso-status/')
@cache.cached(timeout=CACHE_TIMEOUT, key_prefix=make_cache_key)
@crossdomain(origin="*")
def cso_status():
    water_resp = {}
    water_segments = set()
    
    lookup_date = datetime.today().date()
    request_date = request.args.get('date')
    
    if request_date: # look it up in the database
      lookup_date = datetime.strptime(request_date, "%m/%d/%Y").date()

    if lookup_date < datetime.today().date():
      base_query = db.session.query(distinct(CSOEvent.segment))\
                   .filter(CSOEvent.date == lookup_date)

      for d in base_query.all():
        water_segments.add(d[0])

    else: # get the quick view page directly from the MWRD site
      water_page = requests.get('http://apps.mwrd.org/CSO/cso_quick_view.aspx')
      if water_page.status_code is 200:
        water_resp = {'date': lookup_date.strftime('%m/%d/%Y')}
        water_segments = set(re.findall('images\/(\d+).(?:GIF|gif)"', water_page.content))
      else: 
        resp = make_response(json.dumps({'status': 'error', 'message': 'Something went wrong while performing your query. Try again'}), 500)
      
    # build a JSON response
    water_segment_details = []
    for w in water_segments:
      w = int(w)
      waterway_record = get_waterway_segment(w)

      segment_detail = {}
      segment_detail["segment"] = w
      segment_detail["riverway"] = waterway_record["riverway"]
      segment_detail["description"] = waterway_record["description"]
      water_segment_details.append(segment_detail)

    water_segment_details_sorted = sorted(water_segment_details, key=itemgetter('segment')) 
    
    if len(water_segments) == 0:
        water_resp['cso-events'] = []
        water_resp['is-there-sewage'] = 'no'
    else:
        water_resp['cso-events'] = water_segment_details_sorted
        water_resp['is-there-sewage'] = 'yes'

        water_resp['riverway-geojson'] = get_riverway_geojson(water_segment_details)

    resp = make_response(json.dumps(water_resp))
    resp.headers['Content-Type'] = 'application/json'
    return resp

@app.route('/cso-events/')
@crossdomain(origin="*")
def cso_events():
    request_date = request.args.get('date')
    offset = request.args.get('offset', 0)
    limit = request.args.get('limit', 100)

    if request_date:
      request_date = datetime.strptime(request_date, "%m/%d/%Y").date()
      base_query = CSOEvent.query.filter(CSOEvent.date == request_date)
    else:
      base_query = CSOEvent.query.order_by(CSOEvent.date.desc())\
          .offset(int(offset)).limit(int(limit))

    data = [r.as_dict() for r in base_query.all()]
    resp = make_response(json.dumps(data, default=dthandler))
    resp.headers['Content-Type'] = 'application/json'
    return resp

@app.route('/csos-by-waterway/')
@crossdomain(origin="*")
def csos_by_waterway():
    base_query = db.session.query(CSOEvent.segment,
        func.count(CSOEvent.duration))\
        .group_by(CSOEvent.segment)\
        .order_by(func.count(CSOEvent.duration).desc())
    resp = [{'segment': s, 'count': c} for s,c in base_query.all()]
    resp = make_response(json.dumps(resp))
    resp.headers['Content-Type'] = 'application/json'
    return resp

# HTML routes
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
    today = datetime.today()
    request_date = request.args.get('date', today.strftime("%m/%d/%Y"))
    today_flag = True

    try:
        request_date = datetime.strptime(request_date, "%m/%d/%Y")

        if request_date.strftime("%m/%d/%Y") != today.strftime("%m/%d/%Y"):
          today_flag = False
    except ValueError:
        print(  "Error parsing date", request_date )
        request_date = today

    return render_app_template('index.es.html', date=request_date, today_flag=today_flag, day_count=get_day_count())

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
        print(  "Error parsing date", request_date )
        request_date = today

    return render_app_template('index.html', date=request_date, today_flag=today_flag, day_count=get_day_count())

# UTILITY
def render_app_template(template, **kwargs):
    '''Add some goodies to all templates.'''

    if 'config' not in kwargs:
        kwargs['config'] = app.config
    return render_template(template, **kwargs)

# INIT
if __name__ == "__main__":
    app.run(debug=True, port=9999)
