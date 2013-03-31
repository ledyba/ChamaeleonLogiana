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
	def notifyDataChanged(data:MeasuredData):Unit = Unit
}

object Signal {
	def write(self:Signal, os:DataOutputStream) {
		self match {
			case _:LineSignal => os.writeUTF("line");
			case _:ValueSignal => os.writeUTF("value");
			case _:SPISignal => os.writeUTF("spi");
		}
		self.writeImpl(os);
	}
	def apply(parent:DataProjection, is:DataInputStream):Signal = {
		is.readUTF() match {
			case "line" => { val x = new LineSignal(parent, is); return x; }
			case "value" => { val x = new ValueSignal(parent, is); return x; }
			case "spi" => { val x = new SPISignal(parent, is); return x; }
		}
	}
}

case class LineSignal(override val parent:DataProjection, var name:String, var probeNo:Int) extends Signal(parent) with Serializable {
	def fromWaveData(wavData:MeasuredData, idx:Int):Boolean = {
		return wavData.signalAt(idx, probeNo);
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
	def fromWaveData(wavData:MeasuredData, dataIdx:Int):Int = {
		var idx=(-1);
		return lines.foldLeft(0)( (sig:Int, sigData) => {
			val (probeNo,isNegative) = sigData
			idx+=1;
			if (isNegative) {
				(sig | ((if(wavData.signalAt(dataIdx, probeNo)) 0 else 1) << idx))
			} else {
				(sig | ((if(wavData.signalAt(dataIdx, probeNo)) 1 else 0) << idx))
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

case class SPISignal(override val parent:DataProjection, var name:String, var csProbe:Int, var csAssertedByHigh:Boolean, var sclkProbe:Int, var dataProbe:Int, var isNegEdge:Boolean, var isMSBFirst:Boolean) extends Signal(parent) with Serializable {
	def fromWaveData(wavData:MeasuredData, dataIdx:Int):(Boolean, Int) = {
		return cache(dataIdx);
	}

	override def repr =
		"SPISignal: "++name++" (probes sclk:"+sclkProbe+" data: "+dataProbe+" with "+(if(isNegEdge)("negEdge")else("posEdge"))+")"
	override def toString=repr;

	def this (parent:DataProjection, is:DataInputStream) = {
		this(parent, "", 0, false, 0, 0, false, true);
		this.name = is.readUTF;
		this.csProbe = is.readInt;
		this.csAssertedByHigh = is.readBoolean;
		this.sclkProbe = is.readInt;
		this.dataProbe = is.readInt;
		this.isNegEdge = is.readBoolean;
		this.isMSBFirst = is.readBoolean;
	}
	var cache:Array[(Boolean, Int)] = null;
	override def notifyDataChanged(data:MeasuredData) = {
		val c = ListBuffer.fill(data.length)(null:(Boolean, Int));
		var nowEnabled = false;
		var sigVal = 0;
		var bitIdx = 0;
		var cacheStart = 0;
		for(i <- Range(0,data.length)) {
			val nowCS = if(csAssertedByHigh) data.signalAt(0, csProbe) else !data.signalAt(0, csProbe);
			if(nowCS && !nowEnabled){ //CSがAssetedされたまさにその瞬間
				sigVal = 0;
				bitIdx = 0;
				cacheStart = i;
			}
			nowEnabled = nowCS;
			val sclk:Boolean = //SCLKが書き換わる瞬間
				if (i>0)
					if(isNegEdge) data.signalAt(i-1, sclkProbe) && !data.signalAt(i, sclkProbe)
					else !data.signalAt(i-1, sclkProbe) && data.signalAt(i, sclkProbe)
				else false
			if(nowCS && sclk){
				sigVal |= (if(data.signalAt(i-1, sclkProbe)) 1 else 0) << (if(isMSBFirst)(7-bitIdx)else(bitIdx));
				bitIdx += 1;
				if(bitIdx >= 8){
					c(cacheStart) = (true, sigVal);
					for(x <- Range(cacheStart+1, i)) {
						c(x) = (false, sigVal);
					}
					sigVal = 0;
					bitIdx = 0;
					cacheStart = i;
				}
			}
		}
		cache = c.toArray;
	}
	override def writeImpl(os:DataOutputStream) = {
		os.writeUTF(name);
		os.writeInt(csProbe);
		os.writeBoolean(csAssertedByHigh);
		os.writeInt(sclkProbe);
		os.writeInt(dataProbe);
		os.writeBoolean(isNegEdge);
		os.writeBoolean(isMSBFirst);
	}
}
