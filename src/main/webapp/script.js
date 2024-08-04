document.addEventListener('DOMContentLoaded', () => {
    const folderPath = document.getElementById('folderPath');
    const indexButton = document.getElementById('indexButton');
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');
    const resultsDiv = document.getElementById('results');
    const historyButton = document.createElement('button');
    historyButton.textContent = 'Show Search History';
    historyButton.id = 'historyButton';
    document.querySelector('.search-box').appendChild(historyButton);

    const sortButton = document.createElement('button');
    sortButton.textContent = 'Sort by Term';
    sortButton.id = 'sortButton';
    document.querySelector('.search-box').appendChild(sortButton);

    let currentSortOrder = 'term';

    indexButton.addEventListener('click', indexFolder);
    searchButton.addEventListener('click', performSearch);
    historyButton.addEventListener('click', () => showSearchHistory(currentSortOrder));
    sortButton.addEventListener('click', toggleSort);
    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            performSearch();
        }
    });

    function indexFolder() {
        const path = folderPath.value.trim();
        if (path) {
            fetch('/index', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `folderPath=${encodeURIComponent(path)}`
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message);
                } else {
                    alert('Error: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred while indexing the folder.');
            });
        } else {
            alert('Please enter a folder path.');
        }
    }

    function performSearch() {
        let query = searchInput.value.trim();
        if (query) {
            if (!query.startsWith('"') && !query.endsWith('"') && query.includes(' ')) {
                query = `"${query}"`;
            }


            addToSearchHistory(query);

            fetch(`/search?q=${encodeURIComponent(query)}`)
                .then(response => response.json())
                .then(data => {
                    displayResults(data, query);
                })
                .catch(error => {
                    console.error('Error:', error);
                    resultsDiv.innerHTML = 'An error occurred while searching.';
                });
        }
    }

    function displayResults(results, query) {
        if (results.length === 0) {
            resultsDiv.innerHTML = 'No results found.';
        } else {
            const searchTerm = query.replace(/^"(.*)"$/, '$1').toLowerCase();
            const resultList = results.map(result => {
                const highlightedSnippet = result.snippet.replace(new RegExp(searchTerm, 'gi'), match => `<mark>${match}</mark>`);
                return `<li>
                    <a href="#" onclick="openDocument('${encodeURIComponent(result.fullPath)}', '${encodeURIComponent(searchTerm)}'); return false;">
                        <strong>${result.fileInfo.fileName}</strong>
                    </a> (${result.fileInfo.fileType})
                    <span class="occurrences">${result.occurrences} occurrence${result.occurrences !== 1 ? 's' : ''}</span>
                    <button onclick="previewDocument('${encodeURIComponent(result.fullPath)}', '${encodeURIComponent(searchTerm)}')">Preview</button>
                    <br>
                    <em>${highlightedSnippet}</em>
                </li>`;
            }).join('');
            resultsDiv.innerHTML = `<ul>${resultList}</ul>`;
        }
    }

    function addToSearchHistory(term) {
        fetch('/history', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ term: term })
        })
        .catch(error => console.error('Error saving search history:', error));
    }

    function showSearchHistory(sortOrder) {
        fetch(`/history?sortBy=${sortOrder}`)
            .then(response => response.json())
            .then(history => {
                const historyList = history.map(entry =>
                    `<li>${entry.term} (${new Date(entry.time).toLocaleString()})</li>`
                ).join('');
                resultsDiv.innerHTML = `<h3>Search History (Sorted by ${sortOrder})</h3><ul>${historyList}</ul>`;
            })
            .catch(error => {
                console.error('Error:', error);
                resultsDiv.innerHTML = 'An error occurred while fetching search history.';
            });
    }

    function toggleSort() {
        currentSortOrder = currentSortOrder === 'term' ? 'time' : 'term';
        sortButton.textContent = `Sort by ${currentSortOrder === 'term' ? 'Time' : 'Term'}`;
        showSearchHistory(currentSortOrder);
    }
});

function openDocument(filePath, searchWord) {
    window.open(`/view?path=${filePath}&word=${searchWord}`, '_blank');
}

function previewDocument(filePath, searchWord) {
    fetch(`/preview?path=${filePath}&word=${searchWord}`)
        .then(response => response.text())
        .then(previewContent => {
            const previewDiv = document.createElement('div');
            previewDiv.className = 'preview';
            previewDiv.innerHTML = `
                <h3>File Preview</h3>
                <pre>${previewContent}</pre>
                <button onclick="this.parentElement.remove()">Close Preview</button>
            `;
            document.body.appendChild(previewDiv);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while previewing the file.');
        });
}