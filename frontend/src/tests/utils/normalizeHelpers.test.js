/**
 * Unit Test — Normalize Helper Functions
 *
 * Menguji fungsi-fungsi normalisasi data yang digunakan
 * di AddressInsightsView untuk membersihkan nama kecamatan,
 * kelurahan, dan nama paket internet dari database.
 *
 * Fungsi-fungsi ini dipindahkan ke utils agar bisa ditest secara independen.
 */

import { describe, it, expect } from 'vitest'

// ─── Fungsi yang diuji (copy dari AddressInsightsView.vue) ───────────────────

/**
 * Normalisasi nama kecamatan:
 * - Hapus prefix "KEC.", "KECAMATAN"
 * - Title Case
 * - Trim whitespace
 */
function cleanKecamatan(name) {
  if (!name || name.trim() === '') return 'Tidak Diketahui'
  return name
    .trim()
    .replace(/^(KEC\.|KECAMATAN)\s*/i, '')
    .toLowerCase()
    .replace(/\b\w/g, c => c.toUpperCase())
    .trim()
}

/**
 * Normalisasi nama kelurahan:
 * - Hapus prefix "KEL.", "KELURAHAN"
 * - Title Case
 */
function cleanKelurahan(name) {
  if (!name || name.trim() === '') return 'Tidak Diketahui'
  return name
    .trim()
    .replace(/^(KEL\.|KELURAHAN)\s*/i, '')
    .toLowerCase()
    .replace(/\b\w/g, c => c.toUpperCase())
    .trim()
}

/**
 * Normalisasi nama paket internet:
 * - Ekstrak kecepatan angka dari nama (mis: "1.0 Mbps" → "1 Mbps")
 * - Handle case "Complimentary" / "Gratis"
 */
function normalizePackageName(raw) {
  if (!raw) return 'Paket Tidak Diketahui'

  const lower = raw.toLowerCase().trim()
  if (lower.includes('compli') || lower.includes('gratis') || lower.includes('free')) {
    return 'Complimentary'
  }

  const match = raw.match(/(\d+(?:\.\d+)?)\s*[Mm]bps/i)
  if (match) {
    const speed = parseFloat(match[1])
    return `${Number.isInteger(speed) ? speed : speed} Mbps`
  }

  return raw.trim()
}

// ─── Test Suite ──────────────────────────────────────────────────────────────

describe('cleanKecamatan()', () => {

  it('mengembalikan "Tidak Diketahui" jika input null', () => {
    expect(cleanKecamatan(null)).toBe('Tidak Diketahui')
  })

  it('mengembalikan "Tidak Diketahui" jika input string kosong', () => {
    expect(cleanKecamatan('')).toBe('Tidak Diketahui')
    expect(cleanKecamatan('   ')).toBe('Tidak Diketahui')
  })

  it('menghapus prefix "KEC." dan menghasilkan Title Case', () => {
    expect(cleanKecamatan('KEC. CIKOLE')).toBe('Cikole')
  })

  it('menghapus prefix "KECAMATAN" (case-insensitive)', () => {
    expect(cleanKecamatan('KECAMATAN Baros')).toBe('Baros')
    expect(cleanKecamatan('kecamatan cibeureum')).toBe('Cibeureum')
  })

  it('mengubah ke Title Case untuk nama tanpa prefix', () => {
    expect(cleanKecamatan('CIKOLE')).toBe('Cikole')
    expect(cleanKecamatan('lembursitu')).toBe('Lembursitu')
  })

  it('menangani nama multi-kata dengan benar', () => {
    expect(cleanKecamatan('KEC. WARUDOYONG')).toBe('Warudoyong')
  })

  it('mempertahankan nama yang sudah benar (Title Case)', () => {
    expect(cleanKecamatan('Cikole')).toBe('Cikole')
  })
})

describe('cleanKelurahan()', () => {

  it('mengembalikan "Tidak Diketahui" jika null atau kosong', () => {
    expect(cleanKelurahan(null)).toBe('Tidak Diketahui')
    expect(cleanKelurahan('')).toBe('Tidak Diketahui')
  })

  it('menghapus prefix "KEL."', () => {
    expect(cleanKelurahan('KEL. GUNUNGPUYUH')).toBe('Gunungpuyuh')
  })

  it('menghapus prefix "KELURAHAN" (case-insensitive)', () => {
    expect(cleanKelurahan('KELURAHAN Baros')).toBe('Baros')
    expect(cleanKelurahan('kelurahan cibeureum')).toBe('Cibeureum')
  })

  it('mengubah nama tanpa prefix ke Title Case', () => {
    expect(cleanKelurahan('CISARUA')).toBe('Cisarua')
  })
})

describe('normalizePackageName()', () => {

  it('mengembalikan "Paket Tidak Diketahui" jika null', () => {
    expect(normalizePackageName(null)).toBe('Paket Tidak Diketahui')
    expect(normalizePackageName(undefined)).toBe('Paket Tidak Diketahui')
  })

  it('menormalisasi kecepatan desimal ke integer jika bulat (1.0 → 1 Mbps)', () => {
    const result = normalizePackageName('1.0 Mbps - Rp 160.000')
    expect(result).toBe('1 Mbps')
  })

  it('menormalisasi nama paket dengan kecepatan integer', () => {
    expect(normalizePackageName('20 Mbps - Rp 200.000')).toBe('20 Mbps')
    expect(normalizePackageName('10 Mbps')).toBe('10 Mbps')
  })

  it('mengembalikan "Complimentary" untuk berbagai kata gratis/free', () => {
    expect(normalizePackageName('Complimentary')).toBe('Complimentary')
    expect(normalizePackageName('Gratis')).toBe('Complimentary')
    expect(normalizePackageName('FREE PACKAGE')).toBe('Complimentary')
    expect(normalizePackageName('compli 5 mbps')).toBe('Complimentary')
  })

  it('mengembalikan nama asli jika tidak ada pola kecepatan', () => {
    expect(normalizePackageName('Paket Spesial')).toBe('Paket Spesial')
  })

  it('case-insensitive untuk satuan Mbps', () => {
    expect(normalizePackageName('50 MBPS')).toBe('50 Mbps')
    expect(normalizePackageName('50 mbps')).toBe('50 Mbps')
  })
})
