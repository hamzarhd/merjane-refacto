package com.nimbleways.springboilerplate.contollers;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.ProductService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class MyController {
    @Autowired
    private ProductService ps;

    @Autowired
    private ProductRepository pr;

    @Autowired
    private OrderRepository or;

    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
        Order order = or.findById(orderId).get();
        System.out.println(order);
        List<Long> ids = new ArrayList<>();
        ids.add(orderId);
        Set<Product> products = order.getItems();
        for (Product p : products) {
        	int available = Optional.ofNullable(p.getAvailable()).orElse(0);
        	int sold = Optional.ofNullable(p.getSold()).orElse(0);
        	int maxQuantity = Optional.ofNullable(p.getMaxQuantity()).orElse(0);
        	LocalDate seasonStart = Optional.ofNullable(p.getSeasonStartDate()).orElse(LocalDate.now());
        	LocalDate seasonEnd = Optional.ofNullable(p.getSeasonEndDate()).orElse(LocalDate.now());
        	LocalDate expiryDate = Optional.ofNullable(p.getExpiryDate()).orElse(LocalDate.now());
        	LocalDateTime endFlashSoldDate = Optional.ofNullable(p.getEndFlashSoldDate()).orElse(LocalDateTime.now());
        	if (p.getType().equals("NORMAL")) {
                if (available > 0) {
                    p.setAvailable(available - 1);
                    pr.save(p);
                } else {
                    int leadTime = Optional.ofNullable(p.getLeadTime()).orElse(0);
                    if (leadTime > 0) {
                        ps.notifyDelay(leadTime, p);
                    }
                }
            } else if (p.getType().equals("SEASONAL")) {
                // Add new season rules
                if ((LocalDate.now().isAfter(seasonStart) && LocalDate.now().isBefore(seasonEnd)
                        && available > 0)) {
                    p.setAvailable(available - 1);
                    pr.save(p);
                } else {
                    ps.handleSeasonalProduct(p);
                }
            } else if (p.getType().equals("EXPIRABLE")) {
                if (available > 0 && expiryDate.isAfter(LocalDate.now())) {
                    p.setAvailable(available - 1);
                    pr.save(p);
                } else {
                    ps.handleExpiredProduct(p);
                }
            } else if (p.getType().equals("FLASHSALE")) {
                if (sold <= maxQuantity && endFlashSoldDate.isAfter(LocalDateTime.now()) && available > 0) {
                			 p.setAvailable(available - 1);
                			 p.setSold(p.getSold() + 1);
                             pr.save(p);
                } else {
                    ps.handleFlashSaleProduct(p);
                }
            }
        }

        return new ProcessOrderResponse(order.getId());
    }
}
