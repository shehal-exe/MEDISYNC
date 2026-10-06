// MEDISYNC - Enhanced Clinical Patient Portal Script

document.addEventListener('DOMContentLoaded', async () => {
    const setText = (id, value) => {
        const el = document.getElementById(id);
        if (el) el.textContent = value;
    };

    const escapeHtml = (value) => {
        const div = document.createElement('div');
        div.textContent = value == null ? '' : String(value);
        return div.innerHTML;
    };

    const getProfileName = (profile, fallback = 'Patient') => {
        const name = [profile && profile.firstName, profile && profile.lastName]
            .filter(Boolean)
            .join(' ')
            .trim();
        return name || fallback;
    };

    // 1. Auth Verification
    const meRes = await fetchApi('/auth/me');
    if (!meRes || !meRes.success || meRes.data.role !== 'PATIENT') {
        window.location.href = '../index.html';
        return;
    }

    setText('user-greeting', 'Loading profile...');

    const formatCurrency = (value) => `₹${Number(value || 0).toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    })}`;

    const formatPercent = (value) => `${Number(value || 0).toFixed(2)}%`;

    const refillBadgeClass = (status) => {
        if (status === 'APPROVED' || status === 'COMPLETED') return 'success';
        if (status === 'REJECTED') return 'danger';
        return 'warning';
    };

    const formatDateTime = (value) => value ? new Date(value).toLocaleString() : 'Just now';
    let pharmacistDirectory = [];
    let patientMedicinesCache = [];
    let medicineCatalogCache = [];
    let prescriptionCache = [];
    let refillRequestCache = [];
    let reminderCache = [];

    const formatPharmacistLabel = (pharmacist) => {
        const name = getProfileName(pharmacist, 'Pharmacist');
        const handle = pharmacist.publicHandle ? `@${pharmacist.publicHandle}` : `#${pharmacist.pharmacistId}`;
        return `${name} (${handle})`;
    };

    const getMedicineLabel = (medicine) => {
        const manufacturer = medicine.manufacturer ? ` (${medicine.manufacturer})` : '';
        return `${medicine.medicineName || medicine.name}${manufacturer}`;
    };

    function renderInteractionOverview() {
        const container = document.getElementById('interaction-overview-content');
        if (!container) return;

        const active = patientMedicinesCache.filter(m => m.isActive !== false);
        if (active.length === 0) {
            container.innerHTML = `
                <div class="empty-state" style="padding: 12px;">
                    <div class="icon">🛡️</div>
                    <p>No active medicines on this account yet.</p>
                </div>
            `;
            return;
        }

        const names = active.slice(0, 2).map(m => escapeHtml(m.medicineName)).join(' + ');
        const message = active.length > 1
            ? 'Use the scanner above before combining medicines, supplements, or alcohol.'
            : 'Add another medicine or use the scanner above to check a possible combination.';

        container.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px;">
                <div>
                    <strong>${names}</strong>
                    <p style="font-size: 13px; color: var(--text-secondary); margin-top: 2px;">${message}</p>
                </div>
                <span class="clinical-badge badge-safe">${active.length} active</span>
            </div>
        `;
    }

    function populateRefillMedicationSelect(selectedMedicineId = null) {
        const select = document.getElementById('refill-med-select');
        const submitBtn = document.getElementById('submit-refill-btn');
        if (!select) return;

        const active = patientMedicinesCache.filter(m => m.isActive !== false);
        if (active.length === 0) {
            select.innerHTML = '<option value="">Add an active medicine before requesting a refill</option>';
            select.disabled = true;
            if (submitBtn) submitBtn.disabled = true;
            updateRefillPrice();
            return;
        }

        select.disabled = false;
        if (submitBtn) submitBtn.disabled = false;
        select.innerHTML = active.map(medicine => {
            const selected = selectedMedicineId && Number(selectedMedicineId) === Number(medicine.medicineId) ? ' selected' : '';
            const price = Number(medicine.price || 0);
            return `<option value="${medicine.medicineId}" data-name="${escapeHtml(medicine.medicineName)}" data-price="${price}"${selected}>${escapeHtml(getMedicineLabel(medicine))} - ${formatCurrency(price)}/strip</option>`;
        }).join('');
        updateRefillPrice();
    }

    function populateMedicineCatalogSelect() {
        const select = document.getElementById('modal-medicine-id');
        if (!select) return;

        if (medicineCatalogCache.length === 0) {
            select.innerHTML = '<option value="">No pharmacy catalog items available</option>';
            select.disabled = true;
            return;
        }

        select.disabled = false;
        select.innerHTML = medicineCatalogCache.map(medicine => {
            const manufacturer = medicine.manufacturer ? ` (${medicine.manufacturer})` : '';
            const price = Number(medicine.price || 0);
            const stock = Number(medicine.stockQuantity || 0);
            return `<option value="${medicine.medicineId}">${escapeHtml(medicine.name + manufacturer)} - ${formatCurrency(price)} · Stock ${stock}</option>`;
        }).join('');
    }

    async function loadMedicineCatalog() {
        const res = await fetchApi('/patient/medicines/catalog');
        medicineCatalogCache = res && res.success ? (res.data || []) : [];
        populateMedicineCatalogSelect();
    }

    function updateDashboardSummary() {
        const active = patientMedicinesCache.filter(m => m.isActive !== false);
        const pendingRefills = refillRequestCache.filter(r => r.status === 'PENDING').length;
        const verifiedPrescriptions = prescriptionCache.filter(p => p.status === 'VERIFIED').length;
        const adherenceText = document.getElementById('adherence-score')?.textContent || '0%';
        const nextReminder = reminderCache[0];

        setText('active-medicines-count', active.length);
        setText('active-medicines-note', active.length === 1 ? '1 medicine in your profile' : `${active.length} medicines in your profile`);
        setText('care-adherence-chip', `Adherence ${adherenceText}`);
        setText('care-medicine-chip', `${active.length} active medicine${active.length === 1 ? '' : 's'}`);
        setText('care-prescription-chip', `${verifiedPrescriptions} verified prescription${verifiedPrescriptions === 1 ? '' : 's'}`);

        if (nextReminder) {
            setText('next-priority-name', nextReminder.medicineName || 'Scheduled medicine');
            setText('next-priority-instructions', `Due at ${nextReminder.dueTime || 'today'}`);
            setText('insight-dose-pattern', 'Action pending today');
        } else if (active.length > 0) {
            setText('next-priority-name', active[0].medicineName);
            setText('next-priority-instructions', active[0].instructions || 'No reminder scheduled yet');
            setText('insight-dose-pattern', 'No open dose reminders');
        } else {
            setText('next-priority-name', 'No medicine scheduled');
            setText('next-priority-instructions', 'Add a medicine to build this patient profile.');
            setText('insight-dose-pattern', 'No logs yet');
        }

        setText('next-refill-value', pendingRefills > 0 ? pendingRefills : '--');
        setText('next-refill-note', pendingRefills > 0 ? `${pendingRefills} pending refill request${pendingRefills === 1 ? '' : 's'}` : 'No pending refill requests');
        setText('profile-next-refill-value', pendingRefills > 0 ? `${pendingRefills} pending` : 'No estimate');
        setText('insight-prescription-status', prescriptionCache.length > 0 ? `${verifiedPrescriptions}/${prescriptionCache.length} verified` : 'None uploaded');
        setText('insight-refill-status', pendingRefills > 0 ? `${pendingRefills} pending` : 'No pending requests');
        renderInteractionOverview();
    }

    async function loadRefillPharmacists() {
        const select = document.getElementById('refill-pharmacist-select');
        if (!select) return;

        const res = await fetchApi('/patient/refills/pharmacists');
        if (!res || !res.success || !res.data || res.data.length === 0) {
            pharmacistDirectory = [];
            select.innerHTML = '<option value="">No pharmacy destination available</option>';
            select.disabled = true;
            return;
        }

        pharmacistDirectory = res.data;
        select.disabled = false;
        select.innerHTML = pharmacistDirectory.map(pharmacist => {
            const label = formatPharmacistLabel(pharmacist);
            const license = pharmacist.licenseNumber ? ` · License ${pharmacist.licenseNumber}` : '';
            return `<option value="${pharmacist.pharmacistId}" data-name="${escapeHtml(label)}">${escapeHtml(label + license)}</option>`;
        }).join('');
    }

    // 2. Logout Handler
    document.getElementById('logout-btn').addEventListener('click', async (e) => {
        e.preventDefault();
        await fetchApi('/auth/logout', { method: 'POST' });
        window.location.href = '../index.html';
    });

    document.getElementById('back-home-btn').addEventListener('click', async (e) => {
        e.preventDefault();
        await fetchApi('/auth/logout', { method: 'POST' });
        window.location.href = '../index.html#portal';
    });

    // 3. Sidebar Tab Switcher
    const navLinks = document.querySelectorAll('.sidebar-link');
    const sections = document.querySelectorAll('.view-section');

    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('data-target');
            if (!targetId) return;

            navLinks.forEach(l => l.classList.remove('active'));
            sections.forEach(s => s.classList.remove('active'));

            link.classList.add('active');
            const targetSec = document.getElementById(targetId);
            if (targetSec) targetSec.classList.add('active');
        });
    });

    // 4. Loaders

    // Profile Loader & Emergency Health ID
    async function loadProfile() {
        const res = await fetchApi('/patients/me');
        const container = document.getElementById('profile-content');
        if (res && res.success) {
            const p = res.data;
            const fullName = getProfileName(p);
            setText('user-greeting', `Hello, ${fullName}`);
            container.innerHTML = `
                <div style="font-size: 1.1rem; margin-bottom: 6px;"><strong>${escapeHtml(fullName)}</strong></div>
                <div style="color: var(--text-secondary); margin-bottom: 4px; font-size: 13px;">📅 DOB: ${escapeHtml(p.dateOfBirth || 'Not added')}</div>
                <div style="color: var(--text-secondary); margin-bottom: 4px; font-size: 13px;">📞 Contact: ${escapeHtml(p.contactNumber || 'Not added')}</div>
            `;
            if (document.getElementById('set-name')) document.getElementById('set-name').value = fullName;
            if (document.getElementById('set-phone')) document.getElementById('set-phone').value = p.contactNumber || '';

            // Sync Emergency Health Card
            if (document.getElementById('med-id-name')) document.getElementById('med-id-name').textContent = fullName;
            if (document.getElementById('med-id-dob')) document.getElementById('med-id-dob').textContent = p.dateOfBirth || 'Not added';
        } else {
            setText('user-greeting', 'Hello, Patient');
            container.innerHTML = `<div class="empty-state"><p style="color:var(--danger)">Failed to load profile.</p></div>`;
        }
    }

    // Reminders Loader with Clinical Food/Timing Guidelines
    async function loadReminders() {
        const res = await fetchApi('/patient/reminders');
        const container = document.getElementById('reminders-content');
        if (res && res.success) {
            reminderCache = res.data || [];
            updateDashboardSummary();
            if (res.data.length === 0) {
                container.innerHTML = `<div class="empty-state" style="padding: 24px;"><div class="icon">✅</div><p>You are all caught up on all doses for today!</p></div>`;
                return;
            }
            let html = '';
            res.data.forEach(r => {
                // Determine food/timing guideline based on medicine
                let foodGuideline = '🍽️ Take After Food';
                if (r.medicineName.toLowerCase().includes('omeprazole') || r.medicineName.toLowerCase().includes('levo')) {
                    foodGuideline = '⏳ Take 30m Before Breakfast (Empty Stomach)';
                } else if (r.medicineName.toLowerCase().includes('amoxicillin')) {
                    foodGuideline = '🍽️ Take with full meal & glass of water';
                }

                html += `
                    <div class="reminder-card" style="border-left: 4px solid var(--warning); background: var(--bg-secondary); padding: 16px; margin-bottom: 12px; border-radius: 8px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                        <div>
                            <div style="display: flex; align-items: center; gap: 8px;">
                                <h4 style="margin: 0; color: var(--text-primary); font-size: 15px;">${r.medicineName}</h4>
                                <span class="food-timing-tag">${foodGuideline}</span>
                            </div>
                            <p style="margin: 4px 0 0 0; color: var(--text-secondary); font-size: 13px;">
                                Due today at <strong>${r.dueTime || 'scheduled time'}</strong>
                            </p>
                        </div>
                        <div style="display: flex; gap: 8px;">
                            <button class="btn btn-primary" style="padding: 6px 14px; font-size: 13px;" onclick="markReminder(${r.scheduleId}, 'TAKEN')">Take Dose</button>
                            <button class="btn" style="background:var(--danger-soft); color:var(--danger); padding: 6px 14px; font-size: 13px;" onclick="markReminder(${r.scheduleId}, 'SKIPPED')">Skip</button>
                        </div>
                    </div>
                `;
            });
            container.innerHTML = html;
        } else {
            reminderCache = [];
            updateDashboardSummary();
            container.innerHTML = `<p style="color:var(--danger)">Failed to load reminders.</p>`;
        }
    }

    // Active Medications Loader
    async function loadMedications() {
        const res = await fetchApi('/patient/medicines');
        const container = document.getElementById('medications-content');
        if (res && res.success) {
            patientMedicinesCache = res.data || [];
            populateRefillMedicationSelect();
            updateDashboardSummary();
            if (res.data.length === 0) {
                container.innerHTML = `<div class="empty-state"><div class="icon">💊</div><p>No active medications.</p></div>`;
                return;
            }
            let html = `<div class="table-responsive"><table>
                <thead>
                    <tr>
                        <th>Medicine</th>
                        <th>Dosage</th>
                        <th>Food & Timing</th>
                        <th>Clinical Status</th>
                        <th>Refill Action</th>
                    </tr>
                </thead>
                <tbody>`;
            res.data.forEach(m => {
                const statusBadge = m.isActive ? '<span class="badge success">ACTIVE</span>' : '<span class="badge warning">INACTIVE</span>';
                html += `
                    <tr>
                        <td>
                            <strong>${escapeHtml(m.medicineName)}</strong>
                            <div style="font-size: 11px; color: var(--text-secondary);">${escapeHtml(m.manufacturer || 'Pharmacy catalog')}</div>
                        </td>
                        <td>${escapeHtml(m.dosage || 'Not set')}</td>
                        <td>${escapeHtml(m.instructions || 'No instruction added')}</td>
                        <td>${statusBadge}</td>
                        <td>
                            <button class="btn" style="padding: 4px 10px; font-size: 12px; background: var(--primary-soft); color: var(--primary-color); border: 1px solid var(--primary-color);" onclick="openRefillModal(${m.medicineId})">
                                🛒 Refill (₹)
                            </button>
                        </td>
                    </tr>
                `;
            });
            html += `</tbody></table></div>`;
            container.innerHTML = html;
        } else {
            patientMedicinesCache = [];
            populateRefillMedicationSelect();
            updateDashboardSummary();
            container.innerHTML = `<p style="color:var(--danger)">Failed to load medications.</p>`;
        }
    }

    // Prescriptions Loader
    async function loadPrescriptions() {
        const res = await fetchApi('/patient/prescriptions');
        const container = document.getElementById('prescriptions-content');
        if (res && res.success) {
            prescriptionCache = res.data || [];
            updateDashboardSummary();
            if (res.data.length === 0) {
                container.innerHTML = `<div class="empty-state"><div class="icon">📄</div><p>No prescriptions uploaded.</p></div>`;
                return;
            }
            let html = `<div class="table-responsive"><table>
                <thead>
                    <tr>
                        <th>Upload Date</th>
                        <th>Prescription Document</th>
                        <th>Clinical Status</th>
                        <th>Pharmacist Verification Notes</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>`;
            res.data.forEach(p => {
                let badgeClass = 'warning';
                if (p.status === 'VERIFIED') badgeClass = 'success';
                if (p.status === 'REJECTED') badgeClass = 'danger';

                html += `
                    <tr>
                        <td>${new Date(p.uploadDate).toLocaleDateString()}</td>
                        <td><a href="${window.BACKEND_DOMAIN}${p.filePath}" target="_blank" style="color: var(--primary-color); font-weight: 600;">View Medical PDF</a></td>
                        <td><span class="badge ${badgeClass}">${p.status}</span></td>
                        <td style="color: var(--text-secondary); font-size: 13px;">${escapeHtml(p.notes || 'Waiting for pharmacist notes')}</td>
                        <td>
                            <button class="btn btn-primary" style="padding: 4px 10px; font-size: 12px;" onclick="openRefillModal()">
                                🛒 Order Refill (₹)
                            </button>
                        </td>
                    </tr>
                `;
            });
            html += `</tbody></table></div>`;
            container.innerHTML = html;
        } else {
            prescriptionCache = [];
            updateDashboardSummary();
            container.innerHTML = `<p style="color:var(--danger)">Failed to load prescriptions.</p>`;
        }
    }

    async function loadRefillRequests() {
        const container = document.getElementById('patient-refill-requests-content');
        if (!container) return;

        const res = await fetchApi('/patient/refills');
        if (!res || !res.success) {
            refillRequestCache = [];
            updateDashboardSummary();
            container.innerHTML = `<div class="empty-state"><div class="icon">⚠️</div><p>Unable to load refill requests.</p></div>`;
            return;
        }

        const requests = res.data || [];
        refillRequestCache = requests;
        updateDashboardSummary();
        if (requests.length === 0) {
            container.innerHTML = `<div class="empty-state"><div class="icon">🧾</div><p>No refill requests yet.</p></div>`;
            return;
        }

        let html = `<table>
            <thead>
                <tr>
                    <th>Requested</th>
                    <th>Medicine</th>
                    <th>Quantity</th>
                    <th>Pharmacy</th>
                    <th>Method</th>
                    <th>Total</th>
                    <th>Status</th>
                    <th>Pharmacist Notes</th>
                </tr>
            </thead>
            <tbody>`;

        requests.forEach(request => {
            const pharmacistName = request.requestedPharmacistName || 'Assigned pharmacy';
            const pharmacistHandle = request.requestedPharmacistHandle ? `@${request.requestedPharmacistHandle}` : '';
            html += `<tr>
                <td>${formatDateTime(request.requestedAt)}</td>
                <td><strong>${escapeHtml(request.medicineName)}</strong></td>
                <td>${request.quantity}</td>
                <td>
                    <strong>${escapeHtml(pharmacistName)}</strong>
                    <div style="font-size: 11px; color: var(--text-secondary);">${escapeHtml(pharmacistHandle)}</div>
                </td>
                <td>${escapeHtml(request.fulfillmentMethod)}</td>
                <td><strong>${formatCurrency(request.estimatedTotal)}</strong></td>
                <td><span class="badge ${refillBadgeClass(request.status)}">${escapeHtml(request.status)}</span></td>
                <td style="color: var(--text-secondary); font-size: 13px;">${escapeHtml(request.pharmacistNotes || 'Waiting for pharmacist review')}</td>
            </tr>`;
        });

        html += `</tbody></table>`;
        container.innerHTML = html;
    }

    // Schedule & Adherence Logs
    async function loadScheduleAndAdherence() {
        const adRes = await fetchApi('/patient/adherence');
        if (adRes && adRes.success) {
            document.getElementById('adherence-score').textContent = formatPercent(adRes.data.adherencePercentage);
            updateDashboardSummary();
        }

        const histRes = await fetchApi('/patient/history');
        const historyContainer = document.getElementById('schedule-history-content');
        if (historyContainer) {
            if (histRes && histRes.success && histRes.data.length > 0) {
                let html = `<div class="table-responsive"><table>
                    <thead><tr><th>Date & Time</th><th>Medication</th><th>Food Timing</th><th>Adherence Status</th></tr></thead><tbody>`;
                histRes.data.forEach(h => {
                    let bClass = h.status === 'TAKEN' ? 'success' : (h.status === 'SKIPPED' ? 'warning' : 'danger');
                    html += `<tr>
                        <td>${new Date(h.logTime).toLocaleString()}</td>
                        <td><strong>${h.medicineName}</strong></td>
                        <td><span class="food-timing-tag">🍽️ After Meal</span></td>
                        <td><span class="badge ${bClass}">${h.status}</span></td>
                    </tr>`;
                });
                html += `</tbody></table></div>`;
                historyContainer.innerHTML = html;
            } else {
                historyContainer.innerHTML = `<div class="empty-state"><div class="icon">📅</div><p>No dose history logged yet.</p></div>`;
            }
        }
    }

    // Upload Prescription Form
    const uploadForm = document.getElementById('upload-prescription-form');
    if (uploadForm) {
        uploadForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const alertBox = document.getElementById('upload-alert');
            alertBox.style.display = 'none';

            const fileInput = document.getElementById('prescriptionFile');
            const notesInput = document.getElementById('prescriptionNotes');

            if (!fileInput.files || fileInput.files.length === 0) {
                alertBox.textContent = 'Please select a file to upload.';
                alertBox.className = 'alert error';
                alertBox.style.display = 'block';
                return;
            }

            const formData = new FormData();
            formData.append('file', fileInput.files[0]);
            formData.append('notes', notesInput.value || '');

            const submitBtn = uploadForm.querySelector('button[type="submit"]');
            submitBtn.innerText = 'Uploading...';
            submitBtn.disabled = true;

            try {
                const response = await fetch(`${window.BACKEND_DOMAIN}/api/v1/patient/prescriptions`, {
                    method: 'POST',
                    body: formData,
                    credentials: 'include'
                });

                const res = await response.json();
                submitBtn.innerText = 'Upload Document';
                submitBtn.disabled = false;

                if (res && res.success) {
                    alertBox.textContent = 'Prescription uploaded successfully! Sent to pharmacy queue for clinical verification.';
                    alertBox.className = 'alert success';
                    alertBox.style.display = 'block';
                    uploadForm.reset();
                    loadPrescriptions();
                } else {
                    alertBox.textContent = res ? res.message : 'Upload failed.';
                    alertBox.className = 'alert error';
                    alertBox.style.display = 'block';
                }
            } catch (err) {
                submitBtn.innerText = 'Upload Document';
                submitBtn.disabled = false;
                alertBox.textContent = 'Network error while uploading.';
                alertBox.className = 'alert error';
                alertBox.style.display = 'block';
            }
        });
    }

    // Dose Logging
    window.markReminder = async function(scheduleId, status) {
        const endpoint = status === 'TAKEN' ? `/patient/reminders/${scheduleId}/taken` : `/patient/reminders/${scheduleId}/skipped`;
        const res = await fetchApi(endpoint, { method: 'POST' });
        if (res && res.success) {
            window.showToast(`Dose marked as ${status.toLowerCase()}! Adherence streak updated.`, status === 'TAKEN' ? 'success' : 'warning');
            loadReminders();
            loadScheduleAndAdherence();
        } else {
            window.showToast(res ? res.message : 'Failed to update reminder.', 'error');
        }
    };

    // Add Medicine Modal Logic
    window.openAddMedicineModal = function() {
        document.getElementById('addMedicineModal').classList.add('active');
        if (medicineCatalogCache.length === 0) {
            loadMedicineCatalog();
        }
    };
    window.closeAddMedicineModal = function() {
        document.getElementById('addMedicineModal').classList.remove('active');
    };

    const addMedForm = document.getElementById('add-medicine-form');
    if (addMedForm) {
        addMedForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const medId = document.getElementById('modal-medicine-id').value;
            const dosage = document.getElementById('modal-dosage').value;
            const instructions = document.getElementById('modal-instructions').value;

            if (!medId) {
                window.showToast('Please choose a medicine from the catalog.', 'warning');
                return;
            }

            try {
                const res = await fetchApi('/patient/medicines', {
                    method: 'POST',
                    body: JSON.stringify({
                        medicineId: parseInt(medId),
                        dosage: dosage,
                        instructions: instructions
                    })
                });

                if (res && res.success) {
                    window.showToast('Medication added successfully to your active profile!', 'success');
                    closeAddMedicineModal();
                    loadMedications();
                } else {
                    window.showToast(res ? res.message : 'Failed to add medicine.', 'error');
                }
            } catch (err) {
                window.showToast('Network error while saving medicine.', 'error');
            }
        });
    }

    // Refill Modal Logic (in INR ₹)
    window.openRefillModal = function(selectedMedicineId = null) {
        document.getElementById('refillModal').classList.add('active');
        populateRefillMedicationSelect(selectedMedicineId);
        loadRefillPharmacists();
    };
    window.closeRefillModal = function() {
        document.getElementById('refillModal').classList.remove('active');
    };

    window.updateRefillPrice = function() {
        const select = document.getElementById('refill-med-select');
        const methodSelect = document.getElementById('refill-method');
        const selectedOption = select && select.options[select.selectedIndex];
        const unitPrice = selectedOption ? parseFloat(selectedOption.getAttribute('data-price') || '0') : 0;
        const qty = parseInt(document.getElementById('refill-qty').value) || 1;
        const medicinesTotal = unitPrice * qty;
        const deliveryFee = methodSelect && methodSelect.value === 'Delivery' ? 40 : 0;
        const total = medicinesTotal + deliveryFee;
        const gst = (total * 0.12);

        document.getElementById('refill-base-price').textContent = formatCurrency(medicinesTotal);
        document.getElementById('refill-gst').textContent = formatCurrency(gst);
        const deliveryFeeEl = document.getElementById('refill-delivery-fee');
        if (deliveryFeeEl) deliveryFeeEl.textContent = formatCurrency(deliveryFee);
        document.getElementById('refill-total-price').textContent = formatCurrency(total);
    };

    window.handleRefillOrder = async function(e) {
        e.preventDefault();
        const select = document.getElementById('refill-med-select');
        const selectedOption = select.options[select.selectedIndex];
        const medName = selectedOption ? selectedOption.getAttribute('data-name') : '';
        const qty = Number(document.getElementById('refill-qty').value);
        const method = document.getElementById('refill-method').value;
        const pharmacistSelect = document.getElementById('refill-pharmacist-select');
        const pharmacistId = Number(pharmacistSelect ? pharmacistSelect.value : 0);
        const pharmacistOption = pharmacistSelect ? pharmacistSelect.options[pharmacistSelect.selectedIndex] : null;
        const pharmacistName = pharmacistOption ? (pharmacistOption.getAttribute('data-name') || pharmacistOption.textContent) : 'selected pharmacist';
        const total = document.getElementById('refill-total-price').textContent;
        const totalAmount = Number(total.replace(/[^0-9.]/g, ''));
        const submitBtn = document.getElementById('submit-refill-btn');

        if (!medName) {
            window.showToast('Add an active medicine before requesting a refill.', 'error');
            return;
        }

        if (!pharmacistId) {
            window.showToast('Please choose the pharmacist or pharmacy to receive this refill request.', 'error');
            return;
        }

        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Submitting...';
        }

        const res = await fetchApi('/patient/refills', {
            method: 'POST',
            body: JSON.stringify({
                medicineName: medName,
                quantity: qty,
                fulfillmentMethod: method,
                estimatedTotal: totalAmount,
                pharmacistId
            })
        });

        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Submit Refill Request';
        }

        if (res && res.success) {
            closeRefillModal();
            window.showToast(`Refill request sent to ${pharmacistName}: ${qty} packs of ${medName} (${total}).`, 'success');
            loadRefillRequests();
        } else {
            window.showToast(res ? res.message : 'Failed to submit refill request.', 'error');
        }
    };

    // Clinical Drug Interaction Scanner
    window.runInteractionCheck = function() {
        const med1 = document.getElementById('interaction-med-1').value;
        const med2 = document.getElementById('interaction-med-2').value;
        const resultBox = document.getElementById('interaction-result-box');

        let severity = 'SAFE';
        let badgeHtml = '<span class="clinical-badge badge-safe">SAFE / NO DIRECT CONFLICT</span>';
        let explanation = `No severe pharmacokinetic or pharmacodynamic antagonism established between ${med1} and ${med2}. Maintain standard interval administration.`;
        let borderColor = 'var(--success)';
        let bgColor = 'var(--success-soft)';

        // High Risk Rules
        if ((med1.includes('Lisinopril') && med2.includes('Potassium')) || (med2.includes('Potassium') && med1.includes('Lisinopril'))) {
            severity = 'HIGH';
            badgeHtml = '<span class="clinical-badge badge-high-risk">CRITICAL CONTRAINDICATION (HIGH RISK)</span>';
            explanation = '<strong>Hyperkalemia Alert:</strong> Combining ACE Inhibitors (Lisinopril) with Potassium Supplements significantly impairs renal potassium excretion, risking dangerous cardiac arrhythmias and cardiac arrest.';
            borderColor = 'var(--danger)';
            bgColor = 'var(--danger-soft)';
        } else if ((med1.includes('Ibuprofen') && med2.includes('Aspirin')) || (med1.includes('Ibuprofen') && med2.includes('Warfarin'))) {
            severity = 'HIGH';
            badgeHtml = '<span class="clinical-badge badge-high-risk">HEMORRHAGIC WARNING (HIGH RISK)</span>';
            explanation = '<strong>Severe Bleeding Risk:</strong> NSAIDs (Ibuprofen) competitively inhibit platelet COX-1 and cause gastrointestinal mucosal erosion when paired with anticoagulants or Aspirin.';
            borderColor = 'var(--danger)';
            bgColor = 'var(--danger-soft)';
        } else if (med1.includes('Metformin') && med2.includes('Alcohol')) {
            severity = 'HIGH';
            badgeHtml = '<span class="clinical-badge badge-high-risk">METABOLIC TOXICITY (HIGH RISK)</span>';
            explanation = '<strong>Lactic Acidosis Risk:</strong> Alcohol potentiates the effect of Metformin on lactate metabolism, dramatically increasing the hazard of life-threatening lactic acidosis.';
            borderColor = 'var(--danger)';
            bgColor = 'var(--danger-soft)';
        } else if (med1.includes('Amoxicillin') && med2.includes('Methotrexate')) {
            severity = 'MODERATE';
            badgeHtml = '<span class="clinical-badge badge-moderate-risk">MODERATE CLINICAL PRECAUTION</span>';
            explanation = '<strong>Renal Excretion Interference:</strong> Penicillin antibiotics may decrease the renal tubular clearance of Methotrexate, elevating blood methotrexate concentrations.';
            borderColor = 'var(--warning)';
            bgColor = 'var(--warning-soft)';
        } else if (med1.includes('Paracetamol') && med2.includes('Alcohol')) {
            severity = 'MODERATE';
            badgeHtml = '<span class="clinical-badge badge-moderate-risk">HEPATOTOXIC PRECAUTION</span>';
            explanation = '<strong>Hepatic Load:</strong> Concomitant chronic alcohol consumption and Paracetamol depletes glutathione stores, increasing the generation of the toxic metabolite NAPQI.';
            borderColor = 'var(--warning)';
            bgColor = 'var(--warning-soft)';
        }

        resultBox.style.display = 'block';
        resultBox.style.borderColor = borderColor;
        resultBox.style.backgroundColor = bgColor;
        resultBox.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                <strong>${med1} + ${med2}</strong>
                ${badgeHtml}
            </div>
            <p style="margin:0; font-size:13px; line-height:1.5; color:var(--text-primary);">${explanation}</p>
        `;
    };

    // Toast Helper
    window.showToast = function(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (container) {
            const toast = document.createElement('div');
            toast.className = `toast ${type}`;
            toast.innerHTML = `<span>${message}</span>`;
            container.appendChild(toast);
            setTimeout(() => {
                toast.classList.add('fade-out');
                setTimeout(() => toast.remove(), 300);
            }, 3500);
        }
    };

    // Initial Loaders
    loadProfile();
    loadReminders();
    loadMedications();
    loadPrescriptions();
    loadRefillRequests();
    loadRefillPharmacists();
    loadMedicineCatalog();
    loadScheduleAndAdherence();
});
