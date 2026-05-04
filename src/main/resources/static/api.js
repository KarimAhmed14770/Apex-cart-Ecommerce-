/* ─────────────────────────────────────────────────────────────────
   KeeConnect  ·  Shared API + Auth layer
   Base URL assumes Spring Boot is running on localhost:8080
   ───────────────────────────────────────────────────────────────── */

const BASE = 'http://localhost:8080';

/* ── Auth helpers ─────────────────────────────────────────────── */
const Auth = {
  save   : t   => localStorage.setItem('kc_jwt', t),
  get    : ()  => localStorage.getItem('kc_jwt'),
  clear  : ()  => { localStorage.removeItem('kc_jwt'); localStorage.removeItem('kc_pc'); },
  savePc : v   => localStorage.setItem('kc_pc', String(v)),
  isPc   : ()  => localStorage.getItem('kc_pc') === 'true',
  isIn   : ()  => !!Auth.get(),

  payload() {
    const t = Auth.get(); if (!t) return null;
    try { return JSON.parse(atob(t.split('.')[1])); } catch { return null; }
  },
  roles()      { const p = Auth.payload(); if (!p) return []; return (p.roles||[]).map(r => typeof r==='string'?r:r.authority); },
  hasRole(r)   { return Auth.roles().includes(r); },
  isAdmin()    { return Auth.hasRole('ROLE_ADMIN'); },
  isVendor()   { return Auth.hasRole('ROLE_SELLER'); },
  isCustomer() { return Auth.hasRole('ROLE_CUSTOMER'); },
  userId()     { return Auth.payload()?.userId ?? null; },
  username()   { return Auth.payload()?.sub ?? null; },

  redirectHome() {
    if (Auth.isAdmin())    { window.location.href = 'admin.html';  return; }
    if (Auth.isVendor())   { window.location.href = 'vendor.html'; return; }
    if (Auth.isCustomer()) { window.location.href = 'shop.html';   return; }
    window.location.href = 'index.html';
  },
  guard(requiredRole) {
    if (!Auth.isIn()) { window.location.href = 'index.html'; return; }
    if (requiredRole && !Auth.hasRole(requiredRole)) { Auth.redirectHome(); }
  },
};

/* ── HTTP core ────────────────────────────────────────────────── */
async function req(method, path, body = null, auth = true) {
  const headers = { 'Content-Type': 'application/json' };
  if (auth && Auth.get()) headers['Authorization'] = `Bearer ${Auth.get()}`;
  const res = await fetch(BASE + path, {
    method, headers,
    body: body ? JSON.stringify(body) : null,
  });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) throw new Error(data?.message || data?.error || `Error ${res.status}`);
  return data;
}

/* ── Multipart/form-data request (for file uploads) ──────────── */
// Do NOT set Content-Type — browser adds it with the correct boundary.
async function multipartReq(method, path, formData) {
  const headers = {};
  if (Auth.get()) headers['Authorization'] = `Bearer ${Auth.get()}`;
  const res = await fetch(BASE + path, { method, headers, body: formData });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) throw new Error(data?.message || data?.error || `Error ${res.status}`);
  return data;
}

/* ── Image URL normalizer ─────────────────────────────────────── */
// Images are served from /uploads/** (mapped in WebConfig).
// The DB may store just a filename ("cat.jpg"), a relative path
// ("uploads/cat.jpg"), an absolute path ("/uploads/cat.jpg"),
// or a full URL.  This helper normalises all four cases.
function imgSrc(url) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  if (url.startsWith('/')) return url;
  if (url.startsWith('uploads/')) return '/' + url;
  return '/uploads/' + url;
}

function q(params) {
  const s = Object.entries(params)
    .filter(([, v]) => v !== null && v !== undefined && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&');
  return s ? `?${s}` : '';
}

/* ── API ──────────────────────────────────────────────────────── */
const API = {

  /* Auth */
  login            : dto => req('POST', '/api/auth/login',               dto, false),
  registerCustomer : dto => req('POST', '/api/auth/register/customer',   dto, false),
  registerVendor   : dto => req('POST', '/api/auth/register/vendor',     dto, false),

  /* Products (public) */
  searchProducts : (search, page=0, size=12) =>
    req('GET', `/api/products?search=${encodeURIComponent(search)}&page=${page}&size=${size}`),
  getProductById : id => req('GET', `/api/products/${id}`),
  searchProductsFiltered: (params={}, page=0, size=12) =>
    req('GET', `/api/products/search/customer${q({...params, page, size})}`),

  /* Catalog — public, no auth required */
  getBrands          : (page=0, size=200) => req('GET', `/api/brands/?page=${page}&size=${size}`, null, false),
  getBrandById       : id                 => req('GET', `/api/brands/${id}`, null, false),
  getCategories      : (page=0, size=200) => req('GET', `/api/categories/?page=${page}&size=${size}`, null, false),
  getCategorySubcats : (catId, page=0, size=200) => req('GET', `/api/categories/${catId}/subcategories?page=${page}&size=${size}`, null, false),

  /* Customer */
  myProfile      : ()    => req('GET',   '/api/customers/my-profile'),
  updateProfile  : dto   => req('PATCH', '/api/customers/my-profile', dto),
  addToCart      : dto   => req('POST',  '/api/carts', dto),
  viewCart       : ()    => req('GET',   '/api/carts'),
  checkout       : dto   => req('POST',  '/api/checkouts', dto),
  getInvoice     : id    => req('GET',   `/api/invoices/${id}`),
  getOrders      : (page=0, size=8) => req('GET', `/api/orders?page=${page}&size=${size}`),

  /* Vendor profile */
  vendorProfile       : ()   => req('GET',            '/api/vendors/my-profile'),
  updateVendorProfile : fd   => multipartReq('PATCH', '/api/vendors/my-profile/update', fd),

  /* Vendor shop */
  registerShop    : dto  => req('POST',  '/api/shops', dto),
  updateShop      : dto  => req('PATCH', '/api/shops', dto),
  viewShop        : (page=0,size=12) => req('GET', `/api/shops?page=${page}&size=${size}`),
  activateShop    : ()   => req('PATCH', '/api/shops/activate'),
  deactivateShop  : ()   => req('PATCH', '/api/shops/deactivate'),

  /* Vendor product requests */
  submitProductRequest : fd   => multipartReq('POST', '/api/product-requests', fd),

  /* Vendor orders */
  searchVendorOrders   : (params={}, page=0, size=8) => req('GET', `/api/sub-orders/search${q({...params, page, size})}`),
  updateSubOrderStatus : (id, newStatus)             => req('PATCH', `/api/sub-orders/${id}`, { newStatus }),

  /* Vendor products */
  vendorAddProduct      : fd        => multipartReq('POST',  '/api/stocks/add-to-shop', fd),
  vendorEditStock       : (id, qty) => req('PATCH', `/api/stocks/edit-product-stock/${id}?quantity=${qty}`),
  vendorUpdateProduct   : (id, fd)  => multipartReq('PATCH', `/api/products/vendor/${id}`, fd),
  vendorGetMyProducts   : (page=0, size=12) => req('GET', `/api/products/vendor?page=${page}&size=${size}`),

  /* Vendor product models — requires ROLE_SELLER */
  searchProductModels: (params={}, page=0, size=12) => req('GET', `/api/product-models/search/vendor${q({...params,page,size})}`),

  /* Admin – customers */
  adminGetCustomers    : (page=0,size=10) => req('GET', `/api/customers/customers?page=${page}&size=${size}`),
  adminSearchCustomers : (p,page=0,size=10) => req('GET', `/api/customers/customers/search${q({...p,page,size})}`),
  adminModifyCustStatus: (id,dto)  => req('PATCH', `/api/customers/customers/modify-status/${id}`, dto),

  /* Admin – vendors */
  adminGetVendors      : (page=0,size=10) => req('GET', `/api/vendors?page=${page}&size=${size}`),
  adminSearchVendors   : (p,page=0,size=10) => req('GET', `/api/vendors/search${q({...p,page,size})}`),
  adminModifyVendStatus: (id,dto)  => req('PATCH', `/api/vendors/modify-status/${id}`, dto),

  /* Admin – categories (write only; reads go to public /api/categories) */
  adminAddCategory     : fd   => multipartReq('POST',  '/api/categories', fd),
  adminUpdateCategory  : (id,fd) => multipartReq('PATCH', `/api/categories/${id}`, fd),
  adminDeleteCategory  : id   => req('PATCH', `/api/categories/delete/${id}`),

  /* Admin – subcategories (write only; reads go to public /api/categories/{id}/subcategories) */
  adminAddSubcat       : (catId,fd)       => multipartReq('POST',  `/api/categories/${catId}/subcategories`, fd),
  adminUpdateSubcat    : (catId,subId,fd) => multipartReq('PATCH', `/api/categories/${catId}/subcategories/${subId}`, fd),
  adminDeleteSubcat    : (catId,subId)    => req('PATCH', `/api/categories/${catId}/subcategories/delete/${subId}`),

  /* Admin – brands (write only; reads go to public /api/brands) */
  adminAddBrand        : fd      => multipartReq('POST',  '/api/brands', fd),
  adminUpdateBrand     : (id,fd) => multipartReq('PATCH', `/api/brands/${id}`, fd),
  adminDeleteBrand     : id      => req('PATCH', `/api/brands/delete/${id}`),

  /* Admin – product models */
  adminGetProductModels   : (page=0,size=10) => req('GET', `/api/product-models/search?page=${page}&size=${size}`),
  adminSearchProductModels: (p,page=0,size=10) => req('GET', `/api/product-models/search${q({...p,page,size})}`),
  adminAddProductModel    : fd      => multipartReq('POST',  '/api/product-models', fd),
  adminUpdateProductModel : (id,fd) => multipartReq('PATCH', `/api/product-models/${id}`, fd),
  adminDeleteProductModel : id      => req('PATCH', `/api/product-models/delete/${id}`),

  /* Lookup by id — now served by public catalog controllers */
  adminGetCategories : (page=0,size=10) => req('GET', `/api/categories/?page=${page}&size=${size}`, null, false),
  adminGetSubcats    : (catId,page=0,size=10) => req('GET', `/api/categories/${catId}/subcategories?page=${page}&size=${size}`, null, false),
  adminGetBrands     : (page=0,size=10) => req('GET', `/api/brands/?page=${page}&size=${size}`, null, false),
  adminGetBrandById  : id => req('GET', `/api/brands/${id}`, null, false),
  adminGetSubcatById : id => req('GET', `/api/categories/0/subcategories/${id}`, null, false),

  /* Admin – product requests */
  adminGetProductRequests  : (page=0,size=10) => req('GET',   `/api/product-requests?page=${page}&size=${size}`),
  adminRejectRequest       : id              => req('PATCH', `/api/product-requests/reject/${id}`),
  adminProcessRequest      : (id,fd)         => multipartReq('POST', `/api/product-requests/process/${id}`, fd),
};
