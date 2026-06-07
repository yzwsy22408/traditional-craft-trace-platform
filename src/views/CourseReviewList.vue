<template>
  <div class="page">
    <el-card class="panel-card" v-loading="loading" shadow="never">
      <div class="head">
        <div>
          <div class="title">课程评价列表</div>
          <div class="desc">集中查看课程反馈与评分结果，方便展示研学体验的最终沉淀效果。</div>
        </div>
        <div class="actions">
          <el-button @click="load">刷新</el-button>
          <el-button type="success" @click="exportExcel" :disabled="!rows.length">导出 Excel</el-button>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </div>

      <el-alert
        v-if="!isTeacher"
        title="当前账号不是匠人或管理员角色，若后端开启权限校验，这里可能无法查看完整数据。"
        type="warning"
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

      <el-table :data="rows" style="width:100%" empty-text="暂无评价">
        <el-table-column type="index" width="60" label="#" />
        <el-table-column prop="id" label="评价ID" width="100" />

        <el-table-column label="学员档案" width="220">
          <template #default="{ row }">
            <div class="student-name">{{ displayStudentName(row) }}</div>
            <div class="sub">学员ID：{{ row.studentUserId ?? '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="评分" width="180">
          <template #default="{ row }">
            <div class="rating-cell">
              <el-rate :model-value="row.rating || 0" disabled />
              <span class="rating-text">{{ row.rating ?? 0 }} / 5</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评价内容" min-width="320">
          <template #default="{ row }">
            <div class="review-content">
              {{ row.content || '该学员未填写文字评价。' }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="评价时间" width="200">
          <template #default="{ row }">
            {{ fmt(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="foot-note">
        说明：课程评价通常在学员完成课程并签到后产生，适合用于展示课程体验反馈与成果认可度。
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { reviewApi } from '../api/review'
import { ElMessage } from 'element-plus'
import { getUser } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const rows = ref([])

const user = computed(() => getUser())

const isTeacher = computed(() => {
  const r = (user.value?.role || '').toLowerCase()
  return r === 'handicraft' || r === 'admin'
})

const courseId = computed(() => Number(route.params.id))

const total = computed(() => rows.value.length)

const avg = computed(() => {
  const list = rows.value.filter(r => typeof r.rating === 'number')
  if (list.length === 0) return 0
  const sum = list.reduce((a, b) => a + (b.rating || 0), 0)
  return Math.round((sum / list.length) * 10) / 10
})

const highScoreCount = computed(() => rows.value.filter(r => Number(r.rating ?? 0) >= 4).length)

const avgText = computed(() => {
  if (!total.value) return '暂无'
  return `${avg.value} / 5`
})

const summaryCards = computed(() => [
  { label: '课程ID', value: courseId.value || '-', desc: '当前查看的课程标识' },
  { label: '评价总数', value: total.value, desc: '当前课程收集到的评价条数' },
  { label: '平均评分', value: avgText.value, desc: '学员对课程整体体验的平均反馈' },
  { label: '高分反馈', value: highScoreCount.value, desc: '评分不低于 4 分的评价数' }
])

function displayStudentName(row) {
  return row.studentName || row.username || row.name || `用户#${row.studentUserId ?? '-'}`
}

function fmt(v) {
  if (!v) return '未记录'
  const s = String(v).replace('T', ' ')
  return s.length >= 16 ? s.slice(0, 16) : s
}

async function exportExcel() {
  if (!rows.value.length) return ElMessage.info('暂无数据可导出')

  const xlsxModule = await import('xlsx')
  const XLSX = xlsxModule.default ?? xlsxModule

  const data = rows.value.map((r, idx) => ({
    序号: idx + 1,
    评价ID: r.id ?? '',
    课程ID: r.courseId ?? courseId.value,
    学员ID: r.studentUserId ?? '',
    学员: displayStudentName(r),
    评分: r.rating ?? '',
    评价内容: r.content ?? '',
    评价时间: fmt(r.createdAt)
  }))

  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '评价列表')
  XLSX.writeFile(wb, `course_${courseId.value}_评价列表.xlsx`)
  ElMessage.success('评价列表已导出')
}

async function load() {
  if (!Number.isFinite(courseId.value) || courseId.value <= 0) {
    rows.value = []
    ElMessage.error('课程 ID 不正确，无法加载评价列表。')
    return
  }

  loading.value = true
  try {
    const list = await reviewApi.listByCourse(courseId.value)
    rows.value = Array.isArray(list) ? list : (list?.data || [])
  } catch (e) {
    rows.value = []
    ElMessage.error('加载评价列表失败，可能是接口未开放或当前账号无权限。')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  padding: 16px;
}

.panel-card {
  border-radius: 18px;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.title {
  font-size: 18px;
  font-weight: 800;
}

.desc {
  margin-top: 6px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.page-alert {
  margin-bottom: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.summary-card {
  border-radius: 16px;
  padding: 16px 18px;
  background: linear-gradient(180deg, #fbfdff 0%, #f4f7fb 100%);
  border: 1px solid #e5edf5;
}

.summary-label {
  font-size: 12px;
  color: #64748b;
}

.summary-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 800;
  color: #1f2937;
}

.summary-desc {
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.6;
}

.student-name {
  font-weight: 700;
  color: #1f2937;
}

.sub {
  font-size: 12px;
  color: #64748b;
  margin-top: 6px;
}

.rating-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.rating-text {
  font-size: 12px;
  color: #64748b;
}

.review-content {
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
}

.foot-note {
  margin-top: 16px;
  color: #94a3b8;
  font-size: 12px;
}
</style>
