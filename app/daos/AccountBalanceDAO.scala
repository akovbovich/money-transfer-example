package daos

import javax.inject.{Inject, Singleton}

import dtos.AccountBalance
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

@Singleton
class AccountBalanceDAO @Inject()(dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig.profile.api._

  private class AccountBalanceTable(tag: Tag) extends Table[AccountBalance](tag, "account_balance") {
    def accountId = column[Long]("account_id", O.PrimaryKey)
    def balance = column[Long]("balance")
    def * = (accountId, balance) <> ((AccountBalance.apply _).tupled, AccountBalance.unapply)
  }

  private lazy val accountBalanceTab = TableQuery[AccountBalanceTable]

  def debitDML(accountId: Long, amount: Long): DBIOAction[Int, NoStream, Effect.Write] =
    sqlu"""
         UPDATE account_balance
         SET    balance = balance + $amount
         WHERE  account_id = $accountId
       """

  def creditDML(accountId: Long, amount: Long): DBIOAction[Int, NoStream, Effect.Write] =
    sqlu"""
         UPDATE account_balance
         SET    balance = balance - $amount
         WHERE  account_id = $accountId
       """

  def findAllQuery: DBIOAction[Seq[AccountBalance], NoStream, Effect.Read] = accountBalanceTab.result
}
