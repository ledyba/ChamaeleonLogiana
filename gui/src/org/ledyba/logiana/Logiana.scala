/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */

package org.ledyba.logiana
import com.sun.jna
import com.sun.jna.NativeLibrary
import org.ledyba.logiana.control.Session
import com.sun.jna.Pointer
import org.ledyba.logiana.control.Session

object Logiana {
	val lib = NativeLibrary.getInstance("D:\\Dropbox\\src\\logiana\\driver\\build\\Logiana.dll");
	val find_ = lib.getFunction("findLogiana");
	val isMeasureing_ = lib.getFunction("nowLogianaMeasuring");
	val startMeasuring_ = lib.getFunction("startMeasuring");
	val delete_ = lib.getFunction("closeLogiana");
	
	class Handle(raw : Pointer) {
		val ptr = raw;
		def isMeasureing() = Logiana.isMeasureing(this)
		def close() = Logiana.close(this)
	}
	
	def find() = new Handle(find_.invokePointer(Array()));
	def startMeasuring(hnd : Handle, sess:Session) : Either[String, Boolean] = {
		val codes = List(sess.freqCode, sess.measureTypeCode, sess.condCode, sess.lineCode).map(x => new java.lang.Byte(x)).toArray[Object];
		val ret = startMeasuring_.invokeInt(Array[Object](hnd.ptr)++codes);
		if( ret == 0 ){
			return Right(true);
		}else{
			return Left("Error: "+ret);
		}
	}
	def isMeasureing(hnd : Handle) : Either[String, Boolean] = {
		val ret = this.isMeasureing_.invokeInt(Array[Object](hnd.ptr));
		if( ret == 0 ){
			return Right(true);
		}else{
			return Left("Error: "+ret);
		}
	}
	def close(hnd : Handle) = delete_.invokeVoid(Array(hnd.ptr))
}
