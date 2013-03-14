package org.ledyba.logiana.view

import scala.collection.mutable.Buffer
import scala.swing.BoxPanel
import scala.swing.Orientation
import org.ledyba.logiana.model.Signal
import org.ledyba.logiana.model.WaveData
import org.ledyba.logiana.model.WaveViewer
import scala.swing.GridBagPanel
import scala.swing.Label
import scala.collection.mutable.ArrayBuffer

class WaveGraph extends GridBagPanel {
	val view = new WaveViewer();
	def data_(newdata : WaveData) : Unit = {
		this.view.data = newdata;
		this.updateData
	}
	def signal_(newsignals : Buffer[Signal]) {
		this.view.signals = newsignals;
		updateSignals
	}
	val signalGraphs = ArrayBuffer[SignalGraph]();
	private def updateSignals {
		this.peer.removeAll();
		this.signalGraphs.clear
		var i=0;
		for(signal <- view.signals) {
			this.add(new Label(signal.repr), (0,i))
			val c = new SignalGraph(view, signal);
			signalGraphs+=c;
			this.add(c, (1,i))
			i+=1;
		}
	}
	private def updateData {
		for(sigGraph <- this.signalGraphs) {
			sigGraph.update
		}
		this.peer.revalidate();
	}
	updateSignals;updateData;
}
