
FROM openjdk:8

ENV TZ Asia/Shanghai

EXPOSE 8093

VOLUME /tmp

ADD target/*.jar  /app.jar

ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=prod"]