<template>
  <div class="login-page">
    <div class="page-glow glow-left"></div>
    <div class="page-glow glow-right"></div>

    <div class="login-shell">
      <section class="brand-panel">
        <div class="brand-kicker">平台入口</div>
        <h1 class="brand-title">传统手工艺研学与溯源平台</h1>
        <p class="brand-desc">
          面向课程组织、作品建档与公开溯源展示的综合管理平台，支持将研学活动、工艺制作过程与成果留存串联为完整闭环。
        </p>

        <div class="feature-list">
          <article class="feature-card">
            <div class="feature-icon">
              <el-icon><Collection /></el-icon>
            </div>
            <div>
              <div class="feature-title">课程组织闭环</div>
              <p>覆盖课程发布、报名支付、签到评价与学生成果留存。</p>
            </div>
          </article>

          <article class="feature-card">
            <div class="feature-icon">
              <el-icon><Files /></el-icon>
            </div>
            <div>
              <div class="feature-title">作品档案建档</div>
              <p>记录材料来源、关键工序、制作图片与公开查询信息。</p>
            </div>
          </article>

          <article class="feature-card">
            <div class="feature-icon">
              <el-icon><Monitor /></el-icon>
            </div>
            <div>
              <div class="feature-title">公开溯源展示</div>
              <p>支持编号查询、二维码访问与图像辅助检索等展示方式。</p>
            </div>
          </article>
        </div>

        <div class="brand-footer">
          <span class="brand-footer-label">适用角色</span>
          <div class="role-tags">
            <span>管理员</span>
            <span>匠人</span>
            <span>教师</span>
            <span>学生</span>
          </div>
        </div>
      </section>

      <section class="form-panel">
        <div class="form-card">
          <div class="form-kicker">账号登录</div>
          <h2 class="form-title">欢迎进入平台</h2>
          <p class="form-subtitle">请输入账号和密码后进入对应工作台。</p>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            class="login-form"
            @keydown.enter.prevent="onSubmit"
          >
            <el-form-item label="账号" prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名或邮箱"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入登录密码"
                :prefix-icon="Lock"
                show-password
                size="large"
              />
            </el-form-item>

            <div class="form-row">
              <el-checkbox v-model="remember">记住账号</el-checkbox>
              <el-button type="primary" link @click="goRegister">新用户注册</el-button>
            </div>

            <el-button
              type="primary"
              class="login-button"
              :loading="loading"
              @click="onSubmit"
            >
              登录系统
            </el-button>
          </el-form>

          <p class="form-note">登录后将根据用户角色自动进入对应业务模块。</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Collection, Files, Lock, Monitor, User } from '@element-plus/icons-vue'
import request from '../api/request'
import { setUser } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)
const remember = ref(true)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function persistLogin(res) {
  setUser(res)
  if (remember.value) {
    localStorage.setItem('remember_username', form.username)
  } else {
    localStorage.removeItem('remember_username')
  }
}

const onSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    loading.value = true
    const res = await request.post('/auth/login', {
      username: form.username,
      password: form.password
    })

    persistLogin(res)
    ElMessage.success('登录成功')

    window.setTimeout(() => {
      router.replace(route.query.redirect ? String(route.query.redirect) : '/app/home')
    }, 300)
  } catch (error) {
    console.warn('login failed:', error?.message || error)
  } finally {
    loading.value = false
  }
}

const goRegister = () => router.push('/register')

onMounted(() => {
  const saved = localStorage.getItem('remember_username')
  if (saved) {
    form.username = saved
  }
})
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(217, 175, 114, 0.18), transparent 24%),
    linear-gradient(135deg, #f5efe6 0%, #eef4fb 48%, #f8fbff 100%);
}

.login-page::before {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(115deg, rgba(255, 255, 255, 0.78) 0%, rgba(255, 255, 255, 0.46) 36%, rgba(255, 255, 255, 0.74) 100%);
}

.page-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(32px);
  opacity: 0.48;
}

.glow-left {
  width: 360px;
  height: 360px;
  left: -140px;
  top: -100px;
  background: rgba(176, 208, 247, 0.76);
}

.glow-right {
  width: 300px;
  height: 300px;
  right: 4%;
  bottom: 4%;
  background: rgba(228, 192, 138, 0.3);
}

.login-shell {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 28px;
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) 450px;
  align-items: center;
  gap: 36px;
}

.brand-panel {
  position: relative;
  overflow: hidden;
  padding: 42px 38px;
  border-radius: 34px;
  color: #f9f5ee;
  background:
    radial-gradient(circle at top right, rgba(247, 216, 167, 0.18), transparent 30%),
    linear-gradient(140deg, #0f2338 0%, #17324d 48%, #112944 100%);
  box-shadow: 0 28px 72px rgba(16, 28, 53, 0.22);
}

.brand-panel::after {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.05), transparent 56%),
    radial-gradient(circle at bottom left, rgba(255, 255, 255, 0.05), transparent 28%);
  pointer-events: none;
}

.brand-kicker {
  display: inline-flex;
  align-items: center;
  padding: 7px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  color: #f5d6a7;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 2px;
}

.brand-title {
  margin: 22px 0 14px;
  font-size: 42px;
  line-height: 1.18;
  font-weight: 900;
  letter-spacing: 1px;
}

.brand-desc {
  margin: 0;
  max-width: 560px;
  color: rgba(249, 245, 238, 0.78);
  font-size: 16px;
  line-height: 1.9;
}

.feature-list {
  margin-top: 34px;
  display: grid;
  gap: 16px;
}

.feature-card {
  display: grid;
  grid-template-columns: 54px minmax(0, 1fr);
  gap: 16px;
  align-items: center;
  padding: 18px 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
}

.feature-icon {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, rgba(251, 222, 171, 0.94), rgba(224, 176, 95, 0.92));
  color: #233651;
  font-size: 24px;
  box-shadow: 0 12px 24px rgba(199, 147, 61, 0.18);
}

.feature-title {
  font-size: 17px;
  font-weight: 800;
  color: #fff2de;
}

.feature-card p {
  margin: 6px 0 0;
  color: rgba(249, 245, 238, 0.76);
  line-height: 1.7;
  font-size: 13px;
}

.brand-footer {
  margin-top: 32px;
  padding-top: 22px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.brand-footer-label {
  font-size: 13px;
  color: rgba(249, 245, 238, 0.72);
}

.role-tags {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.role-tags span {
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: #fff1d6;
  font-size: 13px;
}

.form-panel {
  display: flex;
  justify-content: center;
}

.form-card {
  width: 100%;
  padding: 40px 34px 32px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(29, 52, 84, 0.12);
  backdrop-filter: blur(18px);
}

.form-kicker {
  color: #b88a48;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 2px;
}

.form-title {
  margin: 16px 0 8px;
  font-size: 38px;
  line-height: 1.1;
  color: #16243a;
  font-weight: 900;
}

.form-subtitle {
  margin: 0 0 34px;
  color: #7b8798;
  font-size: 14px;
}

.login-form :deep(.el-form-item__label) {
  margin-bottom: 9px !important;
  font-size: 14px;
  font-weight: 700;
  color: #495a73;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 54px;
  padding: 0 16px;
  border-radius: 16px;
  background: #f7f9fc;
  border: 1px solid #dde5ee;
  box-shadow: none !important;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: #c8d5e3;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  border-color: #27496d;
  box-shadow: 0 0 0 4px rgba(39, 73, 109, 0.08) !important;
}

.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 10px 0 26px;
}

.login-button {
  width: 100%;
  height: 56px;
  border: none;
  border-radius: 18px;
  background: linear-gradient(135deg, #16243a 0%, #243a5a 100%);
  box-shadow: 0 18px 28px rgba(22, 36, 58, 0.18);
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 4px;
}

.login-button:hover {
  background: linear-gradient(135deg, #20314c 0%, #314d74 100%);
}

.form-note {
  margin: 22px 0 0;
  text-align: center;
  color: #92a0b3;
  font-size: 12px;
  line-height: 1.7;
}

@media (max-width: 1080px) {
  .login-shell {
    grid-template-columns: 1fr;
    max-width: 560px;
  }

  .brand-panel {
    padding: 30px 28px;
  }

  .brand-title {
    font-size: 32px;
  }
}

@media (max-width: 640px) {
  .login-page {
    background: linear-gradient(180deg, #f5efe6 0%, #eef4fb 100%);
  }

  .login-shell {
    padding: 18px 14px;
    gap: 18px;
  }

  .brand-panel {
    padding: 24px 20px;
    border-radius: 26px;
  }

  .feature-list {
    margin-top: 24px;
  }

  .feature-card {
    grid-template-columns: 44px minmax(0, 1fr);
    padding: 14px;
    border-radius: 18px;
  }

  .feature-icon {
    width: 44px;
    height: 44px;
    border-radius: 14px;
    font-size: 20px;
  }

  .form-card {
    padding: 30px 22px 24px;
    border-radius: 24px;
  }

  .form-title {
    font-size: 30px;
  }

  .form-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
