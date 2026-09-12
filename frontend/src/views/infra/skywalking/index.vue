<template>
  <doc-alert title="服务监控" url="https://doc.iocoder.cn/server-monitor/" />

  <ContentWrap :bodyStyle="{ padding: '0px' }" class="!mb-0">
    <IFrame v-if="!loading && src" :src="src" />
    <el-empty v-else-if="!loading" description="暂未配置企业服务监控地址" />
  </ContentWrap>
</template>
<script lang="ts" setup>
import * as ConfigApi from '@/api/infra/config'

defineOptions({ name: 'InfraSkyWalking' })

const loading = ref(true) // 是否加载中
// 企业尚未配置时显示空状态，不连接模板作者的演示服务。
const src = ref('')

/** 初始化 */
onMounted(async () => {
  try {
    const data = await ConfigApi.getConfigKey('url.skywalking')
    if (data && data.length > 0) {
      src.value = data
    }
  } finally {
    loading.value = false
  }
})
</script>
