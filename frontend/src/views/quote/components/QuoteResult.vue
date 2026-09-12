<script setup lang="ts">
import type { QuoteResult } from '@/api/quote'
/** 结果与导出：展示后端原始金额，客户文件和内部文件使用同一份结果快照。 */
defineProps<{ result: QuoteResult; busy: boolean }>()
defineEmits<{ download: [audience: 'customer' | 'internal'] }>()
const money = (value: number) =>
  new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(
    value
  )
</script>

<template>
  <section class="quote-panel result-panel" aria-label="报价结果">
    <div class="section-heading"
      ><span class="step-number">03</span
      ><div
        ><h2>报价结果</h2><p>由后端计算 · {{ result.formulaVersion }}</p></div
      ><span :class="['status', result.feasible ? 'ok' : 'conflict']">{{
        result.feasible ? '符合当前约束' : '需要调整条件'
      }}</span></div
    >
    <div v-if="!result.feasible" class="error-box" role="alert"
      ><p v-for="conflict in result.conflicts" :key="conflict">{{ conflict }}</p></div
    >
    <div class="result-summary"
      ><div
        ><span>含税总报价 · 人民币</span
        ><strong data-testid="quote-total">¥ {{ money(result.total) }}</strong
        ><small>已含运输及 {{ result.catalog.rules.taxRate }}% 税费</small></div
      ><div
        ><span>预计利润</span><b>¥ {{ money(result.profit) }}</b
        ><small>实际毛利率 {{ result.actualMargin.toFixed(2) }}%</small></div
      ></div
    >
    <div class="table-scroll"
      ><table
        ><thead
          ><tr><th>产品</th><th>数量</th><th>不含税单价</th><th>不含税金额</th></tr></thead
        ><tbody
          ><tr v-for="line in result.lines" :key="line.productId"
            ><td
              >{{ line.name }}<small>{{ line.spec }}</small></td
            ><td>{{ line.quantity }}</td
            ><td>{{ money(line.unitPrice) }}</td
            ><td>{{ money(line.amount) }}</td></tr
          ></tbody
        ></table
      ></div
    >
    <dl class="totals"
      ><div
        ><dt>不含税合计</dt><dd>¥ {{ money(result.net) }}</dd></div
      ><div
        ><dt>税额</dt><dd>¥ {{ money(result.tax) }}</dd></div
      ><div
        ><dt>内部总成本（含运输）</dt><dd>¥ {{ money(result.cost) }}</dd></div
      ></dl
    >
    <details class="calculation-details"
      ><summary>展开计算过程与参数依据</summary
      ><ol
        ><li v-for="(step, i) in result.steps" :key="i"
          ><b>{{ step.unit }}</b
          ><p>{{ step.formula }}</p
          ><span>¥ {{ money(step.amount) }}</span></li
        ></ol
      ><p
        >目标毛利率 {{ result.parameters.targetMargin }}% · 成本版本
        {{ result.catalog.rules.version }}</p
      ><small>报价编号 {{ result.id }}<br />{{ result.createdAt }}</small></details
    >
    <div class="actions"
      ><button
        class="primary-button"
        :disabled="busy || !result.feasible"
        @click="$emit('download', 'customer')"
        >下载客户版 Excel</button
      ><button
        class="secondary-button"
        :disabled="busy || !result.feasible"
        @click="$emit('download', 'internal')"
        >下载内部测算版</button
      ></div
    >
    <p class="muted">客户版仅包含报价清单；内部版增加成本、利润与计算依据。</p>
  </section>
</template>
