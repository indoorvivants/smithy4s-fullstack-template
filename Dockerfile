FROM eclipse-temurin:21 as build

WORKDIR /workdir

# bootstrap SBT as much as possible
RUN apt update && apt install -y curl xz-utils && \
    curl -Lo /usr/bin/sbt https://raw.githubusercontent.com/sbt/sbt/refs/tags/v1.11.7/sbt && \
    chmod +x /usr/bin/sbt
RUN if [ "$(uname -m)" = "x86_64" ]; then \
    curl -Lo node-install.tar.xz https://nodejs.org/dist/v22.18.0/node-v22.18.0-linux-x64.tar.xz; \
    else \
    curl -Lo node-install.tar.xz https://nodejs.org/dist/v22.18.0/node-v22.18.0-linux-arm64.tar.xz; \
    fi && \
    tar -xf node-install.tar.xz && rm *.tar.xz && mv node-v22* node-install
ENV PATH /workdir/node-install/bin:$PATH
COPY project/build.properties project/build.properties
COPY project/plugins.sbt project/plugins.sbt
RUN sbt --sbt-create version update

COPY modules/ modules/
COPY build.sbt .
COPY .sbtopts .
COPY smithy-build.json .
RUN sbt backend/stage frontend/frontendBuild

FROM eclipse-temurin:24

RUN apt update && apt install -y nginx

RUN adduser --disabled-login --disabled-password appuser

COPY --from=build /workdir/modules/backend/target/jvm-3/universal/stage /app/
COPY --from=build /workdir/modules/frontend/dist /app/static/
RUN chown -R appuser:appuser /app /var/lib/nginx /var/log/nginx /run/

COPY nginx/nginx.conf /etc/nginx/sites-available/default
COPY nginx/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

USER appuser

EXPOSE 80

CMD [ "/usr/local/bin/docker-entrypoint.sh" ]
