package com.graduation.crafttrace.config;

import com.graduation.crafttrace.User;
import com.graduation.crafttrace.entity.Course;
import com.graduation.crafttrace.entity.CraftItem;
import com.graduation.crafttrace.entity.Teacher;
import com.graduation.crafttrace.entity.Workshop;
import com.graduation.crafttrace.repository.CourseRepository;
import com.graduation.crafttrace.repository.CraftItemRepository;
import com.graduation.crafttrace.repository.TeacherRepository;
import com.graduation.crafttrace.repository.UserRepository;
import com.graduation.crafttrace.repository.WorkshopRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class DemoTraceDataInitializer implements ApplicationRunner {

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final CraftItemRepository craftItemRepository;
    private final UserRepository userRepository;
    private final WorkshopRepository workshopRepository;

    public DemoTraceDataInitializer(
            TeacherRepository teacherRepository,
            CourseRepository courseRepository,
            CraftItemRepository craftItemRepository,
            UserRepository userRepository,
            WorkshopRepository workshopRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.craftItemRepository = craftItemRepository;
        this.userRepository = userRepository;
        this.workshopRepository = workshopRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, Teacher> teachers = seedTeachers();
        seedCourseTeacherMeta(teachers);
        seedCraftTraceMeta();
    }

    private Map<String, Teacher> seedTeachers() {
        Map<String, Teacher> teacherMap = new LinkedHashMap<>();

        Teacher zhuangjinTeacher = saveTeacher(
                "李雅琴",
                "南宁市青秀区第二中学",
                "综合实践",
                "13877160021",
                "教师在学校集合点完成签到和安全提醒后，带队前往壮锦研学工坊，依次完成非遗背景讲解、材料识别、分组体验、作品展示柜扫码溯源和课程总结。",
                "负责壮锦、织锦与民族手工课程的线下带队组织，适合初中与高中综合实践、劳动教育和校外研学活动。",
                "建议在课程开始前完成分组点名，并在体验环节提醒学生规范使用剪刀、卷尺和手缝工具。"
        );
        teacherMap.put("zhuangjin", zhuangjinTeacher);

        Teacher woodcarvingTeacher = saveTeacher(
                "陈志远",
                "南宁市第十四中学",
                "劳动教育",
                "13877160037",
                "教师带领学生到木雕工坊签到后，由匠人先讲解工具安全规范，再进入木雕基础体验、成品展示和公开溯源码讲解环节。",
                "负责木雕、工具体验与劳动教育方向的研学带队，强调操作规范、过程观察和成果展示。",
                "适合劳动教育课程、社团实践和校外研学展示，建议在课堂前统一发放防护围裙和记录卡。"
        );
        teacherMap.put("woodcarving", woodcarvingTeacher);

        Teacher embroideryTeacher = saveTeacher(
                "顾婉清",
                "南宁市民族高级中学",
                "美术",
                "13877160052",
                "教师组织学生到苏绣研修工坊集中后，先讲解纹样寓意与刺绣礼仪，再安排分组体验、作品比对和成果讲评。",
                "负责苏绣、刺绣审美与传统手工美育方向课程，适合艺术社团、美术实践班和传统文化体验课程。",
                "建议带队时保留课堂作品拍照记录，便于后续在答辩展示中说明学生参与过程与成果差异。"
        );
        teacherMap.put("embroidery", embroideryTeacher);

        Teacher ceramicTeacher = saveTeacher(
                "周岚",
                "南宁师范大学附属实验学校",
                "艺术实践",
                "13877160068",
                "教师带队前往陶艺与青花体验工坊，完成素坯观察、绘制示范、烧制流程讲解和展柜扫码溯源展示。",
                "负责青花瓷、陶艺、紫砂等器物工艺课程组织，适合班级研学日、艺术实践课和校园开放展示。",
                "便于串联作品展示、材料来源说明和传统工艺文化讲解，适合在答辩时作为完整案例展示。"
        );
        teacherMap.put("ceramic", ceramicTeacher);
        teacherMap.put("silver", ceramicTeacher);

        return teacherMap;
    }

    private Teacher saveTeacher(
            String name,
            String schoolName,
            String subjectName,
            String phone,
            String leadRoute,
            String workshopFocus,
            String note
    ) {
        Teacher teacher = teacherRepository.findFirstByName(name).orElseGet(Teacher::new);
        teacher.setName(name);
        teacher.setSchoolName(schoolName);
        teacher.setSubjectName(subjectName);
        teacher.setPhone(phone);
        teacher.setLeadRoute(leadRoute);
        teacher.setWorkshopFocus(workshopFocus);
        teacher.setNote(note);
        return teacherRepository.save(teacher);
    }

    private void seedCourseTeacherMeta(Map<String, Teacher> teachers) {
        List<Course> allCourses = courseRepository.findAll();
        boolean changed = false;

        for (Course course : allCourses) {
            String theme = resolveTheme(course.getTitle(), course.getCategory(), course.getIntro());
            Teacher teacher = teachers.getOrDefault(theme, teachers.get("woodcarving"));
            if (teacher == null) {
                continue;
            }

            if (course.getTeacherId() == null) {
                course.setTeacherId(teacher.getId());
                changed = true;
            }
            if (isBlank(course.getTeacherName())) {
                course.setTeacherName(teacher.getName());
                changed = true;
            }
            if (isBlank(course.getTeacherSchoolName())) {
                course.setTeacherSchoolName(teacher.getSchoolName());
                changed = true;
            }
            if (isBlank(course.getTeacherSubjectName())) {
                course.setTeacherSubjectName(teacher.getSubjectName());
                changed = true;
            }
            if (isBlank(course.getLeadRoute())) {
                course.setLeadRoute(buildCourseLeadRoute(course, teacher));
                changed = true;
            }
            if (isBlank(course.getTeacherNote())) {
                course.setTeacherNote(buildCourseTeacherNote(course, teacher));
                changed = true;
            }
        }

        if (changed) {
            courseRepository.saveAll(allCourses);
        }
    }

    private void seedCraftTraceMeta() {
        List<User> artisans = userRepository.findAll().stream()
                .filter(user -> isArtisanRole(user.getRole()))
                .collect(Collectors.toList());
        List<Workshop> workshops = workshopRepository.findAll();
        Map<Long, User> userMap = artisans.stream()
                .filter(user -> user.getId() != null)
                .collect(Collectors.toMap(User::getId, item -> item, (a, b) -> a, LinkedHashMap::new));

        List<CraftItem> crafts = craftItemRepository.findAll();
        boolean changed = false;
        for (CraftItem craft : crafts) {
            String theme = resolveTheme(craft.getName(), craft.getCategory(), craft.getDescription(), craft.getCode());
            User artisan = pickArtisan(artisans, theme);
            Workshop workshop = pickWorkshop(workshops, artisan, theme);

            if (craft.getArtisanId() == null && artisan != null) {
                craft.setArtisanId(artisan.getId());
                changed = true;
            }
            if (isBlank(craft.getArtisanName())) {
                craft.setArtisanName(resolveArtisanName(artisan, theme));
                changed = true;
            }
            if (isBlank(craft.getArtisanTitle())) {
                craft.setArtisanTitle(resolveArtisanTitle(theme));
                changed = true;
            }
            if (isBlank(craft.getWorkshopName())) {
                craft.setWorkshopName(resolveWorkshopName(workshop, theme));
                changed = true;
            }
            if (isBlank(craft.getDisplayLocation())) {
                craft.setDisplayLocation(resolveDisplayLocation(craft, theme));
                changed = true;
            }
            if (isBlank(craft.getQrPlacement())) {
                craft.setQrPlacement("溯源码张贴于成品展示柜铭牌、作品说明卡和展台侧边，现场扫码即可查看作品工艺流程、负责匠人和材料来源。 ");
                changed = true;
            }
            if (isBlank(craft.getMaterialSourceSummary())) {
                craft.setMaterialSourceSummary(resolveMaterialSummary(theme));
                changed = true;
            }
            if (isBlank(craft.getMaterialSources())) {
                craft.setMaterialSources(String.join("\n", resolveMaterialSources(theme)));
                changed = true;
            }
            if (isBlank(craft.getTraceNotice())) {
                craft.setTraceNotice(resolveTraceNotice(theme, workshop, userMap.get(craft.getArtisanId())));
                changed = true;
            }
        }

        if (changed) {
            craftItemRepository.saveAll(crafts);
        }
    }

    private User pickArtisan(List<User> users, String theme) {
        Predicate<User> matchByName = user -> containsAny(join(user.getName(), user.getUsername()), themeKeywords(theme));
        return users.stream()
                .filter(matchByName)
                .findFirst()
                .orElseGet(() -> users.stream().findFirst().orElse(null));
    }

    private Workshop pickWorkshop(List<Workshop> workshops, User artisan, String theme) {
        if (artisan != null) {
            Optional<Workshop> owned = workshops.stream()
                    .filter(item -> Objects.equals(item.getOwnerUserId(), artisan.getId()))
                    .findFirst();
            if (owned.isPresent()) {
                return owned.get();
            }
        }

        return workshops.stream()
                .filter(item -> containsAny(join(item.getName(), item.getIntro(), item.getAddress()), themeKeywords(theme)))
                .findFirst()
                .orElseGet(() -> workshops.stream().findFirst().orElse(null));
    }

    private String resolveTheme(String... texts) {
        String merged = join(texts).toLowerCase(Locale.ROOT);
        if (containsAny(merged, "壮锦", "织锦", "锦", "手工包", "包")) {
            return "zhuangjin";
        }
        if (containsAny(merged, "木雕", "木", "雕刻", "雕")) {
            return "woodcarving";
        }
        if (containsAny(merged, "苏绣", "刺绣", "绣", "针法")) {
            return "embroidery";
        }
        if (containsAny(merged, "青花", "瓷", "陶", "紫砂", "器物")) {
            return "ceramic";
        }
        if (containsAny(merged, "苗银", "银", "錾刻")) {
            return "silver";
        }
        return "woodcarving";
    }

    private String[] themeKeywords(String theme) {
        switch (theme) {
            case "zhuangjin":
                return new String[]{"壮锦", "织锦", "锦", "手工包"};
            case "woodcarving":
                return new String[]{"木雕", "木", "雕刻"};
            case "embroidery":
                return new String[]{"苏绣", "刺绣", "绣"};
            case "ceramic":
                return new String[]{"青花", "瓷", "陶", "紫砂"};
            case "silver":
                return new String[]{"苗银", "银", "錾刻"};
            default:
                return new String[]{theme};
        }
    }

    private String buildCourseLeadRoute(Course course, Teacher teacher) {
        String workshopName = guessWorkshopTitle(course, resolveTheme(course.getTitle(), course.getCategory(), course.getIntro()));
        return String.format(
                "教师在学校集合点完成签到与安全提醒后，带领学生前往%s，由匠人进行工艺讲解、材料识别、分组体验、作品展示柜扫码溯源和课程总结。",
                workshopName
        );
    }

    private String buildCourseTeacherNote(Course course, Teacher teacher) {
        return String.format(
                "%s负责线下带队与课堂组织，适合在答辩中说明“教师组织学生到工坊研学、匠人负责现场教学、学生扫码查看作品溯源”的完整流程。",
                teacher.getName()
        );
    }

    private String guessWorkshopTitle(Course course, String theme) {
        if (course.getWorkshopId() != null) {
            Optional<Workshop> workshop = workshopRepository.findById(course.getWorkshopId());
            if (workshop.isPresent() && !isBlank(workshop.get().getName())) {
                return workshop.get().getName();
            }
        }

        switch (theme) {
            case "zhuangjin":
                return "壮锦手工包研学工坊";
            case "embroidery":
                return "苏绣工艺研修工坊";
            case "ceramic":
                return "青花瓷绘制展示工坊";
            case "silver":
                return "苗银錾刻体验工坊";
            default:
                return "木雕基础体验工坊";
        }
    }

    private String resolveArtisanName(User artisan, String theme) {
        if (artisan != null && !isBlank(artisan.getName())) {
            return artisan.getName();
        }
        switch (theme) {
            case "zhuangjin":
                return "张小成师傅";
            case "embroidery":
                return "沈绣兰老师";
            case "ceramic":
                return "周青禾师傅";
            case "silver":
                return "龙银海师傅";
            default:
                return "黄文林师傅";
        }
    }

    private String resolveArtisanTitle(String theme) {
        switch (theme) {
            case "zhuangjin":
                return "壮锦织造传承人";
            case "embroidery":
                return "苏绣指导老师";
            case "ceramic":
                return "青花瓷绘制匠人";
            case "silver":
                return "苗银錾刻匠人";
            default:
                return "木雕制作匠人";
        }
    }

    private String resolveWorkshopName(Workshop workshop, String theme) {
        if (workshop != null && !isBlank(workshop.getName())) {
            return workshop.getName();
        }
        switch (theme) {
            case "zhuangjin":
                return "壮锦手工包研学工坊";
            case "embroidery":
                return "苏绣工艺研修工坊";
            case "ceramic":
                return "青花瓷绘制展示工坊";
            case "silver":
                return "苗银錾刻体验工坊";
            default:
                return "匠心非遗研学工坊（木雕·壮锦）";
        }
    }

    private String resolveDisplayLocation(CraftItem craft, String theme) {
        String craftName = isBlank(craft.getName()) ? "该作品" : craft.getName();
        switch (theme) {
            case "zhuangjin":
                return craftName + "陈列于壮锦主题展示柜A区，便于学生在课程结束后扫码查看工艺流程与材料来源。";
            case "embroidery":
                return craftName + "陈列于苏绣成果展示架B区，配有针法说明卡与匠人信息牌。";
            case "ceramic":
                return craftName + "陈列于陶艺与青花展台C区，旁侧放置成型工序和烧制说明。";
            case "silver":
                return craftName + "陈列于苗银工艺展示柜D区，可结合纹样卡片查看錾刻步骤。";
            default:
                return craftName + "陈列于木雕作品展示柜A区，配套工具说明与工序展示卡。";
        }
    }

    private String resolveMaterialSummary(String theme) {
        switch (theme) {
            case "zhuangjin":
                return "以壮锦织片、棉布里衬、手缝辅料和装饰配件为主，突出民族纹样与实用成品结合。";
            case "embroidery":
                return "以真丝绣线、绣绷、扇面底布和描样纸为主，突出图样设计与针法表现。";
            case "ceramic":
                return "以素坯器型、青花颜料、勾线毛笔和釉料说明卡为主，突出器物绘制与烧制流程。";
            case "silver":
                return "以银片坯料、纹样纸样、錾刻锤和抛光辅料为主，突出手工敲击与纹样成型过程。";
            default:
                return "以木料坯板、描线纸、雕刻刀具和打磨耗材为主，突出木雕基础操作与层次处理。";
        }
    }

    private List<String> resolveMaterialSources(String theme) {
        switch (theme) {
            case "zhuangjin":
                return Arrays.asList(
                        "主布料：广西本地壮锦织片，由合作织造工坊统一提供。",
                        "辅料：棉布里衬、拉链、包边条和手缝线由课程材料包统一发放。",
                        "工具：剪刀、卷尺、定位夹和针具由工坊课堂统一配置。"
                );
            case "embroidery":
                return Arrays.asList(
                        "底材：真丝扇面与绣绷由苏绣研修工坊按班次统一准备。",
                        "线材：多色真丝绣线和练习用棉线由课堂材料包发放。",
                        "辅助材料：描样纸、转印纸和收纳卡片由工坊统一整理。"
                );
            case "ceramic":
                return Arrays.asList(
                        "器型：素坯盘、素坯杯等基础器型由陶艺合作工作室提前备货。",
                        "绘制材料：青花颜料、勾线毛笔和试色纸由教师与工坊共同核发。",
                        "说明资料：烧制流程卡、釉色示意卡和成品展示牌由展台统一陈列。"
                );
            case "silver":
                return Arrays.asList(
                        "金属材料：银片坯料与练习金属片由苗银体验工坊按作品类型配发。",
                        "纹样资料：传统纹样纸样、定位纸和线稿说明卡由课程包提供。",
                        "工具：錾刻锤、钢錾、抛光布和防护垫由工坊统一保管并课堂发放。"
                );
            default:
                return Arrays.asList(
                        "主材：木料坯板由木雕工坊按课程主题提前裁切并编号。",
                        "辅助材料：描线纸、砂纸、木蜡油和擦拭布由课堂材料箱统一提供。",
                        "工具：基础雕刻刀、木槌和防护手套由匠人现场发放并回收。"
                );
        }
    }

    private String resolveTraceNotice(String theme, Workshop workshop, User artisan) {
        String workshopName = resolveWorkshopName(workshop, theme);
        String artisanName = resolveArtisanName(artisan, theme);
        return String.format(
                "该作品由%s在%s完成示范制作与过程记录，公开溯源页可查看负责匠人、材料来源、关键工序与现场展示位置。",
                artisanName,
                workshopName
        );
    }

    private boolean isArtisanRole(String role) {
        String value = role == null ? "" : role.toLowerCase(Locale.ROOT);
        return value.contains("handicraft") || value.contains("artisan") || value.contains("craft");
    }

    private String join(String... texts) {
        List<String> values = new ArrayList<>();
        for (String text : texts) {
            if (!isBlank(text)) {
                values.add(text.trim());
            }
        }
        return String.join(" ", values);
    }

    private boolean containsAny(String text, String... keywords) {
        if (isBlank(text) || keywords == null) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (!isBlank(keyword) && lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
