$( document ).ready(function() {
    $('.user-select').select2({
            'language': {
                'noResults': function() {
                    return 'Пользователи не найдены';
                }
            }
        }
    );

    $('.type-select').select2({
            minimumResultsForSearch: -1,
            'language': {
                'noResults': function() {
                    return '';
                }
            }
        }
    );

    initRoleSelect($('.role-select'));
    initIconPicker($('.iconpicker'));
    initTypeIcons();

    $change_role_button = $('#change_role_button');
    $admin_checkbox = $('#admin_checkbox');
    initUserRolesBlock();
    history.replaceState({}, null, window.location.href.split('?')[0]);

});

var tempId = -1;

function seminar_crud(mode, elemId, canChangeOnt) {

    var $allButtons = $(':button');
    $allButtons.prop('disabled', true);

    // collecting data
    var res = {};
    res['mode'] = mode;
    res['id'] = mode == 'create' ? '-1' : elemId;
    if (mode != 'delete') {
        res['name'] = $('#sem_name_' + elemId).val();
        res['description'] = $('#sem_descr_' + elemId).val();

        var participations = [];
        $('.sem-user-' + elemId, '#sem_users_' + elemId).each(function() {
            var participation = {};
            var userId = $(this).data('id');
            participation['id'] = userId;
            participation['role'] = $('#sem_role_' + elemId + '_' + userId + ' option:selected').val();
            participations.push(participation);
        });
        res['participations'] = participations;

        res['types'] = [];
        res['connections'] = [];

        if (canChangeOnt) {

            var types = [];
            var err = false;
            $('.sem-type-' + elemId, '#sem_types_' + elemId).each(function () {
                var type = {};
                var typeName = $(this).data('name');
                var typeId = $(this).data('id');
                var iconData = getIconData(elemId, typeId);

                if (iconData[0] == null || iconData[1] == null) {
                    alert('Иконка для типа ' + typeName + ' не выбрана!');
                    err = true;
                    return false;
                }
                type['name'] = typeName;
                type['iconName'] = iconData[0];
                type['iconValue'] = iconData[1];
                types.push(type);
            });
            if (err) {
                $allButtons.prop('disabled', false);
                return;
            }
            res['types'] = types;

            var connections = [];
            $('.sem-connection-' + elemId, '#sem_connections_' + elemId).each(function () {
                var $cn = $(this);
                var el = {};
                var type1 = $cn.data('type1name');
                var type2 = $cn.data('type2name');
                el['type1'] = type1;
                el['type2'] = type2;
                connections.push(el);
            });
            res['connections'] = connections;
        }
    }

    // send a post request
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $.ajax({
        type: 'POST',
        url: window.location.href.split('?')[0],
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
                        var $fb = $('#sem_fb_' + elemId);
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

function add_user(elemId, rolesJSON) {

    var parsed = JSON.parse(rolesJSON);

    var rls = parsed['data'];

    var $addButton = $('#btn_add_user_' + elemId);
    $addButton.prop('disabled', true);

    // checking
    var $select = $('#all_users_' + elemId);
    var $selected = $('#all_users_' + elemId + ' option:selected');
    if ($select.has('option').length == 0 || $selected == null) {
        $addButton.prop('disabled', false);
        alert('Пользователь не выбран!');
        return;
    }

    // getting user
    var userId = $selected.attr('id');
    var userName = $selected.text();

    // checking existence
    var $checkUser = $('#sem_user_' + elemId + '_' + userId);
    if ($checkUser.length != 0) {
        $addButton.prop('disabled', false);
        //alert('Пользователь уже был выбран!');
        return;
    }

    // creating elements
    var $mainDiv = $('<div class="row gap-top vertical-align sem-user-' + elemId + '" id="sem_user_' + elemId + '_' + userId + '" ' +
        'data-id="' + userId+ '">' + '</div>');
    var $spanName = $('<div class="col-sm-5">' + userName +'</div>');
    var roleSlct = '<div class="col-sm-4"><select style="width: 100%" id="sem_role_' + elemId + '_' + userId + '" class="role-select" + ' +
         '>';
    var i;
    for (i = 0; i < rls.length; ++i) {
        roleSlct += '<option>' + rls[i] + '</option>'
    }
    roleSlct += '</select></div>';
    var $roleSelect = $(roleSlct);
    var $removeButton = $('<div class="col-sm-1"><button id="btn_remove_user_' + elemId + '_' + userId + '" ' +
        'onclick="remove_user(\'' + elemId + '\',' + userId + ');" type="button" class="btn btn-danger">' +
       '<span class="glyphicon glyphicon-trash"></span>' + '</button></div>');

    $mainDiv.append($spanName);
    $mainDiv.append($roleSelect);
    $mainDiv.append($removeButton);
    $('#sem_users_' + elemId).append($mainDiv);

    initRoleSelect($('#sem_role_' + elemId + '_' + userId));
    initRoleSelect($('.role-select'));

    $addButton.prop('disabled', false);
}

function remove_user(elemId, userId) {
    $('#btn_remove_user_' + elemId + '_' + userId).prop('disabled', true);
    $('#sem_user_' + elemId + '_' + userId).remove();
}

function add_type(elemId) {

    var $addButton = $('#btn_add_type_' + elemId);
    $addButton.prop('disabled', true);

    var $typeInput = $('#new_type_' + elemId);
    var typeName = $typeInput.val();
    if (typeName == '') {
        $addButton.prop('disabled', false);
        return;
    }

    // checking existence
    var found = false;
    $('.sem-type-' + elemId, '#sem_types_' + elemId).each(function() {
        if ($(this).data('name') == typeName) {
            found = true;
            return false;
        }
    });
    if (found) {
        $addButton.prop('disabled', false);
        //alert('Тип уже был добавлен!');
        return;
    }

    var typeId = tempId;
    tempId--;
    add_type_elems(typeId, typeName, elemId);

    $typeInput.val('');
    $addButton.prop('disabled', false);
}

function add_type_elems(typeId, typeName, elemId) {

    // creating elements
    var $mainDiv = $('<div class="row gap-top vertical-align sem-type-' + elemId + '" id="sem_type_' + elemId + '_' + typeId + '" ' +
        'data-name="' + typeName + '" data-id="' + typeId + '">' + '</div>');
    var $spanName = $('<div class="col-sm-5">' + typeName +'</div>');
    var $iconpicker = $('<div class="col-sm-2"><button class="btn btn-default iconpicker" id="sem_icon_' +
        elemId + '_' + typeId + '">' + '</button></div>');
    var $removeButton = $('<div class="col-sm-1"><button id="btn_remove_type_' + elemId + '_' + typeId + '" ' +
        'onclick="remove_type(\'' + elemId + '\',\'' + typeId + '\');" type="button" class="btn btn-danger">' +
        '<span class="glyphicon glyphicon-trash"></span>' + '</button></div>');

    $mainDiv.append($spanName);
    $mainDiv.append($iconpicker);
    $mainDiv.append($removeButton);
    $('#sem_types_' + elemId).append($mainDiv);

    initIconPicker($('#sem_icon_' + elemId + '_' + typeId));


    // adding to selects
    var $option1 = $('<option id="sem_cns_1_' + elemId + '_' + typeId + '" data-id="' + typeId +  '">' + typeName +'</option>');
    var $option2 = $('<option id="sem_cns_2_' + elemId + '_' + typeId + '" data-id="' + typeId + '">' + typeName +'</option>');

    var $semConnections1 = $('#sem_connections_1_' + elemId);
    var $semConnections2 = $('#sem_connections_2_' + elemId);
    $semConnections1.append($option1);
    $semConnections1.change();
    $semConnections2.append($option2);
    $semConnections2.change();
}

function remove_type(elemId, typeId) {
    var $addConnectionBtn = $('#btn_add_connection_' + elemId);

    $('#btn_remove_type_' + elemId + '_' + typeId).prop('disabled', true);
    $addConnectionBtn.prop('disabled', true);
    $('#sem_type_' + elemId + '_' + typeId).remove();

    // removing associated connections
    $('.sem-connection-' + elemId, '#sem_connections_' + elemId).each(function() {
        var $connection = $(this);
        if ($connection.data('type1id') == typeId || $connection.data('type2id') == typeId) {
            $connection.remove();
        }
    });

    // and from selects
    $('#sem_cns_1_' + elemId + '_' + typeId).remove();
    $('#sem_connections_1_' + elemId).change();
    $('#sem_cns_2_' + elemId + '_' + typeId).remove();
    $('#sem_connections_2_' + elemId).change();

    $addConnectionBtn.prop('disabled', false);
}

function add_connection(elemId) {

    var $addButton = $('#btn_add_connection_' + elemId);
    $addButton.prop('disabled', true);

    // checking for types
    var $select1 = $('#sem_connections_1_' + elemId);
    var $selected1 = $('#sem_connections_1_' + elemId + ' option:selected');
    var $select2 = $('#sem_connections_2_' + elemId);
    var $selected2 = $('#sem_connections_2_' + elemId + ' option:selected');
    if ($select1.has('option').length == 0 || $selected1 == null || $select2.has('option').length == 0 || $selected2 == null) {
        $addButton.prop('disabled', false);
        alert('Типы не выбраны!');
        return;
    }

    var type1Id = $selected1.data('id');
    var type2Id = $selected2.data('id');
    var type1Name = $selected1.text();
    var type2Name = $selected2.text();

    // checking existence
    var $checkConnection = $('#sem_connection_' + elemId + '_' + type1Id + '_' + type2Id);
    if ($checkConnection.length != 0) {
        $addButton.prop('disabled', false);
        //alert('Связь уже была добавлена!');
        return;
    }

    add_connection_elems(type1Id, type2Id, type1Name, type2Name, elemId);

    $addButton.prop('disabled', false);
}

function add_connection_elems(type1Id, type2Id, type1Name, type2Name, elemId) {
    // creating elements
    var $mainDiv = $('<div class="row gap-top vertical-align sem-connection-' + elemId + '" id="sem_connection_' + elemId + '_' + type1Id + '_' + type2Id +
        '" data-type1id="' + type1Id + '" data-type2id="' + type2Id +
        '" data-type1name="' + type1Name + '" data-type2name="' + type2Name + '">' + '</div>');
    var $spanName1 = $('<div class="col-sm-4">' + type1Name +'</div>');
    var $delimiter = $('<div class="col-sm-1"><b>' + '->' + '</b></div>');
    var $spanName2 = $('<div class="col-sm-4">' + type2Name +'</div>');
    var $removeButton = $('<div class="col-sm-1"><button id="btn_remove_connection_' + elemId + '_' + type1Id + '_' + type2Id + '" ' +
        'onclick="remove_connection(\'' + elemId + '\',\'' + type1Id + '\',\'' + type2Id + '\');" type="button" class="btn btn-danger">' +
        '<span class="glyphicon glyphicon-trash"></span>' + '</button></div>');

    $mainDiv.append($spanName1);
    $mainDiv.append($delimiter);
    $mainDiv.append($spanName2);
    $mainDiv.append($removeButton);
    $('#sem_connections_' + elemId).append($mainDiv);
}

function remove_connection(elemId, type1Id, type2Id) {
    $('#btn_remove_connection_' + elemId + '_' + type1Id + '_' + type2Id).prop('disabled', true);
    $('#sem_connection_' + elemId + '_' + type1Id + '_' + type2Id).remove();
}

function removeAllOntology(elemId) {
    $('.sem-type-' + elemId, '#sem_types_' + elemId).each(function() {
        $(this).remove();
    });
    $('.sem-connection-' + elemId, '#sem_connections_' + elemId).each(function() {
        $(this).remove();
    });

    $('option', '#sem_connections_1_' + elemId).each(function() {
        $(this).remove();
    });
    $('#sem_connections_1_' + elemId).change();

    $('option', '#sem_connections_2_' + elemId).each(function() {
        $(this).remove();
    });
    $('#sem_connections_2_' + elemId).change();
}

function set_ibis(elemId) {
    removeAllOntology(elemId);

    var ideaId = tempId;
    tempId--;
    var queId = tempId;
    tempId--;
    var argProId = tempId;
    tempId--;
    var argConId = tempId;
    tempId--;

    add_type_elems(ideaId, 'Идея', elemId);
    add_type_elems(queId, 'Вопрос', elemId);
    add_type_elems(argProId, 'Аргумент за', elemId);
    add_type_elems(argConId, 'Аргумент против', elemId);

    $('#sem_icon_' + elemId + '_' + ideaId).find('i').attr('class', 'fa fa-exclamation');
    $('#sem_icon_' + elemId + '_' + queId).find('i').attr('class', 'fa fa-question');
    $('#sem_icon_' + elemId + '_' + argProId).find('i').attr('class', 'fa fa-arrow-up');
    $('#sem_icon_' + elemId + '_' + argConId).find('i').attr('class', 'fa fa-arrow-down');

    add_connection_elems(ideaId, queId, 'Идея', 'Вопрос', elemId);
    add_connection_elems(queId, ideaId, 'Вопрос', 'Идея', elemId);
    add_connection_elems(queId, queId, 'Вопрос', 'Вопрос', elemId);
    add_connection_elems(argProId, ideaId, 'Аргумент за', 'Идея', elemId);
    add_connection_elems(argConId, ideaId, 'Аргумент против', 'Идея', elemId);
    add_connection_elems(queId, argProId, 'Вопрос', 'Аргумент за', elemId);
    add_connection_elems(queId, argConId, 'Вопрос', 'Аргумент против', elemId);
}

function getIconData(elemId, typeId) {
    var $pickerIcon = $('#sem_icon_' + elemId + '_' + typeId).find('i');
    var iconName = $pickerIcon.attr('class');
    if (typeof iconName === 'undefined') {
        return [null, null];
    }
    var iconSymbol = window.getComputedStyle($pickerIcon[0], ':before').content;
    var iconValue = '\\' + iconSymbol.charCodeAt(1).toString(16);
    return [iconName, iconValue];
}

function initRoleSelect($elems) {
    $elems.select2({
            minimumResultsForSearch: -1
        }
    );
}

function initIconPicker($elems) {
    $elems.iconpicker({
        iconset: 'fontawesome',
        placement: 'right'
    });
}

function initTypeIcons() {
    var parsedJSON = JSON.parse(tsJSON);
    var data = parsedJSON['data'];
    for (var semId in data) {
        if (data.hasOwnProperty(semId)) {
            var sem = data[semId];
            for (var typeId in sem) {
                if (sem.hasOwnProperty(typeId)) {
                    $('#sem_icon_' + semId + '_' + typeId).find('i').attr('class', sem[typeId]);
                }
            }
        }
    }
}

var $change_role_button;
var $admin_checkbox;
function initUserRolesBlock() {
    var $select = $('#all_users_select');
    $select.select2({
            'language': {
                'noResults': function() {
                    return 'Пользователи не найдены';
                }
            }
        }
    );

    $select.change(function() {
        var $selected = $('#all_users_select option:selected');
        $change_role_button.prop('disabled', true);
        if ($select.has('option').length == 0 || $selected == null) {
            $admin_checkbox.attr('checked', false);
            return false;
        }
        var isAdmin = $selected.attr('data-admin') == 'true';
        $admin_checkbox.prop('checked', isAdmin);
        $change_role_button.prop('disabled', true);
    });
    $select.change();

    $admin_checkbox.change(function() {
        var $selected = $('#all_users_select option:selected');
        if ($select.has('option').length == 0 || $selected == null) {
            $change_role_button.prop('disabled', true);
            $admin_checkbox.prop('checked', false);
            return false;
        }
        var matches = ($selected.attr('data-admin') == 'true') == $admin_checkbox.is(':checked');
        $change_role_button.prop('disabled', matches);
    });
}

function change_role() {

    var $select = $('#all_users_select');
    var $selected = $('#all_users_select option:selected');
    if ($select.has('option').length == 0 || $selected == null) {
        return false;
    }

    var $allButtons = $(':button');
    $allButtons.prop('disabled', true);

    // collecting data
    var res = {};
    res['id'] = $selected.attr('id');
    res['admin'] = $admin_checkbox.is(':checked');

    // send a post request
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $.ajax({
        type: 'POST',
        url: window.location.href + '/role',
        data: JSON.stringify(res),
        contentType: 'application/json',
        beforeSend: function(xhr){xhr.setRequestHeader(header, token);},
        complete: function (data) {
            var resp = data.responseText;
            if (resp == 'success') {
                window.location.href = window.location.href + "?success"
            }
            else {
                var $fb = $('#role_fb');
                if ($fb.hasClass('hidden')) {
                    $fb.removeClass('hidden');
                }
                $fb.text(resp);
                $allButtons.prop('disabled', false);
            }
        }
    });
}