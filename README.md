# Telegram calendar notifications

This project contains source codes for telegram bot: https://telegram.me/calendar_notifications_bot 

Main purpose of this bot is notification about your calendar event, using telegram for notification target. 

You need to set up one or more calendar in `ics`-format.  

## Adding calendar to the bot
You need to send `ics` URL and before notification minutes to the bot. 

In this example we want to get notifications for calendar `https://example.ru/3242352/file.ics`, before `15` minutes for event.
```
https://example.ru/3242352/file.ics 15
```

## Bot commands:
1. `stop` - Stop sending notifications
2. `about` - Get github link to the bot repository
3. `calendars` - Send subscribed calendars
4. `help` - Info, how to use the bot
5. `events` - Send my events

## Run bot:
1. We have pre-build docker images with this bot: https://github.com/users/Hixon10/packages/container/package/telegram-calendar-notifications%2Ftelegram-calendar-notifications
2. Most straightforward way how to run it is configure of settings in `env.list` ( https://github.com/Hixon10/telegram-calendar-notifications/blob/master/env.list ) and run `run.sh` ( https://github.com/Hixon10/telegram-calendar-notifications/blob/master/run.sh )

## Build:
1. Set telegram token via `TELEGRAM_TOKEN` environment variable
2. Set MongoDB url via `MONGODB_URI` environment variable (note! you can use only `mongodb://` protocol, because of GraalVM Native image)
3. Create a GraalVM Native image via `./gradlew dockerBuildNative`

## Run Unit tests:
1. `./gradle clean test`

## Run Integration tests:
1. Set telegram token via `TELEGRAM_TOKEN` environment variable
2. Set MongoDB url via `MONGODB_URI` environment variable (note! you can use only `mongodb://` protocol, because of GraalVM Native image)
3. `./gradle clean integrationTest` 
