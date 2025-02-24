package io.github.alexshamrai.orderservice;

import io.github.alexshamrai.orderservice.client.BookClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class OrderServiceApplicationTests {

    @MockBean
    private BookClient bookClient;


    @Test
    void contextLoads() {
    }

}
