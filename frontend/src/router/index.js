import { createRouter, createWebHistory } from "vue-router";
import DashboardView from "@/views/DashboardView.vue";
import LoginView from "@/views/LoginView.vue";
import SignupView from "@/views/SignupView.vue";

const routes = [
  { path: "/", redirect: "/dashboard" },
  {
    path: "/login",
    name: "login",
    component: LoginView,
    meta: { hideLayout: true, guestOnly: true }
  },
  {
    path: "/signup",
    name: "signup",
    component: SignupView,
    meta: { hideLayout: true, guestOnly: true }
  },
  { 
    path: "/dashboard", 
    name: "dashboard", 
    component: DashboardView,
    meta: { requiresAuth: true }
  },
  {
    path: "/packages",
    name: "packages",
    component: () => import("@/views/PackageView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/data-import",
    name: "data-import",
    component: () => import("@/views/DataImportView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/data-preview",
    name: "data-preview",
    component: () => import("@/views/DataPreviewView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/customers",
    name: "customers",
    component: () => import("@/views/CustomersView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/agent-performance",
    name: "agent-performance",
    component: () => import("@/views/AgentPerformanceView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/address-insights",
    name: "address-insights",
    component: () => import("@/views/AddressInsightsView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/settings",
    name: "settings",
    component: () => import("@/views/SettingsView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/support",
    name: "support",
    component: () => import("@/views/SupportView.vue"),
    meta: { requiresAuth: true }
  },
  // Fallback to login or dashboard
  { path: "/:pathMatch(.*)*", redirect: "/dashboard" }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("vnet_token");
  
  if (to.meta.requiresAuth && !token) {
    next("/login");
  } else if (to.meta.guestOnly && token) {
    next("/dashboard");
  } else {
    next();
  }
});

export default router;
