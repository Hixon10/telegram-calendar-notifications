package ru.hixon;

import biweekly.Biweekly;
import biweekly.io.chain.ChainingTextParser;
import io.micronaut.runtime.Micronaut;

import java.io.ByteArrayInputStream;

public class Application {

    public static void main(String[] args) {
        /// Hack for GraalVM native image
        try {
            ChainingTextParser<ChainingTextParser<?>> result = Biweekly.parse(new ByteArrayInputStream("hack".getBytes()));
        } catch (Throwable th) {
            System.out.println("Error, but it is a hack");
        }

        Micronaut.run(Application.class, args);
    }
}
