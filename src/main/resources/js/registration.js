$( document ).ready(function() {
    $('.form-control').keyup(function(event){
        if(event.keyCode == 13){
            $(".submit-btn").click();
        }
    });
});

function register() {
    var $allButtons = $(':button');
    $allButtons.prop('disabled', true);

    // collecting data
    var res = {};
    res['email'] = $('#inputEmail').val();
    res['password'] = $('#inputPassword').val();
    res['passwordConfirm'] = $('#inputPasswordConfirm').val();
    res['firstName'] = $('#inputFirstName').val();
    res['lastName'] = $('#inputLastName').val();
    res['oldPassword'] = '';

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
            if (resp == 'success') {
                window.location.href = "/stroodie/login?registered"
            }
            else {
                var $fb = $('#reg_fb');
                if ($fb.hasClass('hidden')) {
                    $fb.removeClass('hidden');
                }
                $fb.text(resp);
                $allButtons.prop('disabled', false);
            }
        }
    });
}