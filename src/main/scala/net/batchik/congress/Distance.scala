package net.batchik.congress

import java.util.concurrent.ConcurrentHashMap
import java.util.function.{Function => JFunction}

import scala.collection.JavaConverters._

import DataTypes._


trait CalcDistance[A] {
  
  /**
   * Calculates the distance between this type and the given type
   */
  def distance(other: A): Double

}

