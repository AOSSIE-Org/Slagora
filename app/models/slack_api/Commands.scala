package models.slack_api

// This object holds all slash commands name supported by Slagora
object Commands extends Enumeration {

  type Commands = String

  val HELP = "help"
  val CREATE = "create"

}
