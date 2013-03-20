package org.ledyba.logiana.model

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

class WaveViewer() extends Serializable {
	var dotsPerNanoSec:Float = 0.1f;
	var data:WaveData = new WaveData();
	var signals:Buffer[Signal] = new ListBuffer[Signal]();
	for( i<-Range(0,32) ) {
		signals += new LineSignal(this, "Probe: %02d".format(i), i);
	}

	def write(fname:String) = {
		val st = new ObjectOutputStream(new FileOutputStream(fname));
		try {
			st.writeObject(this);
		} finally {
			st.close();
		}
	}
}

object WaveViewer {
	def apply(fname:String):WaveViewer = {
		val f = new java.io.File(fname);
		if(f.exists && f.isFile) {
			val st = new ObjectInputStream(new FileInputStream(fname));
			try {
				return st.readObject().asInstanceOf[WaveViewer];
			} finally {
				st.close;
			}
		}else{
			return new WaveViewer();
		}
	}
}
