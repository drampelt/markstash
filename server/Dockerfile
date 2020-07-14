FROM debian:buster-slim

RUN mkdir /usr/share/man/man1/

RUN groupadd --system markstash && \
    useradd --system --create-home --gid markstash --groups audio,video markstash && \
    mkdir -p /home/markstash && \
    chown --recursive markstash:markstash /home/markstash

RUN apt-get -yqq update && \
    apt-get install -yqq gnupg2 curl unzip xvfb fonts-ipafont-gothic xfonts-100dpi xfonts-75dpi xfonts-scalable xfonts-cyrillic default-jre && \
    rm -rf /var/lib/apt/lists/*

# Install Chrome WebDriver
RUN CHROMEDRIVER_VERSION=`curl -sS chromedriver.storage.googleapis.com/LATEST_RELEASE` && \
    mkdir -p /opt/chromedriver-$CHROMEDRIVER_VERSION && \
    curl -sS -o /tmp/chromedriver_linux64.zip http://chromedriver.storage.googleapis.com/$CHROMEDRIVER_VERSION/chromedriver_linux64.zip && \
    unzip -qq /tmp/chromedriver_linux64.zip -d /opt/chromedriver-$CHROMEDRIVER_VERSION && \
    rm /tmp/chromedriver_linux64.zip && \
    chmod +x /opt/chromedriver-$CHROMEDRIVER_VERSION/chromedriver && \
    ln -fs /opt/chromedriver-$CHROMEDRIVER_VERSION/chromedriver /usr/local/bin/chromedriver

# Install Google Chrome
RUN curl -sS -o - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list && \
    apt-get -yqq update && \
    apt-get -yqq install google-chrome-stable && \
    rm -rf /var/lib/apt/lists/*

ENV TINI_VERSION v0.19.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "--"]

ADD xvfb-run-safe /usr/bin/xvfb-run-safe
ADD xvfb-chromedriver /usr/bin/xvfb-chromedriver

USER markstash
WORKDIR /home/markstash

ADD build/libs/server-1.0-SNAPSHOT-all.jar /home/markstash/server.jar

ENV MARKSTASH_ENV=production
ENV MARKSTASH_CHROMEDRIVER_BIN=/usr/bin/xvfb-chromedriver

CMD ["java", "-server", "-XX:+UseContainerSupport", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "server.jar"]

