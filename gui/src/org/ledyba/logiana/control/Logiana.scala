/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */

package org.ledyba.logiana.control
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import scala.collection.mutable.ArrayBuffer
import com.sun.jna.ptr.PointerByReference
import org.ledyba.logiana.model.WaveData

object Logiana {
	var instance:DynamicLib = null;
	def apply(fname:String, force:Boolean=false):DynamicLib = {
		if(instance == null){
			instance = new DynamicLib(fname);
		}else if(force) {
			instance.lib.dispose();
			instance = new DynamicLib(fname);
		}
		return instance;
	}
	sealed class DynamicLib(val dynlib:String) {
		val lib = NativeLibrary.getInstance(dynlib);
		val find_ = lib.getFunction("findLogiana");
		val isMeasureing_ = lib.getFunction("nowLogianaMeasuring");
		val startMeasuring_ = lib.getFunction("startMeasuring");
		val endMeasuring_ = lib.getFunction("endMeasuring");
		val delete_ = lib.getFunction("closeLogiana");
	
		def withLogiana[X](lamb : (Logiana.Handle => Either[String,X]) ):Either[String, X] = {
			find() match {
				case Left(msg) => return Left(msg);
				case Right(hnd) => {
					try {
						return lamb(hnd);
					} finally {
						close(hnd.ptr);
					}
				}
			}
		}
		private def find():Either[String, Logiana.Handle] = {
			val args = ArrayBuffer[Object]();
			val ptr = new PointerByReference();
			val messageBuffer = new Array[Byte](8192);
			
			args += ptr;
			args += messageBuffer;
			args += (messageBuffer.length :java.lang.Integer);
			val result:Int = find_.invokeInt(args.toArray[Object]);
			if( result == 0 ) {
				return Right(new Handle(this, ptr.getValue()));
			}else{
				return Left(new String(messageBuffer));
			}
		}
		def startMeasuring(ptr : Pointer, sess:Session) : Either[String, Boolean] = {
			val codes = ArrayBuffer(sess.freq.code, sess.measureType.code, sess.cond.code, sess.line.code).map(x => new java.lang.Byte(x)).toArray[Object];
	
			val args = ArrayBuffer[Object]();
			val messageBuffer = new Array[Byte](8192);
			
			args += ptr;
			args += messageBuffer;
			args += (messageBuffer.length :java.lang.Integer);
			args ++= codes;
	
			val ret = startMeasuring_.invokeInt(args.toArray[Object]);
			if( ret == 0 ){
				return Right(true);
			}else{
				return Left(new String(messageBuffer));
			}
		}
		def endMeasuring(ptr : Pointer, sess:Session) : Either[String, WaveData] = {
			val args = ArrayBuffer[Object]();
			val messageBuffer = new Array[Byte](8192);
			val ramBuffer = new Array[Int](1024*32);
			
			args += ptr;
			args += messageBuffer;
			args += (messageBuffer.length :java.lang.Integer);
			args += ramBuffer;
			args += (ramBuffer.length :java.lang.Integer);
	
			val ret = endMeasuring_.invokeInt(args.toArray[Object]);
			if( ret >= 0 ){
				return Right(new WaveData(sess, ramBuffer));
			}else{
				return Left(new String(messageBuffer));
			}
		}
		def isMeasureing(ptr : Pointer) : Either[String, Boolean] = {
			val args = ArrayBuffer[Object]();
			val messageBuffer = new Array[Byte](8192);
			
			args += ptr;
			args += messageBuffer;
			args += (messageBuffer.length :java.lang.Integer);
	
			val ret = this.isMeasureing_.invokeInt(args.toArray[Object]);
			if( ret >= 0 ){
				return Right(ret != 0);
			}else{
				return Left(new String(messageBuffer));
			}
		}
		def close(ptr:Pointer) = delete_.invokeVoid(Array(ptr))
	}
	sealed class Handle(val parent:DynamicLib, val ptr : Pointer) {
		def isMeasureing() = parent.isMeasureing(ptr);
		def start(sess:Session) = parent.startMeasuring(ptr, sess);
		def end(sess:Session) = parent.endMeasuring(ptr, sess);
	}
}