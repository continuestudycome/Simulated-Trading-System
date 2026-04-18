<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAccountByUserId, createOrder } from '@/api/trade'
import type { Account } from '@/types'

const userId = ref<number>(1)
const accounts = ref<Account[]>([])
const loading = ref(false)

const orderForm = reactive({
  stockCode: '',
  type: 1,
  price: 0,
  quantity: 100,
  status: 0
})

const rules = {
  stockCode: [
    { required: true, message: '请输入股票代码', trigger: 'blur' },
    { min: 6, max: 10, message: '股票代码长度为6-10位', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入委托价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格最小为0.01元', trigger: 'blur' }
  ],
  quantity: [
    { required: true, message: '请输入委托数量', trigger: 'blur' },
    { type: 'number', min: 100, message: '数量至少100股', trigger: 'blur' }
  ]
}

const formRef = ref()

const fetchAccounts = async () => {
  loading.value = true
  try {
    const res = await getAccountByUserId(userId.value)
    accounts.value = res.data
  } catch (error) {
    console.error('获取账户信息失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await createOrder({
          userId: userId.value,
          stockCode: orderForm.stockCode,
          type: orderForm.type,
          price: orderForm.price,
          quantity: orderForm.quantity,
          status: orderForm.status,
          createTime: new Date().toISOString().substring(0, 19)
        })
        ElMessage.success('下单成功')
        handleReset()
        fetchAccounts()
      } catch (error) {
        console.error('下单失败:', error)
      }
    }
  })
}

const handleReset = () => {
  orderForm.stockCode = ''
  orderForm.price = 0
  orderForm.quantity = 100
  orderForm.status = 0
  formRef.value?.clearValidate()
}

onMounted(() => {
  fetchAccounts()
})
</script>

<template>
  <div class="trading-system">
    <el-container>
      <el-header>
        <h1>模拟交易系统</h1>
      </el-header>

      <el-main>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="account-card">
              <template #header>
                <div class="card-header">
                  <span>账户信息</span>
                  <el-button type="primary" size="small" @click="fetchAccounts" :loading="loading">
                    刷新
                  </el-button>
                </div>
              </template>

              <el-table :data="accounts" style="width: 100%">
                <el-table-column prop="id" label="账户ID" width="100" />
                <el-table-column prop="userId" label="用户ID" width="100" />
                <el-table-column prop="balance" label="可用余额" width="150">
                  <template #default="{ row }">
                    ¥{{ row.balance.toFixed(2) }}
                  </template>
                </el-table-column>
                <el-table-column prop="frozen" label="冻结金额" width="150">
                  <template #default="{ row }">
                    ¥{{ row.frozen.toFixed(2) }}
                  </template>
                </el-table-column>
                <el-table-column label="总资产">
                  <template #default="{ row }">
                    ¥{{ (row.balance + row.frozen).toFixed(2) }}
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card class="order-card">
              <template #header>
                <div class="card-header">
                  <span>交易下单</span>
                </div>
              </template>

              <el-form
                ref="formRef"
                :model="orderForm"
                :rules="rules"
                label-width="100px"
              >
                <el-form-item label="用户ID">
                  <el-input-number v-model="userId" :min="1" disabled />
                </el-form-item>

                <el-form-item label="股票代码" prop="stockCode">
                  <el-input
                    v-model="orderForm.stockCode"
                    placeholder="请输入股票代码"
                    maxlength="10"
                  />
                </el-form-item>

                <el-form-item label="交易类型" prop="type">
                  <el-radio-group v-model="orderForm.type">
                    <el-radio :value="1">买入</el-radio>
                    <el-radio :value="2">卖出</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="委托价格" prop="price">
                  <el-input-number
                    v-model="orderForm.price"
                    :min="0.01"
                    :precision="2"
                    :step="0.01"
                    controls-position="right"
                    style="width: 100%"
                  />
                </el-form-item>

                <el-form-item label="委托数量" prop="quantity">
                  <el-input-number
                    v-model="orderForm.quantity"
                    :min="100"
                    :step="100"
                    controls-position="right"
                    style="width: 100%"
                  />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="handleSubmit" style="width: 100%">
                    提交订单
                  </el-button>
                  <el-button @click="handleReset" style="width: 100%; margin-left: 0">
                    重置
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="24">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>操作说明</span>
                </div>
              </template>
              <el-descriptions :column="3" border>
                <el-descriptions-item label="买入规则">
                  输入股票代码、价格和数量，确保账户余额充足
                </el-descriptions-item>
                <el-descriptions-item label="卖出规则">
                  需要有对应股票的持仓才能卖出
                </el-descriptions-item>
                <el-descriptions-item label="数量要求">
                  A股交易数量必须为100的整数倍
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.trading-system {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.el-header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.el-header h1 {
  margin: 0;
  font-size: 24px;
}

.el-main {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.account-card,
.order-card {
  height: 100%;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>
