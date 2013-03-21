package org.ledyba.logiana.view

import javax.swing.JPopupMenu
import scala.swing.Component
import scala.swing.SequentialContainer

class PopupMenu(title: String) extends Component with SequentialContainer.Wrapper { self: PopupMenu =>
	override lazy val peer: JPopupMenu = new JPopupMenu(title)
}

trait PopupMenuContainer { self: Component =>
	lazy val popupMenu = new PopupMenu("");
	peer.setComponentPopupMenu(popupMenu.peer)
	peer.setInheritsPopupMenu(true);
}
