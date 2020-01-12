$( document ).ready(function() {
    var path = window.location.href;
    if (path.endsWith('/repeatActivation')) {
        history.replaceState({}, null, path.substring(0, path.lastIndexOf("/")));
    }
});


function clickAndDisable(link) {
    link.onclick = function(event) {
        event.preventDefault();
    }
}