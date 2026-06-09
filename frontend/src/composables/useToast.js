import { useUIStore } from '@/stores/ui';

export function useToast() {
  const uiStore = useUIStore();

  return {
    success: (msg, duration = 3000) => uiStore.addToast(msg, 'success', duration),
    error: (msg, duration = 4000) => uiStore.addToast(msg, 'error', duration),
    info: (msg, duration = 3000) => uiStore.addToast(msg, 'info', duration),
    warning: (msg, duration = 3000) => uiStore.addToast(msg, 'warning', duration)
  };
}
