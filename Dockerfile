FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/ifs25005-p5-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
