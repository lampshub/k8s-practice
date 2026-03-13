package com.beyond23.orderSystem.product.controller;

import com.beyond23.orderSystem.common.dtos.CommonErrorDto;
import com.beyond23.orderSystem.product.dtos.*;
import com.beyond23.orderSystem.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@ModelAttribute ProductCreateDto dto, @RequestHeader("X-User-Email") String email) {
        Long productId = productService.save(dto,email);
        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    }

    @GetMapping("/list")
    public ResponseEntity<?> findAll(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable, @ModelAttribute ProductSearchDto searchDto) {
        System.out.println("dto : " + searchDto);
        Page<ProductResDto> productResDtoList = productService.findAll(pageable, searchDto);
        return ResponseEntity.status(HttpStatus.OK).body(productResDtoList);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
//        try {
        ProductResDto dto = productService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

//    상품 수정 update
    @PutMapping("update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @ModelAttribute ProductUpdateDto dto) {
        productService.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

//    상품 재고 변경
    @PutMapping("updatestock")
    public ResponseEntity<?> updateStock(@RequestBody ProductStockUpdateDto dto) {
        System.out.println(dto);
        productService.updateStock(dto);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

}
