# vi: set ft=dockerfile :
# 
# Copyright 2019 Allan Espinosa
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM marketplace.gcr.io/google/centos7 as base

RUN echo 7 > /etc/yum/vars/releasever && \
    yum makecache && \
    yum install -q -y java-11-openjdk-headless && \
    yum clean all
ENV JAVA_HOME /usr/lib/jvm/jre-11-openjdk

FROM base as hadoop-builder

RUN curl -O -s http://mirror.cc.columbia.edu/pub/software/apache/hadoop/common/hadoop-3.2.0/hadoop-3.2.0.tar.gz && \
    tar xzf hadoop-3.2.0.tar.gz

# Build the Zookeeper image with something like
# docker build --target hadoop -t myusername/hadoop docker/
FROM base as hadoop

COPY --from=hadoop-builder /hadoop-3.2.0 /hadoop-3.2.0

FROM base as zookeeper-builder

RUN curl -s -O http://mirror.cc.columbia.edu/pub/software/apache/zookeeper/zookeeper-3.5.5/apache-zookeeper-3.5.5-bin.tar.gz && \
    tar xzf apache-zookeeper-3.5.5-bin.tar.gz

# Build the Zookeeper image with something like
# docker build --target zookeeper -t myusername/zookeeper docker/
FROM base as zookeeper

COPY --from=zookeeper-builder /apache-zookeeper-3.5.5-bin /zookeeper-3.5.5
# FIXME run and configure zookeeper properly
COPY --from=zookeeper-builder /apache-zookeeper-3.5.5-bin/conf/zoo_sample.cfg /zookeeper-3.5.5/conf/zoo.cfg
