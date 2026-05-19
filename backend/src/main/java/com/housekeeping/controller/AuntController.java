package com.housekeeping.controller;

import com.housekeeping.common.Result;
import com.housekeeping.entity.Aunt;
import com.housekeeping.service.AuntService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aunts")
@CrossOrigin(origins = "*")
public class AuntController {

    @Autowired
    private AuntService auntService;

    @PostMapping
    public Result<Aunt> createAunt(@RequestBody Aunt aunt) {
        try {
            Aunt created = auntService.createAunt(aunt);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Aunt> getAunt(@PathVariable String id) {
        Aunt aunt = auntService.getAunt(id);
        if (aunt == null) {
            return Result.error("阿姨不存在");
        }
        return Result.success(aunt);
    }

    @GetMapping
    public Result<List<Aunt>> getAllAunts() {
        return Result.success(auntService.getAllAunts());
    }

    @PutMapping
    public Result<Aunt> updateAunt(@RequestBody Aunt aunt) {
        try {
            Aunt updated = auntService.updateAunt(aunt);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
