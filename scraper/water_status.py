from flask import Flask, request, make_response
from datetime import date, datetime, timedelta
import json
import requests
import re

app = Flask(__name__)

@app.route('/water-status/')
def water_status():
    
    current_date = datetime.today().strftime("%m/%d/%y")
    water_page = requests.get('http://apps.mwrd.org/CSO/display_all.aspx?link_date=%s' % current_date)
    if water_page.status_code is 200:
        water_resp = {'date': current_date}
        water_segments = re.findall('images\/\d+.GIF"', water_page.content)
        if len(water_segments) == 0:
          water_resp['cso-events'] = []
          water_resp['is-there-sewage'] = 'no'
        else:
          water_resp['cso-events'] = water_segments
          water_resp['is-there-sewage'] = 'yes'

        resp = make_response(json.dumps(water_resp))
    else: 
        resp = make_response(json.dumps({'status': 'error', 'message': 'Something went wrong while performing your query. Try again'}), 500)

    resp.headers['Content-Type'] = 'application/json'
    return resp

if __name__ == "__main__":
    app.run(debug=True, port=9999)
