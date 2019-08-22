package models.slack_api

object ActionIDs extends Enumeration {

  type ActionIDs = String

  val DELETE = "delete"
  val START_DATE = "startDate"
  val END_DATE = "endDate"
  val FINISH_ELECTION_SETUP = "finish"
  val TRY_VOTING = "tryVoting"
  val SIGN_UP = "signup"
  val GENERAL_SIGN_UP = "signupGeneral"
  val VOTE = "vote"
  val CANDIDATE_SELECTED = "candidateSelected"
  val RESULT = "result"
}
