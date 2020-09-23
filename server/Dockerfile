FROM ekidd/rust-musl-builder:latest AS monolith

RUN git clone https://github.com/drampelt/monolith.git .
RUN cargo build --release

# Disabled for now
# FROM oracle/graalvm-ce:20.2.0 AS builder
#
# RUN gu install native-image
#
# ADD build/libs/server.min.jar /server.jar
# RUN native-image \
#     --no-server \
#     --no-fallback \
#     -H:IncludeResources='org/sqlite/native/Linux/x86_64/.*' \
#     -H:+ReportExceptionStackTraces \
#     -jar /server.jar
#
# FROM frolvlad/alpine-glibc:alpine-3.11_glibc-2.31

FROM alpine:3.12.0

RUN adduser -D markstash && addgroup markstash video

RUN echo "http://dl-cdn.alpinelinux.org/alpine/edge/main" > /etc/apk/repositories \
    && echo "http://dl-cdn.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories \
    && echo "http://dl-cdn.alpinelinux.org/alpine/edge/testing" >> /etc/apk/repositories \
    && echo "http://dl-cdn.alpinelinux.org/alpine/v3.11/main" >> /etc/apk/repositories \
    && apk upgrade -U -a \
    && apk add --no-cache \
    libstdc++ \
    chromium \
    chromium-chromedriver \
    harfbuzz \
    nss \
    freetype \
    ttf-freefont \
    wqy-zenhei \
    tini \
    imagemagick \
    openjdk11-jre-headless \
    && rm -rf /var/cache/* \
    && mkdir /var/cache/apk

ENTRYPOINT ["/sbin/tini", "--"]

COPY --from=monolith /home/rust/src/target/x86_64-unknown-linux-musl/release/monolith /usr/bin/monolith

USER markstash
WORKDIR /home/markstash

# COPY --from=builder --chown=markstash:markstash /server /home/markstash/server
ADD build/libs/server.min.jar /home/markstash/server.jar

CMD ["java", "-jar", "/home/markstash/server.jar"]
