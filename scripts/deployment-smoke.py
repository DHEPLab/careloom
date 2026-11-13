#!/usr/bin/env python3
"""Check an explicitly opted-in, disposable local Compose instance with synthetic data."""
import argparse
import json
import os
import sys
import uuid
import urllib.error
import urllib.parse
import urllib.request

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--base-url', default='http://127.0.0.1:3000')
parser.add_argument('--api-url', default='http://127.0.0.1:8080')
parser.add_argument('--allow-synthetic-writes', action='store_true', required=True)
args = parser.parse_args()
username = os.environ.get('CARELOOM_BOOTSTRAP_ADMIN_USERNAME')
password = os.environ.get('CARELOOM_BOOTSTRAP_ADMIN_PASSWORD')
if not username or not password:
    parser.error('Set CARELOOM_BOOTSTRAP_ADMIN_USERNAME and CARELOOM_BOOTSTRAP_ADMIN_PASSWORD for the disposable instance.')
for value in (args.base_url, args.api_url):
    url = urllib.parse.urlsplit(value)
    if url.scheme != 'http' or url.hostname not in ('127.0.0.1', 'localhost') or url.username or url.password or url.path not in ('', '/'):
        parser.error('Only a plain HTTP localhost origin is allowed; never use an existing deployment.')

class NoRedirects(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        raise urllib.error.HTTPError(req.full_url, code, 'Redirect refused by local smoke-test guard', headers, fp)

opener = urllib.request.build_opener(urllib.request.ProxyHandler({}), NoRedirects())
def request(path, method='GET', data=None, token=None, origin=None):
    headers = {'Content-Type': 'application/json', 'X-Timezone': 'UTC'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    req = urllib.request.Request((origin or args.base_url).rstrip('/') + path,
        data=None if data is None else json.dumps(data).encode(), headers=headers, method=method)
    with opener.open(req, timeout=20) as response:
        raw = response.read()
        return response.status, json.loads(raw) if raw and 'json' in response.headers.get('Content-Type', '') else None

# The caller supplies the explicitly bootstrapped synthetic account.
# Keep any returned token or generated project credential in memory only.
checks = []
status, health = request('/actuator/health', origin=args.api_url)
assert status == 200 and health == {'status': 'UP'}, 'API health must expose status only'
checks.append('API status-only health')
try:
    request('/api/account/profile')
    raise AssertionError('Unauthenticated profile unexpectedly succeeded')
except urllib.error.HTTPError as error:
    assert error.code in (401, 403), 'Proxy must return API auth status, not HTML fallback'
checks.append('unauthenticated API request rejected through dashboard proxy')
status, login = request('/admin/authenticate', 'POST', {'username': username, 'password': password})
assert status == 200 and login.get('idToken'), 'Configured synthetic admin login failed'
token = login['idToken']
status, profile = request('/api/account/profile', token=token)
assert status == 200 and profile.get('username') == username, 'Authenticated profile failed'
checks.append('admin login and profile through dashboard proxy')
# All writes are synthetic and confined to a disposable fresh database.
status, rows = request('/admin/curriculums', token=token)
assert status == 200 and isinstance(rows.get('content'), list), 'Curriculum list failed'
checks.append('authenticated curriculum read through dashboard proxy')
name = 'SYNTHETIC-SMOKE-' + uuid.uuid4().hex[:12]
status, created = request('/admin/project/create', 'POST', {'name': name}, token)
assert status == 200 and created.get('projectId'), 'Synthetic project create failed'
project_id = created['projectId']
try:
    status, projects = request('/admin/project/all', token=token)
    assert any(row['id'] == project_id and row['name'] == name for row in projects), 'Created project not readable'
    status, _ = request('/admin/project/update/' + str(project_id), 'POST', {'name': name + '-UPDATED'}, token)
    assert status == 200, 'Synthetic project update failed'
    status, projects = request('/admin/project/all', token=token)
    assert any(row['id'] == project_id and row['name'] == name + '-UPDATED' for row in projects), 'Updated name not readable'
finally:
    status, _ = request('/admin/project/' + str(project_id), 'DELETE', token=token)
    assert status == 200, 'Synthetic project delete request failed'
status, projects = request('/admin/project/all', token=token)
# Project deletion is soft in this application. Its repository currently includes
# soft-deleted projects in this list, so list absence is not a deletion assertion.
# The disposable Compose volume is removed after the run to discard all test rows.
checks.append('synthetic project create/read/update and delete response through dashboard proxy')
print(json.dumps({'result': 'passed', 'checks': checks, 'scope': 'disposable localhost instance; tokens not recorded'}, indent=2))
