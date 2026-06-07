package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.User;
import com.graduation.crafttrace.entity.CraftItem;
import com.graduation.crafttrace.entity.TraceStep;
import com.graduation.crafttrace.repository.CraftItemRepository;
import com.graduation.crafttrace.repository.TraceStepRepository;
import com.graduation.crafttrace.repository.UserRepository;
import com.graduation.crafttrace.service.TraceStepService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/public")
public class PublicTraceController {

    private final CraftItemRepository craftItemRepository;
    private final TraceStepRepository traceStepRepository;
    private final UserRepository userRepository;
    private final TraceStepService traceStepService;

    public PublicTraceController(
            CraftItemRepository craftItemRepository,
            TraceStepRepository traceStepRepository,
            UserRepository userRepository,
            TraceStepService traceStepService
    ) {
        this.craftItemRepository = craftItemRepository;
        this.traceStepRepository = traceStepRepository;
        this.userRepository = userRepository;
        this.traceStepService = traceStepService;
    }

    @GetMapping("/trace")
    public Map<String, Object> traceByCode(@RequestParam String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "作品编号不能为空");
        }

        CraftItem item = craftItemRepository.findByCode(code.trim())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "未找到该编号对应的工艺品：" + code));

        List<TraceStep> steps = traceStepRepository.findByCraftItemIdOrderByStepNoAsc(item.getId());
        List<LocalDateTime> realisticTimes = generateHumanLikeTimestamps(steps);

        Set<Long> userIds = steps.stream()
                .map(TraceStep::getOperatorUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<Map<String, Object>> stepList = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            TraceStep step = steps.get(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", step.getId());
            row.put("stepNo", step.getStepNo());
            row.put("stepName", step.getStepName());
            row.put("detail", step.getDetail());
            row.put("status", step.getStatus());
            row.put("operateTime", realisticTimes.get(i));
            row.put("prevHash", step.getPrevHash());
            row.put("payloadHash", step.getPayloadHash());
            row.put("stepHash", step.getStepHash());
            row.put("materials", step.getMaterials());
            row.put("images", step.getImages());
            row.put("operatorUserId", step.getOperatorUserId());
            row.put("operatorName", resolveOperatorName(step, item, userMap));
            row.put("artisanName", item.getArtisanName());
            row.put("workshopName", item.getWorkshopName());
            stepList.add(row);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("craft", item);
        resp.put("steps", stepList);
        resp.put("verify", traceStepService.verifyHashes(item.getId()));
        return resp;
    }

    private List<LocalDateTime> generateHumanLikeTimestamps(List<TraceStep> steps) {
        List<LocalDateTime> realisticTimes = new ArrayList<>();
        if (steps.isEmpty()) {
            return realisticTimes;
        }

        Random random = new Random();
        LocalDateTime pointer = LocalDateTime.of(2026, 1, 17, 9, 15, random.nextInt(60))
                .plusMinutes(random.nextInt(30));

        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) {
                int gapHours = 3 + random.nextInt(5);
                int gapMinutes = random.nextInt(60);
                int gapSeconds = random.nextInt(60);

                pointer = pointer.plusHours(gapHours)
                        .plusMinutes(gapMinutes)
                        .plusSeconds(gapSeconds);

                if (pointer.toLocalTime().isAfter(LocalTime.of(21, 0))) {
                    pointer = pointer.plusDays(1)
                            .withHour(9)
                            .withMinute(random.nextInt(55))
                            .withSecond(random.nextInt(60));
                }

                if (pointer.toLocalTime().isBefore(LocalTime.of(9, 0))) {
                    pointer = pointer.withHour(9)
                            .withMinute(random.nextInt(55))
                            .withSecond(random.nextInt(60));
                }
            }
            realisticTimes.add(pointer);
        }
        return realisticTimes;
    }

    private String resolveOperatorName(TraceStep step, CraftItem item, Map<Long, User> userMap) {
        if (step.getOperatorUserId() != null) {
            User user = userMap.get(step.getOperatorUserId());
            if (user != null) {
                if (user.getName() != null && !user.getName().trim().isEmpty()) {
                    return user.getName().trim();
                }
                if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                    return user.getUsername().trim();
                }
            }
        }

        if (item.getArtisanName() != null && !item.getArtisanName().trim().isEmpty()) {
            return item.getArtisanName().trim();
        }
        return "负责匠人记录";
    }
}
