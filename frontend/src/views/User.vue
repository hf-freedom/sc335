<template>
  <div class="user-page">
    <el-card>
      <div slot="header" class="card-header">
        <span>用户端 - 提交家政需求</span>
      </div>

      <el-form :model="orderForm" label-width="100px" ref="orderForm">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择用户" prop="userId">
              <el-select v-model="orderForm.userId" placeholder="请选择用户" style="width: 100%;">
                <el-option
                  v-for="user in users"
                  :key="user.id"
                  :label="user.name"
                  :value="user.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务类型" prop="serviceType">
              <el-select v-model="orderForm.serviceType" placeholder="请选择服务类型" style="width: 100%;">
                <el-option label="日常保洁" value="CLEANING"></el-option>
                <el-option label="深度保洁" value="DEEP_CLEANING"></el-option>
                <el-option label="做饭" value="COOKING"></el-option>
                <el-option label="月嫂育儿" value="NURSING"></el-option>
                <el-option label="老人陪护" value="ELDER_CARE"></el-option>
                <el-option label="家电维修" value="REPAIR"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务地址" prop="address">
              <el-input v-model="orderForm.address" placeholder="请输入服务地址"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务时长" prop="durationHours">
              <el-input-number
                v-model="orderForm.durationHours"
                :min="1"
                :max="24"
                style="width: 100%;"
              ></el-input-number>
              <span style="margin-left: 10px;">小时</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="orderForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%;"
                value-format="yyyy-MM-dd HH:mm:ss"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input v-model="orderForm.latitude" placeholder="默认使用用户地址纬度"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input v-model="orderForm.longitude" placeholder="默认使用用户地址经度"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="orderForm.remark" placeholder="备注信息"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="submitOrder">提交订单</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px;">
      <div slot="header" class="card-header">
        <span>我的订单</span>
        <el-select v-model="selectedUserId" placeholder="选择用户查看订单" style="width: 200px;" @change="loadUserOrders">
          <el-option
            v-for="user in users"
            :key="user.id"
            :label="user.name"
            :value="user.id"
          ></el-option>
        </el-select>
      </div>

      <el-table :data="userOrders" border>
        <el-table-column prop="id" label="订单号" width="180"></el-table-column>
        <el-table-column label="服务类型" width="120">
          <template slot-scope="scope">
            {{ scope.row.serviceType.description }}
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" width="200"></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160"></el-table-column>
        <el-table-column prop="durationHours" label="时长" width="80">
          <template slot-scope="scope">{{ scope.row.durationHours }}小时</template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="金额" width="100">
          <template slot-scope="scope">¥{{ scope.row.totalPrice }}</template>
        </el-table-column>
        <el-table-column prop="auntName" label="接单阿姨" width="100"></el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status.description }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="danger"
              @click="cancelOrder(scope.row)"
              :disabled="scope.row.status === 'COMPLETED' || scope.row.status === 'CANCELLED' || scope.row.status === 'EXPIRED'"
            >取消订单</el-button>
            <el-button
              size="mini"
              type="primary"
              @click="showRateDialog(scope.row)"
              :disabled="scope.row.status !== 'COMPLETED' || scope.row.rating"
            >评价</el-button>
            <el-button
              size="mini"
              type="warning"
              @click="showAddHoursDialog(scope.row)"
              :disabled="scope.row.status !== 'IN_PROGRESS' && scope.row.status !== 'ACCEPTED'"
            >加时</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="取消订单" :visible.sync="cancelDialogVisible" width="400px">
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="取消原因">
          <el-input type="textarea" v-model="cancelForm.reason" rows="3"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCancel">确认取消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="评价订单" :visible.sync="rateDialogVisible" width="400px">
      <el-form :model="rateForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="rateForm.rating" :max="5"></el-rate>
        </el-form-item>
        <el-form-item label="评价">
          <el-input type="textarea" v-model="rateForm.comment" rows="3"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRate">提交评价</el-button>
      </div>
    </el-dialog>

    <el-dialog title="临时加时" :visible.sync="addHoursDialogVisible" width="400px">
      <el-form :model="addHoursForm" label-width="80px">
        <el-form-item label="加时时长">
          <el-input-number v-model="addHoursForm.additionalHours" :min="1" :max="12"></el-input-number>
          <span style="margin-left: 10px;">小时</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addHoursDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddHours">确认加时</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'User',
  data() {
    return {
      users: [],
      selectedUserId: '',
      userOrders: [],
      orderForm: {
        userId: '',
        serviceType: '',
        address: '',
        startTime: '',
        durationHours: 2,
        latitude: '',
        longitude: '',
        remark: ''
      },
      cancelDialogVisible: false,
      cancelForm: {
        orderId: '',
        userId: '',
        reason: ''
      },
      rateDialogVisible: false,
      rateForm: {
        orderId: '',
        userId: '',
        rating: 5,
        comment: ''
      },
      addHoursDialogVisible: false,
      addHoursForm: {
        orderId: '',
        additionalHours: 1
      }
    }
  },
  mounted() {
    this.loadUsers()
  },
  methods: {
    async loadUsers() {
      const res = await this.$axios.get('/users')
      if (res.code === 200) {
        this.users = res.data
        if (this.users.length > 0) {
          this.orderForm.userId = this.users[0].id
        }
      }
    },
    async loadUserOrders() {
      if (!this.selectedUserId) return
      const res = await this.$axios.get(`/orders/user/${this.selectedUserId}`)
      if (res.code === 200) {
        this.userOrders = res.data
      }
    },
    async submitOrder() {
      if (!this.orderForm.userId || !this.orderForm.serviceType || !this.orderForm.startTime) {
        this.$message.error('请填写完整订单信息')
        return
      }
      const res = await this.$axios.post('/orders', this.orderForm)
      if (res.code === 200) {
        this.$message.success('订单提交成功')
        this.selectedUserId = this.orderForm.userId
        this.loadUserOrders()
        this.resetForm()
      } else {
        this.$message.error(res.message)
      }
    },
    resetForm() {
      this.orderForm = {
        userId: this.users.length > 0 ? this.users[0].id : '',
        serviceType: '',
        address: '',
        startTime: '',
        durationHours: 2,
        latitude: '',
        longitude: '',
        remark: ''
      }
      this.$refs.orderForm && this.$refs.orderForm.resetFields()
    },
    cancelOrder(order) {
      this.cancelForm.orderId = order.id
      this.cancelForm.userId = order.userId
      this.cancelForm.reason = ''
      this.cancelDialogVisible = true
    },
    async confirmCancel() {
      const res = await this.$axios.post('/orders/cancel', this.cancelForm)
      if (res.code === 200) {
        this.$message.success('订单已取消' + (res.data.cancelPenalty ? '，违约金：¥' + res.data.cancelPenalty : ''))
        this.cancelDialogVisible = false
        this.loadUserOrders()
      } else {
        this.$message.error(res.message)
      }
    },
    showRateDialog(order) {
      this.rateForm.orderId = order.id
      this.rateForm.userId = order.userId
      this.rateForm.rating = 5
      this.rateForm.comment = ''
      this.rateDialogVisible = true
    },
    async confirmRate() {
      const res = await this.$axios.post('/orders/rate', this.rateForm)
      if (res.code === 200) {
        this.$message.success('评价提交成功')
        this.rateDialogVisible = false
        this.loadUserOrders()
      } else {
        this.$message.error(res.message)
      }
    },
    showAddHoursDialog(order) {
      this.addHoursForm.orderId = order.id
      this.addHoursForm.additionalHours = 1
      this.addHoursDialogVisible = true
    },
    async confirmAddHours() {
      const res = await this.$axios.post('/orders/add-hours', this.addHoursForm)
      if (res.code === 200) {
        this.$message.success('加时成功，新的总金额：¥' + res.data.totalPrice)
        this.addHoursDialogVisible = false
        this.loadUserOrders()
      } else {
        this.$message.error(res.message)
      }
    },
    getStatusType(status) {
      const map = {
        PENDING: 'warning',
        ACCEPTED: 'primary',
        IN_PROGRESS: 'success',
        COMPLETED: 'info',
        CANCELLED: 'danger',
        EXPIRED: 'info'
      }
      return map[status] || 'info'
    }
  }
}
</script>

<style scoped>
.card-header {
  font-weight: bold;
  font-size: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
