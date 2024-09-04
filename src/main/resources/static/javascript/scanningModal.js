// const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
// const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
const identifyButton = document.getElementById('identifyButton');
const resultContainer = document.getElementById('resultContainer');
const errorContainer= document.getElementById('errorContainer');
const imageInput= document.getElementById('imageInput');
const imagePreview= document.getElementById('imagePreview');
const saveToCollectionButton= document.getElementById('saveToCollectionButton');
const fileName= document.getElementById('fileName');
const gbifInfo= document.getElementById('gbifInfo');

let identifiedPlantData = null;
var goToCollectionButton = document.getElementById('goToCollectionButton');

let csrfToken, csrfHeader;

function updateCsrfTokens() {
    const csrfMeta = document.querySelector("meta[name='_csrf']");
    const csrfHeaderMeta = document.querySelector("meta[name='_csrf_header']");

    if (csrfMeta && csrfHeaderMeta) {
        csrfToken = csrfMeta.getAttribute("content");
        csrfHeader = csrfHeaderMeta.getAttribute("content");
    } else {
        console.warn("CSRF meta tags not found. CSRF protection may not be active.");
    }
}

updateCsrfTokens();


function openGbifDetails(gbifId) {
    window.open(`https://www.gbif.org/species/${gbifId}`, '_blank');
}

document.addEventListener('DOMContentLoaded', function() {
    var scanningModal = document.getElementById('scanningModal');
    var defaultImageSrc = './images/defaultScanningImage.png';

    // this is to refresh the modal when it is closed
    scanningModal.addEventListener('hidden.bs.modal', function () {
        document.getElementById('scanForm').reset();
        resultContainer.style.display = 'none';
        errorContainer.style.display = 'none';
        imageInput.value = '';
        imagePreview.src = defaultImageSrc;
        saveToCollectionButton.style.display = 'none';
        fileName.value = '';
        fileName.style.display = 'none';
        gbifInfo.style.display = 'none';
    })
    // this is to show image preview when a file is selected
    imageInput.addEventListener('change', function(event) {
        var file = event.target.files[0];
        fileName.textContent = file.name;
        fileName.style.display = 'block';
        if (file) {
            var reader = new FileReader();

            reader.onload = function(e) {
                var img = document.getElementById('imagePreview');
                img.src = e.target.result;
            }

            reader.readAsDataURL(file);
        } else {
            imagePreview.src = defaultImageSrc;
            fileName.value = '';
        }
    });

    //This is what happens when you click identify button
    identifyButton.addEventListener('click', function(event) {
        event.preventDefault();
        var formData = new FormData(document.getElementById('scanForm'));

        resultContainer.innerHTML = '<div class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';
        resultContainer.style.display = 'block';
        gbifInfo.style.display = 'block';
        errorContainer.style.display = 'none';
        saveToCollectionButton.style.display = 'none';

        updateCsrfTokens();
        const headers = new Headers();
        if (csrfHeader && csrfToken) {
            headers.append(csrfHeader, csrfToken);
        }
        headers.append('X-Requested-With', 'XMLHttpRequest');

        <!--The code below was generated by Claude-->
        fetch('/identifyPlant', {
            method: 'POST',
            headers: headers,
            body: formData,
            credentials: 'same-origin'
        })
            .then(response => response.json())
            .then(data => {
                if (data.error) {
                    errorContainer.textContent = data.error;
                    errorContainer.style.display = 'block';
                    resultContainer.style.display = 'none';
                    saveToCollectionButton.style.display = 'none';
                    gbifInfo.style.display = 'none';
                    fileName.value = '';
                    fileName.style.display = 'none';


                } else {
                    //Store the identified plant to pass to the back-end later if user wants to save.
                    identifiedPlantData = data
                    var resultHtml = `
                            <div class="d-flex flex-column flex-md-row gap-4 p-3">
                                <div class="flex-shrink-0">
                                    <strong>Sample Image:</strong>
                                    <br>${data.imageUrl ? `<img src="${data.imageUrl}"
                                        class="img-fluid"
                                        style="max-width: 200px; max-height: 200px; object-fit: contain;">`
                                                : 'No image available'}
                                </div>
                                <div class="flex-grow-1 overflow-auto">
                                    <div class="d-flex flex-column gap-3">
                                        <div>
                                            <strong>Plant Name:</strong>
                                            <div class="text-break">${data.bestMatch || 'No record'}</div>
                                        </div>
                                        <div><strong>Score:</strong> ${data.score + " / 1" || 'No record'}</div>
                                        <div>
                                            <strong>Common Names:</strong> 
                                            <div class="text-break">${data.commonNames && data.commonNames.length > 0 ? data.commonNames.join(', ') : 'No record'}</div>
                                        </div>
                                        <div class="d-flex align-items-center flex-wrap">
                                            <strong>*GBIF ID:</strong>
                                            <span class="ms-1 text-break">${data.gbifId || 'No record'}</span>
                                            <button onclick="openGbifDetails('${data.gbifId}')" class="detail-button btn btn-sm btn-outline-secondary ms-2 py-0 px-1" style="font-size: 0.75rem;">
                                                See details
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        `;
                    resultContainer.innerHTML = resultHtml;
                    resultContainer.style.display = 'block';
                    errorContainer.style.display = 'none';
                    saveToCollectionButton.style.display = 'block';
                    gbifInfo.style.display = 'block';
                }
            })
            .catch(error => {
                errorContainer.textContent = 'An error occurred: ' + error.message;
                errorContainer.style.display = 'block';
                resultContainer.style.display = 'none';
                saveToCollectionButton.style.display = 'none';
                gbifInfo.style.display = 'none';
                fileName.value = '';
                fileName.style.display = 'none';

            });
    });
});


//User got the plant identified and clicks save button for myCollection
saveToCollectionButton.addEventListener('click', function() {
    if (identifiedPlantData) {
        var successModal = new bootstrap.Modal(document.getElementById('successModal'));
        successModal.show();
        var modal = bootstrap.Modal.getInstance(document.getElementById('scanningModal'));
        modal.hide();
    } else {
        alert('No plant data to save. Please identify a plant first.');
    }
});

//button directs user to myCollection page
goToCollectionButton.addEventListener('click', function() {
    var modal = bootstrap.Modal.getInstance(successModal);
    modal.hide();
    setTimeout(function() {
        window.location.href = goToCollectionButton.getAttribute('data-url');
    }, 150);


    fetch('/saveIdentifiedPlant', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
        },
        body: JSON.stringify(identifiedPlantData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            var modal = bootstrap.Modal.getInstance(successModal);
            modal.hide();
        })
        .catch((error) => {
            console.error('Error:', error);
            alert('An error occurred while saving the plant.');
        });




});