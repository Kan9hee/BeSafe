function openOAuthPopup(provider) {
  const popup = window.open(
    `http://localhost:8080/oauth2/authorization/${provider}`,
    `${provider}Login`,
    'width=500,height=600'
  );

  const timer = setInterval(() => {
      if (popup.closed) {
        clearInterval(timer);
        alert(`${provider}로 로그인했습니다.`);
        window.location.href = '/besafe/servicePage';
      }
    }, 500);
}

function localLogIn(event) {
  event.preventDefault();

  fetch("/besafe/api/logIn", {
      method: "POST",
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        insertedEmail: document.getElementById('insertedEmail').value,
        insertedPassword: document.getElementById('insertedPassword').value
      })
    })
    .then(response => {
      if (response.ok) {
        alert("로컬 계정으로 로그인했습니다.");
        window.location.href = '/besafe/servicePage';
      } else {
        return response.text().then(message => { alert(message); });
      }
    })
    .catch(error => {
      console.error(error);
    });
}

document.getElementById('confirmLogIn').addEventListener('click', localLogIn);
document.getElementById('googleLogin').addEventListener('click', () => openOAuthPopup('google'));
document.getElementById('kakaoLogin').addEventListener('click', () => openOAuthPopup('kakao'));