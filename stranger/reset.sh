#!/usr/bin/env bash
# Back to the beginning: two honest tickets, an empty log on the stranger's server.
set -e
curl -s -X POST http://localhost:8081/tickets/reset > /dev/null
curl -s -X DELETE http://localhost:8082/log
echo "reset done"
