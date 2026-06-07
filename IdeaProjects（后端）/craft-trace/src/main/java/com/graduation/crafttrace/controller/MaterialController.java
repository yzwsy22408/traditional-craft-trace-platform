package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.entity.Material;
import com.graduation.crafttrace.repository.MaterialRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/material")
public class MaterialController {

    private final MaterialRepository repo;

    public MaterialController(MaterialRepository repo) {
        this.repo = repo;
    }

    // ✅ 列表 + 可选搜索：?q=xxx
    @GetMapping
    public List<Material> list(@RequestParam(value = "q", required = false) String q) {
        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");

        if (q == null || q.trim().isEmpty()) {
            return repo.findAll(sort);
        }

        String kw = q.trim().toLowerCase();
        Specification<Material> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.like(cb.lower(root.get("name")), "%" + kw + "%"));
            ps.add(cb.like(cb.lower(root.get("category")), "%" + kw + "%"));
            ps.add(cb.like(cb.lower(root.get("supplier")), "%" + kw + "%"));
            return cb.or(ps.toArray(new Predicate[0]));
        };

        return repo.findAll(spec, sort);
    }

    // ✅ 新增
    @PostMapping
    public Material create(@RequestBody Material body) {
        body.setId(null); // 避免前端乱传 id
        return repo.save(body);
    }

    // ✅ 更新
    @PutMapping("/{id}")
    public Material update(@PathVariable Long id, @RequestBody Material body) {
        Material old = repo.findById(id).orElseThrow(() -> new RuntimeException("材料不存在"));
        old.setName(body.getName());
        old.setCategory(body.getCategory());
        old.setUnit(body.getUnit());
        old.setStock(body.getStock());
        old.setPrice(body.getPrice());
        old.setSupplier(body.getSupplier());
        old.setDescription(body.getDescription());
        old.setImageUrl(body.getImageUrl());
        return repo.save(old);
    }

    // ✅ 删除
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
