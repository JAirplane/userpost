FROM openjdk:21-jdk-slim
VOLUME /tmp
COPY target/userpost.jar userpost.jar
ENTRYPOINT ["java","-jar","/userpost.jar"]