package uk.ac.wellcome.platform.stacks.common.models

import uk.ac.wellcome.platform.sierra.models.Hold

import scala.util.{Failure, Success, Try}

sealed trait Identifier[T] {
  val value: T

  override def toString: String = value.toString
}

case class SierraItemIdentifier(value: Long) extends Identifier[Long]

object SierraItemIdentifier {
  def createFromString(id: String): SierraItemIdentifier = Try {
    id.toLong
  } match {
    case Success(v) => SierraItemIdentifier(v)
    case Failure(e) => throw new Exception("Failed to create SierraItemIdentifier", e)
  }

  def createFromHold(hold: Hold): SierraItemIdentifier =
    createFromString(
      hold.getRecord.split("/").last
    )
}

case class CatalogueItemIdentifier(value: String) extends Identifier[String]

case class StacksItemIdentifier(
                                 catalogueId: CatalogueItemIdentifier,
                                 sierraId: SierraItemIdentifier
                               )  extends Identifier[String] {
  override val value: String = catalogueId.value
}

case class StacksWorkIdentifier(value: String) extends Identifier[String]