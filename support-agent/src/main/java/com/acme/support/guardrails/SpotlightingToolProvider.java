package com.acme.support.guardrails;

import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.acme.support.CurrentTicket;
import com.acme.support.DemoStage;
import com.acme.support.ToolAudit;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * Spotlighting. Every MCP tool result is wrapped so the model can tell
 * "this is data somebody else wrote" from "this is an instruction".
 *
 * With a FIXED delimiter the stranger just closes it inside the ticket. With a
 * random nonce per call, the stranger cannot guess what to close.
 *
 * Wraps LangChain4j's McpToolProvider; the Quarkus-generated provider is
 * disabled in application.properties (quarkus.langchain4j.mcp.generate-tool-provider=false).
 */
@ApplicationScoped
public class SpotlightingToolProvider implements ToolProvider {

    public static final String FIXED_TAG = "untrusted_content";

    /** The MCP tools this agent may use. Anything new the server announces is ignored. */
    static final java.util.Set<String> MCP_ALLOWLIST = java.util.Set.of("read_ticket", "list_open_tickets");

    // MCP clients are qualified beans (@McpClientName), so ask for all of them.
    @Inject
    @Any
    Instance<McpClient> mcpClients;

    @Inject
    DemoStage stage;

    @Inject
    CurrentTicket currentTicket;

    @Inject
    ToolAudit audit;

    private volatile String lastResult = "";

    /** The most recent tool result exactly as handed to the model (shown on stage). */
    public String lastResult() {
        return lastResult;
    }

    @Override
    public ToolProviderResult provideTools(ToolProviderRequest request) {
        if (mcpClients.isUnsatisfied()) {
            return ToolProviderResult.builder().build();
        }
        return McpToolProvider.builder()
                .mcpClients(List.copyOf(mcpClients.stream().toList()))
                .filterToolNames(MCP_ALLOWLIST.toArray(String[]::new))   // only these MCP tools reach the model
                .toolWrapper(this::wrap)
                .build()
                .provideTools(request);
    }

    ToolExecutor wrap(ToolExecutor delegate) {
        return new ToolExecutor() {
            @Override
            public String execute(ToolExecutionRequest req, Object memoryId) {
                var denied = outOfScope(req);
                audit.record(req.name(), req.arguments(), denied != null ? denied : "ok");
                return denied != null ? denied : spotlight(delegate.execute(req, memoryId));
            }

            @Override
            public ToolExecutionResult executeWithContext(ToolExecutionRequest req, InvocationContext ctx) {
                var denied = outOfScope(req);
                audit.record(req.name(), req.arguments(), denied != null ? denied : "ok");
                if (denied != null) {
                    return ToolExecutionResult.builder().isError(true).resultText(denied).build();
                }
                var result = delegate.executeWithContext(req, ctx);
                return ToolExecutionResult.builder()
                        .isError(result.isError())
                        .result(result.result())
                        .resultText(spotlight(result.resultText()))
                        .build();
            }
        };
    }

    /** Scoped tools: the MCP tools only see the ticket this request is about. */
    public String outOfScope(ToolExecutionRequest req) {
        if (!stage.enabled(DemoStage.SCOPED_TOOLS)) {
            return null;
        }
        var current = currentTicket.id();
        if (current.isEmpty()) {
            return "Not allowed: this request does not name a ticket.";
        }
        if ("list_open_tickets".equals(req.name())) {
            return "This request is about ticket #" + current.get() + ".";
        }
        if (!MCP_ALLOWLIST.contains(req.name())) {
            return "Not allowed: unknown tool.";
        }
        if ("read_ticket".equals(req.name())) {
            var asked = new io.vertx.core.json.JsonObject(req.arguments()).getInteger("id", -1);
            if (asked != current.get().intValue()) {
                return "Not allowed: this request is about ticket #" + current.get() + ".";
            }
        }
        return null;
    }

    String spotlight(String toolResult) {
        if (!stage.enabled(DemoStage.SPOTLIGHTING)) {
            lastResult = toolResult;
            return toolResult;
        }
        String tag = stage.randomNonce() ? FIXED_TAG + "_" + nonce() : FIXED_TAG;
        lastResult = "<" + tag + ">\n" + toolResult + "\n</" + tag + ">";
        return lastResult;
    }

    private static String nonce() {
        var bytes = new byte[6];
        ThreadLocalRandom.current().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
