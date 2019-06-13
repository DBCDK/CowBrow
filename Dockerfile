FROM docker.dbc.dk/dbc-python3

MAINTAINER cluster

ARG artifact_name=CowBrow-0.1.0-payaramicro.jar
ARG Cowbrow_jar_artifact=target/${artifact_name}

ENV CBW_CONF "https://git.dbc.dk/config/dataio/raw/master/cowbrowweb_conf.json"

ENV SSL no

USER root

RUN mkdir -p /data/cowbrowweb && \
    apt-get update && \
    apt-get install wget jdk8-dbc openssl -qy && \
    pip install requests Flask

RUN apt-get update && apt-get install -y procps

ADD ${Cowbrow_jar_artifact} /data/cowbrowweb/
ADD ui/*.py /data/cowbrowweb/
ADD ui/mq_python/* /data/cowbrowweb/mq_python/
ADD ui/utils/* /data/cowbrowweb/utils/
ADD ui/templates/* /data/cowbrowweb/templates/
ADD ui/static/css/* /data/cowbrowweb/static/css/
ADD ui/static/js/* /data/cowbrowweb/static/js/
ADD ui/static/fonts/* /data/cowbrowweb/static/fonts/
ADD ui/start-servers.sh /data/cowbrowweb/

ADD ui/generate-ssl-certificate.sh /data/cowbrowweb/

WORKDIR /data/cowbrowweb


RUN /data/cowbrowweb/generate-ssl-certificate.sh

CMD  ./start-servers.sh



EXPOSE 5000
