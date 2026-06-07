<template>
  <div class="page">
    <el-card class="panel-card" v-loading="loading" shadow="never">
      <div class="head">
        <div>
          <div class="title">课程签到名单</div>
          <div class="desc">从到场结果反向查看课程执行情况，便于掌握课程完成度和现场参与表现。</div>
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

      <el-table :data="rows" style="width:100%" empty-text="暂无签到记录">
        <el-table-column type="index" width="60" label="#" />
        <el-table-column prop="id" label="记录ID" width="120" />

        <el-table-column label="学员档案" min-width="220">
          <template #default="{ row }">
            <div class="student-name">{{ displayStudentName(row) }}</div>
            <div class="sub">学员ID：{{ row.userId ?? '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="签到状态" width="140">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SIGNED' ? 'success' : 'info'" effect="light" round>
              {{ row.status === 'SIGNED' ? '已签到' : (row.status || '未知') }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="签到结果" width="140">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SIGNED' ? 'primary' : 'warning'" effect="plain" round>
              {{ row.status === 'SIGNED' ? '到场完成' : '待核验' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="signTime" label="签到时间" min-width="200">
          <template #default="{ row }">
            {{ fmt(row.signTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { attendanceApi } from '../api/attendance'
import { ElMessage } from 'element-plus'
import { getUser } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const rows = ref([])
const summary = ref(null)

const user = computed(() => getUser())

const isTeacher = computed(() => {
  const r = (user.value?.role || '').toLowerCase()
  return r === 'handicraft' || r === 'admin'
})

const courseId = computed(() => Number(route.params.id))

const rateText = computed(() => {
  const r = summary.value?.attendanceRate
  if (typeof r !== 'number' || Number.isNaN(r)) return '0%'
  return `${(r * 100).toFixed(0)}%`
})

const summaryCards = computed(() => [
  { label: '课程ID', value: courseId.value || '-', desc: '当前查看的课程标识' },
  { label: '已报名', value: summary.value?.bookedCount ?? 0, desc: '进入课程报名链路的人数' },
  { label: '已支付', value: summary.value?.paidCount ?? 0, desc: '完成支付的学员数' },
  { label: '已签到', value: summary.value?.signedCount ?? 0, desc: '现场完成签到的学员数' },
  { label: '签到率', value: rateText.value, desc: '基于签到汇总计算的到场转化' }
])

function displayStudentName(row) {
  return row.studentName || row.username || row.name || `用户#${row.userId ?? '-'}`
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
    记录ID: r.id ?? '',
    课程ID: r.courseId ?? courseId.value,
    学员ID: r.userId ?? '',
    学员: displayStudentName(r),
    签到状态: r.status === 'SIGNED' ? '已签到' : (r.status || ''),
    签到时间: fmt(r.signTime)
  }))

  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '签到名单')
  XLSX.writeFile(wb, `course_${courseId.value}_签到名单.xlsx`)
  ElMessage.success('签到名单已导出')
}

async function load() {
  if (!Number.isFinite(courseId.value) || courseId.value <= 0) {
    rows.value = []
    summary.value = null
    ElMessage.error('课程 ID 不正确，无法加载签到名单。')
    return
  }

  loading.value = true
  try {
    const [s, list] = await Promise.all([
      attendanceApi.summaryByCourse(courseId.value),
      attendanceApi.listByCourse(courseId.value)
    ])

    summary.value = s || null
    rows.value = Array.isArray(list) ? list : (list?.data || [])
  } catch (e) {
    summary.value = null
    rows.value = []
    ElMessage.error('加载签到名单失败，可能是接口未开放或当前账号无权限。')
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
</style>
