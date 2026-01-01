package com.vegatrader.journal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
public class JournalController {

    @Autowired
    private JournalReader reader;

    /**
     * Verify a journal file by counting records.
     * Use curl to invoke: /api/journal/verify?path=...
     */
    @GetMapping("/api/journal/verify")
    public String verify(@RequestParam String path) {
        StringBuilder sb = new StringBuilder();

        List<Integer> sizes = new ArrayList<>();
        reader.replay(path, (payload) -> {
            sizes.add(payload.length);
        });

        return "Replay check passed. Found " + sizes.size() + " records.";
    }
}
