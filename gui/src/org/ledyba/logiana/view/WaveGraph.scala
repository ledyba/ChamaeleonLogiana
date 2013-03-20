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

class WaveGraph(val fname:String) extends GridBagPanel with PopupMenuContainer {
	private val view = WaveViewer(fname);
	def data = Unit;
	def data_=(newdata : WaveData):Unit = {
		this.view.data = newdata;
		this.updateData
	}
	def save() {
		view.write(fname);
	}
	def downscale() = {
		view.dotsPerNanoSec /= 2;
		this.updateData
		this.revalidate();
	}
	def upscale() = {
		view.dotsPerNanoSec *= 2;
		this.updateData
		this.revalidate();
	}
	def signals = this.view.signals;
	def signals_= (newsignals : Buffer[Signal]) {
		this.view.signals = newsignals;
		updateSignals;
		this.revalidate;
	}
	val signalGraphs = ArrayBuffer[(Label, SignalGraph)]();
	private def updateSignals {
		this.peer.removeAll();
		this.signalGraphs.clear
		var i=0;
		for(signal <- view.signals) {
			val l = new Label(signal.name);
			val c = new SignalGraph(this, signal);
			this.add(l, (0,i))
			this.add(c, (1,i))
			signalGraphs += ((l,c));
			i+=1;
		}
	}
	private def updateData {
		for((_, sigGraph) <- this.signalGraphs) {
			sigGraph.update
		}
		this.peer.revalidate();
	}
	def addGraphLast(x:Signal) = {
		val i = this.signalGraphs.length;
		val l = new Label(x.name);
		val c = new SignalGraph(this, x);
		this.add(l, (0,i));
		this.add(c, (1,i))
		this.view.signals+=x;
		signalGraphs += ((l,c));
		this.revalidate;
		this.peer.getParent().revalidate();
	}
	def updateGraphLast(x:Signal) = {
		for( edit <- this.signalGraphs.filter({ it=>val (l,c)=it;c.signal == x })){
			val (l,c) = edit;
			l.text=c.signal.name;
		}
		this.revalidate;
		this.peer.getParent().revalidate();
	}
	def delGraphLast(x:Signal) = {
		val del = this.signalGraphs.filter({ it=>val (l,c)=it;c.signal == x })
		for( d <- del ) {
			val (l,c) = d;
			this.view.signals-=c.signal;
			signalGraphs -= d;
			this.peer.remove(l.peer);
			this.peer.remove(c.peer);
		}
		this.revalidate;
		this.peer.getParent().revalidate();
	}
	popupMenu.contents += new MenuItem(Action("add new line signal"){
		val sig = LineSignal(view, "new", 0);
		WaveGraph.this.addGraphLast(sig)
		new LineSignalDialog(sig).open({sig=>WaveGraph.this.updateGraphLast(sig)});
	});
	popupMenu.contents += new MenuItem(Action("add new value signal"){
		val sig = ValueSignal(view, "new", (0 to 7).toArray.map(i=>(i,false)));
		WaveGraph.this.addGraphLast(sig);
		new ValueSignalDialog(sig).open({sig=>WaveGraph.this.updateGraphLast(sig)});
	});
	popupMenu.contents += new MenuItem(Action("clear"){
		signals=ListBuffer();
		WaveGraph.this.revalidate;
	});
	updateSignals;updateData;
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
				(x.toInt, true)
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
