package com.questlog.backend.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

public class ResourceNotFoundException extends AbstractThrowableProblem {

    private static final URI TYPE = URI.create("https://questlog.com/errors/not-found");

    public ResourceNotFoundException(String message) {
        super(TYPE, "Resource Not Found", Status.NOT_FOUND, message);
    }
}
