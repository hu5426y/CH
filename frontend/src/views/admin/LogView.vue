<template>
  <div class="panel">
    <el-row :gutter="12">
      <el-col :span="6"><el-input v-model="query.userId" placeholder="用户ID" @change="load" /></el-col>
      <el-col :span="6"><el-input v-model="query.operationType" placeholder="操作类型" @change="load" /></el-col>
    </el-row>
    <el-table :data="list" style="margin-top: 12px">
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="operationType" label="操作类型" width="220" />
      <el-table-column prop="content" label="操作内容" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="operationTime" label="时间" width="180" />
      <el-table-column prop="result" label="结果" width="180" />
    </el-table>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { logApi } from '../../api'

const query = reactive({ userId: '', operationType: '' })
const list = ref([])

const load = async () => {
  list.value = await logApi.list({ userId: query.userId || undefined, operationType: query.operationType || undefined })
}

onMounted(load)
</script>
