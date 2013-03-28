package org.ledyba.logiana.view

import scala.swing.Component
import javax.swing.JViewport

class Viewport extends Component{
	override lazy val peer: JViewport = new JViewport();

}