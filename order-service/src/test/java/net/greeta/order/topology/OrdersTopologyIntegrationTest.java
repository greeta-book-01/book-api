package net.greeta.order.topology;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.greeta.order.domain.Order;
import net.greeta.order.domain.OrderLineItem;
import net.greeta.order.domain.OrderType;
import net.greeta.order.security.WebSecurityConfig;
import net.greeta.order.service.OrderService;
import net.greeta.order.service.OrderStreamService;
import net.greeta.order.service.OrdersWindowService;
import net.greeta.order.service.UserService;
import org.apache.kafka.streams.KeyValue;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.greeta.order.topology.OrdersTopology.GENERAL_ORDERS;
import static net.greeta.order.topology.OrdersTopology.ORDERS;
import static net.greeta.order.topology.OrdersTopology.RESTAURANT_ORDERS;
import static net.greeta.order.topology.OrdersTopology.STORES;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedKafka(topics = {ORDERS, STORES})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@TestPropertySource(properties = {
        "spring.kafka.streams.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.streams.auto-startup=false"
})
@MockBean(value = {
        OrderService.class,
        UserService.class,
        WebSecurityConfig.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrdersTopologyIntegrationTest {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    OrderStreamService orderStreamService;

    @Autowired
    OrdersWindowService ordersWindowService;

    @BeforeEach
    public void setUp() {

        streamsBuilderFactoryBean.start();

    }

    @AfterEach
    public void destroy() {

        streamsBuilderFactoryBean.getKafkaStreams().close();
        streamsBuilderFactoryBean.getKafkaStreams().cleanUp();


    }

    @Test
    void ordersCount() {

        publishOrders();

        Awaitility.await().atMost(10, SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> orderStreamService.getOrdersCount(GENERAL_ORDERS).size(), equalTo(1));

        var generalOrdersCount = orderStreamService.getOrdersCount(GENERAL_ORDERS);
        assertEquals(1, generalOrdersCount.get(0).orderCount());

    }

    @Test
    void ordersRevenue() {

        publishOrders();

        Awaitility.await().atMost(20, SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> orderStreamService.revenueByOrderType(GENERAL_ORDERS).size(), equalTo(1));

        Awaitility.await().atMost(20, SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> orderStreamService.revenueByOrderType(RESTAURANT_ORDERS).size(), equalTo(1));

        var generalOrdersRevenue = orderStreamService.revenueByOrderType(GENERAL_ORDERS);
        assertEquals(new BigDecimal("27.00"), generalOrdersRevenue.get(0).totalRevenue().runningRevenue());

        var restaurantOrdersRevenue = orderStreamService.revenueByOrderType(RESTAURANT_ORDERS);
        assertEquals(new BigDecimal("15.00"), restaurantOrdersRevenue.get(0).totalRevenue().runningRevenue());

    }

    @Test
    void ordersRevenue_multipleOrders() {

        publishOrders();
        publishOrders();

        Awaitility.await().atMost(20, SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> orderStreamService.revenueByOrderType(GENERAL_ORDERS).size(), equalTo(1));

        Awaitility.await().atMost(20, SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> orderStreamService.revenueByOrderType(RESTAURANT_ORDERS).size(), equalTo(1));

        var generalOrdersRevenue = orderStreamService.revenueByOrderType(GENERAL_ORDERS);
        assertEquals(new BigDecimal("54.00"), generalOrdersRevenue.get(0).totalRevenue().runningRevenue());

        var restaurantOrdersRevenue = orderStreamService.revenueByOrderType(RESTAURANT_ORDERS);
        assertEquals(new BigDecimal("30.00"), restaurantOrdersRevenue.get(0).totalRevenue().runningRevenue());

    }

    @Test
    void ordersRevenue_multipleOrdersByWindows() {

        publishOrders();
        publishOrders();

        Awaitility.await().atMost(20, SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> ordersWindowService.getOrdersRevenueWindowsByType(GENERAL_ORDERS).size(), equalTo(1));

        Awaitility.await().atMost(20, SECONDS)
                .pollDelay(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> ordersWindowService.getOrdersRevenueWindowsByType(RESTAURANT_ORDERS).size(), equalTo(1));

        var generalOrdersRevenue = ordersWindowService.getOrdersRevenueWindowsByType(GENERAL_ORDERS);
        System.out.println("generalOrdersRevenue Windows : " + generalOrdersRevenue);
        assertEquals(new BigDecimal("54.00"), generalOrdersRevenue.get(0).totalRevenue().runningRevenue());

        var expectedStartTime = LocalDateTime.parse("2023-02-22T03:25:00");
        var expectedEndTime = LocalDateTime.parse("2023-02-22T03:25:15");

        assert generalOrdersRevenue.get(0).startWindow().isEqual(expectedStartTime);
        assert generalOrdersRevenue.get(0).endWindow().isEqual(expectedEndTime);

        var restaurantOrdersRevenue = ordersWindowService.getOrdersRevenueWindowsByType(RESTAURANT_ORDERS);
        System.out.println("restaurantOrdersRevenue Windows : " + restaurantOrdersRevenue);
        assertEquals(new BigDecimal("30.00"), restaurantOrdersRevenue.get(0).totalRevenue().runningRevenue());

    }


    private void publishOrders() {
        orders()
                .forEach(order -> {
                    String orderJSON = null;
                    try {
                        orderJSON = objectMapper.writeValueAsString(order.value);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    kafkaTemplate.send(ORDERS, order.key, orderJSON);
                });


    }


    static List<KeyValue<String, Order>> orders() {

        var orderItems = List.of(
                new OrderLineItem("Bananas", 2, new BigDecimal("2.00")),
                new OrderLineItem("Iphone Charger", 1, new BigDecimal("25.00"))
        );

        var orderItemsRestaurant = List.of(
                new OrderLineItem("Pizza", 2, new BigDecimal("12.00")),
                new OrderLineItem("Coffee", 1, new BigDecimal("3.00"))
        );

        var order1 = new Order(12345, "store_1234",
                new BigDecimal("27.00"),
                OrderType.GENERAL,
                orderItems,
                //LocalDateTime.now()
                LocalDateTime.parse("2023-02-21T21:25:01")
        );

        var order2 = new Order(54321, "store_1234",
                new BigDecimal("15.00"),
                OrderType.RESTAURANT,
                orderItemsRestaurant,
                //LocalDateTime.now()
                LocalDateTime.parse("2023-02-21T21:25:01")
        );
        var keyValue1 = KeyValue.pair(order1.orderId().toString()
                , order1);

        var keyValue2 = KeyValue.pair(order2.orderId().toString()
                , order2);


        return List.of(keyValue1, keyValue2);

    }


}
