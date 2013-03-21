package org.ledyba.logiana.model

sealed abstract class Signal(val parent:WaveViewer) extends Serializable {
	def repr:String;
	def name:String;
}

case class LineSignal(override val parent:WaveViewer, var name:String, var probeNo:Int) extends Signal(parent) with Serializable {
	def fromWaveData(wavData:WaveData, time:Double):Boolean = {
		return wavData.signalAtTime(time, probeNo);
	}
	override def repr() = "LineSignal: "++name++" (probe"+probeNo+")"
	override def toString=repr;
}

case class ValueSignal(override val parent:WaveViewer, var name:String, var lines:Array[(Int, Boolean)]) extends Signal(parent) with Serializable {
	def fromWaveData(wavData:WaveData, time:Double):Int = {
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
}
