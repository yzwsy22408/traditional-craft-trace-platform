<template>
  <div class="page">
    <el-card class="panel-card" shadow="never">
      <div class="head">
        <div>
          <div class="title">工坊信息管理</div>
          <div class="desc">统一维护工坊档案、联系方式与匠人归属，同时展示与已发布课程相匹配的研学工坊矩阵。</div>
        </div>
        <div class="actions">
          <el-button @click="refresh">刷新</el-button>
          <el-button type="primary" @click="openCreate">新增工坊</el-button>
        </div>
      </div>

      <el-alert
        v-if="isAdmin"
        title="当前为管理员视角，可额外执行工坊匠人绑定与更换操作。"
        type="info"
        show-icon
        class="page-alert"
      />

      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索：工坊名称 / 匠人 / 电话 / 地址"
          clearable
          style="max-width: 380px;"
        />
        <el-button @click="keyword = ''" plain>重置</el-button>
      </div>

      <div class="summary-grid">
        <div v-for="item in summaryCards" :key="item.label" class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-desc">{{ item.desc }}</div>
        </div>
      </div>

      <section class="course-workshop-panel">
        <div class="course-workshop-head">
          <div>
            <div class="course-workshop-title">课程关联工坊</div>
            <div class="course-workshop-desc">围绕平台已发布课程补充可直接展示的工坊画像，让课程、作品与线下场景之间的关系更完整。</div>
          </div>
        </div>

        <div class="course-workshop-grid">
          <div v-for="item in courseWorkshopShowcases" :key="item.name" class="course-workshop-card">
            <div class="course-badge">{{ item.course }}</div>
            <div class="course-workshop-name">{{ item.name }}</div>
            <div class="course-workshop-focus">{{ item.focus }}</div>
            <div class="course-workshop-meta">{{ item.city }} · {{ item.speciality }}</div>
          </div>
        </div>
      </section>

      <el-table :data="pagedList" v-loading="loading" style="width:100%" empty-text="暂无工坊数据">
        <el-table-column type="index" label="序号" width="80" />

        <el-table-column label="工坊档案" min-width="220">
          <template #default="{ row }">
            <div class="workshop-name">{{ row.name || '未命名工坊' }}</div>
            <div class="sub">ID：{{ row.id ?? '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="匠人归属" width="200">
          <template #default="{ row }">
            <div class="owner-name">{{ formatOwner(row) }}</div>
            <div class="sub">{{ row.ownerName ? '已绑定负责匠人' : '当前未绑定匠人' }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="联系电话" width="150">
          <template #default="{ row }">
            {{ row.phone || '未填写' }}
          </template>
        </el-table-column>

        <el-table-column prop="address" label="工坊地址" min-width="220">
          <template #default="{ row }">
            <div class="address-text">{{ row.address || '未填写地址' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="简介" min-width="220">
          <template #default="{ row }">
            <div class="intro-text">{{ row.intro || '暂无工坊简介' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>

            <el-button
              v-if="isAdmin"
              link
              type="warning"
              @click="openAssignOwner(row)"
            >
              更换匠人
            </el-button>

            <el-button link type="danger" @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="filteredList.length"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <el-dialog v-model="dlg" :title="form.id ? '编辑工坊' : '新增工坊'" width="700px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="工坊名称">
          <el-input v-model="form.name" placeholder="工坊名称" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="工坊地址">
          <el-input v-model="form.address" placeholder="工坊地址" />
        </el-form-item>
        <el-form-item label="工坊简介">
          <el-input v-model="form.intro" type="textarea" :rows="4" placeholder="工坊简介" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDlg" title="更换匠人" width="520px">
      <div class="assign-title">工坊：<b>{{ assignWorkshopName }}</b></div>

      <el-form label-width="110px">
        <el-form-item label="选择匠人">
          <el-select
            v-model="assignOwnerUserId"
            placeholder="请选择匠人账号"
            style="width: 100%"
            filterable
            clearable
          >
            <el-option
              v-for="u in handicraftUsers"
              :key="u.id"
              :label="formatUserLabel(u)"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="assignDlg = false">取消</el-button>
        <el-button type="primary" :loading="assignSaving" @click="doAssignOwner">确认更换</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { workshopApi } from '../api/workshop'
import { listUsers } from '../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUser } from '../utils/auth'

const loading = ref(false)
const list = ref([])

const currentUser = ref(null)
const isAdmin = computed(() => currentUser.value?.role === 'admin')

const keyword = ref('')
const page = ref(1)
const pageSize = ref(10)

const dlg = ref(false)
const form = ref({})

const formatOwner = (row) => {
  const real = row.realName || '匠人'
  const user = row.ownerName || ''
  return user ? `${real}（${user}）` : '暂未绑定匠人'
}

const filteredList = computed(() => {
  const kw = (keyword.value || '').trim().toLowerCase()
  if (!kw) return list.value

  return (list.value || []).filter(row => {
    const s = [
      row.name || '',
      row.phone || '',
      row.address || '',
      row.ownerName || '',
      row.realName || ''
    ].join(' ').toLowerCase()

    return s.includes(kw)
  })
})

const summaryCards = computed(() => {
  const all = list.value || []
  const bound = all.filter(row => row.ownerName || row.ownerUserId).length
  const unbound = all.length - bound
  const withPhone = all.filter(row => row.phone).length
  const withAddress = all.filter(row => row.address).length

  return [
    { label: '工坊总数', value: all.length, desc: '当前系统内维护的工坊档案' },
    { label: '已绑定匠人', value: bound, desc: '已有明确负责人的工坊数量' },
    { label: '待绑定工坊', value: unbound, desc: '还可继续补充匠人归属信息' },
    { label: '已填电话', value: withPhone, desc: '便于联系与研学活动对接' },
    { label: '已填地址', value: withAddress, desc: '便于线下参观与工坊展示' },
    { label: '课程关联工坊', value: courseWorkshopShowcases.length, desc: '围绕课程沉淀的展示型工坊画像' }
  ]
})

const courseWorkshopShowcases = [
  {
    course: '广西壮锦技艺初探',
    name: '壮锦手工包研学工坊',
    focus: '围绕织锦纹样识别、配色练习和手工包成型展开实践。',
    city: '广西南宁',
    speciality: '壮锦织造'
  },
  {
    course: '传统木雕基础体验课',
    name: '木雕基础体验工坊',
    focus: '聚焦木料认知、线稿转印、基础刀法和立体修整。',
    city: '浙江东阳',
    speciality: '木雕研学'
  },
  {
    course: '苏绣双面绣精研班',
    name: '苏绣工艺研修工坊',
    focus: '展示绣线选配、针法控制和双面纹样的细节表达。',
    city: '江苏苏州',
    speciality: '刺绣工艺'
  },
  {
    course: '紫砂手工壶全流程课程',
    name: '紫砂器型塑造工坊',
    focus: '串联泥料处理、身筒成型、壶嘴壶把安装和修坯。',
    city: '江苏宜兴',
    speciality: '紫砂手作'
  },
  {
    course: '青花瓷手绘体验营',
    name: '青花瓷绘制展示工坊',
    focus: '突出纹样构图、线描上色与烧制前后的效果对照。',
    city: '江西景德镇',
    speciality: '陶瓷彩绘'
  },
  {
    course: '苗族银饰錾刻课程',
    name: '苗银錾刻体验工坊',
    focus: '讲解银片塑形、錾刻纹样和民族饰品的成品呈现。',
    city: '贵州黔东南',
    speciality: '银饰技艺'
  }
]

const pagedList = computed(() => {
  const arr = filteredList.value || []
  const start = (page.value - 1) * pageSize.value
  return arr.slice(start, start + pageSize.value)
})

watch(keyword, () => {
  page.value = 1
})

const refresh = async () => {
  loading.value = true
  try {
    const res = await workshopApi.list()
    list.value = Array.isArray(res) ? res : (res?.data ?? [])
    page.value = 1
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  form.value = { id: undefined, name: '', phone: '', address: '', intro: '' }
  dlg.value = true
}

const openEdit = (row) => {
  form.value = { ...row }
  dlg.value = true
}

const save = async () => {
  if (!form.value.name || !form.value.name.trim()) return ElMessage.error('名称不能为空')

  const payload = {
    name: form.value.name,
    phone: form.value.phone,
    address: form.value.address,
    intro: form.value.intro
  }

  try {
    if (form.value.id) {
      await workshopApi.update(form.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await workshopApi.create(payload)
      ElMessage.success('创建成功')
    }
    dlg.value = false
    await refresh()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '保存失败')
  }
}

const removeRow = async (row) => {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm(
      `确定删除工坊「${row.name || row.id}」吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await workshopApi.remove(row.id)
    ElMessage.success('删除成功')
    await refresh()
  } catch (e) {}
}

const assignDlg = ref(false)
const assignSaving = ref(false)
const assignWorkshopId = ref(null)
const assignWorkshopName = ref('')
const assignOwnerUserId = ref(null)
const handicraftUsers = ref([])

const formatUserLabel = (u) => {
  const username = String(u?.username || '').trim()
  const name = String(u?.name || '').trim()
  const displayName = name || '匠人'
  return username ? `${displayName}（${username}）` : displayName
}

const isCandidateHandicraft = (u) => {
  if (!u) return false
  const username = String(u.username || '').trim()
  const uname = username.toLowerCase()
  const role = String(u.role || '').trim()
  const r = role.toLowerCase()

  if (r === 'admin' || uname.startsWith('admin')) return false

  return (
    uname.startsWith('craft') ||
    r.includes('hand') || r.includes('craft') || r.includes('artisan') || r.includes('匠') ||
    true
  )
}

const loadHandicraftUsers = async () => {
  try {
    const res = await listUsers()
    const body = res?.data ?? res
    const arr = Array.isArray(body) ? body : (body?.data || body?.list || [])

    const all = (arr || []).filter(Boolean)

    let cand = all.filter(isCandidateHandicraft)

    const seen = new Set()
    cand = cand.filter(u => {
      const id = u?.id
      if (id == null) return false
      if (seen.has(id)) return false
      seen.add(id)
      return true
    })

    cand.sort((a, b) => {
      const au = String(a?.username || '').toLowerCase()
      const bu = String(b?.username || '').toLowerCase()
      const ac = au.startsWith('craft') ? 0 : 1
      const bc = bu.startsWith('craft') ? 0 : 1
      if (ac !== bc) return ac - bc
      return au.localeCompare(bu)
    })

    handicraftUsers.value = cand

    if (handicraftUsers.value.length === 0) {
      ElMessage.warning('用户列表为空或全被过滤')
    }
  } catch (e) {
    handicraftUsers.value = []
    ElMessage.error('加载匠人列表失败')
  }
}

const openAssignOwner = async (row) => {
  if (!row?.id) return
  assignWorkshopId.value = row.id
  assignWorkshopName.value = row.name || String(row.id)
  assignOwnerUserId.value = row.ownerUserId || null

  await loadHandicraftUsers()
  assignDlg.value = true
}

const doAssignOwner = async () => {
  if (!assignWorkshopId.value) return
  if (!assignOwnerUserId.value) return ElMessage.error('请选择匠人')

  assignSaving.value = true
  try {
    await workshopApi.assignOwner(assignWorkshopId.value, assignOwnerUserId.value)
    ElMessage.success('更换成功')
    assignDlg.value = false
    await refresh()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '更换失败')
  } finally {
    assignSaving.value = false
  }
}

onMounted(async () => {
  currentUser.value = getUser()
  await refresh()
})
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
  gap: 8px;
  flex-wrap: wrap;
}

.page-alert {
  margin-bottom: 16px;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
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

.course-workshop-panel {
  margin-bottom: 18px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.course-workshop-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.course-workshop-title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}

.course-workshop-desc {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.course-workshop-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.course-workshop-card {
  min-height: 168px;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #dbeafe;
  background: linear-gradient(145deg, #f8fbff 0%, #eef6ff 100%);
}

.course-badge {
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  background: #dbeafe;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
}

.course-workshop-name {
  margin-top: 12px;
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.5;
}

.course-workshop-focus {
  margin-top: 8px;
  color: #475569;
  font-size: 13px;
  line-height: 1.7;
}

.course-workshop-meta {
  margin-top: 12px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.workshop-name,
.owner-name {
  font-weight: 700;
  color: #1f2937;
}

.sub {
  font-size: 12px;
  color: #64748b;
  margin-top: 6px;
}

.address-text,
.intro-text {
  color: #334155;
  line-height: 1.7;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.assign-title {
  margin-bottom: 10px;
}

@media (max-width: 1200px) {
  .course-workshop-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .course-workshop-grid {
    grid-template-columns: 1fr;
  }
}
</style>
