<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi, type User } from '@/api/services'
import { useSession } from '@/composables/session'
const form = ref<Partial<User>>({}); const { setUser } = useSession(); const loading = ref(true); const saving = ref(false)
const save = async () => { saving.value = true; try { const user = await authApi.updateMe(form.value); form.value = user; setUser(user); ElMessage.success('资料已保存') } finally { saving.value = false } }; onMounted(async () => { try { form.value = await authApi.me() } finally { loading.value = false } })
</script>
<template><div class="shell page personal-layout"><aside class="personal-nav"><h3>个人中心</h3><RouterLink to="/profile" class="selected">个人资料</RouterLink><RouterLink to="/orders">我的订单</RouterLink><RouterLink to="/addresses">收货地址</RouterLink><RouterLink to="/account">账户安全</RouterLink></aside><section class="profile-content"><h1>个人资料</h1><el-skeleton :loading="loading" animated :rows="5"><template #default><div class="profile-card"><el-avatar :size="68" :src="form.avatarUrl">{{ form.nickname?.slice(0, 1) }}</el-avatar><div><b>{{ form.nickname }}</b><p>{{ form.username }}</p></div></div><el-form :model="form" label-width="80px" class="profile-form"><el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item><el-form-item label="头像 URL"><el-input v-model="form.avatarUrl" placeholder="填写可公开访问的头像图片 URL" /></el-form-item><el-form-item><el-button type="primary" :loading="saving" @click="save">保存修改</el-button></el-form-item></el-form></template></el-skeleton></section></div></template>
