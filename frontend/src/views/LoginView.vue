<template>
  <div class="auth-root">
    <!-- Left Panel: Illustration + Brand -->
    <div class="auth-left">
      <!-- Top: Logo -->
      <div class="auth-brand">
        <img
          src="/brand/vnet-logo.png"
          alt="VNet"
          class="brand-img"
          @error="handleLogoError"
        />
      </div>

      <!-- Tagline under logo -->
      <p class="auth-tagline">ISP Management Platform</p>

      <!-- Center: Illustration -->
      <div class="auth-illo-wrap">
        <div class="illo-blob illo-blob--1"></div>
        <div class="illo-blob illo-blob--2"></div>

        <!-- Network SVG Illustration -->
        <svg
          class="illo-svg"
          viewBox="0 0 380 320"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <!-- Background shape -->
          <ellipse
            cx="190"
            cy="200"
            rx="160"
            ry="100"
            fill="rgba(56,189,248,0.07)"
          />
          <!-- Monitor / Card -->
          <rect
            x="70"
            y="60"
            width="240"
            height="155"
            rx="14"
            fill="white"
            stroke="#E2E8F0"
            stroke-width="1.5"
          />
          <rect x="70" y="60" width="240" height="30" rx="14" fill="#1A2170" />
          <rect x="82" y="60" width="240" height="15" rx="0" fill="#1A2170" />
          <!-- Dots on top bar -->
          <circle cx="90" cy="75" r="4" fill="rgba(255,255,255,0.35)" />
          <circle cx="105" cy="75" r="4" fill="rgba(255,255,255,0.35)" />
          <circle cx="120" cy="75" r="4" fill="rgba(255,255,255,0.35)" />
          <!-- Chart bars -->
          <rect x="100" y="130" width="20" height="55" rx="4" fill="#E2E8F0" />
          <rect
            x="130"
            y="115"
            width="20"
            height="70"
            rx="4"
            fill="#1A2170"
            opacity="0.8"
          />
          <rect x="160" y="140" width="20" height="45" rx="4" fill="#E2E8F0" />
          <rect x="190" y="105" width="20" height="80" rx="4" fill="#38BDF8" />
          <rect x="220" y="125" width="20" height="60" rx="4" fill="#E2E8F0" />
          <rect
            x="250"
            y="145"
            width="20"
            height="40"
            rx="4"
            fill="#F5A623"
            opacity="0.8"
          />
          <!-- Line graph -->
          <polyline
            points="100,170 130,150 160,162 190,135 220,145 250,158 280,140"
            stroke="#38BDF8"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            fill="none"
          />
          <circle cx="190" cy="135" r="4" fill="#38BDF8" />
          <!-- Stand -->
          <rect x="173" y="215" width="34" height="10" rx="3" fill="#E2E8F0" />
          <rect x="150" y="223" width="80" height="8" rx="4" fill="#CBD5E1" />

          <!-- WiFi signal -->
          <path
            d="M185 265 Q190 258 195 265"
            stroke="#1A2170"
            stroke-width="2"
            stroke-linecap="round"
            fill="none"
          />
          <path
            d="M180 271 Q190 260 200 271"
            stroke="#1A2170"
            stroke-width="2"
            stroke-linecap="round"
            fill="none"
            opacity="0.6"
          />
          <path
            d="M175 277 Q190 262 205 277"
            stroke="#1A2170"
            stroke-width="2"
            stroke-linecap="round"
            fill="none"
            opacity="0.3"
          />
          <circle cx="190" cy="263" r="2.5" fill="#1A2170" />
        </svg>
      </div>
    </div>

    <!-- Right Panel: Form -->
    <div class="auth-right">
      <!-- Top-right: Sign Up link disabled -->

      <div class="form-wrap">
        <!-- Form Header -->
        <div class="form-hdr">
          <h2 class="form-title">Selamat Datang!</h2>
          <p class="form-sub">Masuk ke sistem dashboard VNet Anda.</p>
        </div>

        <!-- Error Alert -->
        <Transition name="shake">
          <div v-if="errorAlert" class="alert-box alert--error">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>{{ errorAlert }}</span>
          </div>
        </Transition>

        <!-- Success Alert -->
        <Transition name="fade">
          <div v-if="successAlert" class="alert-box alert--success">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            <span>{{ successAlert }}</span>
          </div>
        </Transition>

        <!-- Login Form -->
        <form @submit.prevent="handleLoginInit" class="auth-form" novalidate>
          <!-- Username -->
          <div class="field-group">
            <label class="field-label">Username</label>
            <div
              class="field-wrap"
              :class="{ 'field-wrap--focus': focusedField === 'username' }"
            >
              <svg
                class="field-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <input
                id="login-username"
                type="text"
                v-model="form.username"
                class="field-input"
                placeholder="Masukkan username Anda"
                autocomplete="username"
                required
                @focus="focusedField = 'username'"
                @blur="focusedField = ''"
              />
            </div>
          </div>

          <!-- Password -->
          <div class="field-group">
            <label class="field-label">Password</label>
            <div
              class="field-wrap"
              :class="{ 'field-wrap--focus': focusedField === 'password' }"
            >
              <svg
                class="field-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
              <input
                id="login-password"
                :type="showPassword ? 'text' : 'password'"
                v-model="form.password"
                class="field-input"
                placeholder="Masukkan password Anda"
                autocomplete="current-password"
                required
                @focus="focusedField = 'password'"
                @blur="focusedField = ''"
              />
              <button
                type="button"
                class="toggle-pass"
                @click="showPassword = !showPassword"
                tabindex="-1"
              >
                <svg
                  v-if="!showPassword"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
                <svg
                  v-else
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path
                    d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"
                  />
                  <line x1="1" y1="1" x2="23" y2="23" />
                </svg>
              </button>
            </div>
          </div>

          <!-- Captcha -->
          <div class="field-group">
            <label class="field-label"> Verifikasi Keamanan </label>
            <div class="captcha-row">
              <div class="captcha-question-box" v-if="captcha">
                <span class="captcha-q">{{ captcha.question }}</span>
                <span class="captcha-eq">=</span>
                <button
                  type="button"
                  @click="loadCaptcha"
                  class="captcha-refresh"
                  title="Ganti soal"
                >
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path d="M23 4v6h-6M1 20v-6h6" />
                    <path
                      d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"
                    />
                  </svg>
                </button>
              </div>
              <div v-else class="captcha-skeleton">
                <div class="skel-bar"></div>
              </div>
              <div
                class="field-wrap captcha-ans-wrap"
                :class="{ 'field-wrap--focus': focusedField === 'captcha' }"
              >
                <input
                  type="text"
                  v-model="form.captchaAnswer"
                  class="field-input captcha-ans"
                  placeholder="Jawaban?"
                  required
                  @focus="focusedField = 'captcha'"
                  @blur="focusedField = ''"
                />
              </div>
            </div>
          </div>

          <!-- Submit -->
          <button
            type="submit"
            class="btn-submit"
            :disabled="loadingSubmit"
            id="login-submit-btn"
          >
            <span v-if="loadingSubmit" class="btn-loading">
              <span class="btn-spinner"></span>
              Menghubungkan...
            </span>
            <span v-else class="btn-label"> Masuk </span>
          </button>
        </form>

        <!-- Register Link disabled -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getCaptcha, loginInit } from "@/services/api";

const router = useRouter();

const loadingSubmit = ref(false);
const errorAlert = ref("");
const successAlert = ref("");
const captcha = ref(null);
const focusedField = ref("");
const showPassword = ref(false);

const form = ref({
  username: "",
  password: "",
  captchaId: "",
  captchaAnswer: "",
});

onMounted(() => {
  loadCaptcha();
});

const handleLogoError = (e) => {
  e.target.style.display = "none";
};

const loadCaptcha = async () => {
  captcha.value = null;
  form.value.captchaAnswer = "";
  try {
    const res = await getCaptcha();
    captcha.value = res;
    form.value.captchaId = res.captchaId;
  } catch (err) {
    errorAlert.value = "Gagal memuat Captcha keamanan";
  }
};

const handleLoginInit = async () => {
  errorAlert.value = "";
  successAlert.value = "";
  loadingSubmit.value = true;

  try {
    const res = await loginInit({
      username: form.value.username,
      password: form.value.password,
      captchaId: form.value.captchaId,
      captchaAnswer: form.value.captchaAnswer,
    });

    localStorage.setItem("vnet_token", res.token);
    localStorage.setItem(
      "vnet_user",
      JSON.stringify({
        id: res.id,
        username: res.username,
        nama: res.name || res.nama || "",
        email: res.email,
        phone: res.phone || "",
        role: res.role || "",
      }),
    );

    successAlert.value = "Login berhasil! Mengalihkan...";
    setTimeout(() => {
      window.location.href = "/dashboard";
    }, 1000);
  } catch (err) {
    errorAlert.value = err.message || "Username, password, atau Captcha salah!";
    loadCaptcha();
  } finally {
    loadingSubmit.value = false;
  }
};
</script>

<style scoped>
/* ─── Root Layout ───────────────────────────────── */
.auth-root {
  display: flex;
  height: 100vh;
  width: 100vw;
  position: fixed;
  inset: 0;
  overflow: hidden;
  background: #f8faff;
  font-family: var(--font-body, "Inter", sans-serif);
  z-index: 9999;
}

/* ─── Left Panel ─────────────────────────────────── */
.auth-left {
  flex: 1;
  background: linear-gradient(145deg, #1a2170 0%, #243080 50%, #1a3a8a 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  position: relative;
  overflow: hidden;
}

/* Blob accents */
.illo-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  pointer-events: none;
}
.illo-blob--1 {
  width: 350px;
  height: 350px;
  background: rgba(56, 189, 248, 0.2);
  top: -60px;
  right: -60px;
}
.illo-blob--2 {
  width: 280px;
  height: 280px;
  background: rgba(245, 166, 35, 0.12);
  bottom: -80px;
  left: -40px;
}

/* Brand */
.auth-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 10;
  margin-bottom: 8px;
}
.brand-img {
  height: 36px;
  object-fit: contain;
  filter: brightness(0) invert(1);
}
.brand-text {
  font-family: var(--font-display, "Plus Jakarta Sans", sans-serif);
  font-size: 20px;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: -0.5px;
}

/* Illustration */
.auth-illo-wrap {
  position: relative;
  width: 100%;
  max-width: 380px;
  margin: 20px 0;
  z-index: 5;
}
.illo-svg {
  width: 100%;
  height: auto;
  drop-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  filter: drop-shadow(0 20px 40px rgba(0, 0, 0, 0.25));
  animation: float 6s ease-in-out infinite;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

/* Tagline */
.auth-tagline {
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.35);
  letter-spacing: 1.5px;
  text-transform: uppercase;
  z-index: 5;
  margin-bottom: 16px;
}

/* ─── Right Panel ────────────────────────────────── */
.auth-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #f8faff;
  position: relative;
  overflow-y: auto;
}

/* Top bar */
.auth-topbar {
  position: absolute;
  top: 28px;
  right: 36px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #64748b;
}
.topbar-btn {
  padding: 7px 20px;
  border: 1.5px solid #1a2170;
  border-radius: 99px;
  font-size: 12.5px;
  font-weight: 700;
  color: #1a2170;
  text-decoration: none;
  transition: all 0.2s;
  font-family: var(--font-display, sans-serif);
}
.topbar-btn:hover {
  background: #1a2170;
  color: white;
}

/* Form wrapper */
.form-wrap {
  width: 100%;
  max-width: 420px;
  animation: fadeUp 0.5s ease both;
}
@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Form Header */
.form-hdr {
  margin-bottom: 28px;
}
.form-emoji {
  font-size: 28px;
  margin-bottom: 10px;
}
.form-title {
  font-family: var(--font-display, sans-serif);
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.2;
  margin: 0 0 6px;
}
.form-sub {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

/* ─── Alerts ─────────────────────────────────────── */
.alert-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 20px;
  line-height: 1.4;
}
.alert-box svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}
.alert--error {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}
.alert--success {
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}

/* ─── Form Fields ────────────────────────────────── */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.field-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}
.field-label {
  font-size: 12px;
  font-weight: 700;
  color: #475569;
  font-family: var(--font-display, sans-serif);
  display: flex;
  align-items: center;
  gap: 8px;
}
.captcha-badge {
  font-size: 9px;
  background: #eff6ff;
  color: #1a2170;
  border: 1px solid #bfdbfe;
  border-radius: 99px;
  padding: 1px 7px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.field-wrap {
  position: relative;
  display: flex;
  align-items: center;
  background: #ffffff;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}
.field-wrap--focus {
  border-color: #1a2170;
  box-shadow: 0 0 0 3px rgba(26, 33, 112, 0.08);
}
.field-icon {
  position: absolute;
  left: 14px;
  width: 16px;
  height: 16px;
  color: #94a3b8;
  pointer-events: none;
  flex-shrink: 0;
}
.field-input {
  width: 100%;
  background: transparent;
  border: none;
  padding: 12px 16px 12px 42px;
  font-size: 14px;
  color: #0f172a;
  font-family: var(--font-body, "Inter", sans-serif);
  outline: none;
  border-radius: 10px;
}
.field-input::placeholder {
  color: #cbd5e1;
}

/* Show/hide password toggle */
.toggle-pass {
  background: none;
  border: none;
  padding: 0 14px;
  color: #94a3b8;
  cursor: pointer;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  transition: color 0.2s;
}
.toggle-pass svg {
  width: 16px;
  height: 16px;
}
.toggle-pass:hover {
  color: #1a2170;
}

/* Captcha row */
.captcha-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.captcha-question-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #eff6ff;
  border: 1.5px solid #bfdbfe;
  border-radius: 10px;
  padding: 10px 14px;
  white-space: nowrap;
  flex-shrink: 0;
}
.captcha-q {
  font-family: var(--font-display, sans-serif);
  font-size: 15px;
  font-weight: 800;
  color: #1a2170;
  letter-spacing: 1px;
}
.captcha-eq {
  font-size: 15px;
  font-weight: 700;
  color: #94a3b8;
}
.captcha-refresh {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 2px;
  display: flex;
  align-items: center;
  transition:
    color 0.2s,
    transform 0.3s;
}
.captcha-refresh svg {
  width: 13px;
  height: 13px;
}
.captcha-refresh:hover {
  color: #1a2170;
  transform: rotate(180deg);
}

.captcha-ans-wrap {
  flex: 1;
}
.captcha-ans {
  padding-left: 14px !important;
  text-align: center;
  font-weight: 700;
  font-size: 16px;
  letter-spacing: 4px;
  color: #1a2170;
}

.captcha-skeleton {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f8faff;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  flex-shrink: 0;
}
.skel-bar {
  width: 80px;
  height: 14px;
  background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* ─── Submit Button ──────────────────────────────── */
.btn-submit {
  width: 100%;
  padding: 13px 24px;
  background: linear-gradient(135deg, #1a2170 0%, #2e3d9a 100%);
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  margin-top: 6px;
  position: relative;
  overflow: hidden;
  font-family: var(--font-display, sans-serif);
}
.btn-submit::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.1) 0%,
    rgba(255, 255, 255, 0) 100%
  );
  opacity: 0;
  transition: opacity 0.2s;
}
.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(26, 33, 112, 0.35);
}
.btn-submit:hover:not(:disabled)::before {
  opacity: 1;
}
.btn-submit:active:not(:disabled) {
  transform: translateY(1px);
}
.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.btn-label svg {
  width: 16px;
  height: 16px;
}
.btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.btn-spinner {
  width: 15px;
  height: 15px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.65s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ─── Divider & Alt Link ─────────────────────────── */
.or-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 20px 0 16px;
  color: #cbd5e1;
  font-size: 12px;
}
.or-divider::before,
.or-divider::after {
  content: "";
  flex: 1;
  height: 1px;
  background: #e2e8f0;
}

.alt-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  font-size: 13.5px;
  font-weight: 600;
  color: #475569;
  text-decoration: none;
  transition: all 0.2s;
  font-family: var(--font-display, sans-serif);
  background: white;
}
.alt-link svg {
  width: 16px;
  height: 16px;
  color: #1a2170;
}
.alt-link:hover {
  border-color: #1a2170;
  color: #1a2170;
  background: #eff6ff;
}

/* ─── Transitions ────────────────────────────────── */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.shake-enter-active {
  animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}
.shake-leave-active {
  transition: opacity 0.2s;
}
.shake-leave-to {
  opacity: 0;
}
@keyframes shake {
  10%,
  90% {
    transform: translateX(-2px);
  }
  20%,
  80% {
    transform: translateX(4px);
  }
  30%,
  50%,
  70% {
    transform: translateX(-4px);
  }
  40%,
  60% {
    transform: translateX(4px);
  }
}

/* ─── Responsive ─────────────────────────────────── */
@media (max-width: 900px) {
  .auth-left {
    display: none;
  }
  .auth-right {
    flex: 1;
  }
  .auth-topbar {
    top: 20px;
    right: 20px;
  }
}
@media (max-width: 480px) {
  .auth-right {
    padding: 24px 20px;
  }
  .form-wrap {
    max-width: 100%;
  }
}
</style>
