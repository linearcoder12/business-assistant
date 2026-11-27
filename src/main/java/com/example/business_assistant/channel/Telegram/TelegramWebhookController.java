package com.example.business_assistant.channel.Telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController("/tele-webhook")
public class TelegramWebhookController {
//        create a Telegram client
//          send a response once the webhook is connected.
    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookController.class);
    private final String secret;

    public TelegramWebhookController(@Value("${telegram.webhook-secret}") String secret) {
        this.secret = secret;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> onUpdate(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String token,
            @RequestBody JsonNode update) {

        if (token == null || !MessageDigest.isEqual(
                secret.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(403).build();
        }

        String pretty = update.toPrettyString();
        log.info("Telegram update:\n{}", pretty);

        JsonNode chatId = update.path("message").path("chat").path("id");
        if (chatId.isMissingNode()) {
            return ResponseEntity.ok(Map.of("message","ID not present"));   // not a plain message (edit, join, etc.): still return 200
        }

        // Webhook reply: Telegram executes this JSON as a sendMessage call
        Map<String, Object> reply = new LinkedHashMap<>();
        reply.put("method", "sendMessage");
        reply.put("chat_id", chatId.asLong());
        reply.put("text", "hey there");
        return ResponseEntity.ok(reply);
    }
}
