# Telegram calendar notifications

This project contains source codes for telegram bot: https://telegram.me/calendar_notifications_bot 

Main purpose of this bot is notification about your calendar event, using telegram for notification target. 

You need to set up one or more calendar in `ics`-format.  

## Build:
1. Set telegram token via `TELEGRAM_TOKEN` environment variable
2. Set MongoDB url via `MONGODB_URI` environment variable (note! you can use only `mongodb://` protocol, because of GraalVM Native image)
3. Create a GraalVM Native image via `./gradlew dockerBuildNative`
