package com.nimbleways.springboilerplate.entities;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lead_time")
    private Integer leadTime;

    @Column(name = "available")
    private Integer available;

    @Column(name = "type")
    private String type;

    @Column(name = "name")
    private String name;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "season_start_date")
    private LocalDate seasonStartDate;

    @Column(name = "season_end_date")
    private LocalDate seasonEndDate;
    
    @Column(name = "end_flash_sold_date")
    private LocalDateTime endFlashSoldDate;
    
    @Column(name = "max_quantity")
    private Integer maxQuantity;
    
    @Column(name = "sold")
    private Integer sold;
}
