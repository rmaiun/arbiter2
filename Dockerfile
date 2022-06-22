FROM openjdk:11.0-jdk-slim
MAINTAINER @RMaiun
VOLUME /tmp
COPY target/scala-2.13/*.jar arbiter2.jar
EXPOSE 80 443
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /arbiter2.jar ${@}"]