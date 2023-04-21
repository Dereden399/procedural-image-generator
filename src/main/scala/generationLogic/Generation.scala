package generationLogic


import scalafx.beans.property.ObjectProperty

import scala.collection.mutable
import scala.util.{Random, Try}

/**
 * Generation trait is a base for every generation sub classes. Defines all generation logic,
 * only generateImage() function is abstract.
 *
 * @param rules   rules for the generation in format "tileNameAndRotation" -> "Rule".
 * @param rngSeed seed for random, None by default.
 * */
trait Generation(val rules: Map[String, Rule], rngSeed: Option[Int] = None):
  val toPropagate = mutable.Queue[Square]()
  val backtracking = mutable.ArrayDeque[(Vector[(Vector[String], Vector[String], Position)], Position)]()

  /**
   * Main image, has [[Grid]] type. Uses scalaFX [[ObjectProperty]] for subscriptions.
   * */
  var image = ObjectProperty[Grid](Grid(generation = this, rngSeed))
  var rng =
    if rngSeed.isDefined then Random(rngSeed.get) else Random()
  // used for backtracking
  private var collapseNumber = 0

  /**
   * Generate full image, abstract and must be implemented by sub classes.
   * */
  def generateImage(): Unit

  /**
   * All tiles this generation has, is decided by rules.
   *
   * @return All tiles names.
   * */
  def allTiles = rules.keys.toVector

  /**
   * All tags generation has
   *
   * @return All tags
   * */
  def allTags = rules.values.flatMap(_.tags).toVector.distinct

  /**
   * Vector containing all tile names, where every tile can be found its ''weight'' times.
   *
   * @return All tile names.
   * */
  def allTilesWithWeights: Vector[String] =
    val tilesWithWeights = rules.map(x => (x._1, x._2.weight))
    tilesWithWeights.flatMap(x => Seq.fill(x._2)(x._1)).toVector

  /**
   * Collapse randomly selected [[Square]] with minimal entropy.
   * */
  def collapseOne(): Unit =
    if !this.isGenerated then
      val minEntropySquare =
        val uncollapsedSquares = this.image.value.allSquares.filter(!_.isCollapsed)
        val minEntropy = uncollapsedSquares.minBy(_.entropy).entropy
        val squaresWithMinEntropy = uncollapsedSquares.filter(_.entropy == minEntropy)
        squaresWithMinEntropy(rng.nextInt(squaresWithMinEntropy.size))
      collapseOne(minEntropySquare)


  /**
   * Collapse selected tile.
   *
   * @param squareToCollapse a square to collapse.
   * */
  def collapseOne(squareToCollapse: Square): Unit =
    if !this.isGenerated then
      if collapseNumber == 0 then
        // state should be added to backtracking
        var backupGrid = this.image.value.allSquares.map(_.informationToCopy)
        val leftVariants = squareToCollapse.collapse()
        backupGrid = backupGrid.map(x => if x._3 == squareToCollapse.position then (x._1, leftVariants, x._3) else x)
        if backtracking.size >= 20 then
          backtracking.removeLast(true)
        backtracking.prepend((backupGrid, squareToCollapse.position))
      else
      // should not be added to backtracking
        squareToCollapse.collapse()
      // for bigger grid states will be added to backtracking less frequently
      collapseNumber = (collapseNumber + 1) % math
        .max((math.max(this.image.value.height, this.image.value.width) / 5), 1)

      try
        while toPropagate.nonEmpty do
          val square = toPropagate.dequeue()
          square.propagate()
      catch
        case e =>
          var result = Try {
            collapseFromBacktracking()
          }
          while result.isFailure && backtracking.nonEmpty do
            result = Try {
              collapseFromBacktracking()
            }
          if backtracking.isEmpty then throw new EmptyBacktrackingError()

  /**
   * Collapse tile from backtracking.
   * */
  def collapseFromBacktracking() =
    val tuple = backtracking.removeHead(false)
    if tuple._1.find(x => x._3 == tuple._2).get._2.isEmpty then throw new ContradictionError()
    tuple._1.foreach { case (toShow, possible, pos) => {
      val square = this.image.value.elementAt(pos)
      square.imageToShow.clear()
      square.imageToShow.addAll(toShow)
      square.possibleTiles = possible
    }
    }
    collapseNumber = (collapseNumber + 3) % math.max((math.max(this.image.value.height, this.image.value.width) / 5), 1)
    collapseOne(this.image.value.elementAt(tuple._2))

  /**
   * Is the image fully collapsed.
   * */
  def isGenerated = this.image.value.allSquares.forall(_.isCollapsed)

  /**
   * Collapse [[Square]] on [[Position]] with tile ''tileName'', throws an error if square on that position does not contain selected tile.
   * Used by [[Drawer]] for drawing purposes
   *
   * @param tileName Name of the tile to be collapsed with.
   * @param pos      Position of the square.
   * */
  def collapseWith(tileName: String, pos: Position) =
    val squareToCollapse = this.image.value.elementAt(pos)
    if !squareToCollapse.possibleTiles
      .contains(tileName) then throw new IncopitableTileToCollapseError()
    val backupGrid = this.image.value
    squareToCollapse.collapse(tileName)
    try
      while toPropagate.nonEmpty do
        val square = toPropagate.dequeue()
        square.propagate()
    catch
      case e =>
        this.image.setValue(backupGrid)
        throw new ContradictionTileSelectedError()

  /**
   * Clears the image. That is, replace the image with the new Grid with the same size.
   * */
  def clearImage() =
    this.image.setValue(Grid(this.image.value.width, this.image.value.height, this))

  /**
   * Creates new image. That is, replace the image with the new Grid with selected size.
   *
   * @param width  Width of the new image.
   * @param height Height of the new image.
   * */
  def newImage(width: Int, height: Int) =
    this.image.setValue(Grid(width, height, this))

  /**
   * Add ratio for the all tiles with selected tag.
   *
   * @param tag   Tag, for which ratio will be added.
   * @param ratio Ratio to add.
   * */
  def addRatio(tag: String, ratio: Ratio) =
    rules.values.foreach(x => if x.tags.contains(tag) then x.weight = (ratio.getCoefficient / x.tags.length) max 1)

  /**
   * Clears the backtrack.
   * */
  def clearBacktrack() =
    this.backtracking.clear()

/**
 * Companion object for the Generation trait.
 * */
object Generation:
  /**
   * Possible tiles, that a neighbour can have.
   *
   * @param parent    Parent square.
   * @param neighbour Neighbour, for which possible tiles are calculated.
   * @return Vector, containing all tile names.
   * */
  def tilesThatCouldBe(parent: Square, neighbour: Square): Vector[String] =
    val parentTiles = parent.possibleTiles
    val directionOfNeighbour: Direction =
      if parent.position.x > neighbour.position.x then Direction.Left
      else if parent.position.x < neighbour.position.x then Direction.Right
      else if parent.position.y > neighbour.position.y then Direction.Up
      else Direction.Down
    val rules = neighbour.getGeneration.rules

    val parentSideRules =
      directionOfNeighbour match
        case Direction.Up => rules.toVector.filter(x => parentTiles.contains(x._1)).map(_._2.tilesUp)
        case Direction.Down => rules.toVector.filter(x => parentTiles.contains(x._1)).map(_._2.tilesDown)
        case Direction.Right => rules.toVector.filter(x => parentTiles.contains(x._1)).map(_._2.tilesRight)
        case Direction.Left => rules.toVector.filter(x => parentTiles.contains(x._1)).map(_._2.tilesLeft)

    var ans = Set[String]()

    // very hard to read, so here is an explanation: for every possible tile in neighbour's possible tiles list check, is it allowed by the parent's rules.
    // If it is allowed, then the algorithm checks if possible tile can be found in parent 'NOT' list or vice versa.
    directionOfNeighbour match
      case Direction.Up =>
        for possibleTile <- neighbour.possibleTiles do
          if parentSideRules.contains(rules(possibleTile).tilesDown) && (!(parentTiles
            .size == 1) || (!rules(parentTiles.head)
            .tilesUpNot.exists(x => possibleTile.startsWith(x)) && !rules(possibleTile).tilesDownNot
            .exists(x => parentTiles.head.startsWith(x)))) then ans += possibleTile
      case Direction.Right =>
        for possibleTile <- neighbour.possibleTiles do
          if parentSideRules.contains(rules(possibleTile).tilesLeft) && (!(parentTiles
            .size == 1) || (!rules(parentTiles.head)
            .tilesRightNot.exists(x => possibleTile.startsWith(x)) && !rules(possibleTile).tilesLeftNot
            .exists(x => parentTiles.head.startsWith(x)))) then ans += possibleTile
      case Direction.Down =>
        for possibleTile <- neighbour.possibleTiles do
          if parentSideRules.contains(rules(possibleTile).tilesUp) && (!(parentTiles
            .size == 1) || (!rules(parentTiles.head)
            .tilesDownNot.exists(x => possibleTile.startsWith(x)) && !rules(possibleTile).tilesUpNot
            .exists(x => parentTiles.head.startsWith(x)))) then ans += possibleTile
      case Direction.Left =>
        for possibleTile <- neighbour.possibleTiles do
          if parentSideRules.contains(rules(possibleTile).tilesRight) && (!(parentTiles
            .size == 1) || (!rules(parentTiles.head)
            .tilesLeftNot.exists(x => possibleTile.startsWith(x)) && !rules(possibleTile).tilesRightNot
            .exists(x => parentTiles.head.startsWith(x)))) then ans += possibleTile

    ans.toVector

