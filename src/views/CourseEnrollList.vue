<template>
  <div style="padding:16px">
    <el-card>
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <div style="font-size:18px; font-weight:600;">
          课程签到名单（课程ID：{{ courseId }}）
        </div>
        <div style="display:flex; gap:8px;">
          <el-button @click="refresh">刷新</el-button>
          <el-button @click="goBack">返回</el-button>
        </div>
      </div>

      <el-divider />

      <div style="display:flex; gap:10px; flex-wrap:wrap; margin-bottom: 12px;">
        <el-tag type="success">报名：{{ stat.enrolled }}</el-tag>
        <el-tag type="warning">已支付：{{ stat.paid }}</el-tag>
        <el-tag type="info">已签到：{{ stat.signed }}</el-tag>
      </div>

      <el-table :data="list" v-loading="loading" style="width:100%">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="id" label="记录ID" width="100" />
        <el-table-column prop="studentName" label="学生姓名" min-width="160" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status==='SIGNED'" type="success">SIGNED</el-tag>
            <el-tag v-else-if="row.status==='PAID'" type="warning">PAID</el-tag>
            <el-tag v-else type="info">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signTime" label="签到时间" min-width="200" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button
              v-if="row.status==='ENROLLED'"
              link type="warning"
              @click="doPay(row)"
            >标记支付</el-button>

            <el-button
              v-if="row.status==='PAID'"
              link type="success"
              @click="doSign(row)"
            >手动签到</el-button>

            <el-button
              v-if="row.status==='SIGNED'"
              link type="danger"
              @click="doUnsign(row)"
            >取消签到</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { enrollApi } from '../api/enroll'

const route = useRoute()
const router = useRouter()
const courseId = computed(() => Number(route.query.courseId || route.params.courseId || 0))

const loading = ref(false)
const list = ref([])

const stat = computed(() => {
  const arr = list.value || []
  return {
    enrolled: arr.filter(x => x.status === 'ENROLLED').length,
    paid: arr.filter(x => x.status === 'PAID').length,
    signed: arr.filter(x => x.status === 'SIGNED').length
  }
})

const refresh = async () => {
  if (!courseId.value) return ElMessage.error('缺少 courseId')
  loading.value = true
  try {
    const res = await enrollApi.listByCourse(courseId.value)
    list.value = res?.data ?? res
  } finally {
    loading.value = false
  }
}

const doPay = async (row) => {
  await enrollApi.pay(row.id)
  ElMessage.success('已标记支付')
  await refresh()
}

const doSign = async (row) => {
  await enrollApi.sign(row.id)
  ElMessage.success('签到成功')
  await refresh()
}

const doUnsign = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消签到吗？', '提示', { type: 'warning' })
    await enrollApi.unsign(row.id)
    ElMessage.success('已取消签到')
    await refresh()
  } catch (e) {}
}

const goBack = () => router.back()

onMounted(refresh)
</script>
