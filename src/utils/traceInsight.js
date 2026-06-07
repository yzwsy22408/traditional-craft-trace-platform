function parseArray(value) {
  if (!value) return []
  if (Array.isArray(value)) return value.map((item) => String(item).trim()).filter(Boolean)

  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed.map((item) => String(item).trim()).filter(Boolean) : []
  } catch {
    return String(value)
      .split(/,|，|\r?\n/)
      .map((item) => item.trim())
      .filter(Boolean)
  }
}

function normalizeStep(step) {
  const materials = Array.isArray(step?._materials) ? step._materials : parseArray(step?.materials)
  const images = Array.isArray(step?._images) ? step._images : parseArray(step?.images)
  const detail = String(step?.detail || '').trim()
  const status = String(step?.status || '')
  const operateTime = String(step?.operateTime || step?.__displayOperateTime || '').trim()

  return {
    ...step,
    _materials: materials,
    _images: images,
    _detail: detail,
    _status: status,
    _operateTime: operateTime
  }
}

function pushIf(arr, value) {
  if (value && !arr.includes(value)) arr.push(value)
}

export function buildTraceInsight({ steps = [], verifyResult = null } = {}) {
  const normalized = (steps || []).map(normalizeStep)
  const total = normalized.length

  if (!total) {
    return {
      score: 0,
      level: '待建设',
      tagType: 'info',
      completionRate: 0,
      evidenceRate: 0,
      mediaRate: 0,
      timelineRate: 0,
      strengths: [],
      risks: ['当前作品还没有录入任何工序节点。'],
      suggestions: ['先补齐基础工序，再逐步为每个步骤增加说明和证据材料。']
    }
  }

  const completed = normalized.filter((item) => item._status === 'COMPLETED').length
  const detailReady = normalized.filter((item) => item._detail).length
  const materialReady = normalized.filter((item) => item._materials.length > 0).length
  const imageReady = normalized.filter((item) => item._images.length > 0).length
  const evidenceReady = normalized.filter((item) => item._detail && (item._materials.length || item._images.length)).length
  const timeReady = normalized.filter((item) => item._operateTime).length
  const uniqueMaterials = new Set(normalized.flatMap((item) => item._materials)).size

  const completionRate = Math.round((completed / total) * 100)
  const evidenceRate = Math.round((evidenceReady / total) * 100)
  const mediaRate = Math.round((imageReady / total) * 100)
  const timelineRate = Math.round((timeReady / total) * 100)

  const completionScore = Math.round(completionRate * 0.25)
  const evidenceScore = Math.round(evidenceRate * 0.30)
  const mediaScore = Math.round(mediaRate * 0.15)
  const timelineScore = Math.round(timelineRate * 0.10)
  const materialScore = Math.min(10, uniqueMaterials >= total ? 10 : uniqueMaterials * 2)
  const verifyScore = verifyResult?.ok ? 10 : (verifyResult ? 2 : 6)
  const score = Math.max(0, Math.min(100, completionScore + evidenceScore + mediaScore + timelineScore + materialScore + verifyScore))

  let level
  let tagType
  if (score >= 85) {
    level = '优秀档案'
    tagType = 'success'
  } else if (score >= 70) {
    level = '良好档案'
    tagType = 'primary'
  } else if (score >= 50) {
    level = '基础可用'
    tagType = 'warning'
  } else {
    level = '待完善'
    tagType = 'danger'
  }

  const strengths = []
  if (verifyResult?.ok) pushIf(strengths, '哈希链校验通过，档案一致性较好')
  if (completionRate >= 80) pushIf(strengths, '多数工序已完成，流程闭环较完整')
  if (evidenceRate >= 70) pushIf(strengths, '步骤说明与证据覆盖较充分')
  if (imageReady >= Math.ceil(total * 0.6)) pushIf(strengths, '图片证据较丰富，便于查看关键工序')
  if (uniqueMaterials >= Math.max(3, Math.ceil(total / 2))) pushIf(strengths, '材料信息较细，能体现制作过程')

  const risks = []
  if (verifyResult && !verifyResult.ok) pushIf(risks, `哈希链在第 ${verifyResult.brokenAtStepNo || '?'} 步附近存在异常，需要人工复核`)
  if (completionRate < 60) pushIf(risks, '未完成工序较多，当前档案完整度偏弱')
  if (evidenceRate < 50) pushIf(risks, '证据覆盖不足，容易被视为只有流程没有支撑材料')
  if (imageReady === 0) pushIf(risks, '缺少图片证据，展示表现力偏弱')
  if (timeReady < total) pushIf(risks, '部分节点缺少时间记录，时间链条完整性不足')

  const suggestions = []
  if (completionRate < 100) pushIf(suggestions, '优先补齐未完成工序的状态，保证整条工序链闭环')
  if (detailReady < total) pushIf(suggestions, '为缺少说明的节点补充工艺描述，避免时间线只有标题没有内容')
  if (materialReady < Math.ceil(total * 0.7)) pushIf(suggestions, '增加材料清单，让每个关键步骤都能看到用料信息')
  if (imageReady < Math.ceil(total * 0.6)) pushIf(suggestions, '补充步骤图片，至少为关键节点提供一张可展示证据图')
  if (verifyResult && !verifyResult.ok) pushIf(suggestions, '执行一次 Hash 链重建并复核异常节点，提升链式可信度')

  return {
    score,
    level,
    tagType,
    completionRate,
    evidenceRate,
    mediaRate,
    timelineRate,
    strengths: strengths.slice(0, 4),
    risks: risks.slice(0, 4),
    suggestions: suggestions.slice(0, 5)
  }
}
