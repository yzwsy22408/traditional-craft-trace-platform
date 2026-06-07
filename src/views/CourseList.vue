<template>
  <div class="page">
    <el-card class="panel-card" shadow="never">
      <div class="head">
        <div class="hero-copy">
          <div class="eyebrow">STUDY COURSES</div>
          <div class="title">公开课程列表</div>
          <div class="desc">学生可在这里查看已发布课程的时间安排、课堂材料、可带走成果和课程简介，提前了解线下研学会接触到哪些工艺内容。</div>
          <div class="hero-tags">
            <span class="hero-tag">老师带队</span>
            <span class="hero-tag">匠人示范</span>
            <span class="hero-tag">材料透明</span>
            <span class="hero-tag">成果可留存</span>
          </div>
        </div>

        <div class="actions">
          <el-input
            v-model="keyword"
            placeholder="搜索课程标题、分类或材料"
            clearable
            style="width: 260px;"
          />
          <el-checkbox v-model="onlyOngoing">只看未结束课程</el-checkbox>
          <el-button @click="load" type="primary">刷新</el-button>
        </div>
      </div>

      <el-alert
        v-if="errorMsg"
        :title="errorMsg"
        type="error"
        show-icon
        class="page-alert"
      />

      <div class="summary-grid">
        <div v-for="item in summaryCards" :key="item.label" class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-desc">{{ item.desc }}</div>
        </div>
      </div>

      <el-table
        :data="filteredList"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无公开课程"
      >
        <el-table-column label="课程档案" min-width="360">
          <template #default="{ row }">
            <div class="course-cell">
              <el-image
                :src="getCourseCover(row)"
                fit="cover"
                class="course-cover"
                :preview-src-list="[getCourseCover(row)]"
                preview-teleported
              >
                <template #error>
                  <div class="course-cover placeholder">无图</div>
                </template>
              </el-image>
              <div class="course-meta">
                <div class="course-title-row">
                  <span class="course-title">{{ row.title || '未命名课程' }}</span>
                  <el-tag size="small" effect="plain" type="info">{{ row.category || '未分类' }}</el-tag>
                </div>
                <div class="course-brief">{{ briefIntro(row) }}</div>
                <div class="material-tags">
                  <el-tag
                    v-for="item in courseMaterials(row).slice(0, 5)"
                    :key="`${row.id}-${item}`"
                    size="small"
                    effect="light"
                    type="success"
                    round
                  >
                    {{ item }}
                  </el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="课堂提供 / 可带走成果" min-width="280">
          <template #default="{ row }">
            <div class="value-stack">
              <span class="minor-text">课程统一提供：</span>
              <span class="material-brief">{{ courseMaterials(row).slice(0, 6).join('、') }}</span>
              <span class="minor-text" style="margin-top: 8px;">课后可带走：</span>
              <span class="take-home-brief">{{ takeHomeText(row) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="价格 / 容量" width="150">
          <template #default="{ row }">
            <div class="value-stack">
              <span class="money-text">￥{{ fmtMoney(row.price) }}</span>
              <span class="minor-text">{{ formatCapacity(row.capacity) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="当前阶段" width="160">
          <template #default="{ row }">
            <div class="value-stack">
              <el-tag :type="coursePhase(row).type" effect="light" round>{{ coursePhase(row).text }}</el-tag>
              <span class="minor-text">{{ coursePhase(row).desc }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="课程时间" width="220">
          <template #default="{ row }">
            <div class="value-stack schedule-stack">
              <span>开始：{{ fmtTime(row.startTime) }}</span>
              <span>结束：{{ fmtTime(row.endTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { publicCourseApi } from '../api/course'
import { buildCourseRichIntro, getCourseMaterialSet, getCourseTakeHome } from '../utils/coursePresentation'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const errorMsg = ref('')

const keyword = ref('')
const onlyOngoing = ref(false)
const DEFAULT_COURSE_COVER = '/images/course_woodcarving.png'

function fmtTime(v) {
  if (!v) return '未设置'
  return String(v).replace('T', ' ').substring(0, 16)
}

function fmtMoney(v) {
  if (v === null || v === undefined || v === '') return '0.00'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return n.toFixed(2)
}

function toDate(v) {
  if (!v) return null
  const s = String(v).replace('T', ' ').replace(/-/g, '/')
  const d = new Date(s)
  return Number.isNaN(d.getTime()) ? null : d
}

function getCourseCover(row) {
  return row?.cover || row?.coverUrl || row?.imageUrl || DEFAULT_COURSE_COVER
}

function formatCapacity(v) {
  const n = Number(v ?? 0)
  return n > 0 ? `容量 ${n} 人` : '容量不限'
}

function courseMaterials(row) {
  return getCourseMaterialSet(row)
}

function takeHomeText(row) {
  return getCourseTakeHome(row)
}

function briefIntro(row) {
  const content = buildCourseRichIntro(row).replace(/\s+/g, ' ')
  return content.length > 88 ? `${content.slice(0, 88)}...` : content
}

function coursePhase(row) {
  const now = Date.now()
  const start = toDate(row?.startTime)?.getTime()
  const end = toDate(row?.endTime)?.getTime()

  if (!Number.isFinite(start) && !Number.isFinite(end)) {
    return { text: '待排期', type: 'info', desc: '时间可与工坊进一步确认' }
  }
  if (Number.isFinite(start) && now < start) {
    return { text: '即将开始', type: 'warning', desc: '适合提前报名准备' }
  }
  if (Number.isFinite(end) && now > end) {
    return { text: '已结束', type: 'info', desc: '可查看课程成果回顾' }
  }
  return { text: '进行中', type: 'success', desc: '当前可参与线下体验' }
}

function goDetail(id) {
  router.push(`/app/course/${id}`)
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await publicCourseApi.list()
    list.value = Array.isArray(res) ? res : (res?.data || [])
    list.value.sort((a, b) => {
      const da = toDate(a.startTime)?.getTime() ?? 0
      const db = toDate(b.startTime)?.getTime() ?? 0
      return da - db
    })
  } catch (e) {
    errorMsg.value = '获取公开课程失败，请确认后端服务已启动。'
    list.value = []
  } finally {
    loading.value = false
  }
}

const filteredList = computed(() => {
  let arr = [...list.value]

  if (keyword.value.trim()) {
    const k = keyword.value.trim().toLowerCase()
    arr = arr.filter((x) => {
      const content = [
        x.title || '',
        x.category || '',
        x.intro || '',
        getCourseTakeHome(x),
        ...courseMaterials(x)
      ].join(' ').toLowerCase()
      return content.includes(k)
    })
  }

  if (onlyOngoing.value) {
    const now = Date.now()
    arr = arr.filter((x) => {
      const end = toDate(x.endTime)?.getTime()
      return !end || end >= now
    })
  }

  return arr
})

const summaryCards = computed(() => {
  const all = filteredList.value
  const ongoing = all.filter((item) => coursePhase(item).text === '进行中').length
  const upcoming = all.filter((item) => coursePhase(item).text === '即将开始').length
  const freeCount = all.filter((item) => Number(item.price ?? 0) <= 0).length
  const categories = new Set(all.map((item) => item.category).filter(Boolean)).size
  const materials = new Set(all.flatMap((item) => courseMaterials(item))).size

  return [
    { label: '公开课程', value: all.length, desc: '当前列表中的全部课程' },
    { label: '进行中', value: ongoing, desc: '正在开放体验的课程' },
    { label: '即将开始', value: upcoming, desc: '适合提前预约关注' },
    { label: '免费体验', value: freeCount, desc: '可直接参与的课程数量' },
    { label: '工艺分类', value: categories, desc: '覆盖的传统手工艺方向' },
    { label: '课堂材料', value: materials, desc: '课程介绍中涉及的材料种类' }
  ]
})

onMounted(load)
</script>

<style scoped>
.page {
  padding: 16px;
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(215, 166, 99, 0.16), transparent 22%),
    linear-gradient(180deg, #f8f4ee 0%, #f7fbff 16%, #eef5fb 100%);
}
.panel-card {
  border-radius: 26px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  background:
    linear-gradient(180deg, rgba(250, 246, 240, 0.9) 0%, rgba(255, 255, 255, 0.98) 18%, #ffffff 100%);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
}
.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 20px;
  padding: 4px 0 8px;
}
.hero-copy {
  max-width: 760px;
}
.eyebrow {
  display: inline-flex;
  align-items: center;
  padding: 6px 11px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.08);
  color: #2563eb;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 1.4px;
}
.title {
  margin-top: 12px;
  font-size: 28px;
  font-weight: 900;
  color: #172554;
  letter-spacing: 0.3px;
}
.desc {
  font-size: 14px;
  color: #64748b;
  margin-top: 10px;
  line-height: 1.85;
  max-width: 760px;
}
.hero-tags {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.hero-tag {
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(191, 219, 254, 0.92);
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
}
.actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.page-alert { margin-bottom: 16px; }
.summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 14px; margin-bottom: 22px; }
.summary-card {
  border-radius: 18px;
  padding: 17px 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, rgba(245, 249, 255, 0.96) 100%);
  border: 1px solid #e4edf8;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}
.summary-label { font-size: 12px; color: #64748b; }
.summary-value { margin-top: 8px; font-size: 28px; font-weight: 800; color: #1f2937; }
.summary-desc { margin-top: 6px; font-size: 12px; color: #94a3b8; }
.course-cell { display: flex; gap: 12px; align-items: center; }
.course-cover {
  width: 96px;
  height: 72px;
  border-radius: 14px;
  border: 1px solid #e5edf5;
  flex-shrink: 0;
  box-shadow: 0 10px 18px rgba(15, 23, 42, 0.08);
}
.course-cover.placeholder { display: flex; align-items: center; justify-content: center; color: #94a3b8; background: #f8fafc; }
.course-meta { min-width: 0; }
.course-title-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.course-title { font-weight: 800; color: #1e293b; font-size: 15px; }
.course-brief { margin-top: 8px; color: #64748b; font-size: 13px; line-height: 1.7; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.material-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 8px; }
.value-stack { display: flex; flex-direction: column; gap: 6px; }
.schedule-stack { font-size: 13px; color: #475569; }
.money-text { font-weight: 800; color: #dc2626; }
.minor-text { font-size: 12px; color: #64748b; line-height: 1.5; }
.material-brief { color: #334155; line-height: 1.7; }
.take-home-brief { color: #047857; line-height: 1.7; }
:deep(.el-table) {
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid #e7eef7;
  background: rgba(255, 255, 255, 0.9);
}
:deep(.el-table th.el-table__cell) {
  background: linear-gradient(180deg, #f7fafc 0%, #eef4fb 100%);
  color: #334155;
  font-weight: 800;
}
:deep(.el-table tr) {
  background: rgba(255, 255, 255, 0.92);
}
:deep(.el-table .el-table__row:hover > td.el-table__cell) {
  background: #f8fbff;
}
:deep(.el-input__wrapper),
:deep(.el-checkbox) {
  background: rgba(255, 255, 255, 0.9);
}
@media (max-width: 900px) {
  .head {
    flex-direction: column;
  }
}
@media (max-width: 768px) {
  .page {
    padding: 12px;
  }
  .title {
    font-size: 24px;
  }
  .actions {
    width: 100%;
  }
}
</style>
