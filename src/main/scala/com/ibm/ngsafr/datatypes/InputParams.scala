package com.ibm.ngsafr.datatypes

import java.time.LocalDateTime

case class InputParams(val entityName: String,
                       val partitionId: String,
                       var parallelReplicationCount: Int,
                       val fileName: String,
                       val keyPosition: Int,
                       val keyLength: Int,
                       val effectiveDatePosition: Int,
                       val effectiveDateLength: Int,
                       var currentIdValue: BigInt,
                       var recordNumber: BigInt,
                       var currentEffectiveDate: LocalDateTime) {


  override def toString: String = "{ ( " + entityName + " )" + " ( " + partitionId + " )" + " ( " +
    parallelReplicationCount + " ) " + " ( " + fileName + " ) " + " ( " + keyPosition + " ) " + " ( " + keyLength + " ) " +
    " ( " + effectiveDatePosition + " ) " + " ( " + effectiveDateLength + " ) " + " ( " + currentIdValue + " ) " +
    " ( " + recordNumber + " ) " + " ( " + currentEffectiveDate + " ) "
}
