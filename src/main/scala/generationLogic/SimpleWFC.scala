package generationLogic

import scalafx.Includes.eventClosureWrapperWithZeroParam
import scalafx.animation.{KeyFrame, Timeline}
import scalafx.application.Platform
import scalafx.util.Duration

/**
 * Generation subclass, just a simple wave function collapse. Uses TimeLine to collapse a tile every 3ms.
 * Tries to generate an image 20 times, then throws an error.
 * @param throwErrorFun Function to be called when all tries are used, i.e tileset is bad/invalid/does not work well with selected image size
 * */
class SimpleWFC(ruleList: Map[String, Rule], throwErrorFun: (e: Exception) => Unit = (e) => {},
                rngSeed: Option[Int] = None) extends Generation(ruleList, rngSeed) :
  var triesCount = 0

  override def generateImage(): Unit =
    val timer = new Timeline() :
      cycleCount = Timeline.Indefinite
      keyFrames = Seq(
        KeyFrame(Duration(2), onFinished = () => {
          if isGenerated then
            clearBacktrack()
            triesCount = 0
            System.gc()
            this.stop()
          else
            try
              collapseOne()
            catch
              // if Exception is thrown, it means, that backtracking stack is empty, so we need to restart the generation
              case e: Exception =>
                System.gc()
                if triesCount < 20 then
                  triesCount += 1
                  clearBacktrack()
                  clearImage()
                else
                  // too much tries, tileset is bad/invalid/does not work well with selected image size
                  triesCount = 0
                  clearBacktrack()
                  this.stop()
                  //run throwErrorFun in main thread
                  Platform.runLater {
                    throwErrorFun(new InvalidTilesetError())
                  }

        })
        )
    timer.play()

end SimpleWFC

