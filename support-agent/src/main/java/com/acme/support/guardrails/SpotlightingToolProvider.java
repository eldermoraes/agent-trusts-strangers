package com.acme.support.guardrails;

import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.acme.support.DemoStage;

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
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * R1: spotlighting. Every MCP tool result is wrapped so the model can tell
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

    @Inject
    Instance<McpClient> mcpClients;

    @Inject
    DemoStage stage;

    @Override
    public ToolProviderResult provideTools(ToolProviderRequest request) {
        if (mcpClients.isUnsatisfied()) {
            return ToolProviderResult.builder().build();
        }
        return McpToolProvider.builder()
                .mcpClients(List.copyOf(mcpClients.stream().toList()))
                .toolWrapper(this::wrap)
                .build()
                .provideTools(request);
    }

    ToolExecutor wrap(ToolExecutor delegate) {
        return new ToolExecutor() {
            @Override
            public String execute(ToolExecutionRequest req, Object memoryId) {
                return spotlight(delegate.execute(req, memoryId));
            }

            @Override
            public ToolExecutionResult executeWithContext(ToolExecutionRequest req, InvocationContext ctx) {
                var result = delegate.executeWithContext(req, ctx);
                return ToolExecutionResult.builder()
                        .isError(result.isError())
                        .result(result.result())
                        .resultText(spotlight(result.resultText()))
                        .build();
            }
        };
    }

    String spotlight(String toolResult) {
        if (!stage.atLeast(DemoStage.R1_SPOTLIGHTING)) {
            return toolResult;
        }
        String tag = stage.randomNonce() ? FIXED_TAG + "_" + nonce() : FIXED_TAG;
        return "<" + tag + ">\n" + toolResult + "\n</" + tag + ">";
    }

    private static String nonce() {
        var bytes = new byte[6];
        ThreadLocalRandom.current().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
