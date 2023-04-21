package generationLogic

case class Rule(tilesRight: String,
                tilesUp: String,
                tilesDown: String,
                tilesLeft: String,
                tilesRightNot: Vector[String] = Vector(),
                tilesUpNot: Vector[String] = Vector(),
                tilesDownNot: Vector[String] = Vector(),
                tilesLeftNot: Vector[String] = Vector(),
                tags: Vector[String] = Vector(),
                var weight: Int = 1,
               )

case class RuleWithTileName(tileName: Option[String],
                            tilesRight: Option[String],
                            tilesUp: Option[String],
                            tilesDown: Option[String],
                            tilesLeft: Option[String],
                            tilesUpNot: Option[Vector[String]],
                            tilesRightNot: Option[Vector[String]],
                            tilesDownNot: Option[Vector[String]],
                            tilesLeftNot: Option[Vector[String]],
                            tags: Option[Vector[String]],
                            weight: Option[Int],
                            shouldBeRotated: Option[Boolean]):

  /**
   * Convert this object to Vector, that can be easily converted to map, where for every tile there are a rule for it.
   * */
  def convertToTuples(): Vector[(String, Rule)] =
    if tileName.isEmpty then throw new MissingTileNameError
    if tilesRight.isEmpty || tilesUp.isEmpty || tilesDown.isEmpty || tilesLeft
      .isEmpty then throw new MissingBorderError()
    if tilesRight.get.length != 3 || tilesDown.get.length != 3 || tilesLeft.get.length != 3 || tilesUp.get
      .length != 3 then throw new InvalidBorderError()
    if this.shouldBeRotated.getOrElse(true) then
      val normal = (tileName.get + "@0", Rule(tilesRight.get, tilesUp.get, tilesDown.get, tilesLeft.get, tilesRightNot
        .getOrElse(Vector[String]()).map(rotateTileWithExistingRotation(_, 0)), tilesUpNot.getOrElse(Vector[String]())
                                                .map(rotateTileWithExistingRotation(_, 0)), tilesDownNot
                                                .getOrElse(Vector[String]())
                                                .map(rotateTileWithExistingRotation(_, 0)), tilesLeftNot
                                                .getOrElse(Vector[String]())
                                                .map(rotateTileWithExistingRotation(_, 0)), tags
                                                .getOrElse(Vector[String]()), weight.getOrElse(1)))
      val rightRotated = (tileName.get + "@1", Rule(tilesUp.get, tilesLeft.get.reverse, tilesRight.get
        .reverse, tilesDown.get, tilesUpNot
                                                      .getOrElse(Vector[String]())
                                                      .map(rotateTileWithExistingRotation(_, 1)), tilesLeftNot
                                                      .getOrElse(Vector[String]())
                                                      .map(rotateTileWithExistingRotation(_, 1)), tilesRightNot
                                                      .getOrElse(Vector[String]())
                                                      .map(rotateTileWithExistingRotation(_, 1)), tilesDownNot
                                                      .getOrElse(Vector[String]())
                                                      .map(rotateTileWithExistingRotation(_, 1)), tags
                                                      .getOrElse(Vector[String]()), weight.getOrElse(1)))
      val downRotated = (tileName.get + "@2", Rule(tilesLeft.get.reverse, tilesDown.get.reverse, tilesUp.get
        .reverse, tilesRight.get.reverse, tilesLeftNot
                                                     .getOrElse(Vector[String]())
                                                     .map(rotateTileWithExistingRotation(_, 2)), tilesDownNot
                                                     .getOrElse(Vector[String]())
                                                     .map(rotateTileWithExistingRotation(_, 2)), tilesUpNot
                                                     .getOrElse(Vector[String]())
                                                     .map(rotateTileWithExistingRotation(_, 2)), tilesRightNot
                                                     .getOrElse(Vector[String]())
                                                     .map(rotateTileWithExistingRotation(_, 2)), tags
                                                     .getOrElse(Vector[String]()), weight.getOrElse(1)))
      val leftRotated = (tileName.get + "@3", Rule(tilesDown.get.reverse, tilesRight.get, tilesLeft.get, tilesUp.get
        .reverse, tilesDownNot
                                                     .getOrElse(Vector[String]())
                                                     .map(rotateTileWithExistingRotation(_, 3)), tilesRightNot
                                                     .getOrElse(Vector[String]())
                                                     .map(rotateTileWithExistingRotation(_, 3)), tilesLeftNot
                                                     .getOrElse(Vector[String]())
                                                     .map(rotateTileWithExistingRotation(_, 3)), tilesUpNot
                                                     .getOrElse(Vector[String]())
                                                     .map(rotateTileWithExistingRotation(_, 3)), tags
                                                     .getOrElse(Vector[String]()), weight.getOrElse(1)))
      Vector(normal, rightRotated, downRotated, leftRotated)
    else
      Vector((tileName.get, Rule(tilesRight.get, tilesUp.get, tilesDown.get, tilesLeft.get, tilesRightNot
        .getOrElse(Vector[String]()), tilesUpNot.getOrElse(Vector[String]()), tilesDownNot
                                   .getOrElse(Vector[String]()), tilesLeftNot
                                   .getOrElse(Vector[String]()), tags
                                   .getOrElse(Vector[String]()), weight.getOrElse(1))))

  def rotateTileWithExistingRotation(name: String, rotation: Int = 0): String =
    if name.split("@").length < 2 then name
    else
      val existingRotation = name.split("@")(1).toInt
      name.split("@")(0) + "@" + (existingRotation + rotation) % 4
