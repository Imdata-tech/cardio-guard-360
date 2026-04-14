import axios from 'axios'

const BASE_URL = '/api/ecg/annotation'

/**
 * ECG标注类型枚举
 */
export enum AnnotationType {
  P_WAVE = 'P_WAVE',
  QRS_COMPLEX = 'QRS_COMPLEX',
  T_WAVE = 'T_WAVE',
  ST_SEGMENT = 'ST_SEGMENT',
  ABNORMAL_POINT = 'ABNORMAL_POINT',
  OTHER = 'OTHER'
}

/**
 * ECG标注接口
 */
export interface EcgAnnotation {
  id?: number
  userId: number
  analysisResultId: number
  timestampMs: number
  annotationType: AnnotationType | string
  label?: string
  confidence?: number
  isAiGenerated?: number
  createdBy: number
  createdAt?: string
  updatedAt?: string
}

/**
 * 标注统计信息
 */
export interface AnnotationStatistics {
  totalCount: number
  typeStatistics: Record<string, number>
  aiGeneratedCount: number
  manualCount: number
}

/**
 * 创建标注
 */
export function createAnnotation(annotation: EcgAnnotation) {
  return axios.post(BASE_URL, annotation)
}

/**
 * 批量创建标注
 */
export function batchCreateAnnotations(annotations: EcgAnnotation[]) {
  return axios.post(`${BASE_URL}/batch`, annotations)
}

/**
 * 更新标注
 */
export function updateAnnotation(annotationId: number, annotation: Partial<EcgAnnotation>) {
  return axios.put(`${BASE_URL}/${annotationId}`, annotation)
}

/**
 * 删除标注
 */
export function deleteAnnotation(annotationId: number, userId: number) {
  return axios.delete(`${BASE_URL}/${annotationId}`, {
    params: { userId }
  })
}

/**
 * 查询指定分析结果的标注
 */
export function getAnnotationsByResultId(analysisResultId: number) {
  return axios.get(`${BASE_URL}/result/${analysisResultId}`)
}

/**
 * 查询用户标注历史
 */
export function getUserAnnotationHistory(userId: number, page: number = 1, size: number = 20) {
  return axios.get(`${BASE_URL}/history`, {
    params: { userId, page, size }
  })
}

/**
 * AI自动标注
 */
export function aiAutoAnnotate(analysisResultId: number, userId: number) {
  return axios.post(`${BASE_URL}/ai-annotate/${analysisResultId}`, null, {
    params: { userId }
  })
}

/**
 * 查询用户标注统计
 */
export function getUserAnnotationStatistics(userId: number) {
  return axios.get(`${BASE_URL}/statistics`, {
    params: { userId }
  })
}
