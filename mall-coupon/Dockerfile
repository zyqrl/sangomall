FROM openjdk:8

ENV TZ Asia/Shanghai

EXPOSE 8010

VOLUME /tmp

ADD target/*.jar  /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]