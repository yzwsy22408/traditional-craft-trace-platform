<template>
  <div class="my-reviews-page">
    <el-card class="review-card shadow-soft" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">我的研学评价</span>
          <el-tag type="info" round>{{ reviews.length }} 条记录</el-tag>
        </div>
      </template>

      <el-empty v-if="reviews.length === 0" description="暂无评价记录，快去课程详情页发表感悟吧！" />

      <el-timeline v-else class="timeline">
        <el-timeline-item
          v-for="item in reviews"
          :key="item.id"
          :timestamp="item.createdAt ? item.createdAt.replace('T', ' ') : ''"
          placement="top"
          type="primary"
        >
          <el-card class="item-card shadow-hover">
            <div class="item-head">
              <span class="course-name">
                <el-icon><Collection /></el-icon> {{ item.courseName || '课程评价' }}
              </span>
              <el-rate v-model="item.rating" disabled size="small" />
            </div>
            
            <p class="content">“ {{ item.content }} ”</p>

            <div v-if="item.reply" class="reply-section">
              <el-divider border-style="dashed" />
              <div class="reply-container">
                <div class="reply-label">
                  <el-tag size="small" type="success" effect="dark">
                    匠人寄语 - {{ item.replyArtisanName || '匠人师傅' }}
                  </el-tag>
                </div>
                <div class="reply-body">
                  <p class="reply-text">{{ item.reply }}</p>
                </div>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Collection } from '@element-plus/icons-vue'
import request from '../api/request'

const reviews = ref([])

const loadMyReviews = async () => {
  try {
    // ✅ 路径必须对应后端 Controller(@RequestMapping("/api/review") + @GetMapping("/my-reviews-list"))
    // 前端 request 工具通常自带 /api，所以写 /review/my-reviews-list
    const res = await request.get('/review/my-reviews-list')
    
    if (res.success || res.code === 200) {
      reviews.value = res.data || []
    }
  } catch (error) {
    console.error("加载评价失败", error)
  }
}

// 确保在组件挂载时调用加载函数
onMounted(() => {
  loadMyReviews()
})
</script>

<style scoped>
.my-reviews-page { padding: 30px; background: #f8fafc; min-height: 100vh; }
.review-card { max-width: 900px; margin: 0 auto; border-radius: 16px; border: none; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: 800; color: #1e293b; }

.timeline { padding: 20px 10px; }
.item-card { border-radius: 12px; border: 1px solid #f1f5f9; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.course-name { font-weight: 700; color: #3b82f6; display: flex; align-items: center; gap: 6px; }
.content { color: #475569; font-size: 15px; line-height: 1.6; font-style: italic; }

/* 回复区域样式：保留你原本漂亮的样式 */
.reply-section { margin-top: 15px; }
.reply-container { background: #f0fdf4; padding: 12px; border-radius: 8px; border-left: 4px solid #22c55e; }
.reply-label { margin-bottom: 8px; }
.reply-text { color: #166534; font-size: 14px; line-height: 1.5; font-weight: 500; }

.shadow-soft { box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05) !important; }
</style>