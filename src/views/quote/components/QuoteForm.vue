<script setup lang="ts">
import type { Catalog, QuoteRequest } from '@/api/quote'

/** 需求输入区：只收集数量、运输和报价参数，不在浏览器中计算价格。 */
const props = defineProps<{ modelValue: QuoteRequest; catalog: Catalog; busy: boolean }>()
const emit = defineEmits<{
  'update:modelValue': [value: QuoteRequest]
  calculate: []
  suggest: []
}>()
function update<K extends keyof QuoteRequest>(key: K, value: QuoteRequest[K]) {
  emit('update:modelValue', { ...props.modelValue, [key]: value })
}
function changeItem(index: number, key: 'quantity' | 'productId', value: string) {
  update(
    'items',
    props.modelValue.items.map((item, i) =>
      i === index ? { ...item, [key]: key === 'quantity' ? Number(value) : value } : item
    )
  )
}
function addItem() {
  const next = props.catalog.products.find(
    (p) => !props.modelValue.items.some((i) => i.productId === p.id)
  )
  if (next) update('items', [...props.modelValue.items, { productId: next.id, quantity: 100 }])
}
const value = (event: Event) => (event.target as HTMLInputElement).value
</script>

<template>
  <form class="quote-panel" @submit.prevent="emit('calculate')">
    <fieldset :disabled="busy">
      <div class="section-heading"
        ><span class="step-number">01</span
        ><div><h2>这次要报什么？</h2><p>选择产品和数量，成本由后端资料表提供。</p></div></div
      >
      <div v-for="(item, index) in modelValue.items" :key="index" class="item-row">
        <label
          >产品 {{ index + 1 }}
          <select
            :aria-label="`产品 ${index + 1}`"
            :value="item.productId"
            @change="changeItem(index, 'productId', value($event))"
          >
            <option
              v-for="p in catalog.products"
              :key="p.id"
              :value="p.id"
              :disabled="modelValue.items.some((i, n) => n !== index && i.productId === p.id)"
              >{{ p.name }} · {{ p.spec }}</option
            >
          </select>
        </label>
        <label
          >数量（件）<input
            :aria-label="`数量 ${index + 1}`"
            type="number"
            min="1"
            max="100000"
            step="1"
            required
            :value="item.quantity"
            @input="changeItem(index, 'quantity', value($event))"
        /></label>
        <button
          class="text-button remove"
          type="button"
          :aria-label="`删除产品 ${index + 1}`"
          :disabled="modelValue.items.length === 1"
          @click="
            update(
              'items',
              modelValue.items.filter((_, n) => n !== index)
            )
          "
          >移除</button
        >
      </div>
      <button
        class="text-button"
        type="button"
        :disabled="modelValue.items.length >= catalog.products.length"
        @click="addItem"
        >＋ 添加产品</button
      >
      <div class="field-grid">
        <label
          >运输地区<select
            aria-label="运输地区"
            :value="modelValue.regionId"
            @change="update('regionId', value($event))"
            ><option v-for="r in catalog.regions" :key="r.id" :value="r.id">{{
              r.name
            }}</option></select
          ></label
        >
        <label
          >本次目标<select
            aria-label="本次目标"
            :value="modelValue.objective"
            @change="update('objective', value($event) as QuoteRequest['objective'])"
            ><option value="balanced">平衡价格与利润</option
            ><option value="competitive">更有竞争力的价格</option
            ><option value="profit">优先提高利润</option></select
          ></label
        >
        <label
          >目标毛利率（%）<input
            aria-label="目标毛利率"
            type="number"
            :min="catalog.rules.minMargin"
            :max="catalog.rules.maxMargin"
            step="0.01"
            required
            :value="modelValue.targetMargin"
            @input="update('targetMargin', Number(value($event)))"
        /></label>
        <label
          >含税总预算（元）<input
            aria-label="含税总预算"
            type="number"
            min="0"
            max="1000000000"
            step="0.01"
            required
            :value="modelValue.budget"
            @input="update('budget', Number(value($event)))"
          /><small>填 0 表示不限制预算</small></label
        >
      </div>
      <p class="rule-note"
        >后端底线：毛利率 {{ catalog.rules.minMargin }}% 至
        {{ catalog.rules.maxMargin }}%。毛利率是利润占不含税收入的比例。</p
      >
      <label
        >补充要求（选填）<textarea
          aria-label="补充要求"
          maxlength="1000"
          rows="3"
          placeholder="例如：希望降低报价，但保持毛利底线。AI 会结合这些要求给出参数建议。"
          :value="modelValue.note"
          @input="update('note', value($event))"
        ></textarea>
      </label>
      <div class="actions"
        ><button class="primary-button" type="submit">{{ busy ? '处理中…' : '计算报价' }}</button
        ><button class="secondary-button" type="button" @click="emit('suggest')"
          >获取参数建议</button
        ></div
      >
    </fieldset>
  </form>
</template>
