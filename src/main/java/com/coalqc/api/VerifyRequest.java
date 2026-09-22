package com.coalqc.api;

public record VerifyRequest(String verifiedBy) {

    public VerifyRequest {
        if (verifiedBy == null || verifiedBy.isBlank()) {
            throw new IllegalArgumentException("verifiedBy is required");
        }
    }
}
