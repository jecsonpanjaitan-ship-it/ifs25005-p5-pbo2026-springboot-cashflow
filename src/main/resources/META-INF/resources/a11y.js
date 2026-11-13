// Indikator koneksi Vaadin ditaruh di luar landmark; tandai sebagai live region agar
// aturan axe "region" (semua konten berada dalam landmark) tidak melaporkannya.
(function () {
    function mark() {
        document.querySelectorAll('vaadin-connection-indicator:not([role])').forEach(function (el) {
            el.setAttribute('role', 'status');
            el.setAttribute('aria-live', 'polite');
        });
    }
    function start() {
        mark();
        new MutationObserver(mark).observe(document.body, { childList: true });
    }
    if (document.body) {
        start();
    } else {
        document.addEventListener('DOMContentLoaded', start);
    }
})();