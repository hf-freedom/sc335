package com.housekeeping.service;

import com.housekeeping.entity.Aunt;
import com.housekeeping.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuntService {

    @Autowired
    private InMemoryStorage storage;

    public Aunt createAunt(Aunt aunt) {
        aunt.setId(UUID.randomUUID().toString().replace("-", ""));
        aunt.setCreatedAt(LocalDateTime.now());
        aunt.setUpdatedAt(LocalDateTime.now());
        if (aunt.getRating() == null) {
            aunt.setRating(new BigDecimal("5.0"));
        }
        if (aunt.getMaxDailyOrders() == 0) {
            aunt.setMaxDailyOrders(3);
        }
        storage.saveAunt(aunt);
        return aunt;
    }

    public Aunt getAunt(String id) {
        return storage.getAunt(id);
    }

    public List<Aunt> getAllAunts() {
        return storage.getAllAunts();
    }

    public Aunt updateAunt(Aunt aunt) {
        Aunt existing = storage.getAunt(aunt.getId());
        if (existing == null) {
            throw new RuntimeException("阿姨不存在");
        }
        aunt.setCreatedAt(existing.getCreatedAt());
        aunt.setUpdatedAt(LocalDateTime.now());
        storage.saveAunt(aunt);
        return aunt;
    }
}
