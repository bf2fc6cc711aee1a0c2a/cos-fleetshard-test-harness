FROM registry.access.redhat.com/ubi8/openjdk-11

RUN mkdir test-harness
WORKDIR test-harness
COPY pom.xml pom.xml
COPY src src

# pre-download dependencies, excluding unneeded plugins
RUN  mvn dependency:resolve dependency:resolve-plugins -DexcludeArtifactIds=maven-site-plugin,maven-install-plugin,maven-deploy-plugin,maven-dependency-plugin && \
    rm -rf ~/.m2/repository/org/apache/commons/commons-text/1.3



ENTRYPOINT [ "mvn", "verify", "-ntp", "-fn"]
