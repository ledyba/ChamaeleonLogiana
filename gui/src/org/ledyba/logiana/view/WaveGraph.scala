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

class WaveGraph extends GridBagPanel with PopupMenuContainer {
	val view = new WaveViewer();
	def data = Unit;
	def data_=(newdata : WaveData):Unit = {
		this.view.data = newdata;
		this.updateData
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
	private def addGraphLast(x:Signal) = {
		val i = this.signalGraphs.length;
		val l = new Label(x.name);
		val c = new SignalGraph(this, x);
		this.add(l, (0,i));
		this.add(c, (1,i))
		signalGraphs += ((l,c));
		this.revalidate;
	}
	def delGraphLast(x:Signal) = {
		val del = this.signalGraphs.filter({ x=>val (l,c)=x;c.signal == x })
		for( d <- del ) {
			val (l,c) = d;
			signalGraphs -= d;
			this.peer.remove(l.peer);
			this.peer.remove(c.peer);
		}
		this.revalidate;
	}
	popupMenu.contents += new MenuItem(Action("add new line signal"){
		(new LineSignalDialog(LineSignal(view, "new", 0))).open({sig =>
			WaveGraph.this.addGraphLast(sig);
		});
	});
	popupMenu.contents += new MenuItem(Action("add new value signal"){
			val newsig = ValueSignal(view, "new", (0 to 7).toArray.map(i=>(i,false)));
			(new ValueSignalDialog(newsig)).open({sig =>
			WaveGraph.this.addGraphLast(sig);
		});
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
	var onExit:LineSignal => Unit = null;
	override def closeOperation() {
		sig.name = nameField.text;
		sig.probeNo = sigField.selection.index;
		if(onExit != null){
			this.onExit(sig);
		}
		super.closeOperation();
	}
	minimumSize = new Dimension(256,0);
	preferredSize = new Dimension(256 ,0);
	override def open(){
		this.peer.setLocationByPlatform(true);
		super.open();
	}
	def open(x:LineSignal => Unit) = {
		this.onExit = x;
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
	var onExit:ValueSignal => Unit = null;
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
		if(onExit != null){
			this.onExit(sig);
		}
		super.closeOperation();
	}
	minimumSize = new Dimension(256,0);
	preferredSize = new Dimension(256 ,0);
	override def open(){
		this.peer.setLocationByPlatform(true);
		super.open();
	}
	def open(x:ValueSignal => Unit) = {
		this.onExit = x;
		super.open();
	}
}
