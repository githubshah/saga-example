package com.order.saga;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class OrderCreatedEvent {
    private Long orderId;
    private double amount;
}
