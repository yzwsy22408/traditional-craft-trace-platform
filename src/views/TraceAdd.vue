<template>
  <div class="page">
    <el-card class="panel-card" shadow="never">
      <div class="head">
        <div>
          <div class="title">新增溯源工序</div>
          <div class="desc">为指定工艺品补充新的工序节点，记录状态、材料与图像证据，完善可公开展示的溯源链路。</div>
        </div>
      </div>

      <el-alert
        class="guide-alert"
        type="info"
        show-icon
        :closable="false"
        title="请优先录入材料、图片和状态都较完整的工序节点。"
        description="完整工序链可提升公开溯源页的信息密度与展示质量。"
      />

      <el-form :model="form" label-width="110px" class="form">
        <el-form-item label="选择工艺品">
          <el-select
            v-model="form.craftItemId"
            placeholder="请选择工艺品"
            style="width: 100%"
            filterable
            @change="onCraftChange"
          >
            <el-option
              v-for="item in craftList"
              :key="item.id"
              :label="`${item.name}（${item.code}）`"
              :value="item.id"
            />
          </el-select>
          <div v-if="maxStepNo !== null" class="tip">
            当前最大步骤号为 {{ maxStepNo }}，建议下一步使用 {{ maxStepNo + 1 }}。
          </div>
        </el-form-item>

        <el-row :gutter="18">
          <el-col :xs="24" :md="12">
            <el-form-item label="步骤序号">
              <el-input-number v-model="form.stepNo" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="工序状态">
              <el-radio-group v-model="form.status">
                <el-radio-button label="NOT_STARTED">未开始</el-radio-button>
                <el-radio-button label="IN_PROGRESS">进行中</el-radio-button>
                <el-radio-button label="COMPLETED">已完成</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="步骤名称">
          <el-input v-model="form.stepName" placeholder="例如：染线、织造、缝制成型" />
        </el-form-item>

        <el-form-item label="详细描述">
          <el-input
            v-model="form.detail"
            type="textarea"
            :rows="4"
            placeholder="请描述本工序的核心操作、工艺特点或展示重点"
          />
        </el-form-item>

        <el-form-item label="使用材料">
          <el-select
            v-model="form.selectedMaterials"
            multiple
            filterable
            placeholder="请选择本工序使用到的材料"
            style="width: 100%"
          >
            <el-option
              v-for="item in materialList"
              :key="item.id"
              :label="`${item.name}（库存：${item.stock}）`"
              :value="item.name"
            />
          </el-select>
          <div class="tip">提交后后端会按业务规则联动材料库存，建议只勾选当前步骤真实用到的材料。</div>
        </el-form-item>

        <el-form-item label="图片链接">
          <el-input
            v-model="imagesText"
            type="textarea"
            :rows="3"
            placeholder="支持填写多张图片链接，使用逗号或换行分隔"
          />
          <div class="tip">图片可作为步骤证据在公开溯源页中展示。</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">提交工序</el-button>
          <el-button @click="reset">清空表单</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addTraceStepApi, listTraceStepApi } from '../api/trace'
import { listCraftApi } from '../api/craft'
import { materialApi } from '../api/material'

const craftList = ref([])
const materialList = ref([])
const submitting = ref(false)
const maxStepNo = ref(null)
const imagesText = ref('')

const form = reactive({
  craftItemId: null,
  stepNo: 1,
  stepName: '',
  status: 'NOT_STARTED',
  detail: '',
  selectedMaterials: []
})

onMounted(async () => {
  try {
    craftList.value = await listCraftApi()
    materialList.value = await materialApi.list()
  } catch (e) {
    ElMessage.error('初始化列表失败')
  }
})

async function onCraftChange(craftItemId) {
  if (!craftItemId) return
  try {
    const steps = await listTraceStepApi(craftItemId)
    const max = steps.length ? Math.max(...steps.map(item => Number(item.stepNo || 0))) : 0
    maxStepNo.value = max
    form.stepNo = max + 1
  } catch (e) {
    maxStepNo.value = null
  }
}

const submit = async () => {
  if (!form.craftItemId || !form.stepName?.trim()) {
    ElMessage.warning('请先选择工艺品并填写步骤名称')
    return
  }

  const imageList = imagesText.value
    ? imagesText.value
      .split(/[\n,，]/)
      .map(item => item.trim())
      .filter(Boolean)
    : []

  const payload = {
    ...form,
    materials: form.selectedMaterials.length ? JSON.stringify(form.selectedMaterials) : null,
    images: imageList.length ? JSON.stringify(imageList) : null
  }

  submitting.value = true
  try {
    await addTraceStepApi(payload)
    ElMessage.success('溯源工序已新增，材料库存也已同步更新')
    const currentCraftId = form.craftItemId
    reset()
    form.craftItemId = currentCraftId
    await onCraftChange(currentCraftId)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '工序提交失败')
  } finally {
    submitting.value = false
  }
}

const reset = () => {
  form.stepNo = maxStepNo.value != null ? maxStepNo.value + 1 : 1
  form.stepName = ''
  form.status = 'NOT_STARTED'
  form.detail = ''
  form.selectedMaterials = []
  imagesText.value = ''
}
</script>

<style scoped>
.page {
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
}

.panel-card {
  max-width: 860px;
  margin: 0 auto;
  border-radius: 16px;
  border: none;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);
}

.head {
  margin-bottom: 12px;
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

.guide-alert {
  margin-bottom: 18px;
}

.tip {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: #64748b;
}

@media (max-width: 768px) {
  .page {
    padding: 16px;
  }
}
</style>
