<template>
  <div class="page">
    <el-card class="card" shadow="never">
      <div class="header">
        <div class="header-left">
          <div class="title-row">
            <h2 class="title">工艺品管理</h2>
          </div>
          <div class="sub">
            统一维护工艺品档案、负责匠人、展柜溯源码说明和公开溯源展示信息。
          </div>
        </div>

        <div class="header-right">
          <el-checkbox v-if="isHandicraft" v-model="onlyMine" @change="handleMineToggle">只看我的工艺品</el-checkbox>
          <el-button @click="loadAll" :loading="loading">刷新</el-button>
          <el-button type="primary" @click="openCreate">新增工艺品</el-button>
        </div>
      </div>

      <div class="summary-grid">
        <div v-for="item in summaryCards" :key="item.label" class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-desc">{{ item.desc }}</div>
        </div>
      </div>

      <div class="scene-panel">
        <div class="scene-title">使用场景说明</div>
        <div class="scene-desc">
          工艺品展示柜、展台说明牌或作品标签上会张贴溯源码，现场扫码后即可查看该作品由哪位匠人制作、使用了哪些材料、材料来自哪里，以及完整工序实录。
        </div>
      </div>

      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            placeholder="搜索：编号 / 名称 / 类别 / 匠人"
            clearable
            class="w-260"
            @keyup.enter="onSearch"
          />

          <el-select
            v-model="categoryFilter"
            clearable
            filterable
            placeholder="类别筛选"
            class="w-180"
            @change="onSearch"
          >
            <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
          </el-select>

          <el-button @click="resetFilter" plain>清空筛选</el-button>

          <div class="muted">当前展示 <b>{{ filteredList.length }}</b> 条<span v-if="isHandicraft">，全部档案 {{ mergedCraftList.length }} 条</span></div>
        </div>

        <div class="toolbar-right">
          <el-pagination
            v-if="filteredList.length > 0"
            v-model:current-page="currentPage"
            small
            background
            layout="prev, pager, next, jumper"
            :total="filteredList.length"
            :page-size="pageSize"
          />
        </div>
      </div>

      <el-table
        :data="pagedList"
        v-loading="loading"
        border
        stripe
        row-key="id"
        style="margin-top: 12px"
      >
        <el-table-column label="主图" width="120" align="center">
          <template #default="{ row }">
            <el-image
              class="cover"
              :src="getCover(row)"
              fit="cover"
              :preview-src-list="[getCover(row)]"
              preview-teleported
            >
              <template #error>
                <div class="cover-error">无图</div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column label="编号" width="140">
          <template #default="{ row }">
            <el-tag effect="plain" type="info" class="mono">{{ row.code || '-' }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="名称" min-width="180">
          <template #default="{ row }">
            <div class="name-cell">
              <div class="name-main">
                {{ row.name || '-' }}
                <el-tag
                  v-if="isOwnedCraft(row)"
                  size="small"
                  type="success"
                  effect="light"
                  class="owner-tag"
                >
                  我的工艺品
                </el-tag>
              </div>
              <div class="name-sub muted">ID：{{ row.id ?? '-' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类别" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.category" type="success" effect="light">{{ row.category }}</el-tag>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>

        <el-table-column label="录入匠人 / 负责工坊" min-width="240">
          <template #default="{ row }">
            <div class="name-cell">
              <div class="name-main">{{ resolveRecorderName(row) }}</div>
              <div class="name-sub">{{ row.traceMeta.artisanTitle || '项目负责匠人' }}</div>
              <div class="name-sub muted">负责工坊：{{ row.traceMeta.workshopName || '未设置所属工坊' }}</div>
              <div class="name-sub muted">录入时间：{{ formatDateTime(row.createdAt) || '暂无记录' }}</div>
              <div v-if="isOwnedCraft(row)" class="name-sub owner-tip">当前登录匠人可直接维护该作品档案</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="溯源要点" min-width="340">
          <template #default="{ row }">
            <div class="trace-block">
              <div class="trace-line"><span class="trace-label">录入位置：</span>{{ row.traceMeta.workshopName || row.traceMeta.displayLocation || '未设置录入工坊或展示区域' }}</div>
              <div class="trace-line"><span class="trace-label">展示位置：</span>{{ row.traceMeta.displayLocation || '未设置展柜或展台位置' }}</div>
              <div class="trace-line"><span class="trace-label">材料来源：</span>{{ row.traceMeta.materialSourceSummary || '未设置主要材料来源说明' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="描述" min-width="260">
          <template #default="{ row }">
            <el-tooltip v-if="craftDisplayDescription(row)" :content="craftDisplayDescription(row)" placement="top" :show-after="300">
              <div class="ellipsis">{{ craftDisplayDescription(row) }}</div>
            </el-tooltip>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <div class="op">
              <el-button
                size="small"
                type="primary"
                plain
                @click="openEdit(row)"
                :disabled="!canOperateCraft(row)"
              >
                编辑
              </el-button>

              <el-popconfirm
                title="确定删除该工艺品吗？删除后不可恢复。"
                confirm-button-text="删除"
                cancel-button-text="取消"
                confirm-button-type="danger"
                @confirm="remove(row)"
              >
                <template #reference>
                  <el-button size="small" type="danger" plain :disabled="!canOperateCraft(row)">删除</el-button>
                </template>
              </el-popconfirm>

              <el-button size="small" type="success" plain @click="copyPublicLink(row)">复制公开链接</el-button>
              <el-button size="small" plain @click="openInternalTrace(row)">内部核验</el-button>
              <el-button size="small" type="primary" @click="openQr(row)">溯源二维码</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && craftList.length === 0" description="暂无工艺品，请先新增" style="margin-top: 26px" />
      <el-empty
        v-else-if="!loading && filteredList.length === 0"
        :description="onlyMine && isHandicraft ? '当前还没有归属到你的工艺品档案' : '没有符合当前筛选条件的工艺品'"
        style="margin-top: 26px"
      />

      <el-dialog v-model="dialogVisible" :title="dialogTitle" width="940px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="102px" class="form">
          <el-row :gutter="18">
            <el-col :xs="24" :md="11">
              <div class="form-section-title">基本信息</div>

              <el-form-item label="编号" prop="code">
                <el-input v-model="form.code" placeholder="例如：CI001" />
              </el-form-item>

              <el-form-item label="名称" prop="name">
                <el-input v-model="form.name" placeholder="例如：壮锦手工包" />
              </el-form-item>

              <el-form-item label="类别" prop="category">
                <el-input v-model="form.category" placeholder="例如：织锦 / 木雕 / 陶艺" />
              </el-form-item>

              <el-form-item label="描述" prop="description">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="4"
                  placeholder="一句话介绍这个工艺品，公开页会直接展示。"
                />
              </el-form-item>

              <el-form-item label="主图 URL">
                <el-input v-model="form.imageUrl" placeholder="/images/zhuangzucraft_bag.png 或 https://..." />
                <div class="btn-row">
                  <el-button size="small" @click="useLocalDemo">使用推荐封面</el-button>
                  <el-button size="small" plain @click="usePresetDemo">切换封面素材</el-button>
                  <el-button size="small" plain @click="clearImage">清空主图</el-button>
                </div>
              </el-form-item>
            </el-col>

            <el-col :xs="24" :md="13">
              <div class="form-section-title">公开溯源信息</div>

              <el-form-item label="负责匠人">
                <el-select
                  v-model="form.artisanId"
                  filterable
                  clearable
                  placeholder="选择负责制作或记录的匠人"
                  style="width: 100%"
                  @change="handleArtisanChange"
                >
                  <el-option
                    v-for="item in artisanOptions"
                    :key="item.id"
                    :label="item.name || item.username"
                    :value="String(item.id)"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="匠人头衔">
                <el-input v-model="form.artisanTitle" placeholder="例如：壮锦项目负责匠人 / 非遗传承匠人" />
              </el-form-item>

              <el-form-item label="所属工坊">
                <el-input v-model="form.workshopName" placeholder="例如：匠心非遗研学工坊（壮锦）" />
              </el-form-item>

              <el-form-item label="展柜位置">
                <el-input v-model="form.displayLocation" placeholder="例如：非遗展示区 1 号展柜 / 工坊入口成品展台" />
              </el-form-item>

              <el-form-item label="溯源码位置">
                <el-input v-model="form.qrPlacement" placeholder="例如：张贴在展柜铭牌与作品说明卡上，现场扫码查看溯源" />
              </el-form-item>

              <el-form-item label="材料来源摘要">
                <el-input
                  v-model="form.materialSourceSummary"
                  type="textarea"
                  :rows="3"
                  placeholder="概述该作品主要材料的来源、采购或准备方式。"
                />
              </el-form-item>

              <el-form-item label="材料来源明细">
                <el-input
                  v-model="form.materialSourcesText"
                  type="textarea"
                  :rows="4"
                  placeholder="每行一条，例如：主料丝线来自广西本地壮锦织造材料库"
                />
              </el-form-item>

              <el-form-item label="查询说明">
                <el-input
                  v-model="form.traceNotice"
                  type="textarea"
                  :rows="3"
                  placeholder="可填写扫码说明、参观提示或公开介绍文案。"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="preview-panel">
            <div class="preview-panel-title">公开页展示预览要点</div>
            <div class="preview-grid">
              <div class="preview-card">
                <div class="preview-label">负责匠人</div>
                <div class="preview-text">{{ form.artisanName || '保存后公开页将展示负责匠人信息' }}</div>
              </div>
              <div class="preview-card">
                <div class="preview-label">展柜扫码说明</div>
                <div class="preview-text">{{ form.qrPlacement || '建议写明二维码张贴在展柜铭牌、作品说明卡或展台侧边' }}</div>
              </div>
              <div class="preview-card">
                <div class="preview-label">材料来源摘要</div>
                <div class="preview-text">{{ form.materialSourceSummary || '保存后公开页会展示主要材料来源说明' }}</div>
              </div>
            </div>
          </div>
        </el-form>

        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="qrDialogVisible" title="成品溯源二维码" width="680px">
        <div v-if="currentCraft" class="qr-wrap">
          <div class="qr-title">{{ currentCraft.name }}（{{ currentCraft.code }}）</div>

          <div class="qr-grid">
            <div class="qr-left">
              <QrcodeVue ref="qrCanvasRef" :value="publicTraceUrl" :size="220" level="M" render-as="canvas" />
              <div class="muted" style="margin-top: 8px">手机扫码进入公开溯源页面</div>
            </div>

            <div class="qr-right">
              <div class="qr-label">公开溯源链接</div>
              <el-input :model-value="publicTraceUrl" readonly />

              <el-alert
                :title="qrAccessStatus.title"
                :type="qrAccessStatus.type"
                :closable="false"
                class="qr-alert"
              >
                <template #default>
                  {{ qrAccessStatus.message }}
                </template>
              </el-alert>

              <div class="qr-btns">
                <el-button type="primary" @click="copyLink">复制链接</el-button>
                <el-button type="success" @click="downloadQr">下载二维码</el-button>
                <el-button @click="openPublic">打开链接</el-button>
              </div>

              <div class="meta-tip-box">
                <div class="meta-tip-title">摆放建议</div>
                <div class="meta-tip-line">展柜位置：{{ currentCraft.traceMeta.displayLocation || '建议设置在成品展示柜或作品展示台' }}</div>
                <div class="meta-tip-line">扫码位置：{{ currentCraft.traceMeta.qrPlacement || '建议张贴在展柜铭牌、作品说明卡或展台侧边' }}</div>
                <div class="meta-tip-line">查询内容：包含负责匠人、所属工坊、材料来源和全流程工序记录。</div>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <el-button @click="qrDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { computed, defineAsyncComponent, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { pageUsers } from '../api/auth'
import { listCraftApi, createCraftApi, updateCraftApi, deleteCraftApi } from '../api/craft'
import { workshopApi } from '../api/workshop'
import { getUser, getUserRole } from '../utils/auth'
import { craftDisplayDescription } from '../utils/craftPresentation'

const QrcodeVue = defineAsyncComponent(() => import('qrcode.vue'))
const router = useRouter()

const loading = ref(false)
const craftList = ref([])
const artisanList = ref([])
const workshopList = ref([])
const keyword = ref('')
const categoryFilter = ref('')
const currentPage = ref(1)
const pageSize = 8
const onlyMine = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('新增工艺品')
const isEdit = ref(false)
const formRef = ref(null)

const qrDialogVisible = ref(false)
const currentCraft = ref(null)
const qrCanvasRef = ref(null)

const form = reactive({
  id: null,
  code: '',
  name: '',
  category: '',
  description: '',
  imageUrl: '',
  artisanId: '',
  artisanName: '',
  artisanTitle: '',
  workshopName: '',
  displayLocation: '',
  qrPlacement: '',
  materialSourceSummary: '',
  materialSourcesText: '',
  traceNotice: ''
})

const rules = {
  code: [{ required: true, message: '请输入编号（例如 CI001）', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  category: [{ required: true, message: '请输入类别', trigger: 'blur' }],
  description: [{ required: true, message: '请输入公开描述', trigger: 'blur' }]
}

function isLocalPreviewHost(hostname) {
  const normalized = String(hostname || '').trim().toLowerCase()
  return normalized === 'localhost' || normalized === '127.0.0.1' || normalized === '::1'
}

const envPublicOrigin = computed(() => String(import.meta.env.VITE_PUBLIC_ORIGIN || '').trim())

const publicOrigin = computed(() => {
  if (typeof window !== 'undefined' && window.location?.origin) {
    if (isLocalPreviewHost(window.location.hostname) && envPublicOrigin.value) {
      return envPublicOrigin.value
    }
    return window.location.origin
  }
  return envPublicOrigin.value
})

function getQrAccessStatusByUrl(url) {
  if (!url) {
    return {
      isReady: false,
      type: 'warning',
      title: '请先选择需要生成二维码的工艺品',
      message: '生成二维码后，手机扫码会跳转到该工艺品的公开溯源页面。'
    }
  }

  try {
    const parsed = new URL(url)
    if (isLocalPreviewHost(parsed.hostname)) {
      return {
        isReady: false,
        type: 'warning',
        title: '当前链接还是本机地址，手机扫码后无法直接访问',
        message: '答辩时请用电脑的局域网地址打开后台，例如 http://192.168.2.215:5173；或者在 .env.development 中设置 VITE_PUBLIC_ORIGIN 为局域网地址。'
      }
    }

    return {
      isReady: true,
      type: 'success',
      title: '当前二维码可用于手机扫码公开溯源',
      message: `请让手机和电脑连接同一 Wi-Fi，微信扫码后会打开 ${parsed.origin}/trace 并展示该工艺品的公开溯源流程。`
    }
  } catch {
    return {
      isReady: false,
      type: 'warning',
      title: '公开链接格式异常',
      message: '请检查当前公开溯源链接是否完整，再重新生成二维码。'
    }
  }
}

const qrAccessStatus = computed(() => getQrAccessStatusByUrl(publicTraceUrl.value))

const currentUser = computed(() => getUser() || null)
const isHandicraft = computed(() => getUserRole() === 'handicraft')
const isAdmin = computed(() => getUserRole() === 'admin')
const currentUserKeywords = computed(() =>
  [currentUser.value?.name, currentUser.value?.username]
    .map((item) => normalizeText(item))
    .filter(Boolean)
)
const ownedWorkshopNames = computed(() => {
  const uid = normalizeText(currentUser.value?.id)
  if (!uid) return []
  return workshopList.value
    .filter((item) => normalizeText(item?.ownerUserId) === uid)
    .map((item) => normalizeText(item?.name))
    .filter(Boolean)
})

function normalizeText(value) {
  return String(value ?? '').trim().toLowerCase()
}

function parseTextLines(value) {
  if (!value) return []
  if (Array.isArray(value)) return value.map((item) => String(item).trim()).filter(Boolean)
  return String(value)
    .split(/\r?\n|,|，/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function buildTraceMeta(item) {
  return {
    artisanId: item?.artisanId ? String(item.artisanId) : '',
    artisanName: item?.artisanName || '',
    artisanTitle: item?.artisanTitle || '',
    workshopName: item?.workshopName || '',
    displayLocation: item?.displayLocation || '',
    qrPlacement: item?.qrPlacement || '',
    materialSourceSummary: item?.materialSourceSummary || '',
    materialSources: parseTextLines(item?.materialSources),
    traceNotice: item?.traceNotice || ''
  }
}

const mergedCraftList = computed(() =>
  craftList.value.map((item) => ({
    ...item,
    traceMeta: buildTraceMeta(item)
  }))
)

const artisanOptions = computed(() =>
  [...artisanList.value].sort((a, b) =>
    String(a.name || a.username || '').localeCompare(String(b.name || b.username || ''), 'zh-Hans-CN')
  )
)

const categories = computed(() => {
  const set = new Set()
  mergedCraftList.value.forEach((item) => {
    if (item?.category) set.add(String(item.category))
  })
  return Array.from(set).sort((a, b) => a.localeCompare(b, 'zh-Hans-CN'))
})

const summaryCards = computed(() => {
  const all = mergedCraftList.value
  const withImage = all.filter((item) => item.imageUrl || item.cover || item.img).length
  const categoryCount = categories.value.length
  const withArtisan = all.filter((item) => item.traceMeta?.artisanName).length
  const withDisplay = all.filter((item) => item.traceMeta?.displayLocation).length
  const withMaterial = all.filter((item) => item.traceMeta?.materialSourceSummary).length
  const ownCount = all.filter((item) => isOwnedCraft(item)).length

  return [
    { label: '工艺品总数', value: all.length, desc: '当前系统维护的作品档案数量' },
    { label: '工艺类别', value: categoryCount, desc: '已覆盖的传统手工艺方向数量' },
    { label: '我的作品', value: ownCount, desc: '与当前登录匠人一一对应的工艺品数量' },
    { label: '已配主图', value: withImage, desc: '已配置主图的工艺品数量' },
    { label: '已绑匠人', value: withArtisan, desc: '公开页可展示负责匠人的工艺品数量' },
    { label: '已写展柜', value: withDisplay, desc: '已配置展示柜或展台扫码位置说明' },
    { label: '已写来源', value: withMaterial, desc: '已补充材料来源说明的工艺品数量' }
  ]
})

const filteredList = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  const category = String(categoryFilter.value || '').trim()

  let list = mergedCraftList.value
  if (category) {
    list = list.filter((item) => String(item.category || '') === category)
  }
  if (onlyMine.value && isHandicraft.value) {
    list = list.filter((item) => isOwnedCraft(item))
  }
  if (kw) {
    list = list.filter((item) => {
      const searchable = [
        item.code,
        item.name,
        item.category,
        item.traceMeta?.artisanName,
        item.traceMeta?.workshopName,
        item.traceMeta?.displayLocation
      ]
        .join(' ')
        .toLowerCase()
      return searchable.includes(kw)
    })
  }

  if (isHandicraft.value) {
    list = [...list].sort((a, b) => Number(isOwnedCraft(b)) - Number(isOwnedCraft(a)))
  }

  return list
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredList.value.slice(start, start + pageSize)
})

const publicTraceUrl = computed(() => {
  const code = currentCraft.value?.code
  if (!code) return ''
  return `${publicOrigin.value}/trace?code=${encodeURIComponent(code)}`
})

function normalizeUserList(res) {
  if (res?.success && Array.isArray(res.list)) return res.list
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res)) return res
  return []
}

function normalizeWorkshopList(res) {
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res)) return res
  return []
}

function workshopByOwnerUserId(userId) {
  if (!userId) return null
  return workshopList.value.find((item) => String(item.ownerUserId) === String(userId)) || null
}

function getArtisanInfoById(id) {
  if (!id) return null
  return artisanOptions.value.find((item) => String(item.id) === String(id)) || null
}

function isOwnedCraft(row) {
  const uid = normalizeText(currentUser.value?.id)
  if (!uid || !row) return false

  if (normalizeText(row?.artisanId) === uid || normalizeText(row?.createdBy) === uid) {
    return true
  }

  const artisanName = normalizeText(row?.traceMeta?.artisanName || row?.artisanName)
  if (artisanName && currentUserKeywords.value.includes(artisanName)) {
    return true
  }

  const workshopName = normalizeText(row?.traceMeta?.workshopName || row?.workshopName)
  if (workshopName && ownedWorkshopNames.value.includes(workshopName)) {
    return true
  }

  return false
}

function canOperateCraft(row) {
  if (isAdmin.value) return true
  if (!isHandicraft.value) return false
  return isOwnedCraft(row)
}

function resolveRecorderName(row) {
  if (row?.traceMeta?.artisanName) return row.traceMeta.artisanName
  if (isOwnedCraft(row)) return currentUser.value?.name || currentUser.value?.username || '当前登录匠人'
  return '未设置录入匠人'
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').substring(0, 19) : ''
}

function getCover(row) {
  return row.cover || row.imageUrl || row.img || '/images/zhuangzucraft_bag.png'
}

function onSearch() {
  currentPage.value = 1
}

function handleMineToggle() {
  currentPage.value = 1
  if (onlyMine.value && isHandicraft.value && !mergedCraftList.value.some((item) => isOwnedCraft(item))) {
    ElMessage.info('当前没有匹配到归属你的工艺品档案，已按录入人、负责匠人和所属工坊做过识别。')
  }
}

function resetFilter() {
  keyword.value = ''
  categoryFilter.value = ''
  currentPage.value = 1
}

function resetForm() {
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    category: '',
    description: '',
    imageUrl: '/images/zhuangzucraft_bag.png',
    artisanId: '',
    artisanName: '',
    artisanTitle: '',
    workshopName: '',
    displayLocation: '',
    qrPlacement: '',
    materialSourceSummary: '',
    materialSourcesText: '',
    traceNotice: ''
  })
}

function getDefaultQrPlacement() {
  return '二维码张贴在展示柜铭牌、作品说明卡和成品展示台侧边，观众扫码即可查看完整溯源流程。'
}

function buildPayload() {
  return {
    code: String(form.code || '').trim(),
    name: String(form.name || '').trim(),
    category: String(form.category || '').trim(),
    description: String(form.description || '').trim(),
    imageUrl: String(form.imageUrl || '').trim(),
    artisanId: form.artisanId ? Number(form.artisanId) : null,
    artisanName: String(form.artisanName || '').trim(),
    artisanTitle: String(form.artisanTitle || '').trim(),
    workshopName: String(form.workshopName || '').trim(),
    displayLocation: String(form.displayLocation || '').trim(),
    qrPlacement: String(form.qrPlacement || '').trim(),
    materialSourceSummary: String(form.materialSourceSummary || '').trim(),
    materialSources: parseTextLines(form.materialSourcesText).join('\n'),
    traceNotice: String(form.traceNotice || '').trim()
  }
}

function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新增工艺品'
  resetForm()
  if (isHandicraft.value) {
    const mine = getArtisanInfoById(currentUser.value?.id)
    if (mine) {
      form.artisanId = String(mine.id)
      handleArtisanChange(form.artisanId)
    }
  }
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑工艺品'
  const meta = buildTraceMeta(row)

  Object.assign(form, {
    id: row.id,
    code: row.code || '',
    name: row.name || '',
    category: row.category || '',
    description: craftDisplayDescription(row) || '',
    imageUrl: row.imageUrl || row.cover || row.img || '',
    artisanId: meta.artisanId || '',
    artisanName: meta.artisanName || '',
    artisanTitle: meta.artisanTitle || '',
    workshopName: meta.workshopName || '',
    displayLocation: meta.displayLocation || '',
    qrPlacement: meta.qrPlacement || '',
    materialSourceSummary: meta.materialSourceSummary || '',
    materialSourcesText: meta.materialSources.join('\n'),
    traceNotice: meta.traceNotice || ''
  })

  dialogVisible.value = true
}

function useLocalDemo() {
  form.imageUrl = '/images/zhuangzucraft_bag.png'
}

function usePresetDemo() {
  const presetImages = [
    '/images/zhuangzucraft_bag.png',
    '/images/广西壮锦技艺初探.png',
    '/images/苏绣双面绣精研班.png',
    '/images/青花瓷手绘体验营.png',
    '/images/紫砂手工壶全流程课程.png'
  ]
  const currentIndex = presetImages.indexOf(form.imageUrl || '')
  const nextIndex = currentIndex >= 0 ? (currentIndex + 1) % presetImages.length : 0
  form.imageUrl = presetImages[nextIndex]
}

function clearImage() {
  form.imageUrl = ''
}

function handleArtisanChange(value) {
  const artisan = getArtisanInfoById(value)
  form.artisanName = artisan?.name || artisan?.username || ''

  if (!form.artisanTitle) {
    form.artisanTitle = '项目负责匠人'
  }

  const workshop = workshopByOwnerUserId(artisan?.id)
  if (workshop && !form.workshopName) {
    form.workshopName = workshop.name || ''
  }

  if (!form.displayLocation) {
    form.displayLocation = `${form.name || '该工艺品'}所在展柜与工坊展示区`
  }

  if (!form.qrPlacement) {
    form.qrPlacement = getDefaultQrPlacement()
  }
}

async function loadAll() {
  loading.value = true
  try {
    const tasks = [listCraftApi(), workshopApi.list()]
    if (isAdmin.value) {
      tasks.splice(1, 0, pageUsers({ page: 1, size: 200, keyword: '' }))
    }
    const [craftRes, maybeUserRes, workshopRes] = await Promise.all(tasks)

    craftList.value = Array.isArray(craftRes) ? craftRes : craftRes?.data || []
    if (isAdmin.value) {
      artisanList.value = normalizeUserList(maybeUserRes).filter(
        (item) => String(item.role || '').toLowerCase() === 'handicraft'
      )
      workshopList.value = normalizeWorkshopList(workshopRes)
    } else {
      artisanList.value = currentUser.value ? [currentUser.value] : []
      workshopList.value = normalizeWorkshopList(maybeUserRes)
    }

    const maxPage = Math.max(1, Math.ceil(filteredList.value.length / pageSize))
    if (currentPage.value > maxPage) currentPage.value = maxPage
  } catch {
    ElMessage.error('工艺品数据加载失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  const ok = await formRef.value?.validate?.().catch(() => false)
  if (!ok) return

  if (form.artisanId && !form.artisanName) {
    handleArtisanChange(form.artisanId)
  }

  if (!String(form.qrPlacement || '').trim()) {
    form.qrPlacement = getDefaultQrPlacement()
  }

  try {
    const payload = buildPayload()
    if (isEdit.value) {
      await updateCraftApi(form.id, payload)
    } else {
      await createCraftApi(payload)
    }

    dialogVisible.value = false
    ElMessage.success(isEdit.value ? '工艺品信息已更新' : '工艺品已新增')
    await loadAll()
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  }
}

async function remove(row) {
  try {
    await deleteCraftApi(row.id)
    ElMessage.success('工艺品已删除')
    await loadAll()
  } catch {
    ElMessage.error('删除失败，请稍后重试')
  }
}

async function copyPublicLink(row) {
  const url = `${publicOrigin.value}/trace?code=${encodeURIComponent(row.code || '')}`
  try {
    await navigator.clipboard.writeText(url)
    if (getQrAccessStatusByUrl(url).isReady) ElMessage.success('公开链接已复制，可直接用于手机扫码访问')
    else ElMessage.warning('链接已复制，但当前还是本机地址，建议改用局域网地址后再生成二维码')
  } catch {
    ElMessage.info(url)
  }
}

function openInternalTrace(row) {
  if (!row?.code) {
    ElMessage.warning('该工艺品还没有可用编号，暂时无法进入内部核验页')
    return
  }
  router.push({ path: '/app/query', query: { code: row.code } })
}

function openQr(row) {
  currentCraft.value = {
    ...row,
    traceMeta: buildTraceMeta(row)
  }
  qrDialogVisible.value = true
}

async function copyLink() {
  const url = publicTraceUrl.value
  if (!url) return
  try {
    await navigator.clipboard.writeText(url)
    if (qrAccessStatus.value.isReady) ElMessage.success('链接已复制，可直接用于手机扫码访问')
    else ElMessage.warning('链接已复制，但当前还是本机地址，手机扫码无法直接访问')
  } catch {
    const input = document.createElement('input')
    input.value = url
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    if (qrAccessStatus.value.isReady) ElMessage.success('链接已复制，可直接用于手机扫码访问')
    else ElMessage.warning('链接已复制，但当前还是本机地址，手机扫码无法直接访问')
  }
}

async function downloadQr() {
  await nextTick()
  const canvas = qrCanvasRef.value?.$el?.querySelector('canvas')
  if (!canvas) {
    ElMessage.error('二维码生成失败，请确认二维码组件已正确加载')
    return
  }

  const dataUrl = canvas.toDataURL('image/png')
  const a = document.createElement('a')
  a.href = dataUrl
  a.download = `${currentCraft.value?.code || 'trace'}.png`
  a.click()
  ElMessage.success('二维码已下载')
}

function openPublic() {
  if (!publicTraceUrl.value) return
  window.open(publicTraceUrl.value, '_blank')
}

onMounted(loadAll)
</script>

<style scoped>
.page {
  padding: 16px;
  background: #f6f8fb;
  min-height: calc(100vh - 40px);
}

.card {
  max-width: 1320px;
  margin: 12px auto;
  border-radius: 14px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  margin: 0;
  font-size: 22px;
  font-weight: 900;
}

.tag-pill {
  border-radius: 999px;
}

.sub {
  color: rgba(0, 0, 0, 0.55);
  font-size: 13px;
  line-height: 1.7;
}

.header-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.summary-card {
  border-radius: 16px;
  padding: 16px 18px;
  background: linear-gradient(180deg, #fbfdff 0%, #f4f7fb 100%);
  border: 1px solid #e5edf5;
}

.summary-label {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.5);
}

.summary-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 900;
  color: rgba(0, 0, 0, 0.82);
}

.summary-desc {
  margin-top: 6px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.46);
  line-height: 18px;
}

.scene-panel {
  margin-top: 16px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fbff 100%);
  border: 1px solid #dbeafe;
}

.scene-title {
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
}

.scene-desc {
  margin-top: 8px;
  color: #475569;
  font-size: 13px;
  line-height: 1.8;
}

.toolbar {
  margin-top: 14px;
  padding: 12px;
  border-radius: 12px;
  background: #fbfcfe;
  border: 1px solid #eef2f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.w-260 {
  width: 260px;
}

.w-180 {
  width: 180px;
}

.muted {
  color: rgba(0, 0, 0, 0.48);
  font-size: 12px;
}

.cover {
  width: 78px;
  height: 56px;
  border-radius: 10px;
  border: 1px solid #eef2f6;
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.cover-error {
  width: 78px;
  height: 56px;
  border-radius: 10px;
  border: 1px dashed #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
  background: #fff;
}

.name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name-main {
  font-weight: 800;
  color: rgba(0, 0, 0, 0.82);
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.name-sub {
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
}

.owner-tag {
  border-radius: 999px;
}

.owner-tip {
  color: #16a34a;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.trace-block {
  display: grid;
  gap: 8px;
}

.trace-line {
  font-size: 13px;
  color: #334155;
  line-height: 1.7;
}

.trace-label {
  font-weight: 700;
  color: #0f172a;
}

.ellipsis {
  max-width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  color: rgba(0, 0, 0, 0.72);
}

.op {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.form-section-title {
  font-weight: 900;
  margin-bottom: 10px;
  color: rgba(0, 0, 0, 0.78);
}

.btn-row {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.preview-panel {
  margin-top: 8px;
  border-radius: 16px;
  padding: 16px 18px;
  background: #fbfcfe;
  border: 1px solid #eaf0f6;
}

.preview-panel-title {
  font-size: 14px;
  font-weight: 800;
  color: #0f172a;
}

.preview-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.preview-card {
  border-radius: 14px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #e5edf5;
}

.preview-label {
  font-size: 12px;
  color: #64748b;
}

.preview-text {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.7;
  color: #334155;
}

.qr-wrap {
  padding: 4px 2px;
}

.qr-title {
  font-weight: 900;
  margin-bottom: 10px;
  color: rgba(0, 0, 0, 0.82);
}

.qr-grid {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.qr-left {
  padding: 10px;
  border-radius: 12px;
  border: 1px solid #eef2f6;
  background: #fbfcfe;
}

.qr-right {
  flex: 1;
  min-width: 300px;
}

.qr-label {
  font-weight: 800;
  margin-bottom: 6px;
  color: rgba(0, 0, 0, 0.72);
}

.qr-btns {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.qr-alert {
  margin-top: 12px;
}

.meta-tip-box {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #fbfcfe;
  border: 1px dashed #dbe7f3;
}

.meta-tip-title {
  font-size: 13px;
  font-weight: 800;
  color: #0f172a;
}

.meta-tip-line {
  margin-top: 8px;
  font-size: 12px;
  color: #475569;
  line-height: 1.7;
}

@media (max-width: 960px) {
  .preview-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .card {
    margin: 0;
  }

  .w-260,
  .w-180 {
    width: 100%;
  }
}
</style>
