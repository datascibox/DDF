package io.ddf.spark.ds

import io.ddf.{DDFManager, DDF}
import scala.collection.JavaConversions.mapAsScalaMap

trait DataSource {
  def loadDDF(options: Map[AnyRef, AnyRef]): DDF

  def loadDDF(options: java.util.Map[AnyRef, AnyRef]): DDF = {
    loadDDF(options.toMap)
  }
}

abstract class BaseDataSource(val uri: String, val ddfManager: DDFManager) extends DataSource
