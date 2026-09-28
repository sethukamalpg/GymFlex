/**
 * GymFlex — Client-side Interactivity & UI Utilities
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Mobile Sidebar Toggle
    const mobileToggle = document.getElementById('mobileMenuToggle');
    const sidebar = document.getElementById('mainSidebar');

    if (mobileToggle && sidebar) {
        mobileToggle.addEventListener('click', () => {
            sidebar.classList.toggle('mobile-open');
        });

        // Close sidebar when clicking outside on mobile
        document.addEventListener('click', (e) => {
            if (sidebar.classList.contains('mobile-open') &&
                !sidebar.contains(e.target) &&
                !mobileToggle.contains(e.target)) {
                sidebar.classList.remove('mobile-open');
            }
        });
    }

    // 2. Client-side Member Table Search Filter
    const searchInput = document.getElementById('memberSearchInput');
    const memberTable = document.getElementById('membersTable');

    if (searchInput && memberTable) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            const rows = memberTable.querySelectorAll('tbody tr');
            let matchCount = 0;

            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                if (text.includes(query)) {
                    row.style.display = '';
                    matchCount++;
                } else {
                    row.style.display = 'none';
                }
            });

            const emptyNotice = document.getElementById('searchEmptyNotice');
            if (emptyNotice) {
                emptyNotice.style.display = (matchCount === 0 && query !== '') ? 'block' : 'none';
            }
        });
    }

    // 3. Auto-Dismiss Alert Toasts after 6 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-10px)';
            setTimeout(() => alert.remove(), 500);
        }, 6000);
    });
});

/**
 * Confirmation dialog helper
 */
function confirmAction(message) {
    return confirm(message || 'Are you sure you want to proceed with this action?');
}
