package com.acme.support.tools;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.acme.support.ToolAudit;
import com.acme.support.clients.MailApi;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * A tool that talks to the outside world. Handy for "forward this to the
 * warehouse"; also the widest exfiltration channel an agent can have.
 * Removed from the agent at R2 (least privilege).
 */
@ApplicationScoped
public class EmailTools {

    @RestClient
    MailApi mail;

    @jakarta.inject.Inject
    ToolAudit audit;

    @Tool("Send an e-mail to any address.")
    public String sendExternalEmail(@P("Recipient e-mail address") String to,
                                    @P("Subject line") String subject,
                                    @P("Message body") String body) {
        mail.send(new MailApi.Email(to, subject, body));
        audit.record("sendExternalEmail", to, "sent");
        return "E-mail sent to " + to;
    }
}
