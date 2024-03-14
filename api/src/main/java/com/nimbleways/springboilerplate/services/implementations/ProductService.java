package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ProductService {

    @Autowired
    ProductRepository pr;

    @Autowired
    NotificationService ns;

    public void notifyDelay(int leadTime, Product p) {
        p.setLeadTime(leadTime);
        pr.save(p);
        ns.sendDelayNotification(leadTime, p.getName());
    }

    public void handleSeasonalProduct(Product p) {
    	LocalDate seasonStart = Optional.ofNullable(p.getSeasonStartDate()).orElse(LocalDate.now());
    	LocalDate seasonEnd = Optional.ofNullable(p.getSeasonEndDate()).orElse(LocalDate.now());
        if (LocalDate.now().plusDays(p.getLeadTime()).isAfter(seasonEnd)) {
            ns.sendOutOfStockNotification(p.getName());
            p.setAvailable(0);
            pr.save(p);
        } else if (seasonStart.isAfter(LocalDate.now())) {
            ns.sendOutOfStockNotification(p.getName());
            pr.save(p);
        } else {
            notifyDelay(p.getLeadTime(), p);
        }
    }

    public void handleExpiredProduct(Product p) {
    	int available = Optional.ofNullable(p.getAvailable()).orElse(0);
    	LocalDate expiryDate = Optional.ofNullable(p.getExpiryDate()).orElse(LocalDate.now());
        if (available > 0 && expiryDate.isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            pr.save(p);
        } else {
            ns.sendExpirationNotification(p.getName(), p.getExpiryDate());
            p.setAvailable(0);
            pr.save(p);
        }
    }
    public void handleFlashSaleProduct(Product p) {
    	int sold = Optional.ofNullable(p.getSold()).orElse(0);
    	int maxQuantity = Optional.ofNullable(p.getMaxQuantity()).orElse(0);
    	LocalDateTime endFlashSoldDate = Optional.ofNullable(p.getEndFlashSoldDate()).orElse(LocalDateTime.now());
    	if (LocalDateTime.now().plusDays(p.getLeadTime()).isBefore(p.getEndFlashSoldDate())) {
            notifyDelay(p.getLeadTime(), p);
        }
    	else if (sold > maxQuantity ||  LocalDateTime.now().isAfter(endFlashSoldDate)
    			|| LocalDateTime.now().plusDays(p.getLeadTime()).isAfter(endFlashSoldDate)) {
            ns.sendOutOfStockNotification(p.getName());
            p.setAvailable(0);
            pr.save(p);
        } 
    }
}