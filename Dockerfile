# =========================================================
# KAIRÓS backend - imagen de Docker en DOS etapas
#   1. "compilacion": tiene Maven y el JDK; compila y arma el .jar
#   2. imagen final: solo trae Java (JRE) y el .jar -> más liviana y segura
# Construir:  docker build -t kairos-backend .
# =========================================================

# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-21 AS compilacion
WORKDIR /build

# Primero solo el pom.xml: así Docker reutiliza las dependencias descargadas
# si solo cambió el código (las compilaciones siguientes son más rápidas)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Las pruebas no se corren aquí: la prueba de arranque necesita PostgreSQL.
# Se corren antes, con .\mvnw.cmd clean test
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# La aplicación NO corre como administrador (root) del contenedor
RUN useradd --system --no-create-home kairos

COPY --from=compilacion /build/target/*.jar app.jar
USER kairos

EXPOSE 8080

# Java usa como máximo el 75 % de la memoria que se le dé al contenedor
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
