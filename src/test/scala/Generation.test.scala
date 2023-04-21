import generationLogic.{Generation, IncopitableTileToCollapseError, Position, Rule, SimpleWFC}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Try


class GenerationTest extends AnyFlatSpec with Matchers :
  // rules for track.png and track.json
  val rulesForTest = Vector(("track.png@0", Rule("ABA", "AAA", "ABA", "ABA")),
                            ("track.png@1", Rule("AAA", "ABA", "ABA", "ABA")),
                            ("track.png@2", Rule("ABA", "ABA", "AAA", "ABA")),
                            ("track.png@3", Rule("ABA", "ABA", "ABA", "AAA"))).toMap

  // Helper function that generate image same as SimpleWFC (SimpleWFC object collapse one tile every 10 ms, but here we want to do it instantly)
  def generateSimple(generation: Generation) =
    while !generation.isGenerated do
      generation.collapseOne()
    generation.clearBacktrack()

  def checkTilesHor(generation: Generation, width: Int): Boolean =
    if !generation.isGenerated then return false
    generation.image.value.allSquares.grouped(width).toVector.transpose.forall(x => {
      x.dropRight(1).zipWithIndex.forall(squareAndIndexInRow => {
        val rightNeighbour = x(squareAndIndexInRow._2 + 1)
        val ruleRightMain = generation.rules(squareAndIndexInRow._1.imageToShow.head).tilesRight
        val ruleLeftNeighbour = generation.rules(rightNeighbour.imageToShow.head).tilesLeft
        ruleRightMain == ruleLeftNeighbour
      })
    })
  def checkTilesVert(generation: Generation, width: Int): Boolean =
    if !generation.isGenerated then return false
    generation.image.value.allSquares.grouped(width).forall(x => {
      x.dropRight(1).zipWithIndex.forall(squareAndIndexInRow => {
        val rightNeighbour = x(squareAndIndexInRow._2 + 1)
        val ruleDownMain = generation.rules(squareAndIndexInRow._1.imageToShow.head).tilesDown
        val ruleUpNeighbour = generation.rules(rightNeighbour.imageToShow.head).tilesUp
        ruleDownMain == ruleUpNeighbour
      })
    })

  "Generation" should "generate small image without problems" in {
    val wfc = SimpleWFC(rulesForTest)
    assert(
      Try {
        generateSimple(wfc)
      }.isSuccess
      )
  }

  it should "resize the image" in {
    val wfc = SimpleWFC(rulesForTest)
    assert(
      Try {
        wfc.newImage(30, 30)
      }.isSuccess
      )
    assert(
      wfc.image.value.height == 30 && wfc.image.value.width == 30
      )
  }

  it should "generate big images as well" in {
    val wfc = SimpleWFC(rulesForTest)
    wfc.newImage(30, 30)
    assert(
      Try {
        generateSimple(wfc)
      }.isSuccess
      )
  }
  it should "place correct tiles horizontally" in {
    val wfc = SimpleWFC(rulesForTest)
    generateSimple(wfc)
    assert(
      checkTilesHor(wfc, wfc.image.value.width)
    )
  }
  it should "place correct tiles vertically" in {
    val wfc = SimpleWFC(rulesForTest)
    generateSimple(wfc)
    assert(
      checkTilesVert(wfc, wfc.image.value.width)
    )
  }
  it should "use backtracking to avoid contradictions" in {
    var wasUsed = false
    val wfc = new SimpleWFC(rulesForTest, rngSeed = Some(123)) :
      override def collapseFromBacktracking(): Unit =
        wasUsed = true
        super.collapseFromBacktracking()
    wfc.newImage(100, 100)
    val a = Try {
      generateSimple(wfc)
    }
    assert(
      a.isSuccess
      )
    assert(wasUsed)
  }
  it should "throw an exception when draw an incopitable tile" in {
    val wfc = new SimpleWFC(rulesForTest)
    wfc.collapseWith("track.png@0", Position(0, 0))
    intercept[IncopitableTileToCollapseError] {
      wfc.collapseWith("track.png@0", Position(0, 1))
    }
  }


end GenerationTest
