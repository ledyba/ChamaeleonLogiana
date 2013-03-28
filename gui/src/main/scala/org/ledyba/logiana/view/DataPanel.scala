package org.ledyba.logiana.view

import scala.swing.GridBagPanel
import scala.swing.Panel
import scala.swing.ScrollPane
import java.awt.Graphics2D
import org.ledyba.logiana.model.MeasuredData
import org.ledyba.logiana.model.DataProjection
import org.ledyba.logiana.model.Signal
import org.ledyba.logiana.model.LineSignal
import org.ledyba.logiana.model.ValueSignal
import java.awt.Font
import java.awt.Dimension
import java.awt.Color
import org.ledyba.logiana.model.MeasuredData
import scala.collection.mutable.Buffer
import java.awt.Rectangle
import java.awt.BasicStroke
import scala.swing.MenuItem
import scala.swing.Action
import scala.swing.event.ButtonClicked
import scala.swing.event.MouseClicked
import scala.swing.event.MousePressed
import scala.swing.event.MouseDragged
import java.awt.event.InputEvent
import scala.swing.Dialog
import scala.swing.TextField
import scala.swing.Label
import scala.swing.ComboBox
import javax.swing.JViewport
import java.awt.Point
import scala.swing.event.MouseWheelMoved
import scala.swing.ScrollBar
import javax.swing.SwingUtilities
import java.awt.event.AdjustmentListener
import java.awt.event.AdjustmentEvent
import java.awt.event.ComponentListener
import java.awt.event.ComponentEvent

object DataPanel {
	val kItemHeight = 30;
	val kItemMargin = 2;
}
class DataPanel(filename:String, private val scrollX:ScrollBar, private val scrollY:ScrollBar) extends GridBagPanel with PopupMenuContainer {
	
	private val labelPanel = new LabelPanel(DataPanel.this);
	private val sigPanel = new SignalPanel(DataPanel.this);
	
	var proj = DataProjection(filename);
	add(labelPanel, new Constraints{ gridx=0;gridy=0;weightx=0;weighty=1.0f;fill=GridBagPanel.Fill.Both; });
	add(sigPanel, new Constraints{ gridx=1;gridy=0;weightx=1;weighty=1.0f;fill=GridBagPanel.Fill.Both; });
	sigPanel.notifyDataChanged();
	labelPanel.notifyDataChanged();
	
	private def maxX = scrollX.peer.getMaximum()-scrollX.peer.getVisibleAmount();
	private def maxY = scrollY.peer.getMaximum()-scrollY.peer.getVisibleAmount();
	private def updateScroll = {
		val width = sigPanel.inner.preferredSize.width;
		val height = sigPanel.inner.preferredSize.height;
		val amx = size.width;
		val amy = size.height;
		scrollX.peer.setUnitIncrement(width/40);
		scrollX.peer.setBlockIncrement(width/40);
		scrollX.peer.setValues(scrollX.peer.getValue(), amx, 0, width);
		scrollY.peer.setValues(scrollY.peer.getValue(), amy, 0, height);
	};
	
	peer.addComponentListener(new ComponentListener{
		override def componentHidden(e:ComponentEvent) = Unit
		override def componentMoved(e:ComponentEvent) = Unit
		override def componentResized(e:ComponentEvent) = updateScroll
		override def componentShown(e:ComponentEvent) = updateScroll
	});
	def scrollTo(x:Int,y:Int){
		sigPanel.scrollTo(x, y);
		labelPanel.scrollTo(x, y);
	}

	def save(fname:String){
		proj.write(fname)
	}
	def load(fname:String){
		proj = DataProjection(fname);
		sigPanel.notifyDataChanged();
		labelPanel.notifyDataChanged();
		revalidate();
	}
	def updateData(data : MeasuredData) = {
		proj.data = data;
		labelPanel.notifyDataChanged();
	}
	def updateSignal(sigs : Buffer[Signal]) = {
		proj.signals = sigs;
		sigPanel.notifyDataChanged();
		labelPanel.notifyDataChanged();
		revalidate();
	}
	def swapSignal(a:Int, b:Int) = {
		val origSig = proj.signals.clone;
		proj.signals.clear;
		for( i<- (0 to origSig.length-1) ) {
			if(a == i){
				proj.signals += origSig(b);
			}else if(b == i){
				proj.signals += origSig(a);
			}else{
				proj.signals += origSig(i);
			}
		}
		sigPanel.notifyDataChanged();
		labelPanel.notifyDataChanged();
		revalidate();
	}
	def scaleDown() = {
		proj.dotsPerNanoSec /= 2;
		sigPanel.notifyDataChanged();
		updateScroll;
		scrollX.peer.setValue(scrollX.peer.getValue()/2);
	}
	def scaleUp() = {
		proj.dotsPerNanoSec *= 2;
		sigPanel.notifyDataChanged();
		updateScroll;
		scrollX.peer.setValue(scrollX.peer.getValue()*2);
	}
	def addSignal(sig:Signal) = {
		proj.signals+=sig;
		sigPanel.notifyDataChanged();
		labelPanel.notifyDataChanged();
		revalidate();
	}
	def delSignal(sig:Signal) = {
		proj.signals-=sig;
		sigPanel.notifyDataChanged();
		labelPanel.notifyDataChanged();
		revalidate();
	}
	def clearSignals = {
		proj.signals.clear;
		sigPanel.notifyDataChanged();
		labelPanel.notifyDataChanged();
		revalidate();
	}
	def updateSignal(sig:Signal) = {
		sigPanel.notifyDataChanged();
		labelPanel.notifyDataChanged();
		revalidate();
	}
	
	var selectedIdx : Int = -1;

	val edit = {x:Signal=>
		x match {
				case i:LineSignal => (new LineSignalDialog(i)).open({sig=>updateSignal(sig)})
				case i:ValueSignal => (new ValueSignalDialog(i)).open({sig=>updateSignal(sig)})
		}};
	popupMenu.contents += new MenuItem(Action("edit"){
		if(selectedIdx >= 0){
			edit(proj.signals(selectedIdx));
		}
	});
	popupMenu.contents += new MenuItem("delete"){
		reactions += {
			case ButtonClicked(source) => {
				if(selectedIdx >= 0){
					delSignal(proj.signals(selectedIdx));
				}
			}
		}
	};
	popupMenu.contents += new MenuItem(Action("add new line signal"){
		val sig = LineSignal(proj, "new", 0);
		addSignal(sig);
		new LineSignalDialog(sig).open({sig=>updateSignal(sig)});
	});
	popupMenu.contents += new MenuItem(Action("add new value signal"){
		val sig = ValueSignal(proj, "new", (0 to 7).toArray.map(i=>(i,false)));
		addSignal(sig);
		new ValueSignalDialog(sig).open({sig=>updateSignal(sig)});
	});
	popupMenu.contents += new MenuItem(Action("clear"){
		clearSignals;
		DataPanel.this.revalidate;
	});
	this.listenTo(this.mouse.clicks);
	this.listenTo(this.mouse.moves);
	this.listenTo(this.mouse.wheel);
	this.reactions += {
		case MouseClicked(source, point, modifiers, clicks, triggersPopup) => {
			val lastSelected = (point.y/(DataPanel.kItemHeight+DataPanel.kItemMargin)).intValue();
			if(lastSelected >= 0 && lastSelected < proj.signals.length) {
				if(clicks % 2 == 0){
					edit(proj.signals(lastSelected));
				}else{
					sigPanel.notifyDataChanged();
				}
			}
		}
		case ev:MousePressed => {
			val now = (ev.point.y/(DataPanel.kItemHeight+DataPanel.kItemMargin)).intValue();
			if(now >= 0 && now < proj.signals.length) {
				selectedIdx = now;
				sigPanel.notifyDataChanged();
			}else{
				selectedIdx = -1;
				sigPanel.notifyDataChanged();
			}
		}
		case ev:MouseDragged => {
			if((ev.peer.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK){
				val now = Math.min((ev.point.y/(DataPanel.kItemHeight+DataPanel.kItemMargin)).intValue(), proj.signals.length-1);
				if(now >= 0 && now != selectedIdx) {
					swapSignal(selectedIdx, now);
					selectedIdx = now;
				}
			}
		}
		case ev:MouseWheelMoved => {
			val rot = ev.rotation;
			val fact = (maxX:Long) * 50/sigPanel.inner.preferredSize.width;
			val next:Long = scrollX.peer.getValue() + rot * fact;
			scrollX.peer.setValue(Math.min(maxX, next).toInt);
		}
	}
	scrollX.peer.addAdjustmentListener(new AdjustmentListener{
		override def  adjustmentValueChanged(e:AdjustmentEvent):Unit = {
			val x = e.getValue();
			val y = scrollY.peer.getValue();
			scrollTo(x,y);
		}
	});
	scrollY.peer.addAdjustmentListener(new AdjustmentListener{
		override def  adjustmentValueChanged(e:AdjustmentEvent):Unit = {
			val x = scrollX.peer.getValue();
			val y = e.getValue();
			scrollTo(x,y);
		}
	});
}
sealed class LabelPanel(val parent:DataPanel) extends Viewport {
	private val metrics = peer.getFontMetrics(font);
	def scrollTo(x:Int, y:Int){
		peer.setViewPosition(new Point(0,y));
	}
	def notifyDataChanged(){
		val sigs = parent.proj.signals;
		val width = sigs.foldLeft(0)({(now, nextsig)=> Math.max(now, metrics.stringWidth(nextsig.name))})+3;
		val height = (DataPanel.kItemHeight+DataPanel.kItemMargin)*sigs.length - DataPanel.kItemMargin;
		preferredSize = new Dimension( width, height );
		minimumSize = new Dimension(width, 0);
		maximumSize = new Dimension(width, height);
		inner.preferredSize = preferredSize;
		inner.minimumSize = preferredSize;
		inner.maximumSize = preferredSize;
		this.revalidate
		this.repaint
	}
	val inner = new Panel {
		override def paintComponent(g:Graphics2D) = {
			super.paintComponent(g);
			var idx = 0;
			g.setStroke(new BasicStroke(2));
			for( sig <- parent.proj.signals ) {
				val name = sig.name;
				val height = metrics.getStringBounds(name, g).getBounds2D().getY();
				val off = (DataPanel.kItemHeight+DataPanel.kItemMargin)*idx;
				g.setColor(Color.BLACK);
				g.drawString(
						sig.name,
						1.5f,
						(off+(DataPanel.kItemHeight+(height/2))).toFloat);
				if(idx != 0){
					g.setColor(Color.lightGray);
					val rect = peer.getVisibleRect();
					g.drawLine(rect.x, off, rect.x+rect.width, off);
				}
				idx += 1;
			}
		}
	};
	this.peer.setView(inner.peer);
}
sealed class SignalPanel(val parent:DataPanel) extends Viewport {
	def scrollTo(x:Int, y:Int){
		peer.setViewPosition(new Point(x,y));
	}
	def notifyDataChanged(){
		val sigs = parent.proj.signals;
		val width = (parent.proj.data.nanosecPerEntry*parent.proj.data.length*parent.proj.dotsPerNanoSec).toInt
		val height = (DataPanel.kItemHeight+DataPanel.kItemMargin)*sigs.length - DataPanel.kItemMargin;
		preferredSize = new Dimension( width, height );
		minimumSize = new Dimension(0, 0);
		maximumSize = new Dimension(width, height);
		inner.preferredSize = preferredSize;
		inner.minimumSize = preferredSize;
		inner.maximumSize = preferredSize;
		this.revalidate
		this.repaint
	}
	val inner = new Panel {
		override def paintComponent(g:Graphics2D) = {
			super.paintComponent(g);
			val rect = peer.getVisibleRect().clone.asInstanceOf[java.awt.Rectangle];
			rect.x = Math.max(0, rect.x-50);
			rect.width = Math.min(size.width, rect.width+100);
			val lineEnd = Math.min(preferredSize.width, rect.x+rect.width);
			val vrect = new Rectangle(0,0,preferredSize.width, DataPanel.kItemHeight);
			val g2 = g.create().asInstanceOf[Graphics2D];
			try {
				var idx = 0;
				for(sig <- parent.proj.signals){
					renderSignal(g2, sig, vrect.intersection(rect));
					if(parent.selectedIdx == idx){
						g2.setColor(Color.BLACK);
						g2.draw3DRect(0, 0, vrect.width, vrect.height, true);
					}
					idx+=1;
					rect.y -= (DataPanel.kItemHeight+DataPanel.kItemMargin);
					g2.translate(0, DataPanel.kItemHeight+DataPanel.kItemMargin);
				}
			} finally {
				g2.dispose();
			}
			//最後のライン
			val dotsPerNsec = parent.proj.dotsPerNanoSec;
			val nsecPerLine = (100/dotsPerNsec).ceil
			val begin = parent.proj.data.beginTime+(rect.x/dotsPerNsec);
			val end = Math.min(parent.proj.data.endTime, parent.proj.data.beginTime+((rect.x+rect.width)/dotsPerNsec));
			val offset = -(dotsPerNsec * parent.proj.data.beginTime)
			g.setColor(Color.darkGray);
			for( l <- ( (begin/nsecPerLine).ceil.toInt to (end/nsecPerLine).floor.toInt ) ) {
				val t = l * nsecPerLine;
				val x = (offset + (t * dotsPerNsec)).toInt;
				g.drawLine(x, 0, x, preferredSize.height);
				g.drawString("%.1fnsec".format(t.toFloat), x+3, 10);
			}
		}
	};
	
	def renderSignal(g:Graphics2D, signal:Signal, rect:Rectangle):Unit = {
		if(rect.isEmpty()){
			return;
		}
		g.setColor(Color.WHITE);
		g.fill(rect);
		val beginTime = signal.parent.data.beginTime;
		val dotsPerNanoSec = signal.parent.dotsPerNanoSec;
		val nanosecPerEntry = signal.parent.data.nanosecPerEntry;
		
		val startSecOff = (rect.x/dotsPerNanoSec);
		val startIdx = (startSecOff/nanosecPerEntry).floor.intValue;
		val endSecOff = ((rect.x+rect.width)/dotsPerNanoSec);
		val endIdx = Math.min(signal.parent.data.length,(endSecOff/nanosecPerEntry).ceil.intValue+1);
		signal match {
			case lsig:LineSignal => {
				g.setColor(Color.RED);
				g.setStroke(new BasicStroke(3));
				var lastX:Int=0;
				var lastSig=false;
				for( i <- Range(startIdx, endIdx) ) {
					val sig = lsig.fromWaveData(signal.parent.data, (i*nanosecPerEntry)+beginTime);
					val x = ((i+1)*nanosecPerEntry*dotsPerNanoSec).intValue;
					val y = if(sig) 5 else DataPanel.kItemHeight-5;
					if(lastSig != sig) {
						g.setColor(Color.RED);
						g.drawLine(lastX, 5, lastX, DataPanel.kItemHeight-5);
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
					val sig = vsig.fromWaveData(signal.parent.data, (i*nanosecPerEntry)+beginTime);
					val x = ((i+1)*nanosecPerEntry*dotsPerNanoSec).intValue;
					if(first || lastSig != sig){
						g.setColor(Color.BLACK);
						g.setFont(font);
						g.drawString("%04x".format(sig), lastX+3f,  20f);
						lastChangedX = x;
						g.setColor(Color.RED);
						g.drawLine(lastX, 5, lastX, DataPanel.kItemHeight-5);
					}
					g.setColor(Color.GREEN);
					g.drawLine(lastX, 5, x, 5);
					g.drawLine(lastX, DataPanel.kItemHeight-5, x, DataPanel.kItemHeight-5);
					first=false;
					lastSig = sig;
					lastX=x;
				}
			}
		}
	}
	this.peer.setView(inner.peer);
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