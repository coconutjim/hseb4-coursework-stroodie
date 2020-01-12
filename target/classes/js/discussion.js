$( document ).ready(function() {

    initSimpleSelect2($('#message_search_select'));
    initSimpleSelect2($('#message_search_user'));
    initSimpleSelect2($('#message_search_type'));

    initSummernotes();
    insertAccordions();

    $('.updated-tooltip, .type-tooltip').tooltip();
    $('.author-popover').popover();

    initSearch();
    initUnionPopover();
});

function initSimpleSelect2($elem) {
    $elem.select2({
            minimumResultsForSearch: -1,
            'language': {
                'noResults': function() {
                    return '';
                }
            }
        }
    );
}

function initSummernotes() {
    $('#mes_editor_sn').summernote({
        lang: 'ru-RU',
        minHeight: 150,
        maxHeight: 150,
        toolbar: [
            ['style', ['bold', 'italic', 'underline', 'clear']],
            ['font', ['strikethrough', 'superscript', 'subscript']],
            ['para', ['ul', 'ol', 'paragraph']],
            ['insert', ['link']]
        ],
        dialogsInBody: true
    });

    $('.thesaurus-def-sn').summernote({
        lang: 'ru-RU',
        minHeight: 150,
        maxHeight: 150,
        toolbar: [
            ['style', ['bold', 'italic', 'underline', 'clear']],
            ['font', ['strikethrough', 'superscript', 'subscript']],
            ['insert', ['link']]
        ],
        dialogsInBody: true
    });

    $('.thesaurus-def-sn', '#thesaurus_block').each(function() {
        var $elem = $(this);
        var thId = $elem.attr('data-id');
        if (thId != 'new') {
            $elem.summernote('code', $('#thesaurus_content_' + thId).html());
        }
    });
}

function show_editor(mode, mesId, prevId, typeId, typeName, mesName) {

    var $editor = $('#mes_editor');


    // if not the same editor, fill with new data
    if (! ($editor.data('mode') == mode && $editor.data('mesId') == mesId && $editor.data('prevId') == prevId &&
        $editor.data('typeId') == typeId)) {
        if (mode == 'create') {
            if (prevId == 'null') {
                $('#mes_editor_title').text('Начало дискуссии');
            }
            else {
                $('#mes_editor_title').text('Ответ типа \'' + typeName + '\' на сообщение ' + mesName);
            }
            $('#mes_editor_sn').summernote('code', '');

            var $modal = $('#mes_answer_' + prevId);
            $modal.modal('hide');
        }
        else {
            $('#mes_editor_title').text('Редактирование сообщения ' + mesName);
            $('#mes_editor_sn').summernote('code', $('#mes_content_' + mesId).html());
        }
        $editor.data('mode', mode);
        $editor.data('mesId', mesId);
        $editor.data('prevId', prevId);
        $editor.data('typeId', typeId);
    }
    else {
        if (mode == 'create') {
            var $modal1 = $('#mes_answer_' + prevId);
            $modal1.modal('hide');
        }
    }

    if ($editor.hasClass('hidden')) {
        $editor.removeClass('hidden');
        $('body').css('margin-bottom', $editor.height());
    }
}

function hide_editor() {
    var $editor = $('#mes_editor');
    if (! $editor.hasClass('hidden')) {
        $editor.addClass('hidden');
        $('body').css('margin-bottom', 0);
    }
}

function message_cu(disId) {

    var $allButtons = $(':button');
    $allButtons.prop('disabled', true);

    // collecting data
    var $editor = $('#mes_editor');
    var res = {};
    var mode = $editor.data('mode');
    res['mode'] = mode;
    res['discussion'] = disId;
    res['id'] = mode == 'create' ? '-1' : $editor.data('mesId');
    var prevId = $editor.data('prevId');
    res['prev'] = prevId == 'null' ? '-1' : prevId;
    var first = (mode == 'create' && prevId == 'null');
    res['first'] = first;
    res['type'] = first ? '-1' : $editor.data('typeId');

    var $sn = $('#mes_editor_sn');
    res['content'] = $sn.summernote('code');
    res['text'] = $sn.next().find('.note-editable').text(); // consider this

    // send a post request
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $.ajax({
        type: 'POST',
        url: window.location.href + '/message',
        data: JSON.stringify(res),
        contentType: 'application/json',
        beforeSend: function(xhr){xhr.setRequestHeader(header, token);},
        complete: function (data) {
            var resp = data.responseText;
            if (resp == 'success') {
                if (mode == 'create') {
                    //go_page_top();
                }
                window.location.reload(true);
            }
            else {
                if (resp == 'error') {
                    window.location.href='/stroodie/400';
                }
                else {
                    var $fb = $('#mes_editor_fb');
                    if ($fb.hasClass('hidden')) {
                        $fb.removeClass('hidden');
                    }
                    $fb.text(resp);
                    $allButtons.prop('disabled', false);
                }
            }
        }
    });
}

function delete_message(disId, mesId) {

    var $allButtons = $(':button');
    $allButtons.prop('disabled', true);

    var res = {};
    res['mode'] = 'delete';
    res['discussion'] = disId;
    res['id'] = mesId;

    // send a post request
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $.ajax({
        type: 'POST',
        url: window.location.href + '/message',
        data: JSON.stringify(res),
        contentType: 'application/json',
        beforeSend: function(xhr){xhr.setRequestHeader(header, token);},
        complete: function (data) {
            var resp = data.responseText;
            if (resp == 'success') {
                //go_page_top();
                window.location.reload(true);
            }
            else {
                if (resp == 'error') {
                    window.location.href='/stroodie/400';
                }
                else {
                    alert(resp);
                    $allButtons.prop('disabled', false);
                }
            }
        }
    });
}


function fillModalData(mesId) {
    $('#view_modal').attr('data-id', mesId);
    $('#view_modal_name').text('Сообщение ' + $('#mes_name_' + mesId).text());
    $('#view_modal_author').text($('#mes_author_' + mesId).text());
    $('#view_modal_content').html($('#mes_content_' + mesId).html());
    $('#view_modal_created').html($('#mes_created_' + mesId).html());
}

function scrollToMessage() {
    var $modal = $('#view_modal');
    var mesId = $modal.attr('data-id');
    $modal.modal('hide');
    $(window).scrollTop($('#message_' + mesId).offset().top - 70);
}

function showMessageOnGraph(uid, num) {
    var data = [];
    data.push(uid);
    processSearchResults(data, 'Сообщение #' + num);
    go_page_top();
}

function searchMessages() {

    var type = $('#message_search_select option:selected').val();

    var text = '';
    var id = 0;
    var startDate = '';
    var endDate = '';

    if (type == 'text') {
        text = $('#message_search_text').val();
        if (text == null || text == '') {
            return;
        }
    }
    var $select = null;
    var $selected = null;
    if (type == 'user') {
        $select = $('#message_search_user');
        $selected = $('#message_search_user option:selected');
        if ($select.has('option').length == 0 || $selected == null) {
            return;
        }
        id = $selected.val();
    }
    if (type == 'type') {
        $select = $('#message_search_type');
        $selected = $('#message_search_type option:selected');
        if ($select.has('option').length == 0 || $selected == null) {
            return;
        }
        id = $selected.val();
    }
    if (type == 'date') {
        startDate = $('#message_search_date_start').val();
        endDate = $('#message_search_date_end').val();
        if (startDate == null || startDate == '' || endDate == null || endDate == '') {
            return;
        }
    }

    var $searchButton = $('#message_search_button');
    $searchButton.prop('disabled', true);

    var res = {};
    res['type'] = type;
    res['text'] = text;
    res['id'] = id;
    res['dateStart'] = startDate;
    res['dateEnd'] = endDate;

    // send a post request
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $.ajax({
        type: 'POST',
        url: window.location.href + '/search',
        data: JSON.stringify(res),
        contentType: 'application/json',
        beforeSend: function(xhr){xhr.setRequestHeader(header, token);},
        complete: function (data) {
            var resp = data.responseText;
            if (resp == null || resp.startsWith('error')) {
                alert(resp);
                $searchButton.prop('disabled', false);
            }
            else {
                var parsed = JSON.parse(resp);
                var dt = parsed['data'];
                processSearchResults(dt, 'Результаты поиска:');
                $searchButton.prop('disabled', false);
            }
        }
    });
}

function processSearchResults(data, label) {

    updateGraph(data);
    var resText;
    if (data == null || data.length == 0) {
        resText = 'Поиск не дал результатов';
    }
    else {
        resText = label;
    }
    var $result = $('#message_search_result');
    $result.text(resText);

    var $searchRow = $('#search_row');
    if ($searchRow.hasClass('hidden')) {
        $searchRow.removeClass('hidden');
    }
}

function cancelSearch() {
    restoreGraph();
    var $searchRow = $('#search_row');
    if (! $searchRow.hasClass('hidden')) {
        $searchRow.addClass('hidden');
    }
}

function insertAccordions() {
    var i = 0;
    $('.message-content-div', '#message_container').each(function() {
        var $elem = $(this);
        var $content = $elem.find('.message-content');
        var $truncated = $elem.find('.message-truncated-content');
        if ($content.length == 1 && $truncated.length == 1) {
            $content.remove();

            var $mainDiv = $('<div class="accordion-group">' + '</div>');
            var $bodyDiv = $('<div class="accordion-body collapse" id="collapse' + i + '">' + '</div>');
            var $innerDiv = $('<div class="accordion-inner">' + '</div>');

            var $toggleButton = $('<a href="#collapse' + i + '" data-toggle="collapse">' + 'Показать текст' + '</a>');

            $bodyDiv.on("hide.bs.collapse", function(){
                $truncated.removeClass('hidden');
                $toggleButton.html('Показать текст');
            });
            $bodyDiv.on("show.bs.collapse", function(){
                $truncated.addClass('hidden');
                $toggleButton.html('Скрыть текст');
            });


            $innerDiv.append($content);
            $bodyDiv.append($innerDiv);

            $mainDiv.append($bodyDiv);
            $mainDiv.append($toggleButton);

            $elem.append($mainDiv);
            ++i;
        }
    });
}
function initSearch() {
    $('#message_search_select').change(function() {
        var val = $(this).val();
        hideAllSearchElems();
        $('.message_search_' + val + '_elem').removeClass('hidden');
    });

    $.fn.datepicker.defaults.format = 'mm.dd.yyyy';
    $.fn.datepicker.dates['ru'] = {
        days: ["Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"],
        daysShort: ["Вск", "Пнд", "Втр", "Срд", "Чтв", "Птн", "Суб"],
        daysMin: ["Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб"],
        months: ["Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"],
        monthsShort: ["Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"],
        today: "Сегодня",
        clear: "Очистить",
        format: "dd.mm.yyyy",
        weekStart: 1
    };
    $.fn.datepicker.defaults.language = 'ru';
}

function hideAllSearchElems() {
    var $text = $('.message_search_text_elem');
    var $user = $('.message_search_user_elem');
    var $type = $('.message_search_type_elem');
    var $date = $('.message_search_date_elem');
    if (! $text.hasClass('hidden')) {
        $text.addClass('hidden');
    }
    if (! $user.hasClass('hidden')) {
        $user.addClass('hidden');
    }
    if (! $type.hasClass('hidden')) {
        $type.addClass('hidden');
    }
    if (! $date.hasClass('hidden')) {
        $date.addClass('hidden');
    }
}

var $unionMenu;
function initUnionPopover() {
    $unionMenu = $("#union_menu");
    $unionMenu.remove();
    $("#union_menu_popover").popover({
        html : true,
        container: 'body',
        content: function() {
            return $unionMenu.html();
        },
        title: function() {
            return 'Объединения';
        }
    });
}

function thesaurus_crud(mode, elemId) {

    var $allButtons = $(':button');
    $allButtons.prop('disabled', true);

    // collecting data
    var res = {};
    res['mode'] = mode;
    res['id'] = mode == 'create' ? '-1' : elemId;
    if (mode != 'delete') {
        res['name'] = $('#thesaurus_name_' + elemId).val();
        var $sn = $('#thesaurus_def_sn_' + elemId);
        res['content'] = $sn.summernote('code');
        res['text'] = $sn.next().find('.note-editable').text(); // consider this
    }

    // send a post request
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $.ajax({
        type: 'POST',
        url: window.location.href + '/thesaurus',
        data: JSON.stringify(res),
        contentType: 'application/json',
        beforeSend: function(xhr){xhr.setRequestHeader(header, token);},
        complete: function (data) {
            var resp = data.responseText;
            if (resp == 'success') {
                window.location.reload(true);
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
                        var $fb = $('#thesaurus_fb_' + elemId);
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