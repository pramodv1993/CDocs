import pika
import analyzer as nlp

consumer_connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
consumer_channel = consumer_connection.channel()




PUBLISHING_QUEUE = 'C_Docs_ProcessedData'
CONSUMING_QUEUE = 'C_Docs_RecognisedData'

consumer_body = None
def consume_and_publish():
	#consume from 2nd step (OCR data)
	consumer_channel.queue_declare(queue =CONSUMING_QUEUE)
	consumer_channel.basic_consume(queue= CONSUMING_QUEUE, on_message_callback = consumer_callback,
auto_ack = False)
	print(' Waiting for messages..')
	consumer_channel.start_consuming()
	
	

def consumer_callback(channel,method,properties,body):
	body = str(body,'utf-8')
	publish(str(nlp.analyze(body)),properties)
	channel.basic_ack(delivery_tag = method.delivery_tag)
	



pub_connection = None
def publish(msg,properties):
	pub_connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
	pub_channel = pub_connection.channel()
	pub_channel.queue_declare(queue=PUBLISHING_QUEUE)
	pub_channel.confirm_delivery()
	pub_channel.basic_publish(exchange='', routing_key=PUBLISHING_QUEUE, properties = pika.BasicProperties(correlation_id = properties.correlation_id),body=msg)
	print(" Published doc with id ",properties.correlation_id," to queue ",PUBLISHING_QUEUE)
	
	






