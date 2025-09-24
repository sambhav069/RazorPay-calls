package com.example.RazorPay.task.model;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class orderIdService {
    private final Sinks.One<String> orderIdSink = Sinks.one();

    public void setOrder_id(Mono<String> orderIdMono) {

        orderIdMono.subscribe(orderIdSink::tryEmitValue);
    }

    public Mono<String> getOrder_id() {
        return orderIdSink.asMono();
    }
}

