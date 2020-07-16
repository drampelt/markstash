FROM ekidd/rust-musl-builder:latest AS builder

RUN git clone https://github.com/Y2Z/monolith.git .
RUN cargo build --release

FROM alpine:latest

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
    openjdk11-jre-headless \
    argon2-dev \
    && rm -rf /var/cache/* \
    && mkdir /var/cache/apk

ENTRYPOINT ["/sbin/tini", "--"]

COPY --from=builder /home/rust/src/target/x86_64-unknown-linux-musl/release/monolith /usr/bin/monolith

USER markstash
WORKDIR /home/markstash

ADD build/libs/server.jar /home/markstash/server.jar

ENV MARKSTASH_ENV=production
ENV MARKSTASH_CHROMEDRIVER_BIN=/usr/bin/chromedriver

CMD ["java", "-server", "-XX:+UseContainerSupport", "-Xms256m", "-Xmx256m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "server.jar"]
