package com.scathon.ml.scala.cluster.kmeans.config

import java.util.Properties

import org.apache.commons.lang3.StringUtils


object KmeansConfiguration {
   var dimension: Int = 0
   var clusterNum: Int = 0
   var filePath: String = StringUtils.EMPTY
   var maxIterationNum: Int = 0
   var configFilePath: String = "cluster/kmeans/config/kmeans.properties"
   val prop = new Properties()
   prop.load(KmeansConfiguration.getClass.getClassLoader.getResourceAsStream(configFilePath))
   dimension = prop.getProperty("kmeans.point.dimension").toInt
   clusterNum = prop.getProperty("kmeans.cluster.num").toInt
   filePath = prop.getProperty("kmeans.training.file.path")
   maxIterationNum = prop.getProperty("kmeans.max.iteration.num").toInt
}
