package org.ledyba.logiana.view

import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Rectangle
import java.awt.event.MouseEvent
import scala.Array.canBuildFrom
import scala.collection.mutable.ListBuffer
import scala.swing.Action
import scala.swing.Dialog
import scala.swing.Graphics2D
import scala.swing.GridBagPanel
import scala.swing.Label
import scala.swing.MenuItem
import scala.swing.Panel
import scala.swing.TextField
import scala.swing.event.ButtonClicked
import scala.swing.event.MouseClicked
import scala.swing.event.MouseDragged
import scala.swing.event.MousePressed
import org.ledyba.logiana.model.LineSignal
import org.ledyba.logiana.model.Signal
import org.ledyba.logiana.model.ValueSignal
import org.ledyba.logiana.model.WaveData
import org.ledyba.logiana.model.WaveViewer
import scala.swing.ComboBox
import scala.swing.event.InputEvent
import java.awt.event.InputEvent

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
		{
			val g2=g.create().asInstanceOf[Graphics2D];
			try {
				var idx = 0;
				for( gr <-graphs ) {
					// draw label
					g2.setFont(font);
					val labelHeight = metrics.getStringBounds(gr.signal.name, g).getBounds2D().getY();
					g2.setColor(Color.BLACK);
					g2.drawString(gr.signal.name, (-labelWidth+2).toFloat, ((SignalGraph.kSignalViewHeight-labelHeight)/2).toFloat);
					// draw signal
					gr.paintComponent(g2, rect.intersection(vrect));
					if( lastSignal == idx ) {
						g2.setColor(Color.BLACK);
						g2.draw3DRect(-labelWidth, 0, preferredSize.width, SignalGraph.kSignalViewHeight, true);
					}
					rect.y -= SignalGraph.kSignalViewHeight+2;
					g2.translate(0, SignalGraph.kSignalViewHeight+2);
					idx+=1;
				}
			} finally {
				g2.dispose();
			}
		}
		def norm(acc:Int, t:Float):Int = {
			if (acc >= t)
				return acc;
			else
				return norm(acc*2, t)
		}
		val dotsPerNsec = view.dotsPerNanoSec;
		val nsecPerLine = (100/dotsPerNsec).ceil
		val begin = this.view.data.beginTime+(rect.x/dotsPerNsec);
		val end = Math.min(this.view.data.endTime, this.view.data.beginTime+((rect.x+rect.width)/dotsPerNsec));
		val offset = -(dotsPerNsec * this.view.data.beginTime)
		g.setColor(Color.darkGray);
		for( l <- ( (begin/nsecPerLine).toInt to (end/nsecPerLine).toInt ) ) {
			val t = l * nsecPerLine;
			val x = (offset + (t * dotsPerNsec)).toInt;
			g.drawLine(x, 0, x, preferredSize.height);
			g.drawString("%.1fnsec".format(t.toFloat), x, 10);
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
		if(lastSignal >= 0){
			edit(this.view.signals(lastSignal));
		}
	});
	popupMenu.contents += new MenuItem("delete"){
		reactions += {
			case ButtonClicked(source) => {
				if(lastSignal != null){
					delSignal(view.signals(lastSignal));
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
	this.listenTo(this.mouse.moves);
	var lastSignal:Int = -1;
	this.reactions += {
		case MouseClicked(source, point, modifiers, clicks, triggersPopup) => {
			val lastSelected = (point.y/(SignalGraph.kSignalViewHeight+2)).intValue();
			if(lastSelected >= 0 && lastSelected < graphs.length) {
				if(clicks % 2 == 0){
					edit(this.view.signals(lastSelected));
				}else{
					this.lastSignal = lastSelected;
					updateView;
				}
			}
		}
		case ev:MousePressed => {
			val now = (ev.point.y/(SignalGraph.kSignalViewHeight+2)).intValue();
			if(now >= 0 && now < graphs.length) {
				this.lastSignal = now;
				updateView;
			}else{
				lastSignal = -1;
			}
		}
		case ev:MouseDragged => {
			if((ev.peer.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK){
				val now = Math.min((ev.point.y/(SignalGraph.kSignalViewHeight+2)).intValue(), graphs.length-1);
				if(now >= 0 && now != lastSignal) {
					swapSignal(lastSignal, now);
					lastSignal = now;
				}
			}
		}
	}
	def swapSignal(a:Int, b:Int){
		val origSig = view.signals.clone;
		val origGraph = graphs.clone;
		view.signals.clear;
		graphs.clear;
		for( i<- (0 to origSig.length-1) ) {
			if(a == i){
				view.signals += origSig(b);
				graphs += origGraph(b);
			}else if(b == i){
				view.signals += origSig(a);
				graphs += origGraph(a);
			}else{
				view.signals += origSig(i);
				graphs += origGraph(i);
			}
		}
		updateView
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
