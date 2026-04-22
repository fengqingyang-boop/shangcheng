<template>
  <div class="home-container">
    <NavBar />
    
    <div class="content">
      <div class="page-header">
        <h1>商品列表</h1>
        <p>用积分购买你喜欢的商品</p>
      </div>
      
      <el-empty v-if="products.length === 0" description="暂无商品" :image-size="100" />
      
      <el-row :gutter="20" v-else>
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in products" :key="product.id">
          <el-card class="product-card" shadow="hover">
            <template #header>
              <div class="card-image">
                <img 
                  :src="product.imagePath || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder%20image&image_size=square'" 
                  :alt="product.name"
                />
              </div>
            </template>
            
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc" v-if="product.description">{{ product.description }}</p>
              <div class="product-meta">
                <span class="price">
                  <el-icon><Coin /></el-icon>
                  {{ product.price }} 积分
                </span>
                <span class="stock">库存: {{ product.stock }}</span>
              </div>
              <el-button 
                type="primary" 
                class="buy-btn" 
                :disabled="product.stock <= 0"
                @click="handleBuy(product)"
              >
                {{ product.stock > 0 ? '立即购买' : '已售罄' }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import api from '@/utils/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const products = ref([])

const fetchProducts = async () => {
  try {
    const response = await api.get('/api/products')
    products.value = response.data
  } catch (error) {
    console.error('Failed to fetch products:', error)
  }
}

const handleBuy = async (product) => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要花费 ${product.price} 积分购买 "${product.name}" 吗？`,
      '确认购买',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await api.post('/api/orders', {
      productId: product.id,
      quantity: 1
    })
    
    ElMessage.success('购买成功！')
    await userStore.refreshUserInfo()
    fetchProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to buy:', error)
    }
  }
}

onMounted(() => {
  fetchProducts()
  if (userStore.isLoggedIn) {
    userStore.refreshUserInfo()
  }
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
}

.content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  margin-bottom: 10px;
  color: #303133;
}

.page-header p {
  color: #909399;
}

.product-card {
  margin-bottom: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.el-card__header) {
  padding: 0;
  flex-shrink: 0;
}

.card-image {
  height: 200px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 10px 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0;
}

.product-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-shrink: 0;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 5px;
}

.stock {
  font-size: 13px;
  color: #909399;
}

.buy-btn {
  width: 100%;
  margin-top: auto;
  flex-shrink: 0;
}
</style>
