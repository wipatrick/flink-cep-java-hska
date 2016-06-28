from kafka import KafkaProducer
import commands

output = commands.getstatusoutput("docker inspect --format '{{ .NetworkSettings.IPAddress }}' flinkcepjavahska_kafka_1")
KAFKA_IP = output[1];

producer = KafkaProducer(bootstrap_servers=[KAFKA_IP + ':9092'], key_serializer=str.encode)

# for _ in range(100):
producer.send('test', key='test', value=b'neuer wert von python')
