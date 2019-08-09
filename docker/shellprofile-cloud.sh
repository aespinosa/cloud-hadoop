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
