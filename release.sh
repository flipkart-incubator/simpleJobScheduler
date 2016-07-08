#!/usr/bin/env bash

set -e
mvn clean install
mvn deploy -DperformRelease=true -DskipTests -DaltDeploymentRepository=clojars::default::https://clojars.org/repo
