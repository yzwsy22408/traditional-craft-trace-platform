import request from './request'

export function searchSimilarImages(file, limit = 8) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('limit', String(limit))

  return request.post('/public/image-search', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
