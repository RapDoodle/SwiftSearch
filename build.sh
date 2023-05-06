#! /bin/bash
# Build common
cd common
./mvnw clean install
cd ..

# Create a directory for executables
mkdir executables

# Crawler
cd crawler
./mvnw clean package
cp ./target/crawler-*.jar ../executables
cd ..

# Indexer
cd indexer
./mvnw clean package
cp ./target/indexer-*.jar ../executables
cd ..

# Query engine
cd query
./mvnw clean package
cp ./target/query-*.jar ../executables
cd ..

# Web server
cd web
cp ./target/web-*.jar ../executables
./mvnw clean package
cd ..