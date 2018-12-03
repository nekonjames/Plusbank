#FROM maven:3.5.2-jdk-8
FROM java:8
VOLUME /tmp
EXPOSE 8080
ADD target/plusbank-0.0.1-SNAPSHOT.jar plusbank.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","plusbank.jar"]