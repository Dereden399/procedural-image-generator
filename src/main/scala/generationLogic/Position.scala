package generationLogic


case class Position(x: Int, y: Int):
  def neighbourPosition(direction: Direction): Position =
    direction match
      case Direction.Up => Position(x, y - 1)
      case Direction.Right => Position(x + 1, y)
      case Direction.Down => Position(x, y + 1)
      case Direction.Left => Position(x - 1, y)
