<script setup lang="ts">
import { onMounted } from 'vue'
import QuoteForm from './components/QuoteForm.vue'
import QuoteResult from './components/QuoteResult.vue'
import { useQuoteWorkbench } from './useQuoteWorkbench'
import type { QuoteRequest } from '@/api/quote'
import './quote.css'

/** 报价工作台入口：需求 → 参数建议与确认 → 后端计算 → Excel 清单。 */
defineOptions({ name: 'QuoteWorkbench' })
const {
  catalog,
  skills,
  mode,
  model,
  busy,
  error,
  result,
  suggestion,
  form,
  load,
  suggest,
  calculate,
  applySuggestion,
  download
} = useQuoteWorkbench()
function updateForm(next: QuoteRequest) {
  Object.assign(form, next)
}
onMounted(load)
</script>

<template>
  <main class="quote-workbench">
    <header class="quote-hero"
      ><div
        ><p class="eyebrow">金茂源 · 报价助手</p><h1>每一份报价，都有据可查。</h1
        ><p>Skill 帮您调整参数，后端逐项算清，直接生成报价清单。</p></div
      ><span class="demo-label">DEMO · 示例业务数据</span></header
    >
    <div v-if="error" class="error-box" role="alert"
      >{{ error
      }}<button v-if="!catalog" class="text-button" :disabled="busy" @click="load"
        >重新连接后端</button
      ></div
    >
    <div v-if="!catalog && busy" class="quote-panel" role="status">正在读取后端资料与 Skill…</div>
    <div v-if="catalog" class="workbench-grid">
      <QuoteForm
        :model-value="form"
        :catalog="catalog"
        :busy="busy"
        @update:model-value="updateForm"
        @calculate="calculate"
        @suggest="suggest"
      />
      <aside class="quote-panel skill-panel">
        <div class="section-heading"
          ><span class="step-number">02</span
          ><div
            ><h2>参数建议</h2
            ><p>{{
              mode === 'model'
                ? `真实模型 · ${model}`
                : mode === 'config-error'
                  ? '模型配置不完整'
                  : '模型尚未配置'
            }}</p></div
          ></div
        >
        <p class="muted"
          >先读取总 Skill，再读取成本、运输和利润规则。建议只影响本次报价，经您确认才会采用。</p
        >
        <div v-if="suggestion" class="suggestion-box" aria-live="polite"
          ><span>建议调整 · 目标毛利率</span
          ><p class="rate-change">{{ suggestion.before }}% <span>→</span> {{ suggestion.after }}%</p
          ><p>{{ suggestion.reason }}</p
          ><button class="primary-button" :disabled="busy" @click="applySuggestion"
            >确认参数并重新计算</button
          ><button class="text-button" :disabled="busy" @click="suggestion = undefined"
            >保留原参数</button
          ></div
        >
        <div v-else class="suggestion-empty"
          ><span class="quiet-symbol">↗</span><p>先设定您的报价目标</p
          ><small>点击“获取参数建议”，查看调整原因和前后差异。</small></div
        >
        <details class="skill-details"
          ><summary>查看总 Skill 与子 Skill（{{ skills.length }}）</summary
          ><article v-for="skill in skills" :key="skill.id">
            <pre>{{ skill.content }}</pre>
          </article></details
        >
        <div class="source-note"
          ><b>本次计算依据</b><p>{{ catalog.source }}</p
          ><span
            >原料损耗 {{ catalog.rules.lossRate }}% · 税率 {{ catalog.rules.taxRate }}%</span
          ></div
        >
      </aside>
    </div>
    <QuoteResult v-if="result" :result="result" :busy="busy" @download="download" />
    <section v-else-if="catalog" class="quote-panel pending-result"
      ><span class="step-number">03</span
      ><div
        ><h2>报价清单将在这里生成</h2
        ><p>计算后可查看分项金额、核对依据，并下载 Excel。修改参数后需要重新计算。</p></div
      ></section
    >
  </main>
</template>
