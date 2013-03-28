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
import org.ledyba.logiana.control.TriggerLine
import org.ledyba.logiana.view.PopupMenuContainer
import org.ledyba.logiana.view.DataPanel
import scala.swing.ScrollPane
import javax.swing.SwingWorker
import org.ledyba.logiana.model.MeasuredData
import org.ledyba.logiana.control.Logiana
import java.util.concurrent.atomic.AtomicBoolean
import org.ledyba.logiana.control.OperationRunner
import org.ledyba.logiana.control.OperationRunner
import scala.swing.GridPanel
import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.FileChooser
import java.io.File
import org.ledyba.logiana.model.DataProjection
import org.ledyba.logiana.control.Operation
import javax.swing.filechooser.FileNameExtensionFilter
import scala.swing.ScrollBar
import scala.swing.Adjustable
import java.awt.Adjustable
import java.awt.event.AdjustmentListener
import java.awt.event.AdjustmentEvent

object LogianaMain extends SimpleSwingApplication {
	val kConfigFilename = "./conf.bin";
	val kLastFilename="./last.bin"
	
	val statusLine = new Label("status") { horizontalAlignment=Alignment.Left };
	val dataPanel = new DataPanel(kLastFilename);
	
	val scrollX = new ScrollBar() { maximum = 1000; minimum=0; orientation=Orientation.Horizontal; };
	scrollX.peer.addAdjustmentListener(new AdjustmentListener{
		override def  adjustmentValueChanged(e:AdjustmentEvent):Unit = {
			val x = e.getValue();
			val y = scrollY.peer.getValue();
			dataPanel.scrollTo(x,y, maxX, maxY);
		}
	});
	val scrollY = new ScrollBar() { maximum = 1000; minimum=0; orientation=Orientation.Vertical; };
	scrollY.peer.addAdjustmentListener(new AdjustmentListener{
		override def  adjustmentValueChanged(e:AdjustmentEvent):Unit = {
			val x = scrollX.peer.getValue();
			val y = e.getValue();
			dataPanel.scrollTo(x,y, maxX, maxY);
		}
	});
	private val maxX = scrollX.peer.getMaximum()-scrollX.peer.getVisibleAmount();
	private val maxY = scrollY.peer.getMaximum()-scrollY.peer.getVisibleAmount();
	private var opRunner:OperationRunner = null;
	val conf = Config(kConfigFilename);
	def start( op : Operation ) {
		if(this.opRunner == null) {
			this.opRunner = new OperationRunner(conf, op, v=>
				v match {
					case Left(msg) => {
						statusLine.text=msg;
						opRunner = null;
					}
					case Right(wavedata) => {
						statusLine.text="計測が正常に終了しました。";
						dataPanel.updateData(wavedata);
						opRunner = null;
					}
				}
			)
			opRunner.start();
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
				contents += new MenuItem(Action("セーブ") {
					val x=new FileChooser(new File(".").getCanonicalFile());
					x.fileFilter = new FileNameExtensionFilter("Logiana Measure Data", "mes");
					x.showSaveDialog(this) match {
						case FileChooser.Result.Approve => {
							val fpath=x.selectedFile.getCanonicalPath();
							if(fpath.endsWith(".mes")){
								dataPanel.save(fpath);
							}else{
								dataPanel.save(fpath+".mes");
							}
						}
						case _ => Unit
					};
				}) { mnemonic = Key.S; }
				contents += new MenuItem(Action("ロード") {
					val x=new FileChooser(new File(".").getCanonicalFile());
					x.fileFilter = new FileNameExtensionFilter("Logiana Measure Data", "mes");
					x.showOpenDialog(this) match {
						case FileChooser.Result.Approve => {
							val fpath=x.selectedFile.getCanonicalPath();
							if(fpath.endsWith(".mes")){
								dataPanel.load(fpath);
							}else{
								dataPanel.load(fpath+".mes");
							}
						}
						case _ => Unit
					};
				}) { mnemonic = Key.S; }
				contents += new MenuItem(Action("計測中止") {
					if(opRunner == null){
						statusLine.text="計測が開始されていません。";
					}else{
						opRunner.sendExit;
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
			add(new GridBagPanel(){
				this.add(dataPanel, new Constraints(){gridx=0;gridy=0;weightx=1;weighty=1;fill=GridBagPanel.Fill.Both});
				this.add(new BoxPanel(Orientation.Horizontal){
					this.contents+=new Button(Action("+"){
						dataPanel.scaleUp;
					});
					this.contents+=new Button(Action("-"){
						dataPanel.scaleDown;
					});
				}, new Constraints(){gridx=0;gridy=2;weightx=1;fill=GridBagPanel.Fill.Horizontal});
				this.add(scrollX, new Constraints(){gridx=0;gridy=1;weightx=0;weighty=0;fill=GridBagPanel.Fill.Both; gridwidth=1;});
				this.add(scrollY, new Constraints(){gridx=1;gridy=0;weightx=0;weighty=0;fill=GridBagPanel.Fill.Both; gridheight=1;});
			}, BorderPanel.Position.Center)
		}
		override def closeOperation() {
			conf.write(kConfigFilename);
			dataPanel.save(kLastFilename);
			super.closeOperation();
		}
		def openMeasureDialog() {
			if( opRunner != null ) {
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
			LogianaMain.start( new Operation(freq, mes, cond, line) );
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