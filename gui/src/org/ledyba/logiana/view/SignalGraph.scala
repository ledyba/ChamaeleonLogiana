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

class SignalGraph(val parent:WaveGraph, val signal:Signal) extends Panel with PopupMenuContainer {
	border = LineBorder.createBlackLineBorder();
	override def paintComponent(g:Graphics2D) {
		//super.paintComponent(g);
		val rect = peer.getVisibleRect().clone().asInstanceOf[Rectangle];
		g.setColor(Color.WHITE);
		g.fill(rect);
		//FIXME: 一応範囲を広げて対処
		rect.x = Math.max(0, rect.x-50);
		rect.width = Math.min(size.width-rect.x, rect.width+100);
		val startSecOff = (rect.x/signal.parent.dotsPerNanoSec);
		val startIdx = (startSecOff/signal.parent.data.nanosecPerEntry).floor.intValue;
		val endSecOff = ((rect.x+rect.width)/signal.parent.dotsPerNanoSec);
		val endIdx = (endSecOff/signal.parent.data.nanosecPerEntry).ceil.intValue;
		signal match {
			case lsig:LineSignal => {
				g.setColor(Color.RED);
				g.setStroke(new BasicStroke(3));
				var lastX:Int=(-1);
				var lastY:Int=(-1);
				for( i <- Range(startIdx, endIdx-1) ) {
					val sig = lsig.fromWaveData(signal.parent.data, (i*signal.parent.data.nanosecPerEntry)+SignalGraph.this.signal.parent.data.beginTime);
					val x = (i*signal.parent.data.nanosecPerEntry*signal.parent.dotsPerNanoSec).intValue;
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
					val sig = vsig.fromWaveData(SignalGraph.this.signal.parent.data, (i*SignalGraph.this.signal.parent.data.nanosecPerEntry)+SignalGraph.this.signal.parent.data.beginTime);
					val x = (i*SignalGraph.this.signal.parent.data.nanosecPerEntry*SignalGraph.this.signal.parent.dotsPerNanoSec).intValue;
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
		val psize = new Dimension((SignalGraph.this.signal.parent.data.timeLength*SignalGraph.this.signal.parent.dotsPerNanoSec).toInt, 30);
		this.preferredSize = psize
		this.minimumSize = psize;
		this.maximumSize = psize;
	}
	this.update;
	val edit = {x:Signal=>
		x match {
				case i:LineSignal => (new LineSignalDialog(i)).open
				case i:ValueSignal => (new ValueSignalDialog(i)).open
		}};
	popupMenu.contents += new MenuItem(Action("edit"){
		edit(signal);
	});
	popupMenu.contents += new MenuItem(Action("delete"){
		SignalGraph.this.parent.delGraphLast(signal);
	});
	popupMenu.contents += new MenuItem(Action("add new line signal"){
		val newsig = LineSignal(SignalGraph.this.signal.parent, "new", 0);
		(new LineSignalDialog(newsig)).open
	});
	popupMenu.contents += new MenuItem(Action("add new value signal"){
			val newsig = ValueSignal(SignalGraph.this.signal.parent, "new", (0 to 7).toArray.map(i=>(i,false)));
			(new ValueSignalDialog(newsig)).open
	});
	popupMenu.contents += new MenuItem(Action("clear"){
		parent.signals=ListBuffer();
	});
	peer.addMouseListener( new MouseAdapter {
		override def mouseClicked(e: MouseEvent) {
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount % 2 == 0) {
				edit(signal);
			}
		}
	});
}