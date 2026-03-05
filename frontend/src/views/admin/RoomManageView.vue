<template>
  <div class="panel">
    <el-button type="primary" @click="openDialog()">新增自习室</el-button>
    <el-table :data="list" style="margin-top: 12px">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="floor" label="楼层" width="100" />
      <el-table-column prop="seatCount" label="座位数" width="120" />
      <el-table-column prop="openTime" label="开放时间" width="120" />
      <el-table-column prop="closeTime" label="关闭时间" width="120" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" title="自习室信息" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="楼层"><el-input-number v-model="form.floor" :min="1" /></el-form-item>
        <el-form-item label="座位数"><el-input-number v-model="form.seatCount" :min="1" /></el-form-item>
        <el-form-item label="开放时间"><el-time-picker v-model="form.openTime" value-format="HH:mm:ss" /></el-form-item>
        <el-form-item label="关闭时间"><el-time-picker v-model="form.closeTime" value-format="HH:mm:ss" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="开放" value="OPEN" />
            <el-option label="关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { roomApi } from '../../api'

const list = ref([])
const visible = ref(false)
const editingId = ref(null)
const form = reactive({ name: '', floor: 1, seatCount: 1, openTime: '08:00:00', closeTime: '22:00:00', status: 'OPEN' })

const load = async () => { list.value = await roomApi.list() }

const openDialog = (row) => {
  editingId.value = row?.id || null
  Object.assign(form, row || { name: '', floor: 1, seatCount: 1, openTime: '08:00:00', closeTime: '22:00:00', status: 'OPEN' })
  visible.value = true
}

const save = async () => {
  if (editingId.value) await roomApi.update(editingId.value, form)
  else await roomApi.create(form)
  visible.value = false
  ElMessage.success('保存成功')
  load()
}

const remove = async (row) => {
  await roomApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
