#!/usr/bin/env sh

mkdir -p recordings
cd recordings

ROOT_DIR="catalogue"

java -jar ../wiremock-standalone-2.25.1.jar \
  --proxy-all "https://api.wellcomecollection.org" \
  --record-mappings \
  --verbose \
  --root-dir $ROOT_DIR \
  > log_$(date +%s).txt 2>&1 &

sleep 5

WIREMOCK_PID=$!

#curl http://localhost:8080/catalogue/v2/works/cnkv77md?include=items%2Cidentifiers
curl "http://localhost:8080/catalogue/v2/works?query=i1493069&include=items,identifiers"

kill $WIREMOCK_PID

echo ""
echo "Recordings saved to $(pwd)/$ROOT_DIR"