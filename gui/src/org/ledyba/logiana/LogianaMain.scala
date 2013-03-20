/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, PSI
 */
package org.ledyba.logiana

import scala.swing.{ SimpleSwingApplication, MainFrame, Dimension }
import scala.swing.Dialog
import scala.swing.GridBagPanel
import scala.swing.Label
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.Action
import scala.swing.event.Key
import scala.swing.BorderPanel
import scala.swing.Alignment
import java.awt.Color
import scala.swing.ComboBox
import org.ledyba.logiana.control.Frequency
import org.ledyba.logiana.control.MeasureType
import scala.swing.Button
import org.ledyba.logiana.control.Condition
import org.ledyba.logiana.control.Session
import org.ledyba.logiana.control.TriggerLine
import org.ledyba.logiana.view.WaveGraph
import org.ledyba.logiana.view.PopupMenuContainer
import scala.swing.ScrollPane
import javax.swing.SwingWorker
import org.ledyba.logiana.model.WaveData
import org.ledyba.logiana.control.Logiana
import java.util.concurrent.atomic.AtomicBoolean
import org.ledyba.logiana.control.SessionRunner
import org.ledyba.logiana.control.SessionRunner

object LogianaMain extends SimpleSwingApplication {
	val kConfigFilename = "./conf.bin";
	
	val statusLine = new Label("status") { horizontalAlignment=Alignment.Left };
	val waveGraph = new WaveGraph();
	var session:SessionRunner = null;
	val conf = Config(kConfigFilename);
	def start( sess : Session ) {
		if(this.session == null) {
			this.session = new SessionRunner(conf, sess, v=>
				v match {
					case Left(msg) => {
						statusLine.text=msg;
						session = null;
					}
					case Right(wavedata) => {
						statusLine.text="計測が正常に終了しました。";
						waveGraph.data=wavedata;
						session = null;
					}
				}
			)
			session.start();
			statusLine.text="計測中…";
		}else{
			statusLine.text="すでに計測中です。";
		}
	}
	override def top = new MainFrame {
		this.peer.setLocationByPlatform(true);
		// Windowのタイトル
		title = "Logic Analyzer"
		// Windowのサイズ
		preferredSize = new Dimension(640, 480);
		menuBar = new MenuBar() {
			contents += new Menu("ファイル(F)") {
				mnemonic = Key.F;
				contents += new MenuItem(Action("計測中止") {
					if(session == null){
						statusLine.text="計測が開始されていません。";
					}else{
						session.sendExit;
					}
				}) { mnemonic = Key.S; }
				contents += new MenuItem(Action("計測開始") {
					openMeasureDialog();
				}) { mnemonic = Key.S; }
				contents += new MenuItem(Action("終了(E)") { LogianaMain.quit(); }) {
					mnemonic = Key.E;
				}
			}
			contents += new Menu("編集") {
				mnemonic = Key.F;
				contents += new MenuItem(Action("設定") {
					new ConfigDialog(conf).open
				}) { mnemonic = Key.S; }
			}
			contents += new Menu("ヘルプ（H)") {
				mnemonic = Key.H;
				contents += new MenuItem(Action("バージョン情報(V)") {
					VersionDialog.open();
				}) { mnemonic = Key.V; }
			}
		}
		contents = new BorderPanel {
			add(statusLine, BorderPanel.Position.South);
			add(new ScrollPane(){
				verticalScrollBarPolicy = ScrollPane.BarPolicy.Always;
				horizontalScrollBarPolicy = ScrollPane.BarPolicy.Always;
				viewportView = waveGraph;
			}, BorderPanel.Position.Center)
		}
		override def closeOperation() {
			conf.write(kConfigFilename);
			super.closeOperation();
		}
		def openMeasureDialog() {
			if( session != null ) {
				statusLine.text="計測中なので、ダイアログを開けません。";
			}else{
				SessionDialog.open();
			}
		}
	}
	object SessionDialog extends Dialog {
		this.peer.setLocationByPlatform(true);
		title = "Session Setting";
		val freqDlg = new ComboBox( Frequency.values.toSeq );
		val mesDlg = new ComboBox( MeasureType.values.toSeq );
		val condDlg = new ComboBox( Condition.values.toSeq );
		val lineDlg = new ComboBox( TriggerLine.values.toSeq );
		contents = new GridBagPanel {
			layout += new Label("周波数："){horizontalAlignment=Alignment.Right} -> new Constraints{ gridx=0;gridy=0; anchor=GridBagPanel.Anchor.East };
			layout += freqDlg -> new Constraints{ gridx=1; weightx = 1; gridy=0; anchor=GridBagPanel.Anchor.Center;  fill=GridBagPanel.Fill.Horizontal }
			
			layout += new Label("計測タイプ："){ horizontalAlignment=Alignment.Right } -> new Constraints{ gridx=0;gridy=1; anchor=GridBagPanel.Anchor.East };
			layout += mesDlg -> new Constraints{ gridx=1; weightx = 1; gridy=1; anchor=GridBagPanel.Anchor.Center;  fill=GridBagPanel.Fill.Horizontal }

			layout += new Label("トリガ："){ horizontalAlignment=Alignment.Right } -> new Constraints{ gridx=0;gridy=2; anchor=GridBagPanel.Anchor.East };
			layout += condDlg -> new Constraints{ gridx=1; weightx = 1; gridy=2; anchor=GridBagPanel.Anchor.Center;  fill=GridBagPanel.Fill.Horizontal }

			layout += new Label("センサライン："){ horizontalAlignment=Alignment.Right } -> new Constraints{ gridx=0;gridy=3; anchor=GridBagPanel.Anchor.East };
			layout += lineDlg -> new Constraints{ gridx=1; weightx = 1; gridy=3; anchor=GridBagPanel.Anchor.Center;  fill=GridBagPanel.Fill.Horizontal }

			layout += new Button(Action("Start") { SessionDialog.this.onStart(); }) -> new Constraints{gridx=0;gridy=100;weightx=1; gridwidth=2; fill=GridBagPanel.Fill.Horizontal}
		}
		def onStart () {
			val freq = freqDlg.selection.item.asInstanceOf[Frequency.Value];
			val mes = mesDlg.selection.item.asInstanceOf[MeasureType.Value];
			val cond = condDlg.selection.item.asInstanceOf[Condition.Value];
			val line = lineDlg.selection.item.asInstanceOf[TriggerLine.Value];
			LogianaMain.start( new Session(freq, mes, cond, line) );
			this.close;
		}
	}
	object VersionDialog extends Dialog {
		this.peer.setLocationByPlatform(true);
		title = "About";
		contents = new GridBagPanel {
			layout += new Label("ロジックアナライザ・ウォッチャー") -> new Constraints { gridx=0;gridy=0;weightx=1.0f; }
			layout += new Label("(C) 2013 PSI") { horizontalAlignment=Alignment.Left; verticalAlignment=Alignment.Top; } -> new Constraints { gridx=0;gridy=1;weightx=1;fill=GridBagPanel.Fill.Both; }

			layout += new Button(Action("閉じる") { VersionDialog.close(); }) -> new Constraints{gridx=0;gridy=100;weightx=1; gridwidth=2; fill=GridBagPanel.Fill.Horizontal}
		}
	}
}