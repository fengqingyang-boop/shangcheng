<template>
  <el-menu
    :default-active="activeIndex"
    class="nav-menu"
    mode="horizontal"
    background-color="#303133"
    text-color="#fff"
    active-text-color="#409eff"
    router
  >
    <el-menu-item index="/">
      <el-icon><ShoppingCart /></el-icon>
      <span>商城首页</span>
    </el-menu-item>
    
    <el-menu-item v-if="userStore.isLoggedIn" index="/cart">
      <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge">
        <el-icon><Goods /></el-icon>
        <span>购物车</span>
      </el-badge>
    </el-menu-item>
    
    <el-menu-item v-if="userStore.isLoggedIn" index="/orders">
      <el-icon><Document /></el-icon>
      <span>我的订单</span>
    </el-menu-item>
    
    <el-menu-item v-if="userStore.isAdmin" index="/admin">
      <el-icon><Setting /></el-icon>
      <span>管理后台</span>
    </el-menu-item>
    
    <div class="user-info" v-if="userStore.isLoggedIn">
      <span class="points">积分: {{ userStore.userInfo?.points }}</span>
      <el-dropdown @command="handleCommand">
        <span class="user-name">
          <el-icon><User /></el-icon>
          {{ userStore.userInfo?.username }}
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <div class="login-link" v-else>
      <router-link to="/login">登录</router-link>
      <span class="divider">|</span>
      <router-link to="/register">注册</router-link>
    </div>
  </el-menu>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import api from '@/utils/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const cartCount = ref(0)

const activeIndex = computed(() => route.path)

const fetchCartCount = async () => {
  if (!userStore.isLoggedIn) {
    cartCount.value = 0
    return
  }
  try {
    const response = await api.get('/api/cart/count')
    cartCount.value = response.data.count || 0
  } catch (error) {
    console.error('Failed to fetch cart count:', error)
  }
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.logout()
      cartCount.value = 0
      router.push('/login')
    } catch {
      // 用户取消
    }
  }
}

watch(() => userStore.isLoggedIn, () => {
  fetchCartCount()
})

onMounted(() => {
  fetchCartCount()
})
</script>

<style scoped>
.nav-menu {
  margin-bottom: 0;
  border: none;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.cart-badge {
  height: 100%;
}

.user-info {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 20px;
  color: #fff;
}

.points {
  color: #ffd04b;
  font-size: 14px;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.login-link {
  margin-left: auto;
  color: #fff;
  font-size: 14px;
}

.login-link a {
  color: #fff;
  text-decoration: none;
}

.login-link a:hover {
  color: #409eff;
}

.divider {
  margin: 0 10px;
  color: #606266;
}
</style>
