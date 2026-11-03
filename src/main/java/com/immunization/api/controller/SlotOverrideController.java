package com.immunization.api.controller;

import com.immunization.api.repository.SlotOverrideRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.immunization.api.entity.SlotOverride;
import com.immunization.api.service.SlotOverrideService;
import com.immunization.api.dto.OverrideRequest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController 
@RequestMapping ("/api/overrides")
public class SlotOverrideController {

    private final SlotOverrideRepository slotOverrideRepository;
    private final SlotOverrideService slotOverrideService;

    SlotOverrideController(SlotOverrideService slotOverrideService, SlotOverrideRepository slotOverrideRepository) {
        this.slotOverrideService = slotOverrideService;
        this.slotOverrideRepository = slotOverrideRepository;
    }

    @GetMapping
    public List<SlotOverride> getAllOverrides() {
        return slotOverrideRepository.findAll();
    }
    

    @PostMapping
    public SlotOverride createOverride(@RequestBody OverrideRequest overrideRequest) {
        return slotOverrideService.createOverride(overrideRequest);
    }
    
    
}
