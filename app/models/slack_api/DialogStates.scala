package models.slack_api


object DialogStates extends Enumeration {

  type DialogStates = String

  val NEW_ELECTION = "newElection"
  val ELECTION_DATES = "electionDates"

}
