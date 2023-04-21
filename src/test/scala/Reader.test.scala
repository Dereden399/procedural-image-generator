import generationLogic.*
import gui.Reader
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.Buffer

class ReaderTest extends AnyFlatSpec with Matchers :
  val tilesFolder = "src/test/testingAssets"

  "readTiles" should "return all png and jpg tiles from valid folder" in {
    val result = Reader.readTiles(tilesFolder)
    assert(result.length == 9)
    assert(result.contains("water.png") && result.contains("waterCornerGround.png") && result
      .contains("waterSideGround.jpg"))
  }

  it should "throw InvalidTilesetFolderError when folder is invalid" in {
    intercept[InvalidTilesetFolderError] {
      val result = Reader.readTiles("src/notExist")
    }
  }

  it should "return 0 tiles when folder does not contain png and jpg" in {
    val result = Reader.readTiles("src/main")
    assert(result.length == 0)
  }

  "readRules" should "return rules if the file is valid including rotation" in {
    val result = Reader.readRules(tilesFolder + "/valid.json")
    assert(result.size == 9)
  }

  it should "return rules even if the file contains extra unnecessary fields" in {
    val result = Reader.readRules(tilesFolder + "/validWithOthers.json")
    assert(result.size == 4)
  }

  it should "throw MissingTileNameError" in {
    intercept[MissingTileNameError] {
      val result = Reader.readRules(tilesFolder + "/missingName.json")
    }
  }

  it should "throw MissingBorderError" in {
    intercept[MissingBorderError] {
      val result = Reader.readRules(tilesFolder + "/missingBorder.json")
    }
  }

  it should "throw InvalidBorderError" in {
    intercept[InvalidBorderError] {
      val result = Reader.readRules(tilesFolder + "/invalidBorder.json")
    }
  }

  it should "throw ReaderException in case of unknown errors" in {
    intercept[ReaderException] {
      val result = Reader.readRules(tilesFolder + "/readerException.json")
    }
  }

  "readFiles" should "return the map when rules are valid" in {
    val result = Reader.readFiles(tilesFolder, tilesFolder + "/valid.json")
    assert(result.size == 5)
    assert(result.contains("111.png@3") && result.contains("222.png"))
  }

  "readImages" should "read an image correctly" in {
    val result = Reader.readImage(tilesFolder + "/track.png", 10)
    assert(result.size == 1)
  }

  it should "read an image with same tiles correctly" in {
    val result = Reader.readImage(tilesFolder + "/testImageSameTiles.png", 64)
    assert(result.size == 3)
  }

  it should "read a big image correctly" in {
    val result = Reader.readImage(tilesFolder + "/testImage.png", 64)
    assert(result.size == 10)
  }

end ReaderTest
