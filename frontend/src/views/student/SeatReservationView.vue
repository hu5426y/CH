<template>
  <div class="panel">
    <el-row :gutter="12">
      <el-col :span="6">
        <el-select v-model="query.roomId" placeholder="选择自习室" clearable style="width: 100%" @change="loadSeats">
          <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
      </el-col>
      <el-col :span="6">
        <el-select v-model="query.status" clearable style="width: 100%" @change="loadSeats">
          <el-option label="空闲" value="FREE" />
          <el-option label="已预约" value="RESERVED" />
          <el-option label="占用" value="OCCUPIED" />
          <el-option label="维修" value="MAINTENANCE" />
        </el-select>
      </el-col>
      <el-col :span="5">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          value-format="YYYY-MM-DD"
          :editable="false"
          :disabled-date="disablePastDate"
          placeholder="预约日期"
          style="width: 100%"
        />
      </el-col>
      <el-col :span="4">
        <el-select v-model="startTime" placeholder="开始时间" clearable style="width: 100%">
          <el-option v-for="opt in startTimeOptions" :key="opt" :label="opt" :value="opt" />
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-select v-model="endTime" placeholder="结束时间" clearable style="width: 100%">
          <el-option v-for="opt in endTimeOptions" :key="opt" :label="opt" :value="opt" />
        </el-select>
      </el-col>
      <el-col :span="3">
        <el-button style="width: 100%" @click="loadSeats">刷新座位</el-button>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :span="10">
        <el-select v-model="selectedSeatId" placeholder="请选择空闲座位" clearable style="width: 100%">
          <el-option
            v-for="seat in freeSeats"
            :key="seat.id"
            :label="`座位 ${seat.seatNo}（自习室 ${seat.roomId}）`"
            :value="seat.id"
          />
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-button type="primary" style="width: 100%" @click="reserveBySeatId(selectedSeatId)">提交预约</el-button>
      </el-col>
      <el-col :span="10">
        <el-alert
          title="可预约窗口为当天 09:00-21:00，单次预约最长 4 小时。"
          type="info"
          :closable="false"
          show-icon
        />
      </el-col>
    </el-row>

    <el-table :data="seats" style="margin-top: 16px">
      <el-table-column prop="roomId" label="自习室ID" width="100" />
      <el-table-column prop="seatNo" label="座位号" width="120" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="underMaintenance" label="维修" width="100">
        <template #default="s">{{ s.row.underMaintenance ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button type="primary" size="small" :disabled="scope.row.status !== 'FREE'" @click="reserveBySeatId(scope.row.id)">预约</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { roomApi, seatApi, reservationApi } from '../../api'

const rooms = ref([])
const seats = ref([])
const selectedSeatId = ref()
const selectedDate = ref('')
const startTime = ref('')
const endTime = ref('')
const query = reactive({ roomId: undefined, status: '' })
const freeSeats = computed(() => seats.value.filter((s) => s.status === 'FREE'))
const hourOptions = Array.from({ length: 13 }, (_, i) => String(i + 9).padStart(2, '0') + ':00')

const disablePastDate = (date) => {
  const d = new Date(date)
  d.setHours(0, 0, 0, 0)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return d.getTime() < today.getTime()
}

const startTimeOptions = computed(() => {
  if (!selectedDate.value) {
    return hourOptions.slice(0, -1)
  }
  const today = new Date()
  const todayText = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
  let minHour = 9
  if (selectedDate.value === todayText) {
    minHour = Math.max(minHour, today.getHours() + (today.getMinutes() > 0 || today.getSeconds() > 0 ? 1 : 0))
  }
  return hourOptions.filter((t) => Number(t.slice(0, 2)) >= minHour && Number(t.slice(0, 2)) <= 20)
})

const endTimeOptions = computed(() => {
  if (!startTime.value) {
    return []
  }
  const startHour = Number(startTime.value.slice(0, 2))
  const maxHour = Math.min(startHour + 4, 21)
  return hourOptions.filter((t) => {
    const hour = Number(t.slice(0, 2))
    return hour > startHour && hour <= maxHour
  })
})

const loadRooms = async () => {
  rooms.value = await roomApi.list()
}

const loadSeats = async () => {
  seats.value = await seatApi.list(query)
}

const reserveBySeatId = async (seatId) => {
  if (!seatId) {
    ElMessage.warning('请先选择一个空闲座位')
    return
  }
  if (!selectedDate.value || !startTime.value || !endTime.value) {
    ElMessage.warning('请选择预约日期和时间段')
    return
  }
  const startHour = Number(startTime.value.slice(0, 2))
  const endHour = Number(endTime.value.slice(0, 2))
  if (endHour <= startHour) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }
  if (endHour - startHour > 4) {
    ElMessage.warning('单次预约时长不能超过 4 小时')
    return
  }

  await reservationApi.add({
    seatId: Number(seatId),
    startTime: `${selectedDate.value}T${startTime.value}:00`,
    endTime: `${selectedDate.value}T${endTime.value}:00`
  })
  ElMessage.success('预约成功')
  selectedSeatId.value = undefined
  startTime.value = ''
  endTime.value = ''
  loadSeats()
}

watch(selectedDate, () => {
  startTime.value = ''
  endTime.value = ''
})

watch(startTime, () => {
  if (!startTime.value) {
    endTime.value = ''
    return
  }
  if (endTime.value && !endTimeOptions.value.includes(endTime.value)) {
    endTime.value = ''
  }
  if (!endTime.value && endTimeOptions.value.length > 0) {
    endTime.value = endTimeOptions.value[0]
  }
})

onMounted(async () => {
  await loadRooms()
  await loadSeats()
})
</script>
