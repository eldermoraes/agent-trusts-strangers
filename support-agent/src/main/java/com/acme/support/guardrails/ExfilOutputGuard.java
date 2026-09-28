package com.acme.support.guardrails;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.acme.support.DemoStage;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * R4: the final answer is rendered as Markdown in the support panel. An image
 * pointing at a foreign host is a request the browser will make for us, with
 * whatever the model put in the URL. So: no images, no links, outside the
 * allow-list. Credentials get redacted too (heuristic).
 */
@ApplicationScoped
public class ExfilOutputGuard implements OutputGuardrail {

    private static final Pattern MD_IMAGE = Pattern.compile("!\\[[^\\]]*]\\(([^)\\s]+)[^)]*\\)");
    private static final Pattern MD_LINK = Pattern.compile("(?<!!)\\[([^\\]]*)]\\(([^)\\s]+)[^)]*\\)");
    private static final Pattern BARE_URL = Pattern.compile("https?://[^\\s)\\]>\"']+");

    @Inject
    DemoStage stage;

    @ConfigProperty(name = "demo.allowed-hosts", defaultValue = "localhost:8080")
    String allowedHosts;

    @Override
    public OutputGuardrailResult validate(AiMessage aiMessage) {
        if (!stage.atLeast(DemoStage.R4_OUTPUT_GUARDRAIL)) {
            return success();
        }
        String original = aiMessage.text() == null ? "" : aiMessage.text();
        String cleaned = sanitize(original);
        return cleaned.equals(original) ? success() : successWith(cleaned);
    }

    String sanitize(String text) {
        Set<String> allowed = Arrays.stream(allowedHosts.split(","))
                .map(String::trim).collect(Collectors.toSet());
        String out = replace(MD_IMAGE, text, m -> allowed(m.group(1), allowed) ? m.group() : "[image removed]");
        out = replace(MD_LINK, out, m -> allowed(m.group(2), allowed) ? m.group() : m.group(1) + " [link removed]");
        out = replace(BARE_URL, out, m -> allowed(m.group(), allowed) ? m.group() : "[link removed]");
        return ReplyScopeGuard.SECRET.matcher(out).replaceAll("[redacted]");
    }

    private static boolean allowed(String url, Set<String> allowed) {
        try {
            var uri = URI.create(url);
            if (uri.getHost() == null) {
                return true; // relative URL, stays on our own host
            }
            String host = uri.getPort() > 0 ? uri.getHost() + ":" + uri.getPort() : uri.getHost();
            return allowed.contains(host);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String replace(Pattern p, String in, java.util.function.Function<Matcher, String> fn) {
        var m = p.matcher(in);
        var sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(fn.apply(m)));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
