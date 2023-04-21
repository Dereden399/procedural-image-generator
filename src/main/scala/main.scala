import gui.{ElementsHelper, GUI}
import javafx.embed.swing.SwingFXUtils
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.image.Image
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.layout.*
import scalafx.scene.paint.Color.*

import java.awt.Taskbar
import java.awt.Taskbar.Feature
import java.io.FileInputStream

/**
 * Main object. The program starts here
 * */
object Main extends JFXApp3 :

  val gui = GUI()

  def start(): Unit =
    val root = new BorderPane()
    val scene = Scene(parent = root) // Scene acts as a container for the scene graph
    stage = new JFXApp3.PrimaryStage :
      title = "Procedural Images Generator"
      minWidth = 600
      minHeight = 500
      resizable = true
      onCloseRequest = e => {
        ElementsHelper.deleteCache()
      }
    stage.scene = scene // Assigning the new scene as the current scene for the stage

    scene.stylesheets += getClass.getResource("/styles.css").toExternalForm

    val appIcon = new Image(new FileInputStream("src/main/resources/logo.png"))
    stage.icons.addOne(appIcon)
    // set up an icon to the macOSX taskbar
    if Taskbar.isTaskbarSupported then
      val taskbar = Taskbar.getTaskbar
      if taskbar.isSupported(Feature.ICON_IMAGE) then
        val javaImage = SwingFXUtils.fromFXImage(appIcon, null)
        taskbar.setIconImage(javaImage)

    val darkModeCssResource = getClass.getResource("/modena-dark-withStyles.css").toExternalForm

    def changeTheme() =
      if scene.stylesheets.contains(darkModeCssResource) then
        scene.stylesheets.remove(darkModeCssResource)
      else
        scene.stylesheets.add(darkModeCssResource)

    val bottomLine = gui.bottomLine()
    val menu = gui.sideMenu()
    val canvas = gui.canvas()
    val menuBar = gui.createMenuBar(stage, canvas.addScale, changeTheme)

    scene.onKeyPressed = (event: KeyEvent) => {
      if event.isControlDown && (event.code == KeyCode.Add || event.code == KeyCode.Plus || event.code == KeyCode
        .Equals) then
        canvas.addScale(0.2)
      if event.isControlDown && (event.code == KeyCode.Subtract || event.code == KeyCode.Minus) then
        canvas.addScale(-0.2)
    }

    bottomLine.maxWidth <== root.width.subtract(menu.width)

    root.setBottom(bottomLine)
    root.setRight(menu)
    root.setCenter(canvas)
    root.setTop(menuBar)