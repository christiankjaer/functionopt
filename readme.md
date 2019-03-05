# Function inlining

## Build 

    mvn install:install-file -Dfile=libs/java_cup.runtime-0.11b.jar -DgroupId=java_cup -DartifactId=runtime -Dversion=0.11b -Dpackaging=jar

    mvn package

## Run

    java -classpath target/classes:libs/java_cup.runtime-0.11b.jar jc.cli.App
