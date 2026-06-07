<template>
  <div class="page">
    <el-card class="panel-card" shadow="never">
      <div class="head">
        <div>
          <div class="title">用户管理</div>
          <div class="desc">管理员可统一维护平台账号、角色分配与基础联系方式，用于支撑课程和工坊业务协同。</div>
        </div>
        <div class="actions">
          <el-button @click="fetchUsers">刷新</el-button>
          <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
        </div>
      </div>

      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索：用户名 / 姓名 / 角色"
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

      <el-table :data="users" stripe style="width: 100%" v-loading="loading" empty-text="暂无用户数据">
        <el-table-column label="ID" prop="id" width="80" />

        <el-table-column label="用户档案" min-width="220">
          <template #default="{ row }">
            <div class="user-name">{{ row.username || '未命名账号' }}</div>
            <div class="sub">姓名：{{ row.name || '未填写' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="角色" prop="role" width="140">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" effect="light" round>{{ row.role || 'student' }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="邮箱" prop="email" min-width="200">
          <template #default="{ row }">
            {{ row.email || '未填写' }}
          </template>
        </el-table-column>

        <el-table-column label="电话" prop="phone" min-width="160">
          <template #default="{ row }">
            {{ row.phone || '未填写' }}
          </template>
        </el-table-column>

        <el-table-column label="地址" prop="address" min-width="220">
          <template #default="{ row }">
            <div class="address-text">{{ row.address || '未填写地址' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" @click="openRoleDialog(row)">分配角色</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
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
          :page-sizes="[5, 10, 20, 50]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <el-dialog v-model="editDialogVisible" :title="editMode === 'create' ? '新增用户' : '编辑用户'" width="520px">
        <el-form :model="editForm" label-width="90px">
          <el-form-item label="用户名">
            <el-input v-model="editForm.username" :disabled="editMode === 'edit'" placeholder="例如：admin2 / user1" />
          </el-form-item>

          <el-form-item label="密码">
            <el-input v-model="editForm.password" type="password" placeholder="新增必填；编辑可留空表示不改" />
          </el-form-item>

          <el-form-item label="姓名">
            <el-input v-model="editForm.name" placeholder="例如：张三" />
          </el-form-item>

          <el-form-item label="角色">
            <el-select v-model="editForm.role" placeholder="请选择角色" style="width: 100%">
              <el-option label="学生" value="student" />
              <el-option label="匠人" value="handicraft" />
              <el-option label="管理员" value="admin" />
            </el-select>
          </el-form-item>

          <el-form-item label="邮箱">
            <el-input v-model="editForm.email" placeholder="example@email.com" />
          </el-form-item>

          <el-form-item label="电话">
            <el-input v-model="editForm.phone" placeholder="手机号 / 联系电话" />
          </el-form-item>

          <el-form-item label="地址">
            <el-input v-model="editForm.address" placeholder="联系地址" />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveUser">
            {{ editMode === 'create' ? '创建' : '保存' }}
          </el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="roleDialogVisible" title="分配角色" width="360px">
        <div class="role-title">用户：{{ selectedUsername }}</div>

        <el-select v-model="newRole" placeholder="请选择角色" style="width: 100%">
          <el-option label="学生" value="student" />
          <el-option label="匠人" value="handicraft" />
          <el-option label="管理员" value="admin" />
        </el-select>

        <template #footer>
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="savingRole" @click="assignRole">确认</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageUsers, createUser, updateUser, deleteUser, assignRoleToUser } from '../api/auth'

export default {
  name: 'UserManage',
  data() {
    return {
      users: [],
      loading: false,
      keyword: '',
      page: 1,
      pageSize: 10,
      total: 0,
      editDialogVisible: false,
      editMode: 'create',
      editForm: {
        id: null,
        username: '',
        password: '',
        role: 'student',
        name: '',
        email: '',
        phone: '',
        address: ''
      },
      saving: false,
      roleDialogVisible: false,
      selectedUserId: null,
      selectedUsername: '',
      newRole: '',
      savingRole: false
    }
  },
  computed: {
    summaryCards() {
      const list = this.users || []
      const admins = list.filter(item => item.role === 'admin').length
      const artisans = list.filter(item => item.role === 'handicraft').length
      const students = list.filter(item => !item.role || item.role === 'student').length
      const withContact = list.filter(item => item.phone || item.email).length

      return [
        { label: '当前页用户', value: list.length, desc: '当前列表页已加载的账号数量' },
        { label: '管理员', value: admins, desc: '负责平台全局管理的账号数' },
        { label: '匠人', value: artisans, desc: '参与工坊和课程内容生产的账号数' },
        { label: '学生', value: students, desc: '参与研学课程体验的账号数' },
        { label: '已留联系方式', value: withContact, desc: '已填写邮箱或电话的账号数' }
      ]
    }
  },
  created() {
    this.fetchUsers()
  },
  methods: {
    roleTagType(role) {
      if (role === 'admin') return 'danger'
      if (role === 'handicraft') return 'warning'
      return 'success'
    },

    async fetchUsers() {
      this.loading = true
      try {
        const res = await pageUsers({
          page: this.page,
          size: this.pageSize,
          keyword: this.keyword?.trim() || ''
        })

        if (res && res.success) {
          this.users = Array.isArray(res.list) ? res.list : []
          this.total = Number(res.total || 0)
        } else {
          this.users = Array.isArray(res) ? res : []
          this.total = this.users.length
        }
      } catch (e) {
        this.users = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    onSearch() {
      this.page = 1
      this.fetchUsers()
    },

    onResetSearch() {
      this.keyword = ''
      this.page = 1
      this.fetchUsers()
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.page = 1
      this.fetchUsers()
    },

    handleCurrentChange(p) {
      this.page = p
      this.fetchUsers()
    },

    resetEditForm() {
      this.editForm = {
        id: null,
        username: '',
        password: '',
        role: 'student',
        name: '',
        email: '',
        phone: '',
        address: ''
      }
    },

    openCreateDialog() {
      this.editMode = 'create'
      this.resetEditForm()
      this.editDialogVisible = true
    },

    openEditDialog(row) {
      this.editMode = 'edit'
      this.resetEditForm()
      this.editForm.id = row.id
      this.editForm.username = row.username || ''
      this.editForm.role = row.role || 'student'
      this.editForm.name = row.name || ''
      this.editForm.email = row.email || ''
      this.editForm.phone = row.phone || ''
      this.editForm.address = row.address || ''
      this.editForm.password = ''
      this.editDialogVisible = true
    },

    async saveUser() {
      if (!this.editForm.username || !this.editForm.username.trim()) {
        ElMessage.warning('请输入用户名')
        return
      }
      if (this.editMode === 'create' && (!this.editForm.password || !this.editForm.password.trim())) {
        ElMessage.warning('新增用户必须填写密码')
        return
      }

      this.saving = true
      try {
        if (this.editMode === 'create') {
          await createUser({
            username: this.editForm.username.trim(),
            password: this.editForm.password.trim(),
            role: this.editForm.role,
            name: this.editForm.name,
            email: this.editForm.email,
            phone: this.editForm.phone,
            address: this.editForm.address
          })
          ElMessage.success('创建成功')
        } else {
          const payload = {
            role: this.editForm.role,
            name: this.editForm.name,
            email: this.editForm.email,
            phone: this.editForm.phone,
            address: this.editForm.address
          }
          if (this.editForm.password && this.editForm.password.trim()) {
            payload.password = this.editForm.password.trim()
          }

          await updateUser(this.editForm.id, payload)
          ElMessage.success('保存成功')
        }

        this.editDialogVisible = false
        this.fetchUsers()
      } finally {
        this.saving = false
      }
    },

    async handleDelete(userId) {
      try {
        await ElMessageBox.confirm('确定删除该用户吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deleteUser(userId)
        ElMessage.success('删除成功')

        if (this.users.length <= 1 && this.page > 1) {
          this.page -= 1
        }
        this.fetchUsers()
      } catch (e) {}
    },

    openRoleDialog(row) {
      this.selectedUserId = row.id
      this.selectedUsername = row.username
      this.newRole = row.role || 'student'
      this.roleDialogVisible = true
    },

    async assignRole() {
      if (!this.selectedUserId) return
      if (!this.newRole) {
        ElMessage.warning('请选择角色')
        return
      }
      this.savingRole = true
      try {
        await assignRoleToUser(this.selectedUserId, this.newRole)
        ElMessage.success('角色分配成功')
        this.roleDialogVisible = false
        this.fetchUsers()
      } finally {
        this.savingRole = false
      }
    }
  }
}
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
  align-items: flex-start;
  justify-content: space-between;
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
  line-height: 1.6;
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
  color: #94a3b8;
  line-height: 1.6;
}

.user-name {
  font-weight: 700;
  color: #1f2937;
}

.sub {
  font-size: 12px;
  color: #64748b;
  margin-top: 6px;
}

.address-text {
  color: #334155;
  line-height: 1.7;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.role-title {
  margin-bottom: 10px;
  color: #334155;
}
</style>
