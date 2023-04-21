package gui

import generationLogic.Square
import scalafx.Includes.eventClosureWrapperWithZeroParam
import scalafx.scene.Node
import scalafx.scene.image.ImageView
import scalafx.scene.layout.StackPane

/**
 * Tile class shows a square to the screen. Updates its content every time square's image changes.
 * Also listens to user's clicks to pass info to the drawer.
 * */
class Tile(val square: Square, pathToTiles: String, drawer: Drawer):
  private val pane = StackPane()

  pane.setPrefSize(16, 16)

  pane.onMouseClicked = () => {
    drawer.drawAttempt(square.position)
  }

  def getPane = pane

  def setImages(images: Seq[ImageView]) =
    pane.children.clear()
    pane.children = images

  if square.imageToShow.isEmpty then
    setImages(Seq(ElementsHelper.notFoundImageView))
  else
    setImages(square.imageToShow.toSeq.map(name => {
      val result = ElementsHelper.makePathWithRotationFromName(name, pathToTiles)
      ElementsHelper.createImageView(result._1, result._2)
    }))
// if square's image changes, this tile update its content.
  square.imageToShow.onChange((x, _) => {
    if x.isEmpty then
      setImages(Seq(ElementsHelper.notFoundImageView))
    else
      setImages(x.toSeq.map(name => {
        val result = ElementsHelper.makePathWithRotationFromName(name, pathToTiles)
        ElementsHelper.createImageView(result._1, result._2)
      }))
  })

end Tile

