package com.xebialabs.restito.semantics;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.util.function.Function;
import java.util.function.Predicate;

class ConditionValidation {

    static Validation<Seq<String>, Condition> validate(Condition condition, Call input) {
        Function<Seq<Condition>, Validation<Seq<String>, Condition>> validateConditions = conditions -> {
            var failedValidations = conditions.map(c -> c.validate(input)).removeAll(Validation::isValid);
            if (failedValidations.isEmpty()) {
                return Validation.valid(condition);
            }

            return failedValidations.reduce((v1, v2) -> Validation.invalid(v1.getError().appendAll(v2.getError())));
        };

        Function<Predicate<Call>, Validation<Seq<String>, Condition>> validatePredicate = p -> p.test(input) ?
                Validation.valid(condition) :
                Validation.invalid(io.vavr.collection.List.of(condition.failureString()));

        return condition.content
                .map(validateConditions)
                .mapLeft(validatePredicate)
                .getOrElseGet(Function.identity());
    }

}
