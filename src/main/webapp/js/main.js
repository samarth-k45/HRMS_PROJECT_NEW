/**
 * HRMS - Main JavaScript Utility Library
 * js/main.js
 * 
 * Contains: API helpers, session management, toast notifications,
 * sidebar navigation, and shared utility functions.
 */

/* ============================================================
   SESSION / USER INFO
   ============================================================ */

/** Get stored session data from sessionStorage */
function getSession() {
  try {
    return JSON.parse(sessionStorage.getItem('hrmsUser') || 'null');
  } catch { return null; }
}

/** Store session data after login */
function setSession(data) {
  sessionStorage.setItem('hrmsUser', JSON.stringify(data));
}

/** Clear session and redirect to login */
function clearSession() {
  sessionStorage.removeItem('hrmsUser');
  window.location.href = 'login.html';
}

/**
 * Guard: Redirect to login if no session found.
 * Call this at the top of every protected page.
 * @param {string|null} requiredRole - 'admin', 'employee', or null (any)
 */
function requireAuth(requiredRole = null) {
  const user = getSession();
  if (!user) {
    window.location.href = 'login.html';
    return null;
  }
  if (requiredRole && user.role !== requiredRole) {
    showToast('Access denied.', 'error');
    setTimeout(() => window.location.href = 'dashboard.html', 1000);
    return null;
  }
  return user;
}

/* ============================================================
   API FETCH HELPERS
   ============================================================ */

const BASE = ''; // Requests are relative to the webapp root

/**
 * Generic GET request helper.
 * @param {string} endpoint - e.g. 'api/employees'
 * @returns {Promise<any>}
 */
async function apiGet(endpoint) {
  const url = BASE ? `${BASE}/${endpoint}` : endpoint;
  const resp = await fetch(url, {
    method: 'GET',
    credentials: 'same-origin'
  });
  if (resp.status === 401) { clearSession(); return null; }
  return resp.json();
}

/**
 * Generic POST request helper using FormData / URLSearchParams.
 * @param {string} endpoint
 * @param {Object} params - key-value pairs to POST
 * @returns {Promise<any>}
 */
async function apiPost(endpoint, params = {}) {
  const body = new URLSearchParams(params);
  const url = BASE ? `${BASE}/${endpoint}` : endpoint;
  const resp = await fetch(url, {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: body.toString()
  });
  if (resp.status === 401) { clearSession(); return null; }
  return resp.json();
}

/* ============================================================
   TOAST NOTIFICATIONS
   ============================================================ */

/** Show a toast notification at the bottom-right.
 * @param {string} message
 * @param {'success'|'error'|'info'} type
 * @param {number} duration ms
 */
function showToast(message, type = 'info', duration = 3500) {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }
  const icons = { success: '✅', error: '❌', info: 'ℹ️' };
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${icons[type] || 'ℹ️'}</span> ${message}`;
  container.appendChild(toast);
  setTimeout(() => { toast.style.opacity = '0'; toast.style.transform = 'translateX(40px)'; toast.style.transition = '0.4s ease'; setTimeout(() => toast.remove(), 400); }, duration);
}

/* ============================================================
   SIDEBAR SETUP
   ============================================================ */

/**
 * Populates the sidebar user info and marks the active nav link.
 * Call on every protected page after DOM is loaded.
 */
function initSidebar() {
  const user = getSession();
  if (!user) return;

  // Set user name and role in sidebar footer
  const nameEl = document.getElementById('sidebar-user-name');
  const roleEl = document.getElementById('sidebar-user-role');
  const avatarEl = document.getElementById('sidebar-avatar');
  if (nameEl) nameEl.textContent = user.name;
  if (roleEl) roleEl.textContent = user.role === 'admin' ? '🛡 Admin' : '👤 Employee';
  if (avatarEl) avatarEl.textContent = user.name.charAt(0).toUpperCase();

  // Mark active nav link based on current page
  const currentPage = window.location.pathname.split('/').pop();
  document.querySelectorAll('.nav-link').forEach(link => {
    link.classList.remove('active');
    const href = link.getAttribute('href');
    if (href && href.includes(currentPage)) link.classList.add('active');
  });

  // Logout button
  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
      await apiPost('api/login', { action: 'logout' });
      clearSession();
    });
  }

  // Show/hide admin-only nav items
  const adminLinks = document.querySelectorAll('.admin-only');
  adminLinks.forEach(el => {
    el.style.display = user.role === 'admin' ? '' : 'none';
  });
}

/* ============================================================
   MODAL HELPERS
   ============================================================ */

function openModal(id) {
  const el = document.getElementById(id);
  if (el) { el.classList.add('active'); document.body.style.overflow = 'hidden'; }
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) { el.classList.remove('active'); document.body.style.overflow = ''; }
}

// Close modal when clicking outside the modal box
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('active');
    document.body.style.overflow = '';
  }
});

/* ============================================================
   FORM VALIDATION
   ============================================================ */

/**
 * Validates a form element. Returns true if all required fields are filled.
 * Highlights empty required fields.
 */
function validateForm(formId) {
  const form = document.getElementById(formId);
  if (!form) return true;
  let valid = true;
  form.querySelectorAll('[required]').forEach(field => {
    field.style.borderColor = '';
    if (!field.value.trim()) {
      field.style.borderColor = 'var(--danger)';
      valid = false;
    }
  });
  if (!valid) showToast('Please fill all required fields.', 'error');
  return valid;
}

/* ============================================================
   UTILITY HELPERS
   ============================================================ */

/** Format a number as Indian currency (₹) */
function formatCurrency(amount) {
  return '₹' + Number(amount).toLocaleString('en-IN', { maximumFractionDigits: 0 });
}

/** Format a date string as DD/MM/YYYY */
function formatDate(dateStr) {
  if (!dateStr) return '—';
  const [y, m, d] = dateStr.split('-');
  return `${d}/${m}/${y}`;
}

/** Return HTML for a status badge */
function statusBadge(status) {
  const map = {
    'Present':  'badge-success',
    'Absent':   'badge-danger',
    'Approved': 'badge-success',
    'Rejected': 'badge-danger',
    'Pending':  'badge-warning',
    'admin':    'badge-info',
    'employee': 'badge-muted',
  };
  return `<span class="badge ${map[status] || 'badge-muted'}">${status}</span>`;
}

/** Debounce helper for search inputs */
function debounce(fn, delay = 300) {
  let t;
  return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), delay); };
}

/** Get today's date as yyyy-MM-dd string */
function todayStr() {
  return new Date().toISOString().split('T')[0];
}
