package services

import javax.inject.{Inject, Singleton}

import daos.AccountBalanceDAO
import dtos.AccountBalance
import dtos.AccountBalance.{AccountId, BalanceAmt}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FundsService @Inject()(accountBalanceDAO: AccountBalanceDAO, dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  def moveFromTo(fromAccount: Long, toAccount: Long, amount: Long): Future[Either[String, TransferResult]] = {
    val tx = for {
      i <- accountBalanceDAO.creditQuery(fromAccount, amount)
      j <- accountBalanceDAO.debitQuery(toAccount, amount)
      _ <- if (i == 1 && j == 1) DBIO.successful(i + j) else DBIO.failed(new Throwable("Both records must be updated"))
    } yield ()

    db.run(tx.transactionally)
      .map((_: Unit) => Right(TransferResult("ok")))
      .recover {
        case ex =>
          Logger.debug("moveFromTo failed: " + ex.getMessage)
          Left("computer says no")
      }
  }

  def dump: Future[DumpResult] =
    db.run(accountBalanceDAO.findAllQuery)
      .map((abSeq: Seq[AccountBalance]) => DumpResult(abSeq.map((ab: AccountBalance) => ab.accountId -> ab.balance)))
}

case class TransferResult(value: String) extends AnyVal
case class DumpResult(value: Seq[(AccountId, BalanceAmt)])
