document.addEventListener('DOMContentLoaded', async () => {
    // 1. Check Auth & Load User
    const meRes = await fetchApi('/auth/me');
    if (!meRes || !meRes.success || meRes.data.role !== 'PATIENT') {
        window.location.href = '../index.html';
        return;
    }
    
    // We expect meRes.data.email to exist. We will set the top greeting.
    document.getElementById('user-greeting').textContent = Hello, \;

    // Logout
    document.getElementById('logout-btn').addEventListener('click', async (e) => {
        e.preventDefault();
        await fetchApi('/auth/logout', { method: 'POST' });
        window.location.href = '../index.html';
    });

    // 2. Data Loaders for each View
    async function loadProfile() {
        const res = await fetchApi('/patients/me');
        const container = document.getElementById('profile-content');
        if (res && res.success) {
            const p = res.data;
            container.innerHTML = 
                <div style="font-size: 1.1rem; margin-bottom: 10px;"><strong>\ \</strong></div>
                <div style="color: var(--text-light); margin-bottom: 5px;">?? \</div>
                <div style="color: var(--text-light); margin-bottom: 5px;">?? \</div>
            ;
            // Fill Settings inputs too
            document.getElementById('set-name').value = \ \;
            document.getElementById('set-phone').value = p.contactNumber || '';
        } else {
            container.innerHTML = <div class="empty-state"><p style="color:red">Failed to load profile.</p></div>;
        }
    }

    async function loadReminders() {
        const res = await fetchApi('/patient/reminders');
        const container = document.getElementById('reminders-content');
        if (res && res.success) {
            if (res.data.length === 0) {
                container.innerHTML = <div class="empty-state"><div class="icon">?</div><p>You are all caught up for today!</p></div>;
                return;
            }
            let html = '';
            res.data.forEach(r => {
                html += 
                    <div class="reminder-card" style="border-left: 4px solid var(--warning); background: #fff; padding: 15px; margin-bottom: 10px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <h4 style="margin: 0 0 5px 0;">\</h4>
                            <p style="margin: 0; color: #666; font-size: 0.9rem;">Dosage: \ at \</p>
                        </div>
                        <div>
                            <button class="btn btn-primary" onclick="markReminder(\, 'TAKEN')">Take</button>
                            <button class="btn" style="background:#dc3545; color:white; margin-left:5px;" onclick="markReminder(\, 'SKIPPED')">Skip</button>
                        </div>
                    </div>
                ;
            });
            container.innerHTML = html;
        } else {
            container.innerHTML = <p style="color:red">Failed to load reminders.</p>;
        }
    }

    async function loadMedications() {
        const res = await fetchApi('/patient/medication-schedules');
        const container = document.getElementById('medications-content');
        if (res && res.success) {
            if (res.data.length === 0) {
                container.innerHTML = <div class="empty-state"><div class="icon">??</div><p>No active medications.</p></div>;
                return;
            }
            let html = <table>
                <thead>
                    <tr>
                        <th>Medicine</th>
                        <th>Dosage</th>
                        <th>Schedule</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>;
            res.data.forEach(s => {
                const statusBadge = s.isActive ? '<span class="badge success">ACTIVE</span>' : '<span class="badge warning">INACTIVE</span>';
                html += 
                    <tr>
                        <td><strong>\</strong></td>
                        <td>\</td>
                        <td>\ at \</td>
                        <td>\</td>
                        <td><button class="btn" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;" onclick="alert('Viewing details')">View</button></td>
                    </tr>
                ;
            });
            html += </tbody></table>;
            container.innerHTML = html;
        } else {
            container.innerHTML = <p style="color:red">Failed to load medications.</p>;
        }
    }

    async function loadPrescriptions() {
        const res = await fetchApi('/patient/prescriptions');
        const container = document.getElementById('prescriptions-content');
        if (res && res.success) {
            if (res.data.length === 0) {
                container.innerHTML = <div class="empty-state"><div class="icon">??</div><p>No prescriptions uploaded.</p></div>;
                return;
            }
            let html = <table>
                <thead>
                    <tr>
                        <th>Upload Date</th>
                        <th>File</th>
                        <th>Verification Status</th>
                        <th>Pharmacist Notes</th>
                    </tr>
                </thead>
                <tbody>;
            res.data.forEach(p => {
                let badgeClass = 'warning';
                if (p.status === 'VERIFIED') badgeClass = 'success';
                if (p.status === 'REJECTED') badgeClass = 'danger';

                html += 
                    <tr>
                        <td>\</td>
                        <td><a href="http://localhost:8080\" target="_blank" style="color: var(--primary-color); font-weight: 500;">View Document</a></td>
                        <td><span class="badge \">\</span></td>
                        <td style="color: #666; font-style: italic;">\</td>
                    </tr>
                ;
            });
            html += </tbody></table>;
            container.innerHTML = html;
        } else {
            container.innerHTML = <p style="color:red">Failed to load prescriptions.</p>;
        }
    }

    // Prescription Upload
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
                window.showToast('Prescription securely uploaded!', 'success');
                fileInput.value = '';
                document.getElementById('prescriptionNotes').value = '';
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

    window.markReminder = async function(scheduleId, status) {
        const endpoint = status === 'TAKEN' ? /patient/reminders/\/taken : /patient/reminders/\/skipped;
        const res = await fetchApi(endpoint, { method: 'POST' });
        if (res && res.success) {
            window.showToast(Marked as \, status === 'TAKEN' ? 'success' : 'warning');
            loadReminders(); 
        } else {
            window.showToast(res.message || 'Failed to update reminder.', 'error');
        }
    };

    // Calculate Fake Adherence for Dashboard Demo
    function setAdherenceScore() {
        const score = Math.floor(Math.random() * (98 - 75 + 1)) + 75; // random between 75 and 98
        document.getElementById('adherence-score').textContent = \%;
    }

    // Initialize all data
    loadProfile();
    loadReminders();
    loadMedications();
    loadPrescriptions();
    setAdherenceScore();
});
