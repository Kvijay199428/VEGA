package com.vegatrader.upstox.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * OAuth Callback Controller.
 * Handles Upstox OAuth redirect.
 * Returns simple HTML page for Selenium to extract the code.
 *
 * @since 2.0.0
 */
@Controller
@RequestMapping("/api/v1/auth/upstox")
public class OAuthCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(OAuthCallbackController.class);

    /**
     * Handle OAuth callback from Upstox.
     * Returns simple HTML page with code parameter visible in URL.
     * Selenium can extract the code from the URL directly.
     */
    @GetMapping("/callback")
    public void handleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription,
            HttpServletResponse response) throws IOException {

        logger.info("╔═══════════════════════════════════════════════════════╗");
        logger.info("║  OAUTH CALLBACK RECEIVED  ║");
        logger.info("╚═══════════════════════════════════════════════════════╝");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (error != null) {
            logger.error("OAuth error: {} - {}", error, errorDescription);
            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>OAuth Error</title></head>");
            out.println("<body style='font-family: monospace; background: #1a1a2e; color: #ff4757; padding: 40px;'>");
            out.println("<h1>⚠ OAuth Error</h1>");
            out.println("<p>Error: " + escapeHtml(error) + "</p>");
            out.println("<p>Description: " + escapeHtml(errorDescription) + "</p>");
            out.println("<p id='status'>error</p>");
            out.println("</body></html>");
        } else if (code != null) {
            logger.info("✓ Authorization code received (length: {})", code.length());
            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>OAuth Success</title></head>");
            out.println("<body style='font-family: monospace; background: #1a1a2e; color: #00ff88; padding: 40px;'>");
            out.println("<h1>✓ Authorization Successful</h1>");
            out.println("<p>Code received successfully.</p>");
            out.println("<p id='status'>success</p>");
            out.println("<p id='code' style='display:none;'>" + escapeHtml(code) + "</p>");
            out.println("<p style='color: #666;'>This window will close automatically...</p>");
            out.println("</body></html>");
        } else {
            logger.warn("Callback received without code or error");
            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>OAuth Unknown</title></head>");
            out.println("<body style='font-family: monospace; background: #1a1a2e; color: #ffa502; padding: 40px;'>");
            out.println("<h1>⚠ Unknown Response</h1>");
            out.println("<p>No code or error received.</p>");
            out.println("<p id='status'>unknown</p>");
            out.println("</body></html>");
        }

        out.flush();
    }

    private String escapeHtml(String input) {
        if (input == null)
            return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
