FROM openjdk:8

ENV TZ Asia/Shanghai

EXPOSE 8072

VOLUME /tmp

ADD target/*.jar  /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]