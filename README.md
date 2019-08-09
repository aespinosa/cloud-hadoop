# cloud-hadoop

Kubernetes experiments to run Hadoop components natively.

# Kubernetes integration for HDFS

Two additional commands are added that integrates running HDFS in Kubernetes:

* `hdfs kube_namenode` - Automatically formats the NameNode's image directory
  based on state information stored in Kubernetes annotations.
* `hdfs kube_zkfc` - (strictly not a Kubernetes integration) Runs `hdfs zkfc
  -formatZK` in an idempotent manner removing the need to run this out of band
  like in Ambari, or other configuration management systems.

## Installation

An example `Dockerfile` implementing these steps can be found in
`docker/Dockerfile`.

1.  Build the package with maven.  This will create a resulting jar in
    `target/cloud-hadoop-1.0-SNAPSHOT.jar`
    
        mvn package -DskipTests

2.  Deploy the built jar in the previous step alongside your Hadoop
    installation.
        
        /hadoop-3.2.0/bin/hdfs
        /hadoop-3.2.0/...
        /cloud-hadoop/cloud-hadoop-1.0-SNAPSHOT.jar

3.  Create the following Shell Profile and install it in your
    `$HADOOP_CONF_DIR/shellprofile.d/directory`

        # $HADOOP_CONF_DIR/shellprofile.d/kubernetes.sh
        hadoop_add_profile cloud

        function _cloud_hadoop_classpath {
          hadoop_add_classpath /cloud-hadoop/cloud-hadoop-1.0-SNAPSHOT.jar
        }

        function hdfs_subcommand_kube_namenode {
          HADOOP_CLASSNAME=io.espinosa.KubeNameNode
        }

        function hdfs_subcommand_kube_zkfc {
          HADOOP_CLASSNAME=io.espinosa.KubeZkfc
        }

## Kubernetes Deployment

An example deployment can be found in `kubernetes/`.

The following are prerequisites to use `hdfs kube_namenode`

* Must be deployed through a StatefulSet
* The PodSpec of the StatefulSet should attach PersistentVolumeClaim through a
  Volume named `image`.
* `dfs.namenode.dir` must be set to be a child directory of the mountPath of the
  persistent volume.

      apiVersion: apps/v1
      kind: StatefulSet
      metadata:
        name: namenode
      spec:
        # ...
        template:
          # ...
          containers:
            - name: namenode
              image: aespinosa/hadoop:3.2.0
              command: ["/hadoop-3.2.0/bin/hdfs"]
              args: ["kube_namenode"]
              volumeMounts:
                - name: image
                  # The hdfs-site.xml property dfs.namenode.name.dir should be a
                  # directory under this mountPath e.g. /var/lib/hdfs/image
                  mountPath: /var/lib/hdfs
            - name: zkfc
              # ...
        volumeClaimTemplates:
          - metadata:
              name: image
              # ...

The following RBAC properties are needed so that `hdfs kube_namenode` will work:

* GET access to the Pod object running the NameNode itself
* GET and PUT access to the StatefulSet managing the Pod
* GET and PUT access to the PersistentVolume that is bound to each Pod in the
  StatefulSet.
