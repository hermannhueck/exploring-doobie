#!/bin/sh

docker exec -ti docker-db-1 psql -d world -U postgres -c "select name, continent, population from country where name like 'U%';"
