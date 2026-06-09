<template>
  <div class="toast-container">
    <TransitionGroup name="toast">
      <div 
        v-for="toast in toasts" 
        :key="toast.id" 
        class="toast glass-card"
        :class="`toast--${toast.type}`"
      >
        <div class="toast-icon">
          <svg v-if="toast.type === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg>
          <svg v-else-if="toast.type === 'error'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
          <svg v-else-if="toast.type === 'warning'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path stroke-linecap="round" stroke-linejoin="round" d="M12 16v-4m0-4h.01"/></svg>
        </div>
        <p class="toast-msg">{{ toast.message }}</p>
        <button class="toast-close" @click="uiStore.removeToast(toast.id)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { useUIStore } from '@/stores/ui';
import { storeToRefs } from 'pinia';

const uiStore = useUIStore();
const { toasts } = storeToRefs(uiStore);
</script>

<style scoped>
.toast-container {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  pointer-events: none;
}
.toast {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--r-md);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
  min-width: 250px;
  max-width: 400px;
  border-left: 4px solid transparent;
}
.toast--success { border-left-color: var(--green-ok); }
.toast--error { border-left-color: var(--red); }
.toast--warning { border-left-color: var(--gold); }
.toast--info { border-left-color: var(--sky); }

.toast-icon svg {
  width: 20px; height: 20px;
}
.toast--success .toast-icon { color: var(--green-ok); }
.toast--error .toast-icon { color: var(--red); }
.toast--warning .toast-icon { color: var(--gold); }
.toast--info .toast-icon { color: var(--sky); }

.toast-msg {
  margin: 0;
  font-size: 13px;
  color: var(--text-1);
  flex: 1;
}
.toast-close {
  background: none; border: none; cursor: pointer;
  color: var(--text-3);
  padding: 4px;
  border-radius: 4px;
}
.toast-close:hover {
  background: var(--surface-2);
  color: var(--text-1);
}
.toast-close svg { width: 14px; height: 14px; }

/* Transitions */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(30px) scale(0.9);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(30px) scale(0.9);
}
</style>
