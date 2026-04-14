<template>
  <div class="ecg-monitor">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">当前心率</div>
              <div class="stat-value">{{ currentHeartRate }} <span class="unit">bpm</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon><Waveform /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">采样率</div>
              <div class="stat-value">{{ sampleRate }} <span class="unit">Hz</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">记录时长</div>
              <div class="stat-value">{{ recordingDuration }} <span class="unit">s</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: connectionStatus === 'connected' ? 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' : 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' }">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">连接状态</div>
              <div class="stat-value" :class="connectionStatus">{{ connectionText }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ECG 波形图 -->
    <el-card shadow="never" class="waveform-card">
      <template #header>
        <div class="card-header">
          <span class="title">实时 ECG 心电图</span>
          <el-space>
            <el-tag v-if="analysisResult" :type="getDiagnosisType(analysisResult.diagnosis)">
              {{ analysisResult.diagnosis === 'NORMAL' ? '正常' : '异常' }}
            </el-tag>
            <el-button type="primary" size="small" @click="startRecording" :disabled="isRecording">
              <el-icon><VideoPlay /></el-icon>
              开始记录
            </el-button>
            <el-button type="danger" size="small" @click="stopRecording" :disabled="!isRecording">
              <el-icon><VideoPause /></el-icon>
              停止记录
            </el-button>
            <el-button type="success" size="small" @click="analyzeCurrent" :disabled="!ecgData.length">
              <el-icon><Aim /></el-icon>
              AI 分析
            </el-button>
            <el-divider direction="vertical" />
            <el-button type="warning" size="small" @click="aiAutoAnnotate" :disabled="!analysisResult">
              <el-icon><MagicStick /></el-icon>
              AI 标注
            </el-button>
            <el-dropdown @command="handleAnnotationTypeChange">
              <el-button size="small" :type="selectedAnnotationType ? 'primary' : ''">
                <el-icon><EditPen /></el-icon>
                {{ selectedAnnotationType ? getAnnotationTypeName(selectedAnnotationType) : '选择标注类型' }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="P_WAVE">P波</el-dropdown-item>
                  <el-dropdown-item command="QRS_COMPLEX">QRS波群</el-dropdown-item>
                  <el-dropdown-item command="T_WAVE">T波</el-dropdown-item>
                  <el-dropdown-item command="ST_SEGMENT">ST段</el-dropdown-item>
                  <el-dropdown-item command="ABNORMAL_POINT">异常点</el-dropdown-item>
                  <el-dropdown-item command="OTHER">其他</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button size="small" @click="clearAnnotations" :disabled="!annotations.length">
              <el-icon><Delete /></el-icon>
              清除标注
            </el-button>
          </el-space>
        </div>
      </template>
      
      <v-chart 
        ref="ecgChartRef"
        :option="ecgChartOption" 
        autoresize 
        style="height: 400px"
        @click="handleChartClick"
      />
      
      <!-- 标注列表面板 -->
      <el-collapse v-if="annotations.length > 0" v-model="activeAnnotationCollapse" style="margin-top: 20px">
        <el-collapse-item title="标注列表" name="1">
          <el-table :data="annotations" stripe max-height="300">
            <el-table-column prop="annotationType" label="类型" width="120">
              <template #default="{ row }">
                <el-tag :color="getAnnotationColor(row.annotationType)" effect="dark" size="small">
                  {{ getAnnotationTypeName(row.annotationType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="label" label="标签" width="100" />
            <el-table-column prop="timestampMs" label="时间点" width="120">
              <template #default="{ row }">
                {{ (row.timestampMs / 1000).toFixed(2) }}s
              </template>
            </el-table-column>
            <el-table-column prop="confidence" label="置信度" width="100">
              <template #default="{ row }">
                {{ row.confidence ? (row.confidence * 100).toFixed(1) + '%' : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="isAiGenerated" label="来源" width="80">
              <template #default="{ row }">
                <el-tag :type="row.isAiGenerated ? 'success' : 'info'" size="small">
                  {{ row.isAiGenerated ? 'AI' : '手动' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="editAnnotation(row)">
                  编辑
                </el-button>
                <el-button link type="danger" size="small" @click="deleteAnnotationItem(row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <!-- 生理参数详情 -->
    <el-row :gutter="20" class="params-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>心电参数</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="PR间期">
              {{ ecgParams.prInterval || '--' }} ms
            </el-descriptions-item>
            <el-descriptions-item label="QRS时限">
              {{ ecgParams.qrsDuration || '--' }} ms
            </el-descriptions-item>
            <el-descriptions-item label="QT间期">
              {{ ecgParams.qtInterval || '--' }} ms
            </el-descriptions-item>
            <el-descriptions-item label="电轴">
              {{ ecgParams.axis || '--' }}°
            </el-descriptions-item>
            <el-descriptions-item label="HRV">
              {{ ecgParams.hrv || '--' }} ms
            </el-descriptions-item>
            <el-descriptions-item label="数据质量">
              <el-progress 
                :percentage="ecgParams.dataQuality || 0" 
                :color="getQualityColor(ecgParams.dataQuality)"
              />
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>AI 分析结果</span>
          </template>
          <div v-if="analysisResult" class="analysis-result">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="诊断结果">
                <el-tag :type="getDiagnosisType(analysisResult.diagnosis)">
                  {{ analysisResult.diagnosis === 'NORMAL' ? '正常' : '异常' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="心律失常类型" v-if="analysisResult.arrhythmiaType">
                {{ getArrhythmiaName(analysisResult.arrhythmiaType) }}
              </el-descriptions-item>
              <el-descriptions-item label="AI置信度">
                <el-progress 
                  :percentage="Math.round(analysisResult.confidence * 100)" 
                  :format="format => `${format}%`"
                />
              </el-descriptions-item>
              <el-descriptions-item label="风险等级">
                <el-tag :type="getRiskLevelType(analysisResult.riskLevel)">
                  {{ getRiskLevelName(analysisResult.riskLevel) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="建议措施">
                {{ analysisResult.recommendation }}
              </el-descriptions-item>
            </el-descriptions>
            
            <div class="review-status" v-if="analysisResult.reviewStatus">
              <el-divider />
              <div class="status-header">
                <span>医生审核状态:</span>
                <el-tag :type="getReviewStatusType(analysisResult.reviewStatus)">
                  {{ getReviewStatusName(analysisResult.reviewStatus) }}
                </el-tag>
              </div>
              <div v-if="analysisResult.reviewComment" class="review-comment">
                <strong>审核意见:</strong> {{ analysisResult.reviewComment }}
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无分析结果" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 分析历史列表 -->
    <el-card shadow="never" class="history-card">
      <template #header>
        <div class="card-header">
          <span>分析历史记录</span>
          <el-button size="small" @click="loadHistory">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="historyList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="诊断结果" width="120">
          <template #default="{ row }">
            <el-tag :type="getDiagnosisType(row.diagnosis)">
              {{ row.diagnosis === 'NORMAL' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="心律失常" width="150">
          <template #default="{ row }">
            {{ row.arrhythmiaType ? getArrhythmiaName(row.arrhythmiaType) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="置信度" width="120">
          <template #default="{ row }">
            {{ (row.confidence * 100).toFixed(2) }}%
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getRiskLevelType(row.riskLevel)" size="small">
              {{ getRiskLevelName(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getReviewStatusType(row.reviewStatus)" size="small">
              {{ getReviewStatusName(row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="分析时间" width="180" />
        <el-table-column label="操作" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewReport(row.id)">
              查看报告
            </el-button>
            <el-button link type="success" size="small" @click="downloadReport(row.id)">
              下载
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalRecords"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
  
  <!-- 标注编辑对话框 -->
  <el-dialog v-model="annotationDialogVisible" title="编辑标注" width="500px">
    <el-form :model="annotationForm" label-width="100px">
      <el-form-item label="标注类型">
        <el-select v-model="annotationForm.annotationType" style="width: 100%">
          <el-option label="P波" value="P_WAVE" />
          <el-option label="QRS波群" value="QRS_COMPLEX" />
          <el-option label="T波" value="T_WAVE" />
          <el-option label="ST段" value="ST_SEGMENT" />
          <el-option label="异常点" value="ABNORMAL_POINT" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="annotationForm.label" placeholder="输入标签描述" />
      </el-form-item>
      <el-form-item label="时间点">
        <el-input-number 
          v-model="annotationForm.timestampMs" 
          :min="0" 
          :step="10"
          style="width: 100%"
        />
        <span style="margin-left: 10px; color: #909399">ms</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="annotationDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveAnnotation">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, DataZoomComponent } from 'echarts/components'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Monitor, Waveform, Timer, Connection, VideoPlay, VideoPause, Aim, Refresh,
  MagicStick, EditPen, Delete, ArrowDown
} from '@element-plus/icons-vue'
import axios from 'axios'
import * as ecgApi from '@/api/ecg'

// 按需引入 ECharts 组件
use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, DataZoomComponent])

// 响应式数据
const currentHeartRate = ref(72)
const sampleRate = ref(250)
const recordingDuration = ref(0)
const connectionStatus = ref('disconnected')
const isRecording = ref(false)
const ecgData = ref<number[]>([])
const analysisResult = ref<any>(null)
const historyList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalRecords = ref(0)

const ecgParams = ref({
  prInterval: null,
  qrsDuration: null,
  qtInterval: null,
  axis: null,
  hrv: null,
  dataQuality: 95
})

let recordingTimer: any = null
let ws: WebSocket | null = null
let reconnectTimer: any = null
const maxReconnectAttempts = 5
let reconnectAttempts = 0

// 标注相关数据
const annotations = ref<any[]>([])
const selectedAnnotationType = ref<string>('')
const activeAnnotationCollapse = ref(['1'])
const annotationDialogVisible = ref(false)
const annotationForm = ref({
  id: null as number | null,
  userId: 1,
  analysisResultId: 0,
  timestampMs: 0,
  annotationType: 'P_WAVE',
  label: '',
  confidence: null as number | null,
  isAiGenerated: 0,
  createdBy: 1
})

// ECG 图表配置
const ecgChartOption = computed(() => {
  // 准备标注数据
  const markPoints = annotations.value.map(ann => ({
    coord: [ann.timestampMs / 4, ann.value || 0], // 假设采样率250Hz,每4ms一个点
    value: ann.label || getAnnotationTypeName(ann.annotationType),
    itemStyle: {
      color: getAnnotationColor(ann.annotationType)
    }
  }))
  
  return {
  tooltip: {
    trigger: 'axis',
    formatter: (params: any) => {
      const point = params[0]
      return `时间点: ${point.name}<br/>电压: ${point.value} mV`
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '10%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ecgData.value.map((_, index) => index.toString()),
    axisLine: { lineStyle: { color: '#999' } }
  },
  yAxis: {
    type: 'value',
    name: '电压 (mV)',
    min: -2,
    max: 2,
    axisLine: { lineStyle: { color: '#999' } },
    splitLine: { lineStyle: { color: '#eee' } }
  },
  dataZoom: [
    {
      type: 'inside',
      start: 0,
      end: 100
    },
    {
      start: 0,
      end: 100,
      handleIcon: 'path://M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
      handleSize: '80%',
      handleStyle: {
        color: '#fff',
        shadowBlur: 3,
        shadowColor: 'rgba(0, 0, 0, 0.6)',
        shadowOffsetX: 2,
        shadowOffsetY: 2
      }
    }
  ],
  series: [{
    name: 'ECG',
    type: 'line',
    smooth: false,
    showSymbol: false,
    data: ecgData.value,
    lineStyle: {
      color: '#409eff',
      width: 2
    },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ]
      }
    },
    markPoint: {
      data: markPoints,
      symbol: 'pin',
      symbolSize: 50,
      label: {
        show: true,
        fontSize: 10
      }
    }
  }]
}
})

// 计算属性
const connectionText = computed(() => {
  return connectionStatus.value === 'connected' ? '已连接' : '未连接'
})

// 方法
/**
 * 建立WebSocket连接
 */
const connectWebSocket = () => {
  const userId = 1 // TODO: 从用户Store获取
  
  // WebSocket服务器地址
  const wsUrl = `ws://localhost:8080/ws/monitor?userId=${userId}`
  
  try {
    ws = new WebSocket(wsUrl)
    
    ws.onopen = () => {
      console.log('WebSocket连接成功')
      connectionStatus.value = 'connected'
      reconnectAttempts = 0
      ElMessage.success('已连接到ECG监测服务')
    }
    
    ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data)
        handleWebSocketMessage(message)
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }
    
    ws.onerror = (error) => {
      console.error('WebSocket错误:', error)
      connectionStatus.value = 'disconnected'
      ElMessage.error('WebSocket连接错误')
    }
    
    ws.onclose = (event) => {
      console.log('WebSocket连接关闭:', event.code, event.reason)
      connectionStatus.value = 'disconnected'
      
      // 尝试自动重连
      if (event.code !== 1000 && reconnectAttempts < maxReconnectAttempts) {
        attemptReconnect()
      }
    }
  } catch (error) {
    console.error('创建WebSocket连接失败:', error)
    ElMessage.error('连接失败')
  }
}

/**
 * 处理WebSocket消息
 */
const handleWebSocketMessage = (message: any) => {
  switch (message.type) {
    case 'connected':
      console.log('服务器响应:', message.message)
      break
      
    case 'ecg-data':
      handleEcgData(message.data)
      break
      
    case 'realtime-data':
      // 处理其他实时数据(如心率)
      if (message.data.heartRate) {
        currentHeartRate.value = Math.round(message.data.heartRate)
      }
      break
      
    default:
      console.log('未知消息类型:', message.type)
  }
}

/**
 * 处理ECG数据点
 */
const handleEcgData = (data: any) => {
  if (isRecording.value) {
    // 添加电压值到波形数据
    ecgData.value.push(data.voltage)
    
    // 保持最近500个数据点
    if (ecgData.value.length > 500) {
      ecgData.value.shift()
    }
    
    // 更新心率
    if (data.heartRate) {
      currentHeartRate.value = Math.round(data.heartRate)
    }
  }
}

/**
 * 尝试重新连接
 */
const attemptReconnect = () => {
  reconnectAttempts++
  console.log(`尝试重连 (${reconnectAttempts}/${maxReconnectAttempts})...`)
  
  reconnectTimer = setTimeout(() => {
    if (connectionStatus.value === 'disconnected') {
      connectWebSocket()
    }
  }, 3000 * reconnectAttempts) // 指数退避
}

/**
 * 启动ECG数据模拟(后端)
 */
const startSimulation = async () => {
  try {
    await axios.post('/api/ecg/simulation/start', null, {
      params: { userId: 1 } // TODO: 从用户Store获取
    })
    ElMessage.success('ECG数据模拟已启动')
  } catch (error: any) {
    console.error('启动模拟失败:', error)
    ElMessage.error(error.response?.data?.message || '启动模拟失败')
  }
}

/**
 * 停止ECG数据模拟(后端)
 */
const stopSimulation = async () => {
  try {
    await axios.post('/api/ecg/simulation/stop', null, {
      params: { userId: 1 } // TODO: 从用户Store获取
    })
    ElMessage.info('ECG数据模拟已停止')
  } catch (error: any) {
    console.error('停止模拟失败:', error)
    ElMessage.error(error.response?.data?.message || '停止模拟失败')
  }
}

const startRecording = () => {
  isRecording.value = true
  recordingDuration.value = 0
  
  // 清除旧的定时器(如果存在)
  if (recordingTimer) {
    clearInterval(recordingTimer)
  }
  
  // 启动计时器
  recordingTimer = setInterval(() => {
    recordingDuration.value++
  }, 1000)
  
  // 启动后端ECG数据模拟
  startSimulation()
  
  ElMessage.success('开始记录ECG数据')
}

const stopRecording = () => {
  isRecording.value = false
  if (recordingTimer) {
    clearInterval(recordingTimer)
    recordingTimer = null
  }
  
  // 停止后端ECG数据模拟
  stopSimulation()
  
  ElMessage.info('停止记录')
}

const analyzeCurrent = async () => {
  if (!ecgData.value.length) {
    ElMessage.warning('请先记录ECG数据')
    return
  }
  
  try {
    ElMessage.info('正在进行AI分析...')
    
    // 调用后端API进行分析
    const response = await axios.post('/api/ecg/analyze', {
      userId: 1, // TODO: 从用户Store获取
      deviceId: 1,
      waveform: JSON.stringify(ecgData.value),
      sampleRate: sampleRate.value
    })
    
    analysisResult.value = response.data.data
    
    // 更新心电参数
    updateEcgParams()
    
    // 加载该分析结果的标注
    loadAnnotations()
    
    ElMessage.success('分析完成')
  } catch (error: any) {
    console.error('ECG分析失败:', error)
    ElMessage.error(error.response?.data?.message || '分析失败')
  }
}

const loadHistory = async () => {
  try {
    const response = await axios.get('/api/ecg/history', {
      params: {
        userId: 1, // TODO: 从用户Store获取
        page: currentPage.value,
        size: pageSize.value
      }
    })
    
    historyList.value = response.data.data
    totalRecords.value = response.data.data.length // TODO: 后端应返回总数
  } catch (error) {
    console.error('加载历史记录失败:', error)
  }
}

const viewReport = async (resultId: number) => {
  try {
    const response = await axios.get(`/api/ecg/report/${resultId}`)
    const reportContent = response.data.data
    
    // 在新窗口显示报告
    const reportWindow = window.open('', '_blank')
    if (reportWindow) {
      reportWindow.document.write(`
        <html>
          <head>
            <title>ECG诊断报告</title>
            <style>
              body { font-family: Arial, sans-serif; padding: 40px; line-height: 1.6; }
              h1 { color: #409eff; }
              h2 { color: #606266; margin-top: 30px; }
              strong { color: #303133; }
            </style>
          </head>
          <body>${reportContent.replace(/\n/g, '<br>')}</body>
        </html>
      `)
    }
  } catch (error) {
    ElMessage.error('加载报告失败')
  }
}

const downloadReport = async (resultId: number) => {
  try {
    const response = await axios.get(`/api/ecg/report/${resultId}`)
    const reportContent = response.data.data
    
    // 创建下载链接
    const blob = new Blob([reportContent], { type: 'text/markdown' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `ECG_Report_${resultId}.md`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    
    ElMessage.success('报告下载成功')
  } catch (error) {
    ElMessage.error('下载报告失败')
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadHistory()
}

// 辅助方法
const generateMockEcgData = (): number => {
  // 模拟ECG波形数据 (-2mV to 2mV)
  return Math.sin(Date.now() / 100) * 0.5 + (Math.random() - 0.5) * 0.1
}

const updateEcgParams = () => {
  // 模拟更新心电参数
  ecgParams.value = {
    prInterval: 120 + Math.random() * 40,
    qrsDuration: 80 + Math.random() * 20,
    qtInterval: 350 + Math.random() * 50,
    axis: -30 + Math.random() * 60,
    hrv: 30 + Math.random() * 20,
    dataQuality: 85 + Math.random() * 15
  }
}

const getDiagnosisType = (diagnosis: string) => {
  return diagnosis === 'NORMAL' ? 'success' : 'danger'
}

const getArrhythmiaName = (type: string) => {
  const names: Record<string, string> = {
    'AFIB': '房颤',
    'PVC': '室性早搏',
    'PAC': '房性早搏',
    'ST_ELEVATION': 'ST段抬高'
  }
  return names[type] || type
}

const getRiskLevelType = (level: string) => {
  const types: Record<string, any> = {
    'LOW': 'success',
    'MEDIUM': 'warning',
    'HIGH': 'danger',
    'CRITICAL': 'danger'
  }
  return types[level] || 'info'
}

const getRiskLevelName = (level: string) => {
  const names: Record<string, string> = {
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险',
    'CRITICAL': '危急'
  }
  return names[level] || level
}

const getReviewStatusType = (status: string) => {
  const types: Record<string, any> = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return types[status] || 'info'
}

const getReviewStatusName = (status: string) => {
  const names: Record<string, string> = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return names[status] || status
}

const getQualityColor = (quality: number) => {
  if (quality >= 90) return '#67c23a'
  if (quality >= 70) return '#e6a23c'
  return '#f56c6c'
}

// ========== 标注相关方法 ==========

/**
 * 获取标注类型名称
 */
const getAnnotationTypeName = (type: string) => {
  const names: Record<string, string> = {
    'P_WAVE': 'P波',
    'QRS_COMPLEX': 'QRS波群',
    'T_WAVE': 'T波',
    'ST_SEGMENT': 'ST段',
    'ABNORMAL_POINT': '异常点',
    'OTHER': '其他'
  }
  return names[type] || type
}

/**
 * 获取标注颜色
 */
const getAnnotationColor = (type: string) => {
  const colors: Record<string, string> = {
    'P_WAVE': '#67c23a',
    'QRS_COMPLEX': '#f56c6c',
    'T_WAVE': '#409eff',
    'ST_SEGMENT': '#e6a23c',
    'ABNORMAL_POINT': '#f56c6c',
    'OTHER': '#909399'
  }
  return colors[type] || '#409eff'
}

/**
 * 处理标注类型选择
 */
const handleAnnotationTypeChange = (type: string) => {
  selectedAnnotationType.value = type
  ElMessage.info(`已选择标注类型: ${getAnnotationTypeName(type)}`)
}

/**
 * 处理图表点击事件 - 创建标注
 */
const handleChartClick = async (params: any) => {
  if (!selectedAnnotationType.value) {
    ElMessage.warning('请先选择标注类型')
    return
  }
  
  if (!analysisResult.value) {
    ElMessage.warning('请先进行AI分析')
    return
  }
  
  // 计算时间戳(毫秒)
  const timestampMs = params.dataIndex * 4 // 假设采样率250Hz,每4ms一个点
  
  try {
    const response = await ecgApi.createAnnotation({
      userId: 1,
      analysisResultId: analysisResult.value.id,
      timestampMs,
      annotationType: selectedAnnotationType.value,
      label: getAnnotationTypeName(selectedAnnotationType.value),
      isAiGenerated: 0,
      createdBy: 1
    })
    
    annotations.value.push(response.data)
    ElMessage.success('标注创建成功')
    
    // 刷新标注列表
    loadAnnotations()
  } catch (error: any) {
    console.error('创建标注失败:', error)
    ElMessage.error(error.response?.data?.message || '创建标注失败')
  }
}

/**
 * 加载标注列表
 */
const loadAnnotations = async () => {
  if (!analysisResult.value) return
  
  try {
    const response = await ecgApi.getAnnotationsByResultId(analysisResult.value.id)
    annotations.value = response.data
  } catch (error) {
    console.error('加载标注失败:', error)
  }
}

/**
 * AI自动标注
 */
const aiAutoAnnotate = async () => {
  if (!analysisResult.value) {
    ElMessage.warning('请先进行AI分析')
    return
  }
  
  try {
    ElMessage.info('正在进行AI自动标注...')
    
    const response = await ecgApi.aiAutoAnnotate(analysisResult.value.id, 1)
    annotations.value = response.data
    
    ElMessage.success(`AI标注完成,共生成${annotations.value.length}个标注点`)
  } catch (error: any) {
    console.error('AI标注失败:', error)
    ElMessage.error(error.response?.data?.message || 'AI标注失败')
  }
}

/**
 * 编辑标注
 */
const editAnnotation = (annotation: any) => {
  annotationForm.value = { ...annotation }
  annotationDialogVisible.value = true
}

/**
 * 保存标注
 */
const saveAnnotation = async () => {
  try {
    if (annotationForm.value.id) {
      // 更新现有标注
      await ecgApi.updateAnnotation(annotationForm.value.id, {
        label: annotationForm.value.label,
        confidence: annotationForm.value.confidence
      })
      ElMessage.success('标注更新成功')
    } else {
      // 创建新标注
      await ecgApi.createAnnotation(annotationForm.value)
      ElMessage.success('标注创建成功')
    }
    
    annotationDialogVisible.value = false
    loadAnnotations()
  } catch (error: any) {
    console.error('保存标注失败:', error)
    ElMessage.error(error.response?.data?.message || '保存标注失败')
  }
}

/**
 * 删除标注
 */
const deleteAnnotationItem = async (annotationId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此标注吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await ecgApi.deleteAnnotation(annotationId, 1)
    ElMessage.success('标注删除成功')
    
    loadAnnotations()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除标注失败:', error)
      ElMessage.error(error.response?.data?.message || '删除标注失败')
    }
  }
}

/**
 * 清除所有标注
 */
const clearAnnotations = async () => {
  try {
    await ElMessageBox.confirm('确定要清除所有标注吗?此操作不可恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 批量删除
    for (const ann of annotations.value) {
      await ecgApi.deleteAnnotation(ann.id, 1)
    }
    
    annotations.value = []
    ElMessage.success('已清除所有标注')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('清除标注失败:', error)
      ElMessage.error('清除标注失败')
    }
  }
}

// 生命周期
onMounted(() => {
  loadHistory()
  
  // 建立WebSocket连接接收实时ECG数据
  connectWebSocket()
})

onUnmounted(() => {
  stopRecording()
  
  // 清理重连定时器
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }
  
  // 关闭WebSocket连接
  if (ws) {
    ws.close(1000, '组件卸载')
    ws = null
  }
})
</script>

<style scoped lang="scss">
.ecg-monitor {
  padding: 20px;
  
  .stats-row {
    margin-bottom: 20px;
  }
  
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 15px;
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 28px;
      }
      
      .stat-info {
        flex: 1;
        
        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-bottom: 5px;
        }
        
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;
          
          &.connected {
            color: #67c23a;
          }
          
          &.disconnected {
            color: #f56c6c;
          }
          
          .unit {
            font-size: 14px;
            font-weight: normal;
            color: #909399;
          }
        }
      }
    }
  }
  
  .waveform-card,
  .history-card {
    margin-bottom: 20px;
  }
  
  .params-row {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title {
      font-size: 16px;
      font-weight: bold;
      color: #303133;
    }
  }
  
  .analysis-result {
    .review-status {
      .status-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
      }
      
      .review-comment {
        margin-top: 10px;
        padding: 10px;
        background: #f5f7fa;
        border-radius: 4px;
        color: #606266;
      }
    }
  }
}
</style>
