<template>
  <div class="panel">
    <el-row :gutter="16">
      <el-col :span="12">
        <h3>个人信息</h3>
        <el-form :model="profile" label-width="90px">
          <el-form-item label="账号"><el-input v-model="profile.username" disabled /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="profile.realName" /></el-form-item>
          <el-form-item label="性别"><el-input v-model="profile.gender" /></el-form-item>
          <el-form-item label="手机"><el-input v-model="profile.phone" /></el-form-item>
          <el-form-item label="信用分"><el-input :model-value="profile.creditScore" disabled /></el-form-item>
          <el-form-item><el-button type="primary" @click="save">保存</el-button></el-form-item>
        </el-form>
      </el-col>
      <el-col :span="12">
        <h3>修改密码</h3>
        <el-form :model="pwd" label-width="90px">
          <el-form-item label="旧密码"><el-input v-model="pwd.oldPassword" show-password /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwd.newPassword" show-password /></el-form-item>
          <el-form-item><el-button type="warning" @click="changePwd">修改密码</el-button></el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../../api'

const profile = reactive({})
const pwd = reactive({ oldPassword: '', newPassword: '' })

const load = async () => {
  Object.assign(profile, await userApi.profile())
}

const save = async () => {
  await userApi.updateProfile({ realName: profile.realName, gender: profile.gender, phone: profile.phone })
  ElMessage.success('已保存')
}

const changePwd = async () => {
  await userApi.changePassword(pwd)
  ElMessage.success('密码已更新，请重新登录')
}

onMounted(load)
</script>
