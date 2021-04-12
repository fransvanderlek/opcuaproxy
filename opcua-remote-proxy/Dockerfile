FROM arm64v8/alpine
COPY target/app /usr/local/bin/app
RUN chmod +x /usr/local/bin/app
ENTRYPOINT ["app"]
