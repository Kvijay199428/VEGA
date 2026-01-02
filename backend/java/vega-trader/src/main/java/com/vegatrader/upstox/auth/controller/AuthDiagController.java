package com.vegatrader.upstox.auth.controller;

import com.vegatrader.upstox.auth.dto.AuthEvent;
import com.vegatrader.upstox.auth.websocket.AuthSession;
import com.vegatrader.upstox.auth.websocket.AuthStatusWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthDiagController {

    private final AuthStatusWebSocketHandler wsHandler;

    public AuthDiagController(AuthStatusWebSocketHandler wsHandler) {
        this.wsHandler = wsHandler;
    }

    @GetMapping("/trace")
    public ResponseEntity<List<AuthEvent>> getAuthTrace(@RequestParam String sessionId) {
        AuthSession session = wsHandler.getAuthSession(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session.getHistory());
    }
}
