package net.batchik.congress

import java.io.File

import DataTypes._

object Main {

  final val billFile = new File("data/congress/")
  final val currentPeopleF = new File("data/legislators-current.json")

  implicit val billOrder = Ordering.by[Bill, String](b => b.id)
  implicit val clusterOrder = Ordering.by[(Double, Cluster, Cluster), Double](t => t._1)
  
  final val clusterCount = 20

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
  
  def reduceClusters(clusters: Set[Cluster]): Set[Cluster] = if (clusters.size < clusterCount) {
    clusters 
  } else {
    println(s"processing ${clusters.size} clusters")
    val allClusters = clusters.toArray
    val (dist, c1, c2) = clusters.par.map(c => bestDistance(c, allClusters)).min
    val newCluster = c1.merge(c2)
    reduceClusters(clusters - c1 - c2 + newCluster)
  }
    

  def process(bills: List[Bill]): Unit = 
    reduceClusters(bills.map(b => new Cluster(b)).toSet).foreach(println)

  def main(argss: Array[String]): Unit = {
    println("starting application...")
    val bills = Parser.parseBills(billFile).map { b => Bill(b.id, b.text) }
    //val currentPeople = Parser.parsePeople(currentPeopleF)

    process(bills)
      
  }

}
