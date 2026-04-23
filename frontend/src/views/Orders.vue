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
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="status" label="订单状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'PENDING_PAYMENT'" 
              type="primary" 
              link 
              :loading="scope.row.paying"
              @click="payOrder(scope.row)"
            >
              支付
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING_PAYMENT'" 
              type="warning" 
              link 
              :loading="scope.row.cancelling"
              @click="cancelOrder(scope.row)"
            >
              取消
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PAID'" 
              type="danger" 
              link 
              :loading="scope.row.refunding"
              @click="refundOrder(scope.row)"
            >
              退款
            </el-button>
            <span v-else class="no-action">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import api from '@/utils/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const orders = ref([])

const fetchOrders = async () => {
  try {
    const response = await api.get('/api/orders/my')
    orders.value = response.data
  } catch (error) {
    console.error('Failed to fetch orders:', error)
  }
}

const getStatusType = (status) => {
  const typeMap = {
    'PENDING_PAYMENT': 'warning',
    'PAID': 'success',
    'CANCELLED': 'info',
    'REFUNDED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING_PAYMENT': '待支付',
    'PAID': '已支付',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款'
  }
  return textMap[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const payOrder = async (order) => {
  try {
    await ElMessageBox.confirm(`确定要支付吗？需要 ${order.price} 积分`, '确认支付', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    order.paying = true
    await api.post(`/api/orders/${order.id}/pay`)
    ElMessage.success('支付成功')
    order.status = 'PAID'
    await userStore.refreshUserInfo()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to pay:', error)
    }
  } finally {
    order.paying = false
  }
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    order.cancelling = true
    await api.post(`/api/orders/${order.id}/cancel`)
    ElMessage.success('订单已取消')
    order.status = 'CANCELLED'
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to cancel:', error)
    }
  } finally {
    order.cancelling = false
  }
}

const refundOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要申请退款吗？积分将原路返回', '确认退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    order.refunding = true
    await api.post(`/api/orders/${order.id}/refund`)
    ElMessage.success('退款成功')
    order.status = 'REFUNDED'
    await userStore.refreshUserInfo()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to refund:', error)
    }
  } finally {
    order.refunding = false
  }
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

.no-action {
  color: #c0c4cc;
}
</style>
