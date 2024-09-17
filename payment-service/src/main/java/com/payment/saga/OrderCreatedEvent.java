package com.payment.saga;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderCreatedEvent {
    private Long orderId;
    private double amount;
}
