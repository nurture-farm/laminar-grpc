#!/bin/bash

echo "Updating brew and installing watchman"
brew update
brew install watchman

echo "Install graphql schema fetcher"
yarn global add get-graphql-schema

echo "Fetching schema from http://localhost:9090/graphql"
get-graphql-schema http://localhost:9090/graphql > ../frontend/schema.graphql