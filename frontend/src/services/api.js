const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

async function apiFetch(path, options = {}) {
  const headers = {
    Accept: "application/json",
    ...(options.body instanceof FormData ? {} : { "Content-Type": "application/json" }),
    ...options.headers,
  };

  const token = localStorage.getItem("vnet_token");
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers,
    ...options,
  });

  if (!response.ok) {
    let message = "";
    try {
      const data = await response.json();
      message = data.error || data.message || "";
    } catch (e) {
      // ignore
    }

    if (!message) {
      try {
        message = await response.text();
      } catch (e) {
        // ignore
      }
    }

    if (response.status === 401) {
      localStorage.removeItem("vnet_token");
      localStorage.removeItem("vnet_user");
      if (window.location.pathname !== "/login" && window.location.pathname !== "/signup") {
        window.location.href = "/login";
      }
    }
    throw new Error(message || `API request failed: ${response.status}`);
  }

  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch (e) {
    return text;
  }
}

// Authentication API
export function getCaptcha() {
  return apiFetch("/auth/captcha");
}

export function registerInit(payload) {
  return apiFetch("/auth/register-init", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function registerConfirm(payload) {
  return apiFetch("/auth/register-confirm", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function resendOtp(sessionId) {
  return apiFetch("/auth/resend-otp", {
    method: "POST",
    body: JSON.stringify({ sessionId }),
  });
}

export function loginInit(payload) {
  return apiFetch("/auth/login-init", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function loginConfirm(payload) {
  return apiFetch("/auth/login-confirm", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function getDashboardSummary() {
  return apiFetch("/dashboard/summary");
}

export function getDashboardOverview(month = "") {
  const query = month ? `?month=${encodeURIComponent(month)}` : "";
  return apiFetch(`/dashboard/overview${query}`);
}

export function getCustomers() {
  return apiFetch("/customers");
}

export function getCustomersPage(page = 0, size = 10, search = "") {
  const query = new URLSearchParams({ page, size });
  if (search) query.append("search", search);
  return apiFetch(`/customers/page?${query.toString()}`);
}

export function getCustomerDetail(custId) {
  return apiFetch(`/customers/${encodeURIComponent(custId)}/detail`);
}

export function getAgentPerformance() {
  return apiFetch("/agents/performance");
}

export function getAgentPerformanceByPeriod(period = "month") {
  return apiFetch(`/agents/performance?period=${encodeURIComponent(period)}`);
}

export function getAgentCustomers(agentId) {
  return apiFetch(`/agents/${encodeURIComponent(agentId)}/customers`);
}

export function getAddressInsights() {
  return apiFetch("/addresses/insights");
}

export function getImportStats() {
  return apiFetch("/import/stats");
}

export function getPackages() {
  return apiFetch("/packages");
}

export function savePackage(pkg) {
  const method = pkg.id ? "PUT" : "POST";
  const path = pkg.id ? `/packages/${pkg.id}` : "/packages";
  return apiFetch(path, {
    method,
    body: JSON.stringify(pkg),
  });
}

export function deletePackage(id) {
  return apiFetch(`/packages/${id}`, {
    method: "DELETE",
  });
}

export function previewImport(file) {
  const formData = new FormData();
  formData.append("file", file);

  return apiFetch("/import/preview", {
    method: "POST",
    body: formData,
  });
}

export function confirmImport(payload) {
  return apiFetch("/import/confirm", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function downloadImportTemplate() {
  const token = localStorage.getItem("vnet_token");
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";
  const headers = {};
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  
  const response = await fetch(`${API_BASE_URL}/import/template`, {
    headers,
  });
  
  if (!response.ok) {
    throw new Error(`Failed to download template: ${response.status}`);
  }
  
  return response.blob();
}

export function createCustomer(customer) {
  return apiFetch("/customers", {
    method: "POST",
    body: JSON.stringify(customer),
  });
}

export function updateCustomerStatus(id, status) {
  return apiFetch(`/customers/${id}/status?status=${status}`, {
    method: "PUT",
  });
}

export function deleteCustomer(id) {
  return apiFetch(`/customers/${id}`, {
    method: "DELETE",
  });
}

export function createAgent(agent) {
  return apiFetch("/agents", {
    method: "POST",
    body: JSON.stringify(agent),
  });
}

export function updateAgent(id, agent) {
  return apiFetch(`/agents/${id}`, {
    method: "PUT",
    body: JSON.stringify(agent),
  });
}

export function updateAgentStatus(id, status) {
  return apiFetch(`/agents/${id}/status?status=${status}`, {
    method: "PUT",
  });
}

export function deleteAgentRecord(id) {
  return apiFetch(`/agents/${id}`, {
    method: "DELETE",
  });
}

export function updateProfile(payload) {
  return apiFetch("/users/profile", {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function changePassword(payload) {
  return apiFetch("/users/change-password", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function getSystemConfig() {
  return apiFetch("/system-config");
}

export function saveSystemConfig(payload) {
  return apiFetch("/system-config", {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function getActiveSessions() {
  return apiFetch("/users/sessions");
}

export function revokeSession(id) {
  return apiFetch(`/users/sessions/${id}`, {
    method: "DELETE",
  });
}

export function logout() {
  return apiFetch("/auth/logout", {
    method: "POST",
  });
}

export function getUsers() {
  return apiFetch("/users");
}

export function createUser(payload) {
  return apiFetch("/users", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function deleteUser(id) {
  return apiFetch(`/users/${id}`, {
    method: "DELETE",
  });
}

export function getAgentsList() {
  return apiFetch("/agents");
}

