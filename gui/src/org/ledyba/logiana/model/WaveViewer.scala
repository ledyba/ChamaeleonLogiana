package org.ledyba.logiana.model

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Buffer

class WaveViewer() extends Serializable {
	var dotsPerNanoSec:Float = 0.1f;
	var data:WaveData = new WaveData();
	var signals:Buffer[Signal] = new ListBuffer[Signal]();
	for( i<-Range(0,32) ) {
		signals += new LineSignal(this, "Probe: %02d".format(i), i);
	}
}
