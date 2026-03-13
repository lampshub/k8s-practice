package com.beyond23.orderSystem.ordering.service;

import com.beyond23.orderSystem.common.service.SseAlarmService;
import com.beyond23.orderSystem.ordering.domain.Ordering;
import com.beyond23.orderSystem.ordering.domain.OrderingDetail;
import com.beyond23.orderSystem.ordering.dtos.OrderingCreateDto;
import com.beyond23.orderSystem.ordering.dtos.OrderingListDto;
import com.beyond23.orderSystem.ordering.dtos.ProductDto;
import com.beyond23.orderSystem.ordering.feignclients.ProductFeignClient;
import com.beyond23.orderSystem.ordering.repository.OrderDetailRepository;
import com.beyond23.orderSystem.ordering.repository.OrderingRepository;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SseAlarmService sseAlarmService;
    private final RestTemplate restTemplate;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public OrderingService(OrderingRepository orderingRepository, OrderDetailRepository orderDetailRepository, SseAlarmService sseAlarmService, RestTemplate restTemplate, ProductFeignClient productFeignClient, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderingRepository = orderingRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.sseAlarmService = sseAlarmService;
        this.restTemplate = restTemplate;
        this.productFeignClient = productFeignClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Long create(List<OrderingCreateDto> dtoList, String email){
        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();
        orderingRepository.save(ordering);
//
        for(OrderingCreateDto dto : dtoList){
//            1.재고조회요청(동기요청-http요청)
//            http://localhost:8080/product-service : apigateway를 통한 호출
//            http://product-service : eureka에게 질의 후 product-service 직접 호출
            String endpoitnt1 = "http://product-service/product/detail/" + dto.getProductId();
            HttpHeaders headers = new HttpHeaders();
//            HttpEntity : header + body
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ProductDto> responseEntity = restTemplate.exchange(endpoitnt1, HttpMethod.GET, httpEntity, ProductDto.class);    //자동으로 body꺼내서 파싱해줌 -> product로 받을수 있음
            ProductDto product = responseEntity.getBody();
            System.out.println(product.getStockQuantity());
            if(product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다");
            }
//            2.주문발생
            OrderingDetail orderingDetail = OrderingDetail.builder()
                    .ordering(ordering)
                    .productName(product.getName())  //위에 http요청을 통해 가져온 product 안에 포함되어있음
                    .productId(dto.getProductId())
                    .quantity(dto.getProductCount())
                    .build();
            orderDetailRepository.save(orderingDetail);
//            3.재고감소요청(동기-http요청/비동기-이벤트기반 모두 가능) post/put/patch요청. body에는 productId, productCount
            String endpoitnt2 = "http://product-service/product/updatestock";
            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity : header + body
            HttpEntity<OrderingCreateDto> httpEntity2 = new HttpEntity<>(dto, headers2);    //자동으로 직렬화 됨
            restTemplate.exchange(endpoitnt2, HttpMethod.PUT, httpEntity2, Void.class);
        }
        return ordering.getId();
    }


    public Long createFeign(List<OrderingCreateDto> dtoList, String email){
        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();
        orderingRepository.save(ordering);

        for(OrderingCreateDto dto : dtoList){
            ProductDto product = productFeignClient.getProductById(dto.getProductId());
            if(product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다");
            }

            OrderingDetail orderingDetail = OrderingDetail.builder()
                    .ordering(ordering)
                    .productName(product.getName())  //위에 http요청을 통해 가져온 product 안에 포함되어있음
                    .productId(dto.getProductId())
                    .quantity(dto.getProductCount())
                    .build();
            orderDetailRepository.save(orderingDetail);
//            feign을 사용한 동기적 재고감소 요청 방식
//            productFeignClient.updateStockQuantity(dto);
//            kafka를 활용한 비동기적 재고감소 요청
            kafkaTemplate.send("stock-update-topic", dto);
        }
        return ordering.getId();
    }


    @Transactional(readOnly = true)
    public List<OrderingListDto> findAll(){
        return orderingRepository.findAll().stream().map(o->OrderingListDto.fromEntity(o)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderingListDto> myOrders(String email){
        return orderingRepository.findAllByMemberEmail(email).stream().map(o->OrderingListDto.fromEntity(o)).collect(Collectors.toList());
    }
}
