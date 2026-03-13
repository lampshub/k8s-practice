package com.beyond23.orderSystem.ordering.controller;

import com.beyond23.orderSystem.ordering.domain.Ordering;
import com.beyond23.orderSystem.ordering.dtos.OrderingCreateDto;
import com.beyond23.orderSystem.ordering.dtos.OrderingListDto;
import com.beyond23.orderSystem.ordering.service.OrderingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderingController {
    private final OrderingService orderingService;
    @Autowired
    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }


    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody List<OrderingCreateDto> dtoList, @RequestHeader("X-User-Email") String email) {
//        Long id = orderingService.create(dtoList, email);
        Long id = orderingService.createFeign(dtoList, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/list")
//    @PreAuthorize("hasRole('ADMIN')") -> apigateway에서 확인하고 있음.
    public ResponseEntity<?> findAll(){
        List<OrderingListDto> orderingListDtoList = orderingService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(orderingListDtoList);
    }

    @GetMapping("/myorders")
    public ResponseEntity<?> myOrders(@RequestHeader("X-User-Email") String email){
        List<OrderingListDto> orderingListDtoList = orderingService.myOrders(email);
        return ResponseEntity.status(HttpStatus.OK).body(orderingListDtoList);
    }

}
