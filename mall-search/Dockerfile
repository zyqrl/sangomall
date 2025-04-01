FROM openjdk:8

ENV TZ Asia/Shanghai

EXPOSE 8090

VOLUME /tmp

ADD target/*.jar  /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]