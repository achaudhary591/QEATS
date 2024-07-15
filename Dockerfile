FROM eclipse-temurin:11-jdk-focal

VOLUME /tmp

# Copy all the source files
COPY ./gradle ./gradle
COPY gradlew build.gradle ./
COPY settings.gradle ./
COPY ./qeatsbackend/src ./qeatsbackend/src
COPY ./qeatsbackend/build.gradle ./qeatsbackend/build.gradle
COPY ./assets ./assets

ENTRYPOINT ["./gradlew", "bootRun", "--no-daemon"]