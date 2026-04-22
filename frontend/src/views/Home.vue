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
                @click="openBuyDialog(product)"
              >
                {{ product.stock > 0 ? '立即购买' : '已售罄' }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <el-dialog
      v-model="buyDialogVisible"
      title="确认购买"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="buy-dialog-content">
        <div class="product-preview">
          <img 
            :src="selectedProduct?.imagePath || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder%20image&image_size=square'" 
            :alt="selectedProduct?.name"
            class="product-preview-img"
          />
          <div class="product-preview-info">
            <h3>{{ selectedProduct?.name }}</h3>
            <p class="product-preview-price">
              <el-icon><Coin /></el-icon>
              {{ selectedProduct?.price }} 积分/件
            </p>
            <p class="product-preview-stock">库存: {{ selectedProduct?.stock }} 件</p>
          </div>
        </div>
        
        <div class="quantity-section">
          <span class="quantity-label">购买数量:</span>
          <el-input-number
            v-model="buyQuantity"
            :min="1"
            :max="selectedProduct?.stock || 1"
            :controls-position="'right'"
            size="large"
          />
        </div>
        
        <div class="total-section">
          <span>总积分:</span>
          <span class="total-price">{{ totalPrice }} 积分</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="buyDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="buying" @click="confirmBuy">
            确认购买
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import api from '@/utils/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const products = ref([])
const buyDialogVisible = ref(false)
const selectedProduct = ref(null)
const buyQuantity = ref(1)
const buying = ref(false)

const totalPrice = computed(() => {
  return selectedProduct.value ? selectedProduct.value.price * buyQuantity.value : 0
})

const fetchProducts = async () => {
  try {
    const response = await api.get('/api/products')
    products.value = response.data
  } catch (error) {
    console.error('Failed to fetch products:', error)
  }
}

const openBuyDialog = (product) => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  selectedProduct.value = product
  buyQuantity.value = 1
  buyDialogVisible.value = true
}

const confirmBuy = async () => {
  if (!selectedProduct.value) return
  
  buying.value = true
  try {
    await api.post('/api/orders', {
      productId: selectedProduct.value.id,
      quantity: buyQuantity.value
    })
    
    ElMessage({
      message: '购买成功！',
      type: 'success',
      offset: 60,
      duration: 1500
    })
    
    buyDialogVisible.value = false
    await userStore.refreshUserInfo()
    fetchProducts()
  } catch (error) {
    console.error('Failed to buy:', error)
  } finally {
    buying.value = false
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

.buy-dialog-content {
  padding: 10px 0;
}

.product-preview {
  display: flex;
  gap: 20px;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.product-preview-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.product-preview-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.product-preview-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.product-preview-price {
  margin: 0 0 5px 0;
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 5px;
}

.product-preview-stock {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.quantity-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.quantity-label {
  font-size: 14px;
  color: #606266;
}

.total-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.total-section span:first-child {
  font-size: 14px;
  color: #606266;
}

.total-price {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.dialog-footer {
  text-align: right;
}
</style>
