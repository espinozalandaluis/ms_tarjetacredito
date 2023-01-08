FROM openjdk:11
#VOLUME /tmp
EXPOSE 8089
COPY "./target/tarjetacredito-0.0.1-SNAPSHOT.jar" "tarjetacredito.jar"
ENTRYPOINT ["java","-jar","tarjetacredito.jar"]