FROM openjdk:8-jdk-alpine
COPY target/opcuaproxy-0.0.1-SNAPSHOT-jar-with-dependencies.jar app.jar
ENTRYPOINT ["java","-cp","/app.jar", "org.intelligentindustry.opcuaproxy.MainApp"]
