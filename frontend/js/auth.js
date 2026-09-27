// Ensure this script only runs if forms exist
document.addEventListener('DOMContentLoaded', () => {
    // REGISTER ROLE SWITCHER
    const accountRoleInput = document.getElementById('accountRole');
    if (accountRoleInput) {
        window.setRegisterRole = function(role) {
            const normalizedRole = role === 'PHARMACIST' ? 'PHARMACIST' : 'PATIENT';
            accountRoleInput.value = normalizedRole;

            const patientButton = document.getElementById('registerPatientOption');
            const pharmacistButton = document.getElementById('registerPharmacistOption');
            const dateOfBirthGroup = document.getElementById('dateOfBirthGroup');
            const licenseNumberGroup = document.getElementById('licenseNumberGroup');
            const dateOfBirthInput = document.getElementById('dateOfBirth');
            const licenseNumberInput = document.getElementById('licenseNumber');
            const formTitle = document.getElementById('registerFormTitle');
            const formSubtitle = document.getElementById('registerFormSubtitle');
            const heroPill = document.getElementById('registerHeroPillText');
            const heroTitle = document.getElementById('registerHeroTitle');
            const heroDesc = document.getElementById('registerHeroDesc');
            const submitBtn = document.getElementById('registerSubmitBtn');

            if (patientButton) patientButton.classList.toggle('active', normalizedRole === 'PATIENT');
            if (pharmacistButton) pharmacistButton.classList.toggle('active', normalizedRole === 'PHARMACIST');

            const isPharmacist = normalizedRole === 'PHARMACIST';
            if (dateOfBirthGroup) dateOfBirthGroup.style.display = isPharmacist ? 'none' : 'block';
            if (licenseNumberGroup) licenseNumberGroup.style.display = isPharmacist ? 'block' : 'none';
            if (dateOfBirthInput) dateOfBirthInput.required = !isPharmacist;
            if (licenseNumberInput) licenseNumberInput.required = isPharmacist;

            if (formTitle) formTitle.textContent = isPharmacist ? 'Create Pharmacist Account' : 'Create Your Account';
            if (formSubtitle) {
                formSubtitle.textContent = isPharmacist
                    ? 'Register as a licensed pharmacist to manage inventory, prescriptions, POS sales, and refill requests.'
                    : 'Enter your details below to register for the Patient Portal.';
            }
            if (heroPill) heroPill.textContent = isPharmacist ? 'Pharmacist Portal Registration' : 'Patient Portal Registration';
            if (heroTitle) {
                heroTitle.innerHTML = isPharmacist
                    ? 'Run Your Pharmacy With <br><span class="gradient-text">Verified Access.</span>'
                    : 'Take Control of Your Health. <br><span class="gradient-text">Zero Missed Doses.</span>';
            }
            if (heroDesc) {
                heroDesc.textContent = isPharmacist
                    ? 'Create a verified MEDISYNC pharmacist account with your license number, unique request code, inventory tools, prescription review, and refill workflows.'
                    : 'Join MEDISYNC to bridge your daily care with your pharmacy. Experience personalized dosage scheduling, fast digital prescription verification, and continuous adherence monitoring.';
            }
            if (submitBtn) submitBtn.textContent = isPharmacist ? 'Create Pharmacist Account' : 'Complete Registration';
        };

        const params = new URLSearchParams(window.location.search);
        window.setRegisterRole(params.get('role') === 'pharmacist' ? 'PHARMACIST' : 'PATIENT');
    }
    
    // LOGIN
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const alertBox = document.getElementById('login-alert');
            alertBox.style.display = 'none';

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const rememberMeInput = document.getElementById('rememberMe');
            const rememberMe = rememberMeInput ? rememberMeInput.checked : false;

            // Optional: Show loading state on button
            const submitBtn = loginForm.querySelector('button[type="submit"]');
            const origText = submitBtn.innerText;
            submitBtn.innerText = 'Logging in...';
            submitBtn.disabled = true;

            const res = await fetchApi('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email, password, rememberMe })
            });

            submitBtn.innerText = origText;
            submitBtn.disabled = false;

            if (res && res.success) {
                // Success! Check role and redirect
                const role = res.data.role;
                if (role === 'PHARMACIST') {
                    window.location.href = 'pharmacist/dashboard.html';
                } else {
                    window.location.href = 'patient/dashboard.html';
                }
            } else {
                alertBox.textContent = res ? res.message : 'Login failed. Please try again.';
                alertBox.className = 'alert error';
            }
        });
    }

    // REGISTER
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const alertBox = document.getElementById('register-alert');
            alertBox.style.display = 'none';

            const formData = {
                firstName: document.getElementById('firstName').value,
                lastName: document.getElementById('lastName').value,
                email: document.getElementById('email').value,
                password: document.getElementById('password').value,
                contactNumber: document.getElementById('contactNumber').value,
                dateOfBirth: document.getElementById('dateOfBirth').value,
                licenseNumber: document.getElementById('licenseNumber') ? document.getElementById('licenseNumber').value : ''
            };
            const accountRole = document.getElementById('accountRole') ? document.getElementById('accountRole').value : 'PATIENT';
            const endpoint = accountRole === 'PHARMACIST' ? '/auth/register/pharmacist' : '/auth/register';

            const submitBtn = registerForm.querySelector('button[type="submit"]');
            const origText = submitBtn.innerText;
            submitBtn.innerText = 'Registering...';
            submitBtn.disabled = true;

            const res = await fetchApi(endpoint, {
                method: 'POST',
                body: JSON.stringify(formData)
            });

            submitBtn.innerText = origText;
            submitBtn.disabled = false;

            if (res && res.success) {
                alertBox.textContent = 'Registration successful! Logging you in...';
                alertBox.className = 'alert success';
                
                // Explicitly log in via /auth/login to guarantee session is established
                try {
                    const loginRes = await fetchApi('/auth/login', {
                        method: 'POST',
                        body: JSON.stringify({ email: formData.email, password: formData.password, rememberMe: false })
                    });

                    if (loginRes && loginRes.success) {
                        alertBox.textContent = 'Registration successful! Redirecting to your dashboard...';
                        setTimeout(() => {
                            window.location.href = loginRes.data.role === 'PHARMACIST'
                                ? 'pharmacist/dashboard.html'
                                : 'patient/dashboard.html';
                        }, 1000);
                        return;
                    }
                } catch (err) {
                    console.warn('Auto-login exception:', err);
                }

                alertBox.textContent = 'Registration successful! Please sign in to continue.';
                setTimeout(() => {
                    window.location.href = 'index.html#portal';
                }, 1400);
            } else {
                // Handle backend validation array vs standard message
                let errorMsg = 'Registration failed.';
                if (res && res.message) {
                    errorMsg = res.message;
                }
                alertBox.textContent = errorMsg;
                alertBox.className = 'alert error';
            }
        });
    }

});
