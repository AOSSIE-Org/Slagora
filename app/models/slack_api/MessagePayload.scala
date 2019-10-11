package models.slack_api

import models.security.Team

abstract class MessagePayload (
                          val callbackId: String,
                          val state: String,
                          val team: SlackTeamReference,
                          val user: SlackUserReference,
                          val channel: SlackChannelReference,
                          val action_ts: String,
                          val token: String,
                          val response_url: String,
                          )

