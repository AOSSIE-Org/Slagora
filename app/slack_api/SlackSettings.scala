package slack_api

case class SlackSettings (
                         baseUrl: String,
                         openDialog: String,
                         sendEphemeral: String,
                         sendMsg: String,
                         updateMsg: String,
                         deleteMsg: String,
                         deleteScheduleMsg: String,
                         scheduleMsg: String,
                         pinMsg: String,
                         removePinMsg: String
                         )
