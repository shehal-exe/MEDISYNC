
document.addEventListener('DOMContentLoaded', async () => {
    // Auth Check
    const meRes = await fetchApi('/auth/me');
    if (!meRes || !meRes.success || meRes.data.role !== 'PATIENT') {
        window.location.href = '../index.html';
        return;
    }
    
    document.getElementById('user-greeting').textContent = `Hello, ${meRes.data.email.split('@')[0]}`;

    // Logout
    document.getElementById('logout-btn').addEventListener('click', async (e) => {
        e.preventDefault();
        await fetchApi('/auth/logout', { method: 'POST' });
        window.location.href = '../index.html';
    });

    // Loaders
    async function loadProfile() {
        const res = await fetchApi('/patients/me');
        const container = document.getElementById('profile-content');
        if (res && res.success) {
            const p = res.data;
            container.innerHTML = `
                <div style="font-size: 1.1rem; margin-bottom: 10px;"><strong>${p.firstName} ${p.lastName}</strong></div>
                <div style="color: var(--text-secondary); margin-bottom: 5px;">📅 ${p.dateOfBirth || 'No DOB set'}</div>
                <div style="color: var(--text-secondary); margin-bottom: 5px;">📞 ${p.contactNumber || 'N/A'}</div>
            `;
            document.getElementById('set-name').value = `${p.firstName} ${p.lastName}`;
            document.getElementById('set-phone').value = p.contactNumber || '';
        } else {
            container.innerHTML = `<div class="empty-state"><p style="color:var(--danger)">Failed to load profile.</p></div>`;
        }
    }

    async function loadReminders() {
        const res = await fetchApi('/patient/reminders');
        const container = document.getElementById('reminders-content');
        if (res && res.success) {
            if (res.data.length === 0) {
                container.innerHTML = `<div class="empty-state" style="padding: 24px;"><div class="icon">✅</div><p>You are all caught up for today!</p></div>`;
                return;
            }
            let html = '';
            res.data.forEach(r => {
                html += `
                    <div class="reminder-card" style="border-left: 4px solid var(--warning); background: var(--bg-secondary); padding: 16px; margin-bottom: 12px; border-radius: 8px; display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <h4 style="margin: 0 0 4px 0; color: var(--text-primary);">${r.medicineName}</h4>
                            <p style="margin: 0; color: var(--text-secondary); font-size: 13px;">Dosage: ${r.dosage} at ${r.timeOfDay}</p>
                        </div>
                        <div style="display: flex; gap: 8px;">
                            <button class="btn btn-primary" onclick="markReminder(${r.scheduleId}, 'TAKEN')">Take</button>
                            <button class="btn" style="background:var(--danger-soft); color:var(--danger);" onclick="markReminder(${r.scheduleId}, 'SKIPPED')">Skip</button>
                        </div>
                    </div>
                `;
            });
            container.innerHTML = html;
        } else {
            container.innerHTML = `<p style="color:var(--danger)">Failed to load reminders.</p>`;
        }
    }

    async function loadMedications() {
        const res = await fetchApi('/patient/medication-schedules');
        const container = document.getElementById('medications-content');
        if (res && res.success) {
            if (res.data.length === 0) {
                container.innerHTML = `<div class="empty-state"><div class="icon">💊</div><p>No active medications.</p></div>`;
                return;
            }
            let html = `<div class="table-responsive"><table>
                <thead>
                    <tr>
                        <th>Medicine</th>
                        <th>Dosage</th>
                        <th>Schedule</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>`;
            res.data.forEach(s => {
                const statusBadge = s.isActive ? '<span class="badge success">ACTIVE</span>' : '<span class="badge warning">INACTIVE</span>';
                html += `
                    <tr>
                        <td><strong>${s.medicineName}</strong></td>
                        <td>${s.dosage}</td>
                        <td>${s.frequency} at ${s.timeOfDay}</td>
                        <td>${statusBadge}</td>
                    </tr>
                `;
            });
            html += `</tbody></table></div>`;
            container.innerHTML = html;
        } else {
            container.innerHTML = `<p style="color:var(--danger)">Failed to load medications.</p>`;
        }
    }

    async function loadPrescriptions() {
        const res = await fetchApi('/patient/prescriptions');
        const container = document.getElementById('prescriptions-content');
        if (res && res.success) {
            if (res.data.length === 0) {
                container.innerHTML = `<div class="empty-state"><div class="icon">📄</div><p>No prescriptions uploaded.</p></div>`;
                return;
            }
            let html = `<div class="table-responsive"><table>
                <thead>
                    <tr>
                        <th>Upload Date</th>
                        <th>File</th>
                        <th>Status</th>
                        <th>Notes</th>
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
                        <td><a href="http://localhost:8080${p.filePath}" target="_blank" style="color: var(--primary-color); font-weight: 600;">View Document</a></td>
                        <td><span class="badge ${badgeClass}">${p.status}</span></td>
                        <td style="color: var(--text-secondary); font-size: 13px;">${p.notes || '-'}</td>
                    </tr>
                `;
            });
            html += `</tbody></table></div>`;
            container.innerHTML = html;
        } else {
            container.innerHTML = `<p style="color:var(--danger)">Failed to load prescriptions.</p>`;
        }
    }

    async function loadScheduleAndAdherence() {
        const adRes = await fetchApi('/patient/adherence');
        if (adRes && adRes.success) {
            document.getElementById('adherence-score').textContent = `${adRes.data.adherencePercentage}%`;
        }

        const histRes = await fetchApi('/patient/history');
        const viewSchedule = document.getElementById('view-schedule');
        
        let html = `
            <div class="card">
                <h3 style="margin-bottom: 24px; color: var(--text-primary);">📅 Medication History</h3>
        `;

        if (histRes && histRes.success && histRes.data.length > 0) {
            html += `<div class="table-responsive"><table>
                <thead><tr><th>Date & Time</th><th>Medicine</th><th>Status</th></tr></thead><tbody>`;
            histRes.data.forEach(h => {
                let bClass = h.status === 'TAKEN' ? 'success' : (h.status === 'SKIPPED' ? 'warning' : 'danger');
                html += `<tr>
                    <td>${new Date(h.logTime).toLocaleString()}</td>
                    <td><strong>${h.medicineName}</strong></td>
                    <td><span class="badge ${bClass}">${h.status}</span></td>
                </tr>`;
            });
            html += `</tbody></table></div>`;
        } else {
            html += `<div class="empty-state"><div class="icon">📅</div><p>No history available yet.</p></div>`;
        }
        html += `</div>`;
        viewSchedule.innerHTML = html;
    }

    async function loadNotifications() {
        const res = await fetchApi('/notifications');
        const container = document.getElementById('view-notifications');
        
        let html = `
            <div class="card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
                    <h3 style="margin:0; color: var(--text-primary);">🔔 Notifications</h3>
                    <button class="btn" style="background: var(--bg-secondary); color: var(--text-primary);">Mark all as read</button>
                </div>
        `;

        if (res && res.success && res.data.length > 0) {
            res.data.forEach(n => {
                const borderLeft = n.isRead ? '4px solid transparent' : '4px solid var(--primary-color)';
                html += `
                    <div style="padding: 16px; margin-bottom: 12px; background: var(--bg-secondary); border-radius: 8px; border-left: ${borderLeft};">
                        <p style="margin: 0; color: var(--text-primary); font-size: 14px;">${n.message}</p>
                    </div>
                `;
            });
        } else {
            html += `<div class="empty-state"><div class="icon">📭</div><p>You have no new notifications.</p></div>`;
        }
        html += `</div>`;
        container.innerHTML = html;
    }

    // Prescription Upload
    const uploadForm = document.getElementById('upload-prescription-form');
    if (uploadForm) {
        uploadForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const fileInput = document.getElementById('prescriptionFile');
            if (fileInput.files.length === 0) return;
            
            const formData = new FormData();
            formData.append('file', fileInput.files[0]);

            const btn = e.target.querySelector('button');
            btn.disabled = true;
            btn.innerText = 'Uploading...';

            try {
                const response = await fetch('http://localhost:8080/api/v1/patient/prescriptions', {
                    method: 'POST',
                    credentials: 'include',
                    body: formData
                });
                
                const result = await response.json();
                if (result.success) {
                    window.showToast('Prescription securely uploaded!', 'success');
                    fileInput.value = '';
                    if(document.getElementById('prescriptionNotes')) document.getElementById('prescriptionNotes').value = '';
                    loadPrescriptions();
                } else {
                    window.showToast(result.message || 'Upload failed.', 'error');
                }
            } catch (err) {
                window.showToast('Upload failed. Network error.', 'error');
            }
            
            btn.disabled = false;
            btn.innerText = 'Upload securely';
        });
    }

    window.markReminder = async function(scheduleId, status) {
        const endpoint = status === 'TAKEN' ? `/patient/reminders/${scheduleId}/taken` : `/patient/reminders/${scheduleId}/skipped`;
        const res = await fetchApi(endpoint, { method: 'POST' });
        if (res && res.success) {
            window.showToast(`Marked as ${status.toLowerCase()}`, status === 'TAKEN' ? 'success' : 'warning');
            loadReminders(); 
            loadScheduleAndAdherence(); // Update score!
        } else {
            window.showToast(res.message || 'Failed to update reminder.', 'error');
        }
    };

    // Routing Logic to lazy load data
    document.querySelectorAll('.sidebar-link').forEach(link => {
        link.addEventListener('click', (e) => {
            const targetId = e.currentTarget.getAttribute('data-target');
            if (targetId === 'view-schedule') loadScheduleAndAdherence();
            if (targetId === 'view-notifications') loadNotifications();
        });
    });

    // Initial Load
    loadProfile();
    loadReminders();
    loadMedications();
    loadPrescriptions();
    loadScheduleAndAdherence();
});
