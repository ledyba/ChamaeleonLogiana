package org.ledyba.logiana.model

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer
import java.io.DataInputStream
import java.io.DataOutputStream
import scala.collection.GenTraversableOnce

case class DataProjection(var dotsPerNanoSec:Float, private var _data:MeasuredData, private val _signals:Buffer[Signal]) extends Serializable {
	def this()={
		this(0.1f, new MeasuredData(), ListBuffer[Signal]());
	}
	def data:MeasuredData = this._data;
	def data_=(v:MeasuredData) = {
		this._data = v;
	}
	object signals { //TODO: BufferWrapperはどう？
		val spirit:Buffer[Signal]=_signals;
		def += (s:Signal) = {
			this.spirit += s;
			s.notifyDataChanged(_data);
		}
		def -= (s:Signal) = {
			this.spirit -= s;
		}
		def length = spirit.length;
		def apply(i:Int) = spirit(i);
		override def clone:Buffer[Signal] = spirit.clone.asInstanceOf[Buffer[Signal]]
		def foldLeft[B](z: B)(op: (B, Signal) => B):B = spirit.foldLeft(z)(op)
		def clear = spirit.clear;
		def foreach(f: (Signal) => Unit): Unit = spirit.foreach(f);
		
		def dataUpdated = {
			for(s <- spirit) {
				s.notifyDataChanged(_data);
			}
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
		val len = is.readInt();
		for( _ <- (1 to len) ){
			d._signals += Signal(d, is);
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
			val d = new DataProjection();
			for( i<-Range(0,32)) {
				d.signals += new LineSignal(d, "Probe: %02d".format(i), i);
			}
			return d;
		}
	}
}
