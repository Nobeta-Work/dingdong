export type Product = { id: number; name: string; subtitle: string; price: number; oldPrice?: number; image: string; category: string; tag?: string; sales: number; stock: number; specs: string[] }

export const categories = ['手机数码', '电脑办公', '家用电器', '食品生鲜', '美妆个护', '家居家装', '运动户外', '图书文娱']
export const products: Product[] = [
  { id: 1, name: 'Apple iPhone 16 128GB', subtitle: '轻薄耐用，超瓷晶面板', price: 5399, oldPrice: 5999, image: 'https://images.unsplash.com/photo-1727252485756-4c8d75f13f04?auto=format&fit=crop&w=900&q=85', category: '手机数码', tag: '限时直降', sales: 2300, stock: 56, specs: ['深邃蓝色', '128GB', '官方标配'] },
  { id: 2, name: '轻薄办公笔记本 14 英寸', subtitle: '高性能处理器，全天候续航', price: 4499, oldPrice: 4999, image: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=85', category: '电脑办公', tag: '以旧换新', sales: 1240, stock: 32, specs: ['月光银', '16GB + 512GB', '14 英寸'] },
  { id: 3, name: '降噪蓝牙耳机 Pro', subtitle: '沉浸式空间音频', price: 699, oldPrice: 899, image: 'https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?auto=format&fit=crop&w=900&q=85', category: '手机数码', tag: '热卖', sales: 5600, stock: 109, specs: ['曜石黑', '标准版', '含充电盒'] },
  { id: 4, name: '智能扫拖机器人', subtitle: '强劲吸力，自动集尘', price: 1899, image: 'https://images.unsplash.com/photo-1589003077984-894e133dabab?auto=format&fit=crop&w=900&q=85', category: '家用电器', tag: '新品', sales: 860, stock: 43, specs: ['白色', '水箱版', '全国联保'] },
  { id: 5, name: '精品咖啡豆 500g', subtitle: '中深烘焙，醇厚坚果香', price: 69, image: 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=85', category: '食品生鲜', sales: 9010, stock: 220, specs: ['经典拼配', '500g', '盒装'] },
  { id: 6, name: '氨基酸洁面乳 120g', subtitle: '温和净澈，水润不紧绷', price: 89, image: 'https://images.unsplash.com/photo-1556229010-6c3f2c9ca5f8?auto=format&fit=crop&w=900&q=85', category: '美妆个护', tag: '满减', sales: 3450, stock: 186, specs: ['120g', '清润款', '单支装'] },
]

export const formatPrice = (price: number) => price.toFixed(2)
