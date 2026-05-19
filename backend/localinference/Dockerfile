FROM dhi.io/debian-base:trixie-debian13-dev

WORKDIR /server

ARG UID=1000
ARG GID=1000
RUN groupadd --gid ${GID} appuser \
    && useradd --uid ${UID} --gid ${GID} --shell /bin/sh --create-home appuser

RUN apt-get update && apt-get install -y \
    openjdk-25-jdk \
    && rm -rf /var/lib/apt/lists/*

# COPY ./src/ .

RUN chown -R appuser:appuser /server

USER appuser



EXPOSE 54167