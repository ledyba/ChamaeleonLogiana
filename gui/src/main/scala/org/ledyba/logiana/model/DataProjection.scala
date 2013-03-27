package org.ledyba.logiana.model

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

class DataProjection() extends Serializable {
	var dotsPerNanoSec:Float = 0.1f;
	var data:MeasuredData = new MeasuredData();
	var signals:Buffer[Signal] = new ListBuffer[Signal]();
	// default signals
	for( i<-Range(0,32)) yield {
	  signals += new LineSignal(DataProjection.this, "Probe: %02d".format(i), i);
	}
	final def write(fname:String) = {
		val st = new ObjectOutputStream(new FileOutputStream(fname));
		try {
			st.writeObject(DataProjection.this);
		} finally {
			st.close();
		}
	}
}

object DataProjection {
	final def apply(fname:String):DataProjection = {
		val f = new java.io.File(fname);
		if(f.exists && f.isFile) {
					val st = new ObjectInputStream(new FileInputStream(fname));
					try {
						return st.readObject().asInstanceOf[DataProjection];
					} finally {
						st.close;
					}
				}else{
			return new DataProjection();
		}
	}
}
