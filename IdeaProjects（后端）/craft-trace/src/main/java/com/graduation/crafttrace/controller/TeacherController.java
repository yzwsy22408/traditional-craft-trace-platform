package com.graduation.crafttrace.controller;

import com.graduation.crafttrace.common.Authz;
import com.graduation.crafttrace.entity.Teacher;
import com.graduation.crafttrace.repository.TeacherRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherRepository teacherRepository;

    public TeacherController(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @GetMapping
    public List<Teacher> list(HttpSession session) {
        Authz.requireAdminOrHandicraft(session);
        return teacherRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt", "id"));
    }

    @PostMapping
    public Teacher create(@RequestBody Teacher teacher, HttpSession session) {
        Authz.requireAdminOrHandicraft(session);
        normalizeTeacher(teacher);
        if (isBlank(teacher.getName())) {
            throw new ResponseStatusException(BAD_REQUEST, "teacher name can not be empty");
        }
        return teacherRepository.save(teacher);
    }

    @PutMapping("/{id}")
    public Teacher update(@PathVariable Long id, @RequestBody Teacher req, HttpSession session) {
        Authz.requireAdminOrHandicraft(session);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "teacher not found"));

        teacher.setName(req.getName());
        teacher.setSchoolName(req.getSchoolName());
        teacher.setSubjectName(req.getSubjectName());
        teacher.setPhone(req.getPhone());
        teacher.setLeadRoute(req.getLeadRoute());
        teacher.setWorkshopFocus(req.getWorkshopFocus());
        teacher.setNote(req.getNote());

        normalizeTeacher(teacher);
        if (isBlank(teacher.getName())) {
            throw new ResponseStatusException(BAD_REQUEST, "teacher name can not be empty");
        }
        return teacherRepository.save(teacher);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, HttpSession session) {
        Authz.requireAdminOrHandicraft(session);
        if (!teacherRepository.existsById(id)) {
            throw new ResponseStatusException(BAD_REQUEST, "teacher not found");
        }
        teacherRepository.deleteById(id);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "OK");
        return resp;
    }

    private void normalizeTeacher(Teacher teacher) {
        if (teacher == null) return;
        teacher.setName(trimToNull(teacher.getName()));
        teacher.setSchoolName(trimToNull(teacher.getSchoolName()));
        teacher.setSubjectName(trimToNull(teacher.getSubjectName()));
        teacher.setPhone(trimToNull(teacher.getPhone()));
        teacher.setLeadRoute(trimToNull(teacher.getLeadRoute()));
        teacher.setWorkshopFocus(trimToNull(teacher.getWorkshopFocus()));
        teacher.setNote(trimToNull(teacher.getNote()));
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
