FROM docker.dbc.dk/dbc-python3

MAINTAINER cluster

ARG artifact_name=CowBrow-0.1.0-payaramicro.jar
ARG Cowbrow_jar_artifact=http://is.dbc.dk/view/IOscrum/job/CowBrow/lastSuccessfulBuild/artifact/target/${artifact_name}
LABEL SvnRevision=$svn_revision

ENV CB_JAR ${Cowbrow_jar_artifact}
ENV CBW_CONF "https://git.dbc.dk/config/dataio/raw/master/cowbrowweb_conf.json"
ENV CBW_NAME ${artifact_name}

ENV SSL no

USER root

RUN mkdir -p /data/cowbrowweb && \
    apt-get update && \
    apt-get install wget jdk8-dbc    -qy && \
    wget ${Cowbrow_jar_artifact} -P  /data/cowbrowweb && \
    pip3 install requests Flask

ADD *.py /data/cowbrowweb/
ADD mq_python/* /data/cowbrowweb/mq_python/
ADD utils/* /data/cowbrowweb/utils/
ADD templates/* /data/cowbrowweb/templates/
ADD static/css/* /data/cowbrowweb/static/css/
ADD static/js/* /data/cowbrowweb/static/js/
ADD static/fonts/* /data/cowbrowweb/static/fonts/
ADD server.crt  /data/cowbrowweb/
ADD server.key  /data/cowbrowweb/
ADD start-servers.sh /data/cowbrowweb/

WORKDIR /data/cowbrowweb

CMD  ./start-servers.sh



EXPOSE 5000
