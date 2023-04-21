package gui

import scalafx.scene.image.WritableImage

/**
 * Wrapper for WritableImage, overrides a hashCode() and equals() functions, so two Images with same dimensions and same pixel colors are considered to be the same.
 * */
case class WritableImageWrapper(image: WritableImage):
  override def hashCode(): Int =
    image.getWidth.hashCode() + image.getHeight.hashCode()

  override def equals(obj: Any): Boolean =
    obj match
      case otherImage: WritableImageWrapper =>
        ElementsHelper.isImagesEqual(this.image, otherImage.image)
      case _ => false
end WritableImageWrapper


