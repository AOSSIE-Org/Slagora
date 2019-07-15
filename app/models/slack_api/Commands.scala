package models.slack_api

// This object holds all slash commands name supported by Slagora
object Commands extends Enumeration {

  type Commands = Value

  val SLAGORA = Value("Slagora")

}
