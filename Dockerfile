FROM openjdk:8
COPY target/youTubeParser-0.0.1-SNAPSHOT.jar youTubeParser.jar
EXPOSE 8088

ENTRYPOINT ["java", "-jar", "/youTubeParser.jar"]

RUN apt-get update && apt-get install -y \
    libglib2.0-0 \
    libnss3 \
    libx11-6 \
    unzip \
    curl \
    wget \
    xvfb

ARG CHROME_VERSION=65.0.3325.181
#ARG CHROME_VERSION=88
RUN curl https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add \
      && wget https://www.slimjet.com/chrome/download-chrome.php?file=lnx%2Fchrome64_$CHROME_VERSION.deb \
      && dpkg -i download-chrome*.deb || true
RUN apt-get install -y -f \
      && rm -rf /var/lib/apt/lists/*

RUN dpkg-divert --add --rename --divert /opt/google/chrome/google-chrome.real /opt/google/chrome/google-chrome \
        && echo "#! /bin/bash\nexec /opt/google/chrome/google-chrome.real --no-sandbox --disable-setuid-sandbox \"\$@\"" > /opt/google/chrome/google-chrome \
        && chmod 755 /opt/google/chrome/google-chrome

#ARG CHROME_DRIVER_VERSION=2.38
ARG CHROME_DRIVER_VERSION=88.0.4324.96
RUN mkdir -p /opt/selenium \
        && curl http://chromedriver.storage.googleapis.com/$CHROME_DRIVER_VERSION/chromedriver_linux64.zip -o /opt/selenium/chromedriver_linux64.zip \
        && cd /opt/selenium; unzip /opt/selenium/chromedriver_linux64.zip; rm -rf chromedriver_linux64.zip; ln -fs /opt/selenium/chromedriver /usr/local/bin/chromedriver;
