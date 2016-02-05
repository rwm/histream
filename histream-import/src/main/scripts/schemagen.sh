#!/bin/bash
mkdir -p target/generated-sources/schemagen
"$JAVA_HOME/bin/schemagen.exe" -d "target/generated-sources/schemagen" -cp "target/histream-import-${project.version}.jar;../histream-core/target/histream-core-${project.version}.jar" de.sekmi.histream.etl.config.DataSource
