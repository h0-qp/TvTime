import urllib.request
import json
import ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

req = urllib.request.Request("https://api.themoviedb.org/3/tv/1399?api_key=4289895cd34a2e879a95f9c4644da879")
try:
    with urllib.request.urlopen(req, context=ctx) as response:
        data = json.loads(response.read().decode())
        print([(s['season_number'], s.get('episode_count', 'missing')) for s in data.get('seasons', [])])
except Exception as e:
    print(e)
