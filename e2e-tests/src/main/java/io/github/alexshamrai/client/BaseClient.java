package io.github.alexshamrai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import static io.restassured.http.ContentType.JSON;

public abstract class BaseClient {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    static {
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
            ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(
                (cls, charset) -> OBJECT_MAPPER
            ));

        RestAssured.requestSpecification = new RequestSpecBuilder()
            .log(LogDetail.URI)
            .log(LogDetail.METHOD)
            .log(LogDetail.HEADERS)
            .log(LogDetail.BODY)
            .build();

        RestAssured.responseSpecification = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .build();
    }

    protected final String baseUrl;

    protected BaseClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    protected RequestSpecification baseClient() {
        return RestAssured.given()
            .baseUri(baseUrl)
            .contentType(JSON);
    }
}