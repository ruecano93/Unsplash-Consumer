FROM openjdk:11
COPY ./target/unsplashConsumer-0.0.1-SNAPSHOT.jar app/unplash-consumer.jar
EXPOSE 8000
CMD java -jar app/unplash-consumer.jar