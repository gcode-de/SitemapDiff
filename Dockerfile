FROM node:20-alpine AS frontend-build
WORKDIR /workspace/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-21 AS backend-build
WORKDIR /workspace
COPY backend/pom.xml backend/pom.xml
RUN mvn -B -f backend/pom.xml dependency:go-offline
COPY backend/ backend/
COPY --from=frontend-build /workspace/frontend/dist/ backend/src/main/resources/static/
RUN mvn -B -f backend/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=backend-build /workspace/backend/target/sitemapdiff.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
