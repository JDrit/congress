package net.batchik.congress

import scala.collection.mutable.MutableList

import DataTypes._

class Cluster(val clustroid: Bill, val points: Array[Bill]) extends CalcDistance[Cluster] {

  def this(prototype: Bill) {
    this(prototype, Array(prototype))
  }

  @inline
  private def sumDistance(bill: Bill, allBills: Array[Bill]): Double = 
    allBills.foldLeft(0.0) { case (sum, other) => Math.sqrt(sum + bill.distance(other)) }

  def merge(other: Cluster): Cluster = {
    val bills = Array.concat(this.points, other.points)
    val clustroid = bills.minBy { bill => sumDistance(bill, bills) }

    new Cluster(clustroid, bills)
  }

  override def distance(other: Cluster): Double = clustroid.distance(other.clustroid)
    
  override def toString: String = s"Cluster size: ${points.size}"
 
}
