# Laminar gRPC

For sample refer to laminar-grpc-example project.

Release laminar grpc
```shell script

merge to master
git pull
mvn clean versions:set -DnewVersion=0.0.10
mvn clean deploy
mvn versions:set -DnewVersion=0.0.11-SNAPSHOT
mvn versions:commit 
git push

find . -name pom.xml.versionsBackup -print | xargs rm 

```