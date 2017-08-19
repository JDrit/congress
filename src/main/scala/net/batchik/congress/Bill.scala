
package net.batchik.congress

import java.util.concurrent.ConcurrentHashMap
import java.util.function.{Function => JFunction}

object Bill {
  private final val k = 7

  def formatText(text: String, stopWords: Set[String]): String = text.toLowerCase()
      .replaceAll("[^\\-A-Za-z0-9 ]", " ")
      .replaceAll(" +", " ")
      .trim()
      .split(" ")
      .foldLeft(new StringBuilder(text.size)) { case (builder, word) =>
	if (!stopWords.contains(word)) {
	  builder.append(word).append(' ')
	} else {
	  builder
	}
      }.toString()
      .trim

  def create(id: String, text: String, stopWords: Set[String]): Bill = {
    val shingles = formatText(text, stopWords)      
      .sliding(k)
      //.map(key => key.hashCode()) //todo is there a better hash code
      .toSet

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

  @inline
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
class Bill(val id: String, val shingles: Set[String]) extends CalcDistance[Bill] {

  override def distance(other: Bill): Double = {
    val key = Bill.calcKey(id, other.id)

    Bill.cache.computeIfAbsent(key, new JFunction[String, Double] {
      override def apply(id: String): Double = {
	val (smaller, larger) = if (shingles.size < other.shingles.size) {
	  (shingles, other.shingles)
	} else {
	  (other.shingles, shingles)
	}
	val intersect = smaller.count(s => larger.contains(s))
	val union = smaller.size + larger.size - intersect
	intersect.toDouble / union
      }
    })
  }

  override def equals(other: Any): Boolean = other match {
    case b: Bill => b.id == this.id
    case _ => false
  }

  override def hashCode: Int = id.hashCode
}
