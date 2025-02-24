package io.github.alexshamrai.client;

import io.github.alexshamrai.dto.OrderRequest;
import io.restassured.response.Response;
import java.util.Map;

public class OrderClient extends BaseClient {

    public OrderClient(String baseUrl) {
        super(baseUrl);
    }

    public Response createOrder(OrderRequest orderRequest) {
        return baseClient()
            .body(orderRequest)
            .post("/api/orders");
    }

    public Response getOrder(Long id) {
        return baseClient()
            .get("/api/orders/" + id);
    }

    public Response getUserOrders(Long userId) {
        return baseClient()
            .get("/api/orders/user/" + userId);
    }

    public Response updateOrderStatus(Long id, String status) {
        return baseClient()
            .queryParam("status", status)
            .put("/api/orders/" + id + "/status");
    }

    public Response deleteOrder(Long id) {
        return baseClient()
            .delete("/api/orders/" + id);
    }

    public Response deleteAllOrders() {
        return baseClient()
            .delete("/api/orders");
    }
}