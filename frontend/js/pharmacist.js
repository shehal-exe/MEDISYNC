document.addEventListener('DOMContentLoaded', async () => {
    
    // 1. Check Auth & Load User
    const meRes = await fetchApi('/auth/me');
    if (!meRes || !meRes.success || meRes.data.role !== 'PHARMACIST') {
        window.location.href = '../login.html';
        return;
    }
    document.getElementById('user-greeting').textContent = `Dr. ${meRes.data.email}`;

    // Logout
    document.getElementById('logout-btn').addEventListener('click', async (e) => {
        e.preventDefault();
        await fetchApi('/auth/logout', { method: 'POST' });
        window.location.href = '../login.html';
    });

    // 2. Load Dashboard Metrics
    async function loadMetrics() {
        const res = await fetchApi('/pharmacist/reports/dashboard');
        if (res && res.success) {
            document.getElementById('metric-sales').textContent = res.data.totalSalesCount;
            document.getElementById('metric-revenue').textContent = '$' + res.data.totalRevenue.toFixed(2);
            document.getElementById('metric-lowstock').textContent = res.data.lowStockMedicines.length;
        }
    }

    // 3. Load Inventory & Populate POS Dropdown
    async function loadInventory() {
        const res = await fetchApi('/pharmacist/inventory');
        const container = document.getElementById('inventory-content');
        const select = document.getElementById('pos-medicine');
        
        if (res && res.success) {
            // Populate table
            let html = `<table>
                <thead>
                    <tr>
                        <th>Medicine</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th>Rx Required</th>
                    </tr>
                </thead>
                <tbody>`;
            
            let optionsHtml = '<option value="">-- Select --</option>';

            res.data.forEach(m => {
                const isLow = m.stockQuantity <= 10;
                html += `
                    <tr style="${isLow ? 'background:#fff8f8;' : ''}">
                        <td><strong>${m.name}</strong> <br><small>${m.manufacturer}</small></td>
                        <td>$${m.price.toFixed(2)}</td>
                        <td><span style="${isLow ? 'color:red;font-weight:bold;' : ''}">${m.stockQuantity}</span></td>
                        <td>${m.requiresPrescription ? 'Yes' : 'No'}</td>
                    </tr>
                `;
                if (m.stockQuantity > 0) {
                    optionsHtml += `<option value="${m.medicineId}">${m.name} ($${m.price.toFixed(2)}) - Stock: ${m.stockQuantity}</option>`;
                }
            });
            html += `</tbody></table>`;
            container.innerHTML = html;
            select.innerHTML = optionsHtml;
        } else {
            container.innerHTML = `<p style="color:red">Failed to load inventory.</p>`;
        }
    }

    // 4. Load Pending Prescriptions
    async function loadPrescriptions() {
        const res = await fetchApi('/pharmacist/prescriptions?status=PENDING');
        const container = document.getElementById('prescriptions-content');
        if (res && res.success) {
            if (res.data.length === 0) {
                container.innerHTML = `<p style="color:green">✅ All caught up! No pending prescriptions.</p>`;
                return;
            }
            let html = `<table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Patient ID</th>
                        <th>File</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>`;
            res.data.forEach(p => {
                html += `
                    <tr>
                        <td>${new Date(p.uploadDate).toLocaleDateString()}</td>
                        <td>#${p.patientId}</td>
                        <td><a href="http://localhost:8080/api/v1/patient/prescriptions/files/${p.filePath}" target="_blank">Review File</a></td>
                        <td>
                            <button class="btn btn-primary" onclick="verifyPrescription(${p.prescriptionId}, 'VERIFIED')">Approve</button>
                            <button class="btn" style="background:#dc3545; color:white;" onclick="verifyPrescription(${p.prescriptionId}, 'REJECTED')">Reject</button>
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

    // Handle Quick Sale (POS)
    document.getElementById('pos-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const medId = document.getElementById('pos-medicine').value;
        const qty = document.getElementById('pos-quantity').value;
        const alertBox = document.getElementById('pos-alert');

        if (!medId || qty < 1) return;

        const payload = {
            items: [
                { medicineId: parseInt(medId), quantity: parseInt(qty) }
            ]
        };

        const res = await fetchApi('/pharmacist/sales', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        if (res && res.success) {
            alertBox.textContent = `Sale successful! Total: $${res.data.totalAmount.toFixed(2)}`;
            alertBox.className = 'alert success';
            // Refresh data
            loadMetrics();
            loadInventory();
        } else {
            alertBox.textContent = res ? res.message : 'Sale failed.';
            alertBox.className = 'alert error';
        }
        
        setTimeout(() => { alertBox.style.display = 'none'; }, 4000);
    });

    // Make verify global
    window.verifyPrescription = async function(id, status) {
        if (!confirm(`Are you sure you want to mark this as ${status}?`)) return;
        
        const res = await fetchApi(`/pharmacist/prescriptions/${id}/status`, {
            method: 'PUT',
            body: JSON.stringify({ status: status, notes: 'Reviewed by Pharmacist.' })
        });
        
        if (res && res.success) {
            loadPrescriptions(); // Refresh list
        } else {
            alert(res ? res.message : 'Failed to update.');
        }
    };

    // Initial Load
    loadMetrics();
    loadInventory();
    loadPrescriptions();
});
