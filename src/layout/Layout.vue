<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <div class="logo-mark">匠</div>
        <div>
          <div class="logo-title">传统手工艺研学平台</div>
          <div class="logo-subtitle">课程组织 · 作品建档 · 公开溯源</div>
        </div>
      </div>

      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item index="/app/home">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-menu-item index="/app/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>

        <el-divider v-if="isLogin" style="margin: 8px 0; opacity: 0.2;" />

        <el-menu-item v-if="user?.role === 'admin'" index="/app/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item v-if="user?.role === 'admin'" index="/app/artisans">
          <el-icon><Avatar /></el-icon>
          <span>匠人管理</span>
        </el-menu-item>

        <el-menu-item v-if="user?.role === 'admin'" index="/app/students">
          <el-icon><Reading /></el-icon>
          <span>学员管理</span>
        </el-menu-item>

        <el-menu-item
          v-if="user?.role === 'admin' || user?.role === 'handicraft'"
          index="/app/crafts"
        >
          <el-icon><Box /></el-icon>
          <span>工艺品管理</span>
        </el-menu-item>

        <el-menu-item
          v-if="user?.role === 'admin' || user?.role === 'handicraft'"
          index="/app/materials"
        >
          <el-icon><Memo /></el-icon>
          <span>材料管理</span>
        </el-menu-item>

        <el-menu-item
          v-if="user?.role === 'handicraft' || user?.role === 'admin'"
          index="/app/traces"
        >
          <el-icon><Files /></el-icon>
          <span>溯源步骤管理</span>
        </el-menu-item>

        <el-menu-item v-if="isLogin" index="/app/query">
          <el-icon><Search /></el-icon>
          <span>溯源核验</span>
        </el-menu-item>

        <el-menu-item v-if="isLogin" index="/app/courses">
          <el-icon><Monitor /></el-icon>
          <span>公开课程</span>
        </el-menu-item>

        <el-menu-item v-if="user?.role === 'student'" index="/app/my/courses">
          <el-icon><Checked /></el-icon>
          <span>我的课程</span>
        </el-menu-item>

        <el-menu-item v-if="user?.role === 'student'" index="/app/my/reviews">
          <el-icon><ChatLineRound /></el-icon>
          <span>我的评价</span>
        </el-menu-item>

        <el-menu-item v-if="user?.role === 'student'" index="/app/my/works">
          <el-icon><Picture /></el-icon>
          <span>我的作品</span>
        </el-menu-item>

        <el-menu-item
          v-if="user?.role === 'handicraft' || user?.role === 'admin'"
          index="/app/course/manage"
        >
          <el-icon><Collection /></el-icon>
          <span>课程管理</span>
        </el-menu-item>

        <el-menu-item
          v-if="user?.role === 'handicraft' || user?.role === 'admin'"
          index="/app/teachers"
        >
          <el-icon><School /></el-icon>
          <span>老师管理</span>
        </el-menu-item>

        <el-menu-item
          v-if="user?.role === 'handicraft' || user?.role === 'admin'"
          index="/app/workshop/manage"
        >
          <el-icon><OfficeBuilding /></el-icon>
          <span>工坊管理</span>
        </el-menu-item>

        <el-menu-item
          v-if="user?.role === 'handicraft' || user?.role === 'admin'"
          index="/app/review/manage"
        >
          <el-icon><Comment /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="header-title">传统手工艺研学与溯源平台</span>
        </div>

        <div class="header-right">
          <el-avatar
            v-if="user?.avatar"
            :size="28"
            :src="user.avatar"
            style="margin-right: 8px; border: 1px solid #eee;"
          />
          <span class="user-info">
            {{ user?.username }}（{{ roleText }}）
          </span>

          <el-button
            type="danger"
            size="small"
            @click="logout"
            style="margin-left: 12px;"
            round
          >
            退出
          </el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  User,
  UserFilled,
  House,
  Avatar,
  Box,
  Memo,
  Files,
  Search,
  Monitor,
  Checked,
  Collection,
  OfficeBuilding,
  Reading,
  ChatLineRound,
  School,
  Comment,
  Picture
} from '@element-plus/icons-vue'
import request from '../api/request'
import { ElMessage } from 'element-plus'
import { clearUser, getUser, onAuthChange } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const user = ref(null)

function syncUser() {
  user.value = getUser()
}

const isLogin = computed(() => !!user.value)

const roleText = computed(() => {
  if (!user.value) return '游客'
  const r = String(user.value.role || '').toLowerCase()
  if (r === 'admin') return '管理员'
  if (r === 'handicraft') return '匠人'
  if (r === 'student') return '学生'
  return r || '未知角色'
})

watch(
  () => route.fullPath,
  () => {
    syncUser()
    if (user.value && !user.value.role) {
      clearUser()
      ElMessage.error('登录信息异常，请重新登录')
      router.push('/login')
    }
  },
  { immediate: true }
)

let stopAuthSync = () => {}

onMounted(() => {
  stopAuthSync = onAuthChange(syncUser)
  syncUser()
})

onBeforeUnmount(() => {
  stopAuthSync()
})

const logout = async () => {
  try {
    await request.post('/auth/logout')
  } catch (e) {
    console.error('调用退出接口失败', e)
  }

  clearUser()
  user.value = null
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(223, 190, 144, 0.22), transparent 24%),
    linear-gradient(180deg, #f7f3ed 0%, #f5f8fc 18%, #eef4fb 100%);
}
.aside {
  background: linear-gradient(180deg, #0f2235 0%, #112a42 48%, #163654 100%);
  color: #fff;
  box-shadow: 14px 0 40px rgba(15, 23, 42, 0.12);
  z-index: 10;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}
.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 84px;
  padding: 0 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0.01));
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.logo-mark {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #f7d8a7 0%, #d7a663 100%);
  color: #0f2235;
  font-weight: 900;
  letter-spacing: 1px;
  box-shadow: 0 10px 24px rgba(215, 166, 99, 0.35);
}
.logo-title {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.6px;
}
.logo-subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.62);
}
.menu {
  border-right: none;
  padding: 14px 10px 20px;
}
:deep(.el-menu) {
  background-color: transparent;
  border: none;
}
:deep(.el-menu-item) {
  height: 46px;
  margin: 6px 0;
  border-radius: 14px;
  color: rgba(255, 255, 255, 0.72);
  font-weight: 600;
}
:deep(.el-menu-item .el-icon) {
  font-size: 18px;
}
:deep(.el-menu-item.is-active) {
  color: #fff;
  background: linear-gradient(90deg, #2d8cf0 0%, #57a8ff 100%) !important;
  box-shadow: 0 14px 28px rgba(45, 140, 240, 0.26);
}
:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.08);
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
  padding: 0 24px;
  box-shadow: 0 8px 26px rgba(15, 23, 42, 0.04);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-kicker {
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.08);
  color: #2563eb;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 1.4px;
}
.header-title {
  font-size: 16px;
  font-weight: 800;
  color: #22324a;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  font-size: 14px;
  color: #64748b;
  font-weight: 600;
}
.main {
  background: transparent;
  padding: 0;
}
</style>
