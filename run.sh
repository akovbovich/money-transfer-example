#!/bin/sh

sbt test stage &&
target/universal/stage/bin/money-transfer-example -Dplay.http.secret.key=rvlt
