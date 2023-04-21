package gui

import generationLogic.{Generation, Ratio, SimpleWFC}
import javafx.embed.swing.SwingFXUtils
import javafx.stage.Stage
import scalafx.Includes.eventClosureWrapperWithZeroParam
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.SnapshotParameters
import scalafx.scene.control.*
import scalafx.scene.control.TabPane.TabClosingPolicy
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser

import java.io.{File, FileInputStream}
import javax.imageio.ImageIO
import scala.util.Try


/**
 * Main GUI class, that is responsible for creating a graphic for the program. It also handles all communication with the logic part.
 * */
class GUI:
  val drawer = Drawer(showErrorWithText)
  val generationFilesChooser = GenerationFilesChooser()
  var generation: ObjectProperty[Option[Generation]] = ObjectProperty(None)
  val imageGridPane = ImageGridPane(ImageView(new Image(new FileInputStream("src/main/resources/kuva.png"))), drawer)
  private var isImageLoaded = BooleanProperty(false)
  private var savedTilesetFolderPath = ""

  def bottomLine() =
    new HBox :
      padding = Insets(10, 20, 10, 20)
      maxHeight = 50
      alignment = Pos.Center
      spacing = 10

      val clearButton = ElementsHelper.normalButton("Clear", e => {
        generation.value.foreach(_.clearImage())
      })
      val exportButton = ElementsHelper.normalButton("Export", e => exportImage())
      val generateButton = ElementsHelper.normalButton("Generate", e => {
        generation.value.foreach(_.generateImage())
      })
      clearButton.disable <== isImageLoaded.not()
      exportButton.disable <== isImageLoaded.not()
      generateButton.disable <== isImageLoaded.not()

      children ++= Seq(exportButton, ElementsHelper.spacingRegion())
      children ++= Seq(clearButton, generateButton)

  /**
   * Export the image that is shown at that moment.
   * */
  def exportImage(): Unit =
    val params = new SnapshotParameters() :
      fill = Color.Transparent


    val tempSize = imageGridPane.getImage.scaleX.value
    imageGridPane.getImage.scaleX = 1
    imageGridPane.getImage.scaleY = 1
    val snapshotOfTheImage = imageGridPane.getImage.snapshot(params, null)
    imageGridPane.getImage.scaleX = tempSize
    imageGridPane.getImage.scaleY = tempSize
    val fileChooser = new FileChooser() :
      val mainFolder = System.getProperty("user.home")
      initialDirectory = new File(mainFolder)
      title = "Save image"
      extensionFilters.addAll(new FileChooser.ExtensionFilter("Image Files", Seq("*.png", "*.jpg")))
    val selectedFile = fileChooser.showSaveDialog(imageGridPane.getImage.getScene.getWindow)
    if selectedFile != null then
      try
        ImageIO.write(SwingFXUtils.fromFXImage(snapshotOfTheImage, null), "png", selectedFile)
      catch
        case e: Exception => showErrorWithText("Error", e.getMessage)

  def sideMenu() =
    new TabPane :
      val optionsTab = createOptionsTab()
      val drawtab = createDrawTab()
      tabs = Seq(optionsTab, drawtab)
      tabClosingPolicy = TabClosingPolicy.Unavailable
      tabMinWidth = 100
      prefWidth = 250

  def createDrawTab(): Tab =
    new Tab :
      text = "Draw"
      onSelectionChanged = (_) => {
        if this.isSelected then update()
      }

      def update() =
        container.children = drawer.createContent()

      val container = new VBox :
        padding = Insets(10)
        spacing = 10
        prefWidth = 250
        alignment = Pos.Center
        children = drawer.createContent()

      val contentBox = new ScrollPane() :
        hbarPolicy = ScrollPane.ScrollBarPolicy.Never
        content = container

      content = contentBox

  def createOptionsTab(): Tab =
    new Tab :
      text = "Options"
      val contentVbox = new VBox :
        padding = Insets(10)
        spacing = 10
        val generationModelBox = createGenerationMethodBox()
        val imageSizeBox = createImageSizeBox()
        val ratioAdderBox = createRatioBox()
        children += generationModelBox
        children += imageSizeBox
        children += ratioAdderBox
      content = contentVbox

  def createImageSizeBox() =
    new VBox :
      padding = Insets(0, 5, 0, 5)
      spacing = 10
      val widthTextInput = new NumericTextField :
        maxWidth = 40
      val heightTextInput = new NumericTextField :
        maxWidth = 40
      val imageWidthRow = new HBox :
        children += Label("Width")
        children += ElementsHelper.spacingRegion()
        children += widthTextInput
      val imageHeightRow = new HBox :
        children += Label("Height")
        children += ElementsHelper.spacingRegion()
        children += heightTextInput
      children += imageWidthRow
      children += imageHeightRow
      val setSizeButton = ElementsHelper
        .normalButton("Save settings", e => resizeGrid(widthTextInput.value, heightTextInput.value))
      setSizeButton.disable <== isImageLoaded.not()
      children += HBox(ElementsHelper.spacingRegion(), setSizeButton)

  def resizeGrid(width: Option[Int], height: Option[Int]) =
    if width.isEmpty || width.get == 0 || height.isEmpty || height.get == 0 then
      showErrorWithText("Width and height must be more than 0", "Error")
    else
      this.generation.value.foreach(_.newImage(width.get, height.get))

  /**
   * Shows an alert to the user with given header and text.
   * */
  def showErrorWithText(text: String, header: String) =
    val alert = new Alert(Alert.AlertType.Error) :
      title = "Error"
      headerText = header
      contentText = text
    alert.showAndWait()

  def createRatioBox(): VBox =
    new VBox :
      val title = Label("Add Ratio")
      children += title

      def updateTiles() =
        tagsDropdown.items = Try {
          ObservableBuffer.from(generation.value.get.allTags)
        }.getOrElse(ObservableBuffer(""))
        if tagsDropdown.items.value.size() == 0 then
          tagsDropdown.items = ObservableBuffer("")
        tagsDropdown.value.setValue(tagsDropdown.items.value.get(0))

      generation.onChange((_, _, _) => {
        updateTiles()
      })


      val tagsDropdown = new ComboBox[String] :
        prefWidth = 100
        items = Try {
          ObservableBuffer.from(generation.value.get.allTags)
        }.getOrElse(ObservableBuffer(""))
        value.setValue(items.value.get(0))
      val ratioDropdown = new ComboBox[Ratio] :
        items = ObservableBuffer.from(Ratio.values)
        value.setValue(items.value.get(0))
      children += HBox(tagsDropdown, ElementsHelper.spacingRegion(), ratioDropdown)
      val addRatioButton = ElementsHelper.normalButton("Add", () => {
        generation.value.foreach(_.addRatio(tagsDropdown.value.getValue, ratioDropdown.value.getValue))
        generation.value.foreach(_.clearImage())
      })
      addRatioButton.disable <== isImageLoaded.not()
      val buttonRow = new HBox(ElementsHelper.spacingRegion(), addRatioButton) :
        margin = Insets(10, 0, 0, 0)
      children += buttonRow

  def createGenerationMethodBox() =
    new VBox :
      styleClass += "section-background"
      padding = Insets(5)
      val dropdown = new ComboBox[methodSelectionChoices] :
        margin = Insets(0, 0, 10, 0)
        prefWidth = 10000
        items = ObservableBuffer.from(methodSelectionChoices.values)
        value.setValue(items.value.get(0))
        value.onChange((value, _, _) => {
          generationFilesChooser.updateContent(value.value)
        })
      val dropdownLabel = new Label("Generation method")
      generationFilesChooser.updateContent(dropdown.value.value)
      dropdownLabel.labelFor = dropdown

      val loadButton = ElementsHelper
        .normalButton("Load", e => loadGeneration(dropdown.getValue))

      children ++= Seq(dropdownLabel, dropdown, generationFilesChooser.getContent, loadButton)

  /**
   * Load the generation by selected method. It also add needed listeners to the generation to handle image updating. 
   * */
  def loadGeneration(method: methodSelectionChoices) =
    try
      method match
        case methodSelectionChoices.simpleWFC =>
          val readedRules = Reader
            .readFiles(generationFilesChooser.selectedTilesetFolder.value, generationFilesChooser.selectedRulesPath
              .value)
          this.generation.setValue(Some(SimpleWFC(readedRules, throwErrorToMainThread)))
          savedTilesetFolderPath = generationFilesChooser.selectedTilesetFolder.value
        case methodSelectionChoices.autoGeneration =>
          val readedRules = Reader
            .readImage(generationFilesChooser.selectedImagePath.value, generationFilesChooser.tileSize.value)
          savedTilesetFolderPath = System.getProperty("user.home") + "/.proceduralimg"
          this.generation.setValue(Some(SimpleWFC(readedRules, throwErrorToMainThread)))
      ElementsHelper.clearOpenedFiles()
      this.drawer.setGeneration(this.generation.value)
      this.drawer.setPathToTiles(savedTilesetFolderPath)
      this.generation.value.foreach(_.image.onChange((x, _, _) => {
        imageGridPane.setImages(x.value, savedTilesetFolderPath)
        isImageLoaded.setValue(true)
      }))
      this.imageGridPane
        .setImages(generation.value.get.image.value, savedTilesetFolderPath)
      this.isImageLoaded.setValue(true)
    catch
      case e: Exception => showErrorWithText(e.getMessage, "Error")

  def throwErrorToMainThread(e: Exception) =
    showErrorWithText(e.getMessage, "Error")
    generation.value.foreach(_.clearImage())

  def createMenuBar(stage: Stage, zoomFunction: Double => Unit, themeChanger: () => Unit) =
    new MenuBar() :
      useSystemMenuBar = true
      val viewMenu = new Menu("View") :
        val zoomIn = new MenuItem("Zoom In") :
          onAction = e => {
            zoomFunction(0.2)
          }
        val zoomOut = new MenuItem("Zoom Out") :
          onAction = e => {
            zoomFunction(-0.2)
          }
        items = Seq(zoomIn, zoomOut)
      val mainMenu = new Menu("Main") :
        val quit = new MenuItem("Quit") :
          onAction = e => {
            stage.close()
          }
        val themeChangeItem = new MenuItem("ChangeTheme") :
          onAction = e => {
            themeChanger()
          }
        items = Seq(themeChangeItem, quit)
      menus = Seq(mainMenu, viewMenu)

  def canvas() =
    new ZoomableScrollPane(imageGridPane.getImage)

end GUI
