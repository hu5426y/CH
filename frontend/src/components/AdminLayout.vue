<template>
  <el-container class="page-wrap">
    <el-aside width="230px" class="aside">
      <h3>管理后台</h3>
      <el-menu router :default-active="$route.path">
        <el-menu-item index="/admin/dashboard">总览</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/rooms">自习室管理</el-menu-item>
        <el-menu-item index="/admin/seats">座位管理</el-menu-item>
        <el-menu-item index="/admin/reservations">预约管理</el-menu-item>
        <el-menu-item index="/admin/violations">违约管理</el-menu-item>
        <el-menu-item index="/admin/rules">规则配置</el-menu-item>
        <el-menu-item index="/admin/logs">系统日志</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div>校园自习室座位预约系统 - 管理端</div>
        <div>
          <span style="margin-right: 12px">{{ auth.realName || '管理员' }}</span>
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
.aside { background: #134e4a; color: #fff; padding: 12px; }
.aside h3 { color: #fff; text-align: center; }
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}
</style>
