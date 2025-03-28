FROM openjdk:8

ENV TZ Asia/Shanghai

EXPOSE 8080

VOLUME /tmp

ADD  target/*.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=dev"]
