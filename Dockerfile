FROM eclipse-temurin:24 as build

# bootstrap SBT as much as possible
RUN apt update && apt install wget && cd /usr/bin/ && wget https://raw.githubusercontent.com/sbt/sbt/refs/tags/v1.11.6/sbt && chmod +x sbt


WORKDIR /workdir
COPY project/build.properties project/build.properties
COPY project/plugins.sbt project/plugins.sbt
RUN sbt --sbt-create version update

COPY . .
RUN sbt app/stage

FROM eclipse-temurin:24

COPY --from=build /workdir/modules/app/target/jvm-3/universal/stage /workdir/

ENTRYPOINT ["/workdir/bin/app"]
