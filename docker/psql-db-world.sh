#!/bin/sh

docker exec -ti docker-db-1 psql -U postgres -d world
