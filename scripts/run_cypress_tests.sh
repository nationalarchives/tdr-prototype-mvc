#!/usr/bin/env bash
cd cypress-tests
npm install
cd ..
npm install
npm run build
sbt dist
docker-compose up -d
cd cypress-tests
npm run wait
npm run cypress:ci
