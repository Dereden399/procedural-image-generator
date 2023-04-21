package gui

import generationLogic.*
import io.circe.generic.auto.*
import io.circe.parser.decode
import io.circe.syntax.*
import javafx.embed.swing.SwingFXUtils
import scalafx.Includes.{jfxColor2sfx, jfxPixelReader2sfx}
import scalafx.scene.image.{Image, WritableImage}
import scalafx.scene.paint.Color

import java.io.{File, FileInputStream, FilenameFilter}
import javax.imageio.ImageIO
import scala.collection.mutable.Buffer
import scala.io.Source

/**
 * Reader is responsible for reading and paresing files.
 * */
object Reader:
  /**
   * Read all tiles and combine them with rules.
   * @param pathToRules Path to the rules file.
   * @param pathToTiles Path to the folder containing tiles.
   * @return A map with tilename and rotatin as a key, and a rule for this tile as a value.
   * */
  def readFiles(pathToTiles: String, pathToRules: String): Map[String, Rule] =
    val tiles = readTiles(pathToTiles)
    val rules = readRules(pathToRules)
    rules.filter(obj => tiles.contains(obj._1.split('@').head))

  /**
   * Read all tile names from the folder.
   * @param path Path of the folder with tiles.
   * @return Array containing all tile names
   * */
  def readTiles(path: String): Array[String] =
    val folder = File(path)
    if !folder.exists() then throw new InvalidTilesetFolderError()
    val fileFilter = new FilenameFilter() :
      // Must be image in png or jpg format
      override def accept(dir: File, name: String) = name.endsWith(".png") || name.endsWith(".jpg")
    folder.list(fileFilter)

  /**
   * Read and parse all rules from JSON file.
   * @param path Path to the JSON file.
   * @return Map with tile name and rotation as a key and Rule for this tile as a value.
   * */
  def readRules(path: String): Map[String, Rule] =
    val file = File(path)
    if !file.exists() || !path.endsWith(".json") then throw InvalidRuleFileError()
    val fileSource = Source.fromFile(file)
    val textFromFile = fileSource.getLines().mkString("\n")
    fileSource.close()
    val parserResult = decode[List[RuleWithTileName]](textFromFile)
    parserResult match
      case Right(value) => value.flatMap(_.convertToTuples()).toMap
      case Left(value) => throw new ReaderException(value.toString)

  /**
   * Read an image and then divide it to tileSize by tileSize size tiles, generates rules for these tiles based on their pixel border colors.
   * All tiles are saved in the cache folder "user.home"/.proceduralimg
   * @param path Path to the image
   * @return Map with tile name as a key and a rule for the tile as a value.
   * */
  def readImage(path: String, tileSize: Int): Map[String, Rule] =
    val file = File(path)
    if !file.exists() || (!path.endsWith(".png") && !path.endsWith(".jpg")) then throw InvalidImageFileError()
    val image = new Image(new FileInputStream(file.getAbsolutePath))
    if image.getWidth % tileSize != 0 || image.getHeight % tileSize != 0 then throw InvalidImageSize(tileSize)

    val colorsConstraints = scala.collection.mutable.Map[Color, Char]()
    val imagesSaved = scala.collection.mutable.Map[WritableImageWrapper, Rule]()

    val source = image.getPixelReader
    // Input image spltted on tiles.
    val splittedInputImage =
      for
        x <- 0 until image.getWidth.toInt by tileSize
        y <- 0 until image.getHeight.toInt by tileSize
      yield WritableImageWrapper(WritableImage(source, x, y, tileSize, tileSize))
    // For every tile check if it has been already parsed. If not, create a rule for it and save
    for tile <- splittedInputImage do
      if !imagesSaved.contains(tile) then
        val colorUpLeft = tile.image.getPixelReader.getColor(0, 0)
        val colorUp = tile.image.getPixelReader.getColor(tileSize / 2, 0)
        val colorUpRight = tile.image.getPixelReader.getColor(tileSize - 1, 0)
        val colorRight = tile.image.getPixelReader.getColor(tileSize - 1, tileSize / 2)
        val colorRightDown = tile.image.getPixelReader.getColor(tileSize - 1, tileSize - 1)
        val colorDown = tile.image.getPixelReader.getColor(tileSize / 2, tileSize - 1)
        val colorDownLeft = tile.image.getPixelReader.getColor(0, tileSize - 1)
        val colorLeft = tile.image.getPixelReader.getColor(0, tileSize / 2)

        // letters for borders based on color
        val letterUpLeft =
          if colorsConstraints.contains(colorUpLeft) then
            colorsConstraints(colorUpLeft)
          else
            colorsConstraints(colorUpLeft) = ElementsHelper.getNextLetter(colorsConstraints.values.toVector)
            colorsConstraints(colorUpLeft)
        val letterUp =
          if colorsConstraints.contains(colorUp) then
            colorsConstraints(colorUp)
          else
            colorsConstraints(colorUp) = ElementsHelper.getNextLetter(colorsConstraints.values.toVector)
            colorsConstraints(colorUp)
        val letterUpRight =
          if colorsConstraints.contains(colorUpRight) then
            colorsConstraints(colorUpRight)
          else
            colorsConstraints(colorUpRight) = ElementsHelper.getNextLetter(colorsConstraints.values.toVector)
            colorsConstraints(colorUpRight)
        val letterRight =
          if colorsConstraints.contains(colorRight) then
            colorsConstraints(colorRight)
          else
            colorsConstraints(colorRight) = ElementsHelper.getNextLetter(colorsConstraints.values.toVector)
            colorsConstraints(colorRight)
        val letterRightDown =
          if colorsConstraints.contains(colorRightDown) then
            colorsConstraints(colorRightDown)
          else
            colorsConstraints(colorRightDown) = ElementsHelper.getNextLetter(colorsConstraints.values.toVector)
            colorsConstraints(colorRightDown)
        val letterDown =
          if colorsConstraints.contains(colorDown) then
            colorsConstraints(colorDown)
          else
            colorsConstraints(colorDown) = ElementsHelper.getNextLetter(colorsConstraints.values.toVector)
            colorsConstraints(colorDown)
        val letterDownLeft =
          if colorsConstraints.contains(colorDownLeft) then
            colorsConstraints(colorDownLeft)
          else
            colorsConstraints(colorDownLeft) = ElementsHelper.getNextLetter(colorsConstraints.values.toVector)
            colorsConstraints(colorDownLeft)
        val letterLeft =
          if colorsConstraints.contains(colorLeft) then
            colorsConstraints(colorLeft)
          else
            colorsConstraints(colorLeft) = ElementsHelper.getNextLetter(colorsConstraints.values.toVector)
            colorsConstraints(colorLeft)
        val upConstraints = "" + letterUpLeft + letterUp + letterUpRight
        val rightConstraints = "" + letterUpRight + letterRight + letterRightDown
        val downConstraints = "" + letterDownLeft + letterDown + letterRightDown
        val leftConstraints = "" + letterUpLeft + letterLeft + letterDownLeft

        val rule = Rule(rightConstraints, upConstraints, downConstraints, leftConstraints)
        imagesSaved(tile) = rule


    ElementsHelper.deleteCache()
    val cacheFolder = ElementsHelper.getCache
    val convertedMap =
      for (imageWithRule, index) <- imagesSaved.toVector.zipWithIndex yield
        val fileToSave = new File(cacheFolder.getPath, s"${index}.png")
        ImageIO.write(SwingFXUtils.fromFXImage(imageWithRule._1.image, null), "png", fileToSave)
        (s"${index}.png", imageWithRule._2)
    convertedMap.toMap

