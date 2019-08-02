#!/usr/bin/env bash
docker-compose up -d
npm run wait
npm run cypress:ci