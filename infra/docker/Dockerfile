# === Builder Stage ===
FROM maven:3.9.6-amazoncorretto-21 AS builder
WORKDIR /build

# Copy Maven settings for build stage (from build context root)
COPY ./settings-build.xml /root/.m2/settings.xml

# 1. Pre-cache dependencies
COPY ./pom.xml .
RUN mvn dependency:go-offline -B -Dmaven.repo.local=/build/.m2/repository

# 2. Compile and Cache Plugins
COPY . .
RUN mvn -B -Dmaven.repo.local=/build/.m2/repository \
    compile \
    -DskipTests=true \
    -Dexec.skip=true

# === Runtime Stage ===
FROM amazoncorretto:21

# Install only what is strictly necessary for security testing
# hadolint ignore=DL3033
RUN yum update -y && \
    yum install -y awscli jq shadow-utils bc && \
    yum clean all && \
    rm -rf /var/cache/yum

# Setup User
RUN useradd -m -u 1001 testrunner
WORKDIR /opt/project

# Copy Maven and project from builder with correct ownership
COPY --from=builder /usr/share/maven /opt/maven
COPY --from=builder --chown=testrunner:testrunner /build/.m2/repository /home/testrunner/.m2/repository
COPY --from=builder --chown=testrunner:testrunner /build /opt/project

# Copy runtime Maven settings to testrunner user (from build context root)
COPY --chown=testrunner:testrunner ./settings-runtime.xml /home/testrunner/.m2/settings.xml

# Ensure the workdir is writable by testrunner
RUN chown -R testrunner:testrunner /opt/project && \
    chmod -R 775 /opt/project

# Environment Setup
ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH
ENV MAVEN_OPTS="-Dmaven.repo.local=/home/testrunner/.m2/repository"

# Copy Scripts with correct ownership and permissions
COPY --chown=testrunner:testrunner ./test-runner.sh /usr/local/bin/test-runner.sh
RUN chmod +x /usr/local/bin/test-runner.sh

USER testrunner
ENTRYPOINT ["/usr/local/bin/test-runner.sh"]