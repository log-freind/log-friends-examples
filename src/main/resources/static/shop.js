const contextPath = window.location.pathname.startsWith("/examples") ? "/examples" : "";

const fallbackProductImages = {
  "PRD-SNK-001": "/assets/product-sneakers.svg",
  "PRD-BAG-014": "/assets/product-backpack.svg",
  "PRD-ELC-042": "/assets/product-earbuds.svg"
};

const maxCompareItems = 3;

const state = {
  products: [],
  cart: [],
  selectedProductId: null,
  activeProductId: null,
  wishedProductIds: new Set(),
  comparedProductIds: new Set(),
  wishlistBusyProductIds: new Set(),
  coupon: null,
  orderId: null,
  orderTotal: 0,
  transactionId: null,
  shipmentId: null,
  shipmentStatus: null
};

const els = {};

document.addEventListener("DOMContentLoaded", () => {
  [
    "productGrid",
    "productTemplate",
    "searchQuery",
    "categoryFilter",
    "stockFilter",
    "minPriceFilter",
    "maxPriceFilter",
    "refreshProducts",
    "catalogSummary",
    "productDetail",
    "selectedProductName",
    "selectedWish",
    "selectedCompare",
    "selectedAdd",
    "wishCount",
    "compareCount",
    "compareTray",
    "cartList",
    "cartCount",
    "cartTotal",
    "selectedItemLabel",
    "createOrder",
    "processPayment",
    "createShipment",
    "markInTransit",
    "markDelivered",
    "customerEmail",
    "paymentMethod",
    "deliveryMethod",
    "shippingZipCode",
    "couponCode",
    "validateCoupon",
    "selectedSubtotal",
    "couponDiscount",
    "payableAmount",
    "couponMessage",
    "userId",
    "cartId",
    "wishlistId",
    "lastAction",
    "orderId",
    "transactionId",
    "shipmentId",
    "responseLog",
    "statusCart",
    "statusOrder",
    "statusPayment",
    "statusShipment",
    "cartStatusText",
    "orderStatusText",
    "paymentStatusText",
    "shipmentStatusText"
  ].forEach((id) => {
    els[id] = document.getElementById(id);
  });

  const debouncedLoadProducts = debounce(loadProducts, 280);

  els.refreshProducts.addEventListener("click", loadProducts);
  els.searchQuery.addEventListener("input", debouncedLoadProducts);
  els.categoryFilter.addEventListener("change", loadProducts);
  els.stockFilter.addEventListener("change", loadProducts);
  els.minPriceFilter.addEventListener("input", debouncedLoadProducts);
  els.maxPriceFilter.addEventListener("input", debouncedLoadProducts);
  els.selectedWish.addEventListener("click", () => toggleWishlist(selectedProduct()));
  els.selectedCompare.addEventListener("click", () => toggleCompare(selectedProduct()));
  els.selectedAdd.addEventListener("click", () => {
    const product = selectedProduct();
    if (product) {
      addToCart(product, 1);
    }
  });
  els.createOrder.addEventListener("click", createOrder);
  els.processPayment.addEventListener("click", processPayment);
  els.createShipment.addEventListener("click", createShipment);
  els.markInTransit.addEventListener("click", () => changeShipmentStatus("IN_TRANSIT"));
  els.markDelivered.addEventListener("click", () => changeShipmentStatus("DELIVERED"));
  els.validateCoupon.addEventListener("click", validateCoupon);
  els.couponCode.addEventListener("input", () => {
    state.coupon = null;
    renderFlow();
  });
  els.couponCode.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      event.preventDefault();
      validateCoupon();
    }
  });

  renderCart();
  renderProductDetail();
  renderFlow();
  loadProducts();
});

async function loadProducts() {
  const params = new URLSearchParams();
  const query = cleanInput(els.searchQuery.value);
  let minPrice = normalizeOptionalPrice(els.minPriceFilter.value);
  let maxPrice = normalizeOptionalPrice(els.maxPriceFilter.value);

  if (minPrice !== null && maxPrice !== null && minPrice > maxPrice) {
    [minPrice, maxPrice] = [maxPrice, minPrice];
  }

  if (query) {
    params.set("q", query);
  }
  if (els.categoryFilter.value) {
    params.set("category", els.categoryFilter.value);
  }
  if (els.stockFilter.value) {
    params.set("stockStatus", els.stockFilter.value);
  }
  if (minPrice !== null) {
    params.set("minPrice", String(minPrice));
  }
  if (maxPrice !== null) {
    params.set("maxPrice", String(maxPrice));
  }

  const path = params.toString() ? `/products?${params.toString()}` : "/products";
  setBusy(els.refreshProducts, true);
  writeLog("GET " + path);

  try {
    const data = await api(path);
    state.products = Array.isArray(data)
      ? data.map(normalizeProduct).filter(Boolean)
      : [];

    if (state.selectedProductId && !findProduct(state.selectedProductId)) {
      state.selectedProductId = state.products[0]?.productId || null;
    }
    if (!state.selectedProductId && state.products.length) {
      state.selectedProductId = state.products[0].productId;
    }

    renderProducts();
    renderCatalogSummary(path);
    renderProductDetail();
    renderCompareTray();
    renderFlow();
    writeLog("GET " + path, state.products);
  } catch (error) {
    renderProducts();
    renderCatalogSummary(path, true);
    renderProductDetail();
    writeError("GET " + path, error);
  } finally {
    setBusy(els.refreshProducts, false);
  }
}

async function loadProductDetail(productId) {
  if (!productId) {
    return;
  }

  const path = `/products/${encodeURIComponent(productId)}`;
  writeLog("GET " + path);

  try {
    const product = normalizeProduct(await api(path));
    if (product) {
      mergeProduct(product);
      state.selectedProductId = product.productId;
      renderProducts();
      renderProductDetail();
      renderFlow();
      writeLog("GET " + path, product);
    }
  } catch (error) {
    writeError("GET " + path, error);
  }
}

function renderProducts() {
  els.productGrid.replaceChildren();

  if (!state.products.length) {
    const empty = document.createElement("div");
    empty.className = "empty-state";
    empty.textContent = "No products found.";
    els.productGrid.append(empty);
    return;
  }

  state.products.forEach((product) => {
    const node = els.productTemplate.content.firstElementChild.cloneNode(true);
    const image = node.querySelector(".product-image");
    const quantity = node.querySelector(".quantity-input");
    const addButton = node.querySelector(".add-button");
    const detailButton = node.querySelector(".detail-button");
    const wishButton = node.querySelector(".wish-button");
    const compareButton = node.querySelector(".compare-button");
    const stockBadge = node.querySelector(".stock-badge");
    const remaining = remainingQuantity(product);
    const isSoldOut = product.stockStatus === "SOLD_OUT" || remaining <= 0;

    node.dataset.productId = product.productId;
    node.classList.toggle("selected", product.productId === state.selectedProductId);
    node.setAttribute("aria-label", `${product.name}, ${formatMoney(product.price)}, ${formatStockWithQuantity(product)}`);

    image.addEventListener("error", () => {
      image.src = withContextPath("/assets/product-fallback.svg");
    }, { once: true });
    image.src = withContextPath(product.imageUrl || fallbackProductImages[product.productId] || "/assets/product-fallback.svg");
    image.alt = product.name;

    node.querySelector(".product-category").textContent = product.category;
    stockBadge.textContent = formatStockWithQuantity(product);
    stockBadge.classList.add(stockClass(product.stockStatus));
    node.querySelector(".product-name").textContent = product.name;
    node.querySelector(".product-brand").textContent = product.brand;
    node.querySelector(".product-rating").textContent = formatRating(product.rating);
    node.querySelector(".product-description").textContent = product.description;
    node.querySelector(".product-price").textContent = formatMoney(product.price);

    setWishlistControl(wishButton, product);
    setCompareControl(compareButton, product);

    wishButton.addEventListener("click", (event) => {
      event.stopPropagation();
      toggleWishlist(product);
    });
    compareButton.addEventListener("click", (event) => {
      event.stopPropagation();
      toggleCompare(product);
    });
    detailButton.addEventListener("click", (event) => {
      event.stopPropagation();
      selectProduct(product.productId, true);
    });

    quantity.max = String(Math.max(1, Math.min(9, remaining)));

    if (isSoldOut) {
      quantity.disabled = true;
      addButton.disabled = true;
      addButton.lastElementChild.textContent = "Sold out";
      addButton.setAttribute("aria-label", `${product.name} is sold out`);
    } else {
      addButton.setAttribute("aria-label", `Add ${product.name} to cart`);
      addButton.addEventListener("click", (event) => {
        event.stopPropagation();
        addToCart(product, normalizeQuantity(quantity.value));
      });
    }
    detailButton.setAttribute("aria-label", `View details for ${product.name}`);

    node.addEventListener("click", (event) => {
      if (event.target.closest("button, input, label")) {
        return;
      }
      selectProduct(product.productId, false);
    });

    els.productGrid.append(node);
  });
}

function renderCatalogSummary(path, failed = false) {
  if (failed) {
    els.catalogSummary.textContent = "Product API request failed";
    return;
  }

  const count = state.products.length;
  const categories = new Set(state.products.map((product) => product.category).filter(Boolean));
  els.catalogSummary.textContent =
    `${count.toLocaleString()} products · ${categories.size.toLocaleString()} categories · ${path}`;
}

function renderProductDetail() {
  const product = selectedProduct();
  els.productDetail.replaceChildren();
  els.selectedProductName.textContent = product ? product.name : "Choose a product";

  setWishlistControl(els.selectedWish, product);
  setCompareControl(els.selectedCompare, product);
  els.selectedAdd.disabled = !product || product.stockStatus === "SOLD_OUT" || remainingQuantity(product) <= 0;

  if (!product) {
    const empty = document.createElement("div");
    empty.className = "detail-empty";
    empty.textContent = "Select a product to see price, stock, and description.";
    els.productDetail.append(empty);
    renderCompareTray();
    return;
  }

  const summary = document.createElement("div");
  summary.className = "detail-summary";

  const image = document.createElement("img");
  image.alt = product.name;
  image.src = withContextPath(product.imageUrl || fallbackProductImages[product.productId] || "/assets/product-fallback.svg");
  image.addEventListener("error", () => {
    image.src = withContextPath("/assets/product-fallback.svg");
  }, { once: true });

  const copy = document.createElement("div");
  copy.className = "detail-copy";

  const priceRow = document.createElement("div");
  priceRow.className = "detail-price-row";
  const price = document.createElement("strong");
  price.textContent = formatMoney(product.price);
  const badge = document.createElement("span");
  badge.className = `stock-badge ${stockClass(product.stockStatus)}`;
  badge.textContent = formatStockWithQuantity(product);
  priceRow.append(price, badge);

  const name = document.createElement("h2");
  name.textContent = product.name;
  const description = document.createElement("p");
  description.textContent = product.description;

  copy.append(priceRow, name, description);
  summary.append(image, copy);

  const meta = document.createElement("dl");
  meta.className = "detail-meta-list";
  meta.append(
    detailMeta("Product ID", product.productId),
    detailMeta("Brand", product.brand),
    detailMeta("Category", product.category),
    detailMeta("Rating", formatRating(product.rating)),
    detailMeta("Available", String(remainingQuantity(product)))
  );

  els.productDetail.append(summary, meta);
  renderCompareTray();
}

function detailMeta(label, value) {
  const wrapper = document.createElement("div");
  const dt = document.createElement("dt");
  const dd = document.createElement("dd");
  dt.textContent = label;
  dd.textContent = value;
  wrapper.append(dt, dd);
  return wrapper;
}

function selectProduct(productId, fetchDetail) {
  if (!productId) {
    return;
  }

  state.selectedProductId = productId;
  renderProducts();
  renderProductDetail();
  renderFlow();

  if (fetchDetail) {
    loadProductDetail(productId);
  }
}

async function toggleWishlist(product) {
  if (!product || state.wishlistBusyProductIds.has(product.productId)) {
    return;
  }

  if (state.wishedProductIds.has(product.productId)) {
    await removeFromWishlist(product);
  } else {
    await addToWishlist(product);
  }
}

async function addToWishlist(product) {
  const wishlistId = cleanInput(els.wishlistId.value, "wishlist-demo-001");
  const payload = {
    userId: cleanInput(els.userId.value, "demo-user-001"),
    productId: product.productId,
    sourcePage: "shop"
  };
  const path = `/wishlists/${encodeURIComponent(wishlistId)}/items`;

  state.wishlistBusyProductIds.add(product.productId);
  renderProducts();
  renderProductDetail();
  writeLog("POST " + path, payload);

  try {
    await api(path, {
      method: "POST",
      body: payload
    });
    state.wishedProductIds.add(product.productId);
    writeLog("POST " + path, { productId: product.productId, wished: true });
  } catch (error) {
    writeError("POST " + path, error);
  } finally {
    state.wishlistBusyProductIds.delete(product.productId);
    renderProducts();
    renderProductDetail();
  }
}

async function removeFromWishlist(product) {
  const wishlistId = cleanInput(els.wishlistId.value, "wishlist-demo-001");
  const params = new URLSearchParams({
    userId: cleanInput(els.userId.value, "demo-user-001"),
    sourcePage: "shop"
  });
  const path = `/wishlists/${encodeURIComponent(wishlistId)}/items/${encodeURIComponent(product.productId)}?${params.toString()}`;

  state.wishlistBusyProductIds.add(product.productId);
  renderProducts();
  renderProductDetail();
  writeLog("DELETE " + path);

  try {
    await api(path, { method: "DELETE" });
    state.wishedProductIds.delete(product.productId);
    writeLog("DELETE " + path, { productId: product.productId, wished: false });
  } catch (error) {
    writeError("DELETE " + path, error);
  } finally {
    state.wishlistBusyProductIds.delete(product.productId);
    renderProducts();
    renderProductDetail();
  }
}

function toggleCompare(product) {
  if (!product) {
    return;
  }

  if (state.comparedProductIds.has(product.productId)) {
    state.comparedProductIds.delete(product.productId);
  } else if (state.comparedProductIds.size >= maxCompareItems) {
    writeLog("Compare limit", {
      limit: maxCompareItems,
      reason: "Remove one product before comparing another."
    });
  } else {
    state.comparedProductIds.add(product.productId);
  }

  renderProducts();
  renderProductDetail();
}

function renderCompareTray() {
  els.wishCount.textContent = plural(state.wishedProductIds.size, "wished");
  els.compareCount.textContent = plural(state.comparedProductIds.size, "comparing");
  els.compareTray.replaceChildren();

  state.comparedProductIds.forEach((productId) => {
    const product = findProduct(productId);
    const chip = document.createElement("span");
    chip.className = "compare-chip";
    chip.textContent = product?.name || productId;

    const remove = document.createElement("button");
    remove.type = "button";
    remove.setAttribute("aria-label", "Remove compare item");
    remove.textContent = "x";
    remove.addEventListener("click", () => {
      state.comparedProductIds.delete(productId);
      renderProducts();
      renderProductDetail();
    });

    chip.append(remove);
    els.compareTray.append(chip);
  });
}

async function addToCart(product, quantity) {
  const cartId = cleanInput(els.cartId.value, "cart-demo-001");
  const existingQuantity = cartItemQuantity(product.productId);
  const remaining = product.stockQuantity - existingQuantity;

  if (remaining <= 0 || quantity > remaining) {
    writeError(
      `POST /carts/${cartId}/items`,
      new Error(`Only ${Math.max(0, remaining)} items available for ${product.name}.`)
    );
    return;
  }

  const payload = {
    userId: cleanInput(els.userId.value, "demo-user-001"),
    productId: product.productId,
    quantity,
    unitPrice: product.price,
    sourcePage: "shop"
  };

  writeLog(`POST /carts/${cartId}/items`, payload);

  try {
    const item = normalizeCartItem(
      await api(`/carts/${encodeURIComponent(cartId)}/items`, {
        method: "POST",
        body: payload
      }),
      product,
      quantity
    );
    upsertCartItem(item);
    state.activeProductId = item.productId;
    state.selectedProductId = item.productId;
    resetFlowAfterCartChange();
    renderProducts();
    renderProductDetail();
    renderCart();
    renderFlow();
    writeLog(`POST /carts/${cartId}/items`, item);
  } catch (error) {
    writeError(`POST /carts/${cartId}/items`, error);
  }
}

async function removeCartItem(productId) {
  const cartId = cleanInput(els.cartId.value, "cart-demo-001");
  const params = new URLSearchParams({
    userId: cleanInput(els.userId.value, "demo-user-001"),
    sourcePage: "shop"
  });
  const path = `/carts/${encodeURIComponent(cartId)}/items/${encodeURIComponent(productId)}?${params.toString()}`;

  writeLog("DELETE " + path);

  try {
    await api(path, { method: "DELETE" });
    state.cart = state.cart.filter((item) => item.productId !== productId);
    if (state.activeProductId === productId) {
      state.activeProductId = state.cart[0]?.productId || null;
    }
    resetFlowAfterCartChange();
    renderCart();
    renderFlow();
    writeLog("DELETE " + path, { productId, removed: true });
  } catch (error) {
    writeError("DELETE " + path, error);
  }
}

function upsertCartItem(item) {
  const existing = state.cart.find((entry) => entry.productId === item.productId);
  if (!existing) {
    state.cart.push(item);
    return;
  }

  existing.quantity += item.quantity;
  existing.unitPrice = item.unitPrice;
  existing.lineTotal = existing.quantity * existing.unitPrice;
  existing.sourcePage = item.sourcePage;
  existing.productName = item.productName;
  existing.stockStatus = item.stockStatus;
  existing.availableQuantity = item.availableQuantity;
}

function renderCart() {
  els.cartList.replaceChildren();

  if (!state.cart.length) {
    const empty = document.createElement("div");
    empty.className = "empty-state";
    empty.textContent = "Cart is empty.";
    els.cartList.append(empty);
  } else {
    state.cart.forEach((item) => {
      const product = findProduct(item.productId);
      const row = document.createElement("div");
      row.className = "cart-item";
      row.tabIndex = 0;
      row.setAttribute("role", "button");
      row.setAttribute("aria-label", `Select ${product?.name || item.productName || item.productId}`);
      if (item.productId === state.activeProductId) {
        row.classList.add("active");
      }

      const name = document.createElement("strong");
      name.textContent = product?.name || item.productName || item.productId;
      const amount = document.createElement("span");
      amount.textContent = `${item.quantity} x ${formatMoney(item.unitPrice)}`;
      const code = document.createElement("small");
      code.textContent = `${item.productId} / ${formatMoney(item.lineTotal)}`;
      const remove = document.createElement("button");
      remove.type = "button";
      remove.className = "cart-remove";
      remove.setAttribute("aria-label", "Remove cart item");
      remove.textContent = "x";
      remove.addEventListener("click", (event) => {
        event.stopPropagation();
        removeCartItem(item.productId);
      });

      row.append(name, amount, code, remove);
      row.addEventListener("click", () => selectCartItem(item.productId));
      row.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
          selectCartItem(item.productId);
        }
      });
      els.cartList.append(row);
    });
  }

  const total = cartTotal();
  const count = cartCount();
  els.cartCount.textContent = count === 1 ? "1 item" : count + " items";
  els.cartTotal.textContent = formatMoney(total);
}

function selectCartItem(productId) {
  if (state.activeProductId !== productId) {
    resetFlowAfterCartChange();
  }
  state.activeProductId = productId;
  state.selectedProductId = productId;
  renderProducts();
  renderProductDetail();
  renderCart();
  renderFlow();

  if (!findProduct(productId)) {
    loadProductDetail(productId);
  }
}

async function validateCoupon() {
  const subtotal = cartTotal();
  if (subtotal <= 0) {
    return;
  }

  const couponCode = cleanInput(els.couponCode.value);
  if (!couponCode) {
    state.coupon = {
      couponCode: "",
      valid: false,
      discountAmount: 0,
      finalAmount: subtotal,
      reason: "Enter a coupon code."
    };
    renderFlow();
    return;
  }

  const payload = {
    userId: cleanInput(els.userId.value, "demo-user-001"),
    couponCode,
    orderTotal: subtotal
  };

  setBusy(els.validateCoupon, true);
  writeLog("POST /coupons/validate", payload);

  try {
    const result = normalizeCoupon(await api("/coupons/validate", {
      method: "POST",
      body: payload
    }), subtotal, couponCode);
    state.coupon = result;
    renderFlow();
    writeLog("POST /coupons/validate", result);
  } catch (error) {
    writeError("POST /coupons/validate", error);
  } finally {
    setBusy(els.validateCoupon, false);
    renderFlow();
  }
}

async function createOrder() {
  if (!state.cart.length) {
    return;
  }

  const items = orderLineItems();
  const orderTotal = payableAmount(cartTotal());
  const payload = {
    productId: items[0].productId,
    quantity: cartCount(),
    userId: cleanInput(els.userId.value, "demo-user-001"),
    customerEmail: cleanInput(els.customerEmail.value, "customer@example.com"),
    couponCode: state.coupon?.valid ? state.coupon.couponCode : null,
    channel: "WEB",
    orderTotal,
    deliveryMethod: els.deliveryMethod.value,
    shippingZipCode: cleanInput(els.shippingZipCode.value, "06236"),
    items
  };

  setBusy(els.createOrder, true);
  writeLog("POST /orders", payload);

  try {
    const orderId = await api("/orders", {
      method: "POST",
      body: payload
    });
    state.orderId = String(orderId);
    state.orderTotal = orderTotal;
    state.transactionId = null;
    state.shipmentId = null;
    state.shipmentStatus = null;
    renderFlow();
    writeLog("POST /orders", { orderId: state.orderId, orderTotal });
  } catch (error) {
    writeError("POST /orders", error);
  } finally {
    setBusy(els.createOrder, false);
    renderFlow();
  }
}

async function processPayment() {
  if (!state.orderId) {
    return;
  }

  const payload = {
    orderId: state.orderId,
    amount: state.orderTotal,
    method: els.paymentMethod.value
  };

  setBusy(els.processPayment, true);
  writeLog("POST /payments", payload);

  try {
    const transactionId = await api("/payments", {
      method: "POST",
      body: payload
    });
    state.transactionId = String(transactionId);
    state.shipmentId = null;
    state.shipmentStatus = null;
    renderFlow();
    writeLog("POST /payments", { transactionId: state.transactionId });
  } catch (error) {
    writeError("POST /payments", error);
  } finally {
    setBusy(els.processPayment, false);
    renderFlow();
  }
}

async function createShipment() {
  if (!state.orderId || !state.transactionId) {
    return;
  }

  const payload = {
    orderId: state.orderId,
    carrier: "CJ_LOGISTICS",
    trackingNumber: "TRK-" + Date.now(),
    shipmentStatus: "READY_TO_SHIP",
    warehouseCode: "WH-SEOUL-01"
  };

  setBusy(els.createShipment, true);
  writeLog("POST /shipments", payload);

  try {
    const shipmentId = await api("/shipments", {
      method: "POST",
      body: payload
    });
    state.shipmentId = String(shipmentId);
    state.shipmentStatus = "READY_TO_SHIP";
    renderFlow();
    writeLog("POST /shipments", { shipmentId: state.shipmentId, shipmentStatus: state.shipmentStatus });
  } catch (error) {
    writeError("POST /shipments", error);
  } finally {
    setBusy(els.createShipment, false);
    renderFlow();
  }
}

async function changeShipmentStatus(shipmentStatus) {
  if (!state.shipmentId) {
    return;
  }

  const payload = {
    shipmentStatus,
    warehouseCode: shipmentStatus === "DELIVERED" ? "CUSTOMER" : "HUB-SEOUL-02",
    carrier: "CJ_LOGISTICS"
  };
  const path = `/shipments/${encodeURIComponent(state.shipmentId)}/status`;
  const button = shipmentStatus === "DELIVERED" ? els.markDelivered : els.markInTransit;

  setBusy(button, true);
  writeLog("PUT " + path, payload);

  try {
    await api(path, {
      method: "PUT",
      body: payload
    });
    state.shipmentStatus = shipmentStatus;
    renderFlow();
    writeLog("PUT " + path, { shipmentId: state.shipmentId, shipmentStatus });
  } catch (error) {
    writeError("PUT " + path, error);
  } finally {
    setBusy(button, false);
    renderFlow();
  }
}

function renderFlow() {
  const hasCart = state.cart.length > 0;
  const subtotal = cartTotal();
  const discount = couponDiscount(subtotal);
  const payable = payableAmount(subtotal);

  els.selectedItemLabel.textContent = hasCart ? `${cartCount()} items in cart` : "No item";
  els.createOrder.disabled = !hasCart || Boolean(state.orderId);
  els.validateCoupon.disabled = !hasCart || Boolean(state.orderId);
  els.processPayment.disabled = !state.orderId || Boolean(state.transactionId);
  els.createShipment.disabled = !state.orderId || !state.transactionId || Boolean(state.shipmentId);
  els.markInTransit.disabled = !state.shipmentId || state.shipmentStatus !== "READY_TO_SHIP";
  els.markDelivered.disabled = !state.shipmentId || state.shipmentStatus !== "IN_TRANSIT";
  els.orderId.textContent = state.orderId || "-";
  els.transactionId.textContent = state.transactionId || "-";
  els.shipmentId.textContent = state.shipmentId || "-";
  els.selectedSubtotal.textContent = formatMoney(subtotal);
  els.couponDiscount.textContent = formatMoney(discount);
  els.payableAmount.textContent = formatMoney(payable);

  renderCouponMessage(hasCart);
  renderStatusSteps(hasCart);
}

function renderCouponMessage(hasCart) {
  els.couponMessage.classList.remove("valid", "invalid");

  if (!hasCart) {
    els.couponMessage.textContent = "Apply a coupon after selecting an item.";
    return;
  }

  if (!state.coupon) {
    els.couponMessage.textContent = "Coupon will be checked against the cart subtotal.";
    return;
  }

  if (state.coupon.valid) {
    els.couponMessage.classList.add("valid");
    els.couponMessage.textContent = `${state.coupon.couponCode} applied. Final amount is ${formatMoney(state.coupon.finalAmount)}.`;
    return;
  }

  els.couponMessage.classList.add("invalid");
  els.couponMessage.textContent = state.coupon.reason || "Coupon is not valid for this order.";
}

function renderStatusSteps(hasCart) {
  const hasOrder = Boolean(state.orderId);
  const hasPayment = Boolean(state.transactionId);
  const hasShipment = Boolean(state.shipmentId);
  const delivered = state.shipmentStatus === "DELIVERED";

  setStep(els.statusCart, hasCart ? "complete" : "active");
  setStep(els.statusOrder, hasOrder ? "complete" : hasCart ? "active" : "");
  setStep(els.statusPayment, hasPayment ? "complete" : hasOrder ? "active" : "");
  setStep(els.statusShipment, delivered ? "complete" : hasPayment ? "active" : "");

  els.cartStatusText.textContent = hasCart
    ? `${cartCount()} selected, ${formatMoney(cartTotal())}`
    : "Waiting for item";
  els.orderStatusText.textContent = hasOrder
    ? state.orderId
    : hasCart
      ? "Ready to create"
      : "Not created";
  els.paymentStatusText.textContent = hasPayment
    ? state.transactionId
    : hasOrder
      ? "Ready to pay"
      : "Not paid";
  els.shipmentStatusText.textContent = hasShipment
    ? formatShipmentStatus(state.shipmentStatus)
    : hasPayment
      ? "Ready to ship"
      : "Not started";
}

function setStep(element, status) {
  element.classList.remove("active", "complete");
  if (status) {
    element.classList.add(status);
  }
}

function resetFlowAfterCartChange() {
  state.coupon = null;
  state.orderId = null;
  state.orderTotal = 0;
  state.transactionId = null;
  state.shipmentId = null;
  state.shipmentStatus = null;
}

async function api(path, options = {}) {
  const init = {
    method: options.method || "GET",
    headers: {
      Accept: "application/json"
    }
  };

  if (options.body !== undefined) {
    init.headers["Content-Type"] = "application/json";
    init.body = JSON.stringify(options.body);
  }

  const response = await fetch(withContextPath(path), init);
  const text = await response.text();
  const data = parseBody(text);

  if (!response.ok) {
    const details = typeof data === "string" ? data : JSON.stringify(data);
    throw new Error(`${response.status} ${response.statusText}${details ? ": " + details : ""}`);
  }

  return data;
}

function withContextPath(path) {
  if (!path.startsWith("/")) {
    return path;
  }

  return `${contextPath}${path}`;
}

function parseBody(text) {
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function writeLog(action, payload) {
  els.lastAction.textContent = action;
  els.responseLog.textContent = payload === undefined
    ? action
    : JSON.stringify(payload, null, 2);
}

function writeError(action, error) {
  els.lastAction.textContent = "Error";
  els.responseLog.textContent = action + "\n" + error.message;
}

function setBusy(button, busy) {
  button.dataset.busy = busy ? "true" : "false";
  button.disabled = busy;
}

function selectedProduct() {
  if (state.selectedProductId) {
    return findProduct(state.selectedProductId);
  }

  return state.products[0] || null;
}

function activeItem() {
  if (!state.cart.length) {
    return null;
  }

  return state.cart.find((item) => item.productId === state.activeProductId) || state.cart[0];
}

function cartItemQuantity(productId) {
  return state.cart
    .filter((item) => item.productId === productId)
    .reduce((sum, item) => sum + item.quantity, 0);
}

function orderLineItems() {
  return state.cart.map((item) => ({
    productId: item.productId,
    quantity: item.quantity,
    unitPrice: item.unitPrice,
    lineTotal: item.lineTotal
  }));
}

function remainingQuantity(product) {
  if (!product) {
    return 0;
  }

  return Math.max(0, product.stockQuantity - cartItemQuantity(product.productId));
}

function findProduct(productId) {
  return state.products.find((product) => product.productId === productId);
}

function mergeProduct(product) {
  const index = state.products.findIndex((entry) => entry.productId === product.productId);
  if (index === -1) {
    state.products.push(product);
    return;
  }

  state.products[index] = product;
}

function normalizeProduct(raw) {
  if (!raw || typeof raw !== "object") {
    return null;
  }

  const productId = cleanInput(raw.productId);
  if (!productId) {
    return null;
  }

  return {
    productId,
    name: cleanInput(raw.name, productId),
    category: cleanInput(raw.category, "general"),
    price: normalizeMoneyValue(raw.price, 0),
    stockStatus: cleanInput(raw.stockStatus, "IN_STOCK").toUpperCase(),
    stockQuantity: normalizeStockQuantity(raw.stockQuantity, raw.stockStatus),
    brand: cleanInput(raw.brand, "Log Friends"),
    rating: normalizeRating(raw.rating),
    imageUrl: cleanInput(raw.imageUrl),
    description: cleanInput(raw.description, "No description available.")
  };
}

function normalizeCartItem(raw, product, fallbackQuantity) {
  const item = raw && typeof raw === "object" ? raw : {};
  const quantity = normalizeQuantity(item.quantity || fallbackQuantity);
  const unitPrice = normalizeMoneyValue(item.unitPrice, product.price);

  return {
    cartId: cleanInput(item.cartId, cleanInput(els.cartId.value, "cart-demo-001")),
    userId: cleanInput(item.userId, cleanInput(els.userId.value, "demo-user-001")),
    productId: cleanInput(item.productId, product.productId),
    productName: cleanInput(item.productName, product.name),
    quantity,
    unitPrice,
    lineTotal: normalizeMoneyValue(item.lineTotal, quantity * unitPrice),
    sourcePage: cleanInput(item.sourcePage, "shop"),
    stockStatus: cleanInput(item.stockStatus, product.stockStatus).toUpperCase(),
    availableQuantity: normalizeStockQuantity(item.availableQuantity, product.stockStatus)
  };
}

function normalizeCoupon(raw, orderTotal, couponCode) {
  const result = raw && typeof raw === "object" ? raw : {};
  const discountAmount = normalizeMoneyValue(result.discountAmount, 0);
  const valid = Boolean(result.valid);
  const finalAmount = valid
    ? normalizeMoneyValue(result.finalAmount, Math.max(0, orderTotal - discountAmount))
    : orderTotal;

  return {
    couponCode: cleanInput(result.couponCode, couponCode),
    valid,
    discountAmount: valid ? Math.min(orderTotal, discountAmount) : 0,
    finalAmount,
    reason: cleanInput(result.reason, valid ? "Coupon applied." : "Coupon is not valid.")
  };
}

function setWishlistControl(button, product) {
  const label = button.querySelector("span:last-child");
  const wished = product ? state.wishedProductIds.has(product.productId) : false;
  const busy = product ? state.wishlistBusyProductIds.has(product.productId) : false;

  button.disabled = !product || busy;
  button.setAttribute("aria-pressed", String(wished));
  button.setAttribute("aria-label", product
    ? `${wished ? "Remove" : "Add"} ${product.name} ${wished ? "from" : "to"} wishlist`
    : "Wishlist unavailable");
  button.title = wished ? "Remove from wishlist" : "Add to wishlist";
  if (label) {
    label.textContent = wished ? "Wished" : "Wish";
  }
}

function setCompareControl(button, product) {
  const label = button.querySelector("span:last-child");
  const compared = product ? state.comparedProductIds.has(product.productId) : false;

  button.disabled = !product;
  button.setAttribute("aria-pressed", String(compared));
  button.setAttribute("aria-label", product
    ? `${compared ? "Remove" : "Add"} ${product.name} ${compared ? "from" : "to"} compare`
    : "Compare unavailable");
  button.title = compared ? "Remove from compare" : "Compare product";
  if (label) {
    label.textContent = compared ? "Comparing" : "Compare";
  }
}

function couponDiscount(orderTotal) {
  if (!state.coupon?.valid) {
    return 0;
  }

  return Math.min(orderTotal, state.coupon.discountAmount);
}

function payableAmount(orderTotal) {
  if (!state.coupon?.valid) {
    return orderTotal;
  }

  return Math.max(0, state.coupon.finalAmount);
}

function cartTotal() {
  return state.cart.reduce((sum, item) => sum + item.lineTotal, 0);
}

function cartCount() {
  return state.cart.reduce((sum, item) => sum + item.quantity, 0);
}

function normalizeQuantity(value) {
  const number = Number.parseInt(value, 10);
  if (Number.isNaN(number)) {
    return 1;
  }
  return Math.min(9, Math.max(1, number));
}

function normalizeStockQuantity(value, stockStatus) {
  const quantity = Number.parseInt(value, 10);
  if (Number.isFinite(quantity)) {
    return Math.max(0, quantity);
  }

  return cleanInput(stockStatus, "IN_STOCK").toUpperCase() === "SOLD_OUT" ? 0 : 99;
}

function normalizeMoneyValue(value, fallback) {
  const number = Number(value);
  if (!Number.isFinite(number)) {
    return fallback;
  }
  return Math.max(0, Math.round(number));
}

function normalizeOptionalPrice(value) {
  if (cleanInput(value) === "") {
    return null;
  }

  const number = Number(value);
  if (!Number.isFinite(number)) {
    return null;
  }
  return Math.max(0, Math.round(number));
}

function normalizeRating(value) {
  const rating = Number(value);
  if (!Number.isFinite(rating)) {
    return 0;
  }
  return Math.min(5, Math.max(0, rating));
}

function cleanInput(value, fallback = "") {
  const next = String(value ?? "").trim();
  return next || fallback;
}

function formatMoney(value) {
  return new Intl.NumberFormat("ko-KR", {
    style: "currency",
    currency: "KRW",
    maximumFractionDigits: 0
  }).format(normalizeMoneyValue(value, 0));
}

function formatRating(value) {
  const rating = normalizeRating(value);
  return rating > 0 ? rating.toFixed(1) + " rating" : "No rating";
}

function formatStock(value) {
  return cleanInput(value, "IN_STOCK").toLowerCase().replaceAll("_", " ");
}

function formatStockWithQuantity(product) {
  if (!product || product.stockStatus === "SOLD_OUT") {
    return "sold out";
  }

  return `${formatStock(product.stockStatus)} · ${remainingQuantity(product)} left`;
}

function stockClass(value) {
  return formatStock(value).replaceAll(" ", "-");
}

function formatShipmentStatus(value) {
  return cleanInput(value, "Not started").toLowerCase().replaceAll("_", " ");
}

function plural(count, label) {
  return count === 1 ? `1 ${label}` : `${count} ${label}`;
}

function debounce(callback, delay) {
  let timeoutId = null;
  return () => {
    window.clearTimeout(timeoutId);
    timeoutId = window.setTimeout(callback, delay);
  };
}
