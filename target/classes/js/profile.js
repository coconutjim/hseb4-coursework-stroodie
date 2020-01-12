$( document ).ready(function() {
    history.replaceState({}, null, window.location.href.split('?')[0]);
});

function update_user() {
    var $allButtons = $(':button');
    $allButtons.prop('disabled', true);

    // collecting data
    var res = {};
    res['firstName'] = $('#inputFirstName').val();
    res['lastName'] = $('#inputLastName').val();
    res['email'] = '';
    res['password'] = '';
    res['passwordConfirm'] = '';
    res['oldPassword'] = '';

    // send a post request
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $.ajax({
        type: 'POST',
        url: window.location.href + '/user',
        data: JSON.stringify(res),
        contentType: 'application/json',
        beforeSend: function(xhr){xhr.setRequestHeader(header, token);},
        complete: function (data) {
            var resp = data.responseText;
            if (resp == 'success') {
                window.location.href = window.location.href + "?success"
            }
            else {
                var $fb = $('#user_fb');
                if ($fb.hasClass('hidden')) {
                    $fb.removeClass('hidden');
                }
                $fb.text(resp);
                $allButtons.prop('disabled', false);
            }
        }
    });
}

function update_user_password() {
    var $allButtons = $(':button');
    $allButtons.prop('disabled', true);

    // collecting data
    var res = {};
    res['password'] = $('#inputPassword').val();
    res['passwordConfirm'] =  $('#inputPasswordConfirm').val();
    res['oldPassword'] =  $('#inputOldPassword').val();
    res['firstName'] = '';
    res['lastName'] = '';
    res['email'] = '';

    // send a post request
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $.ajax({
        type: 'POST',
        url: window.location.href + '/password',
        data: JSON.stringify(res),
        contentType: 'application/json',
        beforeSend: function(xhr){xhr.setRequestHeader(header, token);},
        complete: function (data) {
            var resp = data.responseText;
            if (resp == 'success') {
                window.location.href = window.location.href + "?success"
            }
            else {
                var $fb = $('#user_password_fb');
                if ($fb.hasClass('hidden')) {
                    $fb.removeClass('hidden');
                }
                $fb.text(resp);
                $allButtons.prop('disabled', false);
            }
        }
    });
}