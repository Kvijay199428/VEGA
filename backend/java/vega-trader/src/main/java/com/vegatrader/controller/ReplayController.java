package com.vegatrader.controller;

import com.vegatrader.market.journal.MarketReplayEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/replay")
@CrossOrigin(origins = "*") // Allow frontend access
public class ReplayController {

    private final MarketReplayEngine replayEngine;

    public ReplayController(MarketReplayEngine replayEngine) {
        this.replayEngine = replayEngine;
    }

    @PostMapping("/load")
    public ResponseEntity<?> loadJournal(@RequestBody Map<String, String> payload) {
        String journalPath = payload.get("path");
        try {
            replayEngine.loadJournal(Path.of(journalPath));
            return ResponseEntity.ok(Map.of("message", "Journal loaded", "path", journalPath));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/seek/{timestamp}")
    public ResponseEntity<?> seek(@PathVariable long timestamp) {
        long ptr = replayEngine.seekByTimestamp(timestamp);
        return ResponseEntity.ok(Map.of("message", "Seek successful", "timestamp", timestamp, "ptr", ptr));
    }

    @PostMapping("/stream")
    public ResponseEntity<?> streamReplay(@RequestParam long startPtr, @RequestParam long limit) {
        // In a real app, this would stream via WebSocket or SSE
        // For now, we'll just acknowledge the command to start streaming to the
        // internal bus
        // Implementation TBD: Hook ReplayEngine to the main EventBus
        return ResponseEntity.ok(Map.of("message", "Replay stream started (Stub implementation)"));
    }
}
