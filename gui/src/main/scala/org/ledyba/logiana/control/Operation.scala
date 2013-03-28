/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */

package org.ledyba.logiana.control
import org.ledyba.logiana.control._
import org.ledyba.logiana.model.MeasuredData
import javax.swing.SwingUtilities
import org.ledyba.logiana.Config
import java.io.DataOutputStream
import java.io.DataInputStream

case class Operation(val freq : Frequency.Value, val measureType:MeasureType.Value, val cond:Condition.Value, val line:TriggerLine.Value) extends Serializable{
	def write(os:DataOutputStream){
		os.writeByte(freq.code);
		os.writeByte(measureType.code);
		os.writeByte(cond.code);
		os.writeByte(line.code);
	}
}

object Operation {
	def apply(is:DataInputStream):Operation = {
		val freq = Frequency.fromCode(is.readByte());
		val mesType = MeasureType.fromCode(is.readByte());
		val cond = Condition.fromCode(is.readByte());
		val line = TriggerLine.fromCode(is.readByte());
		return Operation(freq, mesType, cond, line);
	}
}

class OperationRunner(val conf:Config, val op:Operation, val callback:(Either[String, MeasuredData]=>Unit)) extends Thread{
	var exit = false;
	private def waitLoop(hnd : Logiana.Handle):Either[String,Unit] = {
		println("running...");
		if(OperationRunner.this.exit) {
			return Left("強制的に終了させられました");;
		}else{
			hnd.isMeasureing match {
				case Right(true) => {
					Thread.sleep(500);
					waitLoop(hnd);
				}
				case Right(false) => Right()
				case Left(msg) => Left(msg)
			}
		}
	}
	
	def sendExit() = {
		OperationRunner.this.exit = true;
	}

	def measure(hnd : Logiana.Handle):Either[String, MeasuredData] = for(
			_ <- hnd.start(op).right;
			_ <- waitLoop(hnd).right;
			dat <- hnd.end(op).right
		) yield dat;

	override def run() {
		try {
			val r = Logiana(conf.dynamic).withLogiana((hnd:Logiana.Handle) => OperationRunner.this.measure(hnd))
			SwingUtilities.invokeLater(
				new Runnable(){
					override def run(){
						callback(r);
					}
				}
			);
		} catch {
		case e:Throwable => {
				SwingUtilities.invokeLater(new Runnable(){
					override def run(){
						callback(Left(e.getMessage()));
					}
				}
			);
			}
		}
	}
}