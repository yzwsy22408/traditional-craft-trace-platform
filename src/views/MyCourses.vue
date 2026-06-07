<template>
  <div class="my-courses-container">
    <el-card class="main-card-wrapper" shadow="never">
      <div class="page-header">
        <div class="header-left">
          <div class="title-with-badge">
            <h2 class="title">我的研学之旅</h2>
            <el-badge :value="rows.length" :hidden="rows.length === 0" type="primary" class="count-badge" />
          </div>
          <p class="subtitle">围绕报名、支付、签到和结业四个环节，持续记录你的课程进度。</p>
        </div>
        <div class="header-right">
          <el-button type="primary" :icon="Refresh" @click="load" circle plain class="refresh-btn"></el-button>
        </div>
      </div>

      <el-alert
        v-if="!isStudent"
        type="warning"
        show-icon
        title="角色提示"
        description="当前页面以学生学习流程为主，非学生账号可查看课程进度与业务结果。"
        class="role-alert"
      />

      <div class="summary-grid">
        <div v-for="item in summaryCards" :key="item.label" class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-desc">{{ item.desc }}</div>
        </div>
      </div>

      <template v-if="rows.length">
        <el-table
          :data="rows"
          v-loading="loading"
          style="width: 100%"
          class="modern-table"
          row-class-name="modern-row"
        >
          <el-table-column type="index" width="70" label="序号" align="center" />

          <el-table-column label="课程档案" min-width="300">
            <template #default="{ row }">
              <div class="course-profile-cell">
                <div class="course-name-box">
                  <span class="course-main-title">{{ row.course.title }}</span>
                  <span class="course-id-tag">#{{ row.course.id }}</span>
                </div>
                <div class="course-sub-info">
                  <el-tag size="small" type="info" effect="plain" class="mini-tag">{{ row.course.category || '研学课程' }}</el-tag>
                  <el-tag size="small" :type="phaseTagType(row.coursePhase)" effect="light" class="mini-tag">{{ phaseText(row.coursePhase) }}</el-tag>
                </div>
                <div class="journey-tip">{{ nextStepText(row) }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="时间安排" width="220">
            <template #default="{ row }">
              <div class="time-schedule-cell">
                <div class="time-row start">
                  <el-icon class="icon"><Calendar /></el-icon>
                  <span>{{ fmt(row.course.startTime) }}</span>
                </div>
                <div class="time-row end">
                  <el-icon class="icon"><Stopwatch /></el-icon>
                  <span class="time-text-bold">{{ fmt(row.course.endTime) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="进度状态" width="140" align="center">
            <template #default="{ row }">
              <el-tag :type="overallTagType(row.overallStatus)" effect="dark" round class="status-pill-modern">
                {{ overallText(row.overallStatus) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="签到实录" width="220">
            <template #default="{ row }">
              <div v-if="row.signed" class="checkin-status-card fade-in">
                <div class="checkin-main">
                  <el-icon class="check-icon"><CircleCheckFilled /></el-icon>
                  <span class="check-label">研学签到成功</span>
                </div>
                <div class="checkin-time-info-bold">
                  <el-icon><Timer /></el-icon>
                  <span>{{ fmt(row.signTime) }}</span>
                </div>
              </div>
              <div v-else class="not-checked-in">
                <el-icon><Warning /></el-icon>
                <span>{{ row.coursePhase === 'ENDED' ? '课程已结束' : '待完成签到' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="快捷指引" width="220" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons-group">
                <el-button type="primary" size="small" @click="goDetail(row.course.id)" class="action-item">查看</el-button>

                <el-button
                  v-if="row.overallStatus === 'DONE'"
                  type="info"
                  size="small"
                  plain
                  @click="handleShowCert(row)"
                  class="action-item cert-btn"
                >结业证书</el-button>

                <el-button
                  v-if="isStudent && row.orderStatus === 'UNPAID'"
                  type="warning"
                  size="small"
                  @click="pay(row.latestOrderId)"
                  class="action-item pulse"
                >立即支付</el-button>

                <el-button
                  v-if="isStudent && row.orderStatus === 'PAID' && !row.signed"
                  type="success"
                  size="small"
                  @click="goDetail(row.course.id)"
                  class="action-item highlight"
                >去签到</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <div v-else class="empty-state">
        <el-empty description="你还没有课程记录，先去公开课程里挑选感兴趣的研学内容吧。" :image-size="96" />
        <el-button type="primary" round @click="goExploreCourses">去看公开课程</el-button>
      </div>
    </el-card>

    <el-dialog v-model="certVisible" title="研学结业证书" width="650px" center destroy-on-close>
      <div class="cert-outer-wrapper">
        <div id="certificate-content" class="certificate-inner">
          <div class="cert-border-double">
            <div class="cert-header">
              <div class="cert-brand">传统手工艺研学与溯源平台</div>
              <h1 class="cert-main-title">研学结业证书</h1>
              <p class="cert-en-title">课程结业证明</p>
            </div>

            <div class="cert-body">
              <p class="cert-to">兹证明 <span class="student-name">{{ user?.name || user?.username }}</span> 学员：</p>
              <p class="cert-main-text">
                在该传统手工艺研学平台中，圆满完成了课程
                <span class="highlight-course">《{{ currentCourse?.course.title }}》</span>
                的研学内容与实践任务。经审核，您在工序溯源与艺术探索中表现出色，准予结业，特发此证。
              </p>
            </div>

            <div class="cert-footer">
              <div class="cert-serial">
                <p>证书编号：CT-{{ currentCourse?.course.id }}-{{ user?.id }}</p>
                <p>签发日期：{{ new Date().toLocaleDateString() }}</p>
              </div>
              <div class="seal-box">
                <div class="official-seal">
                  研学中心<br/>认证专用
                </div>
                <p class="committee">课程项目组</p>
              </div>
            </div>
          </div>
          <div class="watermark">匠心研学</div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="certVisible = false" round>返 回</el-button>
          <el-button type="primary" :loading="exporting" @click="downloadCert" round>下载高清证书 (PNG)</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, Stopwatch, CircleCheckFilled, Refresh, Timer, Warning } from '@element-plus/icons-vue'
import request from '../api/request'
import { orderApi } from '../api/order'
import { attendanceApi } from '../api/attendance'
import { ElMessage } from 'element-plus'
import { getUser } from '../utils/auth'

const router = useRouter()
const loading = ref(false)
const rows = ref([])

const certVisible = ref(false)
const exporting = ref(false)
const currentCourse = ref(null)

const user = computed(() => getUser())
const isStudent = computed(() => user.value?.role === 'student')

const fmt = (v) => {
  if (!v) return '未设置'
  return String(v).replace('T', ' ').substring(0, 16)
}

const toTs = (v) => {
  if (!v) return NaN
  const s = String(v).trim().replace(' ', 'T')
  return Date.parse(s)
}

const resolveCoursePhase = (course) => {
  const start = toTs(course?.startTime)
  const end = toTs(course?.endTime)
  const now = Date.now()
  if (!Number.isFinite(start) && !Number.isFinite(end)) return 'UNSCHEDULED'
  if (Number.isFinite(start) && now < start) return 'UPCOMING'
  if (Number.isFinite(end) && now > end) return 'ENDED'
  return 'ONGOING'
}

const phaseText = (phase) => ({
  UNSCHEDULED: '待排期',
  UPCOMING: '即将开始',
  ONGOING: '进行中',
  ENDED: '已结束'
}[phase] || '进行中')

const phaseTagType = (phase) => ({
  UNSCHEDULED: 'info',
  UPCOMING: 'warning',
  ONGOING: 'success',
  ENDED: 'info'
}[phase] || 'info')

const goDetail = (id) => router.push(`/app/course/${id}`)
const goExploreCourses = () => router.push('/app/courses')

const handleShowCert = (row) => {
  currentCourse.value = row
  certVisible.value = true
}

const downloadCert = async () => {
  exporting.value = true
  const element = document.getElementById('certificate-content')
  try {
    const { default: html2canvas } = await import('html2canvas')
    const canvas = await html2canvas(element, {
      backgroundColor: '#ffffff',
      scale: 2,
      useCORS: true
    })
    const link = document.createElement('a')
    link.download = `研学证书-${currentCourse.value.course.title}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
    ElMessage.success('证书已生成并下载')
  } catch (e) {
    ElMessage.error('证书生成失败，请稍后重试')
  } finally {
    exporting.value = false
  }
}

const calcOverallStatus = ({ bookingStatus, orderStatus, signed }) => {
  if (signed) return 'DONE'
  if (orderStatus === 'PAID') return 'PAID_WAIT_SIGN'
  if (orderStatus === 'UNPAID') return 'BOOKED_WAIT_PAY'
  if (orderStatus === 'REFUNDED') return 'REFUNDED'
  if (bookingStatus === 'BOOKED') return 'BOOKED_WAIT_PAY'
  if (bookingStatus === 'CANCELED') return 'CANCELED'
  return 'NOT_BOOKED'
}

const overallText = (s) => {
  const map = {
    DONE: '已研学',
    PAID_WAIT_SIGN: '待签到',
    BOOKED_WAIT_PAY: '待支付',
    REFUNDED: '已退款',
    CANCELED: '已取消'
  }
  return map[s] || '未报名'
}

const overallTagType = (s) => {
  const map = {
    DONE: 'success',
    PAID_WAIT_SIGN: 'warning',
    BOOKED_WAIT_PAY: 'primary',
    REFUNDED: 'danger',
    CANCELED: 'info'
  }
  return map[s] || 'info'
}

const nextStepText = (row) => {
  if (row.signed) return '已完成签到，可查看详情页评价课程并下载结业证书。'
  if (row.orderStatus === 'PAID') return '订单已支付，下一步前往详情页完成签到。'
  if (row.orderStatus === 'UNPAID') return '当前仍有待支付订单，建议优先完成支付。'
  if (row.bookingStatus === 'BOOKED') return '你已完成报名，可以继续进入课程详情创建订单。'
  if (row.coursePhase === 'ENDED') return '课程已结束，当前保留历史记录供回顾查看。'
  return '当前记录已建立，可进入详情页继续完成课程流程。'
}

const buildRows = ({ courses = [], bookings = [], orders = [], attendances = [] }) => {
  const courseMap = new Map(courses.map(c => [c.id, c]))
  const bookingByCourse = new Map()
  bookings.forEach(b => {
    const old = bookingByCourse.get(b.courseId)
    if (!old || Number(b.id) > Number(old.id)) bookingByCourse.set(b.courseId, b)
  })
  const orderByCourse = new Map()
  orders.forEach(o => {
    const old = orderByCourse.get(o.courseId)
    if (!old || Number(o.id) > Number(old.id)) orderByCourse.set(o.courseId, o)
  })
  const attendanceByCourse = new Map()
  attendances.forEach(a => {
    const old = attendanceByCourse.get(a.courseId)
    if (!old || String(a.signTime || '') > String(old.signTime || '')) {
      attendanceByCourse.set(a.courseId, a)
    }
  })

  const courseIds = new Set([
    ...bookings.map(b => b.courseId),
    ...orders.map(o => o.courseId),
    ...attendances.map(a => a.courseId)
  ])

  return Array.from(courseIds).map(cid => {
    const course = courseMap.get(cid) || { id: cid, title: '(课程信息失效)' }
    const booking = bookingByCourse.get(cid)
    const order = orderByCourse.get(cid)
    const att = attendanceByCourse.get(cid)
    const bookingStatus = booking?.status || null
    const orderStatus = order?.status || null
    const signed = !!att
    const signTime = att?.signTime || null
    return {
      course,
      bookingStatus,
      orderStatus,
      latestOrderId: order?.id || null,
      signed,
      signTime,
      coursePhase: resolveCoursePhase(course),
      overallStatus: calcOverallStatus({ bookingStatus, orderStatus, signed })
    }
  }).sort((a, b) => {
    const phaseRank = { ONGOING: 0, UPCOMING: 1, ENDED: 2, UNSCHEDULED: 3 }
    if (phaseRank[a.coursePhase] !== phaseRank[b.coursePhase]) return phaseRank[a.coursePhase] - phaseRank[b.coursePhase]
    const ta = toTs(a.course.startTime)
    const tb = toTs(b.course.startTime)
    return (Number.isFinite(ta) ? ta : Number.MAX_SAFE_INTEGER) - (Number.isFinite(tb) ? tb : Number.MAX_SAFE_INTEGER)
  })
}

const summaryCards = computed(() => {
  const list = rows.value
  return [
    { label: '课程记录', value: list.length, desc: '当前账号已参与的课程数' },
    { label: '待支付', value: list.filter(item => item.orderStatus === 'UNPAID').length, desc: '还有订单等待完成支付' },
    { label: '待签到', value: list.filter(item => item.orderStatus === 'PAID' && !item.signed).length, desc: '课程到场后记得完成签到' },
    { label: '已结业', value: list.filter(item => item.overallStatus === 'DONE').length, desc: '可下载结业证书并沉淀成果' },
    { label: '退款/取消', value: list.filter(item => item.overallStatus === 'REFUNDED' || item.overallStatus === 'CANCELED').length, desc: '已退出的课程记录' }
  ]
})

const load = async () => {
  loading.value = true
  try {
    const data = await request.get('/my/courses')
    let myAtt = []
    try {
      const a = await attendanceApi.my()
      myAtt = Array.isArray(a) ? a : (a?.data || [])
    } catch (e) {
      myAtt = []
    }
    rows.value = buildRows({ ...(data || {}), attendances: myAtt })
  } finally {
    loading.value = false
  }
}

const pay = async (id) => {
  await orderApi.pay(id)
  ElMessage.success('支付成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.my-courses-container { padding: 30px; background-color: #f6f8fa; min-height: 100vh; }
.main-card-wrapper { border-radius: 20px; border: none; box-shadow: 0 10px 40px -10px rgba(0,0,0,0.08) !important; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; padding-bottom: 25px; margin-bottom: 20px; border-bottom: 1px solid #edf2f7; }
.title-with-badge { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.title { font-size: 26px; font-weight: 800; color: #1a202c; margin: 0; }
.subtitle { color: #718096; font-size: 15px; margin: 0; }
.refresh-btn { transition: all 0.3s; }
.refresh-btn:hover { transform: rotate(180deg); color: #3b82f6; }
.role-alert { border-radius: 12px; margin-bottom: 20px; }
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
.summary-label { font-size: 12px; color: #64748b; }
.summary-value { margin-top: 8px; font-size: 28px; font-weight: 800; color: #1f2937; }
.summary-desc { margin-top: 6px; font-size: 12px; color: #94a3b8; line-height: 1.6; }
.modern-table { border-radius: 16px; overflow: hidden; }
:deep(.modern-row) { min-height: 96px; }
.course-profile-cell { display: flex; flex-direction: column; gap: 8px; }
.course-name-box { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.course-main-title { font-weight: 800; color: #2d3748; font-size: 17px; }
.course-id-tag { font-size: 11px; color: #a0aec0; background: #f7fafc; padding: 2px 6px; border-radius: 4px; }
.course-sub-info { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.journey-tip { font-size: 13px; color: #64748b; line-height: 1.6; }
.time-schedule-cell { display: flex; flex-direction: column; gap: 6px; font-size: 13px; }
.time-row { display: flex; align-items: center; gap: 8px; color: #4a5568; }
.time-row .icon { color: #3b82f6; }
.status-pill-modern { font-weight: 800; padding: 0 15px; height: 32px; font-size: 12px; }
.checkin-status-card { background: #f0fdf4; padding: 10px; border-radius: 12px; border: 1px solid #dcfce7; display: flex; flex-direction: column; gap: 4px; }
.checkin-main { display: flex; align-items: center; gap: 8px; }
.check-icon { color: #16a34a; }
.check-label { font-size: 13px; font-weight: 700; color: #166534; }
.checkin-time-info-bold { color: #166534; font-weight: 600; font-size: 12px; display: flex; align-items: center; gap: 6px; }
.not-checked-in { color: #64748b; display: flex; align-items: center; gap: 8px; }
.action-buttons-group { display: flex; gap: 6px; justify-content: center; flex-wrap: wrap; }
.action-item { border-radius: 8px; font-weight: 700; }
.cert-btn { border: 1px dashed #909399; }
.empty-state {
  padding: 32px 0 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.cert-outer-wrapper { background: #f5f5f5; padding: 20px; display: flex; justify-content: center; }
.certificate-inner {
  width: 580px; height: 410px; background: #fff; padding: 25px;
  position: relative; font-family: "STKaiti", "楷体", serif;
  box-shadow: 0 0 20px rgba(0,0,0,0.1);
}
.cert-border-double { border: 4px double #b58d59; height: 100%; padding: 20px; display: flex; flex-direction: column; }
.cert-header { text-align: center; margin-bottom: 25px; }
.cert-brand { font-size: 10px; color: #b58d59; letter-spacing: 2px; }
.cert-main-title { font-size: 34px; color: #b58d59; margin: 10px 0 5px; letter-spacing: 12px; }
.cert-en-title { font-size: 9px; color: #94a3b8; letter-spacing: 2px; }
.cert-body { margin-top: 20px; line-height: 2; }
.cert-to { font-size: 18px; margin-bottom: 10px; }
.student-name { font-weight: bold; border-bottom: 1px solid #333; padding: 0 10px; }
.cert-main-text { text-indent: 2em; text-align: justify; font-size: 16px; color: #333; }
.highlight-course { font-weight: bold; color: #b58d59; }
.cert-footer { margin-top: auto; display: flex; justify-content: space-between; align-items: flex-end; }
.cert-serial { font-size: 10px; color: #64748b; font-family: sans-serif; }
.seal-box { text-align: center; position: relative; }
.official-seal {
  position: absolute; top: -60px; right: 0; width: 100px; height: 100px;
  border: 3px solid rgba(220, 38, 38, 0.5); border-radius: 50%;
  color: rgba(220, 38, 38, 0.5); font-size: 14px; display: flex;
  align-items: center; justify-content: center; transform: rotate(-15deg);
  font-weight: bold; line-height: 1.2;
}
.committee { font-weight: bold; font-size: 15px; margin-top: 5px; }
.watermark {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(-45deg);
  font-size: 80px;
  color: rgba(0, 0, 0, 0.03);
  z-index: 0;
  pointer-events: none;
  white-space: nowrap;
  letter-spacing: 20px;
}
.pulse { animation: pulse-border 2s infinite; }
@keyframes pulse-border {
  0% { box-shadow: 0 0 0 0 rgba(236, 151, 31, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(236, 151, 31, 0); }
  100% { box-shadow: 0 0 0 0 rgba(236, 151, 31, 0); }
}
.fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateX(-10px); } to { opacity: 1; transform: translateX(0); } }
</style>
