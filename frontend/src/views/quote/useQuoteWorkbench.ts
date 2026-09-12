import { ref, reactive, watch } from 'vue'
import { getCatalog, getSkills, suggestQuote, calculateQuote, exportQuote } from '@/api/quote'
import type { Catalog, QuoteRequest, QuoteResult, Skill, Suggestion } from '@/api/quote'

/** 工作台状态：编辑后立即使旧建议和旧报价失效，防止下载与当前参数不符的清单。 */
export function useQuoteWorkbench() {
  const catalog = ref<Catalog>()
  const skills = ref<Skill[]>([])
  const mode = ref('loading')
  const model = ref('')
  const busy = ref(false)
  const error = ref('')
  const result = ref<QuoteResult>()
  const suggestion = ref<Suggestion>()
  const form = reactive<QuoteRequest>({
    items: [{ productId: 'aluminum', quantity: 100 }],
    regionId: 'province',
    targetMargin: 25,
    budget: 0,
    objective: 'balanced',
    note: ''
  })
  const revision = ref(0)
  watch(
    form,
    () => {
      revision.value++
      result.value = undefined
      suggestion.value = undefined
      error.value = ''
    },
    { deep: true, flush: 'sync' }
  )
  const copy = (): QuoteRequest => JSON.parse(JSON.stringify(form))
  const message = (e: unknown) => (e instanceof Error ? e.message : String(e))
  async function run(action: () => Promise<void>) {
    busy.value = true
    error.value = ''
    try {
      await action()
    } catch (e) {
      error.value = message(e)
    } finally {
      busy.value = false
    }
  }
  async function load() {
    await run(async () => {
      const [data, info] = await Promise.all([getCatalog(), getSkills()])
      catalog.value = data
      skills.value = info.skills
      mode.value = info.mode
      model.value = info.model
    })
  }
  async function suggest() {
    const version = revision.value
    suggestion.value = undefined
    await run(async () => {
      const answer = await suggestQuote(copy())
      if (version === revision.value) suggestion.value = answer
    })
  }
  async function calculate() {
    const version = revision.value
    result.value = undefined
    await run(async () => {
      const answer = await calculateQuote(copy())
      if (version === revision.value) result.value = answer
    })
  }
  async function applySuggestion() {
    if (!suggestion.value) return
    const next = suggestion.value.after
    form.targetMargin = next
    suggestion.value = undefined
    await calculate()
  }
  async function download(audience: 'customer' | 'internal') {
    if (!result.value?.feasible) return
    const id = result.value.id
    await run(async () => {
      const blob = await exportQuote(id, audience)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `报价清单-${audience === 'customer' ? '客户版' : '内部版'}-${id.slice(0, 8)}.xlsx`
      link.click()
      setTimeout(() => URL.revokeObjectURL(url), 1000)
    })
  }
  return {
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
  }
}
