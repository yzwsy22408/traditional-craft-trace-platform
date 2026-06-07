const TEACHER_STORAGE_KEY = 'crafttrace-teacher-records'
const COURSE_TEACHER_META_KEY = 'crafttrace-course-teacher-meta'
const CRAFT_TRACE_META_KEY = 'crafttrace-craft-trace-meta'

const DEFAULT_TEACHERS = [
  {
    id: 'teacher-zhuangjin-liyaqin',
    name: '李雅琴',
    schoolName: '南宁市青秀区民族中学',
    subjectName: '综合实践 / 劳动教育',
    phone: '13877182016',
    leadRoute:
      '学校集合 → 壮锦手工包研学工坊签到 → 匠人讲解壮锦纹样与配色 → 学生分组体验织锦与手工包成型 → 展柜扫码查看成品溯源',
    workshopFocus:
      '负责初中段非遗研学班带队，重点对接壮锦织造、民族纹样识别和文创成品展示课程。',
    note: '用于说明老师组织学生到工坊开展线下研学的完整业务链路。'
  },
  {
    id: 'teacher-wood-chenzhiyuan',
    name: '陈志远',
    schoolName: '东阳市实验中学',
    subjectName: '美术 / 劳动教育',
    phone: '13957941208',
    leadRoute:
      '校门集合 → 木雕基础体验工坊安全讲解 → 匠人示范线稿转印与刀法 → 学生分组雕刻 → 成品展台扫码复盘工艺流程',
    workshopFocus:
      '负责木雕基础体验研学，突出工具认知、基础刀法、安全规范和成品打磨展示。',
    note: '突出老师在现场组织分组、签到和安全提醒等管理职责。'
  },
  {
    id: 'teacher-suxiu-guwanqing',
    name: '顾婉清',
    schoolName: '苏州市平江实验学校',
    subjectName: '美术 / 非遗研学',
    phone: '13862153629',
    leadRoute:
      '校园集合 → 苏绣工艺研修工坊导览 → 匠人讲解双面绣针法与配线 → 学生体验起针收针 → 扫码查看团扇成品档案与工序记录',
    workshopFocus:
      '负责苏绣、刺绣类研学课程带队，适合展示细节工艺、针法训练与成品审美表达。',
    note: '适合串联课程体验、成果留存与溯源查询等环节。'
  },
  {
    id: 'teacher-ceramic-zhoulan',
    name: '周岚',
    schoolName: '景德镇市第十二中学',
    subjectName: '美术 / 校本研学',
    phone: '13779861142',
    leadRoute:
      '校内签到 → 青花瓷绘制展示工坊参观 → 匠人讲解构图与上色 → 学生临摹纹样 → 作品入柜展示并扫码查看烧制前后溯源信息',
    workshopFocus:
      '负责陶艺、青花瓷与器物彩绘类研学带队，适合展示图样设计、绘制和烧制展示闭环。',
    note: '可兼顾器物展示、课程成果陈列和公开二维码讲解。'
  }
]

const SPECIALITY_KEYWORDS = {
  zhuangjin: ['壮锦', '织锦', '手工包', '广西壮锦', '织造'],
  woodcarving: ['木雕', '雕刻', '木作'],
  embroidery: ['苏绣', '刺绣', '双面绣', '团扇'],
  ceramic: ['青花瓷', '陶艺', '瓷', '紫砂', '器物'],
  silver: ['银', '银饰', '苗银', '项圈', '錾刻']
}

function canUseStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'
}

function readJson(key, fallback) {
  if (!canUseStorage()) return fallback

  try {
    const raw = window.localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

function writeJson(key, value) {
  if (!canUseStorage()) return
  window.localStorage.setItem(key, JSON.stringify(value))
}

function normalizeTeacher(record = {}) {
  const id =
    String(record.id || '').trim() ||
    `teacher-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`

  return {
    id,
    name: String(record.name || '').trim(),
    schoolName: String(record.schoolName || '').trim(),
    subjectName: String(record.subjectName || '').trim(),
    phone: String(record.phone || '').trim(),
    leadRoute: String(record.leadRoute || '').trim(),
    workshopFocus: String(record.workshopFocus || '').trim(),
    note: String(record.note || '').trim()
  }
}

function normalizeCourseTeacherMeta(meta = {}) {
  return {
    teacherId: String(meta.teacherId || '').trim(),
    teacherName: String(meta.teacherName || '').trim(),
    schoolName: String(meta.schoolName || '').trim(),
    subjectName: String(meta.subjectName || '').trim(),
    leadRoute: String(meta.leadRoute || '').trim(),
    note: String(meta.note || '').trim()
  }
}

function normalizeTraceMeta(meta = {}) {
  return {
    artisanId: String(meta.artisanId || '').trim(),
    artisanName: String(meta.artisanName || '').trim(),
    artisanTitle: String(meta.artisanTitle || '').trim(),
    workshopName: String(meta.workshopName || '').trim(),
    displayLocation: String(meta.displayLocation || '').trim(),
    qrPlacement: String(meta.qrPlacement || '').trim(),
    materialSourceSummary: String(meta.materialSourceSummary || '').trim(),
    materialSources: Array.isArray(meta.materialSources)
      ? meta.materialSources.map((item) => String(item).trim()).filter(Boolean)
      : String(meta.materialSources || '')
          .split(/\r?\n|,|，/)
          .map((item) => item.trim())
          .filter(Boolean),
    traceNotice: String(meta.traceNotice || '').trim()
  }
}

function getTextFingerprint(value) {
  return String(value || '').trim().toLowerCase()
}

function containsAny(text, keywords) {
  return keywords.some((item) => text.includes(getTextFingerprint(item)))
}

function inferSpeciality(payload = {}) {
  const text = [
    payload.code,
    payload.name,
    payload.category,
    payload.description,
    payload.title,
    payload.intro
  ]
    .join(' ')
    .toLowerCase()

  if (containsAny(text, SPECIALITY_KEYWORDS.zhuangjin)) return 'zhuangjin'
  if (containsAny(text, SPECIALITY_KEYWORDS.woodcarving)) return 'woodcarving'
  if (containsAny(text, SPECIALITY_KEYWORDS.embroidery)) return 'embroidery'
  if (containsAny(text, SPECIALITY_KEYWORDS.ceramic)) return 'ceramic'
  if (containsAny(text, SPECIALITY_KEYWORDS.silver)) return 'silver'
  return ''
}

function findDefaultTeacherBySpeciality(speciality) {
  if (speciality === 'zhuangjin') return DEFAULT_TEACHERS[0]
  if (speciality === 'woodcarving') return DEFAULT_TEACHERS[1]
  if (speciality === 'embroidery') return DEFAULT_TEACHERS[2]
  if (speciality === 'ceramic' || speciality === 'silver') return DEFAULT_TEACHERS[3]
  return DEFAULT_TEACHERS[0]
}

function buildDefaultCourseTeacherMeta(course = {}) {
  const speciality = inferSpeciality(course)
  if (!speciality) return null

  const teacher = findDefaultTeacherBySpeciality(speciality)
  return normalizeCourseTeacherMeta({
    teacherId: teacher.id,
    teacherName: teacher.name,
    schoolName: teacher.schoolName,
    subjectName: teacher.subjectName,
    leadRoute: teacher.leadRoute,
    note: teacher.note
  })
}

function buildDefaultCraftTraceMeta(craft = {}) {
  const speciality = inferSpeciality(craft)
  const code = String(craft.code || '').trim().toUpperCase()

  if (code === 'CI001' || speciality === 'zhuangjin') {
    return normalizeTraceMeta({
      artisanName: '张小成师傅',
      artisanTitle: '壮锦项目负责匠人',
      workshopName: '壮锦手工包研学工坊',
      displayLocation: '非遗研学展示区 A-01 壮锦成品展柜',
      qrPlacement: '二维码张贴在展柜铭牌与作品说明卡右下角，学生和评委可现场扫码查看完整溯源。',
      materialSourceSummary:
        '主要材料为广西本地壮锦织片、棉布底衬、手缝棉线与包边辅料，均由壮锦工坊统一备料并在制作前完成规格检查。',
      materialSources: [
        '主料壮锦织片来自广西本地壮锦织造材料库',
        '棉布底衬由工坊常备辅料区统一裁配',
        '手缝棉线与包边辅料由课程备料台按班次发放',
        '图样描线纸与定位工具由研学教学包统一配置'
      ],
      traceNotice: '该作品用于展示壮锦纹样识别、配色应用与文创成品转化过程。'
    })
  }

  if (speciality === 'woodcarving') {
    return normalizeTraceMeta({
      artisanName: '黄文林师傅',
      artisanTitle: '木雕示范匠人',
      workshopName: '木雕基础体验工坊',
      displayLocation: '木雕体验区 B-02 成品展台',
      qrPlacement: '二维码设置在作品底座说明牌与展台侧边标签上，便于线下参观时直接扫码。',
      materialSourceSummary:
        '作品使用经筛选的木料坯板、描线纸和基础雕刻工具，由木雕工坊统一发放到每个体验小组。',
      materialSources: [
        '樟木或椴木坯料来自木雕教学材料仓',
        '描线纸与转印纸由课程教具柜统一领取',
        '刻刀、砂纸与打磨辅料由工坊安全工具区配发'
      ],
      traceNotice: '可用于说明木雕研学中的安全讲解、刀法体验和成品展示流程。'
    })
  }

  if (code === 'CI003' || speciality === 'embroidery') {
    return normalizeTraceMeta({
      artisanName: '沈绣兰老师',
      artisanTitle: '苏绣双面绣指导匠人',
      workshopName: '苏绣工艺研修工坊',
      displayLocation: '苏绣展陈区 C-03 团扇展示柜',
      qrPlacement: '二维码粘贴在团扇展示托牌与展柜铭牌处，方便观众查看双面绣细节与步骤记录。',
      materialSourceSummary:
        '主要材料为真丝扇面、苏绣丝线、绣绷与定位样稿，材料由苏绣工坊按双面绣教学规格统一准备。',
      materialSources: [
        '真丝扇面由苏绣工坊展示材料库统一配发',
        '双面绣丝线按照冷暖配色方案预先分装',
        '绣绷、定位样稿和针具由教学工具柜统一领取'
      ],
      traceNotice: '该作品重点展示双面绣针法控制、配线处理与成品装裱效果。'
    })
  }

  if (code === 'CI002' || speciality === 'ceramic') {
    return normalizeTraceMeta({
      artisanName: '周青禾师傅',
      artisanTitle: '青花瓷绘制指导匠人',
      workshopName: '青花瓷绘制展示工坊',
      displayLocation: '器物展示区 D-01 青花瓷成品柜',
      qrPlacement: '二维码张贴于展柜铭牌和器物说明卡上，便于对照查看绘制与烧制前后的工艺记录。',
      materialSourceSummary:
        '作品采用素坯瓷器、青花颜料、毛笔与样稿，由青花瓷工坊根据体验课程统一备料并完成烧制排期。',
      materialSources: [
        '素坯器型来自景德镇教学素坯供应库',
        '青花颜料由工坊绘制材料台统一分装',
        '毛笔、样稿纸与定稿辅料由课程教具柜统一发放'
      ],
      traceNotice: '可重点说明构图、绘制和烧制展示之间的前后关系。'
    })
  }

  if (code === 'CI005' || speciality === 'silver') {
    return normalizeTraceMeta({
      artisanName: '龙银海师傅',
      artisanTitle: '苗银錾刻指导匠人',
      workshopName: '苗银錾刻体验工坊',
      displayLocation: '民族银饰展示区 E-02 项圈展柜',
      qrPlacement: '二维码设置在展柜底牌与银饰说明卡上，方便对照纹样与錾刻步骤进行讲解。',
      materialSourceSummary:
        '作品主要采用银片坯料、纹样模板和錾刻工具，材料由苗银体验工坊统一备料并按工序分发。',
      materialSources: [
        '银片坯料由苗银教学材料区统一领取',
        '纹样模板由民族纹饰资料库提供',
        '錾刻工具与抛光辅料由工坊操作台统一配发'
      ],
      traceNotice: '可用于展示民族纹饰表达、錾刻工序和成品饰物呈现效果。'
    })
  }

  return null
}

function craftMetaStorageKey(craft = {}) {
  const code = String(craft.code || '').trim().toUpperCase()
  if (code) return `code:${code}`

  const id = craft.id ?? craft.craftId
  if (id != null && id !== '') return `id:${id}`
  return ''
}

export function ensureTeacherSeeded() {
  const records = readJson(TEACHER_STORAGE_KEY, [])
  if (Array.isArray(records) && records.length > 0) return listTeacherRecords()

  const next = DEFAULT_TEACHERS.map((item) => normalizeTeacher(item))
  writeJson(TEACHER_STORAGE_KEY, next)
  return next
}

export function listTeacherRecords() {
  const records = readJson(TEACHER_STORAGE_KEY, [])
  const source = Array.isArray(records) && records.length > 0 ? records : DEFAULT_TEACHERS

  return source
    .map((item) => normalizeTeacher(item))
    .sort((a, b) => a.name.localeCompare(b.name, 'zh-Hans-CN'))
}

export function getTeacherRecord(id) {
  if (!id) return null
  return listTeacherRecords().find((item) => String(item.id) === String(id)) || null
}

export function upsertTeacherRecord(record) {
  const next = normalizeTeacher(record)
  const list = listTeacherRecords()
  const index = list.findIndex((item) => item.id === next.id)

  if (index >= 0) list.splice(index, 1, next)
  else list.unshift(next)

  writeJson(TEACHER_STORAGE_KEY, list)
  return next
}

export function removeTeacherRecord(id) {
  if (!id) return

  const nextList = listTeacherRecords().filter((item) => item.id !== String(id))
  writeJson(TEACHER_STORAGE_KEY, nextList)

  const courseMap = readJson(COURSE_TEACHER_META_KEY, {})
  Object.keys(courseMap || {}).forEach((courseId) => {
    if (String(courseMap[courseId]?.teacherId || '') === String(id)) {
      delete courseMap[courseId]
    }
  })
  writeJson(COURSE_TEACHER_META_KEY, courseMap)
}

export function getCourseTeacherMeta(courseOrId) {
  const courseId =
    typeof courseOrId === 'object' && courseOrId !== null ? courseOrId.id : courseOrId

  if (!courseId && courseId !== 0) {
    return typeof courseOrId === 'object' ? buildDefaultCourseTeacherMeta(courseOrId) : null
  }

  const map = readJson(COURSE_TEACHER_META_KEY, {})
  const saved = map?.[String(courseId)]
  if (saved) return normalizeCourseTeacherMeta(saved)

  if (typeof courseOrId === 'object' && courseOrId !== null) {
    return buildDefaultCourseTeacherMeta(courseOrId)
  }

  return null
}

export function saveCourseTeacherMeta(courseId, meta) {
  if (!courseId && courseId !== 0) return null

  const normalized = normalizeCourseTeacherMeta(meta)
  const map = readJson(COURSE_TEACHER_META_KEY, {})

  if (!normalized.teacherId && !normalized.teacherName) {
    delete map[String(courseId)]
  } else {
    map[String(courseId)] = normalized
  }

  writeJson(COURSE_TEACHER_META_KEY, map)
  return normalized
}

export function ensureCourseTeacherSeeded(courses = []) {
  if (!Array.isArray(courses) || courses.length === 0) return

  ensureTeacherSeeded()
  const map = readJson(COURSE_TEACHER_META_KEY, {})
  let changed = false

  courses.forEach((course) => {
    const key = String(course?.id ?? '')
    if (!key || map[key]) return

    const inferred = buildDefaultCourseTeacherMeta(course)
    if (!inferred) return

    map[key] = inferred
    changed = true
  })

  if (changed) writeJson(COURSE_TEACHER_META_KEY, map)
}

export function mergeCourseTeacherMeta(course) {
  return {
    ...course,
    teacherMeta: getCourseTeacherMeta(course)
  }
}

export function getCraftTraceMeta(craft) {
  const key = craftMetaStorageKey(craft)
  const map = readJson(CRAFT_TRACE_META_KEY, {})
  const saved = key ? map?.[key] : null
  if (saved) return normalizeTraceMeta(saved)

  return buildDefaultCraftTraceMeta(craft)
}

export function saveCraftTraceMeta(craft, meta) {
  const key = craftMetaStorageKey(craft)
  if (!key) return null

  const normalized = normalizeTraceMeta(meta)
  const map = readJson(CRAFT_TRACE_META_KEY, {})
  map[key] = normalized
  writeJson(CRAFT_TRACE_META_KEY, map)
  return normalized
}

export function ensureCraftTraceSeeded(crafts = []) {
  if (!Array.isArray(crafts) || crafts.length === 0) return

  const map = readJson(CRAFT_TRACE_META_KEY, {})
  let changed = false

  crafts.forEach((craft) => {
    const key = craftMetaStorageKey(craft)
    if (!key || map[key]) return

    const inferred = buildDefaultCraftTraceMeta(craft)
    if (!inferred) return

    map[key] = inferred
    changed = true
  })

  if (changed) writeJson(CRAFT_TRACE_META_KEY, map)
}

export function mergeCraftTraceMeta(craft) {
  const meta = getCraftTraceMeta(craft)
  return meta ? { ...craft, traceMeta: meta } : craft
}
