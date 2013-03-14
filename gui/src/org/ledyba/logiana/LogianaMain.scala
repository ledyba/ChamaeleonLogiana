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
import scala.swing.ScrollPane
import javax.swing.SwingWorker
import org.ledyba.logiana.model.WaveData

object LogianaMain extends SimpleSwingApplication {
	var handle : Logiana.Handle = null;
	val statusLine = new Label("status") { horizontalAlignment=Alignment.Left };
	val waveGraph = new WaveGraph();
	def start( sess : Session ) {
		if(handle == null){
			Logiana.find() match {
				case Left(msg) => statusLine.text=msg;return;
				case Right(hnd) => this.handle = hnd
			}
		}
		handle.start(sess) match {
			case Left(msg) => statusLine.text = msg;
			case Right(res) => {
				statusLine.text = "計測中…";
				val sensor = new SwingWorker[Either[String, WaveData],Unit](){
					override def doInBackground():Either[String, WaveData] = {
						while(true){
							val mes = handle.isMeasureing()
							mes match {
								case Right(x) => {
									if(x){
										Thread.sleep(1000);
									}else{
										return handle.end(sess)
									}
								}
								case Left(x) => return Left(x);
							}
						}
						return Left("Why came here?");
					}
					override def done() = {
						val result = get;
						result match {
							case Right(x) => {
								statusLine.text="正常に終了しました。";
								waveGraph.data_(x);
								handle.close;
								handle = null;
							}
							case Left(x) => statusLine.text=x;
						}
					}
				}
				sensor.execute()
			}
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
				contents += new MenuItem(Action("計測開始") {
					openMeasureDialog();
				}) { mnemonic = Key.S; }
				contents += new MenuItem(Action("終了(E)") { LogianaMain.quit(); }) {
					mnemonic = Key.E;
				}
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
			super.closeOperation();
		}
		def openMeasureDialog() {
			if( handle != null ) {
				handle.isMeasureing() match {
					case Left(x) => {
						statusLine.text="IOエラー："++x;
						return;
					}
					case Right(true) => {
						statusLine.text = "すでに計測中です";
						return;
					}
					case _ => {}
				}
			}
			SessionDialog.open();
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
			val freq = freqDlg.selection.item.asInstanceOf[org.ledyba.logiana.control.Frequency.Value];
			val mes = mesDlg.selection.item.asInstanceOf[org.ledyba.logiana.control.MeasureType.Value];
			val cond = condDlg.selection.item.asInstanceOf[org.ledyba.logiana.control.Condition.Value];
			val line = lineDlg.selection.item.asInstanceOf[org.ledyba.logiana.control.TriggerLine.Value];
			val sess : Session = new Session(freq, mes, cond, line);
			LogianaMain.start( sess );
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