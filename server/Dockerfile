FROM rust:1.46.0-buster AS monolith

RUN git clone https://github.com/drampelt/monolith.git
RUN cd monolith && cargo build --release


FROM oracle/graalvm-ce:20.2.0 AS builder

RUN gu install native-image

ADD build/libs/server.min.jar /server.jar
RUN native-image \
    --no-server \
    --no-fallback \
    -H:IncludeResources='org/sqlite/native/Linux/x86_64/.*' \
    -H:+ReportExceptionStackTraces \
    -jar /server.jar


FROM debian:buster-slim

RUN groupadd -r markstash && useradd -r -g markstash -G audio,video markstash \
	&& mkdir -p /home/markstash && chown -R markstash:markstash /home/markstash \
	&& mkdir -p /opt/google/chrome && chown -R markstash:markstash /opt/google/chrome

ENV CHROMEDRIVER_VERSION 85.0.4183.87

RUN apt-get update && apt-get install -y \
	apt-transport-https \
	ca-certificates \
	curl \
	gnupg \
	unzip \
	--no-install-recommends \
	&& curl -sSL https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
	&& echo "deb https://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list \
	&& apt-get update && apt-get install -y \
	google-chrome-stable \
	fontconfig \
	fonts-ipafont-gothic \
	fonts-wqy-zenhei \
	fonts-thai-tlwg \
	fonts-kacst \
	fonts-symbola \
	fonts-noto \
	fonts-freefont-ttf \
	imagemagick \
	librsvg2-bin \
	tini \
	--no-install-recommends \
	&& wget -q --continue -P /tmp "http://chromedriver.storage.googleapis.com/$CHROMEDRIVER_VERSION/chromedriver_linux64.zip" \
	&& unzip /tmp/chromedriver_linux64.zip -d /usr/bin \
	&& rm /tmp/chromedriver_linux64.zip \
	&& apt-get purge --auto-remove -y curl gnupg unzip \
	&& rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["/usr/bin/tini", "--"]

COPY --from=monolith /monolith/target/release/monolith /usr/bin/monolith
COPY --from=builder --chown=markstash:markstash /server /home/markstash/server

USER markstash
WORKDIR /home/markstash

CMD ["/home/markstash/server"]
