#!/bin/sh

docker exec -ti docker-db-1 psql -U postgres -d world -c "select name, continent, population from country where name like 'U%';"
