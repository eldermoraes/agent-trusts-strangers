#!/usr/bin/env bash
# Back to the beginning: one honest ticket (the Victim's), empty logs.
set -e
curl -s -X POST http://localhost:8081/tickets/reset > /dev/null
curl -s -X DELETE http://localhost:8082/log
curl -s -X DELETE http://localhost:8080/api/debug
echo "reset done"
