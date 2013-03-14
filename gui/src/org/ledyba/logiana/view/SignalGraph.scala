package org.ledyba.logiana.view

import scala.swing.Panel
import java.awt.Graphics2D
import org.ledyba.logiana.model.Signal
import org.ledyba.logiana.model.WaveViewer
import java.awt.Color
import java.awt.Dimension
import javax.swing.border.LineBorder
import org.ledyba.logiana.model.LineSignal
import org.ledyba.logiana.model.ValueSignal
import java.awt.BasicStroke
import java.awt.Rectangle

class SignalGraph(_view : WaveViewer, _signal:Signal) extends Panel {
	val signal = _signal;
	val view = _view;
	border = LineBorder.createBlackLineBorder();
	override def paintComponent(g:Graphics2D) {
		//super.paintComponent(g);
		val rect = peer.getVisibleRect().clone().asInstanceOf[Rectangle];
		g.setColor(Color.WHITE);
		g.fill(rect);
		//FIXME: 一応範囲を広げて対処
		rect.x = Math.max(0, rect.x-50);
		rect.width = Math.min(size.width-rect.x, rect.width+100);
		val startSecOff = (rect.x/this.view.dotsPerNanoSec);
		val startIdx = (startSecOff/this.view.data.nanosecPerEntry).floor.intValue;
		val endSecOff = ((rect.x+rect.width)/this.view.dotsPerNanoSec);
		val endIdx = (endSecOff/this.view.data.nanosecPerEntry).ceil.intValue;
		_signal match {
			case lsig:LineSignal => {
				g.setColor(Color.RED);
				g.setStroke(new BasicStroke(3));
				var lastX:Int=(-1);
				var lastY:Int=(-1);
				for( i <- Range(startIdx, endIdx-1) ) {
					val sig = lsig.fromWaveData(view.data, (i*view.data.nanosecPerEntry)+view.data.beginTime);
					val x = (i*view.data.nanosecPerEntry*view.dotsPerNanoSec).intValue;
					val y = if(sig) 5 else 25;
					if(lastX >= 0 && lastY >= 0) {
						g.drawLine(lastX, lastY, x, y);
					}
					lastX=x;
					lastY=y;
				}
			}
			case vsig:ValueSignal => {
				g.setStroke(new BasicStroke(3));
				var first=startIdx == 0;
				var lastX:Int = 0;
				var lastSig:Int = 0;
				var lastChangedX = 0;
				for( i <- Range(startIdx, endIdx) ) {
					val sig = vsig.fromWaveData(view.data, (i*view.data.nanosecPerEntry)+view.data.beginTime);
					val x = (i*view.data.nanosecPerEntry*view.dotsPerNanoSec).intValue;
					if(first){
						g.setColor(Color.BLACK);
						g.drawString("%04x".format(sig), x+3f,  20f);
						lastChangedX = x;
					}else if(lastSig != sig) {
						g.setColor(Color.RED);
						g.drawLine(lastX, 5, x, 25);
						g.drawLine(lastX, 25, x, 5);
						g.setColor(Color.BLACK);
						g.drawString("%04x".format(sig), lastChangedX+3f,  20f);
						lastChangedX = x;
					}else if(lastX >= 0){
						g.setColor(Color.GREEN);
						g.drawLine(lastX, 5, x, 5);
						g.drawLine(lastX, 25, x, 25);
					}
					first=false;
					lastSig = sig;
					lastX=x;
				}
			}
		}
	}
	def update() = {
		val psize = new Dimension((this.view.data.timeLength*this.view.dotsPerNanoSec).toInt, 30);
		this.preferredSize = psize
		this.minimumSize = psize;
		this.maximumSize = psize;
	}
	this.update;
}