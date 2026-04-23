<template>
  <div class="cart-container">
    <NavBar />
    
    <div class="content">
      <div class="page-header">
        <h1>购物车</h1>
      </div>
      
      <el-empty v-if="cartItems.length === 0" description="购物车是空的，快去选购商品吧" :image-size="100" />
      
      <div v-else>
        <el-table :data="cartItems" stripe style="width: 100%">
          <el-table-column label="商品" min-width="300">
            <template #default="scope">
              <div class="product-info-cell">
                <img 
                  :src="scope.row.product?.imagePath || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder%20image&image_size=square'" 
                  class="product-thumb"
                />
                <div class="product-details">
                  <h4 class="product-name">{{ scope.row.product?.name }}</h4>
                  <p class="product-price">
                    <span class="price-text">{{ scope.row.product?.price }} 积分/件</span>
                    <span v-if="scope.row.product?.stock > 0" class="stock-text">库存: {{ scope.row.product?.stock }}</span>
                    <span v-else class="out-of-stock">已售罄</span>
                  </p>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="150">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.quantity"
                :min="1"
                :max="scope.row.product?.stock || 1"
                :disabled="scope.row.product?.stock <= 0"
                size="small"
                controls-position="right"
                @change="updateQuantity(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="150">
            <template #default="scope">
              <span class="subtotal-price">{{ scope.row.product?.price * scope.row.quantity }} 积分</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button type="danger" link @click="removeItem(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="cart-footer">
          <div class="cart-summary">
            <span class="total-label">总计:</span>
            <span class="total-price">{{ totalPrice }} 积分</span>
          </div>
          <div class="cart-actions">
            <el-button @click="clearCart">清空购物车</el-button>
            <el-button type="primary" :disabled="!canCheckout" :loading="checkingOut" @click="checkout">
              结算
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import api from '@/utils/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const cartItems = ref([])
const checkingOut = ref(false)

const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => {
    return sum + (item.product?.price || 0) * item.quantity
  }, 0)
})

const canCheckout = computed(() => {
  if (cartItems.value.length === 0) return false
  const hasOutOfStock = cartItems.value.some(item => !item.product || item.product.stock <= 0)
  if (hasOutOfStock) return false
  const hasInsufficientStock = cartItems.value.some(item => item.quantity > item.product?.stock)
  if (hasInsufficientStock) return false
  return userStore.userInfo?.points >= totalPrice.value
})

const fetchCart = async () => {
  try {
    const response = await api.get('/api/cart')
    cartItems.value = response.data
  } catch (error) {
    console.error('Failed to fetch cart:', error)
  }
}

const updateQuantity = async (item) => {
  try {
    await api.put(`/api/cart/${item.id}`, { quantity: item.quantity })
    ElMessage.success('数量已更新')
  } catch (error) {
    console.error('Failed to update quantity:', error)
    fetchCart()
  }
}

const removeItem = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.delete(`/api/cart/${item.id}`)
    ElMessage.success('已删除')
    fetchCart()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to remove item:', error)
    }
  }
}

const clearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.delete('/api/cart')
    ElMessage.success('购物车已清空')
    cartItems.value = []
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to clear cart:', error)
    }
  }
}

const checkout = async () => {
  if (!canCheckout.value) return
  
  try {
    await ElMessageBox.confirm(`确定要结算吗？总计 ${totalPrice.value} 积分`, '确认结算', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    checkingOut.value = true
    
    for (const item of cartItems.value) {
      await api.post('/api/orders', {
        productId: item.product.id,
        quantity: item.quantity
      })
    }
    
    await api.delete('/api/cart')
    
    ElMessage.success('结算成功！')
    await userStore.refreshUserInfo()
    router.push('/orders')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to checkout:', error)
    }
  } finally {
    checkingOut.value = false
  }
}

onMounted(() => {
  fetchCart()
})
</script>

<style scoped>
.cart-container {
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

.product-info-cell {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.product-details {
  flex: 1;
}

.product-name {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}

.product-price {
  margin: 0;
  font-size: 14px;
}

.price-text {
  color: #f56c6c;
  font-weight: bold;
  margin-right: 15px;
}

.stock-text {
  color: #909399;
}

.out-of-stock {
  color: #f56c6c;
}

.subtotal-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.cart-summary {
  display: flex;
  align-items: center;
  gap: 10px;
}

.total-label {
  font-size: 16px;
  color: #606266;
}

.total-price {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.cart-actions {
  display: flex;
  gap: 15px;
}
</style>
