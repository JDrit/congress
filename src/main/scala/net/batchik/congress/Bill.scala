
package net.batchik.congress

import java.util.concurrent.ConcurrentHashMap
import java.util.function.{Function => JFunction}

object Bill {
  private final val k = 5

  def apply(id: String, text: String): Bill = {
    val shingles = text.sliding(k).map(key => key.hashCode()).toSet
    new Bill(id, shingles)
  }


  // ----------------------------------------------------------------------
  // --------- CACHE ------------------------------------------------------
  // ----------------------------------------------------------------------
  private final val cache = new ConcurrentHashMap[String, Double]()
  
  private final val localBuilder = new ThreadLocal[StringBuilder]() {
    override def initialValue(): StringBuilder = new StringBuilder()

    override def get(): StringBuilder = {
      val builder = super.get()
      builder.setLength(0)
      builder
    }
  }

  def calcKey(s1: String, s2: String): String = if (s1 < s2) {
    localBuilder.get.append(s1).append(',').append(s2).toString
  } else {
    localBuilder.get.append(s2).append(',').append(s1).toString
  }
    

}

/**
 * Main data structure for the bill, contains a representation of the text as
 * shingles
 */
class Bill(val id: String, val shingles: Set[Int]) extends CalcDistance[Bill] {

  override def distance(other: Bill): Double = {
    val key = Bill.calcKey(id, other.id)

    Bill.cache.computeIfAbsent(key, new JFunction[String, Double] {
      override def apply(id: String): Double = {
	val (smaller, larger) = if (shingles.size < other.shingles.size) {
	  (shingles, other.shingles)
	} else {
	  (other.shingles, shingles)
	}

	val intersect = (smaller.count(s => larger.contains(s))).toDouble
	val union = (smaller.size + larger.size - intersect).toDouble

	intersect / union
      }
    })

  }

  override def equals(other: Any): Boolean = other match {
    case b: Bill => b.id == this.id
    case _ => false
  }

  override def hashCode: Int = id.hashCode
}
