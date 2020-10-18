package ru.hixon
import io.micronaut.jackson.serialize.JacksonObjectSerializer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.hixon.telegram.GetUpdatesResponse
import ru.hixon.telegram.OkResultResponse
import javax.inject.Inject

@MicronautTest
class TelegramDeserializationResponsesTest {

    @Inject
    lateinit var jacksonObjectSerializer: JacksonObjectSerializer

    @Test
    fun testDeserializationGetMe() {
        val responseBody = """
{
  "ok": true,
  "result": {
    "id": 42,
    "is_bot": true,
    "first_name": "Calendar Notification",
    "username": "calendar_notifications_bot",
    "can_join_groups": false,
    "can_read_all_group_messages": false,
    "supports_inline_queries": false
  }
}
                """
        val response = jacksonObjectSerializer.deserialize(responseBody.toByteArray(), OkResultResponse::class.java)
        Assertions.assertTrue(response.get().ok)
    }

    @Test
    fun testDeserializationSendMessage() {
        val responseBody = """
{
  "ok": true,
  "result": {
    "message_id": 46,
    "from": {
      "id": 53463,
      "is_bot": true,
      "first_name": "Calendar Notification",
      "username": "calendar_notifications_bot"
    },
    "chat": {
      "id": 235235,
      "first_name": "Ivan",
      "last_name": "Makarov",
      "username": "ivan_makarov",
      "type": "private"
    },
    "date": 1612814513,
    "text": "hello from bot"
  }
}
                """
        val response = jacksonObjectSerializer.deserialize(responseBody.toByteArray(), OkResultResponse::class.java)
        Assertions.assertTrue(response.get().ok)
    }

    @Test
    fun testDeserializationGetUpdatesEmpty() {
        val responseBody = """
{
  "ok": true,
  "result": []
}
                """
        val response = jacksonObjectSerializer.deserialize(responseBody.toByteArray(), GetUpdatesResponse::class.java)
        Assertions.assertTrue(response.get().ok)
        Assertions.assertTrue(response.get().result.isEmpty())
    }

    @Test
    fun testDeserializationGetUpdates() {
        val responseBody = """
{
  "ok": true,
  "result": [
    {
      "update_id": 194345102,
      "message": {
        "message_id": 1,
        "from": {
          "id": 42,
          "is_bot": false,
          "first_name": "Ivan",
          "last_name": "Kirov",
          "username": "kirov_ivan"
        },
        "chat": {
          "id": 42,
          "first_name": "Ivan",
          "last_name": "Kirov",
          "username": "kirov_ivan",
          "type": "private"
        },
        "date": 1612864385,
        "text": "/start",
        "entities": [
          {
            "offset": 0,
            "length": 6,
            "type": "bot_command"
          }
        ]
      }
    },
    {
      "update_id": 924324302,
      "message": {
        "message_id": 2,
        "from": {
          "id": 43,
          "is_bot": false,
          "first_name": "Ivan",
          "last_name": "Kirov",
          "username": "kirov_ivan"
        },
        "chat": {
          "id": 43,
          "first_name": "Ivan",
          "last_name": "Kirov",
          "username": "kirov_ivan",
          "type": "private"
        },
        "date": 1603814389,
        "text": "test message"
      }
    }
  ]
}
                """
        val response = jacksonObjectSerializer.deserialize(responseBody.toByteArray(), GetUpdatesResponse::class.java)
        Assertions.assertTrue(response.get().ok)
        Assertions.assertEquals(2, response.get().result.size)

        Assertions.assertEquals(194345102, response.get().result.get(0).update_id)
        Assertions.assertEquals("/start", response.get().result.get(0).message!!.text)
        Assertions.assertEquals(42, response.get().result.get(0).message!!.chat.id)
        Assertions.assertEquals(1, response.get().result.get(0).message!!.entities!!.size)
        Assertions.assertEquals("bot_command", response.get().result.get(0).message!!.entities!!.get(0).type)

        Assertions.assertEquals(924324302, response.get().result.get(1).update_id)
        Assertions.assertEquals("test message", response.get().result.get(1).message!!.text)
        Assertions.assertEquals(43, response.get().result.get(1).message!!.chat.id)
        Assertions.assertNull(response.get().result.get(1).message!!.entities)
    }
}
