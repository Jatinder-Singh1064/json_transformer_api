FROM eclipse-temurin:17-jdk-alpine
# The VOLUME line is removed to comply with Railway standards
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
