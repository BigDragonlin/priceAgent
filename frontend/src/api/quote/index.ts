import request from '@/config/axios'

/** 报价接口合同：前端只提交需求，金额和成本一律取后端结果。 */
export interface QuoteRequest {
  items: { productId: string; quantity: number }[]
  regionId: string
  targetMargin: number
  budget: number
  objective: 'balanced' | 'competitive' | 'profit'
  note: string
}
export interface Catalog {
  products: {
    id: string
    name: string
    spec: string
    material: number
    processing: number
    packaging: number
    weightKg: number
  }[]
  regions: { id: string; name: string; baseFee: number; perKg: number }[]
  rules: {
    minMargin: number
    maxMargin: number
    lossRate: number
    taxRate: number
    version: string
  }
  source: string
}
export interface QuoteResult {
  id: string
  createdAt: string
  formulaVersion: string
  parameters: QuoteRequest
  catalog: Catalog
  lines: {
    productId: string
    name: string
    spec: string
    quantity: number
    cost: number
    allocatedFreight: number
    unitPrice: number
    amount: number
  }[]
  steps: { unit: string; formula: string; amount: number }[]
  cost: number
  freight: number
  net: number
  tax: number
  total: number
  profit: number
  actualMargin: number
  feasible: boolean
  conflicts: string[]
}
export interface Skill {
  id: string
  title: string
  content: string
}
export interface Suggestion {
  mode: string
  before: number
  after: number
  reason: string
  skills: string[]
  field: 'targetMargin'
}
// 默认使用现有芋道后端；独立验收时可指定报价后端地址，不改全局登录配置。
const config = import.meta.env.VITE_QUOTE_BASE_URL
  ? { baseURL: `${import.meta.env.VITE_QUOTE_BASE_URL}/admin-api` }
  : {}
export const getCatalog = () => request.get<Catalog>({ ...config, url: '/quote/catalog' })
export const getSkills = () =>
  request.get<{ mode: string; model: string; skills: Skill[] }>({ ...config, url: '/quote/skills' })
export const suggestQuote = (data: QuoteRequest) =>
  request.post<Suggestion>({ ...config, url: '/quote/suggest', timeout: 65000, data })
export const calculateQuote = (data: QuoteRequest) =>
  request.post<QuoteResult>({ ...config, url: '/quote/calculate', data })
export const exportQuote = (id: string, audience: 'customer' | 'internal') =>
  request.download<Blob>({ ...config, url: `/quote/${id}/excel`, params: { audience } })
