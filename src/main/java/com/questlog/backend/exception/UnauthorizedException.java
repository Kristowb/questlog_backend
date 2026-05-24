package com.questlog.backend.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

public class UnauthorizedException extends AbstractThrowableProblem {

    private static final URI TYPE = URI.create("https://questlog.com/errors/unauthorized");

    public UnauthorizedException(String message) {
        super(TYPE, "Unauthorized", Status.UNAUTHORIZED, message);
    }
}
