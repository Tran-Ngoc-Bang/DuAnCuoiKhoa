function togglePassword(icon, fieldId) {
  const input = document.getElementById(fieldId);
  const isVisible = input.type === 'text';
  input.type = isVisible ? 'password' : 'text';
  icon.classList.toggle('fa-eye');
  icon.classList.toggle('fa-eye-slash');
}






const btnConfirmPassword = document.getElementById('btnConfirmPassword');
const btnChangePassword = document.getElementById('btnChangePassword');
// Thêm dòng này để lấy input mật khẩu hiện tại
const currentPasswordInput = document.getElementById('currentPassword');
btnConfirmPassword.addEventListener('click', () => {
  const currentPass = currentPasswordInput.value.trim(); // Lấy mật khẩu hiện tại
  const newPass = newPasswordInput.value.trim();
  const confirmPass = confirmPasswordInput.value.trim();

  const isLengthOk = newPass.length >= 8 && newPass.length <= 20;
  const isValid = /[a-z]/.test(newPass) &&
    /[A-Z]/.test(newPass) &&
    /[0-9]/.test(newPass) &&
    /[\W]/.test(newPass);


  // ❗ Kiểm tra đã nhập mật khẩu hiện tại chưa
  if (!currentPass) {
    showPopup('⚠️ Vui lòng nhập mật khẩu hiện tại.', 'error');
    return;
  }



  if (!isLengthOk || !isValid) {
    showPopup('❌ Mật khẩu chưa đáp ứng yêu cầu. Vui lòng kiểm tra lại.', 'error');
    return;
  }

  if (newPass !== confirmPass) {
    showPopup('❌ Mật khẩu xác nhận không khớp.', 'error');
    return;
  }

  // Thành công -> Khóa input + Bật nút "Đổi mật khẩu"
  newPasswordInput.disabled = true;
  confirmPasswordInput.disabled = true;
  btnConfirmPassword.disabled = true;
  btnConfirmPassword.style.opacity = '0.6';
  btnConfirmPassword.style.cursor = 'not-allowed';

  btnChangePassword.disabled = false;
  btnChangePassword.style.opacity = '1';
  btnChangePassword.style.cursor = 'pointer';

  passwordMatchStatusDiv.textContent = '✅ Mật khẩu đã được xác nhận. Bạn có thể đổi mật khẩu.';
  passwordMatchStatusDiv.style.color = 'green';

  showPopup('✅ Mật khẩu đã được xác nhận!', 'success');
});



function showPopup(message, type = 'success') {
  const popup = document.getElementById('notification-popup');
  popup.textContent = message;

  if (type === 'success') {
    popup.style.backgroundColor = '#28a745';
  } else if (type === 'error') {
    popup.style.backgroundColor = '#dc3545';
  } else {
    popup.style.backgroundColor = '#333';
  }

  popup.style.display = 'block';

  setTimeout(() => {
    popup.style.display = 'none';
  }, 3000);
}


document.addEventListener('DOMContentLoaded', () => {
  const btnChangePassword = document.getElementById('btnChangePassword');
  const otpModal = document.getElementById('otpModal');
  const otpOverlay = document.getElementById('otpOverlay');
  const btnCloseOtp = document.getElementById('btnCloseOtp');
  const btnVerifyOtp = document.getElementById('btnVerifyOtp');
  const btnResendOtp = document.getElementById('btnResendOtp');
  const otpCodeInput = document.getElementById('otpCodeInput');

  // Mở popup
  btnChangePassword.addEventListener('click', () => {
    otpModal.style.display = 'block';
    otpOverlay.style.display = 'block';
    otpCodeInput.value = '';
  });

  // Đóng popup
  function closePopup() {
    otpModal.style.display = 'none';
    otpOverlay.style.display = 'none';
  }

  btnCloseOtp.addEventListener('click', closePopup);
  otpOverlay.addEventListener('click', closePopup); // Click nền để tắt

  // Xác minh OTP
  btnVerifyOtp.addEventListener('click', () => {
    const otp = otpCodeInput.value.trim();
    const validOtp = '123456';

    if (otp === validOtp) {
      closePopup();
      showNotification('✅ Đổi mật khẩu thành công!', 'success');
    } else {
      showNotification('❌ Mã OTP không đúng!', 'error');
    }
  });

  // Gửi lại mã
  btnResendOtp.addEventListener('click', () => {
    showNotification('🔄 Mã OTP mới đã được gửi.', 'info');
  });

  // Thông báo nổi ở góc phải
  function showNotification(message, type) {
    const notify = document.createElement('div');
    notify.textContent = message;
    notify.style.position = 'fixed';
    notify.style.top = '20px';
    notify.style.right = '20px';
    notify.style.background = type === 'success' ? '#4caf50' : type === 'error' ? '#f44336' : '#2196f3';
    notify.style.color = '#fff';
    notify.style.padding = '12px 20px';
    notify.style.borderRadius = '6px';
    notify.style.boxShadow = '0 4px 10px rgba(0,0,0,0.2)';
    notify.style.zIndex = '2000';
    notify.style.opacity = '1';
    notify.style.transition = 'opacity 0.5s ease';

    document.body.appendChild(notify);

    setTimeout(() => {
      notify.style.opacity = '0';
      setTimeout(() => document.body.removeChild(notify), 500);
    }, 2500);
  }
});
