<script lang="ts" setup>
import { companyLogo, companyName } from '@/config/brand'
import { useUserStore } from '@/store/modules/user'

defineOptions({ name: 'Index' })

// 企业欢迎区读取真实的登录用户，不再展示模板项目和示例统计。
const userStore = useUserStore()
const username = computed(() => userStore.getUser.nickname)
const { push } = useRouter()
</script>

<template>
  <div class="company-home">
    <!-- 企业标识：直接使用提供的原始 Logo，保持图片比例。 -->
    <el-card shadow="never" class="company-welcome">
      <div class="company-welcome__content">
        <img :src="companyLogo" :alt="`${companyName} Logo`" class="company-welcome__logo" />
        <div>
          <p class="company-welcome__eyebrow">企业工作台</p>
          <h1>{{ companyName }}</h1>
          <p class="company-welcome__greeting">{{ username }}，欢迎回来。</p>
          <p class="company-welcome__description">在这里管理企业成员、角色权限与日常工作。</p>
        </div>
      </div>
    </el-card>

    <!-- 快捷入口沿用已有页面及权限，不显示尚未实现的业务功能。 -->
    <el-card shadow="never" class="company-shortcuts">
      <template #header><span>常用功能</span></template>
      <div class="company-shortcuts__links">
        <el-button size="large" type="primary" @click="push('/quote/workbench')">
          <Icon icon="ep:document" class="mr-8px" />报价工作台
        </el-button>
        <el-button size="large" @click="push('/user/profile')">
          <Icon icon="ep:user" class="mr-8px" />个人中心
        </el-button>
        <el-button v-hasPermi="['system:user:query']" size="large" @click="push('/system/user')">
          <Icon icon="ep:user-filled" class="mr-8px" />用户管理
        </el-button>
        <el-button v-hasPermi="['system:role:query']" size="large" @click="push('/system/role')">
          <Icon icon="ep:key" class="mr-8px" />角色管理
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
// 欢迎区在窄屏自动换行，Logo 使用 contain 完整展示。
.company-home {
  display: grid;
  gap: 20px;
}

.company-welcome {
  &__content {
    display: flex;
    flex-wrap: wrap;
    gap: 32px;
    align-items: center;
    padding: 24px;
  }

  &__logo {
    width: 180px;
    max-width: 100%;
    height: auto;
    border-radius: 16px;
  }

  h1 {
    margin: 8px 0 20px;
    font-size: 36px;
    line-height: 1.25;
  }

  &__eyebrow,
  &__description {
    margin: 8px 0;
    color: var(--el-text-color-secondary);
    line-height: 1.6;
  }

  &__greeting {
    margin: 8px 0;
    font-size: 18px;
  }
}

.company-shortcuts__links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  .el-button {
    margin-left: 0;
  }
}

@media (max-width: 600px) {
  .company-welcome__content {
    gap: 20px;
    padding: 4px;
  }

  .company-welcome__logo {
    width: 120px;
  }
}
</style>
