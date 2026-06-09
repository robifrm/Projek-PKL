/**
 * Unit Test — Auth Store (localStorage-based auth state)
 *
 * Menguji logika auth yang ada di localStorage dan api.js:
 * - Penyimpanan token dan user ke localStorage
 * - Pembacaan token dari localStorage
 * - Penghapusan token saat logout
 * - Format data user yang tersimpan
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'

// ─── Simulasi Auth Store (extracted from LoginView logic) ────────────────────

const AUTH_TOKEN_KEY = 'vnet_token'
const AUTH_USER_KEY  = 'vnet_user'

const authStore = {
  /**
   * Simpan token dan user ke localStorage setelah login sukses
   */
  saveSession(token, user) {
    localStorage.setItem(AUTH_TOKEN_KEY, token)
    localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user))
  },

  /**
   * Ambil token dari localStorage
   */
  getToken() {
    return localStorage.getItem(AUTH_TOKEN_KEY)
  },

  /**
   * Ambil user object dari localStorage
   */
  getUser() {
    const raw = localStorage.getItem(AUTH_USER_KEY)
    if (!raw) return null
    try {
      return JSON.parse(raw)
    } catch {
      return null
    }
  },

  /**
   * Hapus semua data auth dari localStorage (logout)
   */
  clearSession() {
    localStorage.removeItem(AUTH_TOKEN_KEY)
    localStorage.removeItem(AUTH_USER_KEY)
  },

  /**
   * Cek apakah user sedang login (ada token valid)
   */
  isLoggedIn() {
    return !!localStorage.getItem(AUTH_TOKEN_KEY)
  }
}

// ─── Test Suite ──────────────────────────────────────────────────────────────

describe('authStore', () => {

  beforeEach(() => {
    // Bersihkan localStorage sebelum setiap test
    localStorage.clear()
  })

  afterEach(() => {
    localStorage.clear()
  })

  // ─── saveSession() ────────────────────────────────────────

  describe('saveSession()', () => {

    it('menyimpan token ke localStorage dengan key yang benar', () => {
      authStore.saveSession('jwt.token.test', { id: 1, name: 'Admin' })
      expect(localStorage.getItem('vnet_token')).toBe('jwt.token.test')
    })

    it('menyimpan user sebagai JSON string ke localStorage', () => {
      const user = { id: 1, name: 'Admin VNet', role: 'ADMIN', email: 'admin@vnet.id' }
      authStore.saveSession('some.token', user)

      const stored = localStorage.getItem('vnet_user')
      expect(stored).not.toBeNull()
      expect(JSON.parse(stored)).toEqual(user)
    })

    it('menimpa token lama jika login ulang', () => {
      authStore.saveSession('old.token', { id: 1 })
      authStore.saveSession('new.token', { id: 2 })
      expect(authStore.getToken()).toBe('new.token')
    })
  })

  // ─── getToken() ───────────────────────────────────────────

  describe('getToken()', () => {

    it('mengembalikan null jika belum ada sesi', () => {
      expect(authStore.getToken()).toBeNull()
    })

    it('mengembalikan token yang tersimpan', () => {
      authStore.saveSession('test.jwt', { id: 1 })
      expect(authStore.getToken()).toBe('test.jwt')
    })
  })

  // ─── getUser() ────────────────────────────────────────────

  describe('getUser()', () => {

    it('mengembalikan null jika belum ada sesi', () => {
      expect(authStore.getUser()).toBeNull()
    })

    it('mengembalikan objek user yang tersimpan dengan benar', () => {
      const user = { id: 99, name: 'Budi', role: 'NOC', email: 'budi@vnet.id' }
      authStore.saveSession('token', user)
      expect(authStore.getUser()).toEqual(user)
    })

    it('mengembalikan null jika localStorage berisi JSON invalid', () => {
      localStorage.setItem('vnet_user', 'bukan json valid {')
      expect(authStore.getUser()).toBeNull()
    })
  })

  // ─── isLoggedIn() ─────────────────────────────────────────

  describe('isLoggedIn()', () => {

    it('mengembalikan false jika tidak ada token', () => {
      expect(authStore.isLoggedIn()).toBe(false)
    })

    it('mengembalikan true jika ada token', () => {
      authStore.saveSession('any.token', { id: 1 })
      expect(authStore.isLoggedIn()).toBe(true)
    })
  })

  // ─── clearSession() ───────────────────────────────────────

  describe('clearSession()', () => {

    it('menghapus token dan user dari localStorage', () => {
      authStore.saveSession('token.to.remove', { id: 1, name: 'User' })
      authStore.clearSession()

      expect(localStorage.getItem('vnet_token')).toBeNull()
      expect(localStorage.getItem('vnet_user')).toBeNull()
    })

    it('setelah clearSession, isLoggedIn() harus false', () => {
      authStore.saveSession('token', { id: 1 })
      authStore.clearSession()
      expect(authStore.isLoggedIn()).toBe(false)
    })

    it('clearSession pada state kosong tidak menyebabkan error', () => {
      expect(() => authStore.clearSession()).not.toThrow()
    })
  })

  // ─── User Role Checks ─────────────────────────────────────

  describe('User Role Validation', () => {

    const roles = ['ADMIN', 'NOC', 'FINANCE', 'MANAGER']

    roles.forEach(role => {
      it(`user dengan role ${role} dapat disimpan dan dibaca dengan benar`, () => {
        const user = { id: 1, name: 'Test', role, email: `${role.toLowerCase()}@vnet.id` }
        authStore.saveSession('token', user)
        expect(authStore.getUser().role).toBe(role)
      })
    })
  })
})
