#!/usr/bin/env bash

#DB_URL=D:\\Projects\\java\\signals-usage\\data\\test
DB_URL=~/Projects/signals-usage/data/test

java -cp h2-2.2.224.jar org.h2.tools.Shell \
-url "jdbc:h2:$DB_URL" \
-user "sa" \
-password "sa" \
-sql "$(cat ./init-db.sql)"
