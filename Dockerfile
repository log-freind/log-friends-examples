FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY gradle.properties settings.gradle.kts build.gradle.kts ./

COPY src src

RUN chmod +x gradlew

RUN ./gradlew bootJar --no-daemon
RUN cp build/libs/*.jar /workspace/app.jar

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=builder /workspace/app.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", \
  "-Djdk.attach.allowAttachSelf=true", \
  "-Dnet.bytebuddy.experimental=true", \
  "-Dspring.application.name=order-service", \
  "-Dserver.port=8081", \
  "-jar", "app.jar"]
