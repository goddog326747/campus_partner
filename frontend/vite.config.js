import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 支持 SSE 流式传输
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // 确保 SSE 请求的 header 正确传递
            if (req.headers.accept === 'text/event-stream') {
              proxyReq.setHeader('Accept', 'text/event-stream')
            }
          })
        }
      }
    }
  }
})
