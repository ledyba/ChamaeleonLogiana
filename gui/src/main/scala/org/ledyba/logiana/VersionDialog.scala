package org.ledyba.logiana

import scala.swing.Dialog
import scala.swing.GridBagPanel
import scala.swing.Label
import scala.swing.Alignment
import scala.swing.Button
import scala.swing.Action
import java.awt.Insets

object VersionDialog extends Dialog {
	this.peer.setLocationByPlatform(true);
	title = "About";
	contents = new GridBagPanel() {add(new GridBagPanel(){
		layout += new Label("ChamaeleonLogiana") -> new Constraints { gridx=0;gridy=0;anchor=GridBagPanel.Anchor.Center; }
		layout += new Label("GUI Frontend") -> new Constraints { gridx=0;gridy=1; anchor=GridBagPanel.Anchor.Center; }
		layout += new Label("(C) 2013 PSI") { horizontalAlignment=Alignment.Left; verticalAlignment=Alignment.Top; } -> new Constraints { gridx=0;gridy=2;weightx=1;anchor=GridBagPanel.Anchor.West; insets=new Insets(10,0,0,0); }
		layout += new Label("http://ledyba.org/") -> new Constraints { gridx=0;gridy=3; anchor=GridBagPanel.Anchor.Center; }
		layout += new Label("https://github.com/ledyba/ChamaeleonLogiana") -> new Constraints { gridx=0;gridy=4; anchor=GridBagPanel.Anchor.Center; }
		layout += new Label("Original version by optimize:") { horizontalAlignment=Alignment.Left; verticalAlignment=Alignment.Top; } -> new Constraints { gridx=0;gridy=5;weightx=1;anchor=GridBagPanel.Anchor.West; insets=new Insets(10,0,0,0); }
		layout += new Label("http://optimize.ath.cx/cusb/index.html") -> new Constraints { gridx=0;gridy=6; anchor=GridBagPanel.Anchor.Center; }
		layout += new Button(Action("閉じる") { VersionDialog.close(); }) -> new Constraints{gridx=0;gridy=100;weightx=1; gridwidth=2; fill=GridBagPanel.Fill.Horizontal}
	}, new Constraints(){weightx=1;weighty=1;fill=GridBagPanel.Fill.Both;insets=new Insets(5,5,5,5);})};
}
