<template>
  <el-container class="page-wrap">
    <el-aside width="220px" class="aside">
      <h3>学生端</h3>
      <el-menu router :default-active="$route.path">
        <el-menu-item index="/student/seats">座位预约</el-menu-item>
        <el-menu-item index="/student/reservations">预约记录</el-menu-item>
        <el-menu-item index="/student/profile">个人中心</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div>校园自习室座位预约系统</div>
        <div>
          <span style="margin-right: 12px">{{ auth.realName || '同学' }}</span>
          <el-button size="small" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const logout = () => {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.page-wrap { min-height: 100vh; }
.aside { background: #0f766e; color: #fff; padding: 12px; }
.aside h3 { color: #fff; text-align: center; }
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}
</style>
