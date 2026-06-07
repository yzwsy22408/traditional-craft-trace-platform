package com.graduation.crafttrace.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduation.crafttrace.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MaterialService {
    private final MaterialRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public MaterialService(MaterialRepository repo) { this.repo = repo; }

    @Transactional
    public void deductStockFromJson(String json) {
        if (json == null || json.isEmpty()) return;
        try {
            List<String> names = mapper.readValue(json, new TypeReference<List<String>>() {});
            for (String name : names) {
                repo.findByName(name.trim()).ifPresent(m -> {
                    if (m.getStock() > 0) {
                        m.setStock(m.getStock() - 1);
                        repo.save(m);
                    }
                });
            }
        } catch (Exception ignored) {}
    }
}