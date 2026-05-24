package com.questlog.backend.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

public class BadRequestException extends AbstractThrowableProblem {

    private static final URI TYPE = URI.create("https://questlog.com/errors/bad-request");

    public BadRequestException(String message) {
        super(TYPE, "Bad Request", Status.BAD_REQUEST, message);
    }
}
