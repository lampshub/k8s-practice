package com.beyond23.orderSystem.ordering.feignclients;

import com.beyond23.orderSystem.ordering.dtos.OrderingCreateDto;
import com.beyond23.orderSystem.ordering.dtos.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

//name부분은 eureka에 등록된 application name을 의미
//url부분은 k8s의 서비스명
//@Profile("local") / ("prod") -> 어노테이션사용하면 어떤걸 선택할지 지정할수있음
//url 부분 yml 파일에서 가져옴
@FeignClient(name = "product-service", url= "${product.service.url:}")
public interface ProductFeignClient {
//    product controller의 url과 매핑
    @GetMapping("/product/detail/{id}")
    ProductDto getProductById(@PathVariable("id")Long id);

    @PutMapping("/product/updatestock")
    void updateStockQuantity(@RequestBody OrderingCreateDto dto);

}
