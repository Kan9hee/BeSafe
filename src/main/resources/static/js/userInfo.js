function initInfo() {
  fetch("/besafe/api/getUserInfo")
    .then(response => response.json())
    .then(data => {
      document.getElementById("username").innerText = data.name;
    })
    .catch(error => console.error("사용자 정보 불러오기 실패:", error));

    fetch("/besafe/api/getCurrentUsage?page=0&size=10")
    .then(response => response.json())
    .then(data => {
      const usageList = document.getElementById("usageList");
      usageList.innerHTML = "";

      data.forEach(item => {
        const historyItem = document.createElement("a");
        historyItem.className = "list-group-item list-group-item-action py-3 lh-sm";
        historyItem.innerHTML = `
          <div class="d-flex w-100 align-items-center justify-content-between">
          <strong class="mb-1">${item.usedAt}</strong>
          </div>
          <div class="col-10 mb-1 small">출발: ${item.startX}, ${item.startY}</div>
          <div class="col-10 mb-1 small">도착: ${item.endX}, ${item.endY}</div>
        `;
        usageList.appendChild(historyItem);
      });
    })
    .catch(error => console.error("이력 불러오기 실패:", error));
}

function logOut(event){
  fetch("/besafe/api/logOut", {
      method: "POST",
      headers: {
        'Content-Type': 'application/json'
      }
  })
  .then(response => {
      if (response.ok) {
        alert("로그아웃했습니다.");
        window.location.href = '/besafe';
      } else {
        return response.text().then(message => { alert(message); });
      }
  })
  .catch(error => {
      console.error(error);
  });
}

function deleteAccount(event){
  fetch("/besafe/api/deleteAccount", {
      method: "POST",
      headers: {
        'Content-Type': 'application/json'
      }
  })
  .then(response => {
      if (response.ok) {
        alert("회원탈퇴되었습니다.");
        window.location.href = '/besafe';
      } else {
        return response.text().then(message => { alert(message); });
      }
  })
  .catch(error => {
      console.error(error);
  });
}

function deleteAccount(event){
}

document.addEventListener("DOMContentLoaded", initInfo);
document.getElementById('logOut').addEventListener('click', logOut);
document.getElementById('deleteAccount').addEventListener('click', deleteAccount);