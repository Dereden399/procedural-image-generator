package gui

import generationLogic.Grid
import scalafx.geometry.Insets
import scalafx.scene.image.ImageView
import scalafx.scene.layout.GridPane
import scalafx.scene.paint.Color

/**
 * A GridPane, that contains all Tiles, that are shown to the user.
 * */
class ImageGridPane(baseImage: ImageView, drawer: Drawer):
  private val pane = new GridPane()
  pane.add(baseImage, 0, 0)

  def getImage = pane

  /**
   * Update the content of this grid pane to match the new Grid.
   * */
  def setImages(grid: Grid, path: String) =
    pane.children.clear()
    grid.allSquares.map(square => Tile(square, path, drawer))
      .foreach(x => pane.add(x.getPane, x.square.position.x, x.square.position.y))


end ImageGridPane

