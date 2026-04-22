<template>
  <div class="orders-container">
    <NavBar />
    
    <div class="content">
      <div class="page-header">
        <h1>我的订单</h1>
      </div>
      
      <el-empty v-if="orders.length === 0" description="暂无订单记录" :image-size="100" />
      
      <el-table :data="orders" v-else stripe style="width: 100%">
        <el-table-column prop="productName" label="商品名称" min-width="200" />
        <el-table-column label="商品图片" width="120">
          <template #default="scope">
            <img 
              :src="scope.row.productImage || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder%20image&image_size=square'" 
              class="product-thumb"
            />
          </template>
        </el-table-column>
        <el-table-column prop="price" label="花费积分" width="120">
          <template #default="scope">
            <span class="price">{{ scope.row.price }} 积分</span>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="createdAt" label="购买时间" min-width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import api from '@/utils/api'

const orders = ref([])

const fetchOrders = async () => {
  try {
    const response = await api.get('/api/orders/my')
    orders.value = response.data
  } catch (error) {
    console.error('Failed to fetch orders:', error)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-container {
  min-height: 100vh;
}

.content {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  color: #303133;
}

.product-thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}
</style>
