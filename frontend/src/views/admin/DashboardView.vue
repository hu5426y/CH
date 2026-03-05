<template>
  <div>
    <el-row :gutter="12">
      <el-col v-for="(v, k) in overview" :key="k" :span="4">
        <div class="panel">
          <div style="color: #6b7280">{{ labels[k] || k }}</div>
          <div style="font-size: 24px; font-weight: 600">{{ v }}</div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :span="12"><div class="panel" ref="usageRef" style="height: 360px"></div></el-col>
      <el-col :span="12"><div class="panel" ref="trendRef" style="height: 360px"></div></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi } from '../../api'

const overview = ref({})
const usageRef = ref()
const trendRef = ref()

const labels = {
  studentCount: '学生数',
  roomCount: '自习室数',
  seatCount: '座位数',
  reservationToday: '今日预约',
  violationCount: '违约数',
  utilization: '利用率(%)'
}

onMounted(async () => {
  overview.value = await statisticsApi.overview()

  const usage = await statisticsApi.roomUsage()
  const usageChart = echarts.init(usageRef.value)
  usageChart.setOption({
    title: { text: '自习室使用情况' },
    tooltip: {},
    xAxis: { type: 'category', data: usage.map(i => i.roomName) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: usage.map(i => i.reservationCount), itemStyle: { color: '#0f766e' } }]
  })

  const trend = await statisticsApi.trend(7)
  const trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    title: { text: '近7天预约趋势' },
    tooltip: {},
    xAxis: { type: 'category', data: trend.map(i => i.day) },
    yAxis: { type: 'value' },
    series: [{ type: 'line', smooth: true, data: trend.map(i => i.count), itemStyle: { color: '#1d4ed8' } }]
  })
})
</script>
