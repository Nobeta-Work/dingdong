<script setup lang="ts">
import { ref } from 'vue'

const form = ref({ nickname: '\u5f20\u5c0f\u5317', phone: '13800008888', email: 'zhang@example.com' })
const saved = ref(false)
</script>

<template>
  <div class="shell page personal-layout">
    <aside class="personal-nav">
      <h3>&#x4e2a;&#x4eba;&#x4e2d;&#x5fc3;</h3>
      <RouterLink to="/profile" class="selected">&#x4e2a;&#x4eba;&#x8d44;&#x6599;</RouterLink>
      <RouterLink to="/orders">&#x6211;&#x7684;&#x8ba2;&#x5355;</RouterLink>
      <a>&#x6536;&#x8d27;&#x5730;&#x5740;</a><a>&#x8d26;&#x6237;&#x5b89;&#x5168;</a>
    </aside>
    <section class="profile-content">
      <h1>&#x4e2a;&#x4eba;&#x8d44;&#x6599;</h1>
      <div class="profile-card"><div class="avatar">&#x5f20;</div><div><b>&#x5f20;&#x5c0f;&#x5317;</b><p>&#x666e;&#x901a;&#x4f1a;&#x5458;</p><el-button plain size="small">&#x66f4;&#x6362;&#x5934;&#x50cf;</el-button></div></div>
      <el-form :model="form" label-width="80px" class="profile-form">
        <el-form-item label="&#x6635;&#x79f0;"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="&#x624b;&#x673a;&#x53f7;"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="&#x90ae;&#x7bb1;"><el-input v-model="form.email" /></el-form-item>
        <el-form-item><el-button type="primary" @click="saved = true">&#x4fdd;&#x5b58;&#x4fee;&#x6539;</el-button></el-form-item>
      </el-form>
      <el-alert v-if="saved" type="success" title="&#x8d44;&#x6599;&#x5df2;&#x4fdd;&#x5b58;" show-icon :closable="false" />
    </section>
  </div>
</template>
