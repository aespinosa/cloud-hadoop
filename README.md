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


1.  Prepare the base Docker image containing the Hadoop distribution.  An
    example can be found in `docker/Dockerfile`.
        
        docker build -t aespinosa/hadoop -f docker/Dockerfile .
        docker push aespinosa/hadoop

1.  Build the shaded Kubernetes Java client and install to the local maven
    repository.

        pushd shade-kubernetes
        maven install
        popd

2.  Build the Docker image.  This will directly push the image to the target
    registry defined by `-Dimage...`  

        mvn compile jib:build -Dimage=aespinosa/hdfs

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
              image: aespinosa/hdfs:3.2.0
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
