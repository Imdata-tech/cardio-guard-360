// 统计用户ECG异常情况
export function getEcgAbnormalStats(
  userId: number,
  startDate?: string,
  endDate?: string
) {
  return request.get('/api/ecg/statistics/abnormal', {
    params: { userId, startDate, endDate }
  })
}

/**
 * ECG标注管理API (v1.4.0)
 */

// 创建标注
export function createAnnotation(data: {
  userId: number
  analysisResultId: number
  timestampMs: number
  annotationType: string
  label?: string
  confidence?: number
  isAiGenerated?: number
  createdBy: number
}) {
  return request.post('/api/ecg/annotation', data)
}

// 批量创建标注
export function batchCreateAnnotations(annotations: Array<{
  userId: number
  analysisResultId: number
  timestampMs: number
  annotationType: string
  label?: string
  confidence?: number
  isAiGenerated?: number
  createdBy: number
}>) {
  return request.post('/api/ecg/annotation/batch', annotations)
}

// 更新标注
export function updateAnnotation(annotationId: number, data: {
  label?: string
  confidence?: number
}) {
  return request.put(`/api/ecg/annotation/${annotationId}`, data)
}

// 删除标注
export function deleteAnnotation(annotationId: number, userId: number) {
  return request.delete(`/api/ecg/annotation/${annotationId}`, {
    params: { userId }
  })
}

// 查询指定分析结果的标注
export function getAnnotationsByResultId(analysisResultId: number) {
  return request.get(`/api/ecg/annotation/result/${analysisResultId}`)
}

// 查询用户标注历史
export function getUserAnnotationHistory(userId: number, page: number = 1, size: number = 20) {
  return request.get('/api/ecg/annotation/history', {
    params: { userId, page, size }
  })
}

// AI自动标注
export function aiAutoAnnotate(analysisResultId: number, userId: number) {
  return request.post(`/api/ecg/annotation/ai-annotate/${analysisResultId}`, null, {
    params: { userId }
  })
}

// 查询用户标注统计
export function getUserAnnotationStatistics(userId: number) {
  return request.get('/api/ecg/annotation/statistics', {
    params: { userId }
  })
}
