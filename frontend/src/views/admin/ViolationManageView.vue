<template>
  <div class="panel">
    <el-input v-model="keyword" placeholder="学生姓名/账号" style="width: 280px" @change="load" />
    <el-table :data="list" style="margin-top: 12px">
      <el-table-column prop="username" label="账号" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="violationType" label="违约类型" width="120" />
      <el-table-column prop="scoreDeducted" label="扣分" width="100" />
      <el-table-column prop="violationTime" label="违约时间" width="180" />
      <el-table-column prop="processStatus" label="处理状态" width="120" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" :disabled="row.processStatus !== 'ACTIVE'" @click="revoke(row)">撤销违约</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { violationApi } from '../../api'

const keyword = ref('')
const list = ref([])
const load = async () => { list.value = await violationApi.list(keyword.value) }

const revoke = async (row) => {
  await violationApi.revoke(row.id)
  ElMessage.success('已撤销')
  load()
}

onMounted(load)
</script>
