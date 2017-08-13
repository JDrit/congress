package net.batchik.congress

import scala.collection.mutable.MutableList

import DataTypes._

class Cluster(val points: Array[Bill]) extends CalcDistance[Cluster] {

  def this(prototype: Bill) {
    this(Array(prototype))
  }

  def merge(other: Cluster): Cluster = new Cluster(Array.concat(this.points, other.points))

  override def distance(other: Cluster): Double = {
    var distance = 0.0
    var count = 0

    var c1Length = this.points.length
    var c2Length = other.points.length
    var c1Index = 0

    while (c1Index < c1Length) {
      var c2Index = 0
      while (c2Index < c2Length) {
	distance += this.points(c1Index).distance(other.points(c2Index))
	count += 1
      }
      c2Index += 1
    }
    distance / count
  }


  override def toString: String = {
    s"Cluster size: ${points.size}"
  }

 
}
