<template>
  <div class="page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-head">
        <div>
          <div class="hero-title">我的作品成果墙</div>
          <div class="hero-desc">
            完成课堂签到后，学生可以把自己的作品照片、学习收获和研学感受沉淀在这里，让课程成果形成可持续查看的个人记录。
          </div>
        </div>
        <div class="hero-actions">
          <el-button round @click="loadAll">刷新数据</el-button>
          <el-button type="primary" round @click="openCreateDialog" :disabled="!signedCourses.length">上传我的作品</el-button>
        </div>
      </div>

      <div class="summary-grid">
        <div v-for="item in summaryCards" :key="item.label" class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-desc">{{ item.desc }}</div>
        </div>
      </div>
    </el-card>

    <el-alert
      v-if="!signedCourses.length"
      type="info"
      show-icon
      title="当前还没有可上传作品的课程"
      description="需要先完成课程签到，才可以上传自己的课堂作品。完成签到后，这里会自动出现可选课程。"
      class="page-alert"
    />

    <el-row :gutter="16" class="section-row">
      <el-col :xs="24" :md="14">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-head">
              <div class="panel-title">已完成签到的课程</div>
              <el-tag size="small" type="success" effect="plain" round>只有完成签到后才能上传作品</el-tag>
            </div>
          </template>

          <div v-if="signedCourses.length" class="signed-course-grid">
            <button
              v-for="item in signedCourses"
              :key="item.id"
              type="button"
              class="signed-course-card"
              :class="{ active: Number(selectedCourseId) === Number(item.id) }"
              @click="pickCourse(item)"
            >
              <div class="signed-course-top">
                <div>
                  <div class="signed-course-title">{{ item.title }}</div>
                  <div class="signed-course-meta">{{ item.category || '公开课程' }} · {{ formatDateText(item.startTime) || '待安排时间' }}</div>
                </div>
                <el-tag size="small" effect="plain" round>{{ item.teacherName || '未设置带队老师' }}</el-tag>
              </div>

              <div class="signed-course-line">
                <span class="line-label">课堂提供：</span>
                <span>{{ item.providedValue }}</span>
              </div>
              <div class="signed-course-line take-home-line">
                <span class="line-label">课后可带走：</span>
                <span>{{ item.takeHome }}</span>
              </div>

              <div class="signed-materials">
                <el-tag
                  v-for="material in item.materials.slice(0, 6)"
                  :key="`${item.id}-${material}`"
                  size="small"
                  type="success"
                  effect="light"
                  round
                >
                  {{ material }}
                </el-tag>
              </div>
            </button>
          </div>
          <el-empty v-else description="当前没有已签到课程" :image-size="80" />
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">当前选中课程提示</div>
          </template>

          <div v-if="selectedCourse" class="current-course-box">
            <div class="current-course-title">{{ selectedCourse.title }}</div>
            <div class="current-course-kicker">{{ selectedCourse.category || '公开课程' }} · {{ selectedCourse.teacherName || '未设置带队老师' }}</div>
            <div class="current-course-tip">{{ selectedCourse.takeHome }}</div>
            <div class="current-course-desc">{{ briefText(selectedCourse.richIntro, 160) }}</div>
            <div class="current-course-footer">
              <el-button type="primary" plain round @click="openCreateDialog">去上传这门课的作品</el-button>
              <el-button round @click="goCourseDetail(selectedCourse.id)">查看课程详情</el-button>
            </div>
          </div>
          <el-empty v-else description="选择左侧课程后，这里会显示课程成果提示。" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-head">
          <div class="panel-title">我的作品展示墙</div>
          <div class="toolbar-inline">
            <el-select v-model="filterCourseId" placeholder="按课程筛选" clearable style="width: 220px;">
              <el-option label="全部作品" value="" />
              <el-option v-for="item in signedCourses" :key="item.id" :label="item.title" :value="String(item.id)" />
            </el-select>
          </div>
        </div>
      </template>

      <div v-if="filteredWorks.length" class="work-grid">
        <div v-for="item in filteredWorks" :key="item.id" class="work-card">
          <div class="work-image-wrap">
            <el-image
              :src="normalizeImageUrl(item.imageUrl)"
              fit="cover"
              class="work-image"
              :preview-src-list="[normalizeImageUrl(item.imageUrl)]"
              preview-teleported
            />
            <div class="work-badge">我的课堂成果</div>
          </div>

          <div class="work-body">
            <div class="work-title-row">
              <div class="work-title">{{ item.workTitle }}</div>
              <el-tag size="small" type="primary" effect="plain" round>{{ item.courseTitle || '课堂作品' }}</el-tag>
            </div>

            <div class="work-course">上传时间：{{ formatDateText(item.updatedAt || item.createdAt) }}</div>

            <div class="gain-panel">
              <div class="gain-label">我的收获</div>
              <div class="gain-text">{{ item.gainText || '已完成作品留存，形成课堂成果展示。' }}</div>
            </div>

            <div class="work-reflection">{{ item.reflection || '这件作品记录了我在课堂中的制作过程与体验感受。' }}</div>

            <div class="work-actions">
              <el-button size="small" type="primary" plain @click="openEditDialog(item)">编辑内容</el-button>
              <el-button size="small" type="danger" plain @click="removeWork(item)">删除作品</el-button>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂未上传作品，完成签到后可以把自己的课堂成果展示在这里。" :image-size="96" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑我的作品' : '上传我的作品'" width="680px" destroy-on-close>
      <el-form :model="form" label-width="96px">
        <el-form-item label="所属课程">
          <el-select v-model="form.courseId" placeholder="请选择已签到课程" style="width: 100%;" :disabled="!!editingId">
            <el-option v-for="item in signedCourses" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="作品标题">
          <el-input v-model="form.workTitle" maxlength="50" show-word-limit placeholder="例如：我的壮锦手工包课堂成果" />
        </el-form-item>

        <el-form-item label="作品照片">
          <el-upload
            class="upload-box"
            action="/api/upload"
            name="file"
            :show-file-list="false"
            :with-credentials="true"
            :before-upload="beforeImageUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
          >
            <div class="upload-inner" v-if="!form.imageUrl">
              <div class="upload-title">点击上传作品图片</div>
              <div class="upload-desc">支持 JPG/PNG/WEBP，建议上传课堂现场或成品展示图</div>
            </div>
            <el-image v-else :src="normalizeImageUrl(form.imageUrl)" fit="cover" class="preview-image" />
          </el-upload>
        </el-form-item>

        <el-form-item label="作品收获">
          <el-input
            v-model="form.gainText"
            type="textarea"
            :rows="3"
            maxlength="120"
            show-word-limit
            placeholder="例如：我学会了基础针法、材料识别和成品整理。"
          />
        </el-form-item>

        <el-form-item label="体验感受">
          <el-input
            v-model="form.reflection"
            type="textarea"
            :rows="4"
            maxlength="240"
            show-word-limit
            placeholder="写下你的课程感受、制作过程中的收获或者带回作品后的体验。"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button round @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" round :loading="saving" @click="submitForm">保存作品</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import { attendanceApi } from '../api/attendance'
import { showcaseApi } from '../api/showcase'
import { buildCourseRichIntro, getCourseMaterialSet, getCourseProvidedValue, getCourseTakeHome } from '../utils/coursePresentation'

const route = useRoute()
const router = useRouter()
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const workList = ref([])
const signedCourses = ref([])
const filterCourseId = ref('')
const selectedCourseId = ref('')
const form = ref(createEmptyForm())

function createEmptyForm() {
  return {
    courseId: '',
    workTitle: '',
    imageUrl: '',
    reflection: '',
    gainText: ''
  }
}

function formatDateText(value) {
  return value ? String(value).replace('T', ' ').substring(0, 16) : '未记录时间'
}

function normalizeImageUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  if (url.startsWith('/api/')) return url
  if (url.startsWith('/uploads/')) return `/api${url}`
  if (url.startsWith('uploads/')) return `/api/${url}`
  return url
}

function briefText(text, maxLength = 120) {
  const content = String(text || '').replace(/\s+/g, ' ').trim()
  return content.length > maxLength ? `${content.slice(0, maxLength)}...` : content
}

const selectedCourse = computed(() => {
  return signedCourses.value.find((item) => String(item.id) === String(selectedCourseId.value)) || null
})

const filteredWorks = computed(() => {
  if (!filterCourseId.value) return workList.value
  return workList.value.filter((item) => String(item.courseId) === String(filterCourseId.value))
})

const summaryCards = computed(() => {
  const latestWork = workList.value[0]
  const completionRate = signedCourses.value.length
    ? `${Math.round((workList.value.length / signedCourses.value.length) * 100)}%`
    : '0%'

  return [
    {
      label: '已上传作品',
      value: workList.value.length,
      desc: '当前账号已保存的课堂作品数量'
    },
    {
      label: '可上传课程',
      value: signedCourses.value.length,
      desc: '已经完成签到、可以上传作品的课程数量'
    },
    {
      label: '成果留存率',
      value: completionRate,
      desc: '已签到课程中，已有作品留存的占比'
    },
    {
      label: '最近上传',
      value: latestWork ? formatDateText(latestWork.updatedAt || latestWork.createdAt) : '暂无',
      desc: '便于老师查看学生作品是否持续更新'
    }
  ]
})

async function loadSignedCourses() {
  const [coursePack, attendanceRes] = await Promise.all([
    request.get('/my/courses'),
    attendanceApi.my()
  ])

  const courseList = Array.isArray(coursePack?.courses) ? coursePack.courses : []
  const attendanceList = Array.isArray(attendanceRes) ? attendanceRes : (attendanceRes?.data || [])
  const signedIds = new Set(
    attendanceList
      .filter((item) => String(item.status || '').toUpperCase() === 'SIGNED' || item.signTime)
      .map((item) => Number(item.courseId))
  )

  signedCourses.value = courseList
    .filter((item) => signedIds.has(Number(item.id)))
    .map((item) => ({
      ...item,
      takeHome: getCourseTakeHome(item),
      providedValue: getCourseProvidedValue(item),
      materials: getCourseMaterialSet(item),
      richIntro: buildCourseRichIntro(item)
    }))

  if (!selectedCourseId.value && signedCourses.value[0]) {
    selectedCourseId.value = String(signedCourses.value[0].id)
  }
}

async function loadWorks() {
  const res = await showcaseApi.my()
  const list = Array.isArray(res) ? res : (res?.data || [])
  workList.value = [...list].sort((a, b) => String(b.updatedAt || b.createdAt).localeCompare(String(a.updatedAt || a.createdAt)))
}

async function loadAll() {
  await Promise.all([loadSignedCourses(), loadWorks()])
  const queryCourseId = String(route.query.courseId || '')
  if (queryCourseId && signedCourses.value.some((item) => String(item.id) === queryCourseId)) {
    selectedCourseId.value = queryCourseId
    if (!editingId.value) {
      form.value.courseId = Number(queryCourseId)
    }
  }
}

function pickCourse(course) {
  selectedCourseId.value = String(course.id)
  filterCourseId.value = String(course.id)
  form.value.courseId = course.id
  if (!form.value.workTitle) {
    form.value.workTitle = `${course.title}课堂成果`
  }
}

function openCreateDialog() {
  editingId.value = null
  form.value = createEmptyForm()
  if (selectedCourse.value) {
    form.value.courseId = Number(selectedCourse.value.id)
    form.value.workTitle = `${selectedCourse.value.title}课堂成果`
  } else if (signedCourses.value[0]) {
    form.value.courseId = Number(signedCourses.value[0].id)
    form.value.workTitle = `${signedCourses.value[0].title}课堂成果`
  }
  dialogVisible.value = true
}

function openEditDialog(item) {
  editingId.value = item.id
  selectedCourseId.value = String(item.courseId)
  form.value = {
    courseId: item.courseId,
    workTitle: item.workTitle || '',
    imageUrl: item.imageUrl || '',
    reflection: item.reflection || '',
    gainText: item.gainText || ''
  }
  dialogVisible.value = true
}

function beforeImageUpload(file) {
  const ok = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
  if (!ok) ElMessage.error('仅支持 JPG、PNG 或 WEBP 图片')
  return ok
}

function handleUploadSuccess(res) {
  const imageUrl = res?.data || ''
  if (!imageUrl) {
    ElMessage.error('图片上传失败，请重试')
    return
  }
  form.value.imageUrl = imageUrl
  ElMessage.success('作品图片上传成功')
}

function handleUploadError() {
  ElMessage.error('连接上传接口失败，请确认后端服务已启动')
}

async function submitForm() {
  if (!form.value.courseId) return ElMessage.warning('请先选择课程')
  if (!form.value.workTitle.trim()) return ElMessage.warning('请填写作品标题')
  if (!form.value.imageUrl) return ElMessage.warning('请先上传作品图片')

  saving.value = true
  try {
    const payload = {
      courseId: Number(form.value.courseId),
      workTitle: form.value.workTitle.trim(),
      imageUrl: form.value.imageUrl,
      reflection: form.value.reflection.trim(),
      gainText: form.value.gainText.trim()
    }

    if (editingId.value) {
      await showcaseApi.update(editingId.value, payload)
    } else {
      await showcaseApi.save(payload)
    }

    ElMessage.success(editingId.value ? '作品已更新' : '作品已上传')
    dialogVisible.value = false
    editingId.value = null
    form.value = createEmptyForm()
    await loadWorks()
  } finally {
    saving.value = false
  }
}

async function removeWork(item) {
  await ElMessageBox.confirm(`确定删除作品《${item.workTitle}》吗？`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await showcaseApi.remove(item.id)
  ElMessage.success('作品已删除')
  await loadWorks()
}

function goCourseDetail(id) {
  if (!id) return
  router.push(`/app/course/${id}`)
}

onMounted(loadAll)
</script>

<style scoped>
.page {
  padding: 16px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #eef4fb 100%);
}
.hero-card,
.panel-card {
  border-radius: 20px;
  border: 1px solid #e6edf5;
}
.hero-head,
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}
.hero-title {
  font-size: 24px;
  font-weight: 900;
  color: #0f172a;
}
.hero-desc {
  margin-top: 10px;
  color: #64748b;
  line-height: 1.9;
  max-width: 820px;
}
.hero-actions,
.toolbar-inline,
.work-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.page-alert {
  margin-top: 16px;
  border-radius: 16px;
}
.summary-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}
.summary-card {
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #fbfdff 0%, #f5f8fc 100%);
  border: 1px solid #e5edf5;
}
.summary-label {
  font-size: 12px;
  color: #64748b;
}
.summary-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 900;
  color: #0f172a;
}
.summary-desc {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.7;
}
.section-row {
  margin-top: 16px;
}
.panel-title {
  font-weight: 800;
  color: #1e293b;
}
.signed-course-grid,
.work-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 14px;
}
.signed-course-card,
.work-card {
  width: 100%;
  text-align: left;
  border: 1px solid #e5edf5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}
.signed-course-card {
  padding: 16px;
  cursor: pointer;
}
.signed-course-card.active,
.signed-course-card:hover,
.work-card:hover {
  transform: translateY(-2px);
  border-color: #60a5fa;
  box-shadow: 0 14px 28px rgba(37, 99, 235, 0.08);
}
.signed-course-top,
.work-title-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}
.signed-course-title,
.current-course-title,
.work-title {
  font-weight: 900;
  color: #0f172a;
  line-height: 1.6;
}
.signed-course-meta,
.current-course-kicker,
.work-course,
.work-time {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.7;
}
.signed-course-line {
  margin-top: 12px;
  color: #334155;
  line-height: 1.8;
  font-size: 13px;
}
.line-label {
  color: #2563eb;
  font-weight: 700;
}
.take-home-line {
  color: #047857;
}
.signed-materials {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.current-course-box {
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #f8fbff 0%, #eef6ff 100%);
  border: 1px solid #dbeafe;
}
.current-course-tip {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #ecfdf5;
  color: #047857;
  line-height: 1.8;
}
.current-course-desc {
  margin-top: 14px;
  color: #475569;
  line-height: 1.8;
  font-size: 13px;
}
.current-course-footer {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.work-card {
  overflow: hidden;
}
.work-image-wrap {
  position: relative;
}
.work-image {
  width: 100%;
  height: 220px;
  display: block;
}
.work-badge {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.72);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}
.work-body {
  padding: 16px;
}
.gain-panel {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #eff6ff;
  border: 1px solid #dbeafe;
}
.gain-label {
  font-size: 12px;
  font-weight: 700;
  color: #2563eb;
}
.gain-text {
  margin-top: 6px;
  color: #334155;
  line-height: 1.8;
  font-size: 13px;
}
.work-reflection {
  margin-top: 12px;
  color: #475569;
  line-height: 1.8;
  font-size: 13px;
}
.upload-box :deep(.el-upload) {
  width: 100%;
}
.upload-inner {
  width: 100%;
  min-height: 200px;
  display: grid;
  place-items: center;
  text-align: center;
  border: 1px dashed #cbd5e1;
  border-radius: 16px;
  background: #f8fafc;
  padding: 20px;
}
.upload-title {
  font-weight: 800;
  color: #1e293b;
}
.upload-desc {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}
.preview-image {
  width: 100%;
  height: 240px;
  border-radius: 16px;
  display: block;
}
@media (max-width: 768px) {
  .signed-course-grid,
  .work-grid {
    grid-template-columns: 1fr;
  }
  .signed-course-top,
  .work-title-row,
  .hero-head,
  .panel-head {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
