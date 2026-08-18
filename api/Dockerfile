FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
