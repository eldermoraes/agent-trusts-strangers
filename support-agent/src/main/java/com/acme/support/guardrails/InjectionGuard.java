package com.acme.support.guardrails;

import com.acme.support.DemoStage;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.guardrails.PatternBasedPromptInjectionGuardrail;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * R0: the fix everybody tries first. LangChain4j's built-in pattern guardrail.
 * It only ever sees the USER message. The ticket text arrives as a tool result,
 * which this guardrail never looks at.
 */
@ApplicationScoped
public class InjectionGuard extends PatternBasedPromptInjectionGuardrail {

    @Inject
    DemoStage stage;

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        if (!stage.atLeast(DemoStage.R0_INPUT_GUARDRAIL)) {
            return success();
        }
        return super.validate(userMessage);
    }
}
