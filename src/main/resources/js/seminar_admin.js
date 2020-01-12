
function discussion_crud(mode, semId, elemId) {

    var $allButtons = $(':button');
    $allButtons.prop('disabled', true);

    // collecting data
    var res = {};
    res['mode'] = mode;
    res['seminar'] = semId;
    res['id'] = mode == 'create' ? '-1' : elemId;
    if (mode != 'delete') {
        res['name'] = $('#dis_name_' + elemId).val();
        res['description'] = $('#dis_descr_' + elemId).val();

        var $select = $('#dis_master_' + elemId);
        var $selected = $('#dis_master_' + elemId + ' option:selected');
        if ($select.has('option').length == 0 || $selected == null) {
            $allButtons.prop('disabled', false);
            alert('Руководитель не выбран!');
            return;
        }

        res['master'] = $selected.attr('id');
    }

    // send a post request
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $.ajax({
        type: 'POST',
        url: window.location.href.split('?')[0] + '/discussion',
        data: JSON.stringify(res),
        contentType: 'application/json',
        beforeSend: function(xhr){xhr.setRequestHeader(header, token);},
        complete: function (data) {
            var resp = data.responseText;
            if (resp == 'success') {
                window.location.href = window.location.href.split('?')[0] + '?success';
            }
            else {
                if (resp == 'error') {
                    window.location.href='/stroodie/400';
                }
                else {
                    if (mode == 'delete') {
                        alert(resp);
                    }
                    else {
                        var $fb = $('#dis_fb_' + elemId);
                        if ($fb.hasClass('hidden')) {
                            $fb.removeClass('hidden');
                        }
                        $fb.text(resp);
                    }
                    $allButtons.prop('disabled', false);
                }
            }
        }
    });
}
