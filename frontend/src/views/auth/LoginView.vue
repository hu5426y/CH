<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <h2>校园自习室座位预约系统</h2>
      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入账号/学号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="login">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="tips">
        <el-link type="primary" @click="$router.push('/register')">学生注册</el-link>
      </div>
      <p class="default">默认管理员：admin / password</p>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../../api'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const form = reactive({ username: '', password: '' })

const login = async () => {
  const data = await authApi.login(form)
  auth.setLogin(data)
  ElMessage.success('登录成功')
  if (data.role === 'ADMIN') router.push('/admin/dashboard')
  else router.push('/student/seats')
}
</script>

<style scoped>
.auth-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
.auth-card {
  width: 420px;
  background: #fff;
  border-radius: 18px;
  padding: 28px;
  box-shadow: 0 10px 35px rgba(15, 23, 42, 0.1);
}
.auth-card h2 { margin-top: 0; color: #134e4a; }
.tips { display: flex; justify-content: flex-end; }
.default { color: #6b7280; font-size: 12px; }
</style>
