package com.payment.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topic.order-cancelled}")
    private String orderCancelledTopic;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.order-created}", groupId = "payment-group")
    public void handleOrderCreated(Object consumerRecord) {
        OrderCreatedEvent event = objectMapper.convertValue(((ConsumerRecord<?, ?>) consumerRecord).value(), OrderCreatedEvent.class);
        System.out.println("payment-service:event-consumed : " + event);
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
