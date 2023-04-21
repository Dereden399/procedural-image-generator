package gui

import generationLogic.TooMuchDifferentTilesError
import javafx.event.{ActionEvent, EventHandler}
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView, WritableImage}
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.*

import java.io.{File, FileInputStream}
import scala.collection.mutable.Map

/**
 * Helper object that is used by propgram parts. For example, it contains a "not found" image and methods for creating an image views.
 * */
object ElementsHelper:

  val notFoundImage = new Image(new FileInputStream("src/main/resources/error.png"), 64, 64, false, false)
// map containing all opened images, so there is no need to open same image more than once.
  val openedFiles = Map[String, Image]()
// alphabet is used for generating the rules during autogeneration.
  val alphabet = Vector('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')

  def notFoundImageView =
    new ImageView(notFoundImage) :
      smooth = false
      preserveRatio = true
      fitWidth = 64

  /**
   * Make a path to the tile with its rotation.
   * @param name Name of the tile, that may (or may not) contain a rotation info.
   * @param pathToTiles Path to the tileset folder.
   * @return Tuple, first part of it is a full path to the image. Second part is the rotation number.
   * */
  def makePathWithRotationFromName(name: String, pathToTiles: String) =
    val pathToImage = pathToTiles + "/" + name.split("@").head
    val rotation =
      if name.split("@").length == 2 then name.split("@")(1).toInt
      else 0
    (pathToImage, rotation)

  def normalButton(label: String, handler: EventHandler[ActionEvent]) =
    new Button :
      text = label
      onAction = handler
      focusTraversable = false

  /**
   * Region, that fills all free space. Can be used to make a ''justify-between'' alignment, or just to move some elemnt to the border.
   * */
  def spacingRegion() =
    new Region :
      hgrow = Priority.Always
      prefWidth = 10

  /**
   * Make a label with the image by given path as an icon. Rotates the image if needed.
   * @param pathToImage Path to the image.
   * @param rotation How much times an image should be rotated by 90 degrees clockwise.
   * */
  def labelImage(pathToImage: String, rotation: Int = 0) =
    new Label() :
      padding = Insets(10)
      styleClass += "tile-element-background"
      val view = createImageView(pathToImage, rotation)
      view.fitWidth = 80
      graphic = view

  /**
   * Make an image view of the image by given path. Rotates the image if needed.
   * If the image was previously loaded to the memory, does not load it again.
   *
   * @param pathToImage Path to the image.
   * @param rotation    How much times an image should be rotated by 90 degrees counter-clockwise.
   * */
  def createImageView(pathToImage: String, rotation: Int = 0) =
    val img =
      if openedFiles.contains(pathToImage) then openedFiles(pathToImage)
      else
        val img = Image(new FileInputStream(pathToImage), 64, 64, false, false)
        openedFiles(pathToImage) = img
        img
    val view = ImageView(img)
    view.preserveRatio = true
    view.smooth = false
    view.rotate = 90 * rotation
    view.fitWidth = 64
    view

  /**
   * Deletes a cache folder and all files in it.
   * */
  def deleteCache(): Unit =
    val folder = new File(System.getProperty("user.home") + "/.proceduralimg")
    if folder.exists() then
      for file <- folder.list() do
        val currentFile = new File(folder.getPath, file)
        currentFile.delete()
    folder.delete()
    clearOpenedFiles()

  /**
   * Clears opened files.
   * */
  def clearOpenedFiles() =
    openedFiles.clear()

  /**
   * Returns a cache folder. If it is not created yet, creates it first.
   * */
  def getCache: File =
    val folder = new File(System.getProperty("user.home") + "/.proceduralimg")
    if folder.exists() then folder
    else
      folder.mkdirs()
      folder

  /**
   * Compare two WritableImage object pixel by pixel.
   * */
  def isImagesEqual(first: WritableImage, second: WritableImage) =
    if first == null || second == null then false
    else if first.getWidth != second.getWidth then false
    else if first.getHeight != second.getHeight then false
    else
      var ans = true
      var x = 0
      while x < first.getWidth && ans do
        var y = 0
        while y < first.getHeight && ans do
          if (first.getPixelReader.getArgb(x, y) != second.getPixelReader.getArgb(x, y)) then
            ans = false
          y += 1
        x += 1
      ans

  /**
   * Get next letter from the alphabet, which is not yet used. Throws an error, if all letter are in use already. 
   * */
  def getNextLetter(lettersUsed: Vector[Char]) =
    var ans = 0
    while ans < alphabet.size && lettersUsed.contains(alphabet(ans)) do
      ans += 1
    if ans >= alphabet.size then throw new TooMuchDifferentTilesError()
    alphabet(ans)

end ElementsHelper


