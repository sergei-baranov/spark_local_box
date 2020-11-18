FROM openjdk:jre-alpine

# Spark
ARG APACHE_SPARK_VERSION=2.4.7
ARG HADOOP_VERSION=2.7
ENV SPARK_NAME=spark-${APACHE_SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}

ENV SPARK_DIR /opt/${SPARK_NAME}
ENV SPARK_HOME /usr/local/spark
ENV SPARK_OPTS --driver-java-options=-Xms1024M --driver-java-options=-Xmx2048M --driver-java-options=-Dlog4j.logLevel=warn

# curl https://www.apache.org/dyn/closer.lua/spark/spark-${APACHE_SPARK_VERSION}/${SPARK_NAME}.tgz | \
RUN mkdir -p /usr/share/man/man1 && \
    mkdir /opt && \
    apk update && \
    apk add curl bash && \
    curl https://archive.apache.org/dist/spark/spark-${APACHE_SPARK_VERSION}/${SPARK_NAME}.tgz | \
    tar xzf - -C /opt && \
    apk del curl

# Standardize system
RUN ln -s $SPARK_DIR $SPARK_HOME

COPY ./spark_local_box-1.0-uber.jar /usr/bin/app/spark_local_box-1.0-uber.jar
COPY ./shara/spark_local_box.conf /shara/spark_local_box.conf
WORKDIR /usr/bin/app

ENTRYPOINT tail -f /dev/null
# mvn clean package
# sudo podman stop spark_local_box_1
# sudo podman rm spark_local_box_1
# sudo podman rmi localhost/spark_local_box
# sudo podman build -f Dockerfile -t localhost/spark_local_box -m 2g --cpuset-cpus 1,3,5 --cpu-shares 512
# sudo podman run -d --network host -v shara:/shara --name spark_local_box_1 -m 2g --cpuset-cpus 1,3,5 --cpu-shares 512 spark_local_box
# sudo podman volume inspect shara
# sudo cp ./shara/spark_local_box.conf /var/lib/containers/storage/volumes/shara/_data/spark_local_box.conf
# sudo podman exec -d -t spark_local_box_1 java -jar spark_local_box-1.0-uber.jar com.sergei_baranov.spark_local_box.MsApp # ENTRYPOINT
# wget -qO- http://localhost:9077/ping # 9077 see spark_local_box.conf