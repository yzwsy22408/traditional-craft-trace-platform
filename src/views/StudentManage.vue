<template>
  <div class="page">
    <el-card class="panel-card" shadow="never">
      <div class="head">
        <div>
          <div class="title">学员管理</div>
          <div class="desc">集中维护学员档案，并支持按课程发起团体报名，方便班级、社团或研学小组统一组织参课。</div>
        </div>
        <div class="actions">
          <el-button @click="fetchStudents">刷新</el-button>
          <el-button
            type="success"
            :disabled="!selectedIds.length"
            :class="{ 'pulse-button': selectedIds.length > 0 }"
            @click="openEnrollDialog"
          >
            团体报名<span v-if="selectedIds.length">（{{ selectedIds.length }}）</span>
          </el-button>
          <el-button @click="exportExcel">导出</el-button>
          <el-button type="primary" @click="openCreateDialog">新增学员</el-button>
        </div>
      </div>

      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索：用户名 / 姓名 / 电话"
          clearable
          style="max-width: 320px;"
          @keyup.enter="onSearch"
        />
        <el-button type="primary" @click="onSearch">搜索</el-button>
        <el-button @click="onResetSearch">重置</el-button>
      </div>

      <div class="summary-grid">
        <div v-for="item in summaryCards" :key="item.label" class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-desc">{{ item.desc }}</div>
        </div>
      </div>

      <el-table
        :data="list"
        stripe
        v-loading="loading"
        class="custom-table"
        header-cell-class-name="table-header"
        empty-text="暂无学员档案"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" prop="id" width="70" align="center" />

        <el-table-column label="学员档案" min-width="220">
          <template #default="{ row }">
            <div class="user-name">{{ row.name || '未填写姓名' }}</div>
            <div class="sub">@{{ row.username || 'student-user' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="联系方式" min-width="220">
          <template #default="{ row }">
            <div class="contact-line">电话：{{ row.phone || '未填写' }}</div>
            <div class="contact-line">邮箱：{{ row.email || '未填写' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="联系地址" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="address-text">{{ row.address || '未填写地址' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          :page-sizes="[10, 20, 50]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="enrollDialogVisible" title="发起团体报名" width="520px">
      <div class="dialog-note">
        已选中 <span class="highlight">{{ selectedIds.length }}</span> 名学员，请选择本次统一报名的目标课程。
      </div>

      <el-form label-position="top">
        <el-form-item label="目标研学课程" required>
          <el-select
            v-model="targetCourseId"
            placeholder="请选择课程"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="courseOptionLabel(course)"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <div v-if="selectedCourse" class="course-brief">
        <div class="course-brief-title">{{ selectedCourse.title || selectedCourse.name || '未命名课程' }}</div>
        <div class="course-brief-meta">
          <span>分类：{{ selectedCourse.category || '未分类' }}</span>
          <span>价格：{{ selectedCourse.price ?? 0 }}</span>
          <span>容量：{{ formatCapacity(selectedCourse.capacity) }}</span>
          <span>阶段：{{ courseStageText(selectedCourse) }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="enrollDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="enrolling" @click="submitBatchEnroll">确认提交报名</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" :title="editMode === 'create' ? '新增学员档案' : '修改学员信息'" width="560px">
      <el-form :model="editForm" label-width="100px" label-suffix="：">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="editForm.username" :disabled="editMode === 'edit'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码">
              <el-input v-model="editForm.password" type="password" placeholder="留空则不修改" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="真实姓名">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="邮箱地址">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="联系地址">
          <el-input v-model="editForm.address" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveStudent">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageStudents, createStudent, updateUser, deleteUser } from '@/api/auth'
import { enrollApi } from '@/api/enroll'
import request from '@/api/request'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const selectedIds = ref([])

const enrollDialogVisible = ref(false)
const targetCourseId = ref(null)
const courseList = ref([])
const enrolling = ref(false)

const editDialogVisible = ref(false)
const editMode = ref('create')
const saving = ref(false)
const editForm = reactive({
  id: null,
  username: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  address: ''
})

const selectedCourse = computed(() => (
  courseList.value.find(item => String(item.id) === String(targetCourseId.value)) || null
))

const summaryCards = computed(() => {
  const students = list.value || []
  const withPhone = students.filter(item => item.phone).length
  const withEmail = students.filter(item => item.email).length
  const withAddress = students.filter(item => item.address).length

  return [
    { label: '当前页学员', value: students.length, desc: '本页已加载的学员档案数量' },
    { label: '已留电话', value: withPhone, desc: '可直接联系确认课程安排的学员数' },
    { label: '已留邮箱', value: withEmail, desc: '便于发送课程通知和材料提醒的学员数' },
    { label: '已留地址', value: withAddress, desc: '档案信息相对完整的学员数量' },
    { label: '已勾选', value: selectedIds.value.length, desc: '当前准备参与团体报名的学员数量' }
  ]
})

const fetchStudents = async () => {
  loading.value = true
  try {
    const res = await pageStudents({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value.trim()
    })

    if (res?.success) {
      list.value = Array.isArray(res.list) ? res.list : []
      total.value = Number(res.total || 0)
    } else {
      list.value = Array.isArray(res) ? res : []
      total.value = list.value.length
    }
  } catch (e) {
    ElMessage.error('学员列表加载失败')
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (val) => {
  selectedIds.value = val.map(item => item.id)
}

const openEnrollDialog = async () => {
  try {
    const res = await request.get('/course')
    courseList.value = Array.isArray(res) ? res : (Array.isArray(res?.list) ? res.list : [])
    targetCourseId.value = null
    enrollDialogVisible.value = true
  } catch (e) {
    ElMessage.error('课程列表加载失败，暂时无法发起团体报名')
  }
}

const submitBatchEnroll = async () => {
  if (!targetCourseId.value) return ElMessage.warning('请选择目标课程')
  if (!selectedIds.value.length) return ElMessage.warning('请至少选择一名学员')

  enrolling.value = true
  try {
    await enrollApi.batch({
      courseId: targetCourseId.value,
      studentIds: selectedIds.value
    })
    ElMessage.success('团体报名已提交')
    enrollDialogVisible.value = false
    selectedIds.value = []
    fetchStudents()
  } catch (e) {
    ElMessage.error('团体报名提交失败')
  } finally {
    enrolling.value = false
  }
}

const saveStudent = async () => {
  if (!editForm.username?.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }

  if (editMode.value === 'create' && !editForm.password?.trim()) {
    ElMessage.warning('新增学员时必须填写密码')
    return
  }

  saving.value = true
  try {
    if (editMode.value === 'create') {
      await createStudent({ ...editForm })
    } else {
      await updateUser(editForm.id, { ...editForm, role: 'student' })
    }
    ElMessage.success('学员档案保存成功')
    editDialogVisible.value = false
    fetchStudents()
  } catch (e) {
    ElMessage.error('学员档案保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该学员档案吗？删除后需要重新维护其报名记录。', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await deleteUser(id)
    ElMessage.success('学员档案已删除')
    fetchStudents()
  } catch (e) {}
}

const onSearch = () => {
  page.value = 1
  fetchStudents()
}

const onResetSearch = () => {
  keyword.value = ''
  page.value = 1
  fetchStudents()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  page.value = 1
  fetchStudents()
}

const handleCurrentChange = (current) => {
  page.value = current
  fetchStudents()
}

const openCreateDialog = () => {
  editMode.value = 'create'
  Object.assign(editForm, {
    id: null,
    username: '',
    password: '',
    name: '',
    email: '',
    phone: '',
    address: ''
  })
  editDialogVisible.value = true
}

const openEditDialog = (row) => {
  editMode.value = 'edit'
  Object.assign(editForm, {
    id: row.id,
    username: row.username || '',
    password: '',
    name: row.name || '',
    email: row.email || '',
    phone: row.phone || '',
    address: row.address || ''
  })
  editDialogVisible.value = true
}

const exportExcel = async () => {
  if (!list.value.length) {
    ElMessage.info('当前没有可导出的学员数据')
    return
  }

  const xlsxModule = await import('xlsx')
  const XLSX = xlsxModule.default ?? xlsxModule
  const data = list.value.map(item => ({
    ID: item.id,
    用户名: item.username,
    姓名: item.name,
    电话: item.phone,
    邮箱: item.email,
    地址: item.address
  }))
  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '学员档案')
  XLSX.writeFile(wb, '学员列表.xlsx')
  ElMessage.success('学员列表已导出')
}

const courseOptionLabel = (course) => {
  const name = course?.title || course?.name || '未命名课程'
  const category = course?.category || '未分类'
  return `${name} [${category}]`
}

const formatCapacity = (capacity) => {
  const num = Number(capacity || 0)
  return num > 0 ? `${num} 人` : '不限人数'
}

const courseStageText = (course) => {
  const now = Date.now()
  const start = course?.startTime ? new Date(course.startTime).getTime() : null
  const end = course?.endTime ? new Date(course.endTime).getTime() : null

  if (start && now < start) return '待开课'
  if (end && now > end) return '已结束'
  if (start || end) return '进行中'
  return '时间待定'
}

onMounted(fetchStudents)
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
  margin-bottom: 16px;
}

.title {
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
}

.desc {
  margin-top: 6px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
}

.actions,
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar {
  margin-bottom: 18px;
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
  line-height: 1.5;
  color: #94a3b8;
}

.user-name {
  font-weight: 700;
  color: #1f2937;
}

.sub,
.contact-line,
.address-text {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.6;
  color: #64748b;
}

.address-text {
  white-space: normal;
}

.pager {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}

.dialog-note {
  padding: 12px 14px;
  border-radius: 12px;
  background: #eff6ff;
  color: #1d4ed8;
  margin-bottom: 14px;
}

.highlight {
  font-size: 18px;
  font-weight: 800;
  margin: 0 4px;
}

.course-brief {
  margin-top: 8px;
  padding: 14px 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
  border: 1px solid #dbeafe;
}

.course-brief-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e3a8a;
}

.course-brief-meta {
  margin-top: 8px;
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #475569;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.28); }
  70% { box-shadow: 0 0 0 10px rgba(34, 197, 94, 0); }
  100% { box-shadow: 0 0 0 0 rgba(34, 197, 94, 0); }
}

.pulse-button {
  animation: pulse 2s infinite;
}

@media (max-width: 768px) {
  .page {
    padding: 16px;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
