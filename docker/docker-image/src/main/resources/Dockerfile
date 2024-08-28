FROM ${docker.repo}/${docker.base.name}:${docker.tag}

COPY adapter-server-${project.version}.jar /opt/

USER root
WORKDIR /opt
RUN unzip adapter-server-${project.version}.jar run.sh
RUN unzip adapter-server-${project.version}.jar logback.xml
RUN chmod +x run.sh

EXPOSE 2222

CMD /opt/run.sh
