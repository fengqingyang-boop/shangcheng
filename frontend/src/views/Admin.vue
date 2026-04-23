<template>
  <div class="admin-container">
    <NavBar />
    
    <div class="content">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="商品管理" name="products">
          <div class="tab-content">
            <div class="action-bar">
              <el-button type="primary" @click="showAddDialog = true">
                <el-icon><Plus /></el-icon>
                添加商品
              </el-button>
            </div>
            
            <el-table :data="products" stripe style="width: 100%">
              <el-table-column prop="name" label="商品名称" min-width="150" />
              <el-table-column label="商品图片" width="120">
                <template #default="scope">
                  <img 
                    :src="scope.row.imagePath || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder%20image&image_size=square'" 
                    class="product-thumb"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
              <el-table-column prop="price" label="积分价格" width="120">
                <template #default="scope">
                  <span class="price">{{ scope.row.price }} 积分</span>
                </template>
              </el-table-column>
              <el-table-column prop="stock" label="库存" width="80" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'info'">
                    {{ scope.row.status === 'ACTIVE' ? '上架中' : '已下架' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="280" fixed="right">
                <template #default="scope">
                  <el-button type="primary" link @click="editProduct(scope.row)">编辑</el-button>
                  <el-button 
                    :type="scope.row.status === 'ACTIVE' ? 'warning' : 'success'" 
                    link 
                    @click="toggleProductStatus(scope.row)"
                  >
                    {{ scope.row.status === 'ACTIVE' ? '下架' : '上架' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="用户管理" name="users">
          <div class="tab-content">
            <el-table :data="users" stripe style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" width="150" />
              <el-table-column prop="email" label="邮箱" width="200" />
              <el-table-column prop="role" label="角色" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'primary'">
                    {{ scope.row.role === 'ADMIN' ? '管理员' : '普通用户' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="points" label="积分" width="160">
                <template #default="scope">
                  <el-input-number
                    v-model="scope.row.points"
                    :min="0"
                    :max="999999999"
                    size="small"
                    controls-position="right"
                    @change="updatePoints(scope.row)"
                    style="width: 130px"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="注册时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="订单管理" name="orders">
          <div class="tab-content">
            <el-table :data="allOrders" stripe style="width: 100%">
              <el-table-column prop="id" label="订单ID" width="80" />
              <el-table-column prop="username" label="购买用户" width="100" />
              <el-table-column prop="productName" label="商品名称" min-width="150" />
              <el-table-column prop="price" label="花费积分" width="100">
                <template #default="scope">
                  <span class="price">{{ scope.row.price }} 积分</span>
                </template>
              </el-table-column>
              <el-table-column prop="quantity" label="数量" width="60" />
              <el-table-column prop="status" label="订单状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <el-dialog
      :title="isEdit ? '编辑商品' : '添加商品'"
      v-model="showAddDialog"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="productForm" :rules="productRules" ref="productFormRef" label-width="80px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="积分价格" prop="price">
          <el-input-number v-model="productForm.price" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload
            class="avatar-uploader"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            accept="image/*"
          >
            <img v-if="imagePreview" :src="imagePreview" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">点击上传商品图片</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitProduct">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import api from '@/utils/api'

const activeTab = ref('products')
const products = ref([])
const users = ref([])
const allOrders = ref([])

const showAddDialog = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const productFormRef = ref(null)
const selectedFile = ref(null)
const imagePreview = ref('')

const messageTimer = ref(null)
const messageCount = ref(0)

const productForm = reactive({
  id: null,
  name: '',
  description: '',
  price: 1,
  stock: 1
})

const productRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入积分价格', trigger: 'blur' }]
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

const fetchProducts = async () => {
  try {
    const response = await api.get('/api/products/all')
    products.value = response.data
  } catch (error) {
    console.error('Failed to fetch products:', error)
  }
}

const fetchUsers = async () => {
  try {
    const response = await api.get('/api/users')
    users.value = response.data
  } catch (error) {
    console.error('Failed to fetch users:', error)
  }
}

const fetchOrders = async () => {
  try {
    const response = await api.get('/api/orders')
    allOrders.value = response.data
  } catch (error) {
    console.error('Failed to fetch orders:', error)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const resetForm = () => {
  productForm.id = null
  productForm.name = ''
  productForm.description = ''
  productForm.price = 1
  productForm.stock = 1
  selectedFile.value = null
  imagePreview.value = ''
  isEdit.value = false
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const editProduct = (product) => {
  resetForm()
  isEdit.value = true
  productForm.id = product.id
  productForm.name = product.name
  productForm.description = product.description || ''
  productForm.price = product.price
  productForm.stock = product.stock
  imagePreview.value = product.imagePath || ''
  showAddDialog.value = true
}

const toggleProductStatus = async (product) => {
  try {
    const action = product.status === 'ACTIVE' ? '下架' : '上架'
    await ElMessageBox.confirm(
      `确定要${action}商品 "${product.name}" 吗？`,
      `确认${action}`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await api.post(`/api/products/${product.id}/toggle-status`)
    ElMessage.success(`${action}成功`)
    fetchProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to toggle status:', error)
    }
  }
}

const updatePoints = async (user) => {
  try {
    await api.put(`/api/users/${user.id}/points`, { points: user.points })
    
    if (messageCount.value < 2) {
      ElMessage({
        message: '积分更新成功',
        type: 'success',
        offset: 60,
        duration: 1500
      })
      messageCount.value++
    }
    
    if (messageTimer.value) {
      clearTimeout(messageTimer.value)
    }
    
    messageTimer.value = setTimeout(() => {
      messageCount.value = 0
    }, 2000)
  } catch (error) {
    console.error('Failed to update points:', error)
    fetchUsers()
  }
}

const submitProduct = async () => {
  if (!productFormRef.value) return
  
  await productFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const formData = new FormData()
        formData.append('product', JSON.stringify({
          name: productForm.name,
          description: productForm.description,
          price: productForm.price,
          stock: productForm.stock
        }))
        
        if (selectedFile.value) {
          formData.append('image', selectedFile.value)
        }
        
        if (isEdit.value) {
          await api.put(`/api/products/${productForm.id}`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          })
          ElMessage.success('编辑成功')
        } else {
          await api.post('/api/products', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          })
          ElMessage.success('添加成功')
        }
        
        showAddDialog.value = false
        fetchProducts()
      } catch (error) {
        console.error('Failed to submit:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

watch(showAddDialog, (val) => {
  if (!val) {
    resetForm()
  }
})

watch(activeTab, (val) => {
  if (val === 'products') {
    fetchProducts()
  } else if (val === 'users') {
    fetchUsers()
  } else if (val === 'orders') {
    fetchOrders()
  }
})

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
}

.content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.tab-content {
  padding: 10px 0;
}

.action-bar {
  margin-bottom: 15px;
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

.avatar-uploader {
  width: 100px;
  height: 100px;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.dialog-footer {
  text-align: right;
}
</style>
