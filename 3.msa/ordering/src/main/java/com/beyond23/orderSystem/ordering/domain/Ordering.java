package com.beyond23.orderSystem.ordering.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ordering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    msa모듈 간의 관계성 제거
    private String memberEmail; //member와 관계성을 제거

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @CreationTimestamp
    private LocalDateTime createdTime;

    @OneToMany(mappedBy = "ordering", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<OrderingDetail> orderingDetailList = new ArrayList<>();    //order와 orderDetail은 한 서버로 묶어둘수있음
}
