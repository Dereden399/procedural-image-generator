package generationLogic

import scalafx.collections.ObservableBuffer

/**
 * Squares holder, immutable
 * */
class Grid(val width: Int = 10, val height: Int = 10, val generation: Generation, rngSeed: Option[Int] = None):
  private val elements: Vector[Vector[Square]] =
    this.initialElements.grouped(this.width).toVector.transpose

  def copy(): Grid =
    val ans = Grid(width, height, generation, rngSeed)
    ans.allSquares.foreach(square => {
      val sameSquareNow = this.elementAt(square.position)
      square.possibleTiles = sameSquareNow.possibleTiles
      square.imageToShow =
        sameSquareNow.imageToShow.headOption match
          case Some(value) => ObservableBuffer(value)
          case None => ObservableBuffer[String]()
    })
    ans


  def allSquares = this.elements.flatten

  /**
   * All neighbour squares, does not include diagonal squares, so the max return number is 4ю
   * @param pos Position of the square for which neighbors need to be found.
   * @return Vector, containing square neighbours
   * */
  def neighbours(pos: Position): Vector[Square] =
    Direction.values.flatMap(dir => this.elementAtOption(pos.neighbourPosition(dir))).toVector

  def elementAtOption(position: Position): Option[Square] =
    if this.contains(position) then Some(this.elementAt(position)) else None

  def contains(position: Position): Boolean =
    position.x >= 0 && position.x < width && position.y >= 0 && position.y < height

  def elementAt(position: Position): Square =
    this.elements(position.x)(position.y)

  private def initialElements: Seq[Square] =
    val allPositions = (0 until this.size).map(x => Position(x % this.width, x / this.width))
    allPositions.map(Square(this, _, rngSeed))

  def size = width * height
