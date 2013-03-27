package org.ledyba.logiana.model

import java.io.FileOutputStream
import java.io.DataOutputStream
import java.io.OutputStream
import java.io.InputStream
import java.io.DataInputStream
import scala.collection.mutable.ListBuffer

sealed abstract class Signal(val parent:DataProjection) extends Serializable {
	def repr:String;
	def name:String;
	protected def writeImpl(os:DataOutputStream);
	def write(os:DataOutputStream) = Signal.write(this, os);
}

object Signal {
	def write(self:Signal, os:DataOutputStream) {
		self match {
			case _:LineSignal => os.writeUTF("line");
			case _:ValueSignal => os.writeUTF("value");
		}
		self.writeImpl(os);
	}
	def apply(parent:DataProjection, is:DataInputStream):Signal = {
		is.readUTF() match {
			case "line" => { val x = new LineSignal(parent, is); return x; }
			case "value" => { val x = new ValueSignal(parent, is); return x; }
		}
	}
}

case class LineSignal(override val parent:DataProjection, var name:String, var probeNo:Int) extends Signal(parent) with Serializable {
	def fromWaveData(wavData:MeasuredData, time:Double):Boolean = {
		return wavData.signalAtTime(time, probeNo);
	}
	override def repr() = "LineSignal: "++name++" (probe"+probeNo+")"
	override def toString=repr;

	def this (parent:DataProjection, is:DataInputStream) = {
		this(parent, "", 0);
		name = is.readUTF();
		probeNo = is.readInt();
	}
	override def writeImpl(os:DataOutputStream) = {
		os.writeUTF(name);
		os.writeInt(probeNo);
	}
}

case class ValueSignal(override val parent:DataProjection, var name:String, var lines:Array[(Int, Boolean)]) extends Signal(parent) with Serializable {
	def fromWaveData(wavData:MeasuredData, time:Double):Int = {
		var idx=(-1);
		return lines.foldLeft(0)( (sig:Int, sigData) => {
			val (probeNo,isNegative) = sigData
			idx+=1;
			if (isNegative) {
				(sig | ((if(wavData.signalAtTime(time, probeNo)) 0 else 1) << idx))
			} else {
				(sig | ((if(wavData.signalAtTime(time, probeNo)) 1 else 0) << idx))
			}
		});
	}

	override def repr() = "ValueSignal: "++name++" (probe: "+(lines.map({x => val (num, isNega)=x; (if(isNega) ("!" ++ num.toString) else num.toString)}).mkString(","))+")"
	override def toString=repr;

	def this (parent:DataProjection, is:DataInputStream) = {
		this(parent, "", Array());
		name = is.readUTF();
		val len = is.readInt();
		val list = ListBuffer[(Int,Boolean)]();
		for(_ <- (1 to len)){
			val no = is.readInt();
			val isNeg = is.readBoolean();
			list += ((no,isNeg));
		}
		this.lines = list.toArray;
	}
	override def writeImpl(os:DataOutputStream) = {
		os.writeUTF(name);
		os.writeInt(lines.length);
		for(line <- lines){
			val (no,isNeg) = line;
			os.writeInt(no);
			os.writeBoolean(isNeg);
		}
	}
}
