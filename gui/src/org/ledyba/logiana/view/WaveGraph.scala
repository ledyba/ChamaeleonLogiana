package org.ledyba.logiana.view

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Buffer
import scala.swing.GridBagPanel
import scala.swing.Label
import org.ledyba.logiana.model.Signal
import org.ledyba.logiana.model.WaveData
import org.ledyba.logiana.model.WaveViewer
import scala.swing.MenuItem
import scala.swing.Action
import org.ledyba.logiana.model.LineSignal
import org.ledyba.logiana.model.ValueSignal
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities
import scala.collection.mutable.ListBuffer
import scala.swing.Dialog
import scala.swing.TextField
import scala.swing.ComboBox
import java.awt.Dimension
import org.ledyba.logiana.model.LineSignal
import scala.swing.Component
import scala.swing.event.ButtonClicked
import scala.swing.event.MouseClicked
import scala.swing.Graphics2D
import java.awt.Rectangle
import scala.swing.Panel
import java.awt.Color
import java.awt.BasicStroke
import scala.swing.event.MousePressed
import scala.swing.event.MouseEvent
import java.awt.Font
import java.awt.FontMetrics

class WaveGraph(val fname:String) extends Panel with PopupMenuContainer {
	private val view = WaveViewer(fname);
	private val graphs = ListBuffer[SignalGraph]();
	private var labelWidth = 30;
	private var graphWidth = 0;
	font = new Font("SansSerif", Font.PLAIN, 12);
	private val metrics = peer.getFontMetrics(font);
	def data = Unit;
	def data_=(newdata : WaveData):Unit = {
		this.view.data = newdata;
	}
	def save() {
		view.write(fname);
	}
	def downscale() = {
		view.dotsPerNanoSec /= 2;
		updateView;
	}
	def upscale() = {
		view.dotsPerNanoSec *= 2;
		updateView;
	}
	override def paintComponent(g:Graphics2D) = {
		super.paintComponent(g);
		g.translate(labelWidth, 0);
		val rect = peer.getVisibleRect().clone().asInstanceOf[Rectangle];
		rect.x = Math.max(0, rect.x-labelWidth-50);
		rect.width = Math.min(size.width-labelWidth, rect.width+100);
		val vrect = new Rectangle(0,0,graphWidth, SignalGraph.kSignalViewHeight);
		val lineEnd = Math.min(graphWidth, rect.x+rect.width);
		for( gr <-graphs ) {
			// draw label
			g.setFont(font);
			val labelHeight = metrics.getStringBounds(gr.signal.name, g).getBounds2D().getY();
			g.drawString(gr.signal.name, (-labelWidth+2).toFloat, ((SignalGraph.kSignalViewHeight-labelHeight)/2).toFloat);
			// draw signal
			val inter=rect.intersection(vrect);
			gr.paintComponent(g, inter);
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(2));
			g.drawLine(rect.x, SignalGraph.kSignalViewHeight, lineEnd, SignalGraph.kSignalViewHeight);
			rect.y -= SignalGraph.kSignalViewHeight+2;
			g.translate(0, SignalGraph.kSignalViewHeight+2);
		}
	}
	def updateView = {
		labelWidth = view.signals.foldLeft(0)({(now, nextsig)=> Math.max(now, metrics.stringWidth(nextsig.name))})+3;
		graphWidth = (this.view.data.nanosecPerEntry*this.view.data.length*this.view.dotsPerNanoSec).toInt;
		val width=labelWidth + graphWidth;
		val height = ((SignalGraph.kSignalViewHeight+2) * this.graphs.length);
		preferredSize = new Dimension( width, height );
		minimumSize = new Dimension( width, height );
		maximumSize = new Dimension( width, height );
		this.revalidate
		this.repaint
	}
	def addSignal(sig:Signal) = {
		this.view.signals+=sig;
		this.graphs+=new SignalGraph(this, sig);
		updateView;
	}
	def updateSignal(sig:Signal) = {
		updateView;
	}
	def delSignal(sig:Signal) = {
		val d = this.graphs.filter({ it=>it.signal == sig });
		this.graphs --= d;
		this.view.signals --= d.map({it => it.signal});
		updateView;
	}
	def clearSignals = {
		this.view.signals.clear;
		this.graphs.clear;
		updateView;
	}
	val edit = {x:Signal=>
		x match {
				case i:LineSignal => (new LineSignalDialog(i)).open({sig=>WaveGraph.this.updateSignal(sig)})
				case i:ValueSignal => (new ValueSignalDialog(i)).open({sig=>WaveGraph.this.updateSignal(sig)})
		}};
	popupMenu.contents += new MenuItem(Action("edit"){
		if(lastSignal != null){
			edit(lastSignal);
		}
	});
	popupMenu.contents += new MenuItem("delete"){
		reactions += {
			case ButtonClicked(source) => {
				if(lastSignal != null){
					delSignal(lastSignal);
				}
			}
		}
	};
	popupMenu.contents += new MenuItem(Action("add new line signal"){
		val sig = LineSignal(view, "new", 0);
		WaveGraph.this.addSignal(sig);
		new LineSignalDialog(sig).open({sig=>WaveGraph.this.updateSignal(sig)});
	});
	popupMenu.contents += new MenuItem(Action("add new value signal"){
		val sig = ValueSignal(view, "new", (0 to 7).toArray.map(i=>(i,false)));
		WaveGraph.this.addSignal(sig);
		new ValueSignalDialog(sig).open({sig=>WaveGraph.this.updateSignal(sig)});
	});
	popupMenu.contents += new MenuItem(Action("clear"){
		WaveGraph.this.clearSignals;
		WaveGraph.this.revalidate;
	});
	this.listenTo(this.mouse.clicks);
	var lastSignal:Signal = null;
	this.reactions += {
		case MouseClicked(source, point, modifiers, clicks, triggersPopup) => {
			val lastSelected = (point.y/(SignalGraph.kSignalViewHeight+2)).intValue();
			if(lastSelected >= 0 && lastSelected < graphs.length && clicks % 2 == 0) {
				edit(this.view.signals(lastSelected));
			}
		}
		case ev:MousePressed => {
			if(ev.peer.getButton() == MouseEvent.BUTTON3){
				val lastSelected = (ev.point.y/(SignalGraph.kSignalViewHeight+2)).intValue();
				if(lastSelected >= 0 && lastSelected < graphs.length) {
					lastSignal = this.view.signals(lastSelected);
				}else{
					lastSignal = null;
				}
			}
		}
	}
	for(sig <- this.view.signals){
		this.graphs+=new SignalGraph(this, sig);
	}
	updateView;
}

sealed class LineSignalDialog(val sig:LineSignal) extends Dialog{
	title="ラインシグナル";
	modal=true;
	val nameField=new TextField(sig.name);
	val sigField=new ComboBox(0 to 31){selection.index=sig.probeNo;};
	contents = new GridBagPanel(){
		add(new Label("名前："), new Constraints(){gridx=0;gridy=0;});
		add(nameField, new Constraints(){gridx=1;gridy=0;weightx=1;fill=GridBagPanel.Fill.Horizontal;})
		add(new Label("プローブ："), new Constraints(){gridx=0;gridy=1;});
		add(sigField, new Constraints(){gridx=1;gridy=1;weightx=1;fill=GridBagPanel.Fill.Horizontal;});
	}
	var onEnd:LineSignal=>Unit = null;
	override def closeOperation() {
		sig.name = nameField.text;
		sig.probeNo = sigField.selection.index;
		if( onEnd != null ){
			this.onEnd(sig);
		}
		super.closeOperation();
	}
	minimumSize = new Dimension(256,0);
	preferredSize = new Dimension(256 ,0);
	override def open(){
		this.peer.setLocationByPlatform(true);
		super.open();
	}
	def open(lamb:LineSignal=>Unit) = {
		this.onEnd = lamb;
		this.peer.setLocationByPlatform(true);
		super.open();
	}
}


sealed class ValueSignalDialog(val sig:ValueSignal) extends Dialog{
	title="ラインシグナル";
	modal=true;
	def sigRepr() =
		sig.lines.map( {x => val (num, isNega) = x; (if(isNega) "!" else "") ++ num.toString} ).mkString(" ");
	
	val nameField=new TextField(sig.name);
	val sigField=new TextField(sigRepr);
	contents = new GridBagPanel(){
		add(new Label("名前："), new Constraints(){gridx=0;gridy=0;});
		add(nameField, new Constraints(){gridx=1;gridy=0;weightx=1;fill=GridBagPanel.Fill.Horizontal;})
		add(new Label("プローブ："), new Constraints(){gridx=0;gridy=1;});
		add(sigField, new Constraints(){gridx=1;gridy=1;fill=GridBagPanel.Fill.Horizontal;})
	}
	var onEnd:ValueSignal=>Unit = null;
	override def closeOperation() {
		def parse(x:String)={
			if(x.startsWith("!")){
				(x.substring(1).toInt, true)
			}else{
				(x.toInt, false)
			}
		}
		sig.name = nameField.text;
		sig.lines = sigField.text.split("\\s+").map(parse)
		if( onEnd != null ){
			this.onEnd(sig);
		}
		super.closeOperation();
	}
	minimumSize = new Dimension(256,0);
	preferredSize = new Dimension(256 ,0);
	override def open(){
		this.peer.setLocationByPlatform(true);
		super.open();
	}
	def open(lamb:ValueSignal=>Unit) = {
		this.onEnd = lamb;
		this.peer.setLocationByPlatform(true);
		super.open();
	}
}
