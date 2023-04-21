package gui

import scalafx.Includes.*
import scalafx.beans.property.ObjectProperty
import scalafx.event.EventHandler
import scalafx.geometry.{Bounds, Point2D, Pos}
import scalafx.scene.control.ScrollPane
import scalafx.scene.input.ZoomEvent
import scalafx.scene.layout.VBox
import scalafx.scene.{Group, Node, control}

/**
 * A Scroll pane with zooming functionality.
 * */
class ZoomableScrollPane(target: Node) extends ScrollPane :
  private val zoomIntensity = 1
  private val zoomNode = new Group(target)
  private var scaleValue = 0.7

  def getScaleValue = scaleValue

  content = outerNode(zoomNode)
  pannable = true
  fitToHeight = true
  fitToWidth = true
  updateScale()

  def addScale(value: Double) =
    val zoomFactor = math.exp(value * zoomIntensity)
    scaleValue *= zoomFactor
    updateScale()

  private def outerNode(node: Node): Node =
    val outerNode = centeredNode(node)
    outerNode.onZoom = (e: ZoomEvent) => {
      e.consume()
      onZoom(e.zoomFactor, Point2D(e.getX, e.getY))
    }
    outerNode

  private def centeredNode(node: Node): Node =
    val vBox = new VBox(node) :
      alignment = Pos.Center
    vBox

  private def onZoom(zoomDelta: Double, mousePoint: Point2D) =
    val zoomFactor = math.exp((zoomDelta - 1) * zoomIntensity)

    val innerBounds = zoomNode.getLayoutBounds
    val viewportBounds = this.getViewportBounds

    // calculate pixel offsets from [0, 1] range
    val valX = this.getHvalue * (innerBounds.getWidth - viewportBounds.getWidth)
    val valY = this.getVvalue * (innerBounds.getHeight - viewportBounds.getHeight)

    scaleValue *= zoomFactor
    updateScale()
    this.layout() // refresh ScrollPane scroll positions & target bounds

    // convert target coordinates to zoomTarget coordinates
    val posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint))

    // calculate adjustment of scroll position (pixels)
    val adjustment = target.getLocalToParentTransform.deltaTransform(posInZoomTarget.multiply(zoomFactor - 1))

    // convert back to [0, 1] range
    // (too large/small values are automatically corrected by ScrollPane)
    val updatedInnerBounds = zoomNode.getBoundsInLocal
    this.setHvalue((valX + adjustment.getX) / (updatedInnerBounds.getWidth - viewportBounds.getWidth))
    this.setVvalue((valY + adjustment.getY) / (updatedInnerBounds.getHeight - viewportBounds.getHeight))

  private def updateScale(): Unit =
    target.scaleX = scaleValue
    target.scaleY = scaleValue
end ZoomableScrollPane

