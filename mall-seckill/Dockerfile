FROM openjdk:8

ENV TZ Asia/Shanghai

EXPOSE 9601

VOLUME /tmp

ADD target/*.jar  /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]