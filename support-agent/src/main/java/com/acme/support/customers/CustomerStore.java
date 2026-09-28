package com.acme.support.customers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

/** The private data the agent legitimately needs, and the stranger wants. */
@ApplicationScoped
public class CustomerStore {

    public record Customer(String email, String name, String phone, String address, String voucherCode) {

        public String asText() {
            return "Customer: " + name + "\nEmail: " + email + "\nPhone: " + phone
                    + "\nAddress: " + address + "\nActive voucher: " + voucherCode;
        }

        /** Every value that must not leave the building unless this customer asked. */
        public List<String> sensitiveValues() {
            return List.of(email, phone, address, voucherCode);
        }
    }

    private static final Map<String, Customer> CUSTOMERS = List.of(
            new Customer("bob@example.com", "Bob Ferreira", "+32 470 11 22 33",
                    "Rue de la Loi 42, 1000 Brussels", "BOB-GIFT-9F3K"),
            new Customer("carol@example.com", "Carol Janssens", "+32 495 44 55 66",
                    "Meir 12, 2000 Antwerp", "CAROL-VIP-7Q2X"),
            new Customer("mallory@example.net", "Mallory", "+32 400 00 00 00",
                    "unknown", "NONE"))
            .stream().collect(Collectors.toUnmodifiableMap(Customer::email, c -> c));

    public Optional<Customer> find(String email) {
        return Optional.ofNullable(CUSTOMERS.get(email == null ? "" : email.trim().toLowerCase()));
    }

    public List<Customer> all() {
        return List.copyOf(CUSTOMERS.values());
    }
}
