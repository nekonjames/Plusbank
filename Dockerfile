FROM maven:3.5.2-jdk-8
VOLUME /tmp
EXPOSE 8080
ADD target/plusbank-0.0.1-SNAPSHOT.jar plusbank-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","plusbank-0.0.1-SNAPSHOT.jar"]