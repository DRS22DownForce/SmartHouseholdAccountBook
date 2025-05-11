import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  //開発用の設定
  //viteの開発サーバー上で/api/*というリクエストが発生すると、viteがhttp://localhost:8080/api/*に転送する
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
      },
    },
  },
})
