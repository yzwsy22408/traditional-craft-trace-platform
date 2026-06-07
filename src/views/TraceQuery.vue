<template>
  <div class="trace-page">
    <section class="hero">
      <div class="hero-content">
        <div class="hero-title">作品溯源档案查询</div>
        <div class="hero-desc">
          输入作品编号后，可查看作品基础信息、材料来源、录入工坊与完整工序记录，
          直接展示项目的成品档案状态与公开展示信息。
        </div>
      </div>

      <div class="search-panel">
        <el-input
          v-model="queryForm.code"
          placeholder="请输入作品编号，例如：CI001"
          clearable
          class="grand-input"
          @keyup.enter="doQuery"
        />
        <el-button type="primary" class="query-btn" :loading="loading" @click="doQuery">查询档案</el-button>
      </div>

      <div class="hero-actions">
        <el-button round @click="openImageSearch">图片检索</el-button>
      </div>
    </section>

    <el-empty
      v-if="queried && !craftInfo"
      :image-size="180"
      description="未查询到对应作品档案，请检查作品编号是否正确。"
      class="empty-wrap"
    />

    <div v-if="craftInfo" class="result-wrap">
      <el-card class="archive-card" shadow="never">
        <div class="archive-head">
          <div>
            <div class="archive-title">{{ craftInfo.name || '未命名作品' }}</div>
            <div class="archive-sub">编号：{{ craftInfo.code || '未填写' }}</div>
          </div>
          <div class="archive-head-actions">
            <el-tag effect="plain" type="success">{{ craftInfo.category || '未分类' }}</el-tag>
            <el-button size="small" type="primary" plain @click="openPublicTrace">打开公开展示页</el-button>
            <el-button size="small" @click="copyPublicTraceLink">复制公开链接</el-button>
          </div>
        </div>

        <div class="archive-layout">
          <el-image
            :src="craftCover"
            fit="cover"
            :preview-src-list="[craftCover]"
            preview-teleported
            class="craft-cover-image"
          >
            <template #error>
              <div class="img-error-box">封面加载失败</div>
            </template>
          </el-image>

          <div class="archive-info">
            <div class="archive-grid">
              <div class="archive-item">
                <label>作品说明</label>
                <p>{{ craftDisplayDescription(craftInfo) || '暂无作品描述' }}</p>
              </div>
              <div class="archive-item">
                <label>档案摘要</label>
                <p>共 {{ steps.length }} 个工序节点，可完整展示作品来源、制作过程与成果说明。</p>
              </div>
            </div>

            <div class="archive-descriptions">
              <div class="desc-row"><span>录入匠人</span><strong>{{ traceMeta.artisanName || '待补充' }}</strong></div>
              <div class="desc-row"><span>匠人角色</span><strong>{{ traceMeta.artisanTitle || '项目负责匠人' }}</strong></div>
              <div class="desc-row"><span>录入工坊</span><strong>{{ traceMeta.workshopName || '待补充' }}</strong></div>
              <div class="desc-row"><span>展示位置</span><strong>{{ traceMeta.displayLocation || '待补充展示柜或展台位置' }}</strong></div>
              <div class="desc-row"><span>溯源码位置</span><strong>{{ traceMeta.qrPlacement || '建议张贴在作品铭牌、说明卡或展台侧边' }}</strong></div>
              <div class="desc-row"><span>建档时间</span><strong>{{ formatTime(craftInfo.createdAt) || '暂无记录' }}</strong></div>
            </div>
          </div>
        </div>

        <div class="archive-metrics">
          <div class="metric-box">
            <div class="metric-label">已完成工序</div>
            <div class="metric-value">{{ traceMetrics.completedSteps }}</div>
            <div class="metric-desc">状态为已完成的工序数量</div>
          </div>
          <div class="metric-box">
            <div class="metric-label">工序总数</div>
            <div class="metric-value">{{ traceMetrics.totalSteps }}</div>
            <div class="metric-desc">已录入的完整工艺节点</div>
          </div>
          <div class="metric-box">
            <div class="metric-label">图片证据</div>
            <div class="metric-value">{{ traceMetrics.imageCount }}</div>
            <div class="metric-desc">对应制作过程与成品阶段图片</div>
          </div>
          <div class="metric-box">
            <div class="metric-label">档案完整度</div>
            <div class="metric-value">{{ traceMetrics.completenessRate }}%</div>
            <div class="metric-desc">根据说明、材料和图片记录统计</div>
          </div>
        </div>

        <div class="source-board">
          <div class="source-card">
            <div class="source-title">材料来源摘要</div>
            <div class="source-text">{{ defaultMaterialSummary }}</div>
          </div>
          <div class="source-card">
            <div class="source-title">主要来源标签</div>
            <div class="source-tags">
              <el-tag
                v-for="item in materialSourceList"
                :key="item"
                effect="plain"
                round
                class="source-tag"
              >
                {{ item }}
              </el-tag>
              <span v-if="!materialSourceList.length" class="muted">暂未登记来源标签</span>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="timeline-card" shadow="never">
        <div class="section-title">全流程工序时间线</div>

        <el-empty
          v-if="sortedSteps.length === 0"
          :image-size="120"
          description="该作品暂未录入溯源步骤。"
        />

        <el-timeline v-else class="timeline">
          <el-timeline-item
            v-for="(item, index) in sortedSteps"
            :key="item.id || index"
            :timestamp="formatTime(item.operateTime)"
            placement="top"
            type="primary"
            size="large"
          >
            <el-card class="step-card" shadow="hover">
              <div class="step-head">
                <div>
                  <div class="step-no">步骤 {{ padStepNo(item.stepNo) }}</div>
                  <div class="step-name">{{ item.stepName || '未命名工序' }}</div>
                </div>
                <div class="step-tag-row">
                  <el-tag size="small" type="success" effect="light" round>
                    执行匠人：{{ item.operatorName || traceMeta.artisanName || '负责匠人' }}
                  </el-tag>
                  <el-tag
                    v-if="traceMeta.workshopName"
                    size="small"
                    type="info"
                    effect="plain"
                    round
                  >
                    录入工坊：{{ traceMeta.workshopName }}
                  </el-tag>
                  <el-tag v-if="item._materials.length" size="small" effect="plain" round>材料 {{ item._materials.length }}</el-tag>
                  <el-tag v-if="item._images.length" size="small" effect="plain" round>图片 {{ item._images.length }}</el-tag>
                </div>
              </div>

              <div class="step-detail">{{ item.detail || '暂无工序说明' }}</div>

              <div class="step-source-note">
                <strong>材料来源说明：</strong>{{ buildStepSourceNote(item) }}
              </div>

              <div v-if="item._materials.length" class="step-materials">
                <el-tag
                  v-for="material in item._materials"
                  :key="material"
                  class="material-tag"
                  effect="plain"
                  round
                >
                  {{ material }}
                </el-tag>
              </div>

              <div v-if="item._images.length" class="step-gallery">
                <el-image
                  v-for="(img, imgIndex) in item._images"
                  :key="img"
                  :src="img"
                  :preview-src-list="item._images"
                  :initial-index="imgIndex"
                  preview-teleported
                  fit="cover"
                  class="step-image"
                >
                  <template #error>
                    <div class="img-error-box">图片加载失败</div>
                  </template>
                </el-image>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import { craftDisplayDescription } from '../utils/craftPresentation'

const route = useRoute()
const router = useRouter()

const FILE_BASE_URL = String(import.meta.env.VITE_FILE_BASE_URL || '')
  .trim()
  .replace(/\/+$/, '')

const queryForm = reactive({ code: '' })
const craftInfo = ref(null)
const steps = ref([])
const systemVerifyResult = ref(null)
const queried = ref(false)
const loading = ref(false)

function parseArray(value) {
  if (!value) return []
  if (Array.isArray(value)) return value.map((item) => String(item).trim()).filter(Boolean)

  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed.map((item) => String(item).trim()).filter(Boolean) : []
  } catch {
    return String(value)
      .split(/\r?\n|,|，/)
      .map((item) => item.trim())
      .filter(Boolean)
  }
}

function buildTraceMeta(craft) {
  return {
    artisanName: craft?.artisanName || '',
    artisanTitle: craft?.artisanTitle || '',
    workshopName: craft?.workshopName || '',
    displayLocation: craft?.displayLocation || '',
    qrPlacement: craft?.qrPlacement || '',
    materialSourceSummary: craft?.materialSourceSummary || '',
    materialSources: parseArray(craft?.materialSources),
    traceNotice: craft?.traceNotice || ''
  }
}

const traceMeta = computed(() => buildTraceMeta(craftInfo.value || {}))

const craftCover = computed(() =>
  normalizeImageUrl(
    craftInfo.value?.imageUrl ||
      craftInfo.value?.cover ||
      craftInfo.value?.img ||
      '/images/zhuangzucraft_bag.png'
  )
)

const sortedSteps = computed(() =>
  [...steps.value].sort((a, b) => Number(a.stepNo || 0) - Number(b.stepNo || 0))
)

const materialSourceList = computed(() => {
  if (traceMeta.value.materialSources.length) return traceMeta.value.materialSources
  const materials = new Set(sortedSteps.value.flatMap((item) => item._materials))
  return Array.from(materials)
})

const defaultMaterialSummary = computed(() => {
  if (traceMeta.value.materialSourceSummary) return traceMeta.value.materialSourceSummary
  if (!materialSourceList.value.length) {
    return '当前作品已登记工序记录，建议继续补充主要原料的来源说明，使展示档案更加完整。'
  }
  return `当前作品已登记关键材料：${materialSourceList.value.join('、')}，可继续补充其采购、备料或工坊来源信息。`
})

const traceMetrics = computed(() => {
  const stepList = sortedSteps.value
  const totalSteps = stepList.length
  const completedSteps = stepList.filter((item) => item.status === 'COMPLETED').length
  const evidenceSteps = stepList.filter(
    (item) => String(item.detail || '').trim() && (item._materials.length || item._images.length)
  ).length
  const imageCount = stepList.reduce((sum, item) => sum + item._images.length, 0)

  return {
    totalSteps,
    completedSteps,
    imageCount,
    completenessRate: totalSteps ? Math.round((evidenceSteps / totalSteps) * 100) : 0
  }
})

function normalizeImageUrl(url) {
  if (!url) return ''
  const raw = String(url).trim()
  if (!raw) return ''
  if (/^https?:\/\//i.test(raw)) return raw

  const finalPath = raw.startsWith('/') ? raw : `/${raw}`
  if (finalPath.startsWith('/uploads/') && FILE_BASE_URL) {
    return `${FILE_BASE_URL}${finalPath}`
  }
  return finalPath
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').substring(0, 19) : ''
}

function padStepNo(value) {
  return String(value || 0).padStart(2, '0')
}

function buildStepSourceNote(step) {
  if (traceMeta.value.materialSources.length) {
    return traceMeta.value.materialSources.join('；')
  }
  if (step?._materials?.length) {
    return `该步骤主要使用 ${step._materials.join('、')}，来源由负责匠人在工坊备料或课程材料整理环节统一登记。`
  }
  return defaultMaterialSummary.value
}

async function syncRouteCode(code) {
  const trimmedCode = String(code || '').trim()
  const currentCode = String(route.query.code || '').trim()
  if (trimmedCode === currentCode) return

  const nextQuery = { ...route.query }
  if (trimmedCode) nextQuery.code = trimmedCode
  else delete nextQuery.code

  await router.replace({ query: nextQuery })
}

function clearResult() {
  craftInfo.value = null
  steps.value = []
  systemVerifyResult.value = null
}

function applyResult(res) {
  craftInfo.value = res?.craft || null
  systemVerifyResult.value = res?.verify || null
  steps.value = (res?.steps || []).map((step) => ({
    ...step,
    _images: parseArray(step.images).map(normalizeImageUrl),
    _materials: parseArray(step.materials)
  }))
  queried.value = true
}

async function doQuery({ silent = false } = {}) {
  const code = String(queryForm.code || '').trim()
  if (!code) {
    clearResult()
    queried.value = false
    if (!silent) ElMessage.warning('请输入作品编号')
    await syncRouteCode('')
    return
  }

  loading.value = true
  clearResult()
  queried.value = false

  try {
    const res = await request.get('/public/trace', { params: { code } })
    applyResult(res)
    await syncRouteCode(code)
    if (!silent) ElMessage.success('作品档案获取成功')
  } catch {
    queried.value = true
    await syncRouteCode(code)
  } finally {
    loading.value = false
  }
}

function getPublicTraceLink() {
  const code = craftInfo.value?.code || String(queryForm.code || '').trim()
  if (!code || typeof window === 'undefined') return ''

  const resolved = router.resolve({
    name: 'PublicTraceQuery',
    query: { code }
  })
  return new URL(resolved.href, window.location.origin).href
}

function openPublicTrace() {
  const link = getPublicTraceLink()
  if (!link) {
    ElMessage.warning('当前没有可打开的公开展示链接')
    return
  }
  window.open(link, '_blank', 'noopener,noreferrer')
}

function openImageSearch() {
  window.open('/image-search', '_blank', 'noopener,noreferrer')
}

function fallbackCopyText(text) {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.setAttribute('readonly', 'readonly')
  textarea.style.position = 'fixed'
  textarea.style.top = '-9999px'
  textarea.style.left = '-9999px'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.focus()
  textarea.select()
  textarea.setSelectionRange(0, textarea.value.length)

  const copied = document.execCommand('copy')
  document.body.removeChild(textarea)
  return copied
}

async function copyPublicTraceLink() {
  const link = getPublicTraceLink()
  if (!link) {
    ElMessage.warning('当前没有可复制的公开展示链接')
    return
  }

  let copied = false

  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(link)
      copied = true
    }
  } catch {
    copied = false
  }

  if (!copied) {
    copied = fallbackCopyText(link)
  }

  if (copied) {
    ElMessage.success('公开展示链接已复制')
    return
  }

  ElMessageBox.alert(link, '公开展示链接', {
    confirmButtonText: '知道了'
  })
  ElMessage.warning('当前浏览器限制了自动复制，已为你展示公开链接')
}

watch(
  () => route.query.code,
  (routeCode) => {
    const nextCode = String(routeCode || '').trim()
    if (nextCode && nextCode !== queryForm.code) {
      queryForm.code = nextCode
      doQuery({ silent: true })
    }

    if (!nextCode && queryForm.code) {
      queryForm.code = ''
    }
  }
)

onMounted(() => {
  const initialCode = String(route.query.code || '').trim()
  if (!initialCode) return
  queryForm.code = initialCode
  doQuery({ silent: true })
})
</script>

<style scoped>
.trace-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 32px 24px 48px;
  min-height: 100vh;
  background: #f8fafc;
}

.hero {
  margin-bottom: 24px;
  text-align: center;
}

.hero-title {
  font-size: 30px;
  font-weight: 900;
  color: #0f172a;
}

.hero-desc {
  max-width: 760px;
  margin: 12px auto 0;
  font-size: 14px;
  line-height: 1.8;
  color: #64748b;
}

.search-panel {
  max-width: 780px;
  margin: 24px auto 0;
  display: flex;
  gap: 14px;
  padding: 12px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.08);
}

.hero-actions {
  margin-top: 14px;
  display: flex;
  justify-content: center;
}

.grand-input {
  flex: 1;
}

.query-btn {
  height: 48px;
  padding: 0 28px;
  border-radius: 12px;
}

.empty-wrap {
  margin-top: 40px;
}

.result-wrap {
  display: grid;
  gap: 18px;
}

.verify-alert {
  border-radius: 16px;
}

.verify-text {
  line-height: 1.8;
}

.archive-card,
.timeline-card {
  border-radius: 18px;
  border: none;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);
}

.archive-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.archive-head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.archive-title {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
}

.archive-sub {
  margin-top: 8px;
  font-size: 13px;
  color: #64748b;
}

.archive-layout {
  margin-top: 18px;
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 20px;
}

.craft-cover-image {
  width: 100%;
  height: 240px;
  border-radius: 18px;
  overflow: hidden;
  background: #f1f5f9;
}

.img-error-box {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 13px;
  background: #f8fafc;
}

.archive-info {
  display: grid;
  gap: 18px;
}

.archive-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.archive-item label,
.section-title,
.source-title {
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
}

.archive-item p {
  margin: 8px 0 0;
  font-size: 15px;
  line-height: 1.8;
  color: #334155;
}

.archive-descriptions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.desc-row {
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.desc-row span {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}

.desc-row strong {
  font-size: 14px;
  color: #0f172a;
  line-height: 1.7;
}

.archive-metrics {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.metric-box {
  padding: 14px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.metric-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 700;
}

.metric-value {
  margin-top: 8px;
  font-size: 28px;
  line-height: 1;
  font-weight: 900;
  color: #0f172a;
}

.metric-desc {
  margin-top: 8px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.7;
}

.source-board {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.source-card {
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f8fbff 0%, #eef6ff 100%);
  border: 1px solid #dbeafe;
}

.source-text {
  margin-top: 10px;
  font-size: 14px;
  color: #334155;
  line-height: 1.9;
}

.source-tags {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.source-tag {
  border-color: #bfdbfe;
  color: #2563eb;
}

.muted {
  color: #94a3b8;
  font-size: 13px;
}

.section-title {
  margin-bottom: 14px;
}

.timeline {
  padding-left: 8px;
}

.step-card {
  border-radius: 16px;
  border-left: 6px solid #2563eb;
}

.step-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.step-no {
  font-size: 12px;
  color: #2563eb;
  font-weight: 700;
}

.step-name {
  margin-top: 4px;
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.step-tag-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.step-detail {
  margin-top: 14px;
  padding: 14px;
  border-radius: 14px;
  background: #f8fafc;
  font-size: 14px;
  line-height: 1.8;
  color: #334155;
}

.step-source-note {
  margin-top: 12px;
  font-size: 13px;
  line-height: 1.8;
  color: #475569;
}

.step-materials {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.material-tag {
  border-color: #bbf7d0;
  color: #047857;
}

.step-gallery {
  margin-top: 14px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.step-image {
  width: 124px;
  height: 124px;
  border-radius: 14px;
  overflow: hidden;
  background: #f1f5f9;
}

@media (max-width: 900px) {
  .archive-layout,
  .archive-grid,
  .archive-descriptions,
  .archive-metrics,
  .source-board {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .trace-page {
    padding: 24px 16px 36px;
  }

  .search-panel {
    flex-direction: column;
  }

  .step-tag-row {
    justify-content: flex-start;
  }
}
</style>
