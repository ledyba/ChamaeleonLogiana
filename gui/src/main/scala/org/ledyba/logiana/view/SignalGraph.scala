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
import scala.swing.MenuItem
import scala.swing.Action
import java.awt.event.MouseAdapter
import scala.collection.mutable.ListBuffer
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities
import java.awt.Font

object SignalGraph {
	val kSignalViewHeight=30;
}
class SignalGraph(val parent:WaveGraph, val signal:Signal) {
	val font = new Font("Monospace", Font.PLAIN, 12);
	def paintComponent(g:Graphics2D, rect:Rectangle) {
		g.setColor(Color.WHITE);
		g.fill(rect);

		val startSecOff = (rect.x/signal.parent.dotsPerNanoSec);
		val startIdx = (startSecOff/signal.parent.data.nanosecPerEntry).floor.intValue;
		val endSecOff = ((rect.x+rect.width)/signal.parent.dotsPerNanoSec);
		val endIdx = Math.min(signal.parent.data.length,(endSecOff/signal.parent.data.nanosecPerEntry).ceil.intValue+1);
		signal match {
			case lsig:LineSignal => {
				g.setColor(Color.RED);
				g.setStroke(new BasicStroke(3));
				var lastX:Int=0;
				var lastSig=false;
				for( i <- Range(startIdx, endIdx) ) {
					val sig = lsig.fromWaveData(signal.parent.data, (i*signal.parent.data.nanosecPerEntry)+SignalGraph.this.signal.parent.data.beginTime);
					val x = ((i+1)*signal.parent.data.nanosecPerEntry*signal.parent.dotsPerNanoSec).intValue;
					val y = if(sig) 5 else SignalGraph.kSignalViewHeight-5;
					if(lastSig != sig) {
						g.setColor(Color.RED);
						g.drawLine(lastX, 5, lastX, SignalGraph.kSignalViewHeight-5);
						lastSig = sig;
					}
					g.setColor(Color.GREEN);
					g.drawLine(lastX, y, x, y);
					lastX=x;
				}
			}
			case vsig:ValueSignal => {
				g.setStroke(new BasicStroke(3));
				var first=startIdx == 0;
				var lastX:Int = 0;
				var lastSig:Int = 0;
				var lastChangedX = 0;
				for( i <- Range(startIdx, endIdx) ) {
					val sig = vsig.fromWaveData(SignalGraph.this.signal.parent.data, (i*SignalGraph.this.signal.parent.data.nanosecPerEntry)+SignalGraph.this.signal.parent.data.beginTime);
					val x = ((i+1)*SignalGraph.this.signal.parent.data.nanosecPerEntry*SignalGraph.this.signal.parent.dotsPerNanoSec).intValue;
					if(first || lastSig != sig){
						g.setColor(Color.BLACK);
						g.setFont(font);
						g.drawString("%04x".format(sig), lastX+3f,  20f);
						lastChangedX = x;
						g.setColor(Color.RED);
						g.drawLine(lastX, 5, lastX, SignalGraph.kSignalViewHeight-5);
					}
					g.setColor(Color.GREEN);
					g.drawLine(lastX, 5, x, 5);
					g.drawLine(lastX, SignalGraph.kSignalViewHeight-5, x, SignalGraph.kSignalViewHeight-5);
					first=false;
					lastSig = sig;
					lastX=x;
				}
			}
		}
	}
}