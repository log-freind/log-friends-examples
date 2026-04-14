FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY gradle.properties settings.gradle.kts build.gradle.kts ./

COPY log-friends-sdk/build.gradle.kts log-friends-sdk/
COPY examples/build.gradle.kts examples/
COPY spark-jobs/build.gradle.kts spark-jobs/

COPY log-friends-sdk/src log-friends-sdk/src
COPY examples/src examples/src
COPY proto/ proto/

RUN chmod +x gradlew

RUN ./gradlew :log-friends-sdk:jar :examples:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /workspace/examples/build/libs/examples.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", \
  "-Djdk.attach.allowAttachSelf=true", \
  "-Dnet.bytebuddy.experimental=true", \
  "-Dspring.application.name=order-service", \
  "-Dserver.port=8081", \
  "-jar", "app.jar"]
