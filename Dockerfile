FROM maven:3.5-alpine as builder
COPY ./* /app
RUN cd /app && mvn package
FROM openjdk:11-alpine
COPY --from=builder ./target/unsplashConsumer-0.0.1-SNAPSHOT.jar unplash-consumer.jar
CMD java -jar /unplash-consumer.jar