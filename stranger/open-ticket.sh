#!/usr/bin/env bash
# Helper: open a ticket as the Stranger with the body read from stdin.
# usage: ./open-ticket.sh "Subject" < body.txt
set -e
SUBJECT="$1"
BODY="$(cat)"
python3 - "$SUBJECT" "$BODY" <<'EOF' | curl -s -H 'Content-Type: application/json' -d @- http://localhost:8081/tickets
import json, sys
print(json.dumps({"customerEmail": "stranger@example.net", "subject": sys.argv[1], "body": sys.argv[2]}))
EOF
echo
