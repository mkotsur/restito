package com.xebialabs.restito.semantics;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.glassfish.grizzly.http.server.Request;
import org.junit.Test;

import java.util.Map;

import static com.xebialabs.restito.semantics.Condition.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.xebialabs.restito.semantics.Condition.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.when;

public class ConditionValidationTest {

    @Mock
    private Call call;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnReasonsWhySimpleConditionsFail() {
        var uri = uri("https://google.com");
        var param = parameter("foo", "bar");

        when(call.getUri()).thenReturn("https://yahoo.com");
        when(call.getParameters()).thenReturn(Map.of());

        var validateUri = ConditionValidation.validate(uri, call);
        assertTrue(validateUri.isInvalid());

        var uriErrors = validateUri.getError();
        assertEquals(1, uriErrors.length());
        assertEquals(expectedError(uri), uriErrors.head());

        var validateParam = ConditionValidation.validate(param, call);
        assertTrue(validateParam.isInvalid());

        var paramErrors = validateParam.getError();
        assertEquals(1, paramErrors.length());
        assertEquals(expectedError(param), paramErrors.head());
    }

    @Test
    public void shouldReturnReasonsWhyComplexConditionFailed() {
        var uri = uri("https://google.com");
        var parameter = parameter("foo", "bar");
        var complex = composite(uri, parameter);

        System.out.println("URI: " + uri.hashCode());
        System.out.println("Parameter: " + parameter.hashCode());
        System.out.println("Complex: " + complex.hashCode());

        when(call.getUri()).thenReturn("https://yahoo.com");
        when(call.getParameters()).thenReturn(Map.of());

        Validation<Seq<String>, Condition> validate = ConditionValidation.validate(complex, call);

        assertTrue(validate.isInvalid());
        var errors = validate.getError();

        assertEquals(2, errors.length());
        assertEquals(expectedError(parameter), errors.get(1));
        assertEquals(expectedError(uri), errors.get(0));
    }

    private String expectedError(Condition c) {
        return String.format("Condition `%s@%s` failed.", c.getLabel().get(), c.hashCode());
    }

}