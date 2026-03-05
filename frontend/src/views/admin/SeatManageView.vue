<template>
  <div class="panel">
    <el-row :gutter="10">
      <el-col :span="6">
        <el-select v-model="query.roomId" clearable placeholder="自习室" style="width: 100%" @change="load">
          <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
      </el-col>
      <el-col :span="6"><el-input v-model="query.seatNo" placeholder="座位号" @change="load" /></el-col>
      <el-col :span="6"><el-button type="primary" @click="openDialog()">新增座位</el-button></el-col>
    </el-row>

    <el-table :data="list" style="margin-top: 12px">
      <el-table-column prop="roomId" label="自习室ID" width="100" />
      <el-table-column prop="seatNo" label="座位号" width="120" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="underMaintenance" label="维修" width="100" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" title="座位信息" width="480px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="自习室">
          <el-select v-model="form.roomId" style="width: 100%">
            <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="座位号"><el-input v-model="form.seatNo" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="空闲" value="FREE" />
            <el-option label="已预约" value="RESERVED" />
            <el-option label="占用" value="OCCUPIED" />
            <el-option label="维修" value="MAINTENANCE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否维修"><el-switch v-model="form.underMaintenance" :active-value="1" :inactive-value="0" /></el-form-item>
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
import { roomApi, seatApi } from '../../api'

const list = ref([])
const rooms = ref([])
const query = reactive({ roomId: undefined, seatNo: '' })
const visible = ref(false)
const editingId = ref(null)
const form = reactive({ roomId: undefined, seatNo: '', status: 'FREE', underMaintenance: 0 })

const loadRooms = async () => { rooms.value = await roomApi.list() }
const load = async () => { list.value = await seatApi.list(query) }

const openDialog = (row) => {
  editingId.value = row?.id || null
  Object.assign(form, row || { roomId: rooms.value[0]?.id, seatNo: '', status: 'FREE', underMaintenance: 0 })
  visible.value = true
}

const save = async () => {
  if (editingId.value) await seatApi.update(editingId.value, form)
  else await seatApi.create(form)
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

const remove = async (row) => {
  await seatApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  await loadRooms()
  await load()
})
</script>
