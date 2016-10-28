package org.test.cloud.sandbox;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by srang on 10/28/16.
 */
@Component
public class DummyRoute extends RouteBuilder {

    @Value("${org.test.cloud.sandbox.application.port}")
    String appPort;

    @Value("${org.test.cloud.sandbox.db.host}")
    String dbHost;

    @Value("${org.test.cloud.sandbox.db.port}")
    String dbPort;

    @Value("${org.test.cloud.sandbox.db.context}")
    String dbContext;

    @Override
    public void configure() throws Exception {
        restConfiguration().component("undertow").port(appPort).componentProperty("bridgeEndpoint","true");
        rest("/hello").id("hello-endpoint")
                .get("").id("hello-world-empty").to("direct:world")
                .get("/{person}").id("hello-world-name")
                    .route().id("forward-name")
                        .multicast()
                        .setExchangePattern(ExchangePattern.InOnly)
                        .to("seda:long-process","seda:long-process","seda:forward-persist")
                    .to("direct:name-response");

        from("direct:name-response").id("name-response")
                .log("Responding \"Hello ${header.person}\"")
                .transform().simple("Hello ${header.person}");
        from("seda:long-process?concurrentConsumers=4").id("log-name")
                .delay(10000)
                .log("long process: ${header.person}");
        from("seda:forward-persist").id("persist-person")
                .setHeader(Exchange.HTTP_PATH,simple(dbContext+"/${header.person}"))
                .log("${header.CamelHttpBaseUri}${header.CamelHttpPath}")
                .to("http:"+dbHost+":"+dbPort+"?bridgeEndpoint=true");

        from("direct:world").id("respond-world")
                .transform().constant("Hello world");

        rest("/data").id("data-endpoint")
                .get("/{name}").id("data-endpoint").to("direct:database");

        from("direct:database").id("data-persist")
                .log("persist: ${header.name}");
    }
}
