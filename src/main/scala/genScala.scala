import java.io.{File, PrintWriter}
import scala.io.Source

//###########################################################################
//  Next Gen SAFR
//
//  Program:  genScala
//  This program reads:
//    - logic table or parameter file
//
//  It writes:
//    - A SAFR scala program
//
//	Copyright 2018, IBM Corporation.  All rights reserved.
//     Kip Twitchell <finsysvlogger@gmail.com>.
//                   kip.twitchell@us.ibm.com
//  Created June 2018
//###########################################################################

//###########################################################################
//  to dos:
//###########################################################################
object genScala {
  def main(args: Array[String]): Unit = {

    //*****************************************************************************************
    // Open output files
    //*****************************************************************************************
    // summary view table
    val newScalaProg = new PrintWriter(new File("/data/ngsafrProg"))

    var scalaWritten = 0

    //*****************************************************************************************
    // Open input MergeParm file
    //*****************************************************************************************
    
    val mergeParmFileX: Iterator[String] = Source.fromFile("/data/mergeParms").getLines()
    val mergeParmLine: BufferedIterator[String] = mergeParmFileX.buffered
    println("Module configured for mergparm TBL File")


    //*****************************************************************************************
    // Read MergeParm file
    //*****************************************************************************************

    var mergparmRead = 0

    def readmergparm(): Unit = {
      
      // below to be updated with mergeparm positions...... 
      
      val e: Array[String] = new Array[String](8)
      if (mergeParmLine.hasNext) {
        val mergeParmLineStrValue = mergeParmLine.next()
        e(0) = mergeParmLineStrValue.substring(0, 9)
        e(1) = mergeParmLineStrValue.substring(11, 20)
        e(2) = mergeParmLineStrValue.substring(22, 23)
        e(3) = mergeParmLineStrValue.substring(24, 33)
        e(4) = mergeParmLineStrValue.substring(35, 36)
        e(5) = mergeParmLineStrValue.substring(37, 66)
        e(6) = mergeParmLineStrValue.substring(68, 70)
        e(7) = mergeParmLineStrValue.substring(72, 73)
      }

      mergparmRead += 1
      // if (debugPrint == "Y") println("mergparm rec full key: " + testmergparmFullKey)
    }

    var mergparmEOF = "N"

    def eofmergparm(): Unit = {
      // if (debugPrint == "Y") println("mergparm high values. EOF mergparm")
      mergparmEOF = "Y"
    }

    if (mergeParmLine.isEmpty) eofmergparm()
    else readmergparm()

  }

}
