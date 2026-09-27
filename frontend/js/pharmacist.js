
document.addEventListener('DOMContentLoaded', async () => {
    const setText = (id, value) => {
        const el = document.getElementById(id);
        if (el) el.textContent = value;
    };
    let inventoryCache = [];

    const meRes = await fetchApi('/auth/me');
    if (!meRes || !meRes.success || meRes.data.role !== 'PHARMACIST') {
        window.location.href = '../index.html';
        return;
    }
    setText('user-greeting', 'Loading profile...');

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

    const addStockModal = document.getElementById('addStockModal');
    const addStockForm = document.getElementById('add-stock-form');
    const stockMedicineSelect = document.getElementById('stock-medicine-id');
    const stockNameInput = document.getElementById('stock-name');
    const stockManufacturerInput = document.getElementById('stock-manufacturer');
    const stockDescriptionInput = document.getElementById('stock-description');
    const stockExpiryInput = document.getElementById('stock-expiry-date');
    const addStockAlert = document.getElementById('add-stock-alert');
    const stockQuantityInput = document.getElementById('stock-quantity');
    const stockUnitPriceInput = document.getElementById('stock-unit-price');
    const stockBatchInput = document.getElementById('stock-batch-number');
    const editMedicineModal = document.getElementById('editMedicineModal');
    const editMedicineForm = document.getElementById('edit-medicine-form');
    const editMedicineNameInput = document.getElementById('edit-medicine-name');
    const editMedicineManufacturerInput = document.getElementById('edit-medicine-manufacturer');
    const editMedicineDescriptionInput = document.getElementById('edit-medicine-description');
    const editMedicineStockInput = document.getElementById('edit-medicine-stock');
    const editMedicinePriceInput = document.getElementById('edit-medicine-price');
    const editMedicineAlert = document.getElementById('edit-medicine-alert');
    let activeEditMedicineId = null;

    const tomorrow = new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    const nextYear = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    if (stockExpiryInput) stockExpiryInput.min = tomorrow;

    function formatCurrency(value) {
        return `₹${Number(value || 0).toLocaleString('en-IN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        })}`;
    }

    function escapeHtml(value) {
        const div = document.createElement('div');
        div.textContent = value == null ? '' : String(value);
        return div.innerHTML;
    }

    function getProfileName(profile, fallback = 'Pharmacy Operator') {
        const name = [profile && profile.firstName, profile && profile.lastName]
            .filter(Boolean)
            .join(' ')
            .trim();
        return name || fallback;
    }

    function refillBadgeClass(status) {
        if (status === 'APPROVED' || status === 'COMPLETED') return 'success';
        if (status === 'REJECTED') return 'danger';
        return 'warning';
    }

    function formatDateTime(value) {
        return value ? new Date(value).toLocaleString() : 'Just now';
    }

    function updateStockPreview() {
        const selected = inventoryCache.find(m => String(m.medicineId) === (stockMedicineSelect ? stockMedicineSelect.value : ''));
        const medicineName = selected ? selected.name : (stockNameInput.value.trim() || 'New medicine');
        const quantity = Number(stockQuantityInput ? stockQuantityInput.value : 0) || 0;
        const unitPrice = Number(stockUnitPriceInput ? stockUnitPriceInput.value : 0) || 0;
        const batchNumber = stockBatchInput ? stockBatchInput.value.trim() : '';
        const expiryDate = stockExpiryInput ? stockExpiryInput.value : '';

        setText('stock-summary-medicine', medicineName);
        setText('stock-summary-batch', batchNumber || 'Not entered');
        setText('stock-summary-expiry', expiryDate || 'Select date');
        setText('stock-summary-quantity', `${quantity || 0} unit${quantity === 1 ? '' : 's'}`);
        setText('stock-summary-price', formatCurrency(unitPrice));
        setText('stock-summary-total', formatCurrency(quantity * unitPrice));
        setText('stock-mode-pill', selected ? 'Existing Medicine' : 'New Medicine');
    }

    function updateEditPreview() {
        const stock = Number(editMedicineStockInput ? editMedicineStockInput.value : 0) || 0;
        const price = Number(editMedicinePriceInput ? editMedicinePriceInput.value : 0) || 0;
        const name = editMedicineNameInput ? editMedicineNameInput.value.trim() : '';
        const manufacturer = editMedicineManufacturerInput ? editMedicineManufacturerInput.value.trim() : '';

        setText('edit-summary-name', name || 'Medicine');
        setText('edit-summary-manufacturer', manufacturer || 'No manufacturer entered');
        setText('edit-summary-stock', `${stock} unit${stock === 1 ? '' : 's'}`);
        setText('edit-summary-price', formatCurrency(price));
        setText('edit-summary-status', stock <= 0 ? 'Out of stock' : (stock <= 10 ? 'Low stock' : 'In stock'));
    }

    function setAddStockExistingMode() {
        const usingExisting = Boolean(stockMedicineSelect && stockMedicineSelect.value);
        stockNameInput.required = !usingExisting;
        stockNameInput.disabled = usingExisting;
        stockManufacturerInput.disabled = usingExisting;
        stockDescriptionInput.disabled = usingExisting;

        if (usingExisting) {
            const selected = inventoryCache.find(m => String(m.medicineId) === stockMedicineSelect.value);
            stockNameInput.value = selected ? selected.name : '';
            stockManufacturerInput.value = selected ? selected.manufacturer || '' : '';
            stockDescriptionInput.value = selected ? selected.description || '' : '';
        } else {
            stockNameInput.value = '';
            stockManufacturerInput.value = '';
            stockDescriptionInput.value = '';
        }
        updateStockPreview();
    }

    window.openAddStockModal = function() {
        addStockForm.reset();
        addStockAlert.className = 'alert';
        addStockAlert.textContent = '';
        if (stockExpiryInput) stockExpiryInput.value = nextYear;
        if (stockQuantityInput) stockQuantityInput.value = '50';
        if (stockUnitPriceInput) stockUnitPriceInput.value = '100.00';
        setAddStockExistingMode();
        document.body.classList.add('modal-open');
        addStockModal.classList.add('active');
    };

    window.closeAddStockModal = function() {
        addStockModal.classList.remove('active');
        document.body.classList.remove('modal-open');
    };

    window.openEditMedicineModal = function(medicineId) {
        const medicine = inventoryCache.find(m => Number(m.medicineId) === Number(medicineId));
        if (!medicine) {
            window.showToast('Medicine not found in the loaded inventory list.', 'error');
            return;
        }

        activeEditMedicineId = medicine.medicineId;
        editMedicineForm.reset();
        editMedicineAlert.className = 'alert';
        editMedicineAlert.textContent = '';
        editMedicineNameInput.value = medicine.name || '';
        editMedicineManufacturerInput.value = medicine.manufacturer || '';
        editMedicineDescriptionInput.value = medicine.description || '';
        editMedicineStockInput.value = Number(medicine.stockQuantity || 0);
        editMedicinePriceInput.value = Number(medicine.price || 0).toFixed(2);
        updateEditPreview();
        document.body.classList.add('modal-open');
        editMedicineModal.classList.add('active');
    };

    window.closeEditMedicineModal = function() {
        editMedicineModal.classList.remove('active');
        document.body.classList.remove('modal-open');
        activeEditMedicineId = null;
    };

    if (addStockModal) {
        addStockModal.addEventListener('click', (event) => {
            if (event.target === addStockModal) {
                closeAddStockModal();
            }
        });
    }

    if (editMedicineModal) {
        editMedicineModal.addEventListener('click', (event) => {
            if (event.target === editMedicineModal) {
                closeEditMedicineModal();
            }
        });
    }

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && addStockModal.classList.contains('active')) {
            closeAddStockModal();
        }
        if (event.key === 'Escape' && editMedicineModal.classList.contains('active')) {
            closeEditMedicineModal();
        }
    });

    if (stockMedicineSelect) {
        stockMedicineSelect.addEventListener('change', setAddStockExistingMode);
    }

    [
        stockNameInput,
        stockManufacturerInput,
        stockDescriptionInput,
        stockExpiryInput,
        stockQuantityInput,
        stockUnitPriceInput,
        stockBatchInput
    ].forEach(input => {
        if (input) input.addEventListener('input', updateStockPreview);
    });

    [
        editMedicineNameInput,
        editMedicineManufacturerInput,
        editMedicineStockInput,
        editMedicinePriceInput
    ].forEach(input => {
        if (input) input.addEventListener('input', updateEditPreview);
    });

    if (addStockForm) {
        addStockForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const medicineId = stockMedicineSelect.value ? Number(stockMedicineSelect.value) : null;
            const payload = {
                medicineId,
                name: medicineId ? null : stockNameInput.value.trim(),
                manufacturer: medicineId ? null : stockManufacturerInput.value.trim(),
                description: medicineId ? null : stockDescriptionInput.value.trim(),
                batchNumber: stockBatchInput.value.trim(),
                expiryDate: stockExpiryInput.value,
                quantityInStock: Number(stockQuantityInput.value),
                unitPrice: Number(stockUnitPriceInput.value)
            };

            const submitBtn = addStockForm.querySelector('button[type="submit"]');
            const originalText = submitBtn.textContent;
            submitBtn.disabled = true;
            submitBtn.textContent = 'Saving...';

            const res = await fetchApi('/pharmacist/inventory/stock', {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            submitBtn.disabled = false;
            submitBtn.textContent = originalText;

            if (res && res.success) {
                addStockAlert.textContent = 'Stock batch recorded successfully.';
                addStockAlert.className = 'alert success';
                window.showToast('Stock batch added to inventory.', 'success');
                await loadInventory();
                await loadMetrics();
                closeAddStockModal();
            } else {
                addStockAlert.textContent = res ? res.message : 'Failed to add stock.';
                addStockAlert.className = 'alert error';
            }
        });
    }

    if (editMedicineForm) {
        editMedicineForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            if (!activeEditMedicineId) {
                window.showToast('Choose a medicine before saving.', 'warning');
                return;
            }

            const payload = {
                name: editMedicineNameInput.value.trim(),
                manufacturer: editMedicineManufacturerInput.value.trim(),
                description: editMedicineDescriptionInput.value.trim(),
                stockQuantity: Number(editMedicineStockInput.value),
                price: Number(editMedicinePriceInput.value),
                requiresPrescription: false
            };

            const submitBtn = editMedicineForm.querySelector('button[type="submit"]');
            const originalText = submitBtn.textContent;
            submitBtn.disabled = true;
            submitBtn.textContent = 'Saving...';

            const res = await fetchApi(`/pharmacist/inventory/${activeEditMedicineId}`, {
                method: 'PUT',
                body: JSON.stringify(payload)
            });

            submitBtn.disabled = false;
            submitBtn.textContent = originalText;

            if (res && res.success) {
                editMedicineAlert.textContent = 'Medicine updated successfully.';
                editMedicineAlert.className = 'alert success';
                window.showToast('Medicine stock updated.', 'success');
                await loadInventory();
                await loadMetrics();
                closeEditMedicineModal();
            } else {
                editMedicineAlert.textContent = res ? res.message : 'Failed to update medicine.';
                editMedicineAlert.className = 'alert error';
            }
        });
    }

    async function loadMetrics() {
        const res = await fetchApi('/pharmacist/reports/dashboard');
        if (res && res.success) {
            setText('low-stock-count', res.data.lowStockMedicines ? res.data.lowStockMedicines.length : '0');
            setText('total-sales-count', res.data.totalSalesCount || '0');
            setText('total-revenue-value', `₹${res.data.totalRevenue ? res.data.totalRevenue.toFixed(2) : '0.00'}`);
        } else {
            setText('total-sales-count', '0');
            setText('total-revenue-value', '₹0.00');
        }
    }

    async function loadProfile() {
        const res = await fetchApi('/pharmacist/profile');
        const container = document.getElementById('profile-content');
        if (res && res.success) {
            const p = res.data;
            const fullName = getProfileName(p);
            const publicHandle = p.publicHandle ? `@${p.publicHandle}` : 'Not assigned';
            setText('user-greeting', fullName);
            container.innerHTML = `
                <div style="font-size: 1.1rem; margin-bottom: 12px;"><strong>${escapeHtml(fullName)}</strong></div>
                <div style="font-size: 12px; color: var(--primary-color); font-weight: 800; margin-bottom: 12px;">${escapeHtml(publicHandle)}</div>
                <div class="vitals-grid">
                    <div class="vital-item">
                        <div class="vital-label">License</div>
                        <div class="vital-value" style="font-size: 13px;">${escapeHtml(p.licenseNumber || 'N/A')}</div>
                    </div>
                    <div class="vital-item">
                        <div class="vital-label">Role</div>
                        <div class="vital-value" style="font-size: 13px; color: var(--success);">Verified</div>
                    </div>
                </div>
            `;
            document.getElementById('set-name').value = fullName;
            document.getElementById('set-license').value = p.licenseNumber || '';
            const handleField = document.getElementById('set-public-handle');
            if (handleField) handleField.value = publicHandle;
        } else {
            setText('user-greeting', 'Pharmacy Operator');
            container.innerHTML = `
                <div style="font-size: 1.1rem; margin-bottom: 12px;"><strong>Pharmacy Operator</strong></div>
                <div class="vitals-grid">
                    <div class="vital-item">
                        <div class="vital-label">License</div>
                        <div class="vital-value" style="font-size: 13px;">MEDISYNC-PH-001</div>
                    </div>
                    <div class="vital-item">
                        <div class="vital-label">Status</div>
                        <div class="vital-value" style="font-size: 13px; color: var(--warning);">Local demo</div>
                    </div>
                </div>
            `;
        }
    }

    async function loadInventory() {
        const res = await fetchApi('/pharmacist/inventory');
        const container = document.getElementById('inventory-content');
        const posSelect = document.getElementById('pos-medicine-select'); // if it exists
        const addStockSelect = document.getElementById('stock-medicine-id');
        
        let optionsHtml = '<option value="">-- Select Medicine --</option>';
        let addStockOptionsHtml = '<option value="">Create new medicine</option>';

        if (res && res.success) {
            inventoryCache = res.data;
            inventoryCache.forEach(m => {
                addStockOptionsHtml += `<option value="${m.medicineId}">${m.name} (${m.manufacturer || 'Unknown manufacturer'})</option>`;
            });
            if (addStockSelect) addStockSelect.innerHTML = addStockOptionsHtml;
            updateStockPreview();

            if (res.data.length === 0) {
                container.innerHTML = `<div class="empty-state"><div class="icon">📦</div><p>No inventory found.</p></div>`;
                return;
            }
            let html = `<div class="table-responsive"><table>
                <thead>
                    <tr>
                        <th>Medicine</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>`;
            
            res.data.forEach(m => {
                const isLow = m.stockQuantity <= 10;
                const statusBadge = isLow ? '<span class="badge danger">LOW STOCK</span>' : '<span class="badge success">IN STOCK</span>';
                const medicineName = escapeHtml(m.name);
                const manufacturer = escapeHtml(m.manufacturer || 'Unknown manufacturer');
                html += `
                    <tr data-inventory-row data-search="${escapeHtml(`${m.name} ${m.manufacturer || ''}`.toLowerCase())}">
                        <td><strong>${medicineName}</strong> <br><small style="color:var(--text-secondary)">${manufacturer}</small></td>
                        <td>₹${m.price.toFixed(2)}</td>
                        <td><span style="${isLow ? 'color:var(--danger);font-weight:700;' : ''}">${m.stockQuantity}</span></td>
                        <td>${statusBadge}</td>
                        <td><button class="btn btn-secondary table-action-btn" onclick="openEditMedicineModal(${m.medicineId})">Edit</button></td>
                    </tr>
                `;
                if(m.stockQuantity > 0) {
                    optionsHtml += `<option value="${m.medicineId}">${m.name} (₹${m.price.toFixed(2)}) - Stock: ${m.stockQuantity}</option>`;
                }
            });
            html += `</tbody></table></div>`;
            container.innerHTML = html;
            if(posSelect) posSelect.innerHTML = optionsHtml;

            const lowStockCount = res.data.filter(m => m.stockQuantity <= 10).length;
            setText('low-stock-count', lowStockCount);
        } else {
            container.innerHTML = `<p style="color:var(--danger)">Failed to load inventory.</p>`;
        }
    }

    const inventorySearch = document.getElementById('inventory-search');
    if (inventorySearch) {
        inventorySearch.addEventListener('input', () => {
            const query = inventorySearch.value.trim().toLowerCase();
            document.querySelectorAll('[data-inventory-row]').forEach(row => {
                row.style.display = row.dataset.search.includes(query) ? '' : 'none';
            });
        });
    }

    async function loadPrescriptions() {
        const res = await fetchApi('/pharmacist/prescriptions?status=PENDING');
        const container = document.getElementById('prescriptions-content');
        if (res && res.success) {
            setText('pending-prescriptions-count', res.data.length);
            setText('ops-prescription-summary', res.data.length > 0 ? `${res.data.length} awaiting review` : 'Queue clear');

            if (res.data.length === 0) {
                container.innerHTML = `<div class="empty-state"><div class="icon">✅</div><p>All caught up! No pending prescriptions.</p></div>`;
                return;
            }
            let html = `<div class="table-responsive"><table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Patient ID</th>
                        <th>Document</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>`;
            res.data.forEach(p => {
                html += `
                    <tr>
                        <td>${new Date(p.uploadDate).toLocaleDateString()}</td>
                        <td>#${p.patientId}</td>
                        <td><a href="http://localhost:8080${p.filePath}" target="_blank" style="color:var(--primary-color); font-weight: 600;">Review File</a></td>
                        <td style="display: flex; gap: 8px;">
                            <button class="btn btn-primary" style="padding:6px 12px; font-size: 12px;" onclick="verifyPrescription(${p.prescriptionId}, 'VERIFIED')">Approve</button>
                            <button class="btn" style="background:var(--danger-soft); color:var(--danger); padding:6px 12px; font-size: 12px;" onclick="verifyPrescription(${p.prescriptionId}, 'REJECTED')">Reject</button>
                        </td>
                    </tr>
                `;
            });
            html += `</tbody></table></div>`;
            container.innerHTML = html;
        } else {
            container.innerHTML = `<p style="color:var(--danger)">Failed to load prescriptions.</p>`;
        }
    }

    window.verifyPrescription = async function(id, status) {
        if (!confirm(`Are you sure you want to mark this as ${status}?`)) return;
        
        const res = await fetchApi(`/pharmacist/prescriptions/${id}/status`, {
            method: 'PUT',
            body: JSON.stringify({ status: status, notes: 'Reviewed by Pharmacist.' })
        });
        
        if (res && res.success) {
            window.showToast(`Prescription ${status}!`, status === 'VERIFIED' ? 'success' : 'warning');
            loadPrescriptions();
            loadMetrics();
        } else {
            window.showToast(res ? res.message : 'Failed to update.', 'error');
        }
    };

    async function loadRefillRequests() {
        const container = document.getElementById('refill-requests-content');
        const res = await fetchApi('/pharmacist/refills');
        const requests = res && res.success ? (res.data || []) : [];
        const pendingCount = requests.filter(r => r.status === 'PENDING').length;

        setText('pending-refills-count', pendingCount);
        setText(
            'ops-refill-summary',
            pendingCount > 0
                ? `${pendingCount} customer request${pendingCount === 1 ? '' : 's'}`
                : 'Queue clear'
        );

        if (!container) return;
        if (!res || !res.success) {
            container.innerHTML = `<div class="empty-state"><div class="icon">⚠️</div><p>Failed to load customer refill requests.</p></div>`;
            return;
        }

        if (requests.length === 0) {
            container.innerHTML = `<div class="empty-state"><div class="icon">🧾</div><p>No customer refill requests yet.</p></div>`;
            return;
        }

        let html = `<table>
            <thead>
                <tr>
                    <th>Requested</th>
                    <th>Patient</th>
                    <th>Medicine</th>
                    <th>Qty</th>
                    <th>Method</th>
                    <th>Total</th>
                    <th>Destination</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>`;

        requests.forEach(request => {
            const patientName = escapeHtml(request.patientName || `Patient #${request.patientId}`);
            const patientEmail = escapeHtml(request.patientEmail || '');
            const medicineName = escapeHtml(request.medicineName);
            const notes = escapeHtml(request.pharmacistNotes || 'No notes yet');
            const assignedHandle = request.requestedPharmacistHandle
                ? `@${request.requestedPharmacistHandle}`
                : 'Legacy queue';

            let actions = '<span style="color: var(--text-muted); font-size: 12px;">No action needed</span>';
            if (request.status === 'PENDING') {
                actions = `
                    <div class="order-action-group">
                        <button class="btn btn-primary" onclick="updateRefillStatus(${request.refillRequestId}, 'APPROVED')">Approve</button>
                        <button class="btn btn-secondary" onclick="updateRefillStatus(${request.refillRequestId}, 'REJECTED')">Reject</button>
                    </div>
                `;
            } else if (request.status === 'APPROVED') {
                actions = `
                    <div class="order-action-group">
                        <button class="btn btn-primary" onclick="updateRefillStatus(${request.refillRequestId}, 'COMPLETED')">Mark Done</button>
                    </div>
                `;
            }

            html += `<tr>
                <td>${formatDateTime(request.requestedAt)}</td>
                <td>
                    <strong>${patientName}</strong>
                    <div style="font-size: 11px; color: var(--text-secondary);">${patientEmail}</div>
                </td>
                <td>
                    <strong>${medicineName}</strong>
                    <div style="font-size: 11px; color: var(--text-secondary);">${notes}</div>
                </td>
                <td>${request.quantity}</td>
                <td>${escapeHtml(request.fulfillmentMethod)}</td>
                <td><strong>${formatCurrency(request.estimatedTotal)}</strong></td>
                <td><span class="clinical-badge badge-safe">${escapeHtml(assignedHandle)}</span></td>
                <td><span class="badge ${refillBadgeClass(request.status)}">${escapeHtml(request.status)}</span></td>
                <td>${actions}</td>
            </tr>`;
        });

        html += `</tbody></table>`;
        container.innerHTML = html;
    }

    window.updateRefillStatus = async function(id, status) {
        if (status === 'REJECTED' && !confirm('Reject this refill request?')) return;

        const notesByStatus = {
            APPROVED: 'Approved by pharmacist. Preparing the refill order.',
            COMPLETED: 'Refill completed by pharmacy team.',
            REJECTED: 'Request rejected after pharmacist review.'
        };

        const res = await fetchApi(`/pharmacist/refills/${id}/status`, {
            method: 'PUT',
            body: JSON.stringify({
                status,
                notes: notesByStatus[status] || 'Reviewed by pharmacist.'
            })
        });

        if (res && res.success) {
            window.showToast(`Refill request ${status.toLowerCase()}.`, status === 'REJECTED' ? 'warning' : 'success');
            loadRefillRequests();
        } else {
            window.showToast(res ? res.message : 'Failed to update refill request.', 'error');
        }
    };

    async function loadSalesAndReports() {
        const res = await fetchApi('/pharmacist/sales');
        const sales = res && res.success ? res.data : [];
        setText('total-sales-count', sales.length);
        if (sales.length > 0) {
            const revenueTotal = sales.reduce((sum, sale) => sum + Number(sale.totalAmount || 0), 0);
            setText('total-revenue-value', `₹${revenueTotal.toFixed(2)}`);
        }
        
        // Populate Sales/POS History View
        const posView = document.getElementById('view-sales');
        let posHtml = `
            <div class="card" style="margin-bottom: 24px;">
                <div class="section-toolbar">
                    <div>
                        <h3>Point of Sale</h3>
                        <p>Complete a counter sale and sync stock instantly.</p>
                    </div>
                    <span class="clinical-badge badge-safe">Inventory Linked</span>
                </div>
                <div style="display:grid; grid-template-columns: minmax(0, 1fr) minmax(220px, 0.45fr); gap: 18px; align-items: end;">
                    <div>
                        <div class="form-group">
                            <label>Select Medicine to Sell</label>
                            <select id="pos-medicine-select">
                                <option>Loading...</option>
                            </select>
                        </div>
                    </div>
                    <div>
                        <div class="form-group">
                            <label>Quantity</label>
                            <input type="number" id="pos-qty" min="1" value="1" />
                        </div>
                    </div>
                </div>
                <button class="btn btn-primary" style="margin-top: 6px;" onclick="processSale()">Complete Sale</button>
            </div>
            <div class="card">
                <div class="section-toolbar">
                    <div>
                        <h3>Recent Sales History</h3>
                        <p>Transactions written to the backend.</p>
                    </div>
                </div>
        `;

        if (sales.length > 0) {
            posHtml += `<div class="table-responsive"><table>
                <thead><tr><th>Sale ID</th><th>Date</th><th>Amount</th></tr></thead><tbody>`;
            sales.forEach(s => {
                posHtml += `<tr>
                    <td>#${s.saleId}</td>
                    <td>${new Date(s.saleDate).toLocaleString()}</td>
                    <td><strong style="color: var(--success);">₹${s.totalAmount.toFixed(2)}</strong></td>
                </tr>`;
            });
            posHtml += `</tbody></table></div>`;
        } else {
            posHtml += `<div class="empty-state"><div class="icon">💳</div><p>No sales recorded yet.</p></div>`;
        }
        posHtml += `</div>`;
        if(posView) posView.innerHTML = posHtml;

        // Repopulate inventory dropdown after rendering
        loadInventory();

        // Populate Reports View
        const repView = document.getElementById('view-reports');
        if (repView) {
            repView.innerHTML = `
                <div class="card">
                    <div class="section-toolbar">
                        <div>
                            <h3>Analytics & Reports</h3>
                            <p>Snapshot of pharmacy performance, stock risk, and prescription flow.</p>
                        </div>
                        <span class="clinical-badge badge-safe">Report Ready</span>
                    </div>
                    <div class="metric-grid">
                        <div class="metric-card">
                            <div class="metric-label">Sales records</div>
                            <div class="metric-value">${sales.length}</div>
                            <div class="metric-note">Loaded from POS history</div>
                        </div>
                        <div class="metric-card">
                            <div class="metric-label">Inventory</div>
                            <div class="metric-value">Live</div>
                            <div class="metric-note">Stock table linked</div>
                        </div>
                        <div class="metric-card">
                            <div class="metric-label">Prescriptions</div>
                            <div class="metric-value" id="report-prescription-count">${document.getElementById('pending-prescriptions-count')?.textContent || '--'}</div>
                            <div class="metric-note">Pending verification</div>
                        </div>
                        <div class="metric-card">
                            <div class="metric-label">Refill requests</div>
                            <div class="metric-value">${document.getElementById('pending-refills-count')?.textContent || '--'}</div>
                            <div class="metric-note">Pending customer orders</div>
                        </div>
                    </div>
                </div>
            `;
        }
        
        // Also populate dashboard recent sales replacement
        const dashSales = document.getElementById('dash-recent-sales');
        if (dashSales) {
            if (sales.length > 0) {
                let sHtml = `<ul style="list-style: none; padding: 0;">`;
                sales.slice(0, 5).forEach(s => {
                    sHtml += `<li style="padding: 12px 0; border-bottom: 1px solid var(--border-divider); display: flex; justify-content: space-between;">
                        <span style="color: var(--text-secondary);">${new Date(s.saleDate).toLocaleDateString()}</span>
                        <strong style="color: var(--text-primary);">₹${s.totalAmount.toFixed(2)}</strong>
                    </li>`;
                });
                sHtml += `</ul>`;
                dashSales.innerHTML = sHtml;
            } else {
                dashSales.innerHTML = `<div class="empty-state" style="padding: 24px;"><p>No recent sales.</p></div>`;
            }
        }
    }

    window.processSale = async function() {
        const medId = document.getElementById('pos-medicine-select').value;
        const qty = document.getElementById('pos-qty').value;
        if (!medId) { window.showToast("Select a medicine", "warning"); return; }
        
        const payload = { items: [ { medicineId: parseInt(medId), quantity: parseInt(qty) } ] };
        
        try {
            const res = await fetchApi('/pharmacist/sales', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            if (res && res.success) {
                window.showToast(`Sale completed! Total: ₹${res.data.totalAmount.toFixed(2)}`, 'success');
                loadMetrics();
                loadSalesAndReports();
            } else {
                window.showToast(res.message || 'Sale failed', 'error');
            }
        } catch (e) {
            window.showToast('Network error', 'error');
        }
    };

    // Remove the fake Weekly Revenue Trend chart from HTML and add recent sales container
    const chartContainer = document.querySelector('.main-column .card:nth-child(2)');
    if (chartContainer) {
        chartContainer.innerHTML = `
            <h3 style="margin-bottom: 16px; color: var(--text-primary);">📝 Recent Sales</h3>
            <div id="dash-recent-sales"><div class="empty-state" style="padding: 24px;"><div class="icon">⚙️</div><p>Loading...</p></div></div>
        `;
    }

    loadProfile();
    loadMetrics();
    loadInventory();
    loadPrescriptions();
    loadRefillRequests();
    loadSalesAndReports();
});
