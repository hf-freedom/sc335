<template>
  <div class="orders-page">
    <el-card>
      <div slot="header" class="card-header">
        <span>订单管理</span>
        <el-button type="primary" @click="refreshData">刷新</el-button>
      </div>

      <el-table :data="orders" border>
        <el-table-column prop="id" label="订单号" width="180"></el-table-column>
        <el-table-column prop="userName" label="用户" width="100"></el-table-column>
        <el-table-column prop="auntName" label="阿姨" width="100"></el-table-column>
        <el-table-column label="服务类型" width="120">
          <template slot-scope="scope">
            {{ scope.row.serviceType.description }}
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160"></el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="160"></el-table-column>
        <el-table-column prop="durationHours" label="时长" width="80">
          <template slot-scope="scope">{{ scope.row.durationHours }}h</template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="金额" width="100">
          <template slot-scope="scope">¥{{ scope.row.totalPrice }}</template>
        </el-table-column>
        <el-table-column label="价值等级" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getOrderValueLevel(scope.row.totalPrice).type" size="mini">
              {{ getOrderValueLevel(scope.row.totalPrice).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="阿姨评分" width="90" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.auntId && getAuntRating(scope.row.auntId)" style="color: #E6A23C;">
              ★ {{ getAuntRating(scope.row.auntId) }}
            </span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="当日接单" width="100" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.auntId">
              <el-tag :type="getAuntDailyOrdersType(scope.row.auntId, scope.row.startTime)" size="mini">
                {{ getAuntDailyOrdersCount(scope.row.auntId, scope.row.startTime) }}/{{ getAuntMaxDaily(scope.row.auntId) }}
              </el-tag>
            </span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="分配说明" width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            <span v-if="scope.row.auntId" :style="{ color: getAssignColor(scope.row) }">
              {{ getAssignDesc(scope.row) }}
            </span>
            <span v-else style="color: #909399;">待分配</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status.description }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cancelPenalty" label="违约金" width="110">
          <template slot-scope="scope">
            <span v-if="scope.row.cancelPenalty" style="color: #F56C6C; font-weight: bold;">
              ¥{{ scope.row.cancelPenalty }}
            </span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="评价" width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.rating" style="color: #E6A23C;">★ {{ scope.row.rating }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="warning"
              icon="el-icon-clock"
              @click="showAddHoursDialog(scope.row)"
              :disabled="scope.row.status !== 'ACCEPTED' && scope.row.status !== 'IN_PROGRESS'"
            >加时</el-button>
            <el-button
              size="mini"
              type="danger"
              icon="el-icon-close"
              @click="showCancelDialog(scope.row)"
              :disabled="scope.row.status === 'COMPLETED' || scope.row.status === 'CANCELLED' || scope.row.status === 'EXPIRED'"
            >取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-top: 20px;">
      <div slot="header" class="card-header">
        <span>系统功能说明</span>
      </div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="1. 用户下单">
          用户提交家政需求后，系统按服务类型、地址、时段自动生成订单
        </el-descriptions-item>
        <el-descriptions-item label="2. 阿姨接单校验">
          阿姨接单前系统自动校验：技能标签是否匹配、距离是否在20公里内、当前排班是否冲突
        </el-descriptions-item>
        <el-descriptions-item label="3. 时段冲突检测">
          同一阿姨同一时段不能接多个订单，系统自动检测时间重叠
        </el-descriptions-item>
        <el-descriptions-item label="4. 取消订单违约规则">
          距离服务开始时间小于2小时扣50%，小于24小时扣30%，24小时以上扣10%
        </el-descriptions-item>
        <el-descriptions-item label="5. 评价影响权重">
          服务完成后评价会影响阿姨后续接单权重，高评分阿姨优先获得高价值订单
        </el-descriptions-item>
        <el-descriptions-item label="6. 临时加时">
          临时加时需要重新计算费用，并检测阿姨后续排班是否有冲突
        </el-descriptions-item>
        <el-descriptions-item label="7. 定时任务">
          系统定时扫描即将服务（1小时内）、超时未接（30分钟）、差评订单
        </el-descriptions-item>
        <el-descriptions-item label="8. 智能派单">
          高评分阿姨优先获得高价值订单，但设置每日接单上限防止过载
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <div slot="header" class="card-header">
            <span>高价值订单分配记录</span>
            <el-tag type="danger" size="mini">TOP {{ highValueOrders.length }}单</el-tag>
          </div>
          <el-table :data="highValueOrders" border size="small" max-height="400">
            <el-table-column prop="id" label="订单号" width="150" show-overflow-tooltip></el-table-column>
            <el-table-column prop="totalPrice" label="金额" width="80">
              <template slot-scope="scope">
                <span style="color: #F56C6C; font-weight: bold;">¥{{ scope.row.totalPrice }}</span>
              </template>
            </el-table-column>
            <el-table-column label="价值等级" width="80" align="center">
              <template slot-scope="scope">
                <el-tag :type="getOrderValueLevel(scope.row.totalPrice).type" size="mini">
                  {{ getOrderValueLevel(scope.row.totalPrice).label }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auntName" label="分配阿姨" width="80"></el-table-column>
            <el-table-column label="阿姨评分" width="80" align="center">
              <template slot-scope="scope">
                <span v-if="scope.row.auntId" style="color: #E6A23C;">
                  ★ {{ getAuntRating(scope.row.auntId) }}
                </span>
                <span v-else style="color: #909399;">待接单</span>
              </template>
            </el-table-column>
            <el-table-column label="分配状态" width="90" align="center">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.auntId" type="success" size="mini">已分配</el-tag>
                <el-tag v-else type="warning" size="mini">待分配</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="分配说明" width="150" show-overflow-tooltip>
              <template slot-scope="scope">
                <span v-if="scope.row.auntId" :style="{ color: getAssignColor(scope.row) }">
                  {{ getAssignDesc(scope.row) }}
                </span>
                <span v-else style="color: #909399;">等待高评分阿姨接单</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <div slot="header" class="card-header">
            <span>阿姨接单上限执行记录</span>
            <el-tag type="primary" size="mini">今日</el-tag>
          </div>
          <el-table :data="auntDailyStats" border size="small" max-height="400">
            <el-table-column prop="name" label="阿姨" width="80"></el-table-column>
            <el-table-column label="评分" width="80" align="center">
              <template slot-scope="scope">
                <span style="color: #E6A23C;">★ {{ scope.row.rating }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="todayOrders" label="今日接单" width="90" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.todayOrders >= scope.row.maxDailyOrders ? 'danger' : scope.row.todayOrders > 0 ? 'success' : 'info'" size="mini">
                  {{ scope.row.todayOrders }}单
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="maxDailyOrders" label="上限" width="70" align="center">
              <template slot-scope="scope">{{ scope.row.maxDailyOrders }}单</template>
            </el-table-column>
            <el-table-column label="剩余额度" width="90" align="center">
              <template slot-scope="scope">
                <span :style="{ color: scope.row.remaining > 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
                  {{ scope.row.remaining }}单
                </span>
              </template>
            </el-table-column>
            <el-table-column label="使用率" width="140">
              <template slot-scope="scope">
                <el-progress
                  :percentage="scope.row.usageRate"
                  :status="scope.row.usageRate >= 100 ? 'exception' : scope.row.usageRate >= 70 ? 'warning' : 'success'"
                  :stroke-width="12"
                ></el-progress>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.remaining <= 0" type="danger" size="mini">已满</el-tag>
                <el-tag v-else-if="scope.row.remaining <= 1" type="warning" size="mini">紧张</el-tag>
                <el-tag v-else type="success" size="mini">充足</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      title="取消订单"
      :visible.sync="cancelDialogVisible"
      width="500px"
      @close="resetCancelForm"
    >
      <div v-if="currentOrder" style="margin-bottom: 20px;">
        <el-alert
          :title="cancelAlertTitle"
          :type="cancelAlertType"
          :description="cancelAlertDesc"
          show-icon
        >
        </el-alert>
      </div>

      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="订单号">
          <span>{{ currentOrder ? currentOrder.id : '' }}</span>
        </el-form-item>
        <el-form-item label="服务类型">
          <span>{{ currentOrder ? currentOrder.serviceType.description : '' }}</span>
        </el-form-item>
        <el-form-item label="订单金额">
          <span style="color: #409EFF; font-weight: bold;">
            ¥{{ currentOrder ? currentOrder.totalPrice : '' }}
          </span>
        </el-form-item>
        <el-form-item label="开始时间">
          <span>{{ currentOrder ? currentOrder.startTime : '' }}</span>
        </el-form-item>
        <el-form-item label="距开始">
          <span :style="{ color: hoursToStart < 2 ? '#F56C6C' : hoursToStart < 24 ? '#E6A23C' : '#67C23A', fontWeight: 'bold' }">
            {{ hoursToStartText }}
          </span>
        </el-form-item>
        <el-form-item label="违约比例">
          <el-tag :type="penaltyType" size="medium">{{ penaltyPercent }}%</el-tag>
        </el-form-item>
        <el-form-item label="预计违约金">
          <span style="color: #F56C6C; font-weight: bold; font-size: 18px;">
            ¥{{ estimatedPenalty }}
          </span>
        </el-form-item>
        <el-form-item label="取消原因">
          <el-input
            type="textarea"
            v-model="cancelForm.reason"
            :rows="3"
            placeholder="请输入取消原因"
          ></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelDialogVisible = false">返回</el-button>
        <el-button type="danger" @click="confirmCancelOrder" :loading="canceling">
          确认取消
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="取消结果"
      :visible.sync="resultDialogVisible"
      width="400px"
    >
      <div style="text-align: center; padding: 20px 0;">
        <i class="el-icon-circle-check" style="font-size: 60px; color: #67C23A;"></i>
        <h3 style="margin: 20px 0 10px;">订单取消成功</h3>
        <p style="color: #606266;">订单号：{{ canceledOrderId }}</p>
        <p v-if="canceledPenalty && canceledPenalty > 0" style="color: #F56C6C; margin-top: 10px; font-size: 16px;">
          扣除违约金：<strong>¥{{ canceledPenalty }}</strong>
        </p>
        <p v-else style="color: #67C23A; margin-top: 10px;">
          无需支付违约金
        </p>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="resultDialogVisible = false">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="临时加时"
      :visible.sync="addHoursDialogVisible"
      width="550px"
      @close="resetAddHoursForm"
    >
      <div v-if="addHoursOrder" style="margin-bottom: 20px;">
        <el-alert
          :title="addHoursAlertTitle"
          :type="addHoursAlertType"
          :description="addHoursAlertDesc"
          show-icon
        >
        </el-alert>
      </div>

      <el-form :model="addHoursForm" label-width="120px">
        <el-form-item label="订单号">
          <span>{{ addHoursOrder ? addHoursOrder.id : '' }}</span>
        </el-form-item>
        <el-form-item label="服务类型">
          <span>{{ addHoursOrder ? addHoursOrder.serviceType.description : '' }}</span>
        </el-form-item>
        <el-form-item label="当前结束时间">
          <span>{{ addHoursOrder ? addHoursOrder.endTime : '' }}</span>
        </el-form-item>
        <el-form-item label="当前时长">
          <span>{{ addHoursOrder ? addHoursOrder.durationHours : 0 }}小时</span>
        </el-form-item>
        <el-form-item label="当前金额">
          <span style="color: #409EFF; font-weight: bold;">
            ¥{{ addHoursOrder ? addHoursOrder.totalPrice : '' }}
          </span>
        </el-form-item>
        <el-form-item label="加时时长">
          <el-input-number
            v-model="addHoursForm.additionalHours"
            :min="1"
            :max="12"
            @change="calculateAddHours"
          ></el-input-number>
          <span style="margin-left: 10px;">小时</span>
        </el-form-item>
        <el-form-item label="小时单价">
          <span style="color: #67C23A;">¥{{ addHoursOrder ? addHoursOrder.serviceType.pricePerHour : '' }}/小时</span>
        </el-form-item>
        <el-form-item label="加时费用">
          <span style="color: #E6A23C; font-weight: bold; font-size: 16px;">
            + ¥{{ additionalPrice }}
          </span>
        </el-form-item>
        <el-form-item label="新的总金额">
          <span style="color: #F56C6C; font-weight: bold; font-size: 20px;">
            ¥{{ newTotalPrice }}
          </span>
        </el-form-item>
        <el-form-item label="新的结束时间">
          <span style="color: #409EFF; font-weight: bold;">
            {{ newEndTime }}
          </span>
        </el-form-item>
        <el-form-item label="排班冲突检测">
          <div v-if="!auntHasOrders" style="color: #909399;">
            该订单未分配阿姨，无需检测排班
          </div>
          <div v-else-if="scheduleCheckLoading" style="color: #E6A23C;">
            <i class="el-icon-loading"></i> 正在检测阿姨后续排班...
          </div>
          <div v-else-if="scheduleConflict" style="color: #F56C6C;">
            <i class="el-icon-circle-close"></i> 检测到冲突：阿姨在新结束时间前已有订单安排
          </div>
          <div v-else style="color: #67C23A;">
            <i class="el-icon-circle-check"></i> 检测通过：阿姨后续时段无排班冲突
          </div>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="addHoursDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmAddHours"
          :loading="addingHours"
          :disabled="scheduleConflict"
        >确认加时</el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="加时结果"
      :visible.sync="addHoursResultVisible"
      width="450px"
    >
      <div style="text-align: center; padding: 20px 0;">
        <i class="el-icon-circle-check" style="font-size: 60px; color: #67C23A;"></i>
        <h3 style="margin: 20px 0 10px;">加时成功</h3>
        <p style="color: #606266;">订单号：{{ addHoursResult.orderId }}</p>
        <div style="margin-top: 20px; padding: 15px; background: #f5f7fa; border-radius: 4px;">
          <p style="margin: 8px 0;">
            原时长：{{ addHoursResult.oldDuration }}小时 → 
            <strong style="color: #409EFF;">新时长：{{ addHoursResult.newDuration }}小时</strong>
          </p>
          <p style="margin: 8px 0;">
            原结束时间：{{ addHoursResult.oldEndTime }}
          </p>
          <p style="margin: 8px 0;">
            <strong style="color: #409EFF;">新结束时间：{{ addHoursResult.newEndTime }}</strong>
          </p>
          <p style="margin: 8px 0;">
            加时费用：<span style="color: #E6A23C; font-weight: bold;">+ ¥{{ addHoursResult.additionalPrice }}</span>
          </p>
          <p style="margin: 8px 0;">
            新的总金额：<span style="color: #F56C6C; font-weight: bold; font-size: 18px;">¥{{ addHoursResult.newTotalPrice }}</span>
          </p>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="addHoursResultVisible = false">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Orders',
  data() {
    return {
      orders: [],
      aunts: [],
      cancelDialogVisible: false,
      resultDialogVisible: false,
      currentOrder: null,
      canceling: false,
      canceledOrderId: '',
      canceledPenalty: 0,
      cancelForm: {
        orderId: '',
        userId: '',
        reason: ''
      },
      addHoursDialogVisible: false,
      addHoursResultVisible: false,
      addHoursOrder: null,
      addingHours: false,
      scheduleCheckLoading: false,
      scheduleConflict: false,
      auntOrders: [],
      addHoursForm: {
        orderId: '',
        additionalHours: 1
      },
      addHoursResult: {
        orderId: '',
        oldDuration: 0,
        newDuration: 0,
        oldEndTime: '',
        newEndTime: '',
        additionalPrice: 0,
        newTotalPrice: 0
      }
    }
  },
  computed: {
    hoursToStart() {
      if (!this.currentOrder) return 0
      const now = new Date().getTime()
      const startTime = new Date(this.currentOrder.startTime).getTime()
      const diffMs = startTime - now
      return Math.max(0, diffMs / (1000 * 60 * 60))
    },
    hoursToStartText() {
      if (!this.currentOrder) return ''
      const hours = this.hoursToStart
      if (hours < 1) {
        const mins = Math.round(hours * 60)
        return `${mins}分钟`
      }
      if (hours < 24) {
        return `${hours.toFixed(1)}小时`
      }
      const days = hours / 24
      return `${days.toFixed(1)}天`
    },
    penaltyPercent() {
      if (!this.currentOrder) return 0
      const status = this.currentOrder.status
      if (status === 'PENDING') return 0
      
      const hours = this.hoursToStart
      if (hours < 2) return 50
      if (hours < 24) return 30
      return 10
    },
    penaltyType() {
      if (this.penaltyPercent >= 50) return 'danger'
      if (this.penaltyPercent >= 30) return 'warning'
      if (this.penaltyPercent >= 10) return 'info'
      return 'success'
    },
    estimatedPenalty() {
      if (!this.currentOrder) return 0
      const totalPrice = parseFloat(this.currentOrder.totalPrice)
      return (totalPrice * this.penaltyPercent / 100).toFixed(2)
    },
    cancelAlertTitle() {
      if (!this.currentOrder) return ''
      if (this.currentOrder.status === 'PENDING') {
        return '待接单订单取消'
      }
      if (this.currentOrder.status === 'IN_PROGRESS') {
        return '服务中订单取消'
      }
      return '已接单订单取消'
    },
    cancelAlertType() {
      if (!this.currentOrder) return 'info'
      if (this.currentOrder.status === 'PENDING') return 'success'
      if (this.penaltyPercent >= 50) return 'error'
      if (this.penaltyPercent >= 30) return 'warning'
      return 'info'
    },
    cancelAlertDesc() {
      if (!this.currentOrder) return ''
      if (this.currentOrder.status === 'PENDING') {
        return '待接单状态取消订单无需支付违约金'
      }
      if (this.hoursToStart < 2) {
        return '距离服务开始不足2小时，将扣除50%订单金额作为违约金'
      }
      if (this.hoursToStart < 24) {
        return '距离服务开始不足24小时，将扣除30%订单金额作为违约金'
      }
      return '距离服务开始超过24小时，将扣除10%订单金额作为违约金'
    },
    additionalPrice() {
      if (!this.addHoursOrder) return 0
      const pricePerHour = parseFloat(this.addHoursOrder.serviceType.pricePerHour)
      return (pricePerHour * this.addHoursForm.additionalHours).toFixed(2)
    },
    newTotalPrice() {
      if (!this.addHoursOrder) return 0
      const currentPrice = parseFloat(this.addHoursOrder.totalPrice)
      const addPrice = parseFloat(this.additionalPrice)
      return (currentPrice + addPrice).toFixed(2)
    },
    newEndTime() {
      if (!this.addHoursOrder) return ''
      const endTime = new Date(this.addHoursOrder.endTime)
      endTime.setHours(endTime.getHours() + this.addHoursForm.additionalHours)
      return this.formatDateTime(endTime)
    },
    auntHasOrders() {
      return this.addHoursOrder && this.addHoursOrder.auntId
    },
    addHoursAlertTitle() {
      if (!this.addHoursOrder) return ''
      if (this.addHoursOrder.status === 'IN_PROGRESS') {
        return '服务中订单加时'
      }
      return '已接单订单加时'
    },
    addHoursAlertType() {
      return 'warning'
    },
    addHoursAlertDesc() {
      if (!this.addHoursOrder) return ''
      return '临时加时将重新计算费用，并自动检测阿姨后续排班是否有冲突'
    },
    highValueOrders() {
      return [...this.orders]
        .sort((a, b) => parseFloat(b.totalPrice) - parseFloat(a.totalPrice))
        .slice(0, 10)
    },
    auntDailyStats() {
      const today = new Date().toDateString()
      return this.aunts.map(aunt => {
        const todayOrders = this.orders.filter(o => {
          const orderDate = new Date(o.startTime).toDateString()
          return o.auntId === aunt.id && 
                 orderDate === today && 
                 (o.status === 'ACCEPTED' || o.status === 'IN_PROGRESS' || o.status === 'COMPLETED')
        })
        const todayCount = todayOrders.length
        const remaining = aunt.maxDailyOrders - todayCount
        const usageRate = Math.min(100, Math.round((todayCount / aunt.maxDailyOrders) * 100))
        return {
          id: aunt.id,
          name: aunt.name,
          rating: aunt.rating,
          todayOrders: todayCount,
          maxDailyOrders: aunt.maxDailyOrders,
          remaining: Math.max(0, remaining),
          usageRate
        }
      })
    }
  },
  mounted() {
    this.loadOrders()
    this.loadAunts()
  },
  methods: {
    async loadOrders() {
      const res = await this.$axios.get('/orders')
      if (res.code === 200) {
        this.orders = res.data
      }
    },
    async loadAunts() {
      const res = await this.$axios.get('/aunts')
      if (res.code === 200) {
        this.aunts = res.data
      }
    },
    refreshData() {
      this.loadOrders()
      this.loadAunts()
      this.$message.success('刷新成功')
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
    },
    getOrderValueLevel(price) {
      const p = parseFloat(price)
      if (p >= 500) return { type: 'danger', label: '超高' }
      if (p >= 300) return { type: 'warning', label: '高' }
      if (p >= 150) return { type: 'primary', label: '中' }
      return { type: 'info', label: '普通' }
    },
    getAuntRating(auntId) {
      const aunt = this.aunts.find(a => a.id === auntId)
      return aunt ? aunt.rating : null
    },
    getAuntMaxDaily(auntId) {
      const aunt = this.aunts.find(a => a.id === auntId)
      return aunt ? aunt.maxDailyOrders : 0
    },
    getAuntDailyOrdersCount(auntId, startTime) {
      if (!auntId || !startTime) return 0
      const orderDate = new Date(startTime).toDateString()
      return this.orders.filter(o => {
        const oDate = new Date(o.startTime).toDateString()
        return o.auntId === auntId && 
               oDate === orderDate && 
               (o.status === 'ACCEPTED' || o.status === 'IN_PROGRESS' || o.status === 'COMPLETED')
      }).length
    },
    getAuntDailyOrdersType(auntId, startTime) {
      const count = this.getAuntDailyOrdersCount(auntId, startTime)
      const max = this.getAuntMaxDaily(auntId)
      if (count >= max) return 'danger'
      if (count > 0) return 'success'
      return 'info'
    },
    getAssignDesc(order) {
      if (!order.auntId) return ''
      const aunt = this.aunts.find(a => a.id === order.auntId)
      if (!aunt) return '已分配'
      
      const price = parseFloat(order.totalPrice)
      const isHighValue = price >= 300
      const isHighRating = aunt.rating >= 4.7
      
      if (isHighValue && isHighRating) {
        return '高价值订单优先分配给高评分阿姨'
      } else if (isHighValue) {
        return '高价值订单分配给有时间的高评分阿姨'
      } else if (isHighRating) {
        return '高评分阿姨接单'
      } else {
        return '正常分配'
      }
    },
    getAssignColor(order) {
      if (!order.auntId) return '#909399'
      const price = parseFloat(order.totalPrice)
      const aunt = this.aunts.find(a => a.id === order.auntId)
      if (!aunt) return '#409EFF'
      
      const isHighValue = price >= 300
      const isHighRating = aunt.rating >= 4.7
      
      if (isHighValue && isHighRating) return '#67C23A'
      if (isHighValue) return '#E6A23C'
      return '#409EFF'
    },
    showCancelDialog(order) {
      this.currentOrder = order
      this.cancelForm.orderId = order.id
      this.cancelForm.userId = order.userId
      this.cancelForm.reason = ''
      this.cancelDialogVisible = true
    },
    resetCancelForm() {
      this.currentOrder = null
      this.cancelForm = {
        orderId: '',
        userId: '',
        reason: ''
      }
    },
    async confirmCancelOrder() {
      if (!this.cancelForm.reason.trim()) {
        this.$message.warning('请输入取消原因')
        return
      }
      
      this.canceling = true
      try {
        const res = await this.$axios.post('/orders/cancel', this.cancelForm)
        if (res.code === 200) {
          this.cancelDialogVisible = false
          this.canceledOrderId = res.data.id
          this.canceledPenalty = res.data.cancelPenalty || 0
          this.resultDialogVisible = true
          this.loadOrders()
        } else {
          this.$message.error(res.message || '取消失败')
        }
      } catch (e) {
        this.$message.error('取消失败，请重试')
      } finally {
        this.canceling = false
      }
    },
    formatDateTime(date) {
      const pad = n => n < 10 ? '0' + n : n
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    },
    isTimeOverlap(start1, end1, start2, end2) {
      const s1 = new Date(start1).getTime()
      const e1 = new Date(end1).getTime()
      const s2 = new Date(start2).getTime()
      const e2 = new Date(end2).getTime()
      return s1 < e2 && s2 < e1
    },
    async showAddHoursDialog(order) {
      this.addHoursOrder = order
      this.addHoursForm.orderId = order.id
      this.addHoursForm.additionalHours = 1
      this.scheduleConflict = false
      this.auntOrders = []
      this.addHoursDialogVisible = true
      
      if (order.auntId) {
        await this.loadAuntOrders(order.auntId)
        this.checkScheduleConflict()
      }
    },
    async loadAuntOrders(auntId) {
      this.scheduleCheckLoading = true
      try {
        const res = await this.$axios.get(`/orders/aunt/${auntId}`)
        if (res.code === 200) {
          this.auntOrders = res.data.filter(o => 
            o.id !== this.addHoursForm.orderId && 
            (o.status === 'ACCEPTED' || o.status === 'IN_PROGRESS')
          )
        }
      } catch (e) {
        console.error('加载阿姨订单失败', e)
      } finally {
        this.scheduleCheckLoading = false
      }
    },
    calculateAddHours() {
      this.checkScheduleConflict()
    },
    checkScheduleConflict() {
      if (!this.addHoursOrder || !this.auntOrders.length) {
        this.scheduleConflict = false
        return
      }

      const currentEndTime = new Date(this.addHoursOrder.endTime)
      const newEndTime = new Date(currentEndTime)
      newEndTime.setHours(newEndTime.getHours() + this.addHoursForm.additionalHours)

      const conflict = this.auntOrders.some(o => {
        const oEndTime = new Date(o.endTime)
        return this.isTimeOverlap(currentEndTime, newEndTime, o.startTime, oEndTime)
      })

      this.scheduleConflict = conflict
    },
    resetAddHoursForm() {
      this.addHoursOrder = null
      this.auntOrders = []
      this.scheduleConflict = false
      this.addHoursForm = {
        orderId: '',
        additionalHours: 1
      }
    },
    async confirmAddHours() {
      this.addingHours = true
      try {
        const res = await this.$axios.post('/orders/add-hours', this.addHoursForm)
        if (res.code === 200) {
          this.addHoursDialogVisible = false
          this.addHoursResult = {
            orderId: res.data.id,
            oldDuration: res.data.durationHours - this.addHoursForm.additionalHours,
            newDuration: res.data.durationHours,
            oldEndTime: this.addHoursOrder.endTime,
            newEndTime: res.data.endTime,
            additionalPrice: this.additionalPrice,
            newTotalPrice: res.data.totalPrice
          }
          this.addHoursResultVisible = true
          this.loadOrders()
        } else {
          this.$message.error(res.message || '加时失败')
        }
      } catch (e) {
        this.$message.error('加时失败，请重试')
      } finally {
        this.addingHours = false
      }
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
