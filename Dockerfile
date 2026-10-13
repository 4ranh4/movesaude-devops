# syntax=docker/dockerfile:1
# MoveSaúde API: imagem multi-stage. O build usa o JDK e o Maven Wrapper; o runtime leva só o JRE e o .jar.

# ---------- Etapa 1: build ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Dependências primeiro: esta camada só é refeita quando o pom.xml muda (cache de build)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Código-fonte e empacotamento (os testes rodam no pipeline, antes do build da imagem)
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:25-jre

# Commit gravado na imagem pelo pipeline (--build-arg GIT_SHA=...), exibido em /api/info
ARG GIT_SHA=""
ENV APP_COMMIT=${GIT_SHA} \
    JAVA_OPTS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError"

# curl para o HEALTHCHECK e usuário sem privilégios para rodar a aplicação
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system movesaude \
 && useradd --system --gid movesaude --no-create-home movesaude

WORKDIR /app
COPY --from=build /workspace/target/movesaude-api.jar app.jar
USER movesaude

# O Render informa a porta pela variável PORT; localmente o padrão é 8080
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fsS "http://localhost:${PORT:-8080}/actuator/health/liveness" || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
