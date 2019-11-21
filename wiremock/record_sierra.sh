#!/usr/bin/env sh

set -e
set -o nounset

mkdir -p recordings
cd recordings

USER=$1
PASS=$2

AUTH_STRING=$(echo "$USER":"$PASS" | base64)

ROOT_DIR="sierra"

java -jar ../wiremock-standalone-2.25.1.jar \
  --proxy-all "https://libsys.wellcomelibrary.org" \
  --match-headers="Authorization" \
  --record-mappings \
  --verbose \
  --root-dir $ROOT_DIR \
  > log_$(date +%s).txt 2>&1 &

sleep 5

WIREMOCK_PID=$!

ACCESS_TOKEN=$(curl -X POST -u $USER:$PASS http://localhost:8080/iii/sierra-api/v5/token | jq -r ".access_token")

#curl --header "Authorization:Bearer $ACCESS_TOKEN" http://localhost:8080/iii/sierra-api/v5/items/1606370
#curl --header "Authorization:Bearer $ACCESS_TOKEN" http://localhost:8080/iii/sierra-api/v5/patrons/1100189/holds

curl \
  --header "Content-Type: application/json" \
  --header "Authorization:Bearer $ACCESS_TOKEN" \
  --data '{"recordType": "i", "recordNumber": 1606370, "pickupLocation": "sepbb" }' \
  -XPOST http://localhost:8080/iii/sierra-api/v5/patrons/1100189/holds/requests

kill $WIREMOCK_PID

echo ""
echo "Recordings saved to $(pwd)/$ROOT_DIR"
