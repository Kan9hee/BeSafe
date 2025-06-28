function handleJoin(event) {
  event.preventDefault();

  const password = document.getElementById('insertedPassword').value;
  const checkPassword = document.getElementById('insertedPasswordCheck').value;

  if(password != checkPassword){
    alert("비밀번호가 일치하지 않습니다.");
    return null;
  }

  fetch("/besafe/api/join", {
    method: "POST",
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      name: document.getElementById('insertedUserName').value,
      emailAndPassword: {
        insertedEmail: document.getElementById('insertedEmail').value,
        insertedPassword: password
      }
    })
  })
  .then(response => {
    if (response.ok) {
      console.log("회원가입 성공");
      window.location.href = '/besafe';
    } else {
      return response.text().then(message => {
        alert(message);
        if (message.includes("이미")) {
          window.location.href = '/besafe';
        }
      });
    }
  })
  .catch(error => {
    console.error(error);
  });
}

document.getElementById('confirmJoin').addEventListener('click', handleJoin);