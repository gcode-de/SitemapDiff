FROM --platform=linux/amd64 openjdk:21
EXPOSE 8080
ADD backend/target/SitemapDiff.jar SitemapDiff.jar
ENTRYPOINT ["java", "-jar", "SitemapDiff.jar"]