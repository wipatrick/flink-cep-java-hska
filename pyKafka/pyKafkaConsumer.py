from kafka import KafkaConsumer
import commands

output = commands.getstatusoutput("docker inspect --format '{{ .NetworkSettings.IPAddress }}' flinkcepjavahska_kafka_1")
KAFKA_IP = output[1];

consumer = KafkaConsumer('test',
                         group_id='pyGroup',
                         bootstrap_servers=[KAFKA_IP + ':9092'])

for message in consumer:
    print ("%s:%d:%d key=%s value=%s" % (message.topic, message.partition,
                                         message.offset, message.key,
                                         message.value))
