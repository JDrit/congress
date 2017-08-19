package net.batchik.congress

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Paths, Files}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.JavaConverters._

import DataTypes._

object Main {

  final val billFile = new File("data/congress/115")
  final val stopWordsFile = new File("data/stopwords.txt")
  final val currentPeopleF = new File("data/legislators-current.json")
  final val clusterCount = 20

  implicit val billOrder = Ordering.by[Bill, String](b => b.id)
  implicit val clusterOrder = Ordering.by[(Double, Cluster, Cluster), Double](t => t._1)

  /**
   * Finds the closest cluster to the given cluster
   * @param cluster the cluster to compare to
   * @param allClusters every other cluster
   * @return the distance to the nearest other cluster and the cluster that belongs to
   */
  def bestDistance(cluster: Cluster, allClusters: Array[Cluster]): (Double, Cluster, Cluster) = {
    var bestDistance = Double.MaxValue
    var bestCluster: Cluster = null
    var index = 0
    val length = allClusters.length

    while (index < length) {
      val other = allClusters(index)

      if (other != cluster) {
	val distance = cluster.distance(other)
	if (distance < bestDistance) {
	  bestDistance = distance
	  bestCluster = other
	}
      }
      index += 1
    }
    (bestDistance, cluster, bestCluster)
  }
  
  @tailrec
  def reduceClusters(clusters: Set[Cluster]): Set[Cluster] = if (clusters.size < clusterCount) {
    clusters 
  } else {
    println(s"processing ${clusters.size} clusters")
    val allClusters = clusters.toArray
    val (dist, c1, c2) = clusters.par.map(c => bestDistance(c, allClusters)).min
    val newCluster = c1.merge(c2)
    reduceClusters(clusters - c1 - c2 + newCluster)
  }
    
  def describe(cluster: Cluster, stopWords: Set[String], allBills: Map[String, BillJson]): Unit = {
    val map = new mutable.HashMap[String, Int]().withDefaultValue(0)
    cluster.points.foreach { bill =>
      Bill.formatText(allBills(bill.id).text, stopWords).split(" ").foreach { word => 
	map.put(word, map(word) + 1)
      }
    }
    val topWords = map.toList
      .sortWith { case ((w1, c1), (w2, c2)) => c1 > c2 }
      .take(10)
      .map(_._1)
    println("\n-----------------------------------------------------")
    println(s"size      : ${cluster.points.length}")
    println(s"top words : ${topWords.mkString(", ")}")
  }

  def main(argss: Array[String]): Unit = {
    println("starting application...")
    val initialLoad = Parser.parseBills(billFile)
    val stopWords = Files.readAllLines(stopWordsFile.toPath, StandardCharsets.UTF_8).asScala.toSet
    val mapping = initialLoad.map(b => b.id -> b).toMap

    val mainClusters = reduceClusters(initialLoad.map { b => 
      new Cluster(Bill.create(b.id, b.text, stopWords))
    }.toSet).toList.sortWith { case (c1, c2) => c1.points.length > c2.points.length }

    mainClusters.foreach(c => describe(c, stopWords, mapping))
      
  }

}
