package com.beyond23.orderSystem.ordering.domain;

import com.beyond23.orderSystem.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderingDetail extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderingId", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Ordering ordering;

    @Column(nullable = false)
    private int quantity;

    private Long productId;
    // msa환경에서는 빈번한 http요청에 의한 성능저하를 막기위한 반정규화 설계도 가능
    private String productName;

}
