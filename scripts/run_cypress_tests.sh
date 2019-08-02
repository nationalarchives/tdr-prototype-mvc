#!/usr/bin/env bash
docker-compose up -d
npm i
npm run wait
npm run cypress:ci