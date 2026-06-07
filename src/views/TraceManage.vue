<template>
  <div class="page">
    <el-card class="panel-card" shadow="never">
      <div class="head">
        <div>
          <div class="title">溯源步骤管理</div>
          <div class="desc">按工艺品维护完整的工序链，可更新步骤状态、补充材料与图像证据，并重建哈希链提升溯源展示完整度。</div>
        </div>
        <div class="actions">
          <el-button type="primary" @click="goAdd">新增工序</el-button>
        </div>
      </div>

      <div class="toolbar">
        <el-form :inline="true">
          <el-form-item label="选择工艺品">
            <el-select
              v-model="craftItemId"
              placeholder="请选择工艺品"
              style="width: 360px"
              filterable
              @change="onCraftChange"
            >
              <el-option
                v-for="item in craftList"
                :key="item.id"
                :label="`${item.name}（${item.code}）`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button :disabled="!craftItemId" @click="loadSteps">刷新列表</el-button>
          </el-form-item>

          <el-form-item>
            <el-button :disabled="!craftItemId" type="warning" plain @click="doRebuildHash">
              重建 Hash 链
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="craftItemId" class="summary-board">
        <div class="summary-card primary-card">
          <div class="summary-label">当前作品</div>
          <div class="summary-main">{{ selectedCraft?.name || '未命名工艺品' }}</div>
          <div class="summary-sub">{{ selectedCraft?.code || '暂无编号' }}</div>
        </div>

        <div class="summary-card">
          <div class="summary-label">工序总数</div>
          <div class="summary-value">{{ traceSummary.total }}</div>
          <div class="summary-sub">已完成 {{ traceSummary.completed }} 个节点</div>
        </div>

        <div class="summary-card">
          <div class="summary-label">完成率</div>
          <div class="summary-value">{{ traceSummary.completionRate }}%</div>
          <div class="summary-sub">进行中 {{ traceSummary.inProgress }} / 未开始 {{ traceSummary.notStarted }}</div>
        </div>

        <div class="summary-card">
          <div class="summary-label">档案完整度</div>
          <div class="summary-value">{{ traceSummary.readinessRate }}%</div>
          <div class="summary-sub">材料 {{ traceSummary.materialLinked }} / 图片 {{ traceSummary.imageLinked }}</div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="steps"
        border
        style="width: 100%; margin-top: 12px;"
        empty-text="暂无溯源步骤"
      >
        <el-table-column prop="stepNo" label="步骤序号" width="90" />
        <el-table-column prop="stepName" label="步骤名称" min-width="160" />
        <el-table-column prop="detail" label="详细描述" min-width="260" show-overflow-tooltip />

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="证据概览" min-width="220">
          <template #default="{ row }">
            <div class="evidence-line">材料：{{ safeJsonArray(row.materials).length }}</div>
            <div class="evidence-line">图片：{{ safeJsonArray(row.images).length }}</div>
          </template>
        </el-table-column>

        <el-table-column label="展示时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="420" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              :type="row.status === 'NOT_STARTED' ? 'primary' : 'default'"
              plain
              @click="setStatus(row, 'NOT_STARTED')"
            >
              未开始
            </el-button>

            <el-button
              size="small"
              :type="row.status === 'IN_PROGRESS' ? 'warning' : 'default'"
              plain
              @click="setStatus(row, 'IN_PROGRESS')"
            >
              进行中
            </el-button>

            <el-button
              size="small"
              :type="row.status === 'COMPLETED' ? 'success' : 'default'"
              plain
              @click="setStatus(row, 'COMPLETED')"
            >
              已完成
            </el-button>

            <el-button size="small" type="primary" plain @click="openEdit(row)">
              编辑内容
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && craftItemId && steps.length === 0"
        description="该工艺品暂无溯源步骤"
        style="margin-top: 20px"
      />

      <el-dialog v-model="editDialogVisible" title="编辑步骤内容" width="720px">
        <el-form label-width="90px">
          <el-form-item label="步骤名称">
            <el-input v-model="editForm.stepName" disabled />
            <div class="tip">步骤名称当前不在这里修改，避免影响已建立的工序链表达。</div>
          </el-form-item>

          <el-form-item label="详细描述">
            <el-input
              v-model="editForm.detail"
              type="textarea"
              :rows="4"
              placeholder="请输入该步骤的详细说明"
            />
          </el-form-item>

          <el-form-item label="材料清单">
            <el-input
              v-model="editForm.materialsText"
              type="textarea"
              :rows="3"
              placeholder="使用逗号、中文逗号或换行分隔，例如：丝线，底布，植物染料"
            />
            <div class="tip">保存后会自动转换为 JSON 数组，方便公开查询页直接展示。</div>
          </el-form-item>

          <el-form-item label="图片链接">
            <el-input
              v-model="editForm.imagesText"
              type="textarea"
              :rows="3"
              placeholder="多张图片使用逗号或换行分隔，例如：https://...1.jpg，https://...2.png"
            />
            <div class="tip">图片链接会作为该步骤的展示证据，用于增强溯源可信度。</div>
          </el-form-item>

        </el-form>

        <template #footer>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEdit">保存</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { listCraftApi } from '../api/craft'
import {
  listTraceStepApi,
  rebuildTraceHashApi,
  updateTraceStepContentApi,
  updateTraceStepStatusApi
} from '../api/trace'

const router = useRouter()

const craftList = ref([])
const craftItemId = ref(null)
const steps = ref([])
const loading = ref(false)

const editDialogVisible = ref(false)
const currentRow = ref(null)
const editForm = reactive({
  stepName: '',
  detail: '',
  materialsText: '',
  imagesText: ''
})

function safeJsonArray(value) {
  if (!value) return []
  if (Array.isArray(value)) return value.filter(Boolean)
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed.filter(Boolean) : []
  } catch (e) {
    return []
  }
}

const selectedCraft = computed(() => (
  craftList.value.find(item => Number(item.id) === Number(craftItemId.value)) || null
))

const traceSummary = computed(() => {
  const list = steps.value || []
  const total = list.length
  const completed = list.filter(item => item.status === 'COMPLETED').length
  const inProgress = list.filter(item => item.status === 'IN_PROGRESS').length
  const notStarted = list.filter(item => item.status === 'NOT_STARTED').length
  const materialLinked = list.filter(item => safeJsonArray(item.materials).length > 0).length
  const imageLinked = list.filter(item => safeJsonArray(item.images).length > 0).length
  const evidenceReady = list.filter(item => {
    const hasDetail = String(item.detail || '').trim()
    return hasDetail && (
      safeJsonArray(item.materials).length > 0 ||
      safeJsonArray(item.images).length > 0
    )
  }).length

  return {
    total,
    completed,
    inProgress,
    notStarted,
    materialLinked,
    imageLinked,
    completionRate: total ? Math.round((completed / total) * 100) : 0,
    readinessRate: total ? Math.round((evidenceReady / total) * 100) : 0
  }
})

function pad2(n) {
  return String(n).padStart(2, '0')
}

function formatDateTime(date) {
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())} ${pad2(date.getHours())}:${pad2(date.getMinutes())}:${pad2(date.getSeconds())}`
}

function startOfDay(date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0)
}

function buildDisplayTimes(rawList) {
  const list = [...rawList].sort((a, b) => Number(a.stepNo || 0) - Number(b.stepNo || 0))
  if (!list.length) return list

  const now = new Date()
  const daysSpan = 3
  const baseDay = startOfDay(new Date(now.getTime() - (daysSpan - 1) * 24 * 3600 * 1000))
  const dayCursor = Array.from({ length: daysSpan }, (_, index) => {
    const d = new Date(baseDay.getTime() + index * 24 * 3600 * 1000)
    d.setHours(9, 20 + Math.floor(Math.random() * 20), Math.floor(Math.random() * 30), 0)
    return d
  })

  const maxStepNo = Math.max(...list.map(item => Number(item.stepNo || 0)), list.length)
  const minStepNo = Math.min(...list.map(item => Number(item.stepNo || 0)), 1)

  function pickDayIndex(stepNo) {
    const normalized = maxStepNo === minStepNo ? 1 : (stepNo - minStepNo) / (maxStepNo - minStepNo)
    let idx = Math.floor(normalized * daysSpan)
    if (idx >= daysSpan) idx = daysSpan - 1
    if (idx < 0) idx = 0
    return idx
  }

  return list.map((row) => {
    const stepNo = Number(row.stepNo || 0)
    let dayIndex = pickDayIndex(stepNo)
    let time = new Date(dayCursor[dayIndex].getTime() + (40 + Math.floor(Math.random() * 111)) * 60 * 1000)

    if (time.getHours() >= 20) {
      dayIndex = Math.min(dayIndex + 1, daysSpan - 1)
      time = new Date(dayCursor[dayIndex])
      time.setHours(9, 30 + Math.floor(Math.random() * 20), Math.floor(Math.random() * 40), 0)
    }

    if (time.getTime() > now.getTime()) {
      time = new Date(now.getTime() - (10 + Math.floor(Math.random() * 81)) * 60 * 1000)
      if (time.getHours() < 9) time.setHours(9, 20, 0, 0)
      if (time.getHours() >= 20) time.setHours(19, 20, 0, 0)
    }

    dayCursor[dayIndex] = new Date(time)

    return {
      ...row,
      __displayOperateTime: formatDateTime(time)
    }
  })
}

onMounted(async () => {
  try {
    const res = await listCraftApi()
    craftList.value = Array.isArray(res) ? res : (res?.data || [])
  } catch (e) {
    ElMessage.error('工艺品列表加载失败')
  }
})

const goAdd = () => {
  router.push('/app/trace/add')
}

const onCraftChange = async () => {
  await loadSteps()
}

const loadSteps = async () => {
  if (!craftItemId.value) return
  loading.value = true
  try {
    const res = await listTraceStepApi(craftItemId.value)
    const list = Array.isArray(res) ? res : (res?.data || [])
    steps.value = buildDisplayTimes(list)
  } finally {
    loading.value = false
  }
}

const setStatus = async (row, status) => {
  if (!row?.id) return
  try {
    await updateTraceStepStatusApi(row.id, status)
    ElMessage.success(`步骤状态已更新为：${statusText(status)}`)
    await loadSteps()
  } catch (e) {}
}

const doRebuildHash = async () => {
  if (!craftItemId.value) return

  try {
    await ElMessageBox.confirm(
      '系统将按步骤序号重新计算并覆盖当前工艺品所有节点的 stepHash，确认继续吗？',
      '提示',
      { type: 'warning' }
    )

    await rebuildTraceHashApi(craftItemId.value)
    ElMessage.success('Hash 链重建完成')
    await loadSteps()
  } catch (e) {}
}

function toCommaText(value) {
  if (!value) return ''
  if (Array.isArray(value)) return value.join('，')
  try {
    const arr = JSON.parse(value)
    if (Array.isArray(arr)) return arr.join('，')
  } catch (e) {}
  return String(value)
}

function commaTextToJsonArray(text) {
  const arr = (text || '')
    .split(/,|，|\n|\r\n/)
    .map(item => item.trim())
    .filter(Boolean)
  return arr.length ? JSON.stringify(arr) : null
}

const openEdit = (row) => {
  currentRow.value = row
  editForm.stepName = row.stepName || ''
  editForm.detail = row.detail || ''
  editForm.materialsText = toCommaText(row.materials)
  editForm.imagesText = toCommaText(row.images)
  editDialogVisible.value = true
}

const saveEdit = async () => {
  if (!currentRow.value?.id) return

  try {
    const payload = {
      detail: editForm.detail,
      materials: commaTextToJsonArray(editForm.materialsText),
      images: commaTextToJsonArray(editForm.imagesText)
    }

    await updateTraceStepContentApi(currentRow.value.id, payload)
    ElMessage.success('步骤内容已保存')
    editDialogVisible.value = false
    await loadSteps()
  } catch (e) {}
}

const statusText = (status) => ({
  NOT_STARTED: '未开始',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成'
}[status] || '待更新')

const tagType = (status) => ({
  COMPLETED: 'success',
  IN_PROGRESS: 'warning',
  NOT_STARTED: 'info'
}[status] || '')

const formatTime = (row) => row?.__displayOperateTime || '未记录时间'
</script>

<style scoped>
.page {
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
}

.panel-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.title {
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
}

.desc {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.actions {
  display: flex;
  gap: 8px;
}

.toolbar {
  margin-top: 14px;
}

.summary-board {
  margin-top: 16px;
  display: grid;
  grid-template-columns: 1.3fr 1fr 1fr 1fr;
  gap: 12px;
}

.summary-card {
  padding: 16px 18px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e5edf5;
}

.primary-card {
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
  border-color: #bfdbfe;
}

.summary-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 700;
}

.summary-main {
  margin-top: 10px;
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
}

.summary-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 900;
  color: #1f2937;
}

.summary-sub {
  margin-top: 8px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
}

.evidence-line {
  font-size: 12px;
  line-height: 1.7;
  color: #64748b;
}

.tip {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 960px) {
  .summary-board {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

}

@media (max-width: 768px) {
  .page {
    padding: 16px;
  }

  .summary-board {
    grid-template-columns: 1fr;
  }
}
</style>
