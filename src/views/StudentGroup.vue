<template>
  <div class="page">
    <el-card class="panel-card" shadow="never">
      <div class="head">
        <div>
          <div class="title">学员团体报名</div>
          <div class="desc">面向班级、社团或研学小组的集中报名工作台，可在选定课程后批量提交学员报名记录。</div>
        </div>
        <div class="actions">
          <el-button @click="loadInitialData">刷新数据</el-button>
          <el-button
            type="primary"
            :disabled="!selectedCourseId || !multipleSelection.length"
            @click="handleBatchEnroll"
          >
            提交团体报名
          </el-button>
        </div>
      </div>

      <div class="toolbar">
        <el-select
          v-model="selectedCourseId"
          placeholder="请选择目标课程"
          filterable
          clearable
          style="width: 280px"
        >
          <el-option
            v-for="course in courseList"
            :key="course.id"
            :label="courseOptionLabel(course)"
            :value="course.id"
          />
        </el-select>

        <el-input
          v-model="keyword"
          placeholder="搜索：用户名 / 姓名 / 电话"
          clearable
          style="max-width: 320px;"
          @keyup.enter="applyKeyword"
        />

        <el-button type="primary" @click="applyKeyword">搜索</el-button>
        <el-button @click="resetKeyword">重置</el-button>
      </div>

      <div class="summary-grid">
        <div v-for="item in summaryCards" :key="item.label" class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-desc">{{ item.desc }}</div>
        </div>
      </div>

      <div v-if="selectedCourse" class="course-brief">
        <div class="course-brief-title">{{ selectedCourse.title || selectedCourse.name || '未命名课程' }}</div>
        <div class="course-brief-meta">
          <span>分类：{{ selectedCourse.category || '未分类' }}</span>
          <span>价格：{{ selectedCourse.price ?? 0 }}</span>
          <span>容量：{{ formatCapacity(selectedCourse.capacity) }}</span>
          <span>阶段：{{ courseStageText(selectedCourse) }}</span>
        </div>
      </div>

      <el-table
        :data="filteredStudents"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        empty-text="暂无可选学员"
      >
        <el-table-column type="selection" width="55" />

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

        <el-table-column label="联系地址" min-width="220">
          <template #default="{ row }">
            <div class="address-text">{{ row.address || '未填写地址' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag size="small" type="success" effect="plain" round>{{ row.role || 'student' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageStudents } from '@/api/auth'
import { enrollApi } from '@/api/enroll'
import request from '@/api/request'

const loading = ref(false)
const studentList = ref([])
const courseList = ref([])
const selectedCourseId = ref(null)
const multipleSelection = ref([])
const keyword = ref('')
const appliedKeyword = ref('')

const selectedCourse = computed(() => (
  courseList.value.find(item => String(item.id) === String(selectedCourseId.value)) || null
))

const filteredStudents = computed(() => {
  const kw = appliedKeyword.value.trim().toLowerCase()
  if (!kw) return studentList.value
  return studentList.value.filter(item => {
    const text = `${item.username || ''} ${item.name || ''} ${item.phone || ''}`.toLowerCase()
    return text.includes(kw)
  })
})

const summaryCards = computed(() => {
  const withContact = filteredStudents.value.filter(item => item.phone || item.email).length
  return [
    { label: '可选学员', value: filteredStudents.value.length, desc: '当前列表中可用于团体报名的学员数量' },
    { label: '已选择', value: multipleSelection.value.length, desc: '本次准备一起报名的学员数量' },
    { label: '可报课程', value: courseList.value.length, desc: '当前已加载的研学课程数量' },
    { label: '已留联系方式', value: withContact, desc: '便于统一通知课程安排的学员数量' },
    {
      label: '目标课程',
      value: selectedCourse.value ? (selectedCourse.value.title || selectedCourse.value.name || '已选择') : '未选择',
      desc: selectedCourse.value ? '当前团体报名将写入该课程' : '请先选择要报名的研学课程'
    }
  ]
})

const loadInitialData = async () => {
  loading.value = true
  try {
    const [studentRes, courses] = await Promise.all([
      pageStudents({ page: 1, size: 100 }),
      request.get('/course')
    ])
    studentList.value = Array.isArray(studentRes?.list) ? studentRes.list : (Array.isArray(studentRes) ? studentRes : [])
    courseList.value = Array.isArray(courses) ? courses : (Array.isArray(courses?.list) ? courses.list : [])
  } catch (e) {
    studentList.value = []
    courseList.value = []
    ElMessage.error('初始化数据加载失败')
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

const applyKeyword = () => {
  appliedKeyword.value = keyword.value
}

const resetKeyword = () => {
  keyword.value = ''
  appliedKeyword.value = ''
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

const courseOptionLabel = (course) => {
  const name = course?.title || course?.name || '未命名课程'
  const category = course?.category || '未分类'
  return `${name} [${category}]`
}

const handleBatchEnroll = async () => {
  if (!selectedCourseId.value) {
    ElMessage.warning('请先选择目标课程')
    return
  }

  const studentIds = multipleSelection.value.map(item => item.id).filter(Boolean)
  if (!studentIds.length) {
    ElMessage.warning('请至少勾选一名学员')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定将 ${studentIds.length} 名学员批量报名到当前课程吗？`,
      '确认提交',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await enrollApi.batch({
      courseId: selectedCourseId.value,
      studentIds
    })
    ElMessage.success('团体报名提交成功')
    multipleSelection.value = []
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('批量报名失败')
    }
  }
}

onMounted(loadInitialData)
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
  font-size: 24px;
  font-weight: 800;
  color: #1f2937;
  word-break: break-word;
}

.summary-desc {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: #94a3b8;
}

.course-brief {
  margin-bottom: 18px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
  border: 1px solid #dbeafe;
}

.course-brief-title {
  font-size: 16px;
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

@media (max-width: 768px) {
  .page {
    padding: 16px;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
