hadoop_add_profile cloud

function _cloud_hadoop_classpath {
  for jar in /app/libs/*.jar; do
    hadoop_add_classpath $jar
  done
  hadoop_add_classpath /app/classes
}

function hdfs_subcommand_kube_namenode {
  HADOOP_CLASSNAME=io.espinosa.KubeNameNode
}

function hdfs_subcommand_kube_zkfc {
  HADOOP_CLASSNAME=io.espinosa.KubeZkfc
}
