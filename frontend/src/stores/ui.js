import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUIStore = defineStore('ui', () => {
  const toasts = ref([]);
  let toastId = 0;

  const addToast = (message, type = 'info', duration = 3000) => {
    const id = ++toastId;
    toasts.value.push({ id, message, type });
    if (duration > 0) {
      setTimeout(() => {
        removeToast(id);
      }, duration);
    }
  };

  const removeToast = (id) => {
    toasts.value = toasts.value.filter(t => t.id !== id);
  };

  return { toasts, addToast, removeToast };
});
