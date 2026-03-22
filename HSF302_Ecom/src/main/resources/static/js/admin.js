/* ============================================================
   VOLTEX — admin.js
   ============================================================ */

/* ── Toast system ────────────────────────────────────────── */
(function () {
    const ICONS = {
        success: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>`,
        error:   `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>`,
        info:    `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>`,
        warning: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`
    };

    window.showAdminToast = function (msg, type, duration) {
        type     = ['success','error','info','warning'].includes(type) ? type : 'info';
        duration = duration || 4000;

        const container = document.getElementById('toastContainer');
        if (!container) return;

        const barId = 'admBar' + Date.now();
        const toast = document.createElement('div');
        toast.className = `adm-toast adm-toast--${type}`;
        toast.innerHTML = `
            <span class="adm-toast-icon">${ICONS[type]}</span>
            <span class="adm-toast-msg">${msg}</span>
            <button class="adm-toast-close" aria-label="Close">&#x2715;</button>
            <div id="${barId}" class="adm-toast-bar" style="transition-duration:${duration}ms"></div>
        `;

        container.appendChild(toast);

        let tid;
        function dismiss() {
            clearTimeout(tid);
            toast.classList.remove('adm-toast--in');
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(8px) scale(.97)';
            setTimeout(() => toast.parentNode && toast.parentNode.removeChild(toast), 280);
        }

        toast.addEventListener('click', dismiss);
        toast.querySelector('.adm-toast-close').addEventListener('click', e => { e.stopPropagation(); dismiss(); });

        requestAnimationFrame(() => requestAnimationFrame(() => {
            toast.classList.add('adm-toast--in');
            const bar = document.getElementById(barId);
            if (bar) requestAnimationFrame(() => { bar.style.width = '0'; });
        }));

        tid = setTimeout(dismiss, duration);
    };

    // Fire any server-side flash toast
    const flashEl = document.getElementById('flashToast');
    if (flashEl) {
        const msg  = flashEl.dataset.msg;
        const type = flashEl.dataset.type;
        if (msg) setTimeout(() => window.showAdminToast(msg, type || 'info'), 100);
    }
})();

/* ── Mobile sidebar toggle ───────────────────────────────── */
(function () {
    const toggle  = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('admSidebar');
    if (!toggle || !sidebar) return;

    toggle.addEventListener('click', () => {
        sidebar.classList.toggle('adm-sidebar--open');
    });

    // Close on outside click
    document.addEventListener('click', e => {
        if (sidebar.classList.contains('adm-sidebar--open')
            && !sidebar.contains(e.target)
            && !toggle.contains(e.target)) {
            sidebar.classList.remove('adm-sidebar--open');
        }
    });
})();