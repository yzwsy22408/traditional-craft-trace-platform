<template>
  <el-card>
    <h2>注册</h2>

    <el-form :model="form" ref="form" label-width="100px" class="register-form">
      <el-form-item label="用户名" :rules="[{ required: true, message: '请输入用户名', trigger: 'blur' }]">
        <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
      </el-form-item>

      <el-form-item label="密码" :rules="[{ required: true, message: '请输入密码', trigger: 'blur' }]">
        <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
      </el-form-item>

      <el-form-item label="确认密码" :rules="[{ required: true, message: '请确认密码', trigger: 'blur' }]">
        <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
      </el-form-item>

      <el-form-item label="角色" :rules="[{ required: true, message: '请选择角色', trigger: 'change' }]">
        <el-select v-model="form.role" placeholder="请选择角色">
          <el-option label="学生" value="student"></el-option>
          <el-option label="匠人" value="handicraft"></el-option>
        </el-select>
        <div class="role-tip">管理员账号由系统统一分配，前台注册仅开放学生和匠人。</div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitRegister" :loading="loading">注册</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
import { registerApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const ALLOWED_ROLES = ['student', 'handicraft']

export default {
  data() {
    return {
      form: {
        username: '',
        password: '',
        confirmPassword: '',
        role: 'student'
      },
      loading: false
    }
  },
  methods: {
    async submitRegister() {
      if (this.form.password !== this.form.confirmPassword) {
        ElMessage.error('两次密码输入不一致')
        return
      }

      if (!ALLOWED_ROLES.includes(this.form.role)) {
        ElMessage.error('当前注册入口不支持该角色')
        return
      }

      this.loading = true
      try {
        const payload = {
          username: this.form.username,
          password: this.form.password,
          role: this.form.role
        }
        const response = await registerApi(payload)

        if (response.success) {
          ElMessage.success('注册成功，请登录')
          this.$router.push('/login')
        } else {
          ElMessage.error(response.message || '注册失败，请重试')
        }
      } catch (error) {
        ElMessage.error(error?.response?.data?.message || '注册失败，请稍后再试')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.register-form {
  max-width: 400px;
  margin: 0 auto;
}

.role-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}
</style>
