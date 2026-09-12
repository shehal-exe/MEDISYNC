// Ensure this script only runs if forms exist
document.addEventListener('DOMContentLoaded', () => {
    
    // LOGIN
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const alertBox = document.getElementById('login-alert');
            alertBox.style.display = 'none';

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const rememberMe = document.getElementById('rememberMe').checked;

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
                    window.location.href = '/pharmacist/dashboard.html';
                } else {
                    window.location.href = '/patient/dashboard.html';
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
                dateOfBirth: document.getElementById('dateOfBirth').value
            };

            const submitBtn = registerForm.querySelector('button[type="submit"]');
            const origText = submitBtn.innerText;
            submitBtn.innerText = 'Registering...';
            submitBtn.disabled = true;

            const res = await fetchApi('/auth/register', {
                method: 'POST',
                body: JSON.stringify(formData)
            });

            submitBtn.innerText = origText;
            submitBtn.disabled = false;

            if (res && res.success) {
                alertBox.textContent = 'Registration successful! Redirecting to dashboard...';
                alertBox.className = 'alert success';
                
                // Immediately log them in by redirecting to patient dashboard 
                // (Backend automatically logs in upon register)
                setTimeout(() => {
                    window.location.href = '/patient/dashboard.html';
                }, 1500);
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
