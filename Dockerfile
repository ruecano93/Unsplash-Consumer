FROM openjdk:11
COPY ./target/unsplashConsumer-0.0.1-SNAPSHOT.jar app/unplash-consumer.jar
EXPOSE 8080
CMD java -jar app/unplash-consumer.jar