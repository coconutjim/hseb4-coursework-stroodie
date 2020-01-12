$( document ).ready(function() {
    $('.form-control').keyup(function(event){
        if(event.keyCode == 13){
            $(".submit-btn").click();
        }
    });
});

function recoverPasswordRequest() {

    var email = $('#inputEmail').val();

    if (email == null || email == '') {
        return;
    }

    var $allButtons = $(':button');
    $allButtons.prop('disabled', true);

    // collecting data
    var res = {};
    res['email'] = email;

    // send a post request
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $.ajax({
        type: 'POST',
        url: window.location.href,
        data: JSON.stringify(res),
        contentType: 'application/json',
        beforeSend: function(xhr){xhr.setRequestHeader(header, token);},
        complete: function (data) {
            var resp = data.responseText;
            hideInfoDivs();
            $allButtons.prop('disabled', false);
            if (resp == 'success') {
                $('#sent_success_div').removeClass('hidden');
            }
            else {
                if (resp == 'sentError') {
                    $('#sent_error_div').removeClass('hidden');
                }
                else {
                    $('#email_error_div').removeClass('hidden');
                }
            }
        }
    });
}

function hideInfoDivs() {
    hideDiv($('#sent_success_div'));
    hideDiv($('#sent_error_div'));
    hideDiv($('#email_error_div'));
}

function hideDiv($div) {
    if (! $div.hasClass('hidden')) {
        $div.addClass('hidden');
    }
}

function recoverPassword() {
    var $allButtons = $(':button');
    $allButtons.prop('disabled', true);

    // collecting data
    var res = {};
    res['password'] = $('#inputPassword').val();
    res['passwordConfirm'] = $('#inputPasswordConfirm').val();

    // send a post request
    $.ajax({
        type: 'POST',
        url: window.location.href,
        data: JSON.stringify(res),
        contentType: 'application/json',
        complete: function (data) {
            var resp = data.responseText;
            if (resp == 'success') {
                window.location.href = "/stroodie/login?recovered"
            }
            else {
                var $fb = $('#recover_fb');
                if ($fb.hasClass('hidden')) {
                    $fb.removeClass('hidden');
                }
                $fb.text(resp);
                $allButtons.prop('disabled', false);
            }
        }
    });
}