package com.payment.saga;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, OrderCancelledEvent> kafkaTemplate;

    @Value("${kafka.topic.order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topic.order-cancelled}")
    private String orderCancelledTopic;

    @KafkaListener(topics = "${kafka.topic.order-created}", groupId = "payment-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("event-consumed : " + event);
        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setAmount(event.getAmount());
        payment.setStatus("PENDING");
        paymentRepository.save(payment);

        try {
            // Simulate payment processing
            boolean paymentSuccess = processPayment(event.getAmount());

            if (paymentSuccess) {
                payment.setStatus("SUCCESS");
                paymentRepository.save(payment);
            } else {
                throw new Exception("Payment failed");
            }

        } catch (Exception e) {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
            kafkaTemplate.send(orderCancelledTopic, new OrderCancelledEvent(event.getOrderId()));
        }
    }

    private boolean processPayment(double amount) {
        // Simulate payment processing logic
        return amount <= 100; // Assume payment fails for amounts > 100
    }
}
