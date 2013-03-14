package org.ledyba.logiana.view

import scala.collection.mutable.Buffer
import scala.swing.BoxPanel
import scala.swing.Orientation

import org.ledyba.logiana.model.Signal
import org.ledyba.logiana.model.WaveData
import org.ledyba.logiana.model.WaveViewer

class WaveGraph extends BoxPanel(Orientation.Vertical) {
	val view = new WaveViewer();
	def data_(newdata : WaveData) : Unit = {
		this.view.data = newdata;
		this.updateData
	}
	def signal_(newsignals : Buffer[Signal]) {
		this.view.signals = newsignals;
		updateSignals
	}
	private def updateSignals {
		this.contents.clear;
		for(signal <- view.signals) {
			this.contents += new SignalGraph(view, signal);
		}
	}
	private def updateData {
		for(sigGraph <- this.contents) {
			sigGraph.asInstanceOf[SignalGraph].update();
		}
		this.peer.revalidate();
	}
	updateSignals;updateData;
}
