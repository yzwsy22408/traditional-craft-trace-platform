const COURSE_PROFILES = [
  {
    id: 'zhuangjin',
    keywords: ['壮锦', '织锦', '锦艺', '手工包'],
    displayName: '壮锦织造研学',
    introLead:
      '课程围绕广西壮锦的纹样组织、配色逻辑与基础织造流程展开，学生将在老师带队与匠人示范下，系统了解壮锦纹样的文化寓意、织造工序与成品整理方式。',
    baseIntro:
      '本课程以壮锦传统纹样和织造体验为核心，结合工坊现场导览、材料认知与分组实践，引导学生在有限课时内理解壮锦工艺的基本流程、色彩搭配方法和成品构成逻辑，适用于民族传统工艺研学、劳动实践与文化体验课程。',
    materialSet: ['壮锦织片', '彩色丝线', '棉线', '底布', '内衬布', '纸样模板', '手缝针线', '剪刀', '尺子'],
    materialKeywords: ['锦', '丝线', '棉线', '底布', '内衬', '布', '纸样', '针线', '剪刀', '尺'],
    outcome: '完成一件带有壮锦纹样元素的手工包或织造练习样片',
    teachingFocus: ['壮锦纹样识读', '配色与经纬组织', '手工缝合与成品整理'],
    providedValue: '课堂统一提供壮锦织片、基础缝制材料、纸样模板与常用工具，学生到场后可直接进入观察与制作环节。',
    takeHome: '学生完成的壮锦手工包、织造练习样片或课堂整理好的体验成果可自行带回家留作纪念。'
  },
  {
    id: 'woodcarving',
    keywords: ['木雕', '雕刻', '木作'],
    displayName: '传统木雕体验课',
    introLead:
      '课程以传统木雕基础训练为主线，学生将进入工坊观摩工具使用、纹样起稿、浅浮雕刻和细节修整等环节，感受木雕作品从平面图样到立体造型的形成过程。',
    baseIntro:
      '本课程围绕传统木雕的入门认知与实操体验展开，依托线下工坊环境安排工具安全说明、纹样转印示范、基础下刀训练和表面打磨整理，使学生能够在真实工艺场景中建立对木雕流程、刀法节奏与作品完成标准的初步理解。',
    materialSet: ['木料坯板', '描线纸', '铅笔', '雕刻刀', '木槌', '砂纸', '木蜡油', '防护手套'],
    materialKeywords: ['木料', '坯板', '描线', '铅笔', '刻刀', '木槌', '砂纸', '木蜡油', '手套'],
    outcome: '完成一件基础木雕纹样板或小型木雕体验作品',
    teachingFocus: ['工具安全规范', '纹样转印与下刀方法', '表面打磨与收尾处理'],
    providedValue: '课堂会统一准备木料坯板、描线纸、雕刻工具和表面处理耗材，并由老师和匠人现场做安全说明。',
    takeHome: '学生完成的木雕纹样板或小型木雕体验作品，在课程整理后可带回家作为研学成果留存。'
  },
  {
    id: 'suxiu',
    keywords: ['苏绣', '刺绣', '绣艺'],
    displayName: '苏绣研修体验课',
    introLead:
      '课程围绕苏绣针法、构图分区与丝线走向展开，学生将在工坊中观察示范样稿与成品绣面，逐步建立色线搭配与针法层次的理解。',
    baseIntro:
      '本课程以苏绣基础针法和绣面组织为主要内容，通过老师带队、匠人示范和学生分组练习的方式，帮助学生认识传统刺绣在构图、走线、配色和整理展示方面的关键要求，形成对传统绣艺工序与审美特点的整体认知。',
    materialSet: ['真丝绣线', '绣绷', '绣布', '描图纸', '绣针', '配色样卡', '小剪刀'],
    materialKeywords: ['绣线', '绣绷', '绣布', '描图', '绣针', '样卡', '剪刀'],
    outcome: '完成一幅基础针法练习片或小型绣面体验作品',
    teachingFocus: ['基础针法训练', '丝线配色与过渡', '绣面整理与展示'],
    providedValue: '课堂会统一提供绣线、绣绷、绣布、描图纸和练习针具，便于学生专注体验传统绣艺的操作过程。',
    takeHome: '学生完成的针法练习片、小型绣面体验作品或阶段成果卡，可在课后带回家展示和保存。'
  },
  {
    id: 'ceramic',
    keywords: ['青花', '陶艺', '瓷', '紫砂'],
    displayName: '陶瓷彩绘体验课',
    introLead:
      '课程结合青花瓷与陶艺体验的共性流程进行设计，学生将在老师组织下了解器型、纹样、施绘与展示的基本逻辑，并通过实地观摩和上手体验建立对传统陶瓷工艺的直观认识。',
    baseIntro:
      '本课程以传统陶瓷与彩绘体验为主要场景，围绕器型观察、纹样布局、基础施绘和作品整理等内容展开。课程强调“看得懂工艺、学得到步骤、做得出成果”，适用于非遗研学、工坊体验和传统美育课程。',
    materialSet: ['素坯器型', '青花颜料', '毛笔', '勾线笔', '调色盘', '围裙', '展示托盘'],
    materialKeywords: ['素坯', '青花', '颜料', '毛笔', '勾线', '调色', '围裙', '托盘'],
    outcome: '完成一件基础彩绘陶瓷体验作品或纹样绘制样品',
    teachingFocus: ['纹样布局', '笔触控制', '器物整理与成果呈现'],
    providedValue: '课堂统一准备素坯器型、青花颜料、绘制工具和基础防护用品，学生无需自行准备核心耗材。',
    takeHome: '学生完成的彩绘陶瓷体验作品或纹样练习样品，在晾置整理后可作为课程成果带回家。'
  },
  {
    id: 'silver',
    keywords: ['苗银', '银饰', '錾刻', '金工'],
    displayName: '苗银錾刻体验课',
    introLead:
      '课程以苗银基础纹样和敲錾工序为核心，学生将在匠人指导下认识银饰纹样的文化来源、工具配合方式与操作节奏，理解传统金工工艺中材料、力度和细节控制之间的关系。',
    baseIntro:
      '本课程围绕苗银工艺的基础纹样表达与敲錾体验设计，通过工坊参观、工具认知、纹样示范和分步练习，让学生在安全、可控的教学场景中认识传统银饰工艺的基本流程，提升对民族工艺文化和手工技艺的理解能力。',
    materialSet: ['银片坯料', '纹样模板', '錾刻工具', '小锤', '抛光布', '量尺', '防护指套'],
    materialKeywords: ['银片', '模板', '錾刻', '小锤', '抛光', '量尺', '指套'],
    outcome: '完成一件基础纹样錾刻练习片或小型银饰体验样品',
    teachingFocus: ['纹样结构识别', '敲錾力度控制', '表面整理与成品呈现'],
    providedValue: '课堂将统一发放银片坯料、錾刻工具、防护指套和抛光用品，并安排老师现场组织体验秩序。',
    takeHome: '学生完成的錾刻练习片或基础银饰体验样品，可在课程结束后带回家作为研学纪念。'
  }
]

function text(value) {
  return String(value || '').trim()
}

function collectCourseText(course) {
  return [course?.title, course?.category, course?.intro].map(text).join(' ')
}

function uniqueList(list) {
  return Array.from(new Set((list || []).map(text).filter(Boolean)))
}

export function findCourseProfile(course) {
  const content = collectCourseText(course)
  return (
    COURSE_PROFILES.find((profile) =>
      profile.keywords.some((keyword) => content.includes(keyword))
    ) || COURSE_PROFILES[0]
  )
}

export function buildRecommendedCourseIntro(course) {
  const profile = findCourseProfile(course)
  const title = text(course?.title)
  const titleLead = title ? `《${title}》` : profile.displayName
  return `${titleLead}${profile.baseIntro}`
}

export function getCourseMaterialSet(course) {
  const profile = findCourseProfile(course)
  const intro = text(course?.intro)
  const fromIntro = intro
    .split(/[,，、。\n]/)
    .map((item) => item.trim())
    .filter((item) => item.length >= 2 && item.length <= 12)
    .filter((item) => profile.materialKeywords.some((keyword) => item.includes(keyword)))

  return uniqueList([...profile.materialSet, ...fromIntro]).slice(0, 10)
}

export function buildCourseRichIntro(course) {
  const profile = findCourseProfile(course)
  const baseIntro = text(course?.intro) || buildRecommendedCourseIntro(course)
  const teacherName = text(course?.teacherName)
  const schoolName = text(course?.teacherSchoolName)
  const subjectName = text(course?.teacherSubjectName)
  const route = text(course?.leadRoute)
  const materialText = getCourseMaterialSet(course).join('、')

  const teacherLine = teacherName
    ? `课程采用“老师带队 + 匠人示范 + 学生实践”的线下研学方式，由${teacherName}${schoolName ? `（${schoolName}）` : ''}${subjectName ? `负责${subjectName}组织` : '负责活动组织'}，带领学生完成工坊参观、材料认知、工序体验与课堂总结。`
    : '课程采用“老师带队 + 匠人示范 + 学生实践”的线下研学方式，学生将按集合导览、工坊参观、工序体验和成果整理的节奏完成学习。'
  const routeLine = route
    ? `线下带队路线安排为：${route}。该安排有助于学生在有限课时内依次理解课程导入、工坊秩序、核心工艺演示与成果整理流程。`
    : '课程现场将按照导入说明、材料认识、核心工序体验、成果整理与过程回顾的顺序推进，保证学生能够完整理解研学内容。'
  const materialLine = `课堂将统一提供${materialText}等基础材料与体验工具，学生无需自行准备核心耗材，可更专注于观察传统工艺流程、理解操作规范并完成课堂体验作品。`
  const supportLine = profile.providedValue
  const outcomeLine = `课程完成后，学生通常能够${profile.outcome}，同时形成对${profile.teachingFocus.join('、')}的初步认识，适用于传统手工艺研学与校园实践活动。`
  const takeHomeLine = profile.takeHome

  return [profile.introLead, baseIntro, teacherLine, routeLine, materialLine, supportLine, outcomeLine, takeHomeLine]
    .filter(Boolean)
    .join('\n\n')
}

export function getCourseHighlightList(course) {
  const profile = findCourseProfile(course)
  const teacherName = text(course?.teacherName)
  return [
    teacherName ? `带队老师：${teacherName}` : '带队方式：老师组织、匠人现场示范',
    `课堂材料：${getCourseMaterialSet(course).slice(0, 4).join('、')}`,
    `体验成果：${profile.outcome}`,
    `教学重点：${profile.teachingFocus.join('、')}`
  ]
}

export function getCourseOutcome(course) {
  return findCourseProfile(course).outcome
}

export function getCourseTeachingFocus(course) {
  return findCourseProfile(course).teachingFocus
}

export function getCourseProvidedValue(course) {
  return findCourseProfile(course).providedValue
}

export function getCourseTakeHome(course) {
  return findCourseProfile(course).takeHome
}

export function materialMatchesCourse(material, course) {
  const rowText = [material?.name, material?.category, material?.description, material?.supplier]
    .map(text)
    .join(' ')
  if (!rowText) return false

  const profile = findCourseProfile(course)
  return profile.materialKeywords.some((keyword) => rowText.includes(keyword))
}

export function materialMatchesCraft(material, craft) {
  const rowText = [material?.name, material?.category, material?.description, material?.supplier]
    .map(text)
    .join(' ')
  if (!rowText) return false

  const craftText = [
    craft?.title,
    craft?.category,
    craft?.materialSourceSummary,
    craft?.materialSources,
    craft?.traceMeta?.materialSourceSummary,
    ...(craft?.traceMeta?.materialSources || [])
  ]
    .map(text)
    .join(' ')

  const tokens = craftText
    .split(/[\n,，、；;。 ]/)
    .map((item) => item.trim())
    .filter((item) => item.length >= 2)

  return tokens.some((token) => rowText.includes(token) || token.includes(text(material?.name)))
}

export function matchCourseListForCraftCategories(courses, categories) {
  const categoryList = uniqueList(categories)
  return (courses || []).filter((course) => {
    const content = collectCourseText(course)
    return categoryList.some((category) => content.includes(category))
  })
}
