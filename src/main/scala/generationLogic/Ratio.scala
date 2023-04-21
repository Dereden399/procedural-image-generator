package generationLogic

/**
 * Enumarator describing a ratio, has a coefficient and a text description to be displayed
 * */
enum Ratio(coef: Int, text: String):
  def getCoefficient = this.coef

  override def toString: String = this.text

  case Normal extends Ratio(1, "Normal")
  case Big extends Ratio(10, "Big")
  case Huge extends Ratio(100, "Huge")
