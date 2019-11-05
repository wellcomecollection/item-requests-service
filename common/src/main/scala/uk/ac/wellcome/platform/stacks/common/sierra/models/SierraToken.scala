package uk.ac.wellcome.platform.stacks.common.sierra.models

import io.circe.Decoder

case class SierraToken(accessToken: String, tokenType: String, expiresIn: Int)

object SierraToken {
  implicit val decodeUser: Decoder[SierraToken] =
    Decoder.forProduct3("access_token", "token_type", "expires_in")(
      SierraToken.apply)
}
