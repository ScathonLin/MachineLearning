package com.scathon.ml.scala.cluster.kmeans

import scala.collection.mutable

trait Kmeans {
   type DIM_TYPE
   type DS_TYPE

   def getInitClusterCenterPts(data: DS_TYPE): mutable.Buffer[mutable.Buffer[DIM_TYPE]]

   def loadData(): DS_TYPE

   def calculateDistance(oriPts: mutable.Buffer[DIM_TYPE], tarPts: mutable.Buffer[DIM_TYPE]): Double
}
