package gui

import generationLogic.{Generation, Grid, Position, Rule}
import scalafx.Includes.eventClosureWrapperWithZeroParam
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.*

import scala.util.Try

/**
 * Drawer class is used for drawing on the image. It also creates all drawing menus for the UI.
 * @param showError A function that is used when some error/exception is thrown.
 * */
class Drawer(showError: (String, String) => Unit):
  var pathToTiles = ""

  var generation: Option[Generation] = None
  var selectedTileToDraw: Option[(Label, String)] = None

  /**
   * Make a draw attempt with selectedTileToDraw.
   * @param pos position of the square to be collapsed
   * */
  def drawAttempt(pos: Position) =
    try
      if selectedTileToDraw.isDefined then
        generation.get.collapseWith(selectedTileToDraw.get._2, pos)
    catch
      case e: Exception => showError(e.getMessage, "Error")

  /**
   * Creates a content for the Draw tab in the UI
   * */
  def createContent() =
    if this.isLoaded then
      this.allTiles.grouped(2).map(group => {
        new HBox() {
          alignment = Pos.Center
          spacing = 20
          children = group.map(x => {
            val label = createLabelFromText(x)
            label.onMouseClicked = () => {
              if selectedTileToDraw.isEmpty then
                selectedTileToDraw = Some((label, x))
                label.styleClass -= "tile-element-background"
                label.styleClass += "tile-element-background-selected"
              else if selectedTileToDraw.get._1 == label then
                selectedTileToDraw = None
                label.styleClass += "tile-element-background"
                label.styleClass -= "tile-element-background-selected"
              else
                selectedTileToDraw.get._1.styleClass += "tile-element-background"
                selectedTileToDraw.get._1.styleClass -= "tile-element-background-selected"
                selectedTileToDraw = Some((label, x))
                label.styleClass -= "tile-element-background"
                label.styleClass += "tile-element-background-selected"
            }
            label
          })
        }
      }).toSeq
    else
      val errorLabel = new Label() :
        text = "You have not set up the rules and tiles yet.\nTiles will appear here after that."
        wrapText = true
        padding = Insets(10)
        styleClass += "section-background"
      val box = new HBox(errorLabel)
      box.alignment = Pos.Center
      Seq(box)

  private def createLabelFromText(text: String) =
    val result = ElementsHelper.makePathWithRotationFromName(text, pathToTiles)
    ElementsHelper.labelImage(result._1, result._2)

  def isLoaded = this.generation.isDefined

  def allTiles =
    Try {
      this.generation.get.allTiles
    }.getOrElse(Vector[String]())

  def setPathToTiles(path: String) =
    this.pathToTiles = path

  def setGeneration(generation: Option[Generation]) =
    this.generation = generation


end Drawer
