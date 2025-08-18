import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import fs from 'node:fs'

export default defineConfig({
    plugins: [vue(), vueDevTools()],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
        },
    },
    server: {
        // ⚠️ in prod — https
        // https: {
        //     key: fs.readFileSync('./cert/localhost-key.pem'),
        //     cert: fs.readFileSync('./cert/localhost.pem'),
        // },
        port: 5173,
    },
})
