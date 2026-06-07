<template>
  <div class="page">
    <el-card class="panel-card" shadow="never">
      <div class="head">
        <div>
          <div class="title">材料管理</div>
          <div class="desc">集中维护材料基础档案、库存和供应来源，同时标记匠人授课与工艺品制作中会用到的关键材料，方便按课程和作品快速查看。</div>
        </div>

        <div class="actions">
          <el-input v-model="keyword" clearable placeholder="搜索：名称 / 分类 / 供应商 / 关联课程" style="width:260px;" />
          <el-checkbox v-if="isHandicraft" v-model="onlyMineMaterials">只看我常用的材料</el-checkbox>
          <el-button type="primary" @click="openCreate">新增材料</el-button>
          <el-button @click="load">刷新</el-button>
        </div>
      </div>

      <div class="summary-grid">
        <div v-for="item in summaryCards" :key="item.label" class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-desc">{{ item.desc }}</div>
        </div>
      </div>

      <el-table :data="filteredRows" v-loading="loading" empty-text="暂无材料">
        <el-table-column type="index" width="60" label="#" />

        <el-table-column label="图片" width="110">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              class="material-image"
              fit="cover"
              :preview-src-list="[row.imageUrl]"
              preview-teleported
            />
            <div v-else class="image-placeholder">无图</div>
          </template>
        </el-table-column>

        <el-table-column label="材料档案" min-width="260">
          <template #default="{ row }">
            <div class="material-name-row">
              <div class="material-name">{{ row.name || '未命名材料' }}</div>
              <el-tag v-if="isMineMaterial(row)" type="success" effect="light" round size="small">我的常用材料</el-tag>
            </div>
            <div class="sub">分类：{{ row.category || '未分类' }}</div>
            <div class="relation-tags" v-if="relationMeta(row).courseHits.length || relationMeta(row).craftHits.length">
              <el-tag v-if="relationMeta(row).courseHits.length" type="warning" effect="light" round size="small">我的课程材料</el-tag>
              <el-tag v-if="relationMeta(row).craftHits.length" type="info" effect="light" round size="small">我的工艺品材料</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="unit" label="单位" width="120">
          <template #default="{ row }">
            {{ row.unit || '未设置' }}
          </template>
        </el-table-column>

        <el-table-column label="库存" width="140">
          <template #default="{ row }">
            <div class="stock-cell">
              <span>{{ row.stock ?? 0 }}</span>
              <el-tag :type="stockTagType(row.stock)" effect="light" round size="small">
                {{ stockText(row.stock) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="price" label="单价" width="140">
          <template #default="{ row }">
            {{ fmtMoney(row.price) }}
          </template>
        </el-table-column>

        <el-table-column prop="supplier" label="供应商" min-width="160">
          <template #default="{ row }">
            {{ row.supplier || '未填写' }}
          </template>
        </el-table-column>

        <el-table-column label="我的关联使用" min-width="260">
          <template #default="{ row }">
            <div class="usage-box">
              <div v-if="relationMeta(row).courseHits.length" class="usage-line">
                <span class="usage-label">关联课程：</span>{{ relationMeta(row).courseHits.map((item) => item.title).slice(0, 2).join('、') }}
              </div>
              <div v-if="relationMeta(row).craftHits.length" class="usage-line">
                <span class="usage-label">关联工艺品：</span>{{ relationMeta(row).craftHits.map((item) => item.name || item.title).slice(0, 2).join('、') }}
              </div>
              <div v-if="!relationMeta(row).courseHits.length && !relationMeta(row).craftHits.length" class="usage-empty">当前未匹配到我的课程或工艺品</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="备注" min-width="220">
          <template #default="{ row }">
            <div class="description-text">{{ row.description || '暂无备注' }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">
            {{ fmtTime(row.updatedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlgVisible" :title="dlgTitle" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" maxlength="200" />
        </el-form-item>

        <el-form-item label="分类">
          <el-input v-model="form.category" />
        </el-form-item>

        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="如：kg / 件 / 米 / 卷" />
        </el-form-item>

        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 100%;" />
        </el-form-item>

        <el-form-item label="单价">
          <el-input v-model="form.price" placeholder="如：12.50" />
        </el-form-item>

        <el-form-item label="供应商">
          <el-input v-model="form.supplier" />
        </el-form-item>

        <el-form-item label="图片URL">
          <el-input v-model="form.imageUrl" placeholder="/images/xuancai1.png 或 http://..." />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dlgVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { materialApi } from '../api/material'
import { courseApi } from '../api/course'
import { listCraftApi } from '../api/craft'
import { getUser, getUserRole } from '../utils/auth'
import {
  matchCourseListForCraftCategories,
  materialMatchesCourse,
  materialMatchesCraft
} from '../utils/coursePresentation'

const loading = ref(false)
const rows = ref([])
const courses = ref([])
const crafts = ref([])
const keyword = ref('')
const onlyMineMaterials = ref(false)

const dlgVisible = ref(false)
const dlgTitle = ref('新增材料')
const editingId = ref(null)

const currentUser = computed(() => getUser())
const isHandicraft = computed(() => getUserRole() === 'handicraft')

const form = ref({
  name: '',
  category: '',
  unit: '',
  stock: 0,
  price: '0',
  supplier: '',
  description: '',
  imageUrl: ''
})

function fmtTime(v) {
  if (!v) return '未记录'
  return String(v).replace('T', ' ').slice(0, 19)
}

function fmtMoney(v) {
  if (v === null || v === undefined || v === '') return '0.00'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return n.toFixed(2)
}

function stockText(v) {
  const n = Number(v ?? 0)
  if (n <= 0) return '缺货'
  if (n < 5) return '偏低'
  return '正常'
}

function stockTagType(v) {
  const n = Number(v ?? 0)
  if (n <= 0) return 'danger'
  if (n < 5) return 'warning'
  return 'success'
}

const mineCrafts = computed(() => {
  const uid = String(currentUser.value?.id || '')
  if (!uid) return []
  return (crafts.value || []).filter((item) => String(item?.artisanId || '') === uid || String(item?.createdBy || '') === uid)
})

const mineCategories = computed(() => Array.from(new Set(mineCrafts.value.map((item) => String(item.category || '')).filter(Boolean))))

const mineCourses = computed(() => {
  const uid = String(currentUser.value?.id || '')
  const direct = (courses.value || []).filter((item) => String(item?.createdBy || '') === uid || String(item?.userId || '') === uid)
  const byCategory = matchCourseListForCraftCategories(courses.value || [], mineCategories.value)
  return Array.from(new Map([...direct, ...byCategory].map((item) => [String(item.id), item])).values())
})

const relationMetaMap = computed(() => {
  const map = new Map()
  for (const row of rows.value || []) {
    const courseHits = isHandicraft.value ? mineCourses.value.filter((course) => materialMatchesCourse(row, course)) : []
    const craftHits = isHandicraft.value ? mineCrafts.value.filter((craft) => materialMatchesCraft(row, craft)) : []
    map.set(String(row.id), { courseHits, craftHits })
  }
  return map
})

function relationMeta(row) {
  return relationMetaMap.value.get(String(row.id)) || { courseHits: [], craftHits: [] }
}

function isMineMaterial(row) {
  const meta = relationMeta(row)
  return meta.courseHits.length > 0 || meta.craftHits.length > 0
}

const filteredRows = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  let list = [...rows.value]

  if (isHandicraft.value && onlyMineMaterials.value) {
    list = list.filter((row) => isMineMaterial(row))
  }

  if (k) {
    list = list.filter((x) => {
      const meta = relationMeta(x)
      const content = [
        x.name || '',
        x.category || '',
        x.supplier || '',
        x.description || '',
        ...meta.courseHits.map((item) => item.title || ''),
        ...meta.craftHits.map((item) => item.name || item.title || '')
      ].join(' ').toLowerCase()
      return content.includes(k)
    })
  }

  if (isHandicraft.value) {
    list.sort((a, b) => Number(isMineMaterial(b)) - Number(isMineMaterial(a)))
  }

  return list
})

const summaryCards = computed(() => {
  const list = rows.value || []
  const categories = new Set(list.map((item) => item.category).filter(Boolean)).size
  const lowStock = list.filter((item) => Number(item.stock ?? 0) < 5).length
  const outOfStock = list.filter((item) => Number(item.stock ?? 0) <= 0).length
  const suppliers = new Set(list.map((item) => item.supplier).filter(Boolean)).size
  const inventoryValue = list.reduce((sum, item) => sum + (Number(item.stock ?? 0) * Number(item.price ?? 0)), 0)
  const mineCount = list.filter((item) => isMineMaterial(item)).length

  return [
    { label: '材料总数', value: list.length, desc: '当前系统维护的材料档案数' },
    { label: '材料分类', value: categories, desc: '已覆盖的材料类别数量' },
    { label: '低库存', value: lowStock, desc: '库存少于 5 的材料数' },
    { label: '缺货材料', value: outOfStock, desc: '当前库存为 0 的材料数' },
    { label: '库存估值', value: `￥${inventoryValue.toFixed(0)}`, desc: '按库存与单价估算的材料价值' },
    { label: '供应来源', value: suppliers, desc: '已记录的供应商数量' },
    { label: '我的常用材料', value: mineCount, desc: isHandicraft.value ? '与我授课课程或工艺品关联的材料' : '仅匠人端显示个人关联材料' }
  ]
})

async function load() {
  loading.value = true
  try {
    const [materialRes, courseRes, craftRes] = await Promise.all([
      materialApi.list(),
      courseApi.listAll().catch(() => []),
      listCraftApi().catch(() => [])
    ])
    rows.value = Array.isArray(materialRes) ? materialRes : (materialRes?.data || [])
    courses.value = Array.isArray(courseRes) ? courseRes : (courseRes?.data || [])
    crafts.value = Array.isArray(craftRes) ? craftRes : (craftRes?.data || [])
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingId.value = null
  form.value = {
    name: '',
    category: '',
    unit: '',
    stock: 0,
    price: '0',
    supplier: '',
    description: '',
    imageUrl: ''
  }
}

function openCreate() {
  resetForm()
  dlgTitle.value = '新增材料'
  dlgVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  dlgTitle.value = '编辑材料'
  form.value = {
    name: row.name || '',
    category: row.category || '',
    unit: row.unit || '',
    stock: row.stock ?? 0,
    price: row.price ?? '0',
    supplier: row.supplier || '',
    description: row.description || '',
    imageUrl: row.imageUrl || ''
  }
  dlgVisible.value = true
}

async function submit() {
  if (!form.value.name || !form.value.name.trim()) {
    return ElMessage.error('名称不能为空')
  }

  const payload = {
    ...form.value,
    name: form.value.name.trim()
  }

  if (editingId.value) {
    await materialApi.update(editingId.value, payload)
    ElMessage.success('已更新')
  } else {
    await materialApi.create(payload)
    ElMessage.success('已新增')
  }

  dlgVisible.value = false
  await load()
}

async function removeRow(row) {
  await ElMessageBox.confirm(`确定删除材料【${row.name}】吗？`, '提示', { type: 'warning' })
  await materialApi.remove(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.page { padding: 12px; }
.panel-card { border-radius: 18px; }
.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 12px;
  flex-wrap: wrap;
}
.title { font-size: 18px; font-weight: 800; }
.desc { font-size: 13px; color: #64748b; margin-top: 6px; line-height: 1.7; }
.actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
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
.material-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
}
.image-placeholder {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  border: 1px dashed #d0d7e2;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  background: #f8fafc;
  font-size: 12px;
}
.material-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.material-name {
  font-weight: 700;
  color: #1f2937;
}
.sub {
  font-size: 12px;
  color: #64748b;
  margin-top: 6px;
}
.relation-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 8px;
}
.stock-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.usage-box {
  color: #334155;
  line-height: 1.7;
}
.usage-line + .usage-line {
  margin-top: 6px;
}
.usage-label {
  color: #64748b;
}
.usage-empty {
  color: #94a3b8;
}
.description-text {
  color: #334155;
  line-height: 1.7;
}
</style>
