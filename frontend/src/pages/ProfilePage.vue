<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { authApi, type User } from '@/api/services'
import { useSession } from '@/composables/session'
// 用户资料查询与修改的前端页面状态
const form = ref<Partial<User>>({}); const saved = ref(false); const { setUser } = useSession(); const loading = ref(true)
// 保存修改：调用 authApi.updateMe() 提交用户资料 → 更新本地表单和全局 session
const save = async () => { const user = await authApi.updateMe(form.value); form.value = user; setUser(user); saved.value = true }
// 页面加载：调用 authApi.me() 查询当前用户资料并填充表单
onMounted(async () => { try { form.value = await authApi.me() } finally { loading.value = false } })
</script>
<template>
  <!-- 用户资料查询与修改页面：展示当前用户信息，支持编辑昵称/手机号/邮箱/头像URL并保存 -->
  <div class="shell page personal-layout">
    <aside class="personal-nav">
      <h3>个人中心</h3>
      <RouterLink to="/profile" class="selected">个人资料</RouterLink>
      <RouterLink to="/orders">我的订单</RouterLink>
      <a>收货地址</a>
      <a>账户安全</a>
    </aside>
    <section class="profile-content">
      <h1>个人资料</h1>
      <!-- 加载骨架屏：数据请求过程中展示占位动画 -->
      <el-skeleton :loading="loading" animated :rows="5">
        <template #default>
          <!-- 用户头像与名称卡片 -->
          <div class="profile-card">
            <el-avatar :size="68" :src="form.avatarUrl">{{ form.nickname?.slice(0, 1) }}</el-avatar>
            <div><b>{{ form.nickname }}</b><p>{{ form.username }}</p></div>
          </div>
          <!-- 用户资料编辑表单：昵称（必填）、手机号、邮箱、头像 URL -->
          <el-form :model="form" label-width="80px" class="profile-form">
            <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
            <el-form-item label="头像 URL"><el-input v-model="form.avatarUrl" placeholder="填写可公开访问的头像图片 URL" /></el-form-item>
            <el-form-item>
              <!-- 保存修改按钮：点击后触发 save() 调用 PUT /api/users/me 更新资料 -->
              <el-button type="primary" @click="save">保存修改</el-button>
            </el-form-item>
          </el-form>
          <!-- 保存成功提示 -->
          <el-alert v-if="saved" type="success" title="资料已保存" show-icon :closable="false" />
        </template>
      </el-skeleton>
    </section>
  </div>
</template>
