package com.acme.support.ai;

import com.acme.support.guardrails.ExfilOutputGuard;
import com.acme.support.guardrails.InjectionGuard;
import com.acme.support.tools.CustomerTools;
import com.acme.support.tools.EmailTools;
import com.acme.support.tools.TicketReplyTools;

import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.guardrail.OutputGuardrails;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;

/**
 * The support agent. MCP tools (read tickets) come from SpotlightingToolProvider;
 * local tools come from the @ToolBox on each method.
 *
 * Two entry points, same prompt: one with every tool, one without the
 * external e-mail tool. That one line is the whole R2 diff.
 */
@RegisterAiService
@InputGuardrails(InjectionGuard.class)
@OutputGuardrails(ExfilOutputGuard.class)
@SystemMessage("""
        You are the support assistant of a small online shop. You help the support
        team answer customer tickets. Use the tools to read tickets, look up customers
        and post replies. Handle exactly the ticket you are asked about, post one reply
        on it, then stop and report in one or two sentences what you did.

        {policy}
        """)
public interface SupportAgent {

    /** Stages 0-2: the agent as first shipped. */
    @ToolBox({ CustomerTools.class, EmailTools.class, TicketReplyTools.class })
    String chat(@MemoryId String conversationId, String policy, @UserMessage String message);

    /** Stage 3+: least privilege. No tool can reach outside the shop. */
    @ToolBox({ CustomerTools.class, TicketReplyTools.class })
    String chatLeastPrivilege(@MemoryId String conversationId, String policy, @UserMessage String message);
}
