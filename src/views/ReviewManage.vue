<template>
  <div class="review-manage-page">
    <el-card class="main-card" shadow="never">
      <div class="head">
        <div>
          <div class="title">研学评价管理</div>
          <div class="desc">集中查看学员反馈，并以匠人或管理员身份对课程体验做回应，形成教学闭环。</div>
        </div>
        <div class="actions">
          <el-button type="primary" @click="loadAllReviews">刷新列表</el-button>
        </div>
      </div>

      <div class="summary-grid">
        <div v-for="item in summaryCards" :key="item.label" class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-desc">{{ item.desc }}</div>
        </div>
      </div>

      <el-table :data="reviews" style="width: 100%" v-loading="loading" empty-text="暂无评价记录">
        <el-table-column label="学员档案" width="170">
          <template #default="{ row }">
            <div class="student-name">{{ row.studentName || '研学学员' }}</div>
            <div class="sub">课程：{{ row.courseName || '未命名课程' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="课程评分" width="180">
          <template #default="{ row }">
            <div class="rating-cell">
              <el-rate :model-value="row.rating || 0" disabled />
              <span class="rating-text">{{ row.rating ?? 0 }} / 5</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评价内容" min-width="280">
          <template #default="{ row }">
            <div class="review-text">{{ row.content || '该学员未填写文字评价。' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="回复状态" width="180">
          <template #default="{ row }">
            <div v-if="row.reply">
              <el-tag type="success" effect="plain" round size="small">已回复</el-tag>
              <div class="reply-artisan">回复人：{{ row.replyArtisanName || '系统' }}</div>
            </div>
            <el-tag v-else type="warning" effect="dark" round size="small">待处理</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="评价时间" width="180">
          <template #default="{ row }">
            <span class="date-text">{{ fmt(row.createdAt) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleReply(row)">
              {{ row.reply ? '修改回复' : '立即回复' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="寄语学员" width="460px" destroy-on-close>
      <div class="quote-box">
        <div class="quote-label">学员感悟</div>
        <p class="quote-text">“{{ currentRow?.content || '暂无评价内容' }}”</p>
      </div>
      <el-input
        v-model="replyText"
        type="textarea"
        :rows="5"
        placeholder="在这里输入您对学员的鼓励或课程反馈..."
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { reviewApi } from '../api/review'
import { ElMessage } from 'element-plus'

const reviews = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const replyText = ref('')
const currentRow = ref(null)
const submitting = ref(false)

function fmt(v) {
  if (!v) return '未记录'
  return String(v).replace('T', ' ').substring(0, 16)
}

const summaryCards = computed(() => {
  const all = reviews.value
  const replied = all.filter(item => item.reply).length
  const highScore = all.filter(item => Number(item.rating ?? 0) >= 4).length
  const list = all.filter(item => typeof item.rating === 'number')
  const avg = list.length
    ? Math.round((list.reduce((sum, item) => sum + (item.rating || 0), 0) / list.length) * 10) / 10
    : 0

  return [
    { label: '评价总数', value: all.length, desc: '当前系统内收集到的学员反馈数' },
    { label: '已回复', value: replied, desc: '已经完成导师或匠人回应的反馈数' },
    { label: '待处理', value: all.length - replied, desc: '仍可继续补充回复的反馈数' },
    { label: '平均评分', value: all.length ? `${avg} / 5` : '暂无', desc: '当前评价整体满意度' },
    { label: '高分评价', value: highScore, desc: '评分不低于 4 分的记录数' }
  ]
})

const loadAllReviews = async () => {
  loading.value = true
  try {
    const res = await reviewApi.adminList()
    reviews.value = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : [])
  } catch (e) {
    reviews.value = []
    ElMessage.error('评价加载失败')
  } finally {
    loading.value = false
  }
}

const handleReply = (row) => {
  currentRow.value = row
  replyText.value = row.reply || ''
  dialogVisible.value = true
}

const submitReply = async () => {
  if (!replyText.value.trim()) return ElMessage.warning('回复内容不能为空')
  submitting.value = true
  try {
    await reviewApi.reply(currentRow.value.id, replyText.value)
    ElMessage.success('回复成功')
    dialogVisible.value = false
    await loadAllReviews()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadAllReviews)
</script>

<style scoped>
.review-manage-page {
  padding: 30px;
  background: #f8fafc;
  min-height: 100vh;
}

.main-card {
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

.student-name {
  font-weight: 700;
  color: #1f2937;
}

.sub,
.reply-artisan {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 6px;
}

.date-text {
  font-size: 12px;
  color: #64748b;
}

.rating-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.rating-text {
  font-size: 12px;
  color: #64748b;
}

.review-text {
  color: #334155;
  line-height: 1.7;
  white-space: pre-wrap;
}

.quote-box {
  background: #f8fafc;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e2e8f0;
}

.quote-label {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.quote-text {
  color: #475569;
  font-style: italic;
  margin: 0;
  line-height: 1.6;
}

:deep(.el-rate) {
  display: inline-flex;
  vertical-align: middle;
}
</style>
