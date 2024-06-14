FROM --platform=linux/amd64 openjdk:21
EXPOSE 8080
ADD backend/target/sitemapdiff.jar sitemapdiff.jar
ENTRYPOINT ["java", "-jar", "sitemapdiff.jar"]