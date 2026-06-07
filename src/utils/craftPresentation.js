const formalBagDescription =
  '以广西壮锦传统纹样与织造工艺为设计基础，结合手工包型结构与日常收纳需求，集中呈现民族色彩、织锦肌理和实用展示价值，适合作为非遗研学成果与文创作品进行公开展示。'

export function craftDisplayDescription(craft = {}) {
  const code = String(craft.code || '').trim().toUpperCase()
  const description = String(craft.description || '').trim()

  if (code === 'CI001' || description === '广西壮锦手工制作示例') {
    return formalBagDescription
  }

  return description
}
