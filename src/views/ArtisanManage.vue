<template>
  <div class="page">
    <el-card class="panel-card" shadow="never">
      <div class="head">
        <div>
          <div class="title">匠人管理</div>
          <div class="desc">统一维护匠人账号、联系方式与工坊归属关系，支撑课程发布、作品录入和溯源档案建设。</div>
        </div>
        <div class="actions">
          <el-button @click="fetchArtisans">刷新</el-button>
          <el-button type="primary" @click="openCreateDialog">新增匠人</el-button>
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

      <el-table :data="list" stripe style="width: 100%" v-loading="loading" empty-text="暂无匠人档案">
        <el-table-column label="ID" prop="id" width="80" />

        <el-table-column label="匠人档案" min-width="220">
          <template #default="{ row }">
            <div class="user-name">{{ row.name || '未填写姓名' }}</div>
            <div class="sub">@{{ row.username || 'handicraft-user' }}</div>
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

        <el-table-column label="工坊归属" min-width="210">
          <template #default="{ row }">
            <div v-if="artisanWorkshop(row.id)">
              <div class="user-name">{{ artisanWorkshop(row.id).name }}</div>
              <div class="sub">{{ artisanWorkshop(row.id).address || '已绑定工坊，待补充地址' }}</div>
            </div>
            <el-tag v-else type="warning" effect="plain" round>待绑定工坊</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openBindDialog(row)">绑定工坊</el-button>
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
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

      <el-dialog v-model="editDialogVisible" :title="editMode === 'create' ? '新增匠人' : '编辑匠人'" width="520px">
        <el-form :model="editForm" label-width="90px">
          <el-form-item label="用户名">
            <el-input v-model="editForm.username" :disabled="editMode === 'edit'" placeholder="例如：artisan01" />
          </el-form-item>

          <el-form-item label="密码">
            <el-input v-model="editForm.password" type="password" placeholder="新增必填；编辑可留空表示不改" />
          </el-form-item>

          <el-form-item label="姓名">
            <el-input v-model="editForm.name" placeholder="例如：王师傅" />
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
          <el-button type="primary" :loading="saving" @click="saveArtisan">
            {{ editMode === 'create' ? '创建' : '保存' }}
          </el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="bindDialogVisible" title="绑定工坊" width="540px">
        <div class="bind-head">
          <div class="bind-title">{{ bindTarget?.name || bindTarget?.username || '当前匠人' }}</div>
          <div class="bind-desc">
            {{ currentBoundWorkshop ? `当前已绑定：${currentBoundWorkshop.name}` : '当前尚未绑定工坊，可从下方工坊池中选择。' }}
          </div>
        </div>

        <el-select
          v-model="selectedWorkshopId"
          placeholder="请选择工坊"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="w in bindableWorkshops"
            :key="w.id"
            :label="formatWorkshopLabel(w)"
            :value="w.id"
          />
        </el-select>

        <div class="bind-hint">
          仅展示未绑定工坊，或当前匠人已经绑定的工坊，避免误占用其他匠人的工坊归属。
        </div>

        <template #footer>
          <el-button @click="bindDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="binding" @click="doBind">确认绑定</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageUsers, createUser, updateUser, deleteUser } from '@/api/auth'
import { workshopApi } from '@/api/workshop'

export default {
  name: 'ArtisanManage',
  data() {
    return {
      loading: false,
      list: [],
      keyword: '',
      page: 1,
      pageSize: 10,
      total: 0,
      allWorkshops: [],
      editDialogVisible: false,
      editMode: 'create',
      editForm: {
        id: null,
        username: '',
        password: '',
        name: '',
        email: '',
        phone: '',
        address: ''
      },
      saving: false,
      bindDialogVisible: false,
      bindTarget: null,
      selectedWorkshopId: null,
      binding: false
    }
  },
  computed: {
    workshopByOwner() {
      const map = new Map()
      ;(this.allWorkshops || []).forEach(item => {
        if (item?.ownerUserId != null) {
          map.set(String(item.ownerUserId), item)
        }
      })
      return map
    },

    summaryCards() {
      const artisans = this.list || []
      const boundCount = artisans.filter(item => this.workshopByOwner.has(String(item.id))).length
      const contactReady = artisans.filter(item => item.phone || item.email).length
      const namedCount = artisans.filter(item => item.name).length
      const availableWorkshops = (this.allWorkshops || []).filter(item => item?.ownerUserId == null).length

      return [
        { label: '当前页匠人', value: artisans.length, desc: '本页已加载的匠人账号数量' },
        { label: '已绑定工坊', value: boundCount, desc: '已完成工坊归属配置的匠人数' },
        { label: '待补档案', value: artisans.length - namedCount, desc: '仍未填写真实姓名的账号数' },
        { label: '已留联系方式', value: contactReady, desc: '便于联络与课程协同的匠人数量' },
        { label: '待分配工坊', value: availableWorkshops, desc: '当前尚未绑定负责人的工坊数量' }
      ]
    },

    currentBoundWorkshop() {
      const uid = this.bindTarget?.id
      if (!uid) return null
      return this.workshopByOwner.get(String(uid)) || null
    },

    bindableWorkshops() {
      const uid = this.bindTarget?.id
      return (this.allWorkshops || []).filter(item => (
        item?.ownerUserId == null || String(item.ownerUserId) === String(uid)
      ))
    }
  },
  created() {
    this.fetchArtisans()
  },
  methods: {
    artisanWorkshop(id) {
      return this.workshopByOwner.get(String(id)) || null
    },

    async fetchWorkshops() {
      try {
        const res = await workshopApi.list()
        this.allWorkshops = Array.isArray(res) ? res : (Array.isArray(res?.data) ? res.data : [])
      } catch (e) {
        this.allWorkshops = []
      }
    },

    async fetchArtisans() {
      this.loading = true
      try {
        const [userRes] = await Promise.all([
          pageUsers({
            page: this.page,
            size: this.pageSize,
            keyword: this.keyword?.trim() || ''
          }),
          this.fetchWorkshops()
        ])

        if (userRes?.success) {
          const arr = Array.isArray(userRes.list) ? userRes.list : []
          this.list = arr.filter(item => (item.role || '').toLowerCase() === 'handicraft')
          this.total = Number(userRes.total || this.list.length)
        } else {
          this.list = []
          this.total = 0
        }
      } catch (e) {
        this.list = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    onSearch() {
      this.page = 1
      this.fetchArtisans()
    },

    onResetSearch() {
      this.keyword = ''
      this.page = 1
      this.fetchArtisans()
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.page = 1
      this.fetchArtisans()
    },

    handleCurrentChange(p) {
      this.page = p
      this.fetchArtisans()
    },

    resetEditForm() {
      this.editForm = {
        id: null,
        username: '',
        password: '',
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
      this.editForm.password = ''
      this.editForm.name = row.name || ''
      this.editForm.email = row.email || ''
      this.editForm.phone = row.phone || ''
      this.editForm.address = row.address || ''
      this.editDialogVisible = true
    },

    async saveArtisan() {
      if (!this.editForm.username || !this.editForm.username.trim()) {
        ElMessage.warning('请输入用户名')
        return
      }

      if (this.editMode === 'create' && (!this.editForm.password || !this.editForm.password.trim())) {
        ElMessage.warning('新增匠人时必须填写密码')
        return
      }

      this.saving = true
      try {
        if (this.editMode === 'create') {
          await createUser({
            username: this.editForm.username.trim(),
            password: this.editForm.password.trim(),
            role: 'handicraft',
            name: this.editForm.name,
            email: this.editForm.email,
            phone: this.editForm.phone,
            address: this.editForm.address
          })
          ElMessage.success('匠人创建成功')
        } else {
          const payload = {
            role: 'handicraft',
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
        this.fetchArtisans()
      } finally {
        this.saving = false
      }
    },

    async handleDelete(id) {
      try {
        await ElMessageBox.confirm('确定删除该匠人档案吗？删除后相关课程与工坊归属需要重新检查。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deleteUser(id)
        ElMessage.success('删除成功')
        if (this.list.length <= 1 && this.page > 1) {
          this.page -= 1
        }
        this.fetchArtisans()
      } catch (e) {}
    },

    formatWorkshopLabel(workshop) {
      const owner = workshop?.ownerUserId
      const suffix = owner == null
        ? '（待绑定）'
        : (String(owner) === String(this.bindTarget?.id) ? '（当前已绑定）' : '（已绑定其他匠人）')
      return `${workshop.name}${workshop.address ? `｜${workshop.address}` : ''} ${suffix}`
    },

    async openBindDialog(row) {
      this.bindTarget = row
      this.selectedWorkshopId = null
      this.bindDialogVisible = true
      await this.fetchWorkshops()

      const bound = this.artisanWorkshop(row.id)
      if (bound) {
        this.selectedWorkshopId = bound.id
      }
    },

    async doBind() {
      if (!this.bindTarget?.id) return
      if (!this.selectedWorkshopId) {
        ElMessage.warning('请选择一个工坊')
        return
      }

      this.binding = true
      try {
        await workshopApi.assignOwner(this.selectedWorkshopId, this.bindTarget.id)
        ElMessage.success('绑定成功')
        this.bindDialogVisible = false
        await this.fetchArtisans()
      } finally {
        this.binding = false
      }
    }
  }
}
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

.bind-head {
  margin-bottom: 14px;
}

.bind-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

.bind-desc,
.bind-hint {
  margin-top: 6px;
  font-size: 13px;
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
