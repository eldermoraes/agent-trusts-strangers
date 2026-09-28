package com.acme.sink;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

/** Everything that ever reached the stranger's server, newest last. */
@ApplicationScoped
public class RequestLog {

    public record Entry(String time, String method, String path, String query, String body) {}

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final List<Entry> entries = new CopyOnWriteArrayList<>();

    public Entry record(String method, String path, String query, String body) {
        var entry = new Entry(LocalTime.now().format(TIME), method, path, query == null ? "" : query,
                body == null ? "" : body);
        entries.add(entry);
        return entry;
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public void clear() {
        entries.clear();
    }
}
