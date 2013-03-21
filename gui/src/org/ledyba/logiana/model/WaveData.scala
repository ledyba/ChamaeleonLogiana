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
	private val SAMPLES		= (1024*128);
	private val WAVE_MAX	= 64;
	private val HBAR_MIN	= (-SAMPLES/4+2)
	private val HBAR_MAX	= (SAMPLES/4-1-(WAVE_MAX/2)-1)
	private val HBAR_CENTER	= -17
	
	val timeLength = dat.length * sess.freq.nanosec;
	val beginTime = sess.measureType match{
		case MeasureType.Center => -((HBAR_CENTER*2+WAVE_MAX/2) * sess.freq.nanosec)
		case MeasureType.Last => -((HBAR_MAX*2+WAVE_MAX/2) * sess.freq.nanosec)
		case MeasureType.Top => -((HBAR_MIN*2+24) * sess.freq.nanosec)
	}
	val endTime = beginTime+timeLength
	def timeToIndex(time : Float) = ((time-beginTime)/sess.freq.nanosec).intValue();
	def signalAt(idx:Int, signal : Int):Boolean = {
		val mask = (1<<signal)
		return (dat(idx) & mask) == mask
	}
	def signalAtTime(time : Float, signal : Int) = signalAt(timeToIndex(time), signal)
	val length:Int = dat.length;
	val nanosecPerEntry:Float = sess.freq.nanosec;
}
