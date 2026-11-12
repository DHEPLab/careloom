#!/bin/sh
set -eu
case "${API_URL:-}" in
  http://*|https://*) ;;
  *) echo 'API_URL must be a full http:// or https:// backend origin.' >&2; exit 1 ;;
esac
# An origin has no credentials, path, query, fragment or nginx directives.
# Reject invalid input rather than silently routing API calls to the SPA.
case "${API_URL#*://}" in
  ''|*[!a-zA-Z0-9.:-]*) echo 'API_URL must contain only a backend hostname and optional port, without a trailing slash.' >&2; exit 1 ;;
esac
