FROM openjdk:8

ENV TZ Asia/Shanghai

EXPOSE 22200

VOLUME /tmp

ADD target/*.jar  /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]