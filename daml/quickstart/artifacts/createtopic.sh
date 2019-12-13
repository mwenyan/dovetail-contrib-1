docker run --net=host --rm confluentinc/cp-kafka:5.3.1 kafka-topics --create --topic $1 --partitions 1 --replication-factor 1 --if-not-exists --zookeeper localhost:22181
