let map;
const markers = [];
const start = [];
const end = [];
const drawInfoArr = [];
const streetLightMarkers = [];
const streetLightRanges = [];

function initTmap() {
  map = new Tmapv2.Map("map_div", {
      center: new Tmapv2.LatLng(36.35454109, 127.365012),
      width: "100%",
      height: "100%",
      zoom: 18
  });
  markers.length = 0;
  start.length = 0;
  end.length = 0;
  drawInfoArr.length = 0;
  streetLightMarkers.length = 0;
  streetLightRanges.length = 0;
  map.addListener("click",onClick);
}

function onClick(event){
  const coordinate = event.latLng;
  if(start[0]==null&&start[1]==null){
    start[0]=coordinate.lng();
    start[1]=coordinate.lat();
    markers[0] = new Tmapv2.Marker({
        position: new Tmapv2.LatLng(coordinate.lat(),coordinate.lng()),
        icon : "http://localhost:8080/img/marker/StartMarker.png",
        map: map
    });
  } else if(end[0]==null&&end[1]==null) {
    end[0]=coordinate.lng();
    end[1]=coordinate.lat();
    markers[1] = new Tmapv2.Marker({
        position: new Tmapv2.LatLng(coordinate.lat(),coordinate.lng()),
        icon : "http://localhost:8080/img/marker/EndMarker.png",
        map: map
    });
  }
}

function searchRoute(event){
  event.preventDefault();

  fetch("/besafe/api/getSafeRoute", {
      method: "POST",
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        startX: start[0],
        startY: start[1],
        endX: end[0],
        endY: end[1],
        startName: "start",
        endName: "end",
      })
    })
    .then(response => {
      if (!response.ok) {
        return response.text().then(message => { alert(message); });
      } else {
        return response.json()
      }
    })
    .then(data => {
      const lightNodes = data.lightNodes;
      const routeNodes = data.routeNodes;

      streetLightMarkers.length = 0;
      streetLightRanges.length = 0;
      drawInfoArr.length = 0;

      map.setCenter(new Tmapv2.LatLng(start[1], start[0]));

      for (let i = 0; i < lightNodes.length; i++) {
        const lightLocation = new Tmapv2.LatLng(lightNodes[i].latitude, lightNodes[i].longitude);

        streetLightRanges.push(new Tmapv2.Circle({
            center: lightLocation,
            radius: 20,
            strokeColor: "blue",
            strokeOpacity: 0.1,
            fillColor: "blue",
            fillOpacity: 0.1,
            map: map
        }));

        streetLightRanges.push(new Tmapv2.Marker({
            position: lightLocation,
            icon: "http://localhost:8080/img/marker/LightMarker.png",
            map: map
        }));
      }

      for (let i = 0; i < routeNodes.length; i++) {
        drawInfoArr.push(new Tmapv2.LatLng(routeNodes[i].latitude, routeNodes[i].longitude));
      }

      const polyline = new Tmapv2.Polyline({
        path: drawInfoArr,
        strokeColor: "#DD0000",
        strokeWeight: 6,
        map: map
      });
    })
    .catch(error => {
      console.error(error);
    });
}

document.addEventListener("DOMContentLoaded", initTmap);
document.getElementById('reset').addEventListener('click', initTmap);
document.getElementById('search').addEventListener('click', searchRoute);