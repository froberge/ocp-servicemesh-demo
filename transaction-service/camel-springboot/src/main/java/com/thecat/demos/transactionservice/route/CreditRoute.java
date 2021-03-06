package com.thecat.demos.transactionservice.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
public class CreditRoute extends RouteBuilder {

    private final Environment env;

    public CreditRoute(Environment env) {
        this.env = env;
    }

    public void configure() throws Exception {

        restConfiguration()
            .contextPath(env.getProperty("camel.component.servlet.mapping.contextPath", "/rest/*"))
            .apiContextPath("/api-doc")
            .apiProperty("api.title", "Transaction Service")
            .apiProperty("api.version", "1.0")
            .apiProperty("cors", "true")
            .apiContextRouteId("doc-api")
            .port(env.getProperty("server.port", "8080"))
            .bindingMode(RestBindingMode.json)
            .dataFormatProperty("disableFeatures", "FAIL_ON_EMPTY_BEANS");

        rest("/credit")
            .get("/").produces("application/json").route()
            .to("{{route.creditAllTransaction}}")
            .endRest()
            .get("/version").route()
            .to("{{route.creditVersion}}")
            .end();

        from( "{{route.creditVersion}}")    
            .log("calling the credit Version")
            .removeHeader(Exchange.HTTP_URI)
            .removeHeader(Exchange.HTTP_PATH)
            .to("{{service.creditservice.url}}/version?httpMethod=GET")
            .transform().simple( "credit service => ${body}");

        from( "{{route.creditTransaction}}")    
            .log("calling the credit service")
            .marshal().json(JsonLibrary.Jackson)
            .removeHeader(Exchange.HTTP_URI)
            .removeHeader(Exchange.HTTP_PATH)
            .log("BODY: ${body}")
            .to("{{service.creditservice.url}}?httpMethod=POST");
            
        from("{{route.creditAllTransaction}}")
            .log("calling the credit service  get all transaction")
            .removeHeader(Exchange.HTTP_URI)
            .removeHeader(Exchange.HTTP_PATH)
            .to("{{service.creditservice.url}}?httpMethod=GET");
    }
}
