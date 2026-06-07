<template>
  <div class="detail-container">
    <el-card class="main-card" v-loading="loading" shadow="never">
      <div class="header-section">
        <div class="title-group">
          <span class="page-title">课程详情</span>
          <el-tag v-if="course?.status" :type="statusTagType(course.status)" effect="dark" round class="status-tag-glow">
            {{ statusText(course.status) }}
          </el-tag>
        </div>
        <el-button @click="router.back()" plain round>返回列表</el-button>
      </div>

      <div class="overview-panel">
        <div class="overview-cover-box">
          <el-image :src="courseCover" fit="cover" class="overview-cover">
            <template #error>
              <div class="overview-cover placeholder">课程封面</div>
            </template>
          </el-image>
        </div>
        <div class="overview-main">
          <div class="overview-title-row">
            <div class="course-main-title">{{ course?.title || '-' }}</div>
            <el-tag size="small" effect="plain" type="info">{{ course?.category || '未分类' }}</el-tag>
          </div>
          <div class="overview-subtitle">{{ actionGuidance }}</div>
          <div class="overview-tags">
            <el-tag :type="coursePhase.type" effect="light" round>{{ coursePhase.text }}</el-tag>
            <el-tag effect="plain" round>{{ capacityText }}</el-tag>
            <el-tag effect="plain" round>价格 ￥{{ course?.price ?? 0 }}</el-tag>
            <el-tag v-if="course?.teacherName" effect="plain" type="warning" round>带队老师 {{ course.teacherName }}</el-tag>
          </div>
        </div>
      </div>

      <el-row :gutter="16" class="summary-row">
        <el-col v-for="item in progressCards" :key="item.label" :xs="24" :sm="12" :md="6">
          <div class="summary-card">
            <div class="summary-label">{{ item.label }}</div>
            <div class="summary-value">{{ item.value }}</div>
            <div class="summary-desc">{{ item.desc }}</div>
          </div>
        </el-col>
      </el-row>

      <div class="material-panel">
        <div class="section-header">课堂将提供的材料与工具</div>
        <div class="material-tip">{{ providedValue }}</div>
        <div class="material-tags">
          <el-tag
            v-for="item in courseMaterials"
            :key="item"
            type="success"
            effect="light"
            round
            class="material-tag"
          >
            {{ item }}
          </el-tag>
        </div>
      </div>

      <div class="feature-grid">
        <div class="feature-card focus-card">
          <div class="section-header small">课程重点</div>
          <div class="focus-list">
            <div v-for="item in teachingFocus" :key="item" class="focus-item">
              <span class="focus-index"></span>
              <span>{{ item }}</span>
            </div>
          </div>
        </div>

        <div class="feature-card outcome-card">
          <div class="section-header small">完成成果</div>
          <div class="outcome-text">{{ courseOutcome }}</div>
          <div class="take-home-tip">课后可带走：{{ takeHomeText }}</div>
          <div class="highlight-list">
            <div v-for="item in courseHighlights" :key="item" class="highlight-item">
              {{ item }}
            </div>
          </div>
        </div>
      </div>

      <div class="course-info-panel">
        <div class="section-header small">课程信息</div>
        <div class="course-info-title">{{ course?.title || '-' }}</div>
      </div>

      <el-descriptions :column="2" border class="info-descriptions">
        <el-descriptions-item label="课程价格"><span class="price-text">￥{{ course?.price ?? '-' }}</span></el-descriptions-item>
        <el-descriptions-item label="课程容量">{{ capacityText }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ fmtTime(course?.startTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ fmtTime(course?.endTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="带队老师">{{ course?.teacherName || '待补充' }}</el-descriptions-item>
        <el-descriptions-item label="教师单位">{{ course?.teacherSchoolName || '待补充' }}</el-descriptions-item>
        <el-descriptions-item label="带队路线" :span="2">
          {{ course?.leadRoute || '课程现场将按集合说明、工坊参观、材料认知、匠人示范与成果展示的顺序进行。' }}
        </el-descriptions-item>
        <el-descriptions-item label="课堂提供" :span="2">
          {{ providedValue }}
        </el-descriptions-item>
        <el-descriptions-item label="课后可带走" :span="2">
          {{ takeHomeText }}
        </el-descriptions-item>
        <el-descriptions-item label="课程简介" :span="2">
          <div class="intro-box">{{ richIntro }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <el-alert v-if="isEnded" type="info" show-icon title="该课程已结束：已禁止继续报名、下单、支付和签到。" class="status-alert" />
      <el-alert v-else type="success" show-icon :title="actionGuidance" class="status-alert soft" />

      <template v-if="isStudent">
        <div class="action-card-layer">
          <div class="section-header">我的操作面板</div>
          <div class="button-group">
            <el-button v-if="!isEnded && !booked" type="success" @click="book" :disabled="!isLogin" round>报名课程</el-button>
            <el-button v-if="!isEnded && booked && latestOrderStatus !== 'PAID' && !signed" type="danger" @click="cancelBooking" :disabled="!isLogin" round>取消报名</el-button>
            <el-button v-if="!isEnded && booked && canCreateOrder" type="warning" @click="createOrder" :disabled="!isLogin" round>立即下单</el-button>
            <el-button v-if="!isEnded && latestOrderId && latestOrderStatus === 'UNPAID'" type="primary" @click="payLatest" :disabled="!isLogin" round>支付订单</el-button>
            <el-button v-if="!isEnded && latestOrderId && latestOrderStatus === 'UNPAID'" @click="cancelLatestOrder" :disabled="!isLogin" round>取消订单</el-button>
            <el-button v-if="!isEnded && latestOrderId && latestOrderStatus === 'PAID'" type="danger" plain @click="refundLatestOrder" :disabled="!isLogin" round>申请退款</el-button>
            <el-button v-if="!isEnded && booked && latestOrderStatus === 'PAID' && !signed" type="success" plain @click="sign" :disabled="!isLogin" round>课堂签到</el-button>

            <el-button v-if="signed" type="success" plain disabled round>已签到完成</el-button>
            <el-button v-else-if="booked && latestOrderStatus === 'PAID'" type="warning" plain disabled round>已支付待签到</el-button>
            <el-button v-else-if="booked" type="info" plain disabled round>已报名待支付</el-button>
          </div>
          <div class="action-tips">提示：签到必须满足“已报名 + 已支付”；签到完成后可上传自己的课堂作品。</div>
        </div>

        <div class="showcase-section-layer">
          <div class="section-header no-border">学员作品展示 ({{ showcaseList.length }})</div>
          <div class="showcase-toolbar">
            <div class="showcase-tip">课程完成后，学生可上传自己的课堂作品照片和体验感受，形成作品成果展示与正向反馈。</div>
            <el-button
              v-if="signed"
              type="primary"
              plain
              round
              @click="goMyWorks"
            >
              上传我的作品
            </el-button>
          </div>

          <div v-if="showcaseList.length" class="showcase-grid">
            <div v-for="item in showcaseList" :key="item.id" class="showcase-item-card">
              <el-image
                :src="normalizeImageUrl(item.imageUrl)"
                fit="cover"
                class="showcase-image"
                :preview-src-list="[normalizeImageUrl(item.imageUrl)]"
                preview-teleported
              />
              <div class="showcase-body">
                <div class="showcase-title">{{ item.workTitle }}</div>
                <div class="showcase-meta">{{ item.studentName || '学员作品' }} · {{ fmtTime(item.updatedAt || item.createdAt) }}</div>
                <div class="showcase-reflection">{{ item.reflection || item.gainText || '已完成课程作品上传。' }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="当前课程暂未上传学员作品，完成签到后可上传自己的课堂成果。" :image-size="80" />
        </div>

        <div class="review-section-layer">
          <div class="section-header no-border">课程评价 ({{ reviewList.length }})</div>

          <div v-if="signed" class="review-input-box shadow-hover">
            <div class="input-header">
              <span>发表我的研学体验</span>
              <el-rate v-model="myRating" size="large" />
            </div>
            <el-input v-model="myContent" type="textarea" :rows="3" maxlength="300" show-word-limit placeholder="分享你的研学感受，让更多人了解这门传统工艺课程..." class="review-textarea" />
            <div class="input-footer">
              <el-button type="primary" :loading="submittingReview" @click="submitReview" :disabled="!myRating" round>提交评价</el-button>
              <el-button @click="resetReview" plain round>清空</el-button>
            </div>
          </div>

          <div v-if="reviewList.length > 0" class="review-list">
            <div v-for="item in reviewList" :key="item.id" class="review-item">
              <div class="rev-header">
                <div class="rev-user">
                  <el-avatar :size="40" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" class="user-avatar-shadow" />
                  <div class="rev-meta">
                    <span class="rev-name">{{ item.studentName || '研学学员' }}</span>
                    <el-rate v-model="item.rating" disabled size="small" />
                  </div>
                </div>
                <span class="rev-time">{{ fmtTime(item.createdAt) }}</span>
              </div>
              <div class="rev-content">{{ item.content }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无评价数据，完成课程后欢迎留下体验反馈。" :image-size="80" />
        </div>
      </template>

      <template v-else>
        <el-divider />
        <el-alert title="当前为管理端视角，可查看课程详情、学员作品和评价反馈。" type="info" show-icon class="status-alert" />
        <div class="showcase-section-layer">
          <div class="section-header no-border">学员作品展示 ({{ showcaseList.length }})</div>
          <div v-if="showcaseList.length" class="showcase-grid">
            <div v-for="item in showcaseList" :key="item.id" class="showcase-item-card">
              <el-image
                :src="normalizeImageUrl(item.imageUrl)"
                fit="cover"
                class="showcase-image"
                :preview-src-list="[normalizeImageUrl(item.imageUrl)]"
                preview-teleported
              />
              <div class="showcase-body">
                <div class="showcase-title">{{ item.workTitle }}</div>
                <div class="showcase-meta">{{ item.studentName || '学员作品' }} · {{ fmtTime(item.updatedAt || item.createdAt) }}</div>
                <div class="showcase-reflection">{{ item.reflection || item.gainText || '已完成课程作品上传。' }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="当前课程暂未上传学员作品。" :image-size="80" />
        </div>
        <div class="admin-review-list">
          <div class="section-header">历史评价记录 ({{ reviewList.length }})</div>
          <div v-if="reviewList.length">
            <div v-for="item in reviewList" :key="item.id" class="review-item">
              <div style="font-weight:700;">{{ item.studentName || '研学学员' }} <el-rate v-model="item.rating" disabled /></div>
              <div style="margin-top: 8px; color: #64748b; line-height: 1.7;">{{ item.content }}</div>
            </div>
          </div>
          <el-empty v-else description="当前课程还没有评价记录。" :image-size="80" />
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { publicCourseApi } from '../api/course'
import { bookingApi } from '../api/booking'
import { orderApi } from '../api/order'
import { attendanceApi } from '../api/attendance'
import { reviewApi } from '../api/review'
import { showcaseApi } from '../api/showcase'
import { ElMessage } from 'element-plus'
import { getUser } from '../utils/auth'
import {
  buildCourseRichIntro,
  getCourseHighlightList,
  getCourseMaterialSet,
  getCourseOutcome,
  getCourseProvidedValue,
  getCourseTakeHome,
  getCourseTeachingFocus
} from '../utils/coursePresentation'

const DEFAULT_COURSE_COVER = '/images/course_woodcarving.png'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submittingReview = ref(false)
const course = ref(null)
const user = computed(() => getUser())
const isLogin = computed(() => !!user.value)
const isStudent = computed(() => user.value?.role === 'student')
const booked = ref(false)
const latestBookingStatus = ref(null)
const latestOrderId = ref(null)
const latestOrderStatus = ref(null)
const signed = ref(false)
const mySignStatus = ref('')
const mySignTime = ref('')
const courseId = computed(() => Number(route.params.id))
const myRating = ref(5)
const myContent = ref('')
const reviewList = ref([])
const showcaseList = ref([])

function fmtTime(v) {
  if (!v) return ''
  return String(v).replace('T', ' ').substring(0, 19)
}

function toTs(v) {
  if (!v) return NaN
  const s = String(v).trim().replace(' ', 'T')
  return Date.parse(s)
}

function statusText(s) {
  return {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    CLOSED: '已关闭'
  }[s] || (s || '未知')
}

function bookingText(s) {
  return {
    BOOKED: '已报名',
    CANCELED: '已取消'
  }[s] || '未报名'
}

function orderText(s) {
  return {
    UNPAID: '待支付',
    PAID: '已支付',
    CANCELED: '已取消',
    REFUNDED: '已退款'
  }[s] || '暂无订单'
}

const isEnded = computed(() => {
  const endTs = toTs(course.value?.endTime)
  return Number.isFinite(endTs) && endTs < Date.now()
})

const canCreateOrder = computed(() => !(latestOrderStatus.value === 'UNPAID' || latestOrderStatus.value === 'PAID'))

const courseCover = computed(() => course.value?.cover || course.value?.coverUrl || course.value?.imageUrl || DEFAULT_COURSE_COVER)

const coursePhase = computed(() => {
  if (course.value?.status === 'CLOSED') {
    return { text: '课程已关闭', type: 'warning', desc: '当前不再开放后续操作' }
  }
  const start = toTs(course.value?.startTime)
  const end = toTs(course.value?.endTime)
  const now = Date.now()
  if (!Number.isFinite(start) && !Number.isFinite(end)) {
    return { text: '待排期', type: 'info', desc: '课程时间仍可进一步完善' }
  }
  if (Number.isFinite(start) && now < start) {
    return { text: '即将开始', type: 'warning', desc: '适合提前报名和准备' }
  }
  if (Number.isFinite(end) && now > end) {
    return { text: '已结束', type: 'info', desc: '可查看课程成果和评价' }
  }
  return { text: '进行中', type: 'success', desc: '当前可参与研学体验' }
})

const capacityText = computed(() => {
  const n = Number(course.value?.capacity ?? 0)
  return n > 0 ? `${n} 人` : '不限人数'
})

const richIntro = computed(() => buildCourseRichIntro(course.value || {}))
const courseMaterials = computed(() => getCourseMaterialSet(course.value || {}))
const teachingFocus = computed(() => getCourseTeachingFocus(course.value || {}))
const courseOutcome = computed(() => getCourseOutcome(course.value || {}))
const providedValue = computed(() => getCourseProvidedValue(course.value || {}))
const takeHomeText = computed(() => getCourseTakeHome(course.value || {}))
const courseHighlights = computed(() => getCourseHighlightList(course.value || {}))

const actionGuidance = computed(() => {
  if (!isLogin.value) return '登录后可查看个人报名、支付和签到状态。'
  if (!isStudent.value) return '当前为管理视角，可查看课程信息、学员作品与评价数据。'
  if (isEnded.value) return '课程已结束，当前仅保留历史记录、作品和评价查看。'
  if (signed.value) return '你已完成签到，现在可以上传作品并在下方留下课程评价。'
  if (latestOrderStatus.value === 'PAID') return '订单已支付，下一步可以前往签到。'
  if (latestOrderStatus.value === 'UNPAID') return '你已生成订单，建议尽快完成支付。'
  if (booked.value) return '你已完成报名，下一步可以创建并支付订单。'
  return '当前课程可报名参与，建议先完成报名。'
})

const progressCards = computed(() => [
  {
    label: '课程阶段',
    value: coursePhase.value.text,
    desc: coursePhase.value.desc
  },
  {
    label: '报名状态',
    value: bookingText(latestBookingStatus.value),
    desc: booked.value ? '已进入该课程的报名链路' : '尚未进入课程报名链路'
  },
  {
    label: '订单状态',
    value: orderText(latestOrderStatus.value),
    desc: latestOrderId.value ? `最近订单编号 #${latestOrderId.value}` : '当前还没有生成订单'
  },
  {
    label: '签到状态',
    value: signed.value ? '已签到' : '未签到',
    desc: signed.value ? `签到时间 ${mySignTime.value || '已记录'}` : '课程开始后可完成课堂签到'
  }
])

function normalizeImageUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  if (url.startsWith('/api/')) return url
  if (url.startsWith('/uploads/')) return `/api${url}`
  if (url.startsWith('uploads/')) return `/api/${url}`
  return url
}

const loadReviews = async () => {
  try {
    const res = await reviewApi.listByCourse(courseId.value)
    reviewList.value = Array.isArray(res) ? res : (res?.data || [])
  } catch (e) {
    console.error('加载评价失败', e)
  }
}

const loadShowcase = async () => {
  try {
    const res = await showcaseApi.listByCourse(courseId.value)
    showcaseList.value = Array.isArray(res) ? res : (res?.data || [])
  } catch (e) {
    showcaseList.value = []
  }
}

const loadMyBookingStatus = async () => {
  const list = await bookingApi.my()
  const same = (list || []).filter((b) => Number(b.courseId) === courseId.value)
  if (same.length > 0) {
    const latest = same.reduce((a, b) => (Number(b.id) > Number(a.id) ? b : a))
    latestBookingStatus.value = latest.status || null
    booked.value = latest.status === 'BOOKED'
    return
  }
  latestBookingStatus.value = null
  booked.value = false
}

const loadMyLatestOrder = async () => {
  const orders = await orderApi.my()
  const same = (orders || []).filter((o) => Number(o.courseId) === courseId.value)
  if (same.length > 0) {
    const latest = same.reduce((a, b) => (Number(b.id) > Number(a.id) ? b : a))
    latestOrderId.value = latest.id
    latestOrderStatus.value = latest.status
    return
  }
  latestOrderId.value = null
  latestOrderStatus.value = null
}

const loadMyAttendance = async () => {
  const list = await attendanceApi.my()
  const latest = (list || []).find((a) => Number(a.courseId) === courseId.value)
  if (latest) {
    signed.value = true
    mySignStatus.value = latest.status || 'SIGNED'
    mySignTime.value = fmtTime(latest.signTime)
    return
  }
  signed.value = false
  mySignStatus.value = ''
  mySignTime.value = ''
}

const refreshStudentState = async () => {
  if (!(isStudent.value && isLogin.value)) return
  await Promise.all([loadMyBookingStatus(), loadMyLatestOrder(), loadMyAttendance()])
}

const loadDetail = async () => {
  loading.value = true
  try {
    course.value = await publicCourseApi.detail(route.params.id)
    await Promise.all([loadReviews(), loadShowcase(), refreshStudentState()])
  } finally {
    loading.value = false
  }
}

const submitReview = async () => {
  if (!myContent.value.trim()) return ElMessage.warning('请输入内容')
  submittingReview.value = true
  try {
    await reviewApi.upsert(courseId.value, { rating: myRating.value, content: myContent.value })
    ElMessage.success('评价发表成功')
    myContent.value = ''
    myRating.value = 5
    await loadReviews()
  } catch (e) {
    const msg = e.response?.data?.message || e.message || '发表失败'
    ElMessage.error(msg)
  } finally {
    submittingReview.value = false
  }
}

const resetReview = () => {
  myRating.value = 5
  myContent.value = ''
}

const runStudentAction = async (handler, successText) => {
  await handler()
  if (successText) ElMessage.success(successText)
  await Promise.all([refreshStudentState(), loadShowcase()])
}

const book = async () => runStudentAction(() => bookingApi.book(courseId.value), '报名成功')
const cancelBooking = async () => runStudentAction(() => bookingApi.cancel(courseId.value), '已取消报名')
const createOrder = async () => runStudentAction(() => orderApi.create(courseId.value), '订单已创建')
const payLatest = async () => runStudentAction(() => orderApi.pay(latestOrderId.value), '支付成功')
const cancelLatestOrder = async () => runStudentAction(() => orderApi.cancel(latestOrderId.value), '订单已取消')
const refundLatestOrder = async () => runStudentAction(() => orderApi.refund(latestOrderId.value), '退款申请已提交')
const sign = async () => runStudentAction(() => attendanceApi.sign(courseId.value), '签到成功')

const goMyWorks = () => {
  router.push({ path: '/app/my/works', query: { courseId: courseId.value } })
}

const statusTagType = (s) => (s === 'PUBLISHED' ? 'success' : (s === 'CLOSED' ? 'warning' : 'info'))

onMounted(loadDetail)
</script>

<style scoped>
.detail-container { padding: 25px; min-height: 100vh; background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%); }
.main-card { border-radius: 18px; border: none; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08); }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 22px; }
.title-group { display: flex; align-items: center; gap: 15px; }
.page-title { font-size: 22px; font-weight: 800; color: #1a1a1a; letter-spacing: 0.5px; }
.status-tag-glow { box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3); }
.overview-panel { display: flex; gap: 18px; align-items: center; padding: 18px; border-radius: 18px; background: linear-gradient(180deg, #f8fbff 0%, #f4f7fb 100%); border: 1px solid #e6edf5; margin-bottom: 18px; }
.overview-cover { width: 168px; height: 118px; border-radius: 16px; border: 1px solid #e6edf5; }
.overview-cover.placeholder { display: flex; align-items: center; justify-content: center; color: #94a3b8; background: #f8fafc; }
.overview-main { flex: 1; min-width: 0; }
.overview-title-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.overview-subtitle { margin-top: 10px; color: #64748b; line-height: 1.7; }
.overview-tags { margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap; }
.summary-row { margin-bottom: 18px; }
.summary-card { height: 100%; min-height: 94px; padding: 12px 16px; border-radius: 16px; border: 1px solid #e8eef5; background: #fff; box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04); }
.summary-label { font-size: 12px; color: #64748b; }
.summary-value { margin-top: 6px; font-size: 18px; font-weight: 800; color: #1f2937; }
.summary-desc { margin-top: 4px; font-size: 12px; color: #94a3b8; line-height: 1.45; }
.material-panel { margin-bottom: 18px; padding: 18px; border-radius: 18px; background: #f8fafc; border: 1px solid #e2e8f0; }
.material-tip { margin-top: -6px; margin-bottom: 14px; color: #64748b; line-height: 1.7; font-size: 14px; }
.material-tags { display: flex; flex-wrap: wrap; gap: 10px; }
.material-tag { padding-left: 10px; padding-right: 10px; }
.feature-grid { margin-bottom: 18px; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; align-items: start; }
.feature-card { padding: 20px; border-radius: 16px; background: #ffffff; border: 1px solid #e8eef5; box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04); }
.section-header { font-size: 17px; font-weight: 700; margin-bottom: 20px; color: #2c3e50; display: flex; align-items: center; }
.section-header::before { content: ''; display: inline-block; width: 6px; height: 18px; background: linear-gradient(to bottom, #409eff, #8cc5ff); border-radius: 4px; margin-right: 10px; }
.section-header.no-border::before { display: none; }
.section-header.small { font-size: 16px; margin-bottom: 14px; }
.focus-list,
.highlight-list { display: grid; gap: 12px; }
.focus-item,
.highlight-item { display: flex; align-items: flex-start; gap: 12px; padding: 12px 14px; border-radius: 14px; background: #f8fbff; color: #334155; line-height: 1.7; border: 1px solid #e0ebf7; }
.focus-index { width: 8px; height: 8px; border-radius: 50%; background: linear-gradient(135deg, #409eff, #60a5fa); margin-top: 9px; flex: 0 0 auto; }
.outcome-text { color: #1f2937; font-weight: 700; line-height: 1.7; }
.take-home-tip { margin-top: 12px; color: #047857; background: #ecfdf5; border: 1px solid #bbf7d0; border-radius: 12px; padding: 10px 12px; line-height: 1.7; font-size: 14px; }
.highlight-list { margin-top: 14px; }
.course-info-panel { margin-bottom: 12px; padding: 18px 20px; border-radius: 16px; background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%); border: 1px solid #e6edf5; }
.course-info-title { font-size: 22px; font-weight: 800; color: #1f2937; line-height: 1.6; word-break: break-word; }
.info-descriptions { margin-bottom: 25px; margin-top: 4px; }
:deep(.el-descriptions__table) { table-layout: fixed; }
:deep(.el-descriptions__cell) { vertical-align: top; }
:deep(.el-descriptions__label) { background: #fafafa; font-weight: 700; color: #4a5568; width: 140px; }
:deep(.el-descriptions__content) { padding-top: 16px; padding-bottom: 16px; word-break: break-word; line-height: 1.7; }
.intro-box { white-space: pre-wrap; line-height: 1.85; color: #334155; background: #f8fafc; padding: 20px; border-radius: 12px; border: 1px solid #e2e8f0; font-size: 15px; }
.course-main-title { font-size: 20px; font-weight: 800; color: #2c3e50; line-height: 1.45; word-break: break-word; }
.course-main-title.small { display: block; font-size: 18px; line-height: 1.5; }
.price-text { color: #f56c6c; font-weight: 800; font-size: 22px; font-family: 'Helvetica Neue', sans-serif; }
.status-alert { margin: 20px 0; border-radius: 10px; border: none; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.status-alert.soft { background: #f0f9ff; }
.action-card-layer, .showcase-section-layer, .review-section-layer, .admin-review-list { background: #fff; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05); border: 1px solid rgba(0,0,0,0.05); padding: 25px; border-radius: 16px; margin: 25px 0; }
.button-group { display: flex; gap: 15px; flex-wrap: wrap; margin-bottom: 15px; }
.action-tips { font-size: 13px; color: #999; background: #f8f8f8; padding: 8px 12px; border-radius: 6px; display: inline-block; }
.showcase-toolbar { display: flex; justify-content: space-between; gap: 12px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
.showcase-tip { color: #64748b; line-height: 1.7; font-size: 14px; }
.showcase-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; }
.showcase-item-card { overflow: hidden; border-radius: 16px; border: 1px solid #e5edf5; background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%); }
.showcase-image { width: 100%; height: 180px; display: block; }
.showcase-body { padding: 14px; }
.showcase-title { font-weight: 800; color: #1f2937; line-height: 1.6; }
.showcase-meta { margin-top: 6px; font-size: 12px; color: #64748b; }
.showcase-reflection { margin-top: 10px; color: #475569; line-height: 1.8; font-size: 13px; }
.review-input-box { background: #fff; padding: 25px; border-radius: 16px; margin-bottom: 30px; box-shadow: 0 4px 20px rgba(0,0,0,0.04); border: 1px solid #f5f5f5; }
.review-item { padding: 25px 0; border-bottom: 1px solid #f0f0f0; }
.rev-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.rev-user { display: flex; align-items: center; gap: 15px; }
.rev-name { font-weight: 700; font-size: 15px; color: #2c3e50; }
.rev-time { font-size: 12px; color: #999; background: #f5f5f5; padding: 4px 10px; border-radius: 12px; }
.rev-content { color: #4a5568; font-size: 15px; line-height: 1.7; padding-left: 55px; }
.input-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.input-footer { margin-top: 15px; display: flex; gap: 10px; }
@media (max-width: 768px) {
  .overview-panel { flex-direction: column; align-items: stretch; }
  .overview-cover { width: 100%; height: 180px; }
  .feature-grid { grid-template-columns: 1fr; }
  .rev-header { align-items: flex-start; flex-direction: column; }
  .rev-content { padding-left: 0; }
}
</style>
