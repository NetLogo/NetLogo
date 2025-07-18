(function () {
	document.addEventListener('DOMContentLoaded', () => {
		console.log(`Extension short name: ${extensionShortName}`);
		// Replace all id.startsWith(extensionShortName + ':') to remove the colon
		document.querySelectorAll('[id^="' + extensionShortName + ':"]').forEach((el) => {
			const oldId = el.id;
			const newId = oldId.replace(extensionShortName + ':', extensionShortName);
			el.id = newId;
		});
		// And match all hrefs that start with #extensionShortName: and replace the colon
		document.querySelectorAll('a[href^="#' + extensionShortName + ':"]').forEach((el) => {
			const oldHref = el.getAttribute('href');
			const newHref = oldHref.replace(extensionShortName + ':', extensionShortName);
			el.setAttribute('href', newHref);
		});

		document.querySelectorAll('a[href^="#' + extensionShortName).forEach((el) => {
			el.classList.add('code'); // Enable code styling for links
		});
	});
})();
