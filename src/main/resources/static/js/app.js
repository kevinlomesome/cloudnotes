// CloudNotes - small helper scripts
document.addEventListener('DOMContentLoaded', function () {

    // 1. Delete confirmation modal: fill in the note title and the form URL
    const deleteModal = document.getElementById('deleteModal');
    if (deleteModal) {
        deleteModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            document.getElementById('deleteNoteTitle').textContent = button.getAttribute('data-note-title');
            document.getElementById('deleteForm').setAttribute('action', button.getAttribute('data-delete-url'));
        });
    }

    // 2. Bootstrap client-side validation (the server validates again, for safety)
    document.querySelectorAll('form.needs-validation').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            // Treat whitespace-only input as empty
            form.querySelectorAll('[required]').forEach(function (field) {
                field.setCustomValidity(field.value.trim() === '' ? 'Required' : '');
            });
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });

    // 3. Live character counters under the inputs
    document.querySelectorAll('.char-count').forEach(function (counter) {
        const field = document.getElementById(counter.dataset.for);
        if (!field) return;
        const update = function () { counter.textContent = field.value.length; };
        field.addEventListener('input', update);
        update();
    });

    // 4. Auto-hide success messages after 4 seconds
    document.querySelectorAll('.alert.auto-dismiss').forEach(function (alertEl) {
        setTimeout(function () {
            bootstrap.Alert.getOrCreateInstance(alertEl).close();
        }, 4000);
    });
});
