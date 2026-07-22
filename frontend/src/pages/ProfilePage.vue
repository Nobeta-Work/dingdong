<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Camera, UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, type UploadRequestOptions, type UploadRawFile } from 'element-plus'
import { authApi, fileApi, type User } from '@/api/services'
import { useSession } from '@/composables/session'

const form = ref<Partial<User>>({})
const { setUser } = useSession()
const loading = ref(true)
const saving = ref(false)
const avatarUploading = ref(false)

const validateAvatar = (file: UploadRawFile) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('头像图片不能超过 5MB')
    return false
  }
  return true
}

const uploadAvatar = async ({ file }: UploadRequestOptions) => {
  avatarUploading.value = true
  try {
    const result = await fileApi.uploadImage(file)
    form.value.avatarUrl = result.url
    ElMessage.success('头像已上传，保存资料后生效')
  } finally {
    avatarUploading.value = false
  }
}

const save = async () => {
  saving.value = true
  try {
    const user = await authApi.updateMe(form.value)
    form.value = user
    setUser(user)
    ElMessage.success('资料已保存')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try { form.value = await authApi.me() }
  finally { loading.value = false }
})
</script>

<template>
  <div class="shell page personal-layout">
    <aside class="personal-nav">
      <h3>个人中心</h3>
      <RouterLink to="/profile" class="selected">个人资料</RouterLink>
      <RouterLink to="/orders">我的订单</RouterLink>
      <RouterLink to="/addresses">收货地址</RouterLink>
      <RouterLink to="/account">账户安全</RouterLink>
    </aside>
    <section class="profile-content">
      <div class="profile-heading"><div><h1>个人资料</h1><p>更新你的公开昵称与头像。</p></div></div>
      <el-skeleton :loading="loading" animated :rows="5">
        <template #default>
          <div class="profile-card">
            <div class="avatar-editor">
              <el-avatar :size="88" :src="form.avatarUrl">{{ form.nickname?.slice(0, 1) || form.username?.slice(0, 1) }}</el-avatar>
              <span><el-icon><Camera /></el-icon></span>
            </div>
            <div><b>{{ form.nickname || '未设置昵称' }}</b><p>@{{ form.username }}</p><small>支持 JPG、PNG、WebP，文件不超过 5MB</small></div>
            <el-upload :show-file-list="false" accept="image/*" :before-upload="validateAvatar" :http-request="uploadAvatar">
              <el-button :icon="UploadFilled" :loading="avatarUploading">上传新头像</el-button>
            </el-upload>
          </div>
          <el-form :model="form" label-position="top" class="profile-form">
            <el-form-item label="昵称"><el-input v-model="form.nickname" maxlength="30" show-word-limit /></el-form-item>
            <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
            <div class="form-pair">
              <el-form-item label="手机号"><el-input v-model="form.phone" disabled /><small>手机号换绑请前往账户安全完成验证。</small></el-form-item>
              <el-form-item label="邮箱"><el-input v-model="form.email" disabled /></el-form-item>
            </div>
            <el-form-item label="头像地址"><el-input v-model="form.avatarUrl" placeholder="上传后自动回填，也可输入公开图片 URL" /></el-form-item>
            <el-form-item><el-button type="primary" size="large" :loading="saving" @click="save">保存修改</el-button></el-form-item>
          </el-form>
        </template>
      </el-skeleton>
    </section>
  </div>
</template>
