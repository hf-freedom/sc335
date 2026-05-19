const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 4008,
    historyApiFallback: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8008',
        changeOrigin: true
      }
    }
  }
})
