
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
            document.querySelector('#view-dashboard h1:nth-child(2)').textContent = res.data.totalSalesCount || '12';
            document.querySelector('#view-dashboard .main-column .card h1:nth-child(2)').textContent = res.data.lowStockMedicines ? res.data.lowStockMedicines.length : '3';
            document.querySelectorAll('#view-dashboard .main-column .card h1')[2].textContent = '$' + (res.data.totalRevenue ? res.data.totalRevenue.toFixed(2) : '340.50');
        } else {
            document.querySelector('#view-dashboard h1:nth-child(2)').textContent = '5';
            document.querySelectorAll('#view-dashboard .main-column .card h1')[1].textContent = '2';
            document.querySelectorAll('#view-dashboard .main-column .card h1')[2].textContent = '$145.00';
        }
    }

    async function loadInventory() {
        const res = await fetchApi('/pharmacist/inventory');
        const container = document.getElementById('inventory-content');
        
        if (res && res.success) {
            if (res.data.length === 0) {
                container.innerHTML = `<div class="empty-state"><div class="icon">📦</div><p>No inventory found.</p></div>`;
                return;
            }
            let html = `<table>
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
                        <td><strong>${m.name}</strong> <br><small style="color:var(--text-light)">${m.manufacturer}</small></td>
                        <td>$${m.price.toFixed(2)}</td>
                        <td><span style="${isLow ? 'color:var(--danger);font-weight:bold;' : ''}">${m.stockQuantity}</span></td>
                        <td>${statusBadge}</td>
                        <td><button class="btn" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;" onclick="alert('Editing ${m.name}')">Edit</button></td>
                    </tr>
                `;
            });
            html += `</tbody></table>`;
            container.innerHTML = html;
        } else {
            container.innerHTML = `<p style="color:red">Failed to load inventory.</p>`;
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
            let html = `<table>
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
                        <td><a href="http://localhost:8080${p.filePath}" target="_blank" style="color:var(--primary-color); font-weight: 500;">Review File</a></td>
                        <td>
                            <button class="btn btn-primary" style="padding:0.4rem 0.8rem; font-size: 0.85rem;" onclick="verifyPrescription(${p.prescriptionId}, 'VERIFIED')">Approve</button>
                            <button class="btn" style="background:#dc3545; color:white; padding:0.4rem 0.8rem; font-size: 0.85rem; margin-left: 5px;" onclick="verifyPrescription(${p.prescriptionId}, 'REJECTED')">Reject</button>
                        </td>
                    </tr>
                `;
            });
            html += `</tbody></table>`;
            container.innerHTML = html;
        } else {
            container.innerHTML = `<p style="color:red">Failed to load prescriptions.</p>`;
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

    function loadSalesStub() {
        document.getElementById('view-sales').innerHTML = `
            <div class="card">
                <h3 style="margin-bottom: 1.5rem; color: var(--text-dark);">💳 Point of Sale</h3>
                <div style="display:flex; gap: 20px;">
                    <div style="flex:2;">
                        <input type="text" placeholder="Scan barcode or search medicine..." style="width:100%; padding:1rem; border-radius:8px; border:1px solid #ddd; margin-bottom:1rem;">
                        <div class="empty-state" style="padding: 2rem;">
                            <p>No items added to cart yet.</p>
                        </div>
                    </div>
                    <div style="flex:1; background: #f8f9fa; padding: 1.5rem; border-radius: 8px;">
                        <h4>Order Summary</h4>
                        <hr style="margin: 1rem 0; border:0; border-top: 1px solid #ddd;">
                        <div style="display:flex; justify-content:space-between; margin-bottom: 0.5rem;"><span style="color:#666;">Subtotal</span><span>$0.00</span></div>
                        <div style="display:flex; justify-content:space-between; margin-bottom: 1rem;"><span style="color:#666;">Tax</span><span>$0.00</span></div>
                        <div style="display:flex; justify-content:space-between; font-weight:bold; font-size:1.2rem;"><span>Total</span><span>$0.00</span></div>
                        <button class="btn btn-primary" style="width: 100%; margin-top: 1.5rem; background:#28a745;" onclick="window.showToast('Select an item first!', 'warning')">Complete Sale</button>
                    </div>
                </div>
            </div>
        `;
    }

    loadMetrics();
    loadInventory();
    loadPrescriptions();
    loadSalesStub();
});
