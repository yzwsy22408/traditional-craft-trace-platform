<template>
  <div class="trace-public-wrapper">
    <div class="bg-layer">
      <div class="mesh-gradient-blue"></div>
      <div class="texture-overlay"></div>
    </div>

    <div class="content-body">
      <section class="hero-section">
        <h1 class="hero-title">传统手工艺作品公开溯源</h1>
        <p class="hero-subtitle">扫码或输入作品编号后，可查看作品信息、材料说明和工序记录。</p>

        <div class="search-panel glass-blue-card">
          <el-input
            v-model="queryForm.code"
            placeholder="请输入作品编号，例如 CI001"
            size="large"
            @keyup.enter="doQuery"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" :loading="loading" @click="doQuery">开始溯源</el-button>
            </template>
          </el-input>
        </div>

        <div class="hero-actions">
          <el-button round @click="openImageSearch">图片检索</el-button>
        </div>
      </section>

      <template v-if="queried && craftInfo">
        <section class="metric-grid">
          <div class="metric-card glass-blue-card">
            <div class="metric-label">工序总数</div>
            <div class="metric-value">{{ traceMetrics.totalSteps }}</div>
            <div class="metric-sub">已记录 {{ traceMetrics.totalSteps }} 个工序</div>
          </div>
          <div class="metric-card glass-blue-card">
            <div class="metric-label">已完成工序</div>
            <div class="metric-value">{{ traceMetrics.completedSteps }}</div>
            <div class="metric-sub">状态为已完成的工序数量</div>
          </div>
          <div class="metric-card glass-blue-card">
            <div class="metric-label">图片记录</div>
            <div class="metric-value">{{ traceMetrics.imageCount }}</div>
            <div class="metric-sub">已上传的过程图片数量</div>
          </div>
          <div class="metric-card glass-blue-card">
            <div class="metric-label">最近记录时间</div>
            <div class="metric-value metric-value-time">{{ traceMetrics.lastOperateTime || '暂无' }}</div>
            <div class="metric-sub">最后一次工序记录时间</div>
          </div>
        </section>

        <el-card class="glass-blue-card" shadow="never">
          <template #header>
            <div class="card-header-inner">
              <span class="dot-blue"></span>
              <span>作品基础档案</span>
            </div>
          </template>

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

            <div>
              <el-descriptions :column="2" class="art-descriptions">
                <el-descriptions-item label="作品名称">{{ craftInfo.name }}</el-descriptions-item>
                <el-descriptions-item label="作品编号"><span class="val-mono-blue">{{ craftInfo.code }}</span></el-descriptions-item>
                <el-descriptions-item label="所属类别">{{ craftInfo.category || '未分类' }}</el-descriptions-item>
                <el-descriptions-item label="录入匠人">{{ traceMeta.artisanName || '未设置' }}</el-descriptions-item>
                <el-descriptions-item label="匠人角色">{{ traceMeta.artisanTitle || '项目负责匠人' }}</el-descriptions-item>
                <el-descriptions-item label="录入工坊">{{ traceMeta.workshopName || '未设置' }}</el-descriptions-item>
                <el-descriptions-item label="建档时间">{{ formatTime(craftInfo.createdAt) || '暂无记录' }}</el-descriptions-item>
                <el-descriptions-item label="最近工序时间">{{ traceMetrics.lastOperateTime || '暂无记录' }}</el-descriptions-item>
              </el-descriptions>

              <div class="archive-desc">
                <div class="archive-desc-label">作品说明</div>
                <p>{{ craftDisplayDescription(craftInfo) || '暂无作品描述' }}</p>
              </div>
            </div>
          </div>

          <div class="info-grid">
            <div class="info-card">
              <div class="info-title">展示信息</div>
              <div class="info-text">
                <div><strong>展示位置：</strong>{{ traceMeta.displayLocation || '未填写' }}</div>
                <div><strong>溯源码位置：</strong>{{ traceMeta.qrPlacement || '未填写' }}</div>
                <div v-if="traceMeta.traceNotice"><strong>备注：</strong>{{ traceMeta.traceNotice }}</div>
              </div>
            </div>
            <div class="info-card">
              <div class="info-title">材料信息</div>
              <div class="info-text">{{ defaultMaterialSummary }}</div>
              <div class="source-tags">
                <el-tag v-for="item in materialSourceList" :key="item" effect="plain" round class="source-tag">{{ item }}</el-tag>
              </div>
            </div>
          </div>
        </el-card>

        <section class="timeline-container">
          <div class="timeline-header-blue">
            <el-icon><Operation /></el-icon>
            <span>工序记录</span>
          </div>

          <el-timeline>
            <el-timeline-item
              v-for="item in sortedSteps"
              :key="item.id || item.stepNo"
              :timestamp="formatTime(item.operateTime)"
              placement="top"
              size="large"
            >
              <div class="step-glass-blue-card">
                <div class="s-header">
                  <div class="s-title">
                    <span class="s-index-blue">{{ padStepNo(item.stepNo) }}</span>
                    <div>
                      <h4>{{ item.stepName }}</h4>
                      <div class="step-tag-row">
                        <el-tag size="small" type="success" effect="light" round>
                          执行匠人：{{ item.operatorName || traceMeta.artisanName || '负责匠人' }}
                        </el-tag>
                        <el-tag v-if="item.workshopName || traceMeta.workshopName" size="small" type="info" effect="plain" round>
                          录入工坊：{{ item.workshopName || traceMeta.workshopName }}
                        </el-tag>
                        <el-tag v-if="item._materials.length" size="small" effect="plain" round>材料 {{ item._materials.length }}</el-tag>
                        <el-tag v-if="item._images.length" size="small" effect="plain" round>图片 {{ item._images.length }}</el-tag>
                      </div>
                    </div>
                  </div>
                </div>

                <p class="s-detail-blue">{{ item.detail || '该工序暂未补充详细说明。' }}</p>

                <div class="step-source-note">
                  <strong>材料说明：</strong>{{ buildStepSourceNote(item) }}
                </div>

                <div v-if="item._materials.length" class="s-materials">
                  <el-tag v-for="material in item._materials" :key="material" class="m-tag-green" effect="plain" round>
                    {{ material }}
                  </el-tag>
                </div>

                <div v-if="item._images.length" class="s-gallery">
                  <el-image
                    v-for="(img, idx) in item._images"
                    :key="img"
                    :src="img"
                    :preview-src-list="item._images"
                    :initial-index="idx"
                    preview-teleported
                    fit="cover"
                    class="s-img-blue"
                  >
                    <template #error>
                      <div class="img-error-box">图片加载失败</div>
                    </template>
                  </el-image>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </section>
      </template>

      <el-empty
        v-if="queried && !craftInfo"
        description="未查询到对应作品档案，请核对展柜溯源码或联系现场老师。"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Operation, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
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
    return '平台已记录作品制作步骤，建议由管理员补充材料来源说明，以便完整说明原料准备过程。'
  }
  return `该作品制作过程中已登记关键材料：${materialSourceList.value.join('、')}，可结合工坊档案继续查看其采购、准备与使用方式。`
})

const traceMetrics = computed(() => {
  const stepList = sortedSteps.value
  const totalSteps = stepList.length
  const completedSteps = stepList.filter((item) => item.status === 'COMPLETED').length
  const evidenceSteps = stepList.filter(
    (item) => item.detail && (item._materials.length || item._images.length)
  ).length
  const imageCount = stepList.reduce((sum, item) => sum + item._images.length, 0)
  const times = stepList
    .map((item) => item.operateTime)
    .filter(Boolean)
    .map((item) => String(item))
    .sort()

  return {
    totalSteps,
    completedSteps,
    imageCount,
    completenessRate: totalSteps ? Math.round((evidenceSteps / totalSteps) * 100) : 0,
    firstOperateTime: times.length ? formatTime(times[0]) : '',
    lastOperateTime: times.length ? formatTime(times[times.length - 1]) : ''
  }
})

function formatTime(value) {
  return value ? String(value).replace('T', ' ').substring(0, 19) : ''
}

function padStepNo(value) {
  return String(value || 0).padStart(2, '0')
}

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

function buildStepSourceNote(step) {
  if (traceMeta.value.materialSources.length) {
    return traceMeta.value.materialSources.join('；')
  }
  if (step?._materials?.length) {
    return `该步骤主要使用 ${step._materials.join('、')}，来源由负责匠人在工坊备料环节统一登记。`
  }
  return defaultMaterialSummary.value
}

function openImageSearch() {
  window.open('/image-search', '_blank', 'noopener,noreferrer')
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
    if (!silent) ElMessage.success('溯源数据获取成功')
  } catch {
    queried.value = true
    await syncRouteCode(code)
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query.code,
  (routeCode) => {
    const nextCode = String(routeCode || '').trim()
    if (nextCode && nextCode !== queryForm.code) {
      queryForm.code = nextCode
      doQuery({ silent: true })
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
.trace-public-wrapper {
  min-height: 100vh;
  position: relative;
  background:
    radial-gradient(circle at top left, rgba(215, 166, 99, 0.18), transparent 24%),
    linear-gradient(180deg, #f7f3ed 0%, #eff7ff 26%, #edf7ff 100%);
  overflow-x: hidden;
}

.mesh-gradient-blue {
  position: fixed;
  inset: 0;
  background:
    radial-gradient(at 0% 0%, rgba(219, 234, 254, 0.55) 0px, transparent 50%),
    radial-gradient(at 100% 0%, rgba(247, 216, 167, 0.28) 0px, transparent 44%),
    radial-gradient(at 50% 100%, rgba(236, 254, 255, 0.38) 0px, transparent 50%);
  z-index: 0;
  filter: blur(40px);
}

.texture-overlay {
  position: fixed;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%233b82f6' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  z-index: 1;
  pointer-events: none;
  mix-blend-mode: multiply;
}

.content-body {
  position: relative;
  z-index: 10;
  max-width: 1080px;
  margin: 0 auto;
  padding: 88px 20px 60px;
}

.hero-section {
  text-align: center;
  margin-bottom: 42px;
}

.hero-title {
  font-size: 46px;
  font-weight: 900;
  color: #10233a;
  margin-bottom: 12px;
  letter-spacing: 0.4px;
}

.hero-subtitle {
  font-size: 17px;
  color: #475569;
  margin-bottom: 28px;
}

.glass-blue-card {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(191, 219, 254, 0.45);
  box-shadow: 0 24px 48px -16px rgba(30, 64, 175, 0.1);
}

.search-panel {
  max-width: 720px;
  margin: 0 auto;
  padding: 14px;
}

.hero-actions {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.metric-card {
  padding: 24px;
}

.highlight-metric-card {
  background: linear-gradient(180deg, rgba(219, 234, 254, 0.72) 0%, rgba(255, 255, 255, 0.9) 100%);
}

.metric-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 700;
}

.metric-value {
  margin-top: 10px;
  font-size: 30px;
  color: #0f172a;
  font-weight: 900;
}

.metric-value-time {
  font-size: 18px;
  line-height: 1.5;
  word-break: break-word;
}

.metric-sub {
  margin-top: 12px;
  color: #475569;
  font-size: 13px;
}

.card-header-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 700;
  color: #1e293b;
}

.dot-blue {
  width: 10px;
  height: 10px;
  background: #3b82f6;
  border-radius: 50%;
}

.archive-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 26px;
  align-items: start;
}

.craft-cover-image {
  width: 100%;
  height: 260px;
  border-radius: 20px;
  border: 3px solid #fff;
  overflow: hidden;
  box-shadow: 0 20px 32px rgba(15, 23, 42, 0.12);
}

.val-mono-blue {
  font-family: 'JetBrains Mono', monospace;
  color: #2563eb;
  background: rgba(37, 99, 235, 0.06);
  padding: 4px 10px;
  border-radius: 6px;
}

.archive-desc {
  margin-top: 18px;
  padding: 18px 20px;
  border-radius: 18px;
  background: linear-gradient(to right, rgba(240, 249, 255, 0.74), rgba(255, 255, 255, 0.24));
}

.archive-desc-label,
.info-title {
  font-size: 13px;
  color: #64748b;
  font-weight: 800;
  margin-bottom: 10px;
}

.archive-desc p,
.info-text {
  margin: 0;
  color: #334155;
  line-height: 1.8;
}

.info-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.info-card {
  border-radius: 18px;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(191, 219, 254, 0.24);
}

.source-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.source-tag {
  border-color: rgba(59, 130, 246, 0.2) !important;
  color: #1d4ed8 !important;
  background: rgba(219, 234, 254, 0.35) !important;
}

.timeline-container {
  padding: 14px 8px;
}

.timeline-header-blue {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 30px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.step-glass-blue-card {
  background: rgba(255, 255, 255, 0.72);
  border-radius: 22px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.56);
  box-shadow: 0 18px 30px -12px rgba(30, 64, 175, 0.08);
}

.s-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
  margin-bottom: 16px;
}

.s-title {
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.s-index-blue {
  font-family: Georgia, serif;
  font-style: italic;
  color: rgba(59, 130, 246, 0.3);
  font-size: 28px;
  font-weight: 800;
  min-width: 34px;
}

.s-title h4 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: #1e293b;
}

.step-tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.s-detail-blue {
  color: #475569;
  line-height: 1.8;
  font-size: 15px;
  background: linear-gradient(to right, rgba(240, 249, 255, 0.5), rgba(255, 255, 255, 0.2));
  padding: 18px;
  border-radius: 14px;
  margin-bottom: 16px;
}

.s-materials {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.m-tag-green {
  border-color: rgba(167, 243, 208, 0.6) !important;
  color: #059669 !important;
  background: rgba(236, 253, 245, 0.7) !important;
}

.s-gallery {
  display: flex;
  gap: 12px;
  margin-top: 18px;
  flex-wrap: wrap;
}

.s-img-blue {
  width: 130px;
  height: 130px;
  border-radius: 16px;
  border: 3px solid #fff;
  box-shadow: 0 12px 24px -6px rgba(30, 64, 175, 0.14);
}

.img-error-box {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  color: #94a3b8;
  font-size: 11px;
}
:deep(.el-card) {
  border: none;
}
:deep(.el-card__header) {
  padding: 22px 24px 8px;
  border-bottom: none;
}
:deep(.el-card__body) {
  padding: 12px 24px 24px;
}
:deep(.el-input__wrapper) {
  min-height: 52px;
  border-radius: 16px;
  box-shadow: none;
}

@media (max-width: 960px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .archive-layout,
  .info-grid {
    grid-template-columns: 1fr;
  }

  .craft-cover-image {
    height: 220px;
  }
}

@media (max-width: 768px) {
  .content-body {
    padding: 72px 14px 40px;
  }

  .hero-title {
    font-size: 30px;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }

  .s-header,
  .s-title {
    flex-direction: column;
  }
}
</style>
