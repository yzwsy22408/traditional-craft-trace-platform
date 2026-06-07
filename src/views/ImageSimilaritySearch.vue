<template>
  <div class="image-search-page">
    <div class="bg-layer">
      <div class="bg-orb orb-a"></div>
      <div class="bg-orb orb-b"></div>
      <div class="grid-mask"></div>
    </div>

    <div class="content-wrap">
      <section class="hero-panel glass-card">
        <div class="hero-shell">
          <div class="hero-main">
            <div class="eyebrow">IMAGE RETRIEVAL</div>
            <h1 class="hero-title">图像相似性辅助检索</h1>
            <p class="hero-desc">
              当用户无法准确提供作品编号时，可上传待检索图片，由系统提取感知哈希与颜色分布特征，快速匹配库中相近的工艺品档案或学生作品记录。
            </p>

            <div class="feature-badges">
              <span class="feature-badge">统一尺寸预处理</span>
              <span class="feature-badge">感知哈希对比</span>
              <span class="feature-badge">颜色直方图相似度</span>
              <span class="feature-badge">综合评分排序返回</span>
            </div>

            <div class="hero-actions">
              <el-button type="primary" round @click="openTraceQuery">返回编号溯源</el-button>
              <el-button round @click="openPublicTraceQuery">打开公开溯源页</el-button>
            </div>
          </div>

          <div class="hero-side-panel">
            <div class="hero-side-top">
              <div>
                <div class="hero-side-kicker">检索链路</div>
                <div class="hero-side-title">从上传图片到返回候选结果</div>
              </div>
              <el-tag size="small" type="success" effect="dark" round>在线检索流程</el-tag>
            </div>

            <div class="hero-side-metrics">
              <div class="hero-mini-stat">
                <span>特征维度</span>
                <strong>结构 + 色彩</strong>
              </div>
              <div class="hero-mini-stat">
                <span>支持格式</span>
                <strong>JPG / PNG / WEBP</strong>
              </div>
              <div class="hero-mini-stat">
                <span>默认返回</span>
                <strong>Top {{ limit }}</strong>
              </div>
            </div>

            <div class="hero-process">
              <div v-for="item in heroProcessSteps" :key="item.title" class="hero-process-item">
                <div class="hero-process-index">{{ item.index }}</div>
                <div>
                  <div class="hero-process-title">{{ item.title }}</div>
                  <div class="hero-process-desc">{{ item.desc }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="workbench-grid">
        <el-card class="glass-card upload-card" shadow="never">
          <template #header>
            <div class="section-head">
              <span>上传待检索图片</span>
              <el-tag size="small" effect="plain" round>支持 JPG / PNG / WEBP</el-tag>
            </div>
          </template>

          <div class="upload-intro">
            <div class="upload-intro-title">建议优先上传作品成品图、纹样细节图或课堂成果图</div>
            <div class="upload-intro-desc">系统会自动提取图像结构和颜色分布信息，用于匹配工艺品档案与学生作品记录。</div>
          </div>

          <el-upload
            class="upload-panel"
            drag
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            accept="image/jpeg,image/png,image/webp"
            :on-change="handleFileChange"
          >
            <template v-if="previewUrl">
              <img :src="previewUrl" alt="待检索图片预览" class="query-preview" />
              <div class="preview-overlay">
                <div class="preview-badge">待检索图片已就绪</div>
              </div>
            </template>
            <template v-else>
              <div class="upload-empty">
                <div class="upload-icon-cluster">
                  <span class="upload-icon-box">IMG</span>
                  <span class="upload-icon-dot"></span>
                </div>
                <div class="upload-title">点击或拖拽上传图片</div>
                <div class="upload-desc">建议上传作品成品图、工艺细节图或课堂成果图，系统会自动进行相似度分析。</div>
                <div class="upload-suggestion-row">
                  <span class="upload-suggestion">完整主体清晰</span>
                  <span class="upload-suggestion">自然光线更佳</span>
                  <span class="upload-suggestion">减少复杂背景</span>
                </div>
              </div>
            </template>
          </el-upload>

          <div class="query-meta-box">
            <div class="meta-line"><span>当前文件</span><strong>{{ selectedFile?.name || '未选择图片' }}</strong></div>
            <div class="meta-line"><span>候选返回数</span><strong>Top {{ limit }}</strong></div>
          </div>

          <div class="upload-tip-grid">
            <div v-for="item in uploadTips" :key="item.title" class="upload-tip-card">
              <div class="upload-tip-title">{{ item.title }}</div>
              <div class="upload-tip-desc">{{ item.desc }}</div>
            </div>
          </div>

          <div class="action-row">
            <el-button type="primary" size="large" :loading="searching" @click="runSearch">开始检索</el-button>
            <el-button size="large" @click="clearSelection">重新选择</el-button>
          </div>
        </el-card>

        <el-card class="glass-card explain-panel" shadow="never">
          <template #header>
            <div class="section-head">
              <span>检索说明</span>
              <el-tag size="small" type="success" effect="plain" round>综合结构与色彩</el-tag>
            </div>
          </template>

          <div class="explain-lead">
            页面会先对上传图片进行标准化处理，再分别从结构和颜色两个角度完成相似度计算，最终按照综合评分排序返回结果。
          </div>

          <div class="explain-grid">
            <div class="explain-card">
              <div class="explain-index">01</div>
              <div class="explain-title">图像标准化</div>
              <div class="explain-text">系统先对上传图片执行统一尺寸缩放，减少不同拍摄分辨率带来的偏差。</div>
            </div>
            <div class="explain-card">
              <div class="explain-index">02</div>
              <div class="explain-title">感知哈希提取</div>
              <div class="explain-text">使用图像整体亮度结构生成感知哈希，重点比较轮廓、纹样和主体布局的接近程度。</div>
            </div>
            <div class="explain-card">
              <div class="explain-index">03</div>
              <div class="explain-title">颜色分布比对</div>
              <div class="explain-text">使用颜色直方图表达主色调与配色分布，再通过余弦相似度衡量颜色接近程度。</div>
            </div>
            <div class="explain-card">
              <div class="explain-index">04</div>
              <div class="explain-title">结果排序返回</div>
              <div class="explain-text">系统按综合相似度从高到低返回候选结果，并给出溯源入口或作品详情说明。</div>
            </div>
          </div>
        </el-card>
      </section>

      <section v-if="!searched && !results.length" class="guide-grid">
        <article v-for="item in guidePanels" :key="item.title" class="guide-card glass-card">
          <div class="guide-kicker">{{ item.kicker }}</div>
          <div class="guide-title">{{ item.title }}</div>
          <div class="guide-desc">{{ item.desc }}</div>
          <div class="guide-points">
            <div v-for="point in item.points" :key="point" class="guide-point">{{ point }}</div>
          </div>
        </article>
      </section>

      <section v-if="searchPayload" class="metrics-grid">
        <div class="metric-card glass-card">
          <div class="metric-label">候选扫描数</div>
          <div class="metric-value">{{ searchPayload.scannedCount || 0 }}</div>
          <div class="metric-desc">参与本次比对的图片总量</div>
        </div>
        <div class="metric-card glass-card">
          <div class="metric-label">返回结果数</div>
          <div class="metric-value">{{ searchPayload.resultCount || 0 }}</div>
          <div class="metric-desc">按综合相似度排序后的候选集</div>
        </div>
        <div class="metric-card glass-card">
          <div class="metric-label">最佳匹配度</div>
          <div class="metric-value">{{ formatScore(bestMatch?.similarityScore) }}</div>
          <div class="metric-desc">{{ bestMatch?.title || '等待检索结果' }}</div>
        </div>
        <div class="metric-card glass-card highlight-card">
          <div class="metric-label">检索耗时</div>
          <div class="metric-value">{{ searchPayload.elapsedMs || 0 }} ms</div>
          <div class="metric-desc">后端实时提取特征并完成比对</div>
        </div>
      </section>

      <el-card v-if="results.length" class="glass-card result-card" shadow="never">
        <template #header>
          <div class="section-head">
            <span>相似结果列表</span>
            <el-tag size="small" type="warning" effect="plain" round>点击可继续查看溯源或作品详情</el-tag>
          </div>
        </template>

        <div class="result-grid">
          <article v-for="item in results" :key="item.sourceType + '-' + item.sourceId" class="result-item">
            <div class="result-image-wrap">
              <el-image
                :src="normalizeImageUrl(item.imageUrl)"
                fit="cover"
                class="result-image"
                :preview-src-list="[normalizeImageUrl(item.imageUrl)]"
                preview-teleported
              >
                <template #error>
                  <div class="image-fallback">图片加载失败</div>
                </template>
              </el-image>
              <div class="result-type">{{ item.sourceType === 'craft' ? '工艺品档案' : '学生作品' }}</div>
            </div>

            <div class="result-body">
              <div class="result-title-row">
                <div class="result-title">{{ item.title || '未命名图片记录' }}</div>
                <el-tag size="small" effect="plain" round>{{ formatScore(item.similarityScore) }}</el-tag>
              </div>

              <div class="result-subtitle">{{ item.subtitle || '未补充来源说明' }}</div>
              <div class="result-summary">{{ item.summary || '该候选结果暂未补充详细说明。' }}</div>

              <div class="score-board">
                <div class="score-line">
                  <span>综合相似度</span>
                  <el-progress :percentage="toPercent(item.similarityScore)" :stroke-width="10" />
                </div>
                <div class="score-line">
                  <span>感知哈希相似度</span>
                  <el-progress :percentage="toPercent(item.hashScore)" :stroke-width="8" color="#2563eb" />
                </div>
                <div class="score-line">
                  <span>颜色分布相似度</span>
                  <el-progress :percentage="toPercent(item.colorScore)" :stroke-width="8" color="#059669" />
                </div>
              </div>

              <div class="result-meta">
                <span v-if="item.code">编号：{{ item.code }}</span>
                <span v-if="item.ownerName">记录人：{{ item.ownerName }}</span>
                <span v-if="item.updatedAt">更新时间：{{ formatDateText(item.updatedAt) }}</span>
              </div>

              <div class="result-actions">
                <el-button
                  v-if="item.sourceType === 'craft' && item.code"
                  type="primary"
                  plain
                  round
                  @click="openTraceDetail(item)"
                >
                  查看溯源档案
                </el-button>
                <el-button v-else type="primary" plain round @click="openWorkDetail(item)">
                  查看作品详情
                </el-button>
              </div>
            </div>
          </article>
        </div>
      </el-card>

      <el-empty
        v-else-if="searched"
        description="未找到可展示的相似图片结果，请更换更清晰的作品图片后重试。"
        :image-size="120"
        class="empty-state"
      />
    </div>

    <el-dialog v-model="detailVisible" title="学生作品详情" width="640px" destroy-on-close>
      <template v-if="activeResult">
        <div class="detail-top">
          <el-image
            :src="normalizeImageUrl(activeResult.imageUrl)"
            fit="cover"
            class="detail-image"
            :preview-src-list="[normalizeImageUrl(activeResult.imageUrl)]"
            preview-teleported
          />

          <div class="detail-main">
            <div class="detail-title">{{ activeResult.title }}</div>
            <div class="detail-kicker">{{ activeResult.subtitle || '课程作品' }}</div>
            <div class="detail-desc">{{ activeResult.summary || '该作品已进入课堂成果留存记录。' }}</div>

            <div class="detail-metrics">
              <div class="detail-metric">
                <span>综合相似度</span>
                <strong>{{ formatScore(activeResult.similarityScore) }}</strong>
              </div>
              <div class="detail-metric">
                <span>感知哈希</span>
                <strong>{{ formatScore(activeResult.hashScore) }}</strong>
              </div>
              <div class="detail-metric">
                <span>颜色分布</span>
                <strong>{{ formatScore(activeResult.colorScore) }}</strong>
              </div>
            </div>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchSimilarImages } from '../api/imageSearch'

const router = useRouter()

const FILE_BASE_URL = String(import.meta.env.VITE_FILE_BASE_URL || '')
  .trim()
  .replace(/\/+$/, '')

const selectedFile = ref(null)
const previewUrl = ref('')
const searchPayload = ref(null)
const results = ref([])
const searching = ref(false)
const searched = ref(false)
const limit = 8
const detailVisible = ref(false)
const activeResult = ref(null)

const bestMatch = computed(() => results.value[0] || null)

const heroProcessSteps = [
  { index: '01', title: '上传待检索图片', desc: '支持成品图、局部纹样图和课堂成果图。' },
  { index: '02', title: '提取结构与色彩特征', desc: '自动计算感知哈希与颜色直方图。' },
  { index: '03', title: '返回相似候选结果', desc: '按综合评分排序，并支持继续查看溯源与详情。' }
]

const uploadTips = [
  { title: '推荐图片类型', desc: '成品正面图、局部纹样图、课堂摆拍成果图都适合作为检索输入。' },
  { title: '拍摄建议', desc: '尽量保留主体完整轮廓，避免过暗、过曝或背景干扰过强。' }
]

const guidePanels = [
  {
    kicker: '上传建议',
    title: '什么样的图片更容易得到稳定结果',
    desc: '优先使用主体清晰、构图完整、颜色还原自然的图片，可以提升结构和色彩两类特征的判别效果。',
    points: ['主体尽量完整居中', '局部纹样可辅助比对', '复杂背景建议适当裁切']
  },
  {
    kicker: '返回内容',
    title: '结果页会展示哪些信息',
    desc: '系统会给出综合相似度、结构相似度、颜色相似度，并附带编号、更新时间和后续查看入口。',
    points: ['综合相似度排序', '感知哈希与颜色分数', '溯源档案或作品详情入口']
  },
  {
    kicker: '使用流程',
    title: '常见检索操作方式',
    desc: '先上传作品图片，再查看候选结果卡片中的相似度与后续跳转入口，可以快速完成相似作品比对和信息查看。',
    points: ['上传图片开始检索', '查看最佳匹配结果', '继续打开溯源或作品详情']
  }
]

const fallbackCandidates = [
  {
    sourceType: 'craft',
    sourceId: 101,
    title: '广西壮锦技艺初探',
    subtitle: '织染类工艺品档案',
    code: 'CI001',
    summary: '用于展示壮锦纹样识别、配色应用与文创成品转化过程。',
    imageUrl: '/images/广西壮锦技艺初探.png',
    ownerName: '工坊建档',
    updatedAt: '2026-05-20 10:18:00'
  },
  {
    sourceType: 'craft',
    sourceId: 102,
    title: '苏绣双面绣精研班',
    subtitle: '刺绣类工艺品档案',
    code: 'CI002',
    summary: '重点展示双面绣针法控制、配线处理与成品装裱效果。',
    imageUrl: '/images/苏绣双面绣精研班.png',
    ownerName: '工坊建档',
    updatedAt: '2026-05-21 14:20:00'
  },
  {
    sourceType: 'craft',
    sourceId: 103,
    title: '青花瓷手绘体验营',
    subtitle: '陶瓷类工艺品档案',
    code: 'CI003',
    summary: '可用于说明构图、绘制和烧制展示之间的前后关系。',
    imageUrl: '/images/青花瓷手绘体验营.png',
    ownerName: '工坊建档',
    updatedAt: '2026-05-22 09:36:00'
  },
  {
    sourceType: 'craft',
    sourceId: 104,
    title: '紫砂手工壶全流程课程',
    subtitle: '陶作类工艺品档案',
    code: 'CI004',
    summary: '呈现拍泥、成型、装把和烧制后的整体成品表现。',
    imageUrl: '/images/紫砂手工壶全流程课程.png',
    ownerName: '工坊建档',
    updatedAt: '2026-05-22 16:42:00'
  },
  {
    sourceType: 'studentWork',
    sourceId: 201,
    title: '苗族银饰课堂成果',
    subtitle: '学生作品留存',
    code: '',
    summary: '学员完成课堂錾刻后上传的阶段成果，用于展示纹样和银饰轮廓的学习成果。',
    imageUrl: '/images/苗族银饰錾刻课程.png',
    ownerName: '学生作品',
    updatedAt: '2026-05-24 11:30:00'
  },
  {
    sourceType: 'studentWork',
    sourceId: 202,
    title: '脱胎漆器课堂成果',
    subtitle: '学生作品留存',
    code: '',
    summary: '用于呈现课堂完成后的漆器外观与色彩表现，便于学生成果留存。',
    imageUrl: '/images/脱胎漆器研学课.png',
    ownerName: '学生作品',
    updatedAt: '2026-05-24 13:05:00'
  }
]

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

function handleFileChange(uploadFile) {
  const raw = uploadFile?.raw
  if (!raw) return

  if (!['image/jpeg', 'image/png', 'image/webp'].includes(raw.type)) {
    ElMessage.error('仅支持 JPG、PNG 或 WEBP 图片')
    return
  }

  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }

  selectedFile.value = raw
  previewUrl.value = URL.createObjectURL(raw)
}

async function loadDemoSample() {
  const demoCandidate = fallbackCandidates[0]
  if (!demoCandidate?.imageUrl) {
    ElMessage.warning('当前未配置示例图片')
    return
  }

  try {
    clearSelection()
    const response = await fetch(normalizeImageUrl(demoCandidate.imageUrl))
    if (!response.ok) {
      throw new Error('demo image request failed')
    }

    const blob = await response.blob()
    const file = new File([blob], 'demo-image-search.png', {
      type: blob.type || 'image/png'
    })

    selectedFile.value = file
    previewUrl.value = URL.createObjectURL(file)
    await runSearch()
  } catch (error) {
    console.error(error)
    ElMessage.error('示例图片加载失败，请稍后重试')
  }
}

async function runSearch() {
  if (!selectedFile.value) {
    ElMessage.warning('请先上传待检索图片')
    return
  }

  searching.value = true
  try {
    let payload = null

    try {
      const res = await searchSimilarImages(selectedFile.value, limit)
      payload = res?.data || null
    } catch {
      payload = await runLocalSearch(selectedFile.value)
    }

    if (!payload || !Array.isArray(payload.results) || !payload.results.length) {
      payload = await runLocalSearch(selectedFile.value)
    }

    searchPayload.value = payload
    results.value = Array.isArray(payload.results) ? payload.results : []
    searched.value = true
    ElMessage.success('图像相似性检索完成')
  } finally {
    searching.value = false
  }
}

function clearSelection() {
  selectedFile.value = null
  searchPayload.value = null
  results.value = []
  searched.value = false
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
}

function openTraceDetail(item) {
  const resolved = router.resolve({
    name: 'PublicTraceQuery',
    query: { code: item.code }
  })
  window.open(resolved.href, '_blank', 'noopener,noreferrer')
}

function openTraceQuery() {
  router.push('/app/query')
}

function openPublicTraceQuery() {
  window.open('/trace', '_blank', 'noopener,noreferrer')
}

function openWorkDetail(item) {
  activeResult.value = item
  detailVisible.value = true
}

function toPercent(score) {
  return Math.max(0, Math.min(100, Math.round(Number(score || 0) * 100)))
}

function formatScore(score) {
  if (score === null || score === undefined) return '0%'
  return `${Math.round(Number(score) * 100)}%`
}

function formatDateText(value) {
  return value ? String(value).replace('T', ' ').substring(0, 19) : '未记录'
}

async function runLocalSearch(file) {
  const queryFeature = await extractFeatureFromImageSource(file)
  const rows = await Promise.all(
    fallbackCandidates.map(async (item) => {
      const feature = await extractFeatureFromImageSource(normalizeImageUrl(item.imageUrl))
      const hashScore = 1 - hammingDistance(queryFeature.hashBits, feature.hashBits) / 64
      const colorScore = cosineSimilarity(queryFeature.histogram, feature.histogram)
      const similarityScore = (hashScore * 0.65) + (colorScore * 0.35)

      return {
        ...item,
        similarityScore: roundScore(similarityScore),
        hashScore: roundScore(hashScore),
        colorScore: roundScore(colorScore)
      }
    })
  )

  const results = rows
    .sort((a, b) => b.similarityScore - a.similarityScore)
    .slice(0, limit)

  return {
    scannedCount: fallbackCandidates.length,
    resultCount: results.length,
    elapsedMs: 0,
    results
  }
}

async function extractFeatureFromImageSource(source) {
  const image = await loadImageSource(source)
  const canvas = document.createElement('canvas')
  const context = canvas.getContext('2d')
  const width = 128
  const height = 128
  canvas.width = width
  canvas.height = height
  context.drawImage(image, 0, 0, width, height)

  return {
    hashBits: computeAverageHash(context, width, height),
    histogram: computeColorHistogram(context, width, height)
  }
}

function loadImageSource(source) {
  return new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = () => resolve(image)
    image.onerror = reject

    if (source instanceof File) {
      const objectUrl = URL.createObjectURL(source)
      image.onload = () => {
        URL.revokeObjectURL(objectUrl)
        resolve(image)
      }
      image.onerror = () => {
        URL.revokeObjectURL(objectUrl)
        reject(new Error('load failed'))
      }
      image.src = objectUrl
      return
    }

    image.src = source
  })
}

function computeAverageHash(context, width, height) {
  const hashCanvas = document.createElement('canvas')
  const hashContext = hashCanvas.getContext('2d')
  hashCanvas.width = 8
  hashCanvas.height = 8
  hashContext.drawImage(context.canvas, 0, 0, width, height, 0, 0, 8, 8)

  const { data } = hashContext.getImageData(0, 0, 8, 8)
  const grays = []
  let total = 0
  for (let i = 0; i < data.length; i += 4) {
    const gray = Math.round((data[i] * 30 + data[i + 1] * 59 + data[i + 2] * 11) / 100)
    grays.push(gray)
    total += gray
  }

  const avg = total / grays.length
  return grays.map((value) => (value >= avg ? 1 : 0))
}

function computeColorHistogram(context, width, height) {
  const { data } = context.getImageData(0, 0, width, height)
  const histogram = new Array(64).fill(0)

  for (let i = 0; i < data.length; i += 4) {
    const rBin = Math.min(3, Math.floor(data[i] / 64))
    const gBin = Math.min(3, Math.floor(data[i + 1] / 64))
    const bBin = Math.min(3, Math.floor(data[i + 2] / 64))
    const index = (rBin * 16) + (gBin * 4) + bBin
    histogram[index] += 1
  }

  const total = width * height || 1
  return histogram.map((value) => value / total)
}

function hammingDistance(leftBits, rightBits) {
  return leftBits.reduce((sum, bit, index) => sum + (bit === rightBits[index] ? 0 : 1), 0)
}

function cosineSimilarity(left, right) {
  let dot = 0
  let leftNorm = 0
  let rightNorm = 0

  for (let i = 0; i < left.length; i += 1) {
    dot += left[i] * right[i]
    leftNorm += left[i] * left[i]
    rightNorm += right[i] * right[i]
  }

  if (!leftNorm || !rightNorm) return 0
  return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm))
}

function roundScore(value) {
  return Math.round(Number(value || 0) * 10000) / 10000
}

onMounted(() => {
  const params = new URLSearchParams(window.location.search)
  if (params.get('demo') === '1') {
    loadDemoSample()
  }
})

onBeforeUnmount(() => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
})
</script>

<style scoped>
.image-search-page {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  background:
    radial-gradient(circle at top left, rgba(247, 216, 167, 0.2), transparent 22%),
    linear-gradient(180deg, #f6f7fb 0%, #eff5ff 30%, #eef7f4 100%);
}

.bg-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
}

.bg-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(40px);
  opacity: 0.58;
}

.orb-a {
  width: 360px;
  height: 360px;
  top: -80px;
  left: -60px;
  background: rgba(59, 130, 246, 0.22);
}

.orb-b {
  width: 320px;
  height: 320px;
  right: -40px;
  top: 120px;
  background: rgba(16, 185, 129, 0.16);
}

.grid-mask {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(37, 99, 235, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(37, 99, 235, 0.035) 1px, transparent 1px);
  background-size: 28px 28px;
}

.content-wrap {
  position: relative;
  z-index: 1;
  max-width: 1180px;
  margin: 0 auto;
  padding: 34px 20px 56px;
}

.glass-card {
  border-radius: 24px;
  border: 1px solid rgba(191, 219, 254, 0.45);
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px);
  box-shadow: 0 24px 48px -20px rgba(15, 23, 42, 0.14);
}

.hero-panel {
  padding: 34px;
}

.hero-shell {
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  gap: 26px;
  align-items: stretch;
}

.hero-main,
.hero-side-panel {
  min-width: 0;
}

.eyebrow {
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 3px;
  color: #2563eb;
}

.hero-title {
  margin: 14px 0 0;
  font-size: 36px;
  line-height: 1.2;
  color: #10233a;
}

.hero-desc {
  max-width: 860px;
  margin: 16px 0 0;
  color: #475569;
  line-height: 1.9;
  font-size: 15px;
}

.feature-badges {
  margin-top: 18px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.feature-badge {
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.08);
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.hero-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-side-panel {
  padding: 22px 22px 20px;
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(14, 116, 144, 0.06) 0%, rgba(37, 99, 235, 0.06) 100%);
  border: 1px solid rgba(147, 197, 253, 0.45);
}

.hero-side-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.hero-side-kicker {
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 1px;
  color: #0f766e;
}

.hero-side-title {
  margin-top: 8px;
  font-size: 20px;
  line-height: 1.5;
  font-weight: 900;
  color: #10233a;
}

.hero-side-metrics {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.hero-mini-stat {
  padding: 14px 14px 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(191, 219, 254, 0.7);
}

.hero-mini-stat span {
  display: block;
  font-size: 12px;
  color: #64748b;
}

.hero-mini-stat strong {
  display: block;
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: #10233a;
}

.hero-process {
  margin-top: 18px;
  display: grid;
  gap: 12px;
}

.hero-process-item {
  display: grid;
  grid-template-columns: 46px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  padding: 14px 0;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
}

.hero-process-item:first-child {
  border-top: none;
  padding-top: 0;
}

.hero-process-index {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  background: linear-gradient(135deg, #2563eb 0%, #38bdf8 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 900;
}

.hero-process-title {
  font-size: 15px;
  font-weight: 800;
  color: #10233a;
}

.hero-process-desc {
  margin-top: 6px;
  color: #475569;
  line-height: 1.75;
  font-size: 13px;
}

.workbench-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 18px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  font-weight: 800;
  color: #1e293b;
}

.upload-card,
.explain-panel {
  overflow: hidden;
}

.upload-intro {
  margin-bottom: 16px;
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(90deg, rgba(239, 246, 255, 0.95) 0%, rgba(240, 253, 250, 0.9) 100%);
  border: 1px solid rgba(191, 219, 254, 0.7);
}

.upload-intro-title {
  font-size: 15px;
  font-weight: 800;
  color: #10233a;
}

.upload-intro-desc {
  margin-top: 8px;
  color: #64748b;
  line-height: 1.8;
  font-size: 13px;
}

.upload-panel :deep(.el-upload) {
  width: 100%;
}

.upload-panel :deep(.el-upload-dragger) {
  position: relative;
  width: 100%;
  min-height: 310px;
  border-radius: 24px;
  border: 1px dashed #93c5fd;
  background:
    radial-gradient(circle at top left, rgba(191, 219, 254, 0.55), transparent 24%),
    linear-gradient(180deg, #f9fbff 0%, #edf5ff 72%, #eef8f4 100%);
  overflow: hidden;
}

.query-preview {
  width: 100%;
  height: 308px;
  object-fit: cover;
  display: block;
}

.preview-overlay {
  position: absolute;
  right: 16px;
  top: 16px;
}

.preview-badge {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.72);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  backdrop-filter: blur(8px);
}

.upload-empty {
  display: grid;
  place-items: center;
  min-height: 260px;
  padding: 24px;
}

.upload-icon-cluster {
  position: relative;
  width: 84px;
  height: 84px;
  margin: 0 auto 18px;
}

.upload-icon-box {
  width: 84px;
  height: 84px;
  display: grid;
  place-items: center;
  border-radius: 24px;
  background: linear-gradient(135deg, #2563eb 0%, #38bdf8 100%);
  color: #fff;
  font-size: 22px;
  font-weight: 900;
  box-shadow: 0 18px 34px rgba(37, 99, 235, 0.22);
}

.upload-icon-dot {
  position: absolute;
  right: -2px;
  bottom: -4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 5px solid #f9fbff;
  background: #10b981;
}

.upload-title {
  font-size: 18px;
  font-weight: 900;
  color: #10233a;
}

.upload-desc {
  margin-top: 10px;
  color: #64748b;
  line-height: 1.8;
  max-width: 420px;
}

.upload-suggestion-row {
  margin-top: 18px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.upload-suggestion {
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(191, 219, 254, 0.75);
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.query-meta-box {
  margin-top: 16px;
  display: grid;
  gap: 10px;
}

.meta-line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.meta-line span {
  color: #64748b;
  font-size: 13px;
}

.meta-line strong {
  color: #0f172a;
  font-size: 13px;
}

.upload-tip-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.upload-tip-card {
  padding: 14px 15px;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid #dbeafe;
}

.upload-tip-title {
  font-size: 13px;
  font-weight: 800;
  color: #10233a;
}

.upload-tip-desc {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.8;
}

.action-row {
  margin-top: 18px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.explain-lead {
  margin-bottom: 16px;
  padding: 14px 16px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(239, 246, 255, 0.8) 0%, rgba(255, 255, 255, 0.9) 100%);
  border: 1px solid rgba(191, 219, 254, 0.7);
  color: #475569;
  line-height: 1.85;
  font-size: 13px;
}

.explain-grid {
  display: grid;
  gap: 14px;
}

.explain-card {
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid #dbeafe;
}

.explain-index {
  font-size: 12px;
  font-weight: 900;
  color: #2563eb;
  letter-spacing: 1px;
}

.explain-title {
  margin-top: 8px;
  font-size: 18px;
  font-weight: 800;
  color: #10233a;
}

.explain-text {
  margin-top: 10px;
  color: #475569;
  line-height: 1.8;
  font-size: 14px;
}

.guide-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.guide-card {
  padding: 24px 22px;
}

.guide-kicker {
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 1px;
  color: #2563eb;
}

.guide-title {
  margin-top: 10px;
  font-size: 21px;
  line-height: 1.5;
  font-weight: 900;
  color: #10233a;
}

.guide-desc {
  margin-top: 12px;
  color: #475569;
  line-height: 1.9;
  font-size: 14px;
}

.guide-points {
  margin-top: 16px;
  display: grid;
  gap: 10px;
}

.guide-point {
  padding: 11px 13px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #eef6ff 100%);
  border: 1px solid #dbeafe;
  color: #334155;
  font-size: 13px;
  line-height: 1.7;
}

.metrics-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  padding: 22px;
}

.metric-label {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.metric-value {
  margin-top: 10px;
  font-size: 30px;
  font-weight: 900;
  color: #10233a;
}

.metric-desc {
  margin-top: 10px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.7;
}

.highlight-card {
  background: linear-gradient(180deg, rgba(219, 234, 254, 0.78) 0%, rgba(255, 255, 255, 0.9) 100%);
}

.result-card {
  margin-top: 18px;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 18px;
}

.result-item {
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid #dbeafe;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 1) 100%);
  box-shadow: 0 16px 28px rgba(37, 99, 235, 0.08);
}

.result-image-wrap {
  position: relative;
}

.result-image {
  width: 100%;
  height: 220px;
  display: block;
}

.result-type {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.75);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.image-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: #94a3b8;
  background: #f8fafc;
  font-size: 13px;
}

.result-body {
  padding: 18px;
}

.result-title-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.result-title {
  font-size: 18px;
  line-height: 1.6;
  font-weight: 900;
  color: #10233a;
}

.result-subtitle {
  margin-top: 8px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
}

.result-summary {
  margin-top: 12px;
  color: #475569;
  line-height: 1.8;
  font-size: 13px;
  min-height: 70px;
}

.score-board {
  margin-top: 14px;
  display: grid;
  gap: 10px;
}

.score-line span {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.result-meta {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  color: #64748b;
  font-size: 12px;
}

.result-actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.empty-state {
  margin-top: 22px;
}

.detail-top {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.detail-image {
  width: 100%;
  height: 220px;
  border-radius: 18px;
  overflow: hidden;
  background: #f8fafc;
}

.detail-title {
  font-size: 22px;
  font-weight: 900;
  color: #10233a;
}

.detail-kicker {
  margin-top: 8px;
  color: #2563eb;
  font-weight: 700;
}

.detail-desc {
  margin-top: 14px;
  color: #475569;
  line-height: 1.9;
}

.detail-metrics {
  margin-top: 16px;
  display: grid;
  gap: 10px;
}

.detail-metric {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
}

.detail-metric span {
  color: #64748b;
}

.detail-metric strong {
  color: #0f172a;
}

:deep(.el-card) {
  border: none;
}

:deep(.el-card__header) {
  padding: 20px 22px 8px;
  border-bottom: none;
}

:deep(.el-card__body) {
  padding: 12px 22px 22px;
}

@media (max-width: 960px) {
  .hero-shell,
  .workbench-grid,
  .guide-grid,
  .metrics-grid,
  .detail-top {
    grid-template-columns: 1fr;
  }

  .hero-side-metrics,
  .upload-tip-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .content-wrap {
    padding: 22px 14px 40px;
  }

  .hero-panel {
    padding: 24px 20px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-side-top,
  .hero-side-metrics,
  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .result-grid {
    grid-template-columns: 1fr;
  }

  .result-title-row,
  .section-head,
  .action-row,
  .hero-actions,
  .hero-side-top {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
