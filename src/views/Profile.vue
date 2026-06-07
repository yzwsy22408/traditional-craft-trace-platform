<template>
  <div class="profile-page">
    <el-card class="profile-card" shadow="never">
      <div class="profile-header">
        <span class="title">个人设置</span>
        <el-tag size="small" effect="plain" round>{{ roleText }}</el-tag>
      </div>

      <div class="profile-body">
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            action="/api/upload" 
            name="file"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar-img" @error="handleImgError" />
            <el-icon v-else class="avatar-icon"><Plus /></el-icon>
            
            <div class="hover-mask">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </el-upload>
          <p class="hint">点击圆圈上传 JPG/PNG 格式图片，不超过 2MB</p>
        </div>

        <el-form :model="userForm" label-width="100px" class="info-form">
          <el-form-item label="用户账号">
            <el-input v-model="userForm.username" disabled />
          </el-form-item>
          <el-form-item label="用户 UID">
            <el-input v-model="userForm.uid" placeholder="UID 未获取" disabled />
          </el-form-item>
          <el-form-item label="身份角色">
            <el-input :value="roleText" disabled />
          </el-form-item>
          
          <el-divider content-position="left">账户操作</el-divider>
          <el-form-item label="密码管理">
            <el-button type="primary" plain round @click="handleEditPassword">修改登录密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Camera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { getUser, setUser } from '../utils/auth'

const userForm = ref({ uid: '', username: '', role: '', avatar: '' })

const roleText = computed(() => {
  const r = (userForm.value.role || '').toLowerCase()
  if (r === 'admin') return '系统管理员'
  if (r === 'handicraft') return '非遗匠人'
  return '研学学员'
})

// 加载用户信息
const loadUserInfo = () => {
  const data = getUser()
  if (!data) return

  userForm.value = {
    uid: data.id || '',
    username: data.username || '',
    role: data.role || '',
    avatar: data.avatar || ''
  }
}

/**
 * ✅ 核心修正：头像上传成功逻辑
 * 修正了请求路径，去掉了多余的 /api
 */
const handleUploadSuccess = async (res) => {
  // 后端 FileUploadController 返回的是 Result 对象，地址在 res.data
  const avatarPath = res.data 
  
  if (!avatarPath) {
    return ElMessage.error('上传失败：未获取到服务器路径')
  }

  try {
    // ✅ 修复：这里改为 /auth/update-avatar，不再重复写 /api
    const updateRes = await request.post('/auth/update-avatar', { 
      avatar: avatarPath 
    })
    
    // 如果后端返回的是 Result 对象，检查其 success 字段
    if (updateRes.success || updateRes.code === 200 || updateRes === "头像更新成功") {
      // 1. 更新页面显示
      userForm.value.avatar = avatarPath
      
      // 2. 更新本地缓存，让顶栏同步
      const currentUser = getUser()
      if (currentUser) {
        setUser({
          ...currentUser,
          avatar: avatarPath
        })
      }
      
      ElMessage.success('头像更换成功！')
    }
  } catch (e) {
    console.error("更新数据库失败:", e)
    ElMessage.error('头像数据库记录保存失败')
  }
}

const handleUploadError = (err) => {
  console.error("上传失败:", err)
  ElMessage.error('连接上传接口失败')
}

const handleImgError = () => {
  ElMessage.warning('预览图加载异常，请检查静态资源配置')
}

const beforeAvatarUpload = (file) => {
  const isType = ['image/jpeg', 'image/png'].includes(file.type)
  if (!isType) ElMessage.error('格式不支持!')
  return isType
}

const handleEditPassword = () => ElMessage.info('功能开发中...')

onMounted(loadUserInfo)
</script>

<style scoped>
.profile-page { padding: 40px; display: flex; justify-content: center; background: #f8fafc; min-height: 100vh; }
.profile-card { width: 100%; max-width: 600px; border-radius: 16px; border: 1px solid #eef2f6; }
.profile-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.title { font-size: 22px; font-weight: 800; color: #1e293b; }

.avatar-section { text-align: center; margin-bottom: 40px; }
.avatar-uploader { 
  width: 120px; height: 120px; border-radius: 50%; border: 2px dashed #dbeafe; 
  overflow: hidden; margin: 0 auto; cursor: pointer; position: relative; background: #fff;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-icon { font-size: 24px; color: #94a3b8; line-height: 120px; }

.hover-mask {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.4); color: #fff; display: flex;
  flex-direction: column; justify-content: center; align-items: center;
  opacity: 0; transition: 0.3s;
}
.avatar-uploader:hover .hover-mask { opacity: 1; }
.hint { font-size: 13px; color: #94a3b8; margin-top: 12px; }

.info-form { max-width: 450px; margin: 0 auto; }
.shadow-soft { box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05); }
</style>
