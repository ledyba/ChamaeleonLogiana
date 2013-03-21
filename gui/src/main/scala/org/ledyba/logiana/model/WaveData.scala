package org.ledyba.logiana.model

import org.ledyba.logiana.control.Session
import org.ledyba.logiana.control.MeasureType
import scala.annotation.serializable
import org.ledyba.logiana.control.Frequency
import org.ledyba.logiana.control.Condition
import org.ledyba.logiana.control.TriggerLine
import java.io.ObjectInputStream
import java.io.FileInputStream
import java.io.ObjectOutputStream
import java.io.FileOutputStream

@SerialVersionUID(0x0L)
class WaveData(sess:Session, dat : Array[Int]) extends Serializable {
	def this() = {
		this(new Session(Frequency._100MHz, MeasureType.Center, Condition.PosEdge, TriggerLine.Probe00), Array.fill(1000)(0));
	}
	val timeLength = dat.length * sess.freq.nanosec;
	val beginTime = sess.measureType match {
		case MeasureType.Center => -timeLength/2;
		case MeasureType.Last => -timeLength
		case MeasureType.Top => 0
	}
	val endTime = beginTime+timeLength
	def timeToIndex(time : Double) = ((time-beginTime)/sess.freq.nanosec).intValue();
	def signalAt(idx:Int, signal : Int):Boolean = {
		val mask = (1<<signal)
		return (dat(idx) & mask) == mask
	}
	def signalAtTime(time : Double, signal : Int) = signalAt(timeToIndex(time), signal)
	val length:Int = dat.length;
	val nanosecPerEntry = sess.freq.nanosec;
}
