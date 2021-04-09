FROM arm64v8/alpine
RUN apk add openjdk11-jre
COPY target/opcua-remote-proxy-0.0.1-SNAPSHOT.jar app.jar
COPY target/dependency /dependency
ENTRYPOINT ["java","-cp","/app.jar", "org.intelligentindustry.opcua.remoteproxy.MainApp"]
