(function () {
	document.addEventListener('DOMContentLoaded', function () {
		// Active link highlighting
		const currentPath = window.location.pathname;
		document.querySelectorAll('.navbar__item a').forEach((link) => {
			const path = new URL(link.href).pathname;
			if (path === currentPath) {
				link.classList.add('active');
				const parentItem = link.closest('.navbar__item');
				if (parentItem) parentItem.classList.add('active');
			}
		});

		// Make tag footer the last tag in the body
		const footer = document.querySelector('footer');
		if (footer) {
			const body = document.querySelector('body');
			if (body && body.lastChild !== footer) {
				body.appendChild(footer);
				footer.classList.remove('hidden'); // Show footer if it was hidden
			}
		}

		/* search modal toggle */
		const modal = document.getElementById('searchModal');
		const btn = document.getElementById('searchBtn');
		const input = modal.querySelector('input');

		function openModal() {
			modal.classList.add('open');
			setTimeout(() => input.focus(), 50);
		}
		function closeModal() {
			modal.classList.remove('open');
			input.value = '';
		}

		btn.addEventListener('click', openModal);
		modal.addEventListener('click', (e) => {
			if (e.target === modal) closeModal();
		});
		window.addEventListener('keydown', (e) => {
			const mac = navigator.platform.toUpperCase().indexOf('MAC') >= 0;
			const metaKey = mac ? e.metaKey : e.ctrlKey;
			if (metaKey && e.key.toLowerCase() === 'k') {
				e.preventDefault();
				openModal();
			}
			if (e.key === 'Escape' && modal.classList.contains('open')) closeModal();
		});

		// Dropdown hover
		document.querySelectorAll('.navbar__item').forEach((item) => {
			let enterTimer, leaveTimer;

			const menu = item.querySelector('.dropdown__menu');

			item.addEventListener('mouseenter', () => {
				clearTimeout(leaveTimer);
				enterTimer = setTimeout(() => {
					menu.classList.add('open');
				}, 150); // enter debounce (150ms)
			});

			item.addEventListener('mouseleave', () => {
				clearTimeout(enterTimer);
				leaveTimer = setTimeout(() => {
					menu.classList.remove('open');
				}, 200); // leave debounce (200ms)
			});
		});

		// Version Change
		function goToVersion(select) {
			const version = select.value;
			if (!version || version === '{{ version }}') return;

			const currentUrl = window.location.href;
			const docsIndex = currentUrl.indexOf('/docs');
			const base =
				docsIndex !== -1
					? currentUrl.slice(0, docsIndex + 1) // includes the slash after docs
					: currentUrl.slice(0, currentUrl.lastIndexOf('/') + 1);

			const root = docsIndex !== -1 ? base.slice(0, base.indexOf('/docs') + 1) : base;

			const versionPath = select.hasAttribute('current') ? '' : version + '/';
			const suffix = docsIndex !== -1 ? 'docs/' : '';

			window.location.href = root + versionPath + suffix;
		}

		document.getElementById('version-select').addEventListener('change', function (e) {
			e.preventDefault();
			goToVersion(this);
		});

		// Copyright year update
		const copyrightElement = document.getElementById('copyright');
		if (copyrightElement) {
			const currentYear = new Date().getFullYear();
			const originalText = copyrightElement.innerHTML;
			copyrightElement.innerHTML = originalText.replace('1999-', '1999-' + currentYear);
		}
	});
})();
