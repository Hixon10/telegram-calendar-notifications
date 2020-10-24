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

## Run bot:
1. We have pre-build docker images with this bot: https://github.com/users/Hixon10/packages/container/package/telegram-calendar-notifications%2Ftelegram-calendar-notifications
2. Most straightforward way how to run it is configure of settings in `env.list` (file in this repo) and run `run.sh` script (file in this repo)

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
