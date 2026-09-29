package com.acme.support;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Every tool call the agent made, with what the server answered. The agent's
 * own summary is not evidence; this list is.
 */
@ApplicationScoped
public class ToolAudit {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final List<String> calls = new CopyOnWriteArrayList<>();

    public void record(String tool, String args, String outcome) {
        String line = LocalTime.now().format(TIME) + "  " + tool + "(" + args + ") -> "
                + (outcome.length() > 90 ? outcome.substring(0, 90) + "…" : outcome).replace('\n', ' ');
        calls.add(line);
        while (calls.size() > 20) {
            calls.remove(0);
        }
    }

    public List<String> calls() {
        return List.copyOf(calls);
    }

    public void clear() {
        calls.clear();
    }
}
