FROM eclipse-temurin:18-jre

ARG VERSION
COPY ./build/libs/packagemap-java-parser-$VERSION-all.jar /packagemap-java-parser.jar

ENTRYPOINT ["java", "-jar", "/packagemap-java-parser.jar"]

