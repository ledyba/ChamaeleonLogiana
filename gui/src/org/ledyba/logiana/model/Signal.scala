package org.ledyba.logiana.model

sealed abstract class Signal extends Serializable {
}

case class LineSignal(probeNo:Int) extends Signal with Serializable {
	def fromWaveData(wavData:WaveData, time:Float):Boolean = {
		return wavData.signalAtTime(time, probeNo);
	}
}

case class ValueSignal(lines:Array[(Int, Boolean)]) extends Signal with Serializable {
	def fromWaveData(wavData:WaveData, time:Float):Int = {
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
}
