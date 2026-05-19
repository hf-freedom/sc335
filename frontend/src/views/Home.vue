<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <div slot="header" class="card-header">
            <span>系统概览</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-card" style="background: #409EFF;">
                <div class="stat-number">{{ stats.totalOrders }}</div>
                <div class="stat-label">总订单数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card" style="background: #67C23A;">
                <div class="stat-number">{{ stats.pendingOrders }}</div>
                <div class="stat-label">待接单</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card" style="background: #E6A23C;">
                <div class="stat-number">{{ stats.inProgressOrders }}</div>
                <div class="stat-label">服务中</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card" style="background: #909399;">
                <div class="stat-number">{{ stats.completedOrders }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <div slot="header" class="card-header">
            <span>注册用户</span>
          </div>
          <el-table :data="users" border>
            <el-table-column prop="name" label="姓名" width="100"></el-table-column>
            <el-table-column prop="phone" label="电话" width="130"></el-table-column>
            <el-table-column prop="address" label="地址"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header" class="card-header">
            <span>注册阿姨</span>
          </div>
          <el-table :data="aunts" border>
            <el-table-column prop="name" label="姓名" width="100"></el-table-column>
            <el-table-column prop="phone" label="电话" width="130"></el-table-column>
            <el-table-column label="技能" width="200">
              <template slot-scope="scope">
                <el-tag
                  v-for="(tag, index) in scope.row.skillTags"
                  :key="index"
                  size="mini"
                  style="margin-right: 5px;"
                >
                  {{ tag }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rating" label="评分" width="80">
              <template slot-scope="scope">
                <span style="color: #E6A23C;">★ {{ scope.row.rating }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <div slot="header" class="card-header">
            <span>最近订单</span>
          </div>
          <el-table :data="recentOrders" border>
            <el-table-column prop="id" label="订单号" width="180"></el-table-column>
            <el-table-column prop="userName" label="用户" width="100"></el-table-column>
            <el-table-column prop="auntName" label="阿姨" width="100"></el-table-column>
            <el-table-column label="服务类型" width="120">
              <template slot-scope="scope">
                {{ scope.row.serviceType.description }}
              </template>
            </el-table-column>
            <el-table-column prop="address" label="地址" width="200"></el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="160"></el-table-column>
            <el-table-column prop="durationHours" label="时长" width="80">
              <template slot-scope="scope">
                {{ scope.row.durationHours }}小时
              </template>
            </el-table-column>
            <el-table-column prop="totalPrice" label="金额" width="100">
              <template slot-scope="scope">
                ¥{{ scope.row.totalPrice }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status.description }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      users: [],
      aunts: [],
      recentOrders: [],
      stats: {
        totalOrders: 0,
        pendingOrders: 0,
        inProgressOrders: 0,
        completedOrders: 0
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      await Promise.all([
        this.loadUsers(),
        this.loadAunts(),
        this.loadOrders()
      ])
    },
    async loadUsers() {
      const res = await this.$axios.get('/users')
      if (res.code === 200) {
        this.users = res.data
      }
    },
    async loadAunts() {
      const res = await this.$axios.get('/aunts')
      if (res.code === 200) {
        this.aunts = res.data
      }
    },
    async loadOrders() {
      const res = await this.$axios.get('/orders')
      if (res.code === 200) {
        this.recentOrders = res.data.slice(0, 10)
        this.stats.totalOrders = res.data.length
        this.stats.pendingOrders = res.data.filter(o => o.status === 'PENDING').length
        this.stats.inProgressOrders = res.data.filter(o => o.status === 'IN_PROGRESS' || o.status === 'ACCEPTED').length
        this.stats.completedOrders = res.data.filter(o => o.status === 'COMPLETED').length
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
}
.stat-card {
  padding: 20px;
  border-radius: 8px;
  color: white;
  text-align: center;
}
.stat-number {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
}
.stat-label {
  font-size: 14px;
  opacity: 0.9;
}
</style>
