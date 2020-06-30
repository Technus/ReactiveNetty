package com.github.technus.reactiveNetty.server;

import com.github.technus.reactiveNetty.core.Core;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpServer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Server extends Core {
    public static void main(String[] args) {
        TcpServer.create()
                .port(9000)
                .doOnConnection(connection -> {
                    System.out.println("c "+connection.address());
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
                .bind().subscribe();
        TcpServer.create()
                .port(9001)
                .doOnConnection(connection -> {
                    System.out.println("c "+connection.address());
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
                .bind().subscribe();
        Mono.never().block();
    }
}
