# Telegram calendar notifications

This project contains source codes for telegram bot: https://telegram.me/calendar_notifications_bot 

Main purpose of this bot is notification about your calendar event, using telegram for notification target. 

You need to set up one or more calendar in `ics`-format.  

## Adding calendar to the bot
You need to send `ics` URL and before notification minutes to the bot. 

In this example we want to get natifications for calendar `https://example.ru/3242352/file.ics`, before `15` minutes for event.
```
https://example.ru/3242352/file.ics 15
```

## Bot commands:
1. `stop` - Stop sending notifications

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
