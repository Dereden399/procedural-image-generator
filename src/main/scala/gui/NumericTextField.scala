package gui

import scalafx.scene.control.TextField

/**
 * A Text Field, that accepts only numeric inputs.
 * */
class NumericTextField extends TextField :
  text.onChange((_, oldValue, newValue) => {
    if (!newValue.matches("\\d*")) then
      text = newValue.replaceAll("\\D", "")
  })

  def value = this.text.value.toIntOption
