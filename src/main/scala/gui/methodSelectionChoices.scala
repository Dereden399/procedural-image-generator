package gui

enum methodSelectionChoices(text: String):
  override def toString: String = this.text

  case simpleWFC extends methodSelectionChoices("Simple WFC")
  case autoGeneration extends methodSelectionChoices("Autogeneration")