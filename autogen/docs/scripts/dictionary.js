(function () {
    document.addEventListener('DOMContentLoaded', function () {
        // Get the sidebar element
        const sidebarDiv = document.querySelector('.sidebar');
        if (!sidebarDiv) {
            console.error('Sidebar element not found');
            return;
        }

        const primList = sidebarDiv.querySelector('.primitive-list');
        if (!primList) {
            console.error('Primitive list element not found');
            return;
        }

        const searchInput = document.getElementById('search-input');
        if (!searchInput) {
            console.error('Search bar element not found');
            return;
        }

        const updateSearchResults = async () => {
            const searchTerm = searchInput.value.toLowerCase();
            const items = document.querySelectorAll('.heading-items li');
            items.forEach((item) => {
                const link = item.querySelector('a');
                if (link && link.textContent.toLowerCase().includes(searchTerm)) {
                    item.style.display = 'block';
                } else {
                    item.style.display = 'none';
                }
            });
        };

        // Maintain last scroll position
        const parentScroll = new URLSearchParams(window.location.search).get('parentScroll');
        sidebarDiv.scrollTop = parentScroll || 0;

        const scrollBehavior = parentScroll ? 'smooth' : 'instant';

        // Maintain last search query
        const searchQuery = new URLSearchParams(window.location.search).get('search');
        if (searchQuery) {
            searchInput.value = searchQuery;
            updateSearchResults();
        }

        // Remove parentScroll from the URL to avoid confusion
        const newURL = new URL(window.location.href);
        newURL.searchParams.delete('parentScroll');
        newURL.searchParams.delete('search');
        window.history.replaceState({}, '', newURL);

        // Process links in the sidebar
        // currentPath: /<version>/dict/<primitive-name>.html
        const links = document.querySelectorAll('.sidebar a');
        const currentPath = window.location.pathname;

        // Base URL matches <base href="../" />
        const baseURL = new URL('../', window.location.href);
        const scrollMargin = 100;

        // Loop through each link
        // and 1. set data-link attribute to the pathname
        //     2. add 'active' class if it matches currentPath
        //     3. add click event to update URL with parentScroll
        let didScroll = false;
        links.forEach((link) => {
            try {
                const url = new URL(link.getAttribute('href'), baseURL);
                link.setAttribute('data-link', url.pathname);
                if (url.pathname === currentPath) {
                    link.classList.add('active');
                    // Local div scroll
                    if (sidebarDiv && !didScroll) {
                        didScroll = true;
                        setTimeout(() => {
                            sidebarDiv.scrollTo({
                                top: link.offsetTop - sidebarDiv.offsetTop - scrollMargin,
                                behavior: scrollBehavior,
                            });
                            primList.scrollTo({
                                top: link.offsetTop - primList.offsetTop - scrollMargin,
                                behavior: 'instant', // Mobile
                            });
                        }, 0);
                    }
                }

                link.addEventListener('click', function (event) {
                    event.preventDefault();
                    // Add ?parentScroll=<parentScroll> to the URL
                    const parentScroll = sidebarDiv ? sidebarDiv.scrollTop : 0;
                    const query = searchInput ? searchInput.value : '';

                    const newURL = new URL(link.getAttribute('href'), baseURL);
                    newURL.searchParams.set('parentScroll', parentScroll);
                    if (query) {
                        newURL.searchParams.set('search', query);
                    }
                    window.location.href = newURL.href;
                });
            } catch (error) {
                console.error(`Error processing link ${link.getAttribute('href')}:`, error);
            }
        });

        // Search functionality
        let timer = null;
        const debounceDelay = 200; // 300 milliseconds
        searchInput.addEventListener('input', function () {
            clearTimeout(timer);
            timer = setTimeout(updateSearchResults, timer ? debounceDelay : 0);
        });
    });
})();
