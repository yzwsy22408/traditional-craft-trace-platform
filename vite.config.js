import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import ElementPlusAutoImport from 'unplugin-element-plus/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const host = env.VITE_DEV_HOST || '0.0.0.0'
  const port = Number(env.VITE_DEV_PORT || 5173)
  const apiTarget = env.VITE_API_TARGET || 'http://127.0.0.1:8080'

  return {
    plugins: [
      vue(),
      Components({
        dts: false,
        resolvers: [
          ElementPlusResolver({
            importStyle: 'css',
            directives: true
          })
        ]
      }),
      ElementPlusAutoImport()
    ],
    resolve: {
      alias: {
        '@': '/src'
      }
    },
    server: {
      host,
      port,
      strictPort: true,
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: true,
          secure: false,
          ws: true
        },
        '/uploads': {
          target: apiTarget,
          changeOrigin: true,
          secure: false
        }
      }
    },
    build: {
      rollupOptions: {
        output: {
          manualChunks: {
            'vendor-vue': ['vue', 'vue-router', 'pinia'],
            'vendor-icons': ['@element-plus/icons-vue'],
            'vendor-http': ['axios'],
            'vendor-qrcode': ['qrcode.vue'],
            'vendor-html2canvas': ['html2canvas'],
            'vendor-xlsx': ['xlsx']
          }
        }
      }
    }
  }
})
