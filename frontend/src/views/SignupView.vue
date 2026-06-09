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

      <!-- Tagline -->
      <p class="auth-tagline">ISP Management Platform</p>

      <!-- Illustration -->
      <div class="auth-illo-wrap">
        <div class="illo-blob illo-blob--1"></div>
        <div class="illo-blob illo-blob--2"></div>

        <!-- ISP Admin Registration SVG Illustration -->
        <svg
          class="illo-svg"
          viewBox="0 0 380 320"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <ellipse
            cx="190"
            cy="210"
            rx="155"
            ry="90"
            fill="rgba(56,189,248,0.06)"
          />
          <!-- Main card / phone -->
          <rect
            x="110"
            y="50"
            width="160"
            height="220"
            rx="18"
            fill="white"
            stroke="#E2E8F0"
            stroke-width="1.5"
          />
          <rect x="110" y="50" width="160" height="40" rx="18" fill="#1A2170" />
          <rect x="110" y="72" width="160" height="18" rx="0" fill="#1A2170" />
          <text
            x="190"
            y="76"
            text-anchor="middle"
            font-size="10"
            fill="rgba(255,255,255,0.7)"
            font-family="sans-serif"
          >
            Registration
          </text>
          <!-- Avatar circle -->
          <circle
            cx="190"
            cy="128"
            r="26"
            fill="#EFF6FF"
            stroke="#BFDBFE"
            stroke-width="2"
          />
          <circle cx="190" cy="122" r="10" fill="#93C5FD" />
          <path
            d="M168 148 Q190 138 212 148"
            stroke="#93C5FD"
            stroke-width="3"
            stroke-linecap="round"
            fill="none"
          />
          <!-- Input lines -->
          <rect
            x="130"
            y="165"
            width="120"
            height="10"
            rx="5"
            fill="#EFF6FF"
            stroke="#E2E8F0"
            stroke-width="1"
          />
          <rect x="130" y="165" width="70" height="10" rx="5" fill="#BFDBFE" />
          <rect
            x="130"
            y="183"
            width="120"
            height="10"
            rx="5"
            fill="#EFF6FF"
            stroke="#E2E8F0"
            stroke-width="1"
          />
          <rect x="130" y="183" width="90" height="10" rx="5" fill="#BFDBFE" />
          <rect
            x="130"
            y="201"
            width="120"
            height="10"
            rx="5"
            fill="#EFF6FF"
            stroke="#E2E8F0"
            stroke-width="1"
          />
          <rect x="130" y="201" width="50" height="10" rx="5" fill="#BFDBFE" />
          <!-- Button -->
          <rect
            x="140"
            y="220"
            width="100"
            height="22"
            rx="11"
            fill="#1A2170"
          />
          <text
            x="190"
            y="235"
            text-anchor="middle"
            font-size="9"
            fill="white"
            font-family="sans-serif"
            font-weight="bold"
          >
            DAFTAR SEKARANG
          </text>
        </svg>
      </div>
    </div>

    <!-- Right Panel: Forms -->
    <div class="auth-right">
      <!-- Top-right: Sign in link -->
      <div class="auth-topbar">
        <span>Sudah punya akun?</span>
        <RouterLink to="/login" class="topbar-btn">Masuk</RouterLink>
      </div>

      <div class="form-wrap">
        <!-- Step indicator pills (mobile) -->
        <div class="mobile-steps">
          <span class="ms-pill" :class="{ 'ms-pill--active': step === 1 }"
            >1. Isi Data</span
          >
          <span class="ms-arrow">→</span>
          <span class="ms-pill" :class="{ 'ms-pill--active': step === 2 }"
            >2. OTP</span
          >
        </div>

        <!-- Header -->
        <Transition name="slide-up" mode="out-in">
          <div class="form-hdr" :key="step"></div>
        </Transition>

        <!-- Alerts -->
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

        <!-- ── STEP 1: Registration Form ── -->
        <Transition name="fade-slide" mode="out-in">
          <form
            v-if="step === 1"
            key="register"
            @submit.prevent="handleRegisterInit"
            class="auth-form"
            novalidate
          >
            <!-- Full Name -->
            <div class="field-group">
              <label class="field-label">Nama Lengkap</label>
              <div
                class="field-wrap"
                :class="{ 'field-wrap--focus': focusedField === 'nama' }"
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
                  id="signup-nama"
                  type="text"
                  v-model="form.nama"
                  class="field-input"
                  placeholder="Nama lengkap Anda"
                  required
                  @focus="focusedField = 'nama'"
                  @blur="focusedField = ''"
                />
              </div>
            </div>

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
                  <circle cx="12" cy="12" r="4" />
                  <path d="M16 8v5a3 3 0 0 0 6 0v-1a10 10 0 1 0-3.92 7.94" />
                </svg>
                <input
                  id="signup-username"
                  type="text"
                  v-model="form.username"
                  class="field-input"
                  placeholder="Kombinasi huruf & angka"
                  required
                  @focus="focusedField = 'username'"
                  @blur="focusedField = ''"
                />
              </div>
            </div>

            <!-- Email + Phone in a row -->
            <div class="field-row">
              <div class="field-group">
                <label class="field-label">Email</label>
                <div
                  class="field-wrap"
                  :class="{ 'field-wrap--focus': focusedField === 'email' }"
                >
                  <svg
                    class="field-icon"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path
                      d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"
                    />
                    <polyline points="22,6 12,13 2,6" />
                  </svg>
                  <input
                    id="signup-email"
                    type="email"
                    v-model="form.email"
                    class="field-input"
                    placeholder="admin@vnet.id"
                    required
                    @focus="focusedField = 'email'"
                    @blur="focusedField = ''"
                  />
                </div>
              </div>
              <div class="field-group">
                <label class="field-label">No. WA / Telp</label>
                <div
                  class="field-wrap"
                  :class="{ 'field-wrap--focus': focusedField === 'phone' }"
                >
                  <svg
                    class="field-icon"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path
                      d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.21 12 19.79 19.79 0 0 1 1.14 3.38 2 2 0 0 1 3.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"
                    />
                  </svg>
                  <input
                    id="signup-phone"
                    type="tel"
                    v-model="form.phone"
                    class="field-input"
                    placeholder="0812xxxxxx"
                    required
                    @focus="focusedField = 'phone'"
                    @blur="focusedField = ''"
                  />
                </div>
              </div>
            </div>

            <!-- Role Akses -->
            <div class="field-group">
              <label class="field-label">Role Akses</label>
              <div
                class="field-wrap"
                :class="{ 'field-wrap--focus': focusedField === 'role' }"
              >
                <svg
                  class="field-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                </svg>
                <select
                  v-model="form.role"
                  class="field-input field-select"
                  @focus="focusedField = 'role'"
                  @blur="focusedField = ''"
                  required
                >
                  <option value="" disabled>Pilih Role Akses</option>
                  <option value="ADMIN">ADMIN (Super Admin)</option>
                  <option value="NOC">NOC (Network Operations)</option>
                  <option value="FINANCE">FINANCE (Keuangan)</option>
                  <option value="MANAGER">MANAGER (Manajer Wilayah)</option>
                </select>
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
                  id="signup-password"
                  :type="showPassword ? 'text' : 'password'"
                  v-model="form.password"
                  class="field-input"
                  placeholder="Min. 6 karakter"
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
              <!-- Password strength -->
              <div class="pw-strength" v-if="form.password">
                <div class="pw-bar">
                  <div
                    class="pw-fill"
                    :style="{
                      width: pwStrength.pct + '%',
                      background: pwStrength.color,
                    }"
                  ></div>
                </div>
                <span class="pw-label" :style="{ color: pwStrength.color }">{{
                  pwStrength.label
                }}</span>
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
                    placeholder="?"
                    required
                    @focus="focusedField = 'captcha'"
                    @blur="focusedField = ''"
                  />
                </div>
              </div>
            </div>

            <button
              type="submit"
              class="btn-submit"
              :disabled="loadingSubmit"
              id="signup-submit-btn"
            >
              <span v-if="loadingSubmit" class="btn-loading"
                ><span class="btn-spinner"></span>Memproses...</span
              >
              <span v-else class="btn-label"> Daftar </span>
            </button>
          </form>

          <!-- ── STEP 2: OTP Form ── -->
          <form
            v-else
            key="otp"
            @submit.prevent="handleRegisterConfirm"
            class="auth-form"
            novalidate
          >
            <div class="otp-info">
              <div class="otp-icon-wrap">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                >
                  <path
                    d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"
                  />
                  <polyline points="22,6 12,13 2,6" />
                </svg>
              </div>
              <div>
                <div class="otp-info-title">Kode terkirim!</div>
                <div class="otp-info-sub">
                  Periksa email Anda. Kode berlaku 60 detik.
                </div>
              </div>
            </div>

            <div class="field-group">
              <label class="field-label otp-label-center"
                >Masukkan 6 Digit Kode OTP</label
              >
              <div class="otp-row">
                <input
                  v-for="(digit, idx) in 6"
                  :key="idx"
                  :id="'otp-' + idx"
                  type="text"
                  maxlength="1"
                  v-model="otpDigits[idx]"
                  @input="focusNext($event, idx)"
                  @keydown.backspace="focusPrev($event, idx)"
                  @paste.prevent="handleOtpPaste($event)"
                  class="otp-box"
                  :class="{ 'otp-box--filled': otpDigits[idx] }"
                  placeholder="·"
                />
              </div>
            </div>

            <div class="otp-actions">
              <button type="button" @click="backToForm" class="btn-back-link">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <line x1="19" y1="12" x2="5" y2="12" />
                  <polyline points="12 19 5 12 12 5" />
                </svg>
                Kembali ke Formulir
              </button>

              <!-- Resend OTP -->
              <div class="resend-row">
                <span class="resend-text">Tidak menerima kode?</span>
                <button
                  type="button"
                  class="btn-resend"
                  :class="{
                    'btn-resend--disabled':
                      resendCountdown > 0 || loadingResend,
                  }"
                  :disabled="resendCountdown > 0 || loadingResend"
                  @click="handleResend"
                  id="resend-otp-btn"
                >
                  <span v-if="loadingResend" class="btn-loading">
                    <span class="btn-spinner btn-spinner--sm"></span>
                    Mengirim...
                  </span>
                  <span v-else-if="resendCountdown > 0">
                    Kirim Ulang ({{ resendCountdown }}s)
                  </span>
                  <span v-else>Kirim Ulang Kode</span>
                </button>
              </div>

              <button
                type="submit"
                class="btn-submit"
                :disabled="loadingSubmit"
                id="otp-confirm-btn"
              >
                <span v-if="loadingSubmit" class="btn-loading"
                  ><span class="btn-spinner"></span>Membuat Akun...</span
                >
                <span v-else class="btn-label">
                  Konfirmasi & Daftarkan
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                  >
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </span>
              </button>
            </div>
          </form>
        </Transition>

        <!-- Bottom sign-in link -->
        <div class="signin-link" v-if="step === 1">
          Sudah punya akun?
          <RouterLink to="/login" id="goto-login" class="text-link"
            >Masuk di sini</RouterLink
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import {
  getCaptcha,
  registerInit,
  registerConfirm,
  resendOtp,
} from "@/services/api";

const router = useRouter();

const step = ref(1);
const loadingSubmit = ref(false);
const loadingResend = ref(false);
const errorAlert = ref("");
const successAlert = ref("");
const captcha = ref(null);
const sessionId = ref("");
const focusedField = ref("");
const showPassword = ref(false);
const otpDigits = ref(["", "", "", "", "", ""]);
const resendCountdown = ref(0);
let countdownTimer = null;

const form = ref({
  nama: "",
  username: "",
  email: "",
  phone: "",
  password: "",
  role: "",
  captchaId: "",
  captchaAnswer: "",
});

// Password strength
const pwStrength = computed(() => {
  const p = form.value.password;
  if (!p) return { pct: 0, color: "#E2E8F0", label: "" };
  let score = 0;
  if (p.length >= 6) score++;
  if (p.length >= 10) score++;
  if (/[A-Z]/.test(p)) score++;
  if (/[0-9]/.test(p)) score++;
  if (/[^A-Za-z0-9]/.test(p)) score++;
  if (score <= 1) return { pct: 20, color: "#EF4444", label: "Lemah" };
  if (score <= 2) return { pct: 40, color: "#F5A623", label: "Sedang" };
  if (score <= 3) return { pct: 65, color: "#3B82F6", label: "Cukup" };
  return { pct: 100, color: "#16A34A", label: "Kuat" };
});

onMounted(() => {
  loadCaptcha();
});

onUnmounted(() => {
  clearInterval(countdownTimer);
});

const startCountdown = (seconds = 60) => {
  clearInterval(countdownTimer);
  resendCountdown.value = seconds;
  countdownTimer = setInterval(() => {
    resendCountdown.value--;
    if (resendCountdown.value <= 0) {
      clearInterval(countdownTimer);
    }
  }, 1000);
};

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
  } catch {
    errorAlert.value = "Gagal memuat Captcha keamanan";
  }
};

const handleRegisterInit = async () => {
  errorAlert.value = "";
  successAlert.value = "";
  loadingSubmit.value = true;

  if (!form.value.role) {
    errorAlert.value = "Role akses wajib dipilih!";
    loadingSubmit.value = false;
    return;
  }

  if (form.value.password.length < 6) {
    errorAlert.value = "Password minimal 6 karakter!";
    loadingSubmit.value = false;
    return;
  }

  try {
    const res = await registerInit({
      nama: form.value.nama,
      username: form.value.username,
      email: form.value.email,
      phone: form.value.phone,
      password: form.value.password,
      role: form.value.role,
      captchaId: form.value.captchaId,
      captchaAnswer: form.value.captchaAnswer,
    });
    sessionId.value = res.sessionId;
    successAlert.value = res.message;
    step.value = 2;
    otpDigits.value = ["", "", "", "", "", ""];
    startCountdown(60);
  } catch (err) {
    errorAlert.value =
      err.message || "Registrasi gagal! Periksa kembali data Anda.";
    loadCaptcha();
  } finally {
    loadingSubmit.value = false;
  }
};

const handleRegisterConfirm = async () => {
  errorAlert.value = "";
  successAlert.value = "";
  loadingSubmit.value = true;

  const fullOtp = otpDigits.value.join("");
  if (fullOtp.length !== 6) {
    errorAlert.value = "Kode OTP harus 6 digit!";
    loadingSubmit.value = false;
    return;
  }

  try {
    const res = await registerConfirm({
      sessionId: sessionId.value,
      otp: fullOtp,
    });
    successAlert.value =
      res.message || "Registrasi berhasil! Mengalihkan ke Login...";
    setTimeout(() => router.push("/login"), 1500);
  } catch (err) {
    errorAlert.value = err.message || "Kode OTP tidak cocok!";
  } finally {
    loadingSubmit.value = false;
  }
};

const backToForm = () => {
  step.value = 1;
  sessionId.value = "";
  errorAlert.value = "";
  successAlert.value = "";
  loadCaptcha();
};

const focusNext = (e, index) => {
  if (e.target.value && index < 5) {
    document.getElementById("otp-" + (index + 1))?.focus();
  }
};

const focusPrev = (e, index) => {
  if (!e.target.value && index > 0) {
    const prev = document.getElementById("otp-" + (index - 1));
    if (prev) {
      prev.focus();
      otpDigits.value[index - 1] = "";
    }
  }
};

const handleOtpPaste = (e) => {
  const text = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6);
  text.split("").forEach((ch, i) => {
    otpDigits.value[i] = ch;
  });
  document.getElementById("otp-" + Math.min(text.length, 5))?.focus();
};

const handleResend = async () => {
  if (resendCountdown.value > 0 || loadingResend.value) return;
  loadingResend.value = true;
  errorAlert.value = "";
  successAlert.value = "";
  try {
    const res = await resendOtp(sessionId.value);
    successAlert.value = res.message || "Kode OTP baru telah dikirim!";
    otpDigits.value = ["", "", "", "", "", ""];
    startCountdown(60);
    setTimeout(() => {
      document.getElementById("otp-0")?.focus();
    }, 100);
  } catch (err) {
    errorAlert.value = err.message || "Gagal mengirim ulang kode OTP.";
  } finally {
    loadingResend.value = false;
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
  flex: 1.15;
  background: linear-gradient(145deg, #1a2170 0%, #243080 55%, #1a3a8a 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  position: relative;
  overflow: hidden;
}
.illo-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  pointer-events: none;
}
.illo-blob--1 {
  width: 320px;
  height: 320px;
  background: rgba(56, 189, 248, 0.18);
  top: -60px;
  right: -60px;
}
.illo-blob--2 {
  width: 260px;
  height: 260px;
  background: rgba(245, 166, 35, 0.1);
  bottom: -60px;
  left: -30px;
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
  font-family: var(--font-display, sans-serif);
  font-size: 20px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.5px;
}
.auth-illo-wrap {
  position: relative;
  width: 100%;
  max-width: 360px;
  margin: 20px 0 8px;
  z-index: 5;
}
.illo-svg {
  width: 100%;
  height: auto;
  filter: drop-shadow(0 20px 40px rgba(0, 0, 0, 0.2));
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

.form-wrap {
  width: 100%;
  max-width: 440px;
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

/* Mobile step pills */
.mobile-steps {
  display: none;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 12px;
}
.ms-pill {
  padding: 4px 12px;
  border-radius: 99px;
  background: #e2e8f0;
  color: #94a3b8;
  font-weight: 600;
}
.ms-pill--active {
  background: #1a2170;
  color: white;
}
.ms-arrow {
  color: #cbd5e1;
}

.form-hdr {
  margin-bottom: 24px;
}
.form-emoji {
  font-size: 28px;
  margin-bottom: 10px;
}
.form-title {
  font-family: var(--font-display, sans-serif);
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 6px;
}
.form-sub {
  font-size: 13.5px;
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
  margin-bottom: 18px;
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

/* ─── Fields ─────────────────────────────────────── */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.field-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}
.field-row {
  display: flex;
  gap: 12px;
}
.field-row .field-group {
  flex: 1;
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
}
.field-input {
  width: 100%;
  background: transparent;
  border: none;
  padding: 11px 16px 11px 42px;
  font-size: 13.5px;
  color: #0f172a;
  font-family: var(--font-body, "Inter", sans-serif);
  outline: none;
  border-radius: 10px;
}
.field-input::placeholder {
  color: #cbd5e1;
}
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

.field-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2394a3b8' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  background-size: 14px;
  padding-right: 40px;
  cursor: pointer;
}

/* Password strength */
.pw-strength {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pw-bar {
  flex: 1;
  height: 4px;
  background: #e2e8f0;
  border-radius: 99px;
  overflow: hidden;
}
.pw-fill {
  height: 100%;
  border-radius: 99px;
  transition:
    width 0.3s,
    background 0.3s;
}
.pw-label {
  font-size: 11px;
  font-weight: 700;
  min-width: 48px;
}

/* Captcha */
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
  padding: 9px 14px;
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
  font-size: 15px;
  letter-spacing: 4px;
  color: #1a2170;
}
.captcha-skeleton {
  display: flex;
  align-items: center;
  padding: 11px 16px;
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

/* ─── OTP ─────────────────────────────────────────── */
.otp-info {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 6px;
}
.otp-icon-wrap {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  background: #16a34a;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.otp-icon-wrap svg {
  width: 18px;
  height: 18px;
  stroke: white;
}
.otp-info-title {
  font-size: 13px;
  font-weight: 700;
  color: #166534;
  margin-bottom: 2px;
}
.otp-info-sub {
  font-size: 12px;
  color: #16a34a;
  line-height: 1.4;
}

.otp-label-center {
  justify-content: center;
}
.otp-row {
  display: flex;
  gap: 10px;
  justify-content: center;
}
.otp-box {
  width: 52px;
  height: 58px;
  background: white;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  font-size: 22px;
  font-weight: 800;
  color: #1a2170;
  text-align: center;
  outline: none;
  font-family: var(--font-display, sans-serif);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}
.otp-box::placeholder {
  color: #e2e8f0;
}
.otp-box:focus {
  border-color: #1a2170;
  box-shadow: 0 0 0 3px rgba(26, 33, 112, 0.1);
  transform: scale(1.05);
}
.otp-box--filled {
  border-color: #38bdf8;
  background: #f0f9ff;
  color: #1a2170;
}

.otp-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.btn-back-link {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  text-decoration: none;
  width: fit-content;
  padding: 0;
  transition: color 0.2s;
  font-family: var(--font-body, sans-serif);
}
.btn-back-link svg {
  width: 14px;
  height: 14px;
}
.btn-back-link:hover {
  color: #1a2170;
}

/* Resend OTP */
.resend-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8faff;
  border: 1.5px dashed #e2e8f0;
  border-radius: 10px;
  padding: 10px 14px;
}
.resend-text {
  font-size: 12.5px;
  color: #94a3b8;
}
.btn-resend {
  background: none;
  border: none;
  font-size: 12.5px;
  font-weight: 700;
  color: #1a2170;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 6px;
  transition: all 0.2s;
  font-family: var(--font-display, sans-serif);
  display: flex;
  align-items: center;
  gap: 6px;
}
.btn-resend:hover:not(:disabled) {
  background: #eff6ff;
}
.btn-resend--disabled {
  color: #94a3b8 !important;
  cursor: not-allowed !important;
  background: none !important;
}
.btn-spinner--sm {
  width: 11px;
  height: 11px;
  border-width: 1.5px;
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
  position: relative;
  overflow: hidden;
  font-family: var(--font-display, sans-serif);
}
.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(26, 33, 112, 0.35);
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

/* ─── Bottom sign-in ─────────────────────────────── */
.signin-link {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: #64748b;
}
.text-link {
  color: #1a2170;
  font-weight: 700;
  text-decoration: none;
  margin-left: 4px;
}
.text-link:hover {
  text-decoration: underline;
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

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(24px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-24px);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-up-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

/* ─── Responsive ─────────────────────────────────── */
@media (max-width: 960px) {
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
  .mobile-steps {
    display: flex;
  }
}
@media (max-width: 480px) {
  .auth-right {
    padding: 24px 20px;
  }
  .field-row {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
