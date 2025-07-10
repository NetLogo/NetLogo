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
				footer.classList.remove('hidden-important'); // Show footer if it was hidden
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
		const CURRENT_VERSION = 'this'; // Default to current version
		function goToVersion(select) {
			let docsURL = select.value;
			if (!docsURL || docsURL === CURRENT_VERSION) return;

			// Get the path up to and including docs/ (if present)
			const currentPath = window.location.pathname.replace(/\/$/, ''); // Remove trailing slash
			const docsIndex = currentPath.indexOf('/docs');
			const rest = currentPath.substring(docsIndex + 5);

			// Construct the new URL
			docsURL = docsURL.endsWith('/') ? docsURL.substring(0, docsURL.length - 1) : docsURL;
			const newURLString = docsURL + rest;
			const newURL = new URL(newURLString, window.location.origin);

			window.location = newURL;
		}

		const $versionSelect = document.getElementById('version-select');
		$versionSelect.selectedIndex = 0;
		$versionSelect.addEventListener('change', function (e) {
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
