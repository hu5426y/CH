<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <h2>学生注册</h2>
      <el-form :model="form" label-position="top">
        <el-form-item label="账号/学号"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" show-password /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="submit">提交注册</el-button>
        </el-form-item>
      </el-form>
      <el-link @click="$router.push('/login')">返回登录</el-link>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../../api'

const router = useRouter()
const form = reactive({ username: '', realName: '', password: '', gender: '男', phone: '', studentNo: '' })

const submit = async () => {
  form.studentNo = form.username
  await authApi.register(form)
  ElMessage.success('注册成功，请登录')
  router.push('/login')
}
</script>

<style scoped>
.auth-wrap { min-height: 100vh; display: grid; place-items: center; }
.auth-card { width: 460px; background: #fff; padding: 28px; border-radius: 18px; }
</style>
