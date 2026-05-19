<template>
  <div class="aunt-page">
    <el-card>
      <div slot="header" class="card-header">
        <span>阿姨端 - 订单管理</span>
        <el-select v-model="selectedAuntId" placeholder="选择阿姨" style="width: 200px;" @change="loadAuntData">
          <el-option
            v-for="aunt in aunts"
            :key="aunt.id"
            :label="aunt.name"
            :value="aunt.id"
          ></el-option>
        </el-select>
      </div>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="可接订单" name="pending">
          <el-table :data="pendingOrdersWithCheck" border>
            <el-table-column prop="id" label="订单号" width="160"></el-table-column>
            <el-table-column prop="userName" label="用户" width="80"></el-table-column>
            <el-table-column label="服务类型" width="110">
              <template slot-scope="scope">
                {{ scope.row.serviceType.description }}
              </template>
            </el-table-column>
            <el-table-column prop="address" label="地址" width="150" show-overflow-tooltip></el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="150"></el-table-column>
            <el-table-column prop="durationHours" label="时长" width="70">
              <template slot-scope="scope">{{ scope.row.durationHours }}h</template>
            </el-table-column>
            <el-table-column prop="totalPrice" label="金额" width="90">
              <template slot-scope="scope">¥{{ scope.row.totalPrice }}</template>
            </el-table-column>
            <el-table-column label="技能匹配" width="90" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.checkResult.skillMatch ? 'success' : 'danger'" size="mini">
                  {{ scope.row.checkResult.skillMatch ? '✓ 匹配' : '✗ 不匹配' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="距离" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.checkResult.distance <= 20 ? 'success' : 'warning'" size="mini">
                  {{ scope.row.checkResult.distance.toFixed(1) }}km
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="排班" width="130" align="center">
              <template slot-scope="scope">
                <div v-if="scope.row.checkResult.scheduleConflict" style="color: #F56C6C; font-size: 12px;">
                  ⚠ 时段冲突
                </div>
                <div v-else-if="scope.row.checkResult.dailyLimitReached" style="color: #E6A23C; font-size: 12px;">
                  ⚠ 达今日上限
                </div>
                <div v-else style="color: #67C23A; font-size: 12px;">
                  ✓ 可接单
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="success"
                  @click="acceptOrder(scope.row)"
                  :disabled="!scope.row.checkResult.canAccept"
                >接单</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="我的订单" name="myOrders">
          <el-table :data="myOrders" border>
            <el-table-column prop="id" label="订单号" width="180"></el-table-column>
            <el-table-column prop="userName" label="用户" width="100"></el-table-column>
            <el-table-column label="服务类型" width="120">
              <template slot-scope="scope">
                {{ scope.row.serviceType.description }}
              </template>
            </el-table-column>
            <el-table-column prop="address" label="地址" width="200"></el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="160"></el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="160"></el-table-column>
            <el-table-column prop="totalPrice" label="金额" width="100">
              <template slot-scope="scope">¥{{ scope.row.totalPrice }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status.description }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="评价" width="120">
              <template slot-scope="scope">
                <span v-if="scope.row.rating" style="color: #E6A23C;">
                  ★ {{ scope.row.rating }}
                </span>
                <span v-else>未评价</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="primary"
                  @click="startService(scope.row)"
                  :disabled="scope.row.status !== 'ACCEPTED'"
                >开始服务</el-button>
                <el-button
                  size="mini"
                  type="success"
                  @click="completeService(scope.row)"
                  :disabled="scope.row.status !== 'IN_PROGRESS'"
                >完成服务</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-card style="margin-top: 20px;" v-if="selectedAunt">
      <div slot="header" class="card-header">
        <span>个人信息</span>
      </div>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="姓名">{{ selectedAunt.name }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ selectedAunt.phone }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ selectedAunt.address }}</el-descriptions-item>
        <el-descriptions-item label="综合评分">
          <span style="color: #E6A23C; font-weight: bold;">★ {{ selectedAunt.rating }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="完成订单">{{ selectedAunt.totalOrders }}单</el-descriptions-item>
        <el-descriptions-item label="每日上限">{{ selectedAunt.maxDailyOrders }}单</el-descriptions-item>
        <el-descriptions-item label="技能标签" :span="3">
          <el-tag
            v-for="(tag, index) in selectedAunt.skillTags"
            :key="index"
            style="margin-right: 10px;"
          >
            {{ tag }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Aunt',
  data() {
    return {
      aunts: [],
      selectedAuntId: '',
      selectedAunt: null,
      activeTab: 'pending',
      pendingOrders: [],
      myOrders: []
    }
  },
  computed: {
    pendingOrdersWithCheck() {
      if (!this.selectedAunt || !this.pendingOrders.length) return []
      
      return this.pendingOrders.map(order => {
        const checkResult = this.checkOrder(order)
        return {
          ...order,
          checkResult
        }
      })
    }
  },
  mounted() {
    this.loadAunts()
  },
  methods: {
    calculateDistance(lat1, lon1, lat2, lon2) {
      const earthRadius = 6371
      const dLat = this.toRad(lat2 - lat1)
      const dLon = this.toRad(lon2 - lon1)
      const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(this.toRad(lat1)) * Math.cos(this.toRad(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
      return earthRadius * c
    },
    toRad(deg) {
      return deg * (Math.PI / 180)
    },
    isTimeOverlap(start1, end1, start2, end2) {
      const s1 = new Date(start1).getTime()
      const e1 = new Date(end1).getTime()
      const s2 = new Date(start2).getTime()
      const e2 = new Date(end2).getTime()
      return s1 < e2 && s2 < e1
    },
    checkOrder(order) {
      const aunt = this.selectedAunt
      if (!aunt) {
        return { skillMatch: false, distance: 999, scheduleConflict: false, dailyLimitReached: false, canAccept: false }
      }

      const skillMatch = aunt.skillTags && aunt.skillTags.includes(order.serviceType.description)
      
      const distance = this.calculateDistance(
        parseFloat(aunt.latitude), parseFloat(aunt.longitude),
        parseFloat(order.latitude), parseFloat(order.longitude)
      )

      const orderDate = new Date(order.startTime).toDateString()
      const todayOrders = this.myOrders.filter(o => {
        const oDate = new Date(o.startTime).toDateString()
        return oDate === orderDate && 
               (o.status === 'ACCEPTED' || o.status === 'IN_PROGRESS')
      })
      const dailyLimitReached = todayOrders.length >= aunt.maxDailyOrders

      const orderEndTime = new Date(order.startTime).getTime() + order.durationHours * 3600000
      const scheduleConflict = this.myOrders.some(o => {
        if (o.status !== 'ACCEPTED' && o.status !== 'IN_PROGRESS') return false
        const oEndTime = new Date(o.startTime).getTime() + o.durationHours * 3600000
        return this.isTimeOverlap(order.startTime, orderEndTime, o.startTime, oEndTime)
      })

      const canAccept = skillMatch && distance <= 20 && !scheduleConflict && !dailyLimitReached

      return {
        skillMatch,
        distance,
        scheduleConflict,
        dailyLimitReached,
        canAccept
      }
    },
    async loadAunts() {
      const res = await this.$axios.get('/aunts')
      if (res.code === 200) {
        this.aunts = res.data
        if (this.aunts.length > 0) {
          this.selectedAuntId = this.aunts[0].id
          this.loadAuntData()
        }
      }
    },
    async loadAuntData() {
      if (!this.selectedAuntId) return
      this.selectedAunt = this.aunts.find(a => a.id === this.selectedAuntId)
      this.loadPendingOrders()
      this.loadMyOrders()
    },
    handleTabClick(tab) {
      if (tab.name === 'pending') {
        this.loadPendingOrders()
      } else {
        this.loadMyOrders()
      }
    },
    async loadPendingOrders() {
      if (!this.selectedAuntId) return
      const res = await this.$axios.get('/orders/pending')
      if (res.code === 200) {
        this.pendingOrders = res.data
      }
    },
    async loadMyOrders() {
      if (!this.selectedAuntId) return
      const res = await this.$axios.get(`/orders/aunt/${this.selectedAuntId}`)
      if (res.code === 200) {
        this.myOrders = res.data
      }
    },
    async acceptOrder(order) {
      const res = await this.$axios.post('/orders/accept', {
        orderId: order.id,
        auntId: this.selectedAuntId
      })
      if (res.code === 200) {
        this.$message.success('接单成功')
        this.loadPendingOrders()
        this.loadMyOrders()
      } else {
        this.$message.error(res.message)
      }
    },
    async startService(order) {
      const res = await this.$axios.post(`/orders/${order.id}/start`)
      if (res.code === 200) {
        this.$message.success('服务已开始')
        this.loadMyOrders()
      } else {
        this.$message.error(res.message)
      }
    },
    async completeService(order) {
      const res = await this.$axios.post(`/orders/${order.id}/complete`)
      if (res.code === 200) {
        this.$message.success('服务已完成')
        this.loadMyOrders()
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
