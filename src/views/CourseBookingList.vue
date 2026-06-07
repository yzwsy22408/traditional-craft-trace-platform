<template>
  <div class="page">
    <el-card class="panel-card" v-loading="loading" shadow="never">
      <div class="head">
        <div>
          <div class="title">课程报名名单</div>
          <div class="desc">查看课程报名、支付与签到转化情况，便于匠人端把握课程参与进度。</div>
        </div>

        <div class="actions">
          <el-button @click="load">刷新</el-button>
          <el-button type="success" :disabled="!rows.length" @click="exportExcel">导出 Excel</el-button>
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

      <el-table
        :data="rows"
        empty-text="暂无报名记录"
        style="width:100%;"
      >
        <el-table-column type="index" width="60" label="#" />
        <el-table-column prop="id" label="报名ID" width="120" />

        <el-table-column label="学员档案" min-width="220">
          <template #default="{ row }">
            <div class="student-name">{{ displayStudentName(row) }}</div>
            <div class="sub">学员ID：{{ row.studentUserId ?? '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="报名状态" width="140">
          <template #default="{ row }">
            <el-tag :type="bookingStatusTag(row.status)" effect="light" round>
              {{ bookingStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="签到转化" width="140">
          <template #default="{ row }">
            <el-tag :type="attendanceStatusTag(row.status)" effect="plain" round>
              {{ attendanceStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="bookingTime" label="报名时间" min-width="180">
          <template #default="{ row }">
            {{ fmt(row.bookingTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { bookingApi } from '../api/booking'
import { attendanceApi } from '../api/attendance'
import { getUser } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const courseId = computed(() => Number(route.params.id))

const loading = ref(false)
const rows = ref([])

const booked = ref(0)
const paid = ref(0)
const attended = ref(0)

const user = computed(() => getUser())

const isTeacher = computed(() => {
  const r = (user.value?.role || '').toLowerCase()
  return r === 'handicraft' || r === 'admin'
})

const attendanceRate = computed(() => {
  if (!booked.value) return '0%'
  return `${Math.round((attended.value / booked.value) * 100)}%`
})

const summaryCards = computed(() => [
  { label: '课程ID', value: courseId.value || '-', desc: '当前查看的课程标识' },
  { label: '已报名', value: booked.value, desc: '进入课程报名链路的学员数' },
  { label: '已支付', value: paid.value, desc: '已完成支付的学员数' },
  { label: '已签到', value: attended.value, desc: '最终到场完成签到的学员数' },
  { label: '到场率', value: attendanceRate.value, desc: '基于已报名人数计算的签到转化' }
])

function fmt(v) {
  if (!v) return '未记录'
  return String(v).replace('T', ' ').slice(0, 19)
}

function displayStudentName(row) {
  return row.studentName || row.username || row.name || (row.studentUserId ? `用户#${row.studentUserId}` : '未命名学员')
}

function bookingStatusText(s) {
  if (s === 'BOOKED') return '已报名'
  if (s === 'ATTENDED') return '已完成'
  if (s === 'CANCELED') return '已取消'
  return s || '未知'
}

function bookingStatusTag(s) {
  if (s === 'BOOKED') return 'success'
  if (s === 'ATTENDED') return 'primary'
  if (s === 'CANCELED') return 'info'
  return 'info'
}

function attendanceStatusText(s) {
  if (s === 'ATTENDED') return '已签到'
  if (s === 'BOOKED') return '待签到'
  if (s === 'CANCELED') return '不参与'
  return '未记录'
}

function attendanceStatusTag(s) {
  if (s === 'ATTENDED') return 'primary'
  if (s === 'BOOKED') return 'warning'
  return 'info'
}

async function load() {
  if (!Number.isFinite(courseId.value) || courseId.value <= 0) {
    rows.value = []
    booked.value = 0
    paid.value = 0
    attended.value = 0
    ElMessage.error('课程 ID 不正确，无法加载报名名单。')
    return
  }

  loading.value = true
  try {
    const [list, summary] = await Promise.all([
      bookingApi.listByCourse(courseId.value),
      attendanceApi.summaryByCourse(courseId.value)
    ])

    rows.value = Array.isArray(list) ? list : (list?.data || [])
    booked.value = summary?.bookedCount || 0
    paid.value = summary?.paidCount || 0
    attended.value = summary?.signedCount || 0
  } catch (e) {
    rows.value = []
    booked.value = 0
    paid.value = 0
    attended.value = 0
  } finally {
    loading.value = false
  }
}

async function exportExcel() {
  if (!rows.value.length) return ElMessage.info('暂无数据可导出')

  const xlsxModule = await import('xlsx')
  const XLSX = xlsxModule.default ?? xlsxModule

  const data = rows.value.map((r, idx) => ({
    序号: idx + 1,
    报名ID: r.id ?? '',
    课程ID: r.courseId ?? courseId.value,
    学员ID: r.studentUserId ?? '',
    学员: displayStudentName(r),
    报名状态: bookingStatusText(r.status),
    签到状态: attendanceStatusText(r.status),
    报名时间: fmt(r.bookingTime)
  }))

  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '报名名单')
  XLSX.writeFile(wb, `course_${courseId.value}_报名名单.xlsx`)
  ElMessage.success('报名名单已导出')
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
