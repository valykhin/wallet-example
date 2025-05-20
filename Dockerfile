ARG BUILD_IMAGE='eclipse-temurin:17.0.11_9-jdk-centos7'

FROM ${BUILD_IMAGE} as builder

ADD ./gradle gradle
ADD ./gradlew gradlew
ADD ./build.gradle.kts build.gradle.kts
ADD ./settings.gradle.kts settings.gradle.kts
ADD ./src src

RUN ./gradlew clean build --no-daemon

FROM ${BUILD_IMAGE}

RUN mkdir -p /app
COPY --from=builder build/libs/wallet.jar /app/app.jar

EXPOSE 8080

RUN groupadd app; useradd app -g app -M -d /app
RUN chown -R app:app /app
USER app

WORKDIR /app
ENV JAVA_OPTS="-Xms100m -Xmx256m"
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]