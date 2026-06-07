package com.graduation.crafttrace.service;

import com.graduation.crafttrace.dto.CraftUpdateReq;
import com.graduation.crafttrace.entity.CraftItem;
import com.graduation.crafttrace.repository.CraftItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class CraftItemService {

    private final CraftItemRepository craftItemRepository;

    public CraftItemService(CraftItemRepository craftItemRepository) {
        this.craftItemRepository = craftItemRepository;
    }

    public List<CraftItem> findAll() {
        return craftItemRepository.findAll();
    }

    public CraftItem findById(Long id) {
        return craftItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "未找到对应工艺品，ID=" + id));
    }

    public CraftItem create(CraftItem item, Long userId, String role) {
        if (item.getCode() == null || item.getCode().trim().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "工艺品编号不能为空");
        }
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "工艺品名称不能为空");
        }
        if (craftItemRepository.findByCode(item.getCode().trim()).isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST, "工艺品编号已存在");
        }

        item.setCode(item.getCode().trim());
        item.setName(item.getName().trim());
        item.setCategory(trimToNull(item.getCategory()));
        item.setDescription(trimToNull(item.getDescription()));
        item.setImageUrl(trimToNull(item.getImageUrl()));
        applyOwnershipOnCreate(item, userId, role);
        normalizeTraceFields(item);
        item.setCreatedBy(userId);
        return craftItemRepository.save(item);
    }

    public CraftItem update(Long id, CraftUpdateReq req, Long userId, String role) {
        CraftItem item = findById(id);
        ensureCanOperate(item, userId, role);

        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            item.setName(req.getName().trim());
        }
        if (req.getCategory() != null) {
            item.setCategory(trimToNull(req.getCategory()));
        }
        if (req.getDescription() != null) {
            item.setDescription(trimToNull(req.getDescription()));
        }
        if (req.getImageUrl() != null) {
            item.setImageUrl(trimToNull(req.getImageUrl()));
        }

        item.setArtisanId(req.getArtisanId());
        item.setArtisanName(req.getArtisanName());
        item.setArtisanTitle(req.getArtisanTitle());
        item.setWorkshopName(req.getWorkshopName());
        item.setDisplayLocation(req.getDisplayLocation());
        item.setQrPlacement(req.getQrPlacement());
        item.setMaterialSourceSummary(req.getMaterialSourceSummary());
        item.setMaterialSources(req.getMaterialSources());
        item.setTraceNotice(req.getTraceNotice());

        applyOwnershipOnUpdate(item, userId, role);
        normalizeTraceFields(item);
        return craftItemRepository.save(item);
    }

    public void delete(Long id, Long userId, String role) {
        CraftItem item = findById(id);
        ensureCanOperate(item, userId, role);
        craftItemRepository.deleteById(id);
    }

    private void ensureCanOperate(CraftItem item, Long userId, String role) {
        if (isAdmin(role)) {
            return;
        }
        if (!isHandicraft(role)) {
            throw new ResponseStatusException(FORBIDDEN, "仅管理员或匠人可操作工艺品档案");
        }
        if (userId == null) {
            throw new ResponseStatusException(FORBIDDEN, "未识别到当前登录匠人");
        }

        boolean owned =
                (item.getCreatedBy() != null && item.getCreatedBy().equals(userId)) ||
                (item.getArtisanId() != null && item.getArtisanId().equals(userId));
        if (!owned) {
            throw new ResponseStatusException(FORBIDDEN, "匠人仅可维护自己录入或自己负责的工艺品");
        }
    }

    private void applyOwnershipOnCreate(CraftItem item, Long userId, String role) {
        if (!isHandicraft(role) || userId == null) {
            return;
        }
        if (item.getArtisanId() == null) {
            item.setArtisanId(userId);
        } else if (!userId.equals(item.getArtisanId())) {
            throw new ResponseStatusException(FORBIDDEN, "匠人新增工艺品时只能绑定自己");
        }
    }

    private void applyOwnershipOnUpdate(CraftItem item, Long userId, String role) {
        if (!isHandicraft(role) || userId == null) {
            return;
        }
        if (item.getArtisanId() == null) {
            item.setArtisanId(userId);
        } else if (!userId.equals(item.getArtisanId())) {
            throw new ResponseStatusException(FORBIDDEN, "匠人只能将工艺品绑定为自己负责");
        }
    }

    private boolean isAdmin(String role) {
        return "admin".equalsIgnoreCase(normalizeRole(role));
    }

    private boolean isHandicraft(String role) {
        return "handicraft".equalsIgnoreCase(normalizeRole(role));
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase(Locale.ROOT);
    }

    private void normalizeTraceFields(CraftItem item) {
        item.setArtisanName(trimToNull(item.getArtisanName()));
        item.setArtisanTitle(trimToNull(item.getArtisanTitle()));
        item.setWorkshopName(trimToNull(item.getWorkshopName()));
        item.setDisplayLocation(trimToNull(item.getDisplayLocation()));
        item.setQrPlacement(trimToNull(item.getQrPlacement()));
        item.setMaterialSourceSummary(trimToNull(item.getMaterialSourceSummary()));
        item.setMaterialSources(normalizeTextBlock(item.getMaterialSources()));
        item.setTraceNotice(trimToNull(item.getTraceNotice()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeTextBlock(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace("\r", "").trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
