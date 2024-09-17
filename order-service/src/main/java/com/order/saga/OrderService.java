package com.order.saga;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired(required = true)
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Value("${kafka.topic.order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topic.order-cancelled}")
    private String orderCancelledTopic;

    public Orders createOrder(double amount) {
        Orders orders = new Orders();
        orders.setAmount(amount);
        orders.setStatus("CREATED");
        orderRepository.save(orders);

        kafkaTemplate.send(orderCreatedTopic, new OrderCreatedEvent(orders.getId(), orders.getAmount()));
        return orders;
    }

    @KafkaListener(topics = "${kafka.topic.order-cancelled}", groupId = "order-group")
    public void handleOrderCancelled(OrderCancelledEvent event) {
        System.out.println("event-consumed : " + event);
        Orders orders = orderRepository.findById(event.getOrderId()).orElseThrow();
        orders.setStatus("CANCELLED");
        orderRepository.save(orders);
    }
}
