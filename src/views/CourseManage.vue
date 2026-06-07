<template>
  <div class="page">
    <el-card class="panel-card" shadow="never">
      <div class="head">
        <div>
          <div class="title">课程管理（匠人 / 管理员）</div>
          <div class="desc">
            课程发布后会进入公开课程列表；这里额外补充带队老师、线下到访路线与工坊安排，更完整地体现“老师带学生到工坊研学”的真实场景。
          </div>
        </div>

        <div class="actions">
          <el-input v-model="keywordInput" placeholder="搜索课程标题" clearable style="width: 260px;" />
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px;">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
          <el-checkbox v-model="onlyNotEnded">只看未结束</el-checkbox>
          <el-button @click="load">刷新</el-button>
          <el-button type="primary" @click="openCreate">新增课程</el-button>
        </div>
      </div>

      <div class="summary-grid">
        <div v-for="item in summaryCards" :key="item.label" class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-desc">{{ item.desc }}</div>
        </div>
      </div>

      <div class="teacher-scene-panel">
        <div class="teacher-scene-title">到访流程概览</div>
        <div class="teacher-scene-desc">
          课程发布后，由老师在学校端统一组织到访；到场后由工坊完成签到接待、流程引导和体验安排；课程结束后，学生还可以通过展柜或说明牌上的溯源码查看作品档案与制作过程。
        </div>
      </div>

      <el-table :data="pageRows" v-loading="loading" style="width: 100%" empty-text="暂无课程">
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column label="封面" width="130">
          <template #default="{ row }">
            <el-image
              :src="getCourseCover(row)"
              fit="cover"
              style="width:72px;height:54px;border-radius:8px;border:1px solid #eef2f6;"
              :preview-src-list="[getCourseCover(row)]"
              preview-teleported
            >
              <template #error>
                <div class="table-cover-fallback">无图</div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="标题" min-width="320">
          <template #default="{ row }">
            <div class="course-title-cell">{{ row.title }}</div>
            <div class="sub-row">
              <el-tag size="small" type="info" effect="plain">{{ row.category || '未分类' }}</el-tag>
              <span>工坊ID：{{ row.workshopId ?? '-' }}</span>
              <span>价格：{{ row.price ?? 0 }}</span>
              <span>容量：{{ formatCapacity(row.capacity) }}</span>
            </div>
            <div class="teacher-line">
              <span class="teacher-label">带队老师：</span>
              <span>{{ teacherSummary(row) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="课程阶段" width="180">
          <template #default="{ row }">
            <div class="stage-cell">
              <el-tag :type="courseStage(row).type" effect="light" round>{{ courseStage(row).text }}</el-tag>
              <div class="stage-desc">{{ courseStage(row).desc }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="带队说明" min-width="220">
          <template #default="{ row }">
            <div class="lead-line">{{ teacherLeadRoute(row) }}</div>
          </template>
        </el-table-column>

        <el-table-column label="开始时间" width="180">
          <template #default="{ row }">{{ fmt(row.startTime) }}</template>
        </el-table-column>

        <el-table-column label="结束时间" width="180">
          <template #default="{ row }">{{ fmt(row.endTime) }}</template>
        </el-table-column>

        <el-table-column label="操作" width="400" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goCourseDetail(row)">课程详情</el-button>
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="info" link @click="goAttendance(row)">签到名单</el-button>
            <el-button type="info" link @click="goBooking(row)">报名名单</el-button>
            <el-button type="info" link @click="goReview(row)">评价反馈</el-button>

            <el-button
              v-if="canRelaunch(row)"
              type="success"
              link
              @click="openRelaunch(row)"
            >
              重新发布
            </el-button>
            <el-button
              v-else-if="row.status === 'DRAFT'"
              type="success"
              link
              @click="confirmPublish(row)"
            >
              发布
            </el-button>
            <el-button v-if="row.status !== 'CLOSED'" type="warning" link @click="confirmClose(row)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <div class="pager-left">共 {{ filteredRows.length }} 条</div>
        <el-pagination
          background
          layout="prev, pager, next, sizes"
          :total="filteredRows.length"
          :page-size="pageSize"
          :current-page="page"
          :page-sizes="[5, 10, 20, 50]"
          @current-change="page = $event"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增课程' : dialogMode === 'relaunch' ? '重新发布下一期' : '编辑课程'"
      width="820px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="课程标题（必填）" />
        </el-form-item>

        <el-form-item label="课程分类" prop="category">
          <el-select v-model="form.category" filterable allow-create default-first-option placeholder="请选择或输入分类" style="width: 100%;">
            <el-option label="木雕" value="木雕" />
            <el-option label="织锦" value="织锦" />
            <el-option label="陶艺" value="陶艺" />
            <el-option label="苏绣" value="苏绣" />
            <el-option label="银饰" value="银饰" />
            <el-option label="漆器" value="漆器" />
            <el-option label="紫砂" value="紫砂" />
          </el-select>
          <div class="hint">分类名称会影响首页研学热度与课程统计。</div>
        </el-form-item>

        <el-form-item label="课程简介" prop="intro">
          <el-input v-model="form.intro" type="textarea" :rows="4" placeholder="课程简介会在公开页直接展示。" />
          <div class="cover-btn-row">
            <el-button size="small" @click="fillSuggestedIntro">生成推荐课程文案</el-button>
            <el-button size="small" plain @click="appendSuggestedIntro">补充正式介绍段落</el-button>
          </div>
          <div class="hint">建议课程简介写得更完整一些，学生端会直接展示该内容。</div>
        </el-form-item>

        <el-row :gutter="14">
          <el-col :span="8">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :step="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="容量" prop="capacity">
              <el-input-number v-model="form.capacity" :min="0" :step="1" style="width: 100%;" />
              <div class="hint">容量为 0 表示不限人数。</div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="工坊ID" prop="workshopId">
              <el-input-number v-model="form.workshopId" :min="0" :step="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%;"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%;"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-block-title">带队老师信息</div>

        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="带队老师">
              <el-select v-model="form.teacherId" placeholder="请选择老师" clearable filterable style="width: 100%;">
                <el-option v-for="item in teacherOptions" :key="item.id" :label="teacherOptionLabel(item)" :value="String(item.id)" />
              </el-select>
              <div class="hint">如果老师列表为空，可以先到“老师管理”里补充带队老师档案。</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学校 / 学科">
              <el-input :model-value="teacherReadonlyText" readonly placeholder="选中老师后自动显示" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="带队路线">
          <el-input
            v-model="form.leadRoute"
            type="textarea"
            :rows="3"
            placeholder="例如：校门集合 -> 到访壮锦工坊 -> 工坊签到 -> 分组体验 -> 展柜扫码查看成品溯源"
          />
        </el-form-item>

        <el-form-item label="带队备注">
          <el-input
            v-model="form.teacherNote"
            type="textarea"
            :rows="2"
            placeholder="可补充班级对象、现场安全提醒、老师分组安排等。"
          />
        </el-form-item>

        <el-form-item label="封面URL" prop="coverUrl">
          <el-input v-model="form.coverUrl" placeholder="可填：https://... 或 /images/course_woodcarving.png" />
          <div class="cover-btn-row">
            <el-button size="small" @click="useDefaultCover">使用木雕默认封面</el-button>
            <el-button size="small" type="danger" plain @click="clearCover">清空封面</el-button>
          </div>
          <div class="cover-preview">
            <el-image
              :src="form.coverUrl || DEFAULT_COURSE_COVER"
              fit="cover"
              style="width: 240px; height: 130px; border-radius: 12px; margin-top: 10px;"
              :preview-src-list="[form.coverUrl || DEFAULT_COURSE_COVER]"
              preview-teleported
            >
              <template #error>
                <div class="cover-placeholder-big">封面无效</div>
              </template>
            </el-image>
          </div>
        </el-form-item>

        <div class="form-block-title">学生端展示预览</div>

        <div class="preview-panel">
          <div class="preview-header">
            <div class="preview-title">{{ form.title || '未命名课程' }}</div>
            <div class="preview-subtitle">保存后，学生端公开课程和课程详情页会按以下方式展示。</div>
          </div>

          <div class="preview-chip-row">
            <el-tag type="info" effect="plain" round>{{ form.category || '未分类' }}</el-tag>
            <el-tag type="success" effect="light" round>{{ previewOutcome }}</el-tag>
            <el-tag v-if="selectedTeacher?.name" type="warning" effect="plain" round>带队老师 {{ selectedTeacher.name }}</el-tag>
          </div>

          <div class="preview-section">
            <div class="preview-label">课堂将提供的材料</div>
            <div class="preview-tags">
              <el-tag
                v-for="item in previewMaterials"
                :key="item"
                type="success"
                effect="light"
                round
                size="small"
              >
                {{ item }}
              </el-tag>
            </div>
          </div>

          <div class="preview-section">
            <div class="preview-label">课程亮点</div>
            <div class="preview-bullets">
              <div v-for="item in previewHighlights" :key="item" class="preview-bullet">{{ item }}</div>
            </div>
          </div>

          <div class="preview-section">
            <div class="preview-label">课程重点</div>
            <div class="preview-bullets compact">
              <div v-for="item in previewTeachingFocus" :key="item" class="preview-bullet">{{ item }}</div>
            </div>
          </div>

          <div class="preview-section">
            <div class="preview-label">课程详情页长简介预览</div>
            <div class="preview-intro">{{ previewRichIntro }}</div>
          </div>
        </div>

        <el-alert
          v-if="dialogMode === 'edit'"
          type="info"
          show-icon
          title="这里编辑课程内容和带队信息；课程状态请继续使用列表里的“发布 / 关闭”操作。"
        />
        <el-alert
          v-if="dialogMode === 'relaunch'"
          type="success"
          show-icon
          title="这是同一门课程的下一期发布流程。修改开始/结束时间后保存，系统会自动重新发布，新的学员即可报名下一期。"
        />
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { courseApi } from '../api/course'
import { teacherApi } from '../api/teacher'
import {
  buildCourseRichIntro,
  buildRecommendedCourseIntro,
  getCourseHighlightList,
  getCourseMaterialSet,
  getCourseOutcome,
  getCourseTeachingFocus
} from '../utils/coursePresentation'

const router = useRouter()

const DEFAULT_COURSE_COVER = '/images/course_woodcarving.png'
const loading = ref(false)
const saving = ref(false)
const list = ref([])
const teacherOptions = ref([])

const keywordInput = ref('')
const keyword = ref('')
let debounceTimer = null
watch(keywordInput, () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    keyword.value = keywordInput.value
  }, 300)
})

const statusFilter = ref('')
const onlyNotEnded = ref(false)
const page = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)
const editingId = ref(null)

const form = ref({
  title: '',
  category: '',
  intro: '',
  price: 0,
  capacity: 0,
  startTime: null,
  endTime: null,
  coverUrl: '',
  workshopId: 0,
  teacherId: '',
  leadRoute: '',
  teacherNote: ''
})

const rules = {
  title: [{ required: true, message: '标题必填', trigger: 'blur' }],
  category: [{ required: true, message: '分类必填', trigger: 'change' }],
  price: [{ required: true, message: '价格必填', trigger: 'change' }],
  capacity: [{ required: true, message: '容量必填', trigger: 'change' }],
  coverUrl: [{
    validator: (_, v, cb) => {
      if (!v) return cb()
      const val = String(v).trim()
      const isHttp = /^https?:\/\/.+/i.test(val)
      const isPublicImages = /^\/images\/.+/i.test(val)
      const isLocalImg = /^\/.+\.(png|jpe?g|webp|gif|svg)$/i.test(val)
      if (isHttp || isPublicImages || isLocalImg) return cb()
      cb(new Error('封面 URL 可填 https://... 或 /images/xxx.png'))
    },
    trigger: 'blur'
  }]
}

const selectedTeacher = computed(() =>
  teacherOptions.value.find((item) => String(item.id) === String(form.value.teacherId || '')) || null
)

const teacherReadonlyText = computed(() => {
  if (!selectedTeacher.value) return ''
  return [selectedTeacher.value.schoolName, selectedTeacher.value.subjectName].filter(Boolean).join(' / ')
})

const previewCourse = computed(() => ({
  title: form.value.title,
  category: form.value.category,
  intro: form.value.intro,
  price: form.value.price,
  capacity: form.value.capacity,
  startTime: form.value.startTime,
  endTime: form.value.endTime,
  coverUrl: form.value.coverUrl,
  workshopId: form.value.workshopId,
  teacherName: selectedTeacher.value?.name || '',
  teacherSchoolName: selectedTeacher.value?.schoolName || '',
  teacherSubjectName: selectedTeacher.value?.subjectName || '',
  leadRoute: form.value.leadRoute
}))

const previewMaterials = computed(() => getCourseMaterialSet(previewCourse.value))
const previewHighlights = computed(() => getCourseHighlightList(previewCourse.value))
const previewTeachingFocus = computed(() => getCourseTeachingFocus(previewCourse.value))
const previewOutcome = computed(() => getCourseOutcome(previewCourse.value))
const previewRichIntro = computed(() => buildCourseRichIntro(previewCourse.value))

watch(selectedTeacher, (teacher) => {
  if (!teacher) return
  if (!form.value.leadRoute) form.value.leadRoute = teacher.leadRoute || ''
  if (!form.value.teacherNote) form.value.teacherNote = teacher.note || ''
})

function teacherOptionLabel(teacher) {
  return [teacher.name, teacher.schoolName].filter(Boolean).join(' / ')
}

function fillSuggestedIntro() {
  form.value.intro = buildRecommendedCourseIntro(previewCourse.value)
}

function appendSuggestedIntro() {
  const suggested = buildRecommendedCourseIntro(previewCourse.value)
  const current = String(form.value.intro || '').trim()
  if (!current) {
    form.value.intro = suggested
    return
  }
  if (current.includes(suggested)) return
  form.value.intro = `${current}\n\n${suggested}`
}

async function refreshTeacherOptions() {
  try {
    const res = await teacherApi.list()
    teacherOptions.value = Array.isArray(res) ? res : (res?.data || [])
  } catch {
    teacherOptions.value = []
  }
}

function goCourseDetail(row) {
  if (!row?.id) return
  router.push(`/app/course/${row.id}`)
}

function goAttendance(row) {
  if (!row?.id) return
  router.push(`/app/course/${row.id}/attendance`)
}

function goBooking(row) {
  if (!row?.id) return
  router.push(`/app/course/${row.id}/bookings`)
}

function goReview(row) {
  if (!row?.id) return
  router.push(`/app/course/${row.id}/reviews`)
}

function formatLocal(date) {
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function toBackendDateTime(value) {
  if (!value) return null
  if (value instanceof Date) {
    if (isNaN(value.getTime())) return null
    return formatLocal(value)
  }

  const text = String(value).trim()
  if (!text) return null
  if (/[zZ]$/.test(text) || /[+-]\d{2}:\d{2}$/.test(text)) {
    const date = new Date(text)
    if (!isNaN(date.getTime())) return formatLocal(date)
  }

  const normalized = text.replace('T', ' ').replace(/\.\d+/, '')
  if (/^\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}$/.test(normalized)) return `${normalized}:00`
  if (/^\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}$/.test(normalized)) return normalized

  const parsed = new Date(normalized)
  if (!isNaN(parsed.getTime())) return formatLocal(parsed)
  return null
}

function toTimestamp(value) {
  const dateText = toBackendDateTime(value)
  if (!dateText) return NaN
  return Date.parse(dateText.replace(' ', 'T'))
}

function fmt(value) {
  const text = toBackendDateTime(value)
  return text ? text.substring(0, 16) : '未设置'
}

function getCourseCover(row) {
  return row?.cover || row?.coverUrl || row?.imageUrl || DEFAULT_COURSE_COVER
}

function formatCapacity(value) {
  const count = Number(value ?? 0)
  return count > 0 ? `${count} 人` : '不限'
}

function statusText(status) {
  return {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    CLOSED: '已关闭'
  }[status] || (status || '未知')
}

function statusTagType(status) {
  return {
    DRAFT: 'info',
    PUBLISHED: 'success',
    CLOSED: 'warning'
  }[status] || 'info'
}

function courseStage(row) {
  if (row?.status === 'DRAFT') {
    return { text: '待发布', type: 'info', desc: '仅后台可见' }
  }
  if (row?.status === 'CLOSED') {
    return { text: '已关闭', type: 'warning', desc: '可改时间后重新发布下一期' }
  }

  const now = Date.now()
  const start = toTimestamp(row?.startTime)
  const end = toTimestamp(row?.endTime)

  if (!Number.isFinite(start) && !Number.isFinite(end)) {
    return { text: '待排期', type: 'info', desc: '尚未设置开课时间' }
  }
  if (Number.isFinite(start) && now < start) {
    return { text: '待开课', type: 'warning', desc: '可提前安排带队到访' }
  }
  if (Number.isFinite(end) && now > end) {
    return { text: '已结束', type: 'info', desc: '可改时间后重新发布下一期' }
  }
  return { text: '进行中', type: 'success', desc: '当前处于现场体验阶段' }
}

function canRelaunch(row) {
  return row?.status === 'CLOSED' || courseStage(row).text === '已结束'
}

function teacherMetaOf(row) {
  return {
    teacherId: row?.teacherId ? String(row.teacherId) : '',
    teacherName: row?.teacherName || '',
    schoolName: row?.teacherSchoolName || '',
    subjectName: row?.teacherSubjectName || '',
    leadRoute: row?.leadRoute || '',
    note: row?.teacherNote || ''
  }
}

function teacherSummary(row) {
  const meta = teacherMetaOf(row)
  if (!meta.teacherName && !meta.schoolName) return '未设置带队老师'
  return [meta.teacherName || '未命名老师', meta.schoolName || '未设置学校'].join(' / ')
}

function teacherLeadRoute(row) {
  const meta = teacherMetaOf(row)
  return meta.leadRoute || '未设置到访路线'
}

function buildTeacherPayload() {
  const teacher = selectedTeacher.value
  return {
    teacherId: form.value.teacherId ? Number(form.value.teacherId) : null,
    teacherName: teacher?.name || '',
    teacherSchoolName: teacher?.schoolName || '',
    teacherSubjectName: teacher?.subjectName || '',
    leadRoute: String(form.value.leadRoute || '').trim(),
    teacherNote: String(form.value.teacherNote || '').trim()
  }
}

async function load() {
  loading.value = true
  try {
    await refreshTeacherOptions()
    const res = await courseApi.listAll()
    list.value = Array.isArray(res) ? res : (res?.data || [])
  } finally {
    loading.value = false
  }
}

watch([keyword, onlyNotEnded, statusFilter], () => {
  page.value = 1
})

const filteredRows = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  const now = Date.now()

  return (list.value || []).filter((course) => {
    if (kw) {
      const meta = teacherMetaOf(course)
      const content = [course.title || '', course.category || '', meta.teacherName || '', meta.schoolName || ''].join(' ').toLowerCase()
      if (!content.includes(kw)) return false
    }

    if (statusFilter.value && course.status !== statusFilter.value) return false

    if (onlyNotEnded.value) {
      if (!course.endTime) return true
      const ts = toTimestamp(course.endTime)
      if (!Number.isFinite(ts)) return true
      return ts >= now
    }

    return true
  })
})

const summaryCards = computed(() => {
  const all = list.value || []
  const published = all.filter((item) => item.status === 'PUBLISHED').length
  const draft = all.filter((item) => item.status === 'DRAFT').length
  const active = all.filter((item) => courseStage(item).text === '进行中').length
  const ended = all.filter((item) => courseStage(item).text === '已结束').length
  const withTeacher = all.filter((item) => teacherMetaOf(item).teacherName).length

  return [
    { label: '课程总数', value: all.length, desc: '当前账号下维护的课程' },
    { label: '草稿课程', value: draft, desc: '尚未发布到公开端' },
    { label: '已发布', value: published, desc: '学生端可见的课程' },
    { label: '进行中', value: active, desc: '当前正在开展的课程' },
    { label: '已结束', value: ended, desc: '可整理评价和成果沉淀' },
    { label: '已配老师', value: withTeacher, desc: '已补充线下带队教师信息的课程数量' }
  ]
})

const pageRows = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredRows.value.slice(start, start + pageSize.value)
})

function onSizeChange(size) {
  pageSize.value = size
  page.value = 1
}

function resetForm() {
  editingId.value = null
  form.value = {
    title: '',
    category: '',
    intro: '',
    price: 0,
    capacity: 0,
    startTime: null,
    endTime: null,
    coverUrl: '',
    workshopId: 0,
    teacherId: '',
    leadRoute: '',
    teacherNote: ''
  }
}

async function openCreate() {
  await refreshTeacherOptions()
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

async function openEdit(row) {
  await refreshTeacherOptions()
  dialogMode.value = 'edit'
  editingId.value = row.id
  const teacherMeta = teacherMetaOf(row)

  form.value = {
    title: row.title ?? '',
    category: row.category ?? '',
    intro: row.intro ?? '',
    price: Number(row.price ?? 0),
    capacity: Number(row.capacity ?? 0),
    startTime: toBackendDateTime(row.startTime),
    endTime: toBackendDateTime(row.endTime),
    coverUrl: row.coverUrl ?? row.cover ?? row.imageUrl ?? '',
    workshopId: Number(row.workshopId ?? 0),
    teacherId: teacherMeta.teacherId || '',
    leadRoute: teacherMeta.leadRoute || '',
    teacherNote: teacherMeta.note || ''
  }

  dialogVisible.value = true
}

async function openRelaunch(row) {
  await openEdit(row)
  dialogMode.value = 'relaunch'
  if (courseStage(row).text === '已结束') {
    form.value.startTime = null
    form.value.endTime = null
  }
}

function useDefaultCover() {
  form.value.coverUrl = DEFAULT_COURSE_COVER
}

function clearCover() {
  form.value.coverUrl = ''
}

async function save() {
  const ok = await formRef.value?.validate?.().catch(() => false)
  if (!ok) return

  if (form.value.startTime && form.value.endTime) {
    const start = toTimestamp(form.value.startTime)
    const end = toTimestamp(form.value.endTime)
    if (Number.isFinite(start) && Number.isFinite(end) && end < start) {
      ElMessage.error('结束时间不能早于开始时间')
      return
    }
  }

  if (dialogMode.value === 'relaunch') {
    const start = toTimestamp(form.value.startTime)
    const end = toTimestamp(form.value.endTime)
    if (!Number.isFinite(start) || !Number.isFinite(end)) {
      ElMessage.warning('重新发布下一期前，请先填写新的开始和结束时间')
      return
    }
    if (end <= start) {
      ElMessage.warning('下一期课程的结束时间必须晚于开始时间')
      return
    }
  }

  saving.value = true
  try {
    const payload = {
      title: form.value.title,
      category: form.value.category,
      intro: form.value.intro,
      price: form.value.price,
      capacity: form.value.capacity,
      startTime: toBackendDateTime(form.value.startTime),
      endTime: toBackendDateTime(form.value.endTime),
      coverUrl: form.value.coverUrl,
      workshopId: form.value.workshopId,
      ...buildTeacherPayload()
    }

    if (dialogMode.value === 'create') {
      await courseApi.create(payload)
      ElMessage.success('创建成功')
    } else if (dialogMode.value === 'relaunch') {
      await courseApi.update(editingId.value, payload)
      await courseApi.publish(editingId.value)
      ElMessage.success('下一期课程已重新发布')
    } else {
      await courseApi.update(editingId.value, payload)
      ElMessage.success('更新成功')
    }

    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function confirmPublish(row) {
  try {
    await ElMessageBox.confirm(
      `确认发布课程“${row.title}”吗？发布后会出现在公开课程列表。`,
      '发布确认',
      { type: 'warning', confirmButtonText: '确认发布', cancelButtonText: '取消' }
    )
  } catch {
    return
  }

  await courseApi.publish(row.id)
  ElMessage.success('已发布')
  await load()
}

async function confirmClose(row) {
  try {
    await ElMessageBox.confirm(
      `确认关闭课程“${row.title}”吗？关闭后不再建议继续报名和支付。`,
      '关闭确认',
      { type: 'warning', confirmButtonText: '确认关闭', cancelButtonText: '取消' }
    )
  } catch {
    return
  }

  await courseApi.close(row.id)
  ElMessage.success('已关闭')
  await load()
}

load()
</script>

<style scoped>
.page { padding: 12px; }
.panel-card { border-radius: 18px; }
.head { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.title { font-size: 18px; font-weight: 800; }
.desc { font-size: 13px; color: #64748b; margin-top: 6px; line-height: 1.7; max-width: 780px; }
.actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.summary-card {
  border-radius: 16px;
  padding: 16px 18px;
  background: linear-gradient(180deg, #fbfdff 0%, #f6f9fc 100%);
  border: 1px solid #e5edf5;
}

.summary-label { font-size: 12px; color: #64748b; }
.summary-value { margin-top: 8px; font-size: 28px; font-weight: 800; color: #1f2937; }
.summary-desc { margin-top: 6px; font-size: 12px; color: #94a3b8; line-height: 1.6; }

.teacher-scene-panel {
  margin-bottom: 18px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fbff 100%);
  border: 1px solid #dbeafe;
}

.teacher-scene-title { font-size: 15px; font-weight: 800; color: #0f172a; }
.teacher-scene-desc { margin-top: 8px; color: #475569; font-size: 13px; line-height: 1.8; }
.course-title-cell { font-weight: 700; color: #1f2937; }
.sub-row { margin-top: 6px; font-size: 12px; color: #64748b; line-height: 1.6; display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
.teacher-line { margin-top: 8px; font-size: 12px; line-height: 1.7; color: #334155; }
.teacher-label { color: #2563eb; font-weight: 700; }
.stage-cell { display: flex; flex-direction: column; gap: 6px; }
.stage-desc { font-size: 12px; color: #94a3b8; line-height: 1.5; }
.lead-line { color: #475569; line-height: 1.7; font-size: 13px; }
.pager { margin-top: 14px; display: flex; align-items: center; justify-content: space-between; }
.pager-left { color: #666; font-size: 12px; }
.hint { font-size: 12px; color: #999; margin-top: 6px; }
.cover-btn-row { margin-top: 10px; display: flex; gap: 10px; flex-wrap: wrap; }
.cover-preview { display: flex; flex-direction: column; }
.cover-placeholder-big {
  width: 240px;
  height: 130px;
  border-radius: 12px;
  background: #f2f3f5;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10px;
  font-size: 12px;
}
.form-block-title {
  margin: 8px 0 14px;
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
}
.preview-panel {
  margin: 4px 0 16px;
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #fbfdff 0%, #f8fbff 100%);
  border: 1px solid #e5edf5;
}
.preview-header {
  margin-bottom: 12px;
}
.preview-title {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}
.preview-subtitle {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}
.preview-chip-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.preview-section + .preview-section {
  margin-top: 14px;
}
.preview-label {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
  margin-bottom: 8px;
}
.preview-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.preview-bullets {
  display: grid;
  gap: 8px;
}
.preview-bullets.compact {
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}
.preview-bullet {
  padding: 10px 12px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e7eef7;
  color: #334155;
  line-height: 1.7;
}
.preview-intro {
  white-space: pre-wrap;
  line-height: 1.85;
  color: #334155;
  background: #ffffff;
  padding: 14px;
  border-radius: 12px;
  border: 1px solid #e7eef7;
}
.table-cover-fallback {
  width: 72px;
  height: 54px;
  border-radius: 8px;
  border: 1px dashed #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
  background: #f8fafc;
}
</style>
