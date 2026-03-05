<template>
  <div class="panel">
    <el-table :data="list">
      <el-table-column prop="ruleKey" label="规则键" width="260" />
      <el-table-column prop="description" label="说明" />
      <el-table-column label="规则值" width="220">
        <template #default="{ row }">
          <el-input v-model="row.ruleValue" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="save(row)">保存</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ruleApi } from '../../api'

const list = ref([])
const load = async () => { list.value = await ruleApi.list() }

const save = async (row) => {
  await ruleApi.save({ ruleKey: row.ruleKey, ruleValue: String(row.ruleValue), description: row.description })
  ElMessage.success('规则已更新')
}

onMounted(load)
</script>
