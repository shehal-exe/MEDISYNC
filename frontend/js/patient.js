document.addEventListener('DOMContentLoaded', async () => {
    
    // 1. Check Auth & Load User
    const meRes = await fetchApi('/auth/me');
    if (!meRes || !meRes.success || meRes.data.role !== 'PATIENT') {
        window.location.href = '../login.html';
        return;
    }
    document.getElementById('user-greeting').textContent = `Hello, ${meRes.data.email}`;

    // Logout
    document.getElementById('logout-btn').addEventListener('click', async (e) => {
        e.preventDefault();
        await fetchApi('/auth/logout', { method: 'POST' });
        window.location.href = '../login.html';
    });

    // 2. Load Profile
    async function loadProfile() {
        const res = await fetchApi('/patient/profile');
        const container = document.getElementById('profile-content');
        if (res && res.success) {
            const p = res.data;
            container.innerHTML = `
                <p><strong>Name:</strong> ${p.firstName} ${p.lastName}</p>
                <p><strong>DOB:</strong> ${p.dateOfBirth}</p>
                <p><strong>Contact:</strong> ${p.contactNumber || 'N/A'}</p>
                <p><strong>Address:</strong> ${p.address || 'N/A'}</p>
                <p><strong>Medical History:</strong> ${p.medicalHistory || 'None recorded'}</p>
            `;
        } else {
            container.innerHTML = `<p style="color:red">Failed to load profile.</p>`;
        }
    }

    // 3. Load Medications & Schedules
    async function loadMedications() {
        const res = await fetchApi('/patient/medication-schedules');
        const container = document.getElementById('medications-content');
        if (res && res.success) {
            if (res.data.length === 0) {
                container.innerHTML = `<p>No medications scheduled.</p>`;
                return;
            }
            let html = `<table>
                <thead>
                    <tr>
                        <th>Medicine</th>
                        <th>Dosage</th>
                        <th>Frequency</th>
                        <th>Time</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>`;
            res.data.forEach(s => {
                html += `
                    <tr>
                        <td><strong>${s.medicineName}</strong></td>
                        <td>${s.dosage}</td>
                        <td>${s.frequency}</td>
                        <td>${s.timeOfDay}</td>
                        <td>${s.isActive ? '<span style="color:green">Active</span>' : '<span style="color:gray">Inactive</span>'}</td>
                    </tr>
                `;
            });
            html += `</tbody></table>`;
            container.innerHTML = html;
        } else {
            container.innerHTML = `<p style="color:red">Failed to load medications.</p>`;
        }
    }

    // 4. Load Reminders
    async function loadReminders() {
        const res = await fetchApi('/patient/reminders');
        const container = document.getElementById('reminders-content');
        if (res && res.success) {
            if (res.data.length === 0) {
                container.innerHTML = `<p style="color:green">✅ You are all caught up for today!</p>`;
                return;
            }
            let html = '';
            res.data.forEach(r => {
                html += `
                    <div class="reminder-card">
                        <h4>${r.medicineName}</h4>
                        <p><strong>Dosage:</strong> ${r.dosage}</p>
                        <p><strong>Time:</strong> ${r.timeOfDay}</p>
                        <div class="reminder-actions">
                            <button class="btn btn-primary" onclick="markReminder(${r.scheduleId}, 'TAKEN')">Take</button>
                            <button class="btn" style="background:#dc3545; color:white; margin-left:10px;" onclick="markReminder(${r.scheduleId}, 'SKIPPED')">Skip</button>
                        </div>
                    </div>
                `;
            });
            container.innerHTML = html;
        } else {
            container.innerHTML = `<p style="color:red">Failed to load reminders.</p>`;
        }
    }

    // 5. Load Prescriptions
    async function loadPrescriptions() {
        const res = await fetchApi('/patient/prescriptions');
        const container = document.getElementById('prescriptions-content');
        if (res && res.success) {
            if (res.data.length === 0) {
                container.innerHTML = `<p>No prescriptions uploaded.</p>`;
                return;
            }
            let html = `<table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>File</th>
                        <th>Status</th>
                        <th>Notes</th>
                    </tr>
                </thead>
                <tbody>`;
            res.data.forEach(p => {
                html += `
                    <tr>
                        <td>${new Date(p.uploadDate).toLocaleDateString()}</td>
                        <td><a href="http://localhost:8080/api/v1/patient/prescriptions/files/${p.filePath}" target="_blank">View File</a></td>
                        <td><span class="badge ${p.status}">${p.status}</span></td>
                        <td>${p.notes || '-'}</td>
                    </tr>
                `;
            });
            html += `</tbody></table>`;
            container.innerHTML = html;
        } else {
            container.innerHTML = `<p style="color:red">Failed to load prescriptions.</p>`;
        }
    }

    // Handle Upload Prescription
    document.getElementById('upload-prescription-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const fileInput = document.getElementById('prescriptionFile');
        const alertBox = document.getElementById('upload-alert');
        
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
                alertBox.textContent = 'Prescription uploaded successfully!';
                alertBox.className = 'alert success';
                fileInput.value = ''; // clear
                loadPrescriptions();
            } else {
                alertBox.textContent = result.message || 'Upload failed.';
                alertBox.className = 'alert error';
            }
        } catch (err) {
            alertBox.textContent = 'Upload failed. Network error.';
            alertBox.className = 'alert error';
        }
        
        btn.disabled = false;
        btn.innerText = 'Upload';
    });

    // Make markReminder global so inline onclick can see it
    window.markReminder = async function(scheduleId, status) {
        const endpoint = status === 'TAKEN' ? `/patient/reminders/${scheduleId}/taken` : `/patient/reminders/${scheduleId}/skipped`;
        const res = await fetchApi(endpoint, { method: 'POST' });
        if (res && res.success) {
            loadReminders(); // Refresh the list
        } else {
            alert(res.message || 'Failed to update reminder.');
        }
    };

    // Initial Load
    loadProfile();
    loadMedications();
    loadReminders();
    loadPrescriptions();
});
