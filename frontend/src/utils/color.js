// Color utilities for tag rendering.
// Light brand colors (e.g. pale yellow #FFFFD2) are hard to read on white;
// darken them while keeping the hue so the original brand identity stays.

const clamp = (n, min, max) => Math.min(max, Math.max(min, n))

function parseHex(hex) {
  if (!hex) return null
  let c = hex.replace('#', '').trim()
  if (c.length === 3) {
    c = c.split('').map(ch => ch + ch).join('')
  }
  if (c.length !== 6) return null
  const r = parseInt(c.slice(0, 2), 16)
  const g = parseInt(c.slice(2, 4), 16)
  const b = parseInt(c.slice(4, 6), 16)
  if ([r, g, b].some(Number.isNaN)) return null
  return { r, g, b }
}

function toHex({ r, g, b }) {
  const h = (v) => clamp(Math.round(v), 0, 255).toString(16).padStart(2, '0')
  return '#' + h(r) + h(g) + h(b)
}

// Relative luminance per WCAG 2.x.
export function luminance(hex) {
  const rgb = parseHex(hex)
  if (!rgb) return 0
  const toLinear = (v) => {
    const s = v / 255
    return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4)
  }
  return 0.2126 * toLinear(rgb.r) + 0.7152 * toLinear(rgb.g) + 0.0722 * toLinear(rgb.b)
}

// Darken a hex color by amount (0-1) — multiplies channels.
export function darken(hex, amount = 0.4) {
  const rgb = parseHex(hex)
  if (!rgb) return hex
  return toHex({
    r: rgb.r * (1 - amount),
    g: rgb.g * (1 - amount),
    b: rgb.b * (1 - amount),
  })
}

// Pick a readable text color for tag/label rendering. If the source color is
// too bright for white backgrounds, darken until contrast clears AA-ish range.
export function readableColor(hex, fallback = '#4a5568') {
  const rgb = parseHex(hex)
  if (!rgb) return fallback
  let lum = luminance(hex)
  if (lum <= 0.5) return hex
  let amount = 0.35
  while (amount < 0.85) {
    const darker = darken(hex, amount)
    if (luminance(darker) <= 0.35) return darker
    amount += 0.1
  }
  return darken(hex, 0.6)
}

// rgba background string with a small alpha.
export function rgbaBg(hex, alpha = 0.12) {
  const rgb = parseHex(hex)
  if (!rgb) return 'rgba(64, 158, 255, 0.12)'
  return `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${alpha})`
}

// rgba border string with a medium alpha.
export function rgbaBorder(hex, alpha = 0.4) {
  return rgbaBg(hex, alpha)
}
