package com.acme.support;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.enterprise.context.RequestScoped;

/**
 * R5: the ticket the Operator asked about, decided by the server from the
 * request, never by the model. Tools that need a scope read it from here.
 */
@RequestScoped
public class CurrentTicket {

    private static final Pattern TICKET_REF = Pattern.compile("#(\\d+)");

    private Integer id;

    public void set(Integer id) {
        this.id = id;
    }

    public Optional<Integer> id() {
        return Optional.ofNullable(id);
    }

    /** "Please handle ticket #2." → 2 */
    public static Optional<Integer> parse(String operatorMessage) {
        Matcher m = TICKET_REF.matcher(operatorMessage == null ? "" : operatorMessage);
        return m.find() ? Optional.of(Integer.parseInt(m.group(1))) : Optional.empty();
    }
}
