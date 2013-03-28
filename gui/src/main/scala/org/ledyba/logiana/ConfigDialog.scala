package org.ledyba.logiana

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import scala.swing.Action
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.FileChooser
import scala.swing.GridBagPanel
import scala.swing.Label
import scala.swing.TextField
import java.awt.Insets

sealed class Config extends Serializable {
	var dynamic = "driver";
	
	def write(fname:String) = {
		val st = new ObjectOutputStream(new FileOutputStream(fname));
		try {
			st.writeObject(this);
		} finally {
			st.close();
		}
	}
}

object Config {
	def apply(fname:String):Config = {
		val f = new java.io.File(fname);
		if(f.exists && f.isFile) {
			val st = new ObjectInputStream(new FileInputStream(fname));
			try {
				return st.readObject().asInstanceOf[Config];
			} finally {
				st.close;
			}
		}else{
			return new Config();
		}
	}
}

class ConfigDialog(val conf:Config) extends Dialog {
	val kDefaultConfigFilename = "./config.bin";
	title="設定";
	modal=true;
	contents = new GridBagPanel() {add(new GridBagPanel(){
		val driver = new TextField(new File(conf.dynamic).getCanonicalPath()) { editable=false; };
		add(new Label("Dynaic Driver: "), new Constraints(){gridx=0;gridy=0;});
		add(driver, new Constraints(){gridx=1;gridy=0;weightx=1.0;fill=GridBagPanel.Fill.Horizontal;});
		add(new Button(Action("select...") {
			val x=new FileChooser(new File(conf.dynamic).getCanonicalFile().getParentFile());
			x.showOpenDialog(this) match {
				case FileChooser.Result.Approve => {
					val fpath=x.selectedFile.getCanonicalPath();
					driver.text=fpath;
					conf.dynamic=fpath;
				}
				case _ => Unit
			};
			}){  }, new Constraints(){gridx=2;gridy=0;});
	}, new Constraints(){weightx=1;weighty=1;fill=GridBagPanel.Fill.Both;insets=new Insets(5,5,5,5);})};
	override def closeOperation() {
		super.closeOperation();
	}
	override def open(){
		this.peer.setLocationByPlatform(true);
		super.open();
	}
}


