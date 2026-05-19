<template>
  <div id="app">
    <el-container>
      <el-header style="background: #409EFF; color: white; display: flex; align-items: center; justify-content: space-between;">
        <div style="font-size: 20px; font-weight: bold;">家政阿姨派单系统</div>
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          background-color="#409EFF"
          text-color="white"
          active-text-color="#ffd04b"
          router
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/user">用户端</el-menu-item>
          <el-menu-item index="/aunt">阿姨端</el-menu-item>
          <el-menu-item index="/orders">订单管理</el-menu-item>
        </el-menu>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App',
  computed: {
    activeMenu() {
      return this.$route.path
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  min-height: 100vh;
  background-color: #f5f7fa;
}
.el-header {
  padding: 0 20px;
}
.el-menu--horizontal {
  border-bottom: none;
}
</style>
