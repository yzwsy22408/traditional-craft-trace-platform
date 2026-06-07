<template>
  <div class="page">
    <section class="hero">
      <div class="hero-main">
        <div class="hero-kicker">平台总览</div>
        <div class="hero-title">传统手工艺研学与溯源平台</div>
        <div class="hero-subtitle">
          以课程组织、作品建档和公开溯源为核心，面向老师带队研学、匠人授课实践与学生成果留存提供一体化支撑。
        </div>

        <div class="hero-badges">
          <span class="badge">老师带队研学</span>
          <span class="badge">匠人授课实践</span>
          <span class="badge">课程材料透明展示</span>
          <span class="badge">作品公开溯源</span>
          <span class="badge">学员作品正向反馈</span>
        </div>

        <div class="hero-user-card">
          <div class="status-dot" :class="{ ok: !!currentUser }"></div>
          <div>
            <div class="hero-user-title">
              <template v-if="currentUser">
                当前登录：<b>{{ currentUser.username }}</b>（{{ roleText }}）
              </template>
              <template v-else>
                当前未获取到登录信息，请重新登录后查看完整数据。
              </template>
            </div>
            <div class="hero-user-desc">系统时间持续刷新，用于显示平台当前运行状态。</div>
          </div>
        </div>

        <div class="hero-actions">
          <template v-if="isStudent">
            <el-button type="primary" round @click="go('/app/courses')">查看公开课程</el-button>
            <el-button round @click="go('/app/my/courses')">进入我的课程</el-button>
            <el-button round @click="go('/app/my/works')">上传我的作品</el-button>
          </template>
          <template v-else>
            <el-button type="primary" round @click="go('/app/course/manage')">进入课程管理</el-button>
            <el-button round @click="go('/app/crafts')">查看工艺品管理</el-button>
            <el-button round @click="go('/app/teachers')">查看老师管理</el-button>
          </template>
          <el-button plain round @click="go('/trace')">打开公开溯源页</el-button>
          <el-button plain round @click="go('/image-search')">图像辅助检索</el-button>
        </div>

        <div class="hero-bottom-grid">
          <div class="hero-panel hero-course-panel">
            <div class="hero-panel-kicker">精选公开课程</div>
            <div class="hero-panel-title">{{ heroFeatureCourse.title }}</div>
            <div class="hero-panel-desc">{{ heroFeatureCourse.introBrief }}</div>

            <div class="hero-course-meta">
              <span>{{ heroFeatureCourse.category }}</span>
              <span>带队老师：{{ heroFeatureCourse.teacherName }}</span>
              <span>{{ heroFeatureCourse.startTimeText || '时间待补充' }}</span>
            </div>

            <div class="hero-outcome-strip">
              <div class="hero-outcome-item">
                <span class="hero-outcome-label">完成成果</span>
                <strong>{{ heroFeatureCourse.outcome }}</strong>
              </div>
              <div class="hero-outcome-item">
                <span class="hero-outcome-label">课后可带走</span>
                <strong>{{ heroFeatureCourse.takeHome }}</strong>
              </div>
            </div>

            <div class="hero-course-stats">
              <div class="hero-course-stat">
                <span>报名</span>
                <strong>{{ heroFeatureCourse.bookingCount }}</strong>
              </div>
              <div class="hero-course-stat">
                <span>签到</span>
                <strong>{{ heroFeatureCourse.signedCount }}</strong>
              </div>
              <div class="hero-course-stat">
                <span>评价</span>
                <strong>{{ heroFeatureCourse.reviewCount }}</strong>
              </div>
            </div>

            <div class="hero-panel-actions">
              <el-button
                v-if="heroFeatureCourse.id"
                type="primary"
                round
                @click="goCourseDetail(heroFeatureCourse.id)"
              >
                查看课程详情
              </el-button>
              <el-button plain round @click="go('/trace')">查看公开溯源</el-button>
            </div>
          </div>

          <div class="hero-panel hero-readiness-panel">
            <div class="hero-panel-kicker">平台能力概览</div>
            <div class="hero-panel-title small">课程组织、成果留存与公开查询一体化展示</div>

            <div class="readiness-list">
              <div v-for="item in heroReadinessItems" :key="item.title" class="readiness-item">
                <div class="readiness-badge">{{ item.icon }}</div>
                <div class="readiness-main">
                  <div class="readiness-top">
                    <span class="readiness-title">{{ item.title }}</span>
                    <strong class="readiness-value">{{ item.value }}</strong>
                  </div>
                  <div class="readiness-desc">{{ item.desc }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="hero-side">
        <div class="trace-focus-card">
          <div class="trace-focus-head">
            <div>
              <div class="trace-focus-kicker">公开展示链路</div>
              <div class="trace-focus-title">从建档到扫码查看的完整流程</div>
            </div>
            <el-tag size="small" type="warning" effect="dark" round>公开查询流程</el-tag>
          </div>

          <div class="trace-flow">
            <div v-for="(item, index) in traceFlowSteps" :key="item.title" class="trace-flow-item">
              <div class="trace-flow-index">{{ String(index + 1).padStart(2, '0') }}</div>
              <div>
                <div class="trace-flow-title">{{ item.title }}</div>
                <div class="trace-flow-desc">{{ item.desc }}</div>
              </div>
            </div>
          </div>

          <div class="trace-focus-foot">
            <span class="trace-foot-label">当前可公开查询工艺档案</span>
            <strong>{{ summary.craftCount }}</strong>
            <span>件</span>
          </div>
        </div>

        <div class="hero-kpi-grid">
          <div class="hero-kpi-card">
            <div class="hero-kpi-label">公开课程</div>
            <div class="hero-kpi-value">{{ summary.courseCount }}</div>
            <div class="hero-kpi-desc">支持学生查看、报名、签到与评价的课程数量</div>
          </div>
          <div class="hero-kpi-card">
            <div class="hero-kpi-label">工艺档案</div>
            <div class="hero-kpi-value">{{ summary.craftCount }}</div>
            <div class="hero-kpi-desc">已建档并支持公开溯源查询的工艺品数量</div>
          </div>
          <div class="hero-kpi-card">
            <div class="hero-kpi-label">合作工坊</div>
            <div class="hero-kpi-value">{{ summary.workshopCount }}</div>
            <div class="hero-kpi-desc">承接线下课程体验与工艺实践的工坊数量</div>
          </div>
          <div class="hero-kpi-card live-card">
            <div class="hero-kpi-label">当前时间</div>
            <div class="hero-kpi-time">{{ currentTime }}</div>
            <div class="hero-kpi-desc">实时显示当前访问时刻</div>
          </div>
        </div>
      </div>
    </section>

    <section class="insight-strip">
      <div v-for="item in insightCards" :key="item.label" class="insight-card">
        <div class="insight-label">{{ item.label }}</div>
        <div class="insight-value">{{ item.value }}</div>
        <div class="insight-desc">{{ item.desc }}</div>
      </div>
    </section>

    <el-row :gutter="16" class="section-row">
      <el-col :xs="24" :md="12">
        <el-card class="card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title">课程热度榜</div>
              <el-tag size="small" type="danger" effect="plain" round>按报名、签到、评价综合计算</el-tag>
            </div>
          </template>

          <div v-if="heatCourses.length" class="heat-list">
            <button
              v-for="(item, index) in heatCourses"
              :key="item.id || index"
              type="button"
              class="heat-item"
              @click="goCourseDetail(item.id)"
            >
              <div class="heat-rank">{{ index + 1 }}</div>
              <div class="heat-main">
                <div class="heat-title-row">
                  <div class="heat-title">{{ item.title }}</div>
                  <el-tag size="small" type="warning" effect="dark" round>{{ item.heatLabel || '热门课程' }}</el-tag>
                </div>
                <div class="heat-meta">
                  <span>{{ item.category || '公开课程' }}</span>
                  <span>带队老师：{{ item.teacherName || '未设置' }}</span>
                  <span>开课时间：{{ formatDateText(item.startTime) || '待安排' }}</span>
                </div>
                <div class="heat-stat-line">
                  <span>报名 {{ item.bookingCount ?? 0 }}</span>
                  <span>签到 {{ item.signedCount ?? 0 }}</span>
                  <span>评价 {{ item.reviewCount ?? 0 }}</span>
                  <span class="heat-score">热度值 {{ item.heatScore ?? 0 }}</span>
                </div>
              </div>
            </button>
          </div>
          <el-empty v-else description="暂无课程热度数据" :image-size="72" />
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card class="card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title">推荐课程</div>
              <el-tag size="small" effect="plain" round>可快速了解课程安排</el-tag>
            </div>
          </template>

          <div v-if="recommendedCoursePanels.length" class="recommend-list">
            <button
              v-for="item in recommendedCoursePanels"
              :key="item.id"
              type="button"
              class="recommend-card"
              @click="goCourseDetail(item.id)"
            >
              <div class="recommend-head">
                <div>
                  <div class="recommend-title">{{ item.title }}</div>
                  <div class="recommend-meta">{{ item.category || '公开课程' }} · {{ item.startTimeText || '待安排时间' }}</div>
                </div>
                <div class="recommend-outcome">{{ item.outcome }}</div>
              </div>
              <div class="recommend-intro">{{ item.introBrief }}</div>
              <div class="recommend-extra">
                <div><span class="extra-label">课堂提供：</span>{{ item.providedValue }}</div>
                <div><span class="extra-label">课后可带走：</span>{{ item.takeHome }}</div>
              </div>
            </button>
          </div>
          <el-empty v-else description="暂无推荐课程" :image-size="72" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="section-row">
      <el-col :xs="24" :md="14">
        <el-card class="card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title">工艺分类分布</div>
              <el-tag size="small" effect="plain" round>按工艺档案统计</el-tag>
            </div>
          </template>
          <div ref="barChartRef" class="chart-box"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card class="card" shadow="never">
          <template #header>
            <div class="card-title">平台资源结构</div>
          </template>
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="section-row">
      <el-col :xs="24" :md="14">
        <el-card class="card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title">快捷入口</div>
              <el-button size="small" round @click="refreshAll" :loading="loading">刷新首页数据</el-button>
            </div>
          </template>

          <div class="quick-grid">
            <button v-for="item in quickEntries" :key="item.title" type="button" class="quick-card" @click="go(item.path)">
              <div class="quick-icon" :class="item.tone">{{ item.icon }}</div>
              <div class="quick-content">
                <div class="quick-title">{{ item.title }}</div>
                <div class="quick-desc">{{ item.desc }}</div>
              </div>
            </button>
          </div>
        </el-card>

        <el-card class="card" shadow="never" style="margin-top: 16px;">
          <template #header>
            <div class="card-title">平台核心能力</div>
          </template>
          <div class="highlight-grid">
            <div v-for="item in platformHighlights" :key="item.title" class="highlight-card">
              <div class="highlight-kicker">{{ item.kicker }}</div>
              <div class="highlight-title">{{ item.title }}</div>
              <div class="highlight-desc">{{ item.desc }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card class="card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title">系统通知</div>
              <el-tag size="small" effect="plain" round>{{ notices.length }} 条平台通知</el-tag>
            </div>
          </template>

          <div class="notice-list">
            <div v-for="item in notices" :key="item.id" class="notice-item">
              <div class="notice-top">
                <el-tag size="small" :type="item.type" effect="light" round>{{ item.level }}</el-tag>
                <span class="notice-title">{{ item.title }}</span>
              </div>
              <div class="notice-content">{{ item.content }}</div>
              <div class="notice-meta">{{ item.time }} · {{ item.by }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="card" shadow="never" style="margin-top: 16px;">
          <template #header>
            <div class="card-title">近期工坊档案</div>
          </template>
          <div v-if="recentWorkshops.length" class="workshop-list">
            <div v-for="item in recentWorkshops" :key="item.id" class="workshop-item">
              <div class="workshop-name">{{ item.name }}</div>
              <div class="workshop-meta">负责人：{{ item.ownerName || item.ownerUsername || '未设置' }}</div>
              <div class="workshop-address">{{ item.address || '未设置工坊地址' }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无工坊档案数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request'
import { publicCourseApi } from '../api/course'
import { workshopApi } from '../api/workshop'
import { listCraftApi } from '../api/craft'
import { getUser } from '../utils/auth'
import { buildCourseRichIntro, getCourseMaterialSet, getCourseOutcome, getCourseProvidedValue, getCourseTakeHome } from '../utils/coursePresentation'

const router = useRouter()
const currentUser = computed(() => getUser())
const role = computed(() => String(currentUser.value?.role || '').toLowerCase())
const isStudent = computed(() => role.value === 'student')

const roleText = computed(() => {
  if (role.value === 'admin') return '管理员'
  if (role.value === 'handicraft') return '匠人'
  if (role.value === 'student') return '学生'
  return '游客'
})

const loading = ref(false)
const currentTime = ref('')
const summary = ref({
  workshopCount: 0,
  craftCount: 0,
  courseCount: 0,
  categories: [],
  categoryData: []
})
const publicCourses = ref([])
const heatSource = ref([])
const workshops = ref([])
const crafts = ref([])
const barChartRef = ref(null)
const pieChartRef = ref(null)

const notices = ref([
  {
    id: 1,
    level: '工坊提示',
    type: 'warning',
    title: '课堂材料会在集合后统一发放',
    content: '学生端已明确展示课程提供材料与可带走成果，便于报名前充分了解课程安排。',
    time: '2026-05-15 09:20',
    by: '课程运营'
  },
  {
    id: 2,
    level: '成果动态',
    type: 'success',
    title: '学员作品页支持成果留存',
    content: '学生完成签到后即可上传作品照片与研学收获，形成持续沉淀的课程成果记录。',
    time: '2026-05-15 09:10',
    by: '产品设计'
  },
  {
    id: 3,
    level: '课程动态',
    type: 'primary',
    title: '课程热度榜已接入首页展示',
    content: '首页支持综合报名、签到、评价生成课程热度排名，便于查看课程关注度变化。',
    time: '2026-05-15 08:50',
    by: '平台系统'
  }
])

const traceFlowSteps = [
  {
    title: '作品建档',
    desc: '登记作品编号、所属类别、负责匠人与展示信息。'
  },
  {
    title: '工序留痕',
    desc: '补充材料来源、关键工序说明与制作过程图片。'
  },
  {
    title: '扫码公开查看',
    desc: '手机扫码即可进入公开溯源页查看完整过程。'
  }
]

const materialTypeCount = computed(() => new Set(publicCourses.value.flatMap((item) => getCourseMaterialSet(item))).size)

const heroFeatureCourse = computed(() => {
  const source = heatCourses.value[0] || publicCourses.value[0] || null
  if (!source) {
    return {
      id: null,
      title: '课程数据加载中',
      category: '公开课程',
      teacherName: '待补充',
      startTimeText: '',
      outcome: '支持成果展示',
      takeHome: '支持课堂作品留存',
      introBrief: '首页会在课程、工坊与工艺档案加载完成后自动生成一条适合查看的课程推荐。',
      bookingCount: 0,
      signedCount: 0,
      reviewCount: 0
    }
  }

  const detail = publicCourses.value.find((item) => item.id === source.id) || source
  return {
    id: detail.id,
    title: detail.title || '未命名课程',
    category: detail.category || '公开课程',
    teacherName: detail.teacherName || '未设置',
    startTimeText: formatDateText(detail.startTime),
    outcome: getCourseOutcome(detail),
    takeHome: getCourseTakeHome(detail),
    introBrief: getBriefText(buildCourseRichIntro(detail), 92),
    bookingCount: Number(source.bookingCount ?? 0),
    signedCount: Number(source.signedCount ?? 0),
    reviewCount: Number(source.reviewCount ?? 0)
  }
})

const heroReadinessItems = computed(() => [
  {
    icon: '作',
    title: '课程成果展示',
    value: `${summary.value.courseCount || 0} 门`,
    desc: '课程详情页已补齐学生作品图、学习心得和历史评价记录。'
  },
  {
    icon: '源',
    title: '公开溯源链路',
    value: `${summary.value.craftCount || 0} 件`,
    desc: '支持作品建档、工序留痕与扫码公开查看的完整查询流程。'
  },
  {
    icon: '材',
    title: '课堂材料透明',
    value: `${materialTypeCount.value} 类`,
    desc: '首页与课程详情已明确展示课堂提供材料和可带走成果。'
  },
  {
    icon: '图',
    title: '图像辅助检索',
    value: '已接入',
    desc: '上传作品图片即可辅助匹配相近工艺档案与学生作品。'
  }
])

const insightCards = computed(() => {
  const courseCount = publicCourses.value.length
  const avgPrice = courseCount
    ? (publicCourses.value.reduce((sum, item) => sum + Number(item.price || 0), 0) / courseCount).toFixed(1)
    : '0.0'
  const categoryIndex = summary.value.categoryData
    .map((value, index) => ({ value: Number(value || 0), category: summary.value.categories[index] || '未分类' }))
    .sort((a, b) => b.value - a.value)[0]
  const materialKinds = materialTypeCount.value

  return [
    {
      label: '课程均价',
      value: `￥${avgPrice}`,
      desc: '基于当前公开课程价格计算，用于查看课程定价区间。'
    },
    {
      label: '热门工艺方向',
      value: categoryIndex ? categoryIndex.category : '待生成',
      desc: '按当前工艺档案数量统计出的重点工艺方向。'
    },
    {
      label: '课堂材料种类',
      value: materialKinds,
      desc: '公开课程说明中已覆盖的材料与工具种类数量。'
    },
    {
      label: '学员体验闭环',
      value: '已打通',
      desc: '支持报名、签到、作品上传、评价与公开溯源的完整使用流程。'
    }
  ]
})

const recommendedCoursePanels = computed(() => {
  return publicCourses.value.slice(0, 3).map((item) => ({
    id: item.id,
    title: item.title || '未命名课程',
    category: item.category || '公开课程',
    teacherName: item.teacherName || '未设置',
    startTimeText: formatDateText(item.startTime),
    outcome: getCourseOutcome(item),
    providedValue: getCourseProvidedValue(item),
    takeHome: getCourseTakeHome(item),
    materials: getCourseMaterialSet(item),
    introBrief: getBriefText(buildCourseRichIntro(item), 110)
  }))
})

const heatCourses = computed(() => {
  if (heatSource.value.length) {
    return heatSource.value.slice(0, 5)
  }
  return publicCourses.value.slice(0, 5).map((item, index) => ({
    id: item.id,
    title: item.title,
    category: item.category,
    teacherName: item.teacherName,
    startTime: item.startTime,
    bookingCount: 0,
    signedCount: 0,
    reviewCount: 0,
    heatScore: Math.max(10 - index, 1),
    heatLabel: '推荐课程'
  }))
})

const quickEntries = computed(() => {
  if (isStudent.value) {
    return [
      { title: '公开课程', desc: '查看课程时间、课堂材料、可带走成果和课程详情。', path: '/app/courses', icon: '课', tone: 'course' },
      { title: '我的课程', desc: '查看报名、支付、签到和结课进度，进入个人学习链路。', path: '/app/my/courses', icon: '学', tone: 'trace' },
      { title: '我的作品', desc: '上传课堂作品照片与心得，形成自己的个人成果记录。', path: '/app/my/works', icon: '作', tone: 'work' },
      { title: '公开溯源', desc: '通过作品编号查看工艺品是谁做的、材料来自哪里。', path: '/trace', icon: '源', tone: 'default' },
      { title: '图像检索', desc: '上传作品图片，快速匹配相近的工艺档案或学生作品。', path: '/image-search', icon: '图', tone: 'course' }
    ]
  }

  return [
    { title: '工艺品管理', desc: '维护工艺品档案、负责匠人、展柜位置和公开溯源说明。', path: '/app/crafts', icon: '档', tone: 'default' },
    { title: '课程管理', desc: '发布课程、查看预览，并补全学生端将看到的材料与成果说明。', path: '/app/course/manage', icon: '课', tone: 'course' },
    { title: '老师管理', desc: '维护带队老师、学校来源和线下研学组织信息。', path: '/app/teachers', icon: '师', tone: 'trace' },
    { title: '溯源管理', desc: '记录工序步骤、执行匠人、材料来源和公开查询信息。', path: '/app/traces', icon: '源', tone: 'work' }
  ]
})

const platformHighlights = computed(() => [
  {
    kicker: '课程体验',
    title: '学生报名前能看懂课堂会提供什么',
    desc: '课程详情页已明确展示老师带队方式、课堂提供材料、课程重点和课后可带走成果。'
  },
  {
    kicker: '作品反馈',
    title: '学生上课后可上传自己的课堂作品',
    desc: '把课堂成果图片、学习收获和体验感受沉淀为个人作品页，增强参与感与成就感。'
  },
  {
    kicker: '公开溯源',
    title: '查看制作责任人、材料来源与工序记录',
    desc: '工艺品溯源页已支持作品负责匠人、材料来源、展柜位置和工序记录的完整查询。'
  },
  {
    kicker: '热度分析',
    title: '课程热度榜直观反映参与情况',
    desc: '首页已把报名、签到、评价三类数据综合成课程热度，便于查看课程关注度与参与度。'
  }
])

const recentWorkshops = computed(() => workshops.value.slice(0, 3))

const normalizedCategoryStats = computed(() => {
  const names = Array.isArray(summary.value.categories) ? summary.value.categories : []
  const values = Array.isArray(summary.value.categoryData) ? summary.value.categoryData : []
  return names
    .map((name, index) => ({
      name: String(name || '未分类工艺'),
      value: Number(values[index] || 0)
    }))
    .filter((item) => item.value > 0)
    .sort((a, b) => b.value - a.value || String(a.name).localeCompare(String(b.name), 'zh-Hans-CN'))
})

function formatDateText(value) {
  return value ? String(value).replace('T', ' ').substring(0, 16) : ''
}

function getBriefText(text, maxLength = 90) {
  const content = String(text || '').replace(/\s+/g, ' ').trim()
  return content.length > maxLength ? `${content.slice(0, maxLength)}...` : content
}

function go(path) {
  if (path.startsWith('/trace') || path.startsWith('/image-search')) {
    window.open(path, '_blank', 'noopener,noreferrer')
    return
  }
  router.push(path)
}

function goCourseDetail(id) {
  if (!id) return
  router.push(`/app/course/${id}`)
}

let echartsApi = null
let barChart = null
let pieChart = null
let timer = null

async function ensureEcharts() {
  if (echartsApi) return echartsApi
  const { init, graphic } = await import('../utils/echartsLoader')
  echartsApi = { init, graphic }
  return echartsApi
}

async function initCharts() {
  await ensureEcharts()
  await nextTick()

  if (barChartRef.value) {
    if (!barChart) barChart = echartsApi.init(barChartRef.value)
    const categoryStats = normalizedCategoryStats.value
    barChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (params) => {
          const row = Array.isArray(params) ? params[0] : params
          if (!row) return ''
          return `${row.name}<br/>建档数量：${row.value} 件`
        }
      },
      grid: { left: 32, right: 18, top: 30, bottom: 58 },
      xAxis: {
        type: 'category',
        data: categoryStats.map((item) => item.name),
        axisLabel: { color: '#64748b', interval: 0, rotate: 18 },
        axisLine: { lineStyle: { color: '#cbd5e1' } }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { color: '#64748b' },
        splitLine: { lineStyle: { color: '#e2e8f0' } }
      },
      series: [
        {
          type: 'bar',
          data: categoryStats.map((item) => item.value),
          barWidth: 34,
          showBackground: true,
          backgroundStyle: {
            color: 'rgba(148, 163, 184, 0.08)',
            borderRadius: [12, 12, 0, 0]
          },
          label: {
            show: true,
            position: 'top',
            color: '#1e3a5f',
            fontWeight: 700,
            formatter: '{c}'
          },
          itemStyle: {
            borderRadius: [12, 12, 0, 0],
            color: new echartsApi.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#7fb3ff' },
              { offset: 0.55, color: '#4f8cf5' },
              { offset: 1, color: '#2b63d9' }
            ])
          }
        }
      ]
    })
  }

  if (pieChartRef.value) {
    if (!pieChart) pieChart = echartsApi.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, textStyle: { color: '#64748b' } },
      series: [
        {
          type: 'pie',
          radius: ['40%', '68%'],
          center: ['50%', '42%'],
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 4 },
          label: { formatter: '{b}\n{c}' },
          data: [
            { value: Number(summary.value.workshopCount || 0), name: '工坊' },
            { value: Number(summary.value.courseCount || 0), name: '课程' },
            { value: Number(summary.value.craftCount || 0), name: '工艺档案' }
          ]
        }
      ]
    })
  }
}

async function fetchSummaryData() {
  const tasks = [
    request.get('/public/stat/summary'),
    publicCourseApi.list(),
    publicCourseApi.heatList()
  ]

  if (role.value === 'admin' || role.value === 'handicraft') {
    tasks.push(workshopApi.list(), listCraftApi())
  }

  const results = await Promise.allSettled(tasks)
  const [summaryRes, courseRes, heatRes, workshopRes, craftRes] = results

  if (summaryRes.status === 'fulfilled') {
    const data = summaryRes.value?.data ?? summaryRes.value
    summary.value = {
      workshopCount: Number(data?.workshopCount || 0),
      craftCount: Number(data?.craftCount || 0),
      courseCount: Number(data?.courseCount || 0),
      categories: Array.isArray(data?.categories) ? data.categories : [],
      categoryData: Array.isArray(data?.categoryData) ? data.categoryData : []
    }
  }

  if (courseRes.status === 'fulfilled') {
    const data = courseRes.value?.data ?? courseRes.value
    publicCourses.value = Array.isArray(data) ? data : []
  } else {
    publicCourses.value = []
  }

  if (heatRes.status === 'fulfilled') {
    const data = heatRes.value?.data ?? heatRes.value
    heatSource.value = Array.isArray(data) ? data : []
  } else {
    heatSource.value = []
  }

  if (workshopRes?.status === 'fulfilled') {
    const data = workshopRes.value?.data ?? workshopRes.value
    workshops.value = Array.isArray(data) ? data : []
  } else {
    workshops.value = []
  }

  if (craftRes?.status === 'fulfilled') {
    const data = craftRes.value?.data ?? craftRes.value
    crafts.value = Array.isArray(data) ? data : []
  } else {
    crafts.value = []
  }

  await initCharts()
}

async function refreshAll() {
  loading.value = true
  try {
    await fetchSummaryData()
  } finally {
    loading.value = false
  }
}

function updateClock() {
  currentTime.value = new Date().toLocaleString()
}

onMounted(() => {
  refreshAll()
  updateClock()
  timer = window.setInterval(updateClock, 1000)
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
  barChart?.dispose?.()
  pieChart?.dispose?.()
})
</script>

<style scoped>
.page {
  padding: 18px;
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(215, 166, 99, 0.15), transparent 24%),
    linear-gradient(145deg, #f8f4ee 0%, #f7fbff 18%, #edf4fb 100%);
}
.hero {
  position: relative;
  display: grid;
  grid-template-columns: 1.25fr 1fr;
  gap: 18px;
  padding: 30px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(247, 216, 167, 0.18), transparent 28%),
    linear-gradient(135deg, #10253a 0%, #17324d 48%, #10243a 100%);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 28px 55px rgba(15, 23, 42, 0.14);
}
.hero::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 28px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.04), transparent 52%),
    radial-gradient(circle at bottom left, rgba(255, 255, 255, 0.05), transparent 28%);
  pointer-events: none;
}
.hero-main,
.hero-side {
  position: relative;
  z-index: 1;
}
.hero-main {
  display: flex;
  flex-direction: column;
}
.hero-kicker {
  display: inline-flex;
  align-items: center;
  padding: 7px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  color: #f4d7a7;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 1.8px;
}
.hero-title {
  margin-top: 18px;
  font-size: 34px;
  font-weight: 900;
  line-height: 1.25;
  letter-spacing: 0.3px;
}
.hero-subtitle {
  margin-top: 14px;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.82);
  max-width: 760px;
}
.hero-badges {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.badge {
  padding: 7px 13px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.09);
  border: 1px solid rgba(255, 255, 255, 0.14);
  font-size: 12px;
  font-weight: 700;
}
.hero-user-card {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.08);
}
.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #94a3b8;
  flex-shrink: 0;
}
.status-dot.ok {
  background: #10b981;
  box-shadow: 0 0 12px #10b981;
}
.hero-user-title {
  font-size: 16px;
  line-height: 1.7;
}
.hero-user-desc {
  margin-top: 4px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.72);
}
.hero-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.hero-bottom-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  gap: 14px;
}
.hero-panel {
  padding: 18px 18px 16px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06);
}
.hero-panel-kicker {
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 1px;
  color: #f7d8a7;
}
.hero-panel-title {
  margin-top: 8px;
  font-size: 20px;
  line-height: 1.5;
  font-weight: 900;
  color: #ffffff;
}
.hero-panel-title.small {
  font-size: 18px;
}
.hero-panel-desc {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.78);
}
.hero-course-meta {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px 14px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}
.hero-outcome-strip {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.hero-outcome-item,
.hero-course-stat {
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}
.hero-outcome-label,
.hero-course-stat span {
  display: block;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.66);
}
.hero-outcome-item strong,
.hero-course-stat strong {
  display: block;
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
  color: #fff7e8;
}
.hero-course-stats {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.hero-course-stat strong {
  font-size: 24px;
  line-height: 1.2;
  color: #ffffff;
}
.hero-panel-actions {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.readiness-list {
  margin-top: 14px;
  display: grid;
  gap: 12px;
}
.readiness-item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  padding: 12px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}
.readiness-item:first-child {
  padding-top: 0;
  border-top: none;
}
.readiness-badge {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, rgba(247, 216, 167, 0.95) 0%, rgba(215, 166, 99, 0.95) 100%);
  color: #132c44;
  font-weight: 900;
}
.readiness-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}
.readiness-title {
  font-size: 14px;
  font-weight: 800;
  color: #fff6e2;
}
.readiness-value {
  font-size: 14px;
  color: #f7d8a7;
}
.readiness-desc {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.72);
}
.hero-side {
  display: grid;
  gap: 14px;
}
.trace-focus-card {
  padding: 20px 20px 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.09);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06);
}
.trace-focus-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}
.trace-focus-kicker {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.68);
  font-weight: 700;
}
.trace-focus-title {
  margin-top: 6px;
  font-size: 18px;
  font-weight: 800;
  color: #ffffff;
}
.trace-flow {
  margin-top: 18px;
  display: grid;
  gap: 12px;
}
.trace-flow-item {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  padding: 12px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}
.trace-flow-item:first-child {
  padding-top: 0;
  border-top: none;
}
.trace-flow-index {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #f7d8a7 0%, #d7a663 100%);
  color: #10253a;
  font-size: 14px;
  font-weight: 900;
}
.trace-flow-title {
  font-size: 15px;
  font-weight: 800;
  color: #fff4de;
}
.trace-flow-desc {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.72);
}
.trace-focus-foot {
  margin-top: 18px;
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.82);
}
.trace-foot-label {
  font-size: 13px;
}
.trace-focus-foot strong {
  font-size: 28px;
  color: #f7d8a7;
}
.hero-kpi-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.hero-kpi-card {
  min-height: 120px;
  padding: 20px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06);
}
.hero-kpi-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.72);
}
.hero-kpi-value {
  margin-top: 10px;
  font-size: 30px;
  font-weight: 900;
}
.hero-kpi-time {
  margin-top: 12px;
  font-size: 22px;
  font-weight: 800;
  color: #34d399;
  font-family: monospace;
}
.hero-kpi-desc {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.7);
}
.insight-strip {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}
.insight-card,
.card {
  border-radius: 22px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.05);
}
.insight-card {
  padding: 20px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(248, 251, 255, 0.98) 100%);
}
.insight-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 700;
}
.insight-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 900;
  color: #0f172a;
}
.insight-desc {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.7;
  color: #64748b;
}
.section-row {
  margin-top: 16px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.card-title {
  font-weight: 800;
  color: #1e293b;
}
.heat-list,
.recommend-list,
.notice-list,
.workshop-list {
  display: grid;
  gap: 12px;
}
.heat-item,
.recommend-card {
  width: 100%;
  text-align: left;
  border: 1px solid #e5edf5;
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 1) 100%);
  padding: 15px 16px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}
.heat-item:hover,
.recommend-card:hover,
.quick-card:hover {
  transform: translateY(-2px);
  border-color: #60a5fa;
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.08);
}
.heat-item {
  display: grid;
  grid-template-columns: 48px 1fr;
  gap: 12px;
  align-items: center;
}
.heat-rank {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 20px;
  font-weight: 900;
}
.heat-title-row,
.recommend-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}
.heat-title,
.recommend-title,
.quick-title,
.highlight-title,
.workshop-name {
  font-weight: 800;
  color: #0f172a;
}
.heat-meta,
.heat-stat-line,
.recommend-meta,
.notice-meta,
.workshop-meta,
.workshop-address {
  margin-top: 6px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #64748b;
}
.heat-score {
  color: #dc2626;
  font-weight: 700;
}
.recommend-outcome {
  max-width: 140px;
  padding: 6px 10px;
  border-radius: 12px;
  background: #ecfdf5;
  color: #047857;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.6;
}
.recommend-intro,
.notice-content,
.highlight-desc,
.quick-desc,
.workshop-address {
  margin-top: 10px;
  color: #475569;
  line-height: 1.8;
  font-size: 13px;
}
.recommend-extra {
  margin-top: 12px;
  display: grid;
  gap: 8px;
  font-size: 13px;
  color: #334155;
  line-height: 1.8;
}
.extra-label {
  color: #2563eb;
  font-weight: 700;
}
.chart-box {
  height: 320px;
}
.quick-grid,
.highlight-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.quick-card {
  width: 100%;
  text-align: left;
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid #e5edf5;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}
.quick-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: #2563eb;
  background: #eff6ff;
  font-weight: 900;
  flex-shrink: 0;
}
.quick-icon.course { background: #fffbeb; color: #d97706; }
.quick-icon.trace { background: #ecfdf5; color: #059669; }
.quick-icon.work { background: #eef2ff; color: #4f46e5; }
.highlight-card {
  padding: 16px;
  border-radius: 18px;
  border: 1px solid #dbeafe;
  background: linear-gradient(180deg, #f8fbff 0%, #eef6ff 100%);
}
.highlight-kicker {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  background: #dbeafe;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
}
.highlight-title {
  margin-top: 12px;
  line-height: 1.6;
}
.notice-item,
.workshop-item {
  padding: 15px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
}
.notice-top {
  display: flex;
  align-items: center;
  gap: 8px;
}
.notice-title {
  font-weight: 700;
  color: #1e293b;
}
:deep(.el-card__header) {
  padding: 18px 22px 8px;
  border-bottom: none;
}
:deep(.el-card__body) {
  padding: 12px 22px 22px;
}
@media (max-width: 1200px) {
  .insight-strip { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 900px) {
  .hero { grid-template-columns: 1fr; }
  .hero-bottom-grid,
  .hero-kpi-grid,
  .quick-grid,
  .highlight-grid { grid-template-columns: 1fr; }
  .trace-focus-head { flex-direction: column; }
}
@media (max-width: 768px) {
  .insight-strip { grid-template-columns: 1fr; }
  .hero-outcome-strip,
  .hero-course-stats,
  .heat-item { grid-template-columns: 1fr; }
  .heat-title-row,
  .recommend-head,
  .readiness-top,
  .hero-actions { flex-direction: column; align-items: stretch; }
}
</style>
