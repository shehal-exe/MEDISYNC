
document.addEventListener('DOMContentLoaded', async () => {
    const meRes = await fetchApi('/auth/me');
    if (!meRes || !meRes.success || meRes.data.role !== 'PHARMACIST') {
        window.location.href = '../index.html';
        return;
    }
    document.getElementById('user-greeting').textContent = `Dr. ${meRes.data.email.split('@')[0]}`;

    document.getElementById('logout-btn').addEventListener('click', async (e) => {
        e.preventDefault();
        await fetchApi('/auth/logout', { method: 'POST' });
        window.location.href = '../index.html';
    });

    async function loadMetrics() {
        const res = await fetchApi('/pharmacist/reports/dashboard');
        if (res && res.success) {
            document.querySelector('#view-dashboard h1:nth-child(2)').textContent = res.data.totalSalesCount || '0';
            document.querySelector('#view-dashboard .main-column .card h1:nth-child(2)').textContent = res.data.lowStockMedicines ? res.data.lowStockMedicines.length : '0';
            document.querySelectorAll('#view-dashboard .main-column .card h1')[2].textContent = '$' + (res.data.totalRevenue ? res.data.totalRevenue.toFixed(2) : '0.00');
        }
    }

    async function loadProfile() {
        const res = await fetchApi('/pharmacist/profile');
        const container = document.getElementById('profile-content');
        if (res && res.success) {
            const p = res.data;
            container.innerHTML = `
                <div style="font-size: 1.1rem; margin-bottom: 10px;"><strong>${p.firstName} ${p.lastName}</strong></div>
                <div style="color: var(--text-secondary); margin-bottom: 5px;">License: ${p.licenseNumber || 'N/A'}</div>
            `;
            document.getElementById('set-name').value = `${p.firstName} ${p.lastName}`;
            document.getElementById('set-license').value = p.licenseNumber || '';
        } else {
            container.innerHTML = `<div class="empty-state"><p style="color:var(--danger)">Failed to load profile.</p></div>`;
        }
    }

    async function loadInventory() {
        const res = await fetchApi('/pharmacist/inventory');
        const container = document.getElementById('inventory-content');
        const posSelect = document.getElementById('pos-medicine-select'); // if it exists
        
        let optionsHtml = '<option value="">-- Select Medicine --</option>';

        if (res && res.success) {
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
                html += `
                    <tr>
                        <td><strong>${m.name}</strong> <br><small style="color:var(--text-secondary)">${m.manufacturer}</small></td>
                        <td>$${m.price.toFixed(2)}</td>
                        <td><span style="${isLow ? 'color:var(--danger);font-weight:700;' : ''}">${m.stockQuantity}</span></td>
                        <td>${statusBadge}</td>
                        <td><button class="btn" style="padding: 4px 12px; font-size: 12px; background: var(--bg-secondary); color: var(--text-primary);" onclick="alert('Editing ${m.name}')">Edit</button></td>
                    </tr>
                `;
                if(m.stockQuantity > 0) {
                    optionsHtml += `<option value="${m.medicineId}">${m.name} ($${m.price.toFixed(2)}) - Stock: ${m.stockQuantity}</option>`;
                }
            });
            html += `</tbody></table></div>`;
            container.innerHTML = html;
            if(posSelect) posSelect.innerHTML = optionsHtml;
        } else {
            container.innerHTML = `<p style="color:var(--danger)">Failed to load inventory.</p>`;
        }
    }

    async function loadPrescriptions() {
        const res = await fetchApi('/pharmacist/prescriptions?status=PENDING');
        const container = document.getElementById('prescriptions-content');
        if (res && res.success) {
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

    async function loadSalesAndReports() {
        const res = await fetchApi('/pharmacist/sales');
        
        // Populate Sales/POS History View
        const posView = document.getElementById('view-sales');
        let posHtml = `
            <div class="card" style="margin-bottom: 24px;">
                <h3 style="margin-bottom: 16px; color: var(--text-primary);">💳 Point of Sale</h3>
                <div style="display:flex; gap: 24px;">
                    <div style="flex:2;">
                        <div class="form-group">
                            <label>Select Medicine to Sell</label>
                            <select id="pos-medicine-select" class="form-group" style="margin-bottom:12px;">
                                <option>Loading...</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Quantity</label>
                            <input type="number" id="pos-qty" min="1" value="1" />
                        </div>
                        <button class="btn btn-primary" style="margin-top: 8px;" onclick="processSale()">Complete Sale</button>
                    </div>
                </div>
            </div>
            <div class="card">
                <h3 style="margin-bottom: 16px; color: var(--text-primary);">📝 Recent Sales History</h3>
        `;

        if (res && res.success && res.data.length > 0) {
            posHtml += `<div class="table-responsive"><table>
                <thead><tr><th>Sale ID</th><th>Date</th><th>Amount</th></tr></thead><tbody>`;
            res.data.forEach(s => {
                posHtml += `<tr>
                    <td>#${s.saleId}</td>
                    <td>${new Date(s.saleDate).toLocaleString()}</td>
                    <td><strong style="color: var(--success);">$${s.totalAmount.toFixed(2)}</strong></td>
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
                    <h3 style="margin-bottom: 24px; color: var(--text-primary);">📊 Analytics & Reports</h3>
                    <p style="color: var(--text-secondary);">Comprehensive sales and inventory reports.</p>
                    <div class="empty-state"><div class="icon">📈</div><p>Full chart analytics coming soon.</p></div>
                </div>
            `;
        }
        
        // Also populate dashboard recent sales replacement
        const dashSales = document.getElementById('dash-recent-sales');
        if (dashSales) {
            if (res && res.success && res.data.length > 0) {
                let sHtml = `<ul style="list-style: none; padding: 0;">`;
                res.data.slice(0, 5).forEach(s => {
                    sHtml += `<li style="padding: 12px 0; border-bottom: 1px solid var(--border-divider); display: flex; justify-content: space-between;">
                        <span style="color: var(--text-secondary);">${new Date(s.saleDate).toLocaleDateString()}</span>
                        <strong style="color: var(--text-primary);">$${s.totalAmount.toFixed(2)}</strong>
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
                window.showToast(`Sale completed! Total: $${res.data.totalAmount.toFixed(2)}`, 'success');
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
    loadSalesAndReports();
});
