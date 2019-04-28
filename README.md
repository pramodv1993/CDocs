## The application (CDocs) is divided into 3 micro-services
 1. UI
 2. OCR 
 3. NLP

It is used for document recignition, processing and potentially context extraction of the documents.
 
 1. UI  and OCR are implenented as Spring Boot applications.
    Tessaract4J is used which is a Java distribution of Tessaract - Optical Character Recognition
 2. NLP is implemented in Python using the open source library Spacy.

Rabbit MQ messaging Queue has been used for communication between the services.

## Further Work:

The microservices needs to be dockerized to achieve total isolation in their workings.

The application assumes the rabbit mq services is running in the local. This has been implemented just as a prototype and not for commercial purpose.

