package controllers

import javax.inject._

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import services.{DumpResult, FundsService}

import scala.concurrent.{ExecutionContext, Future}

class FundsController @Inject()(fundsService: FundsService, cc: MessagesControllerComponents)(
    implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc) {

  val fundsTransferForm: Form[FundsTransferForm] = Form {
    mapping(
      "accfrom" -> longNumber.verifying(min(0L)),
      "accto" -> longNumber.verifying(min(0L)),
      "amt" -> longNumber.verifying(min(0L))
    )(FundsTransferForm.apply)(FundsTransferForm.unapply)
  }

  def moveFundsBetweenAccounts: Action[AnyContent] = Action.async { implicit request =>
    fundsTransferForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(BadRequest(Json.toJson(Json.obj("error" -> errorForm.errorsAsJson))))
      },
      form => {
        fundsService.moveFromTo(form.accountFrom, form.accountTo, form.amount).map {
          case Left(l)  => BadRequest(Json.toJson(Json.obj("error" -> l)))
          case Right(r) => Ok(Json.toJson(Json.obj("res" -> r.value)))
        }
      }
    )
  }

  def dumpAccounts: Action[AnyContent] = Action.async { implicit request =>
    fundsService.dump.map((dr: DumpResult) =>
      Ok(Json.toJson(dr.value.map { case (accId, bal) => Json.obj("account" -> accId, "balance" -> bal) })))
  }
}

case class FundsTransferForm(accountFrom: Long, accountTo: Long, amount: Long)
