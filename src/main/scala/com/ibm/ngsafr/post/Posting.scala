package com.ibm.ngsafr.post

import com.ibm.ngsafr.datatypes._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Try

object Posting {

  private var listOfVendorHistoryObjectWithLowestKey: List[Vendor] = Nil
  private var listOfVendorHistoryObject: List[Vendor] = Nil

  private var listOfVendorUpdateObjectWithLowestKey: List[VendorUpdate] = Nil
  private var listOfVendorUpdateObject: List[VendorUpdate] = Nil

  private var listOfBalanceObjectWithLowestKey: List[Balances] = Nil
  private var listOfBalanceObject: List[Balances] = Nil

  private var listOfTransactionObjectWithLowestKey: List[Transaction] = Nil
  private var listOfTransactionObject: List[Transaction] = Nil

  private var lowestCurrentIdKey = InputParams

  def main(args: Array[String]): Unit = {

    val filePaths = Map("inputPath" -> "c:\\gitwork\\ngsafr\\data\\", "outputPath" ->
      "/ngsafr/temp/")

    val listOfAgencies = populateAgencyData(filePaths)
    val mapOfAgencies: Map[String, String] = listOfAgencies.map(agency => (agency.agencyCode, agency.agencyName)).toMap
    var masterAgencyDataForAccumulatedTransactions: mutable.Map[String, Double] = mutable.Map.empty[String, Double]

    println("#################################################################################")
    println("##########" + "\t" + "Started the CKB")
    println("#################################################################################")
    val t1 = System.currentTimeMillis

    var listParams = setupInputParams()

    val mappedFileNames = listParams.map(listParam => listParam.entityName.concat(listParam.partitionId) -> listParam.fileName).toMap

    var vendorHistoryCurrentData: Iterator[String] = null
    var vendorUpdateCurrentData: Iterator[String] = null
    var balanceHistoryCurrentData: Iterator[String] = null
    var transactionCurrentData: Iterator[String] = null

    for (mappedFileName <- mappedFileNames) {
      if (mappedFileName._1.equals("VendorHistory")) {
        val vendorHistoryFileIterator: Iterator[String] = Source.fromFile(filePaths("inputPath") + mappedFileNames("VendorHistory")).getLines()
        vendorHistoryCurrentData = vendorHistoryFileIterator.buffered
      } else if (mappedFileName._1.equals("VendorUpdate")) {
        val vendorUpdateFileIterator: Iterator[String] = Source.fromFile(filePaths("inputPath") + mappedFileNames("VendorUpdate")).getLines()
        vendorUpdateCurrentData = vendorUpdateFileIterator.buffered
      } else if (mappedFileName._1.equals("BalanceHistory")) {
        val balanceHistoryFileIterator: Iterator[String] = Source.fromFile(filePaths("inputPath") + mappedFileNames("BalanceHistory")).getLines()
        balanceHistoryCurrentData = balanceHistoryFileIterator.buffered
      } else if (mappedFileName._1.equals("TransactionDaily")) {
        val transactionFileIterator: Iterator[String] = Source.fromFile(filePaths("inputPath") + mappedFileNames("TransactionDaily")).getLines()
        transactionCurrentData = transactionFileIterator.buffered
      }
    }

    var vendorHistoryRecordCount: Int = 0
    val vendorCurrentRecord = new Vendor("", "", "", "", "", "", "", "")

    var freshReadIndicatorForVendorHistoryIs: Boolean = true
    var freshReadIndicatorForVendorUpdateIs: Boolean = true
    var freshReadIndicatorForBalancesIs: Boolean = true
    var freshReadIndicatorForTransactionsIs: Boolean = true

    var reachedEOFForVendorHistory = false
    var reachedEOFForVendorUpdate = false
    var reachedEOFForBalanceHistory = false
    var reachedEOFForTransactions = false

    var vendorHistory = Vendor.apply("", "", "", "", "", "", "", "")
    var vendorUpdate = VendorUpdate.apply("", "", "", "")
    var balanceHistory = Balances.apply("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")
    var transaction = Transaction.apply("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")

    var vendorHistoryRow: Array[String] = Array.empty
    var vendorUpdateRow: Array[String] = Array.empty
    var balanceHistoryRow: Array[String] = Array.empty
    var transactionRow: Array[String] = Array.empty

    while (reachedEOFForVendorHistory == false || reachedEOFForVendorUpdate == false || reachedEOFForBalanceHistory == false || reachedEOFForTransactions == false) {
      if (freshReadIndicatorForVendorHistoryIs) {
        vendorHistoryRow = iterateThroughVendorHistory(vendorHistoryCurrentData)
        if (!vendorHistoryRow.isEmpty) {
          vendorHistory = Vendor.apply(vendorHistoryRow(0), vendorHistoryRow(1), vendorHistoryRow(2), vendorHistoryRow(3),
            vendorHistoryRow(4), vendorHistoryRow(5), vendorHistoryRow(6), vendorHistoryRow(7))
        }
      }
      if (freshReadIndicatorForVendorUpdateIs) {
        vendorUpdateRow = iterateThroughVendorUpdate(vendorUpdateCurrentData)
        if (!vendorUpdateRow.isEmpty || vendorUpdateRow != null) {
          vendorUpdate = VendorUpdate.apply(vendorUpdateRow(0), vendorUpdateRow(1), vendorUpdateRow(2), vendorUpdateRow(3))
        }
      }
      if (freshReadIndicatorForBalancesIs) {
        balanceHistoryRow = iterateThroughBalanceHistory(balanceHistoryCurrentData)
        if (!balanceHistoryRow.isEmpty) {
          balanceHistory = Balances.apply(balanceHistoryRow(0), balanceHistoryRow(1), balanceHistoryRow(2), balanceHistoryRow(3),
            balanceHistoryRow(4), balanceHistoryRow(5), balanceHistoryRow(6), balanceHistoryRow(7), balanceHistoryRow(8), balanceHistoryRow(9),
            balanceHistoryRow(10), balanceHistoryRow(11), balanceHistoryRow(12), balanceHistoryRow(13), balanceHistoryRow(14), balanceHistoryRow(15),
            balanceHistoryRow(16), balanceHistoryRow(17), balanceHistoryRow(18))
        }
      }
      if (freshReadIndicatorForTransactionsIs) {
        transactionRow = iterateThroughTransactions(transactionCurrentData)
        if (!transactionRow.isEmpty) {
          transaction = Transaction.apply(transactionRow(0), transactionRow(1), transactionRow(2), transactionRow(3),
            transactionRow(4), transactionRow(5), transactionRow(6), transactionRow(7), transactionRow(8), transactionRow(9),
            transactionRow(10), transactionRow(11), transactionRow(12), transactionRow(13), transactionRow(14), transactionRow(15),
            transactionRow(16), transactionRow(17), transactionRow(18), transactionRow(19), transactionRow(20), transactionRow(21), transactionRow(22),
            transactionRow(23), transactionRow(24), transactionRow(25), transactionRow(26), transactionRow(27), transactionRow(28),
            transactionRow(29), transactionRow(30))
        }
      }
      var vendorHistoryInputParam = (listParams.filter(_.entityName.equals("Vendor")).filter(_.partitionId.equals("History"))).head
      if (vendorHistory.instinstID != null)
        vendorHistoryInputParam.currentIdValue = BigInt(vendorHistory.instinstID)
      if (reachedEOFForVendorHistory)
        vendorHistoryInputParam.currentIdValue = BigInt(Int.MaxValue)
      if (vendorHistoryCurrentData.hasNext == false)
        reachedEOFForVendorHistory = true

      var vendorUpdateInputParam = (listParams.filter(_.entityName.equals("Vendor")).filter(_.partitionId.equals("Update"))).head
      if (vendorUpdate.instinstID != null)
        vendorUpdateInputParam.currentIdValue = BigInt(vendorUpdate.instinstID)
      if (reachedEOFForVendorUpdate)
        vendorUpdateInputParam.currentIdValue = BigInt(Int.MaxValue)
      if (vendorUpdateCurrentData.hasNext == false)
        reachedEOFForVendorUpdate = true

      var balanceHistoryInputParam = (listParams.filter(_.entityName.equals("Balance")).filter(_.partitionId.equals("History"))).head
      if (balanceHistory.balinstID != null)
        balanceHistoryInputParam.currentIdValue = BigInt(balanceHistory.balinstID)
      if (reachedEOFForBalanceHistory)
        balanceHistoryInputParam.currentIdValue = BigInt(Int.MaxValue)
      if (balanceHistoryCurrentData.hasNext == false)
        reachedEOFForBalanceHistory = true

      var transactionInputParam = (listParams.filter(_.entityName.equals("Transaction")).filter(_.partitionId.equals("Daily"))).head
      if (transaction.transinstID != null)
        transactionInputParam.currentIdValue = BigInt(transaction.transinstID)
      if (reachedEOFForTransactions)
        transactionInputParam.currentIdValue = BigInt(Int.MaxValue)
      if (transactionCurrentData.hasNext == false)
        reachedEOFForTransactions = true

      val inputParamWithLowestCurrentIdKey = listParams.minBy(inputParams => inputParams.currentIdValue)
      val mapWithLowestKeyId = listParams.filter(_.currentIdValue == inputParamWithLowestCurrentIdKey.currentIdValue)
        .map(inputParams => (inputParams.entityName.concat(inputParams.partitionId), inputParams.currentIdValue)).toMap
      //println("Map with the mapping of lowest key to the file from which it was read :" + mapWithLowestKeyId)

      mapWithLowestKeyId.foreach {
        entry => {
          if (entry._1.contains("VendorHistory")) {
            if (vendorHistory.instinstID != null) {
              if (BigInt(vendorHistory.instinstID) == entry._2) {
                populateListOfVendorHistoryObjectsMatchingLowestKey(vendorHistory)
              }
            }
            while (Try {
              BigInt(vendorHistoryRow.head)
            }.getOrElse(-1) == entry._2) {
              vendorHistoryRow = iterateThroughVendorHistory(vendorHistoryCurrentData)
              val vendorHistoryForSameID = Vendor.apply(vendorHistoryRow(0), vendorHistoryRow(1), vendorHistoryRow(2), vendorHistoryRow(3),
                vendorHistoryRow(4), vendorHistoryRow(5), vendorHistoryRow(6), vendorHistoryRow(7))
              if (Try {
                BigInt(vendorHistoryRow.head)
              }.getOrElse(-1) == entry._2) {
                populateListOfVendorHistoryObjectsMatchingLowestKey(vendorHistoryForSameID)
              } else {
                if (vendorHistoryForSameID.instinstID != null) {
                  vendorHistory = vendorHistoryForSameID
                  freshReadIndicatorForVendorHistoryIs = false
                  populateListOfVendorHistoryObjects(vendorHistoryForSameID)
                }
              }
            }

          } else if (entry._1.contains("VendorUpdate")) {
            if (vendorUpdate.instinstID != null) {
              if (BigInt(vendorUpdate.instinstID) == entry._2) {
                populateListOfVendorUpdateObjectsMatchingLowestKey(vendorUpdate)
              }
            }
            //println("################################################## :" + vendorUpdateRow.head)
            while (Try {
              BigInt(vendorUpdateRow.head)
            }.getOrElse(-1) == entry._2) {
              vendorUpdateRow = iterateThroughVendorUpdate(vendorUpdateCurrentData)
              val vendorUpdateForSameID = VendorUpdate.apply(vendorUpdateRow(0), vendorUpdateRow(1), vendorUpdateRow(2), vendorUpdateRow(3))
              if (Try {
                BigInt(vendorUpdateRow.head)
              }.getOrElse(-1) == entry._2) {
                populateListOfVendorUpdateObjectsMatchingLowestKey(vendorUpdateForSameID)
              } else {

                if (vendorUpdateForSameID.instinstID != null) {
                  vendorUpdate = vendorUpdateForSameID
                  freshReadIndicatorForVendorUpdateIs = false
                  populateListOfVendorUpdateObjects(vendorUpdateForSameID)
                }
              }
            }
          } else if (entry._1.contains("BalanceHistory")) {
            if (balanceHistory.balinstID != null) {
              if (BigInt(balanceHistory.balinstID) == entry._2) {
                populateListOfBalanceObjectsMatchingLowestKey(balanceHistory)
              }
            }
            while (Try {
              BigInt(balanceHistoryRow.head)
            }.getOrElse(-1) == entry._2) {
              balanceHistoryRow = iterateThroughBalanceHistory(balanceHistoryCurrentData)
              val balanceHistoryForSameID = Balances.apply(balanceHistoryRow(0), balanceHistoryRow(1), balanceHistoryRow(2), balanceHistoryRow(3),
                balanceHistoryRow(4), balanceHistoryRow(5), balanceHistoryRow(6), balanceHistoryRow(7), balanceHistoryRow(8), balanceHistoryRow(9),
                balanceHistoryRow(10), balanceHistoryRow(11), balanceHistoryRow(12), balanceHistoryRow(13), balanceHistoryRow(14), balanceHistoryRow(15),
                balanceHistoryRow(16), balanceHistoryRow(17), balanceHistoryRow(18))
              if (Try {
                BigInt(balanceHistoryRow.head)
              }.getOrElse(-1) == entry._2) {
                if (balanceHistoryForSameID != null)
                  populateListOfBalanceObjectsMatchingLowestKey(balanceHistoryForSameID)
              } else {

                if (balanceHistoryForSameID.balinstID != null) {
                  //println("Entered here - is it a Joke!!!! " + balanceHistoryForSameID.balinstID)
                  balanceHistory = balanceHistoryForSameID
                  freshReadIndicatorForBalancesIs = false
                  populateListOfBalanceObjects(balanceHistoryForSameID)
                }
              }
            }
          } else if (entry._1.contains("Transaction")) {
            if (transaction.transinstID != null) {
              if (BigInt(transaction.transinstID) == entry._2) {
                populateListOfTransactionObjectsMatchingLowestKey(transaction)
              }
            }
            while (Try {
              BigInt(transactionRow.head)
            }.getOrElse(-1) == entry._2) {
              transactionRow = iterateThroughTransactions(transactionCurrentData)
              val transactionForSameID = Transaction.apply(transactionRow(0), transactionRow(1), transactionRow(2), transactionRow(3),
                transactionRow(4), transactionRow(5), transactionRow(6), transactionRow(7), transactionRow(8), transactionRow(9),
                transactionRow(10), transactionRow(11), transactionRow(12), transactionRow(13), transactionRow(14), transactionRow(15),
                transactionRow(16), transactionRow(17), transactionRow(18), transactionRow(19), transactionRow(20), transactionRow(21), transactionRow(22),
                transactionRow(23), transactionRow(24), transactionRow(25), transactionRow(26), transactionRow(27), transactionRow(28),
                transactionRow(29), transactionRow(30))
              if (Try {
                BigInt(transactionRow.head)
              }.getOrElse(-1) == entry._2) {
                populateListOfTransactionObjectsMatchingLowestKey(transactionForSameID)
              } else {
                if (transactionForSameID.transinstID != null) {
                  transaction = transactionForSameID
                  freshReadIndicatorForTransactionsIs = false
                  populateListOfTransactionObjects(transactionForSameID)
                }
              }
            }
          }
        }
      }

      val listOfVendorForViewProcesing = (listOfVendorHistoryObjectWithLowestKey.filter(vendor1 => BigInt(vendor1.instinstID) ==
        inputParamWithLowestCurrentIdKey.currentIdValue) ::: listOfVendorHistoryObject.filter(vendor2 => listOfVendorHistoryObject.
        exists(v => BigInt(vendor2.instinstID) == inputParamWithLowestCurrentIdKey.currentIdValue))).distinct
      //println(listOfVendorForViewProcesing)

      listOfVendorHistoryObjectWithLowestKey = listOfVendorHistoryObjectWithLowestKey.dropWhile(vendorToDrop =>
        BigInt(vendorToDrop.instinstID) != inputParamWithLowestCurrentIdKey.currentIdValue)
      listOfVendorHistoryObject = listOfVendorHistoryObject.dropWhile(vendorToDrop =>
        BigInt(vendorToDrop.instinstID) != inputParamWithLowestCurrentIdKey.currentIdValue)


      val listOfVendorUpdateForViewProcessing = (listOfVendorUpdateObjectWithLowestKey.filter(vendorUpdate1 => BigInt(vendorUpdate1.instinstID) ==
        inputParamWithLowestCurrentIdKey.currentIdValue) ::: listOfVendorUpdateObject.filter(vendorUpdate2 => listOfVendorUpdateObject.
        exists(vUpd => BigInt(vendorUpdate2.instinstID) == inputParamWithLowestCurrentIdKey.currentIdValue))).distinct
      //println(listOfVendorUpdateForViewProcessing)

      listOfVendorUpdateObjectWithLowestKey = listOfVendorUpdateObjectWithLowestKey.dropWhile(vendorUpdateToDrop => BigInt(vendorUpdateToDrop.instinstID) !=
        inputParamWithLowestCurrentIdKey.currentIdValue)
      listOfVendorUpdateObject = listOfVendorUpdateObject.dropWhile(vendorUpdateToDrop => BigInt(vendorUpdateToDrop.instinstID) !=
        inputParamWithLowestCurrentIdKey.currentIdValue)

      val listOfBalancesForViewProcessing = (listOfBalanceObjectWithLowestKey.filter(balance1 => BigInt(balance1.balinstID) ==
        inputParamWithLowestCurrentIdKey.currentIdValue) ::: listOfBalanceObject.filter(balance2 => listOfBalanceObject.
        exists(bal => BigInt(balance2.balinstID) == inputParamWithLowestCurrentIdKey.currentIdValue))).distinct
      //println(listOfBalancesForViewProcessing)

      listOfBalanceObjectWithLowestKey = listOfBalanceObjectWithLowestKey.dropWhile(balanceToDrop =>
        BigInt(balanceToDrop.balinstID) != inputParamWithLowestCurrentIdKey.currentIdValue)
      listOfBalanceObject = listOfBalanceObject.dropWhile(balanceToDrop =>
        BigInt(balanceToDrop.balinstID) != inputParamWithLowestCurrentIdKey.currentIdValue)

      val listOfTransactionsForViewProcessing = (listOfTransactionObjectWithLowestKey.filter(transaction1 => BigInt(transaction1.transinstID) ==
        inputParamWithLowestCurrentIdKey.currentIdValue) ::: listOfTransactionObject.filter(transaction2 => listOfTransactionObject.
        exists(trans => BigInt(transaction2.transinstID) == inputParamWithLowestCurrentIdKey.currentIdValue))).distinct
      //println(listOfTransactionsForViewProcessing)

      val someValue: Map[String, Double] = listOfTransactionsForViewProcessing.groupBy(_.translegalEntityID)
        .mapValues(_.map(_.transtransAmount.toDouble).sum)
      val newValue = mutable.Map[String, Double]() ++ someValue
      masterAgencyDataForAccumulatedTransactions = masterAgencyDataForAccumulatedTransactions ++ insertOrUpdateAgencyMappedData(newValue, masterAgencyDataForAccumulatedTransactions)


      listOfTransactionObjectWithLowestKey = listOfTransactionObjectWithLowestKey.dropWhile(transactionObjectToDrop => BigInt(transactionObjectToDrop.transinstID)
        != inputParamWithLowestCurrentIdKey.currentIdValue)
      listOfTransactionObject = listOfTransactionObject.dropWhile(transactionObjectToDrop => BigInt(transactionObjectToDrop.transinstID)
        != inputParamWithLowestCurrentIdKey.currentIdValue)

    }

    val mapOfAgenciesToAccumulatedTransactions = masterAgencyDataForAccumulatedTransactions.map(agency =>
      mapOfAgencies getOrElse(agency._1.toInt.toString,"noMatch") match {
      case "noMatch" => agency
      case value => (value -> (agency._2))
    })



    //println(mapOfAgencies.map(_._2) zip masterAgencyDataForAccumulatedTransactions.map(_._2))
    println("Accumulated transactions by Agency name")
    println(mapOfAgenciesToAccumulatedTransactions)
    println("#################################################################################")
    val t2 = System.currentTimeMillis
    println((t2 - t1) + " milliseconds")
  }

  private def populateListOfVendorHistoryObjectsMatchingLowestKey(vendor: => Vendor): List[Vendor]

  = {
    listOfVendorHistoryObjectWithLowestKey = vendor :: listOfVendorHistoryObjectWithLowestKey
    listOfVendorHistoryObjectWithLowestKey
  }

  private def populateListOfVendorHistoryObjects(vendor: => Vendor): List[Vendor]

  = {
    listOfVendorHistoryObject = vendor :: listOfVendorHistoryObject
    listOfVendorHistoryObject
  }

  private def populateListOfVendorUpdateObjectsMatchingLowestKey(vendorUpdate: => VendorUpdate): List[VendorUpdate]

  = {
    listOfVendorUpdateObjectWithLowestKey = vendorUpdate :: listOfVendorUpdateObjectWithLowestKey
    listOfVendorUpdateObjectWithLowestKey
  }

  private def populateListOfVendorUpdateObjects(vendorUpdate: => VendorUpdate): List[VendorUpdate]

  = {
    listOfVendorUpdateObject = vendorUpdate :: listOfVendorUpdateObject
    listOfVendorUpdateObject
  }

  private def populateListOfBalanceObjectsMatchingLowestKey(balance: => Balances): List[Balances]

  = {
    listOfBalanceObjectWithLowestKey = balance :: listOfBalanceObjectWithLowestKey
    listOfBalanceObjectWithLowestKey
  }

  private def populateListOfBalanceObjects(balance: => Balances): List[Balances]

  = {
    listOfBalanceObject = balance :: listOfBalanceObject
    listOfBalanceObject
  }

  private def populateListOfTransactionObjectsMatchingLowestKey(transaction: => Transaction): List[Transaction]

  = {
    listOfTransactionObjectWithLowestKey = transaction :: listOfTransactionObjectWithLowestKey
    listOfTransactionObjectWithLowestKey
  }

  private def populateListOfTransactionObjects(transaction: => Transaction): List[Transaction]

  = {
    listOfTransactionObject = transaction :: listOfTransactionObject
    listOfTransactionObject
  }


  private def setupInputParams(): ListBuffer[InputParams]

  = {
    val listInputParams = ListBuffer[InputParams]()
    val vendorHistoryInputParam = new InputParams("Vendor", "History", 0, "VendorHistory.txt",
      1, 10, 12, 10, 0, 0, null)
    val vendorUpdateInputParam = new InputParams("Vendor", "Update", 0, "VendorUpdate.txt",
      1, 10, 0, 0, 0, 0, null)
    val balancesInputParam = new InputParams("Balance", "History", 0, "BalanceHistory.txt",
      1, 10, 0, 0, 0, 0, null)
    val transactionInputParam = new InputParams("Transaction", "Daily", 0, "Transaction.txt",
      1, 10, 0, 0, 0, 0, null)


    listInputParams.append(vendorHistoryInputParam)
    listInputParams.append(vendorUpdateInputParam)
    listInputParams.append(balancesInputParam)
    listInputParams.append(transactionInputParam)

    //println(listInputParams.minBy(InputParams => InputParams.currentIdLowKeyValue))
    /* listInputParams.sortBy(InputParams => InputParams.currentIdLowKeyValue).foreach(println)*/

    listInputParams
  }

  def populateAgencyData(filePaths: Map[String, String]): List[Agency] = {
    val agencyFileIterator: Iterator[String] = Source.fromFile(filePaths("inputPath") + "VARefAgencyCSV.txt").getLines()
    var agencyCurrentData = agencyFileIterator.buffered
    var agencyCurrentRecord: Array[String] = new Array[String](3)

    var listOfAgencyObjects = List[Agency]()
    while (agencyCurrentData.hasNext) {
      //      val agencyTblLineStrValue = agencyCurrentData.next()
      //      val agencyObject = Agency.apply(agencyTblLineStrValue.substring(0,3),agencyTblLineStrValue.substring(8,14))
      agencyCurrentRecord = agencyCurrentData.next().split(",")
      val agencyObject = Agency.apply(agencyCurrentRecord(0), agencyCurrentRecord(2))
      listOfAgencyObjects = agencyObject :: listOfAgencyObjects
    }
    listOfAgencyObjects.sortBy(_.agencyCode)
  }

  def insertOrUpdateAgencyMappedData[K, V](mappedAccumulatedTransactions: mutable.Map[String, Double],
                                           masterAgencyDataForAccumulatedTransactionsParams: mutable.Map[String, Double]): mutable.Map[String, Double] = {
    val masterAgencyDataMap: mutable.Map[String, Double] = masterAgencyDataForAccumulatedTransactionsParams ++ mappedAccumulatedTransactions
      .map { case (agencyCode, transAmount) => agencyCode ->
        (transAmount + mappedAccumulatedTransactions.getOrElse(agencyCode, 0.0))
      }
    masterAgencyDataMap
  }

  def iterateThroughVendorHistory(vendorHistoryCurrentData: => Iterator[String]): Array[String] = {
    val vendorHistoryCurrentRecord: Array[String] = new Array[String](8)
    if (vendorHistoryCurrentData.hasNext) {
      val instTBLLineStrValue = vendorHistoryCurrentData.next()
      vendorHistoryCurrentRecord(0) = instTBLLineStrValue.substring(0, 10)
      vendorHistoryCurrentRecord(1) = instTBLLineStrValue.substring(11, 21)
      vendorHistoryCurrentRecord(2) = instTBLLineStrValue.substring(22, 23)
      vendorHistoryCurrentRecord(3) = instTBLLineStrValue.substring(24, 34)
      vendorHistoryCurrentRecord(4) = instTBLLineStrValue.substring(35, 36)
      vendorHistoryCurrentRecord(5) = instTBLLineStrValue.substring(37, 67)
      vendorHistoryCurrentRecord(6) = instTBLLineStrValue.substring(68, 71)
      vendorHistoryCurrentRecord(7) = instTBLLineStrValue.substring(72, 73)
    }
    vendorHistoryCurrentRecord
  }

  def iterateThroughVendorUpdate(vendorUpdateCurrentData: => Iterator[String]): Array[String] = {
    val vendorUpdateCurrentRecord: Array[String] = new Array[String](4)
    if (vendorUpdateCurrentData.hasNext) {
      val instTBLLineStrValue = vendorUpdateCurrentData.next()
      vendorUpdateCurrentRecord(0) = instTBLLineStrValue.substring(0, 10)
      vendorUpdateCurrentRecord(1) = instTBLLineStrValue.substring(11, 51)
      vendorUpdateCurrentRecord(2) = instTBLLineStrValue.substring(52, 55)
      vendorUpdateCurrentRecord(3) = instTBLLineStrValue.substring(56, 66)
    }
    vendorUpdateCurrentRecord
  }

  def iterateThroughBalanceHistory(balanceHistoryCurrentData: => Iterator[String]): Array[String] = {
    val balanceHistoryCurrentRecord: Array[String] = new Array[String](19)
    if (balanceHistoryCurrentData.hasNext) {
      val balanceLineStrValue = balanceHistoryCurrentData.next()
      balanceHistoryCurrentRecord(0) = balanceLineStrValue.substring(0, 10)
      balanceHistoryCurrentRecord(1) = balanceLineStrValue.substring(11, 21)
      balanceHistoryCurrentRecord(2) = balanceLineStrValue.substring(22, 29)
      balanceHistoryCurrentRecord(3) = balanceLineStrValue.substring(30, 33)
      balanceHistoryCurrentRecord(4) = balanceLineStrValue.substring(34, 48)
      balanceHistoryCurrentRecord(5) = balanceLineStrValue.substring(49, 53)
      balanceHistoryCurrentRecord(6) = balanceLineStrValue.substring(54, 59)
      balanceHistoryCurrentRecord(7) = balanceLineStrValue.substring(60, 65)
      balanceHistoryCurrentRecord(8) = balanceLineStrValue.substring(66, 67)
      balanceHistoryCurrentRecord(9) = balanceLineStrValue.substring(68, 74)
      balanceHistoryCurrentRecord(10) = balanceLineStrValue.substring(75, 78)
      balanceHistoryCurrentRecord(11) = balanceLineStrValue.substring(79, 82)
      balanceHistoryCurrentRecord(12) = balanceLineStrValue.substring(83, 86)
      balanceHistoryCurrentRecord(13) = balanceLineStrValue.substring(87, 94)
      balanceHistoryCurrentRecord(14) = balanceLineStrValue.substring(95, 99)
      balanceHistoryCurrentRecord(15) = balanceLineStrValue.substring(100, 112)
      balanceHistoryCurrentRecord(16) = balanceLineStrValue.substring(113, 115)
      //balanceHistoryCurrentRecord(17) = balanceLineStrValue.substring(116, 117)
      //balanceHistoryCurrentRecord(18) = balanceLineStrValue.substring(119, 120)
    }
    balanceHistoryCurrentRecord
  }

  def iterateThroughTransactions(transactionCurrentData: => Iterator[String]): Array[String] = {
    val transactionCurrentRecord: Array[String] = new Array[String](31);
    if (transactionCurrentData.hasNext) {
      val transLineStrValue = transactionCurrentData.next()
      transactionCurrentRecord(0) = transLineStrValue.substring(0, 10)
      transactionCurrentRecord(1) = transLineStrValue.substring(11, 28)
      transactionCurrentRecord(2) = transLineStrValue.substring(29, 31)
      transactionCurrentRecord(3) = transLineStrValue.substring(31, 71)
      transactionCurrentRecord(4) = transLineStrValue.substring(72, 79)
      transactionCurrentRecord(5) = transLineStrValue.substring(80, 83)
      transactionCurrentRecord(6) = transLineStrValue.substring(84, 98)
      transactionCurrentRecord(7) = transLineStrValue.substring(99, 103)
      transactionCurrentRecord(8) = transLineStrValue.substring(103, 109)
      transactionCurrentRecord(9) = transLineStrValue.substring(110, 115)
      transactionCurrentRecord(10) = transLineStrValue.substring(116, 118)
      transactionCurrentRecord(11) = transLineStrValue.substring(118, 124)
      transactionCurrentRecord(12) = transLineStrValue.substring(125, 128)
      transactionCurrentRecord(13) = transLineStrValue.substring(129, 132)
      transactionCurrentRecord(14) = transLineStrValue.substring(133, 136)
      transactionCurrentRecord(15) = transLineStrValue.substring(137, 144)
      transactionCurrentRecord(16) = transLineStrValue.substring(145, 157)
      transactionCurrentRecord(17) = transLineStrValue.substring(158, 165)
      transactionCurrentRecord(18) = transLineStrValue.substring(166, 176)
      transactionCurrentRecord(19) = transLineStrValue.substring(177, 187)
      transactionCurrentRecord(20) = transLineStrValue.substring(188, 189)
      transactionCurrentRecord(21) = transLineStrValue.substring(190, 191)
      transactionCurrentRecord(22) = transLineStrValue.substring(192, 193)
      transactionCurrentRecord(23) = transLineStrValue.substring(194, 195)
      transactionCurrentRecord(24) = transLineStrValue.substring(196, 197)
      transactionCurrentRecord(25) = transLineStrValue.substring(198, 199)
      transactionCurrentRecord(26) = transLineStrValue.substring(200, 201)
      transactionCurrentRecord(27) = transLineStrValue.substring(202, 203)
      transactionCurrentRecord(28) = transLineStrValue.substring(204, 205)
      transactionCurrentRecord(29) = transLineStrValue.substring(206, 207)
      transactionCurrentRecord(30) = transLineStrValue.substring(208, 209)
    }
    transactionCurrentRecord
  }

}
