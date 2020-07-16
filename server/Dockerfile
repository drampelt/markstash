FROM alpine:latest

RUN adduser -D markstash && addgroup markstash video

RUN echo "http://dl-cdn.alpinelinux.org/alpine/edge/main" > /etc/apk/repositories \
    && echo "http://dl-cdn.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories \
    && echo "http://dl-cdn.alpinelinux.org/alpine/edge/testing" >> /etc/apk/repositories \
    && echo "http://dl-cdn.alpinelinux.org/alpine/v3.11/main" >> /etc/apk/repositories \
    && apk upgrade -U -a \
    && apk add --no-cache \
    bash \
    libstdc++ \
    chromium \
    chromium-chromedriver \
    harfbuzz \
    nss \
    freetype \
    ttf-freefont \
    wqy-zenhei \
    xvfb \
    xvfb-run \
    curl \
    tini \
    openjdk11-jre-headless \
    && rm -rf /var/cache/* \
    && mkdir /var/cache/apk

ENTRYPOINT ["/sbin/tini", "--"]

ADD xvfb-run-safe /usr/bin/xvfb-run-safe
ADD xvfb-chromedriver /usr/bin/xvfb-chromedriver

USER markstash
WORKDIR /home/markstash

ADD build/libs/server.jar /home/markstash/server.jar

ENV MARKSTASH_ENV=production
ENV MARKSTASH_CHROMEDRIVER_BIN=/usr/bin/xvfb-chromedriver

CMD ["java", "-server", "-XX:+UseContainerSupport", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "server.jar"]
