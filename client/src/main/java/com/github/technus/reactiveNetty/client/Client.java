package com.github.technus.reactiveNetty.client;

import com.github.technus.reactiveNetty.core.Core;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Client extends Core {
    public static void main(String[] args) {
        TcpClient.create()
                .port(9000)
                .doOnConnected(connection -> {
                    System.out.println("c " + connection.address());
                    connection.onDispose()
                            .doOnSuccess(nil->System.out.println("D " + connection.address()))
                            .subscribe();
                })
                .handle((in, out) -> {
                    return Flux.interval(Duration.ofSeconds(1))
                            .take(4)
                            .flatMap(l->out.sendString(Mono.just("YEETO")))
                            .doOnError(Throwable::printStackTrace)
                            .then();//after all is done...
                })
                .connect().subscribe();
        TcpClient.create()
                .port(9001)
                .doOnConnected(connection -> {
                    System.out.println("c " + connection.address());
                    connection.onDispose()
                            .doOnSuccess(nil->System.out.println("D " + connection.address()))
                            .subscribe();
                })
                .handle((in, out) -> {
                    return in.receive()
                            .map(byteBuf -> byteBuf.toString(StandardCharsets.UTF_8))
                            .doOnNext(System.out::println)
                            .doOnError(Throwable::printStackTrace)
                            .then();//after all is done...
                })
                .connect().subscribe();
        Mono.never().block();
    }
}
