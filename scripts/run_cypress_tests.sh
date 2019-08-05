#!/usr/bin/env bash
npm install
npm run build
sbt dist
docker-compose up -d
npm run wait
npm run cypress:ci