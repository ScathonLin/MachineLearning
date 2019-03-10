package com.scathon.ml.scala.cluster.kmeans

import com.scathon.ml.scala.cluster.kmeans.config.KmeansConfiguration

import scala.collection.mutable
import scala.io.Source
import scala.util.Random

object ScalaApiKmeansAlgorithm extends App with Kmeans {
   override type DIM_TYPE = Double
   override type DS_TYPE = mutable.Buffer[mutable.Buffer[Double]]

   override def loadData(): DS_TYPE = {
      val filePath = KmeansConfiguration.filePath
      val sourceSome = Some(Source.fromFile(filePath))
      for (source <- sourceSome) {
         val lines = source.getLines().map(_.split(","))
            .filter(_.length == KmeansConfiguration.dimension)
            .map(_.map(_.toDouble).toBuffer).toBuffer
         return lines
      }
      mutable.Buffer(mutable.Buffer())
   }

   override def getInitClusterCenterPts(data: DS_TYPE): mutable.Buffer[mutable.Buffer[Double]] = {
      val sampleNum = data.size
      var haveChoiced = 0
      val random = new Random()
      val choicedPtdIdSet = mutable.Set[Int]()
      val initClusterCenterPts = mutable.Buffer[mutable.Buffer[Double]]()
      val clusterNum = KmeansConfiguration.clusterNum
      while (haveChoiced < clusterNum) {
         val choiced = random.nextInt(sampleNum)
         if (!choicedPtdIdSet.contains(choiced)) {
            initClusterCenterPts += data(choiced)
            haveChoiced += 1
         }
      }
      initClusterCenterPts
   }


   def kmeansCore(): mutable.HashMap[Int, mutable.MutableList[Int]] = {
      val oriData = loadData()
      val initClusterCenter = getInitClusterCenterPts(oriData)
      val cluster = mutable.HashMap[Int, mutable.MutableList[Int]]()
      for (i <- 0 until KmeansConfiguration.clusterNum) {
         cluster.put(i, mutable.MutableList())
      }
      val maxIterationNum = KmeansConfiguration.maxIterationNum
      var iterationCursor = 0
      val clusterNum = KmeansConfiguration.clusterNum
      val sampleNum = oriData.size
      while (iterationCursor < maxIterationNum) {
         for (i <- 0 until sampleNum) {
            var minDistance = Double.MaxValue
            var minClusterIndex = -1;
            for (j <- 0 until clusterNum) {
               val distance = calculateDistance(oriData(i), initClusterCenter(j))
               if (distance < minDistance) {
                  minDistance = distance
                  minClusterIndex = j
               }
            }
            var ptsList = cluster(minClusterIndex)
            if (ptsList == null) {
               cluster.put(minClusterIndex, mutable.MutableList[Int]())
               ptsList = cluster(minClusterIndex)
            }
            ptsList += i
         }
         for (index <- 0 until clusterNum) {
            var sum = 0D
            val ptsList = cluster(index)
            for (di <- 0 until KmeansConfiguration.dimension) {
               for (elem <- ptsList) {
                  sum += oriData(elem)(di)
               }
               initClusterCenter(index)(di) = sum / ptsList.size
            }
         }
         if (iterationCursor != maxIterationNum - 1) {
            cluster.foreach(_._2.clear())
         }
         iterationCursor += 1
      }
      cluster
   }

   override def calculateDistance(oriPts: mutable.Buffer[Double], tarPts: mutable.Buffer[Double]): Double = {
      val dimension = KmeansConfiguration.dimension
      var sum = 0D
      for (index <- 0 until dimension) {
         sum += Math.pow(oriPts(index) - tarPts(index), 2)
      }
      Math.sqrt(sum)
   }

   val result = kmeansCore()
   result.foreach(entry=>println(entry._1+"==>"+ entry._2.mkString(",")))
}
