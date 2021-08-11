FROM openjdk:16
MAINTAINER CodeWithBuff
VOLUME /tmp
ADD target/notification-system-1.0-SNAPSHOT-jar-with-dependencies.jar /tmp/app.jar
ENTRYPOINT ["java","-jar","/tmp/app.jar"]