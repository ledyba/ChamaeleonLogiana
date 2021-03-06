package org.ledyba.logiana.model

import org.ledyba.logiana.control.Condition
import org.ledyba.logiana.control.MeasureType
import scala.annotation.serializable
import org.ledyba.logiana.control.Frequency
import org.ledyba.logiana.control.Condition
import org.ledyba.logiana.control.TriggerLine
import java.io.ObjectInputStream
import java.io.FileInputStream
import java.io.ObjectOutputStream
import java.io.FileOutputStream
import org.ledyba.logiana.control.Operation
import java.io.DataInputStream
import java.io.DataOutputStream
import scala.collection.mutable.ListBuffer

@SerialVersionUID(0x0L)
case class MeasuredData(op:Operation, dat : Array[Int]) extends Serializable {
	def this() = {
		this(new Operation(Frequency._100MHz, MeasureType.Center, Condition.PosEdge, TriggerLine.Probe00), Array.fill(1000)(0));
	}
	val timeLength = dat.length * op.freq.nanosec;
	val beginTime = op.measureType match {
		case MeasureType.Center => -timeLength/2;
		case MeasureType.Last => -timeLength
		case MeasureType.Top => 0
	}
	val endTime = beginTime+timeLength
	def timeToIndex(time : Double) = ((time-beginTime)/op.freq.nanosec).intValue();
	def signalAt(idx:Int, signal : Int):Boolean = {
		val mask = (1<<signal)
		return (dat(idx) & mask) == mask
	}
	def signalAtTime(time : Double, signal : Int) = signalAt(timeToIndex(time), signal)
	val length:Int = dat.length;
	val nanosecPerEntry = op.freq.nanosec;
	def write(os:DataOutputStream) = {
		op.write(os);
		os.writeInt(dat.length);
		for(d <- dat) {
			os.writeInt(d);
		}
	}
}

object MeasuredData{
	def apply(is:DataInputStream):MeasuredData = {
		val op = Operation(is);
		val list = ListBuffer[Int]();
		val len = is.readInt();
		for(_ <- (1 to len)){
			list += is.readInt();
		}
		return MeasuredData(op, list.toArray);
	}
}
