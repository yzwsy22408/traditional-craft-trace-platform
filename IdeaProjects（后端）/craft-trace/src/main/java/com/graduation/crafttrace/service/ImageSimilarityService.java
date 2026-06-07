package com.graduation.crafttrace.service;

import com.graduation.crafttrace.User;
import com.graduation.crafttrace.entity.CraftItem;
import com.graduation.crafttrace.entity.Course;
import com.graduation.crafttrace.entity.StudentWork;
import com.graduation.crafttrace.repository.CraftItemRepository;
import com.graduation.crafttrace.repository.CourseRepository;
import com.graduation.crafttrace.repository.StudentWorkRepository;
import com.graduation.crafttrace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class ImageSimilarityService {

    private static final int HASH_SIZE = 8;
    private static final int HISTOGRAM_BINS_PER_CHANNEL = 4;
    private static final int MAX_LIMIT = 20;
    private static final double HASH_WEIGHT = 0.65D;
    private static final double COLOR_WEIGHT = 0.35D;

    private final CraftItemRepository craftItemRepository;
    private final StudentWorkRepository studentWorkRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final Map<String, ImageFeature> featureCache = new ConcurrentHashMap<>();

    public ImageSimilarityService(
            CraftItemRepository craftItemRepository,
            StudentWorkRepository studentWorkRepository,
            CourseRepository courseRepository,
            UserRepository userRepository
    ) {
        this.craftItemRepository = craftItemRepository;
        this.studentWorkRepository = studentWorkRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> search(MultipartFile file, Integer limit) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "请先上传待检索图片");
        }

        long start = System.currentTimeMillis();
        BufferedImage queryImage = readQueryImage(file);
        ImageFeature queryFeature = extractFeature(queryImage);

        List<CandidateRecord> records = loadCandidates();
        List<Map<String, Object>> results = records.stream()
                .map(record -> buildResultRow(record, queryFeature))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(row -> -toDouble(row.get("similarityScore"))))
                .limit(normalizeLimit(limit))
                .collect(Collectors.toList());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("queryImageName", file.getOriginalFilename());
        payload.put("queryFeature", buildFeatureSummary(queryFeature));
        payload.put("scannedCount", records.size());
        payload.put("resultCount", results.size());
        payload.put("elapsedMs", System.currentTimeMillis() - start);
        payload.put("results", results);
        return payload;
    }

    private BufferedImage readQueryImage(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new ResponseStatusException(BAD_REQUEST, "上传文件不是可识别的图片格式");
            }
            return ensureRgb(image);
        } catch (IOException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "读取上传图片失败");
        }
    }

    private List<CandidateRecord> loadCandidates() {
        List<CandidateRecord> records = new ArrayList<>();
        records.addAll(loadCraftCandidates());
        records.addAll(loadStudentWorkCandidates());
        return records;
    }

    private Collection<CandidateRecord> loadCraftCandidates() {
        return craftItemRepository.findAll().stream()
                .filter(item -> !isBlank(item.getImageUrl()))
                .map(item -> new CandidateRecord(
                        "craft",
                        "craft-" + item.getId(),
                        item.getImageUrl(),
                        item.getName(),
                        item.getCategory(),
                        item.getCode(),
                        item.getDescription(),
                        item.getCreatedAt() == null ? "" : String.valueOf(item.getCreatedAt()),
                        item.getArtisanName(),
                        item.getWorkshopName(),
                        item.getId()
                ))
                .collect(Collectors.toList());
    }

    private Collection<CandidateRecord> loadStudentWorkCandidates() {
        List<StudentWork> works = studentWorkRepository.findAll();
        Set<Long> courseIds = works.stream().map(StudentWork::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = works.stream().map(StudentWork::getStudentUserId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getTitle));
        Map<Long, String> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, this::pickDisplayName));

        return works.stream()
                .filter(item -> !isBlank(item.getImageUrl()))
                .map(item -> new CandidateRecord(
                        "studentWork",
                        "student-work-" + item.getId(),
                        item.getImageUrl(),
                        item.getWorkTitle(),
                        courseMap.getOrDefault(item.getCourseId(), "课程作品"),
                        "",
                        firstNonBlank(item.getReflection(), item.getGainText(), "课堂成果留存记录"),
                        item.getUpdatedAt() == null ? "" : String.valueOf(item.getUpdatedAt()),
                        userMap.getOrDefault(item.getStudentUserId(), "学员"),
                        "",
                        item.getId()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildResultRow(CandidateRecord record, ImageFeature queryFeature) {
        ImageFeature targetFeature = loadCandidateFeature(record);
        if (targetFeature == null) {
            return null;
        }

        double hashScore = 1D - ((double) hammingDistance(queryFeature.hashValue, targetFeature.hashValue) / 64D);
        double colorScore = cosineSimilarity(queryFeature.colorHistogram, targetFeature.colorHistogram);
        double similarityScore = (hashScore * HASH_WEIGHT) + (colorScore * COLOR_WEIGHT);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("sourceType", record.sourceType);
        row.put("sourceId", record.sourceId);
        row.put("title", record.title);
        row.put("subtitle", record.subtitle);
        row.put("code", record.code);
        row.put("summary", record.summary);
        row.put("imageUrl", record.imageUrl);
        row.put("updatedAt", record.updatedAt);
        row.put("ownerName", record.ownerName);
        row.put("workshopName", record.workshopName);
        row.put("similarityScore", round(similarityScore));
        row.put("hashScore", round(hashScore));
        row.put("colorScore", round(colorScore));
        row.put("hashHex", toUnsignedHex(targetFeature.hashValue));
        return row;
    }

    private ImageFeature loadCandidateFeature(CandidateRecord record) {
        return featureCache.computeIfAbsent(record.cacheKey, key -> {
            BufferedImage image = loadCandidateImage(record.imageUrl);
            return image == null ? null : extractFeature(image);
        });
    }

    private BufferedImage loadCandidateImage(String imageUrl) {
        String value = String.valueOf(imageUrl == null ? "" : imageUrl).trim();
        if (value.isEmpty()) {
            return null;
        }

        try {
            if (value.startsWith("http://") || value.startsWith("https://")) {
                return ensureRgb(ImageIO.read(new URL(value)));
            }

            for (Path path : resolveCandidatePaths(value)) {
                File file = path.toFile();
                if (!file.exists() || !file.isFile()) {
                    continue;
                }
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    return ensureRgb(image);
                }
            }
        } catch (IOException ignored) {
            return null;
        }

        return null;
    }

    private List<Path> resolveCandidatePaths(String rawPath) {
        String normalized = rawPath.replace("\\", "/").trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        Path backendRoot = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path workspaceRoot = Optional.ofNullable(backendRoot.getParent())
                .map(Path::getParent)
                .orElse(backendRoot)
                .normalize();

        List<Path> candidates = new ArrayList<>();
        candidates.add(backendRoot.resolve("." + normalized).normalize());
        candidates.add(workspaceRoot.resolve("." + normalized).normalize());

        if (normalized.startsWith("/images/")) {
            candidates.add(workspaceRoot.resolve("public").resolve(normalized.substring(1)).normalize());
            candidates.add(workspaceRoot.resolve("dist").resolve(normalized.substring(1)).normalize());
        }
        if (normalized.startsWith("/uploads/")) {
            candidates.add(backendRoot.resolve(normalized.substring(1)).normalize());
            candidates.add(workspaceRoot.resolve(normalized.substring(1)).normalize());
        }

        return candidates;
    }

    private ImageFeature extractFeature(BufferedImage source) {
        BufferedImage normalized = resize(source, 128, 128);
        long hashValue = computeAverageHash(normalized);
        double[] colorHistogram = computeColorHistogram(normalized);
        return new ImageFeature(hashValue, colorHistogram);
    }

    private long computeAverageHash(BufferedImage source) {
        BufferedImage scaled = resize(source, HASH_SIZE, HASH_SIZE);
        int[] values = new int[HASH_SIZE * HASH_SIZE];
        int index = 0;
        long sum = 0L;

        for (int y = 0; y < HASH_SIZE; y++) {
            for (int x = 0; x < HASH_SIZE; x++) {
                int rgb = scaled.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                int gray = (r * 30 + g * 59 + b * 11) / 100;
                values[index++] = gray;
                sum += gray;
            }
        }

        double average = sum / (double) values.length;
        long hash = 0L;
        for (int i = 0; i < values.length; i++) {
            if (values[i] >= average) {
                hash |= (1L << i);
            }
        }
        return hash;
    }

    private double[] computeColorHistogram(BufferedImage source) {
        int bins = HISTOGRAM_BINS_PER_CHANNEL * HISTOGRAM_BINS_PER_CHANNEL * HISTOGRAM_BINS_PER_CHANNEL;
        double[] histogram = new double[bins];

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                int rBin = Math.min(HISTOGRAM_BINS_PER_CHANNEL - 1, r * HISTOGRAM_BINS_PER_CHANNEL / 256);
                int gBin = Math.min(HISTOGRAM_BINS_PER_CHANNEL - 1, g * HISTOGRAM_BINS_PER_CHANNEL / 256);
                int bBin = Math.min(HISTOGRAM_BINS_PER_CHANNEL - 1, b * HISTOGRAM_BINS_PER_CHANNEL / 256);
                int index = (rBin * HISTOGRAM_BINS_PER_CHANNEL * HISTOGRAM_BINS_PER_CHANNEL)
                        + (gBin * HISTOGRAM_BINS_PER_CHANNEL)
                        + bBin;
                histogram[index] += 1D;
            }
        }

        double total = source.getWidth() * source.getHeight();
        if (total <= 0) {
            return histogram;
        }
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = histogram[i] / total;
        }
        return histogram;
    }

    private BufferedImage resize(BufferedImage source, int width, int height) {
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return target;
    }

    private BufferedImage ensureRgb(BufferedImage image) {
        if (image == null) {
            return null;
        }
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }
        BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = converted.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return converted;
    }

    private int hammingDistance(long left, long right) {
        return Long.bitCount(left ^ right);
    }

    private double cosineSimilarity(double[] left, double[] right) {
        if (left.length != right.length) {
            return 0D;
        }

        double dot = 0D;
        double leftNorm = 0D;
        double rightNorm = 0D;
        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }
        if (leftNorm <= 0D || rightNorm <= 0D) {
            return 0D;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private Map<String, Object> buildFeatureSummary(ImageFeature feature) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("hashHex", toUnsignedHex(feature.hashValue));
        summary.put("histogramPreview", Base64.getEncoder().encodeToString(histogramPreviewBytes(feature.colorHistogram)));
        return summary;
    }

    private byte[] histogramPreviewBytes(double[] histogram) {
        byte[] bytes = new byte[Math.min(12, histogram.length)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Math.max(0, Math.min(255, Math.round(histogram[i] * 255F)));
        }
        return bytes;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 8;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String pickDisplayName(User user) {
        if (user == null) {
            return "学员";
        }
        return firstNonBlank(user.getName(), user.getUsername(), "学员#" + user.getId());
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private double round(double value) {
        return Math.round(value * 10000D) / 10000D;
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0D;
    }

    private String toUnsignedHex(long value) {
        return String.format(Locale.ROOT, "%016x", value);
    }

    private static final class ImageFeature {
        private final long hashValue;
        private final double[] colorHistogram;

        private ImageFeature(long hashValue, double[] colorHistogram) {
            this.hashValue = hashValue;
            this.colorHistogram = colorHistogram;
        }
    }

    private static final class CandidateRecord {
        private final String sourceType;
        private final String cacheKey;
        private final String imageUrl;
        private final String title;
        private final String subtitle;
        private final String code;
        private final String summary;
        private final String updatedAt;
        private final String ownerName;
        private final String workshopName;
        private final Long sourceId;

        private CandidateRecord(
                String sourceType,
                String cacheKey,
                String imageUrl,
                String title,
                String subtitle,
                String code,
                String summary,
                String updatedAt,
                String ownerName,
                String workshopName,
                Long sourceId
        ) {
            this.sourceType = sourceType;
            this.cacheKey = cacheKey;
            this.imageUrl = imageUrl;
            this.title = title;
            this.subtitle = subtitle;
            this.code = code;
            this.summary = summary;
            this.updatedAt = updatedAt;
            this.ownerName = ownerName;
            this.workshopName = workshopName;
            this.sourceId = sourceId;
        }
    }
}
