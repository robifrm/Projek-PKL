import { createRouter, createWebHistory } from "vue-router";
import DashboardView from "@/views/DashboardView.vue";
import LoginView from "@/views/LoginView.vue";

const routes = [
  { path: "/", redirect: "/dashboard" },
  {
    path: "/login",
    name: "login",
    component: LoginView,
    meta: { hideLayout: true, guestOnly: true },
  },
  {
    path: "/dashboard",
    name: "dashboard",
    component: DashboardView,
    meta: { requiresAuth: true },
  },
  {
    path: "/packages",
    name: "packages",
    component: () => import("@/views/PackageView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/data-import",
    name: "data-import",
    component: () => import("@/views/DataImportView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/data-preview",
    name: "data-preview",
    component: () => import("@/views/DataPreviewView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/customers",
    name: "customers",
    component: () => import("@/views/CustomersView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/agent-performance",
    name: "agent-performance",
    component: () => import("@/views/AgentPerformanceView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/address-insights",
    name: "address-insights",
    component: () => import("@/views/AddressInsightsView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/settings",
    name: "settings",
    component: () => import("@/views/SettingsView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/support",
    name: "support",
    component: () => import("@/views/SupportView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/register",
    name: "register",
    component: () => import("@/views/CustomerRegistrationFormView.vue"),
    meta: { hideLayout: true, guestOnly: false, requiresAuth: false },
  },
  {
    path: "/admin/registrations",
    name: "customer-registration",
    component: () => import("@/views/CustomerRegistrationView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/installation-schedule",
    name: "installation-schedule",
    component: () => import("@/views/InstallationScheduleView.vue"),
    meta: { requiresAuth: true },
  },
  // Fallback to login or dashboard
  { path: "/:pathMatch(.*)*", redirect: "/dashboard" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  if (to.path === "/register") {
    console.log("Bypassing all guards for public /register route");
    next();
    return;
  }

  const token = localStorage.getItem("vnet_token");
  const userStr = localStorage.getItem("vnet_user");
  console.log("Router Guard Target:", to.path, "requiresAuth:", to.meta.requiresAuth, "guestOnly:", to.meta.guestOnly, "hasToken:", !!token, "user:", userStr);

  if (to.meta.requiresAuth && !token) {
    console.log("Redirecting to /login: requires auth but no token");
    next("/login");
  } else if (to.meta.guestOnly && token) {
    console.log("Redirecting to /dashboard: guestOnly but has token");
    next("/dashboard");
  } else {
    if (token) {
      let role = null;
      if (userStr) {
        try {
          role = JSON.parse(userStr).role;
        } catch (e) {}
      }
      if (role === "AGENT") {
        const allowedRoutes = [
          "/dashboard",
          "/packages",
          "/customers",
          "/address-insights",
          "/settings",
          "/support",
          "/login",
          "/admin/registrations",
        ];
        if (to.meta.requiresAuth && !allowedRoutes.includes(to.path)) {
          console.log("AGENT redirected to /dashboard from:", to.path);
          next("/dashboard");
          return;
        }
      }
      if (role === "TECHNICIAN") {
        const allowedRoutes = [
          "/installation-schedule",
          "/settings",
          "/support",
          "/login",
        ];
        if (to.meta.requiresAuth && !allowedRoutes.includes(to.path)) {
          console.log("TECHNICIAN redirected to /installation-schedule from:", to.path);
          next("/installation-schedule");
          return;
        }
      }
    }
    console.log("Allowing route:", to.path);
    next();
  }
});

export default router;
