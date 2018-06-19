package services

import javax.inject.{Inject, Singleton}

import daos.AccountBalanceDAO
import dtos.AccountBalance
import dtos.AccountBalance.{AccountId, BalanceAmt}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class FundsService @Inject()(accountBalanceDAO: AccountBalanceDAO, dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  def moveFromTo(fromAccount: AccountId, toAccount: AccountId, amount: Long): Future[Either[String, TransferResult]] = {
    val tx = for {
      _ <- if (amount <= 0) DBIO.failed(InvalidAmountException) else DBIO.successful(())
      _ <- if (fromAccount == toAccount) DBIO.failed(SameAccountException) else DBIO.successful(())
      r <- {
        val dml =
          if (fromAccount > toAccount) // total ordering to avoid deadlock
            accountBalanceDAO.creditDML(fromAccount, amount) zip accountBalanceDAO.debitDML(toAccount, amount)
          else accountBalanceDAO.debitDML(toAccount, amount) zip accountBalanceDAO.creditDML(fromAccount, amount)
        dml.asTry
      }
      _ <- r match {
        case Success((1, 1))                                            => DBIO.successful(())
        case Success(_)                                                 => DBIO.failed(InvalidAccountException)
        case Failure(ex) if ex.getMessage.toLowerCase.contains("check") => DBIO.failed(InsufficientAmountException)
        case Failure(ex)                                                => DBIO.failed(ex)
      }
    } yield ()

    db.run(tx.transactionally)
      .map((_: Unit) => Right(TransferResult("ok")))
      .recover {
        case ex: FundsTransferException =>
          Logger.debug("moveFromTo failed: " + ex.getMessage)
          Left("computer says no")
        case ex =>
          Logger.error("moveFromTo failed with attention needed: " + ex.getMessage)
          Left("computer says no")
      }
  }

  def dump: Future[DumpResult] =
    db.run(accountBalanceDAO.findAll)
      .map((abSeq: Seq[AccountBalance]) => DumpResult(abSeq.map((ab: AccountBalance) => ab.accountId -> ab.balance)))
}

case class TransferResult(value: String) extends AnyVal
case class DumpResult(value: Seq[(AccountId, BalanceAmt)])

abstract class FundsTransferException(message: String) extends Throwable(message)
case object InvalidAmountException extends FundsTransferException("invalid amount")
case object InsufficientAmountException extends FundsTransferException("insufficient amount")
case object InvalidAccountException extends FundsTransferException("invalid account")
case object SameAccountException extends FundsTransferException("same account")
