function buildWave(){var w=document.getElementById('wave');w.innerHTML='';for(var i=0;i<32;i++){var b=document.createElement('div');b.className='wb2';b.style.setProperty('--h',(Math.random()*20+7)+'px');w.appendChild(b);}}
function animW(on){document.querySelectorAll('.wb2').forEach(function(b){if(on)b.classList.add('an');else{b.classList.remove('an');b.style.height='4px';}});}

var recInt=null,recSec=0,isLoop=false,loopCnt=0;

function onMicPermissionGranted(){
  document.getElementById('rstat').textContent='Listo para grabar';
}

function onNativePlaybackComplete(){
  if(isLoop){
    loopCnt++;
    document.getElementById('lcnt').textContent=loopCnt;
    AndroidAudio.startPlayback();
  } else {
    document.getElementById('btplay').innerHTML='<i class="ti ti-player-play"></i>';
    document.getElementById('pbadge').classList.remove('show');
    animW(false);
  }
}

function toggleRec(){
  var isRec=AndroidAudio.isRecording();
  if(!isRec){
    var result=AndroidAudio.startRecording();
    if(result==='PERMISSION_DENIED'){
      document.getElementById('rstat').textContent='Permiso denegado. Reinstala la app.';
      return;
    }
    if(result.indexOf('ERROR')===0){
      document.getElementById('rstat').textContent='Error al grabar';
      return;
    }
    recSec=0;
    recInt=setInterval(function(){
      recSec++;
      var m=Math.floor(recSec/60),s=recSec%60;
      document.getElementById('rtim').textContent=m+':'+String(s).padStart(2,'0');
    },1000);
    document.getElementById('btrec').innerHTML='<i class="ti ti-player-stop"></i>';
    document.getElementById('rstat').textContent='Grabando...';
    document.getElementById('rstat').classList.add('live');
    animW(true);
  } else {
    AndroidAudio.stopRecording();
    clearInterval(recInt);
    document.getElementById('btrec').innerHTML='<i class="ti ti-microphone"></i>';
    document.getElementById('rstat').textContent='Lista ('+document.getElementById('rtim').textContent+')';
    document.getElementById('rstat').classList.remove('live');
    animW(false);
    var pb=document.getElementById('btplay');
    pb.className='rbtn ply';
    pb.disabled=false;
  }
}

function doPlay(){
  if(!AndroidAudio.hasRecording()) return;
  if(!AndroidAudio.isPlaying()){
    loopCnt=0;
    document.getElementById('lcnt').textContent=0;
    AndroidAudio.startPlayback();
    document.getElementById('btplay').innerHTML='<i class="ti ti-player-pause"></i>';
    animW(true);
    if(isLoop) document.getElementById('pbadge').classList.add('show');
  } else {
    AndroidAudio.stopPlayback();
    document.getElementById('btplay').innerHTML='<i class="ti ti-player-play"></i>';
    animW(false);
    document.getElementById('pbadge').classList.remove('show');
  }
}

function toggleLoop(){
  isLoop=!isLoop;
  var t=document.getElementById('ltog');
  if(isLoop) t.classList.add('on');
  else { t.classList.remove('on'); document.getElementById('pbadge').classList.remove('show'); }
}