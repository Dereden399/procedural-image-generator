package gui

import scalafx.beans.property.{IntegerProperty, StringProperty}
import scalafx.geometry.Insets
import scalafx.scene.control.Label
import scalafx.scene.layout.{HBox, VBox}
import scalafx.stage.{DirectoryChooser, FileChooser}

import java.io.File

/**
 * Class that is used to handle all UI parts of user files input. Generates a content box for the UI.
 * */
class GenerationFilesChooser:
  private val content = VBox()
  var selectedTilesetFolder = StringProperty("")
  var selectedRulesPath = StringProperty("")
  var selectedImagePath = StringProperty("")
  var tileSize = IntegerProperty(16)

  def getContent = content

  /**
   * Update the content by selected generation method
   * */
  def updateContent(method: methodSelectionChoices) =
    selectedTilesetFolder.value = ""
    selectedRulesPath.value = ""
    selectedImagePath.value = ""
    tileSize.value = 16
    method match
      case methodSelectionChoices.simpleWFC => updateSimpleWFC()
      case methodSelectionChoices.autoGeneration => updateAutoGeneration()

  /**
   * Update the content to simple wave function collapse files input
   * */
  private def updateSimpleWFC() =
    val tilesetSelction = tilesetSelectionBox()
    val tilesetFolderLabel = new Label :
      margin = Insets(-10, 0, 10, 0)
      text <== selectedTilesetFolder
    val ruleSelection = ruleSelectionBox()
    val rulePathLabel = new Label :
      margin = Insets(-10, 0, 10, 0)
      text <== selectedRulesPath
    content.children = Seq(tilesetSelction, tilesetFolderLabel, ruleSelection, rulePathLabel)

  /**
   * UI box with file input for choosing a tileset folder
   * */
  private def tilesetSelectionBox() = new HBox :
    children += new Label("Tileset folder")
    val dirChooser = new DirectoryChooser :
      val mainFolder = System.getProperty("user.home")
      initialDirectory = new File(mainFolder)
      title = "Path to tileset folder"
    val openChooserButton = ElementsHelper.normalButton("Select", e => {
      val selectedFolder = dirChooser.showDialog(this.getScene.getWindow)
      if selectedFolder != null then
        selectedTilesetFolder.value = selectedFolder.getAbsolutePath
        dirChooser.initialDirectory = File(selectedFolder.getAbsolutePath)
    })
    children += ElementsHelper.spacingRegion()
    children += openChooserButton

  /**
   * UI box with file input for choosing a rule file
   * */
  private def ruleSelectionBox() = new HBox :
    children += Label("Rule's path")
    val fileChooser = new FileChooser :
      val mainFolder = System.getProperty("user.home")
      initialDirectory = new File(mainFolder)
      title = "Path to rules.json"
      extensionFilters.addAll(new FileChooser.ExtensionFilter("JSON File", "*.json"))
    val openChooserButton = ElementsHelper.normalButton("Select", e => {
      val selectedFile = fileChooser.showOpenDialog(this.getScene.getWindow)
      if selectedFile != null then
        selectedRulesPath.value = selectedFile.getAbsolutePath
        fileChooser.initialDirectory = File(selectedFile.getParent)
    })
    children += ElementsHelper.spacingRegion()
    children += openChooserButton

  /**
   * Update the content to autogeneration files input
   * */
  private def updateAutoGeneration() =
    val imageSelection = imageSelectionBox()
    val imageFileLabel = new Label :
      margin = Insets(-10, 0, 10, 0)
      text <== selectedImagePath
    val tileSizeRow = tileSizeBox()
    content.children = Seq(imageSelection, imageFileLabel, tileSizeRow)

  /**
   * UI box with numeric text field for writing a tile size.
   * */
  private def tileSizeBox() = new HBox :
    val numberField = new NumericTextField :
      maxWidth = 40
      text = tileSize.value.toString
      text.onChange((newValue, _, _) => {
        if newValue.value.toIntOption.isDefined && newValue.value.toIntOption.get > 0 then
          tileSize.value = newValue.value.toInt
        else text = tileSize.value.toString
      })
    children += Label("Tile size")
    children += ElementsHelper.spacingRegion()
    children += numberField

  /**
   * UI box with file input for choosing an image file
   * */
  private def imageSelectionBox() = new HBox :
    children += Label("Image's path")
    val fileChooser = new FileChooser :
      val mainFolder = System.getProperty("user.home")
      initialDirectory = new File(mainFolder)
      title = "Path to image"
      extensionFilters.addAll(new FileChooser.ExtensionFilter("Image Files", Seq("*.png", "*.jpg")))
    val openChooserButton = ElementsHelper.normalButton("Select", e => {
      val selectedFile = fileChooser.showOpenDialog(this.getScene.getWindow)
      if selectedFile != null then
        selectedImagePath.value = selectedFile.getAbsolutePath
        fileChooser.initialDirectory = File(selectedFile.getParent)
    })
    children += ElementsHelper.spacingRegion()
    children += openChooserButton
end GenerationFilesChooser
