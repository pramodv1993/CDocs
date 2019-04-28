FROM python:3

ADD . /usr/local/bin


WORKDIR /usr/local/bin
RUN pip install spacy

CMD [ "python", "./queue_listener.py" ]