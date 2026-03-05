<template>
  <div class="panel">
    <el-table :data="list">
      <el-table-column prop="roomName" label="自习室" />
      <el-table-column prop="seatNo" label="座位号" width="120" />
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="cancel(row)" :disabled="row.status !== 'RESERVED'">取消</el-button>
          <el-button size="small" type="primary" @click="checkin(row)" :disabled="row.status !== 'RESERVED'">签到</el-button>
          <el-button size="small" type="success" @click="checkout(row)" :disabled="row.status !== 'CHECKED_IN'">签离</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { reservationApi } from '../../api'

const list = ref([])
const load = async () => {
  list.value = await reservationApi.myList()
}

const cancel = async (row) => {
  await reservationApi.cancel(row.id)
  ElMessage.success('已取消')
  load()
}

const checkin = async (row) => {
  await reservationApi.checkin(row.id)
  ElMessage.success('签到成功')
  load()
}

const checkout = async (row) => {
  await reservationApi.checkout(row.id)
  ElMessage.success('签离成功')
  load()
}

onMounted(load)
</script>
