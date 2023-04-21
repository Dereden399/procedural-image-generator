package generationLogic

import scalafx.collections.ObservableBuffer

import scala.collection.mutable.Buffer
import scala.util.Random

/**
 * Square class represents a cell on a grid, it has own image to show and stores all possible tiles it can be collapsed with.
 * */
case class Square(grid: Grid, val position: Position, rngSeed: Option[Int] = None):
  val rng =
    if rngSeed.isDefined then Random(rngSeed.get)
    else Random()
  var imageToShow: ObservableBuffer[String] = ObservableBuffer()
  var possibleTiles = grid.generation.allTilesWithWeights

  def informationToCopy = (imageToShow.toVector, possibleTiles, position)
  def entropy = possibleTiles.size

  def getGeneration = grid.generation

  /**
   * Collapse this square with random possible tile. Adds this square to propagate queue.
   *
   * @return Vector containing possible tiles without the tile it was collapsed
   * */
  def collapse() =
    val randomTile = possibleTiles.apply(rng.nextInt(possibleTiles.size))
    val toReturn = possibleTiles.filterNot(_ == randomTile)
    possibleTiles = Vector(randomTile)
    imageToShow += randomTile
    grid.generation.toPropagate.enqueue(this)
    toReturn

  /**
   * Collapse this square with selected tile name. Throws an error if given tile name is not possible.
   *
   * @param tileName Name of the tioe to collapse with.
   * @return Vector containing possible tiles without the tile it was collapsed
   * */
  def collapse(tileName: String) =
    if !possibleTiles.contains(tileName) then throw new IncopitableTileToCollapseError()
    val toReturn = possibleTiles.filterNot(_ == tileName)
    possibleTiles = Vector(tileName)
    imageToShow += tileName
    grid.generation.toPropagate.enqueue(this)
    toReturn

  /**
   * Propagate changes to the neighbours. That is, filter neighbours possible tiles to match this square's possible tiles.
   * If neighbour possible tiles were filtered, adds the neighbour to propagate queue.
   * */
  def propagate(): Unit =
    for neighbour <- neighbours.filter(!_.isCollapsed) do
      val tilesThatCouldBe: Vector[String] = Generation.tilesThatCouldBe(this, neighbour)
      if tilesThatCouldBe.isEmpty then throw ContradictionError()
      // we don't need to check are the tilesThatCouldBe the same as neighbour possible tiles, since the Generation.tilesThatCouldBe filters neighbour possible tiles
      // and does not add anything  
      if tilesThatCouldBe.size != neighbour.possibleTiles.size then
        neighbour.possibleTiles = tilesThatCouldBe
        grid.generation.toPropagate.enqueue(neighbour)

  /**
   * All neighbours of this square
   * */
  def neighbours = grid.neighbours(this.position)

  /**
   * The tile is considered to be collapsed, if its image to show is not empty
   * */
  def isCollapsed =
    this.imageToShow.nonEmpty

end Square
