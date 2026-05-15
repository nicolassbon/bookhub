package com.bookhub.catalog.web.admin;

import jakarta.validation.constraints.NotBlank;

public record ImportBookRequest(
    @NotBlank(message = "sourceReference must not be blank") String sourceReference,
    @NotBlank(message = "provider must not be blank") String provider) {}
