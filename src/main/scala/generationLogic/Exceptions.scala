package generationLogic

class ReaderException(text: String) extends Exception(text)

case class InvalidTilesetFolderError() extends ReaderException("Invalid tileset folder")

case class InvalidRuleFileError() extends ReaderException("Invalid rule file")

case class InvalidImageFileError() extends ReaderException("Invalid image file selected")


case class InvalidImageSize(size: Int) extends ReaderException(s"Image must consists of tiles ${size}x${size} pixels")

case class MissingTileNameError() extends ReaderException("Missing tileName in rule")

case class MissingBorderError() extends ReaderException("Missing border in rule")

case class InvalidBorderError() extends ReaderException("Invalid border in rule")

case class TooMuchDifferentTilesError() extends ReaderException("Too much different tiles")

class GenerationException(text: String) extends Exception(text)

case class IncopitableTileToCollapseError() extends GenerationException("Cannot collapse with incopitable tile")


case class EmptyBacktrackingError() extends GenerationException("Backtracking is empty")


case class InvalidTilesetError() extends GenerationException("Invalid tileset for selected image size")

case class ContradictionTileSelectedError() extends GenerationException("Selected tile leads to contradiction")

case class ContradictionError() extends GenerationException("Contradiction found")