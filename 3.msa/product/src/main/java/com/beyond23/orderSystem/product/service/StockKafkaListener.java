package com.beyond23.orderSystem.product.service;

import com.beyond23.orderSystem.product.dtos.ProductStockUpdateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockKafkaListener {

    private final ProductService productService;
    private final ObjectMapper objectMapper;
    @Autowired
    public StockKafkaListener(ProductService productService, ObjectMapper objectMapper){
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "stock-update-topic", containerFactory = "kafkaListener")
    public void stockConsumer(String message) throws JsonProcessingException {
        System.out.println("==========kafka listener start===========");
        System.out.println(message);
        ProductStockUpdateDto dto = objectMapper.readValue(message, ProductStockUpdateDto.class);
        productService.updateStock(dto);
    }

}
