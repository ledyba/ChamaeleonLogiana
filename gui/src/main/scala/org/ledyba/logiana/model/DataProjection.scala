package org.ledyba.logiana.model

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer
import java.io.DataInputStream
import java.io.DataOutputStream

case class DataProjection(var dotsPerNanoSec:Float, var data:MeasuredData, var signals:Buffer[Signal]) extends Serializable {
	def this()={
		this(0.1f, new MeasuredData(), ListBuffer[Signal]());
		// default signals
		for( i<-Range(0,32)) yield {
			signals += new LineSignal(DataProjection.this, "Probe: %02d".format(i), i);
		}
	}
	final def write(fname:String):Unit = {
		val st = new DataOutputStream(new FileOutputStream(fname));
		try {
			write(st);
		} finally {
			st.close();
		}
	}
	final def write(os:DataOutputStream):Unit = {
		os.writeFloat(dotsPerNanoSec);
		data.write(os);
		os.writeInt(signals.length);
		for(sig <- signals){
			sig.write(os);
		}
	}
}

object DataProjection {
	final def apply(is:DataInputStream):DataProjection ={
		val d = new DataProjection();
		d.dotsPerNanoSec = is.readFloat();
		d.data = MeasuredData(is);
		d.signals = ListBuffer[Signal]();
		val len = is.readInt();
		for( _ <- (1 to len) ){
			d.signals += Signal(d, is);
		}
		return d;
	}
	final def apply(fname:String):DataProjection = {
		val f = new java.io.File(fname);
		if(f.exists && f.isFile) {
			val fis = new FileInputStream(fname);
			try {
				return apply(new DataInputStream(fis));
			} finally {
				fis.close();
			}
		}else{
			return new DataProjection();
		}
	}
}
