FROM arm64v8/alpine
RUN apk add openjdk11
COPY target/opcuaproxy-0.0.1-SNAPSHOT.jar app.jar
COPY target/dependency /dependency
ENTRYPOINT ["java","-cp","/app.jar", "org.intelligentindustry.opcuaproxy.MainApp"]
