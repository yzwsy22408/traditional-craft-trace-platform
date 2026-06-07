<template>
  <div class="page">
    <el-card class="panel-card" shadow="never">
      <div class="head">
        <div>
          <div class="title">老师管理</div>
          <div class="desc">
            维护带队老师、所属学校和线下研学组织信息，让课程到访流程与协作关系保持清晰可查。
          </div>
        </div>

        <div class="actions">
          <el-button @click="refresh">刷新</el-button>
          <el-button type="primary" @click="openCreate">新增老师</el-button>
        </div>
      </div>

      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索：老师姓名 / 学校 / 学科 / 电话"
          clearable
          style="max-width: 360px;"
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

      <div class="scene-panel">
        <div class="scene-title">带队流程参考</div>
        <div class="scene-desc">
          老师先组织学生集合，再按课程安排带队到对应工坊，由匠人完成现场引导与实践指导；学生还可以通过展柜或说明牌上的溯源码继续查看作品档案与制作过程。
        </div>
      </div>

      <el-table :data="pagedList" style="width: 100%" empty-text="暂无老师档案">
        <el-table-column type="index" label="序号" width="80" />

        <el-table-column label="老师档案" min-width="220">
          <template #default="{ row }">
            <div class="name-main">{{ row.name || '未命名老师' }}</div>
            <div class="sub">{{ row.schoolName || '未设置学校信息' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="学科与联系" min-width="220">
          <template #default="{ row }">
            <div class="line">{{ row.subjectName || '研学指导老师' }}</div>
            <div class="sub">{{ row.phone || '未设置联系电话' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="带队路线" min-width="240">
          <template #default="{ row }">
            <div class="long-text">{{ row.leadRoute || '未设置带队路线' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="研学方向" min-width="220">
          <template #default="{ row }">
            <div class="long-text">{{ row.workshopFocus || '未设置负责课程、工坊方向或年级对象。' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑老师' : '新增老师'" width="720px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="老师姓名">
              <el-input v-model="form.name" placeholder="例如：李老师" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属学校">
              <el-input v-model="form.schoolName" placeholder="例如：南宁市青秀区第二中学" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="指导学科">
              <el-input v-model="form.subjectName" placeholder="例如：美术 / 综合实践 / 劳动教育" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="例如：138xxxx0000" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="带队路线">
          <el-input
            v-model="form.leadRoute"
            type="textarea"
            :rows="3"
            placeholder="例如：学校集合 -> 壮锦工坊签到 -> 分组体验 -> 展柜扫码溯源"
          />
        </el-form-item>

        <el-form-item label="研学方向">
          <el-input
            v-model="form.workshopFocus"
            type="textarea"
            :rows="3"
            placeholder="例如：负责初中研学班带队，重点对接壮锦、木雕等线下工坊体验课程。"
          />
        </el-form-item>

        <el-form-item label="补充说明">
          <el-input
            v-model="form.note"
            type="textarea"
            :rows="3"
            placeholder="可填写班级对象、到访频次、联络方式补充等。"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTeacher">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teacherApi } from '../api/teacher'

const keyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const teacherList = ref([])

const form = reactive({
  id: '',
  name: '',
  schoolName: '',
  subjectName: '',
  phone: '',
  leadRoute: '',
  workshopFocus: '',
  note: ''
})

const filteredList = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return teacherList.value

  return teacherList.value.filter((item) =>
    [
      item.name,
      item.schoolName,
      item.subjectName,
      item.phone,
      item.leadRoute,
      item.workshopFocus
    ]
      .join(' ')
      .toLowerCase()
      .includes(kw)
  )
})

const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

const summaryCards = computed(() => {
  const list = teacherList.value || []
  const schoolCount = new Set(list.map((item) => item.schoolName).filter(Boolean)).size
  const withPhone = list.filter((item) => item.phone).length
  const withRoute = list.filter((item) => item.leadRoute).length
  const withFocus = list.filter((item) => item.workshopFocus).length

  return [
    { label: '教师总数', value: list.length, desc: '当前已录入的带队教师档案数量' },
    { label: '合作学校', value: schoolCount, desc: '已接入系统展示的学校数量' },
    { label: '已填电话', value: withPhone, desc: '便于线下联络与课程组织协调' },
    { label: '已写路线', value: withRoute, desc: '已补全带队路线说明的教师数量' },
    { label: '已写方向', value: withFocus, desc: '已补全工坊对接方向与课程说明的教师数量' }
  ]
})

function resetForm() {
  Object.assign(form, {
    id: '',
    name: '',
    schoolName: '',
    subjectName: '',
    phone: '',
    leadRoute: '',
    workshopFocus: '',
    note: ''
  })
}

function buildPayload() {
  return {
    name: String(form.name || '').trim(),
    schoolName: String(form.schoolName || '').trim(),
    subjectName: String(form.subjectName || '').trim(),
    phone: String(form.phone || '').trim(),
    leadRoute: String(form.leadRoute || '').trim(),
    workshopFocus: String(form.workshopFocus || '').trim(),
    note: String(form.note || '').trim()
  }
}

async function refresh() {
  try {
    const res = await teacherApi.list()
    teacherList.value = Array.isArray(res) ? res : (res?.data || [])
    if (page.value > Math.max(1, Math.ceil(filteredList.value.length / pageSize.value))) {
      page.value = 1
    }
  } catch {
    teacherList.value = []
    ElMessage.error('老师档案加载失败')
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id || '',
    name: row.name || '',
    schoolName: row.schoolName || '',
    subjectName: row.subjectName || '',
    phone: row.phone || '',
    leadRoute: row.leadRoute || '',
    workshopFocus: row.workshopFocus || '',
    note: row.note || ''
  })
  dialogVisible.value = true
}

async function saveTeacher() {
  if (!form.name.trim()) {
    ElMessage.warning('请先填写老师姓名')
    return
  }

  try {
    if (form.id) {
      await teacherApi.update(form.id, buildPayload())
    } else {
      await teacherApi.create(buildPayload())
    }
    dialogVisible.value = false
    await refresh()
    ElMessage.success('老师档案已保存')
  } catch {
    ElMessage.error('老师档案保存失败')
  }
}

async function removeRow(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除老师“${row.name || '未命名老师'}”吗？`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }

  try {
    await teacherApi.remove(row.id)
    await refresh()
    ElMessage.success('老师档案已删除')
  } catch {
    ElMessage.error('老师档案删除失败')
  }
}

refresh()
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
  line-height: 1.7;
  max-width: 760px;
}

.actions,
.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.toolbar {
  margin-bottom: 16px;
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
  line-height: 1.6;
  color: #94a3b8;
}

.scene-panel {
  margin-bottom: 18px;
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

.name-main,
.line {
  font-weight: 700;
  color: #1f2937;
}

.sub {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.long-text {
  color: #334155;
  line-height: 1.7;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
