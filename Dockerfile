FROM docker.io/openjdk:11-slim

RUN apt-get update \
    && apt-get install -y curl bash fonts-dejavu \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

VOLUME /opt/opentripplanner/graph

ENV OTP_ROOT="/opt/opentripplanner"

WORKDIR ${OTP_ROOT}
RUN touch logback.xml

ADD target/*-shaded.jar ${OTP_ROOT}/otp-shaded.jar
ADD entrypoint.sh .
RUN chmod +x entrypoint.sh

EXPOSE 8080
EXPOSE 8081

ENV JAVA_OPTS="-Xmx8G"

ENTRYPOINT ["./entrypoint.sh"]

