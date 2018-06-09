package dtos

import dtos.AccountBalance.{AccountId, BalanceAmt}
import play.api.libs.json._

case class AccountBalance(accountId: AccountId, balance: BalanceAmt)

object AccountBalance {
  type AccountId = Long
  type BalanceAmt = Long

  implicit val personFormat: Format[AccountBalance] = Json.format[AccountBalance]
}
