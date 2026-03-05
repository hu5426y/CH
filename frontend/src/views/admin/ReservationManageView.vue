<template>
  <div class="panel">
    <el-row :gutter="10">
      <el-col :span="6"><el-input v-model="query.keyword" placeholder="学生姓名/账号" @change="load" /></el-col>
      <el-col :span="6">
        <el-select v-model="query.roomId" clearable placeholder="自习室" style="width: 100%" @change="load">
          <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
      </el-col>
      <el-col :span="6">
        <el-select v-model="query.status" clearable style="width: 100%" @change="load">
          <el-option label="已预约" value="RESERVED" />
          <el-option label="已签到" value="CHECKED_IN" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELED" />
          <el-option label="已违约" value="VIOLATED" />
        </el-select>
      </el-col>
    </el-row>

    <el-table :data="list" style="margin-top: 12px">
      <el-table-column prop="username" label="账号" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="roomName" label="自习室" />
      <el-table-column prop="seatNo" label="座位号" width="100" />
      <el-table-column prop="startTime" label="开始" width="170" />
      <el-table-column prop="endTime" label="结束" width="170" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" :disabled="row.status !== 'RESERVED'" @click="cancel(row)">取消预约</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { reservationApi, roomApi } from '../../api'

const query = reactive({ keyword: '', roomId: undefined, status: '' })
const rooms = ref([])
const list = ref([])

const load = async () => { list.value = await reservationApi.adminList(query) }

const cancel = async (row) => {
  await reservationApi.adminCancel(row.id)
  ElMessage.success('操作成功')
  load()
}

onMounted(async () => {
  rooms.value = await roomApi.list()
  load()
})
</script>
