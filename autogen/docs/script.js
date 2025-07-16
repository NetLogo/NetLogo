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

		// Scroll to hash
		if (window.location.hash) {
			const target = document.querySelector(window.location.hash);
			if (target) {
				target.scrollIntoView({ behavior: 'smooth' });
				// Ensure the target is visible after scrolling
				setTimeout(() => {
					target.scrollIntoView({ behavior: 'smooth', block: 'start' });
				}, 100);
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

		// Wrap tables in div.table-container
		document.querySelectorAll('table').forEach((table) => {
			const container = document.createElement('div');
			container.className = 'table-container';
			table.parentNode.insertBefore(container, table);
			container.appendChild(table);
		});

		// Dropdown hover
		const dropdownExitTimers = [];
		document.querySelectorAll('.navbar__item').forEach((item) => {
			// Cancel any existing timers
			const timers = [...dropdownExitTimers];
			dropdownExitTimers.length = 0;
			timers.forEach((timer) => clearTimeout(timer));

			// Clear any 'open' class from previous items
			document.querySelectorAll('.navbar__item .dropdown__menu.open').forEach((menu) => {
				menu.classList.remove('open');
			});

			// Set up new timers for the current item
			let enterTimer, leaveTimer;

			const menu = item.querySelector('.dropdown__menu');
			if (!menu) return; // Skip if no dropdown menu

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

			dropdownExitTimers.push(leaveTimer);
		});

		// Version Change
		const CURRENT_VERSION = 'this'; // Default to current version
		function goToVersion({ version, urlPrefix }) {
			if (!version || version === CURRENT_VERSION) return;

			// Get the path up to and including docs/ (if present)
			const pathname = window.location.pathname.slice(1); // Remove leading slash
			const versionPathIndexEnd = pathname.indexOf('/'); // Find the first slash after the leading slash

			// Why 2? -> version number always ends with a slash, so we need to skip it
			let rest = window.location.pathname.slice(versionPathIndexEnd + 2); // Get the rest of the path after the first slash
			if (window.netlogo && window.netlogo.redirectPath) {
				// Workaround for new pages
				rest = window.netlogo.redirectPath;
			}

			// Get the option element
			let newURL = '';
			if (urlPrefix) {
				newURL = urlPrefix + '/' + rest; // Construct the new URL with prefix
			} else {
				// If no urlPrefix is provided, use the current path as the base
				newURL = window.location.origin + '/' + version + '/' + rest; // Construct the new URL
			}

			window.location.href = newURL; // Navigate to the new URL
		}

		async function checkNot404(url) {
			try {
				// allow cross-origin requests
				const response = await fetch(url, {
					method: 'HEAD',
					headers: {
						'Content-Type': 'text/html',
					},
					mode: 'cors',
				});
				return response.status !== 404;
			} catch (error) {
				console.error(`Error checking URL ${url}:`, error);
				return false;
			}
		}

		function expandStringVariables(string) {
			if (!string) return '';
			if (!window.netlogo || !window.netlogo.stringVariables) return string;

			// A variable has the form ${variableName}
			const variableRegex = /\$\{([a-zA-Z0-9_]+)\}/g;
			const variables = window.netlogo.stringVariables || {};
			return string.replace(variableRegex, (match, variableName) => {
				// Check if the variable exists in netlogo.stringVariables
				if (variables[variableName]) {
					return variables[variableName];
				}
				console.error(`Variable ${variableName} not found in netlogo.stringVariables`);
				return match; // Return the original match if no variable found
			});
		}

		const $versionSelect = document.getElementById('version-select');
		$versionSelect.selectedIndex = 0;
		$versionSelect.addEventListener('change', function (e) {
			e.preventDefault();
			e.stopImmediatePropagation();

			// Save the selected version
			const version = this.value;

			// Maintains selected index since the browser
			// preserves the selected index on back/forward navigation
			// This is always guaranteed to be the first option
			this.selectedIndex = 0;

			const option = this.querySelector(`option[value="${version}"]`);
			let urlPrefix = undefined;
			if (option && option.dataset.urlPrefix) {
				urlPrefix = expandStringVariables(option.dataset.urlPrefix);
			}
			goToVersion({ version, urlPrefix });
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
