<template>
  <div class="panel">
    <el-input v-model="keyword" placeholder="按账号/姓名/学号搜索" style="width: 300px" @change="load" />
    <el-table :data="list" style="margin-top: 12px">
      <el-table-column prop="username" label="账号" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="phone" label="手机" width="150" />
      <el-table-column prop="creditScore" label="信用分" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" @click="toggle(row)">{{ row.status === 'ENABLED' ? '禁用' : '启用' }}</el-button>
          <el-button size="small" type="warning" @click="reset(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../../api'

const keyword = ref('')
const list = ref([])

const load = async () => {
  list.value = await userApi.listStudents(keyword.value)
}

const toggle = async (row) => {
  await userApi.updateStatus({ userId: row.id, enabled: row.status !== 'ENABLED' })
  ElMessage.success('状态已更新')
  load()
}

const reset = async (row) => {
  await ElMessageBox.confirm(`确认将 ${row.realName} 的密码重置为 123456?`, '提示')
  await userApi.resetPassword({ userId: row.id, newPassword: '123456' })
  ElMessage.success('密码已重置')
}

onMounted(load)
</script>
