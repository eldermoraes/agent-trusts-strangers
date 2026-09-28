package com.acme.support.tools;

import com.acme.support.customers.CustomerStore;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** The one tool support genuinely needs: who is this customer? */
@ApplicationScoped
public class CustomerTools {

    @Inject
    CustomerStore customers;

    @Tool("Look up a customer's profile (name, phone, address, active voucher) by e-mail address.")
    public String lookupCustomer(@P("The customer's e-mail address") String email) {
        return customers.find(email).map(CustomerStore.Customer::asText)
                .orElse("No customer found for " + email);
    }
}
