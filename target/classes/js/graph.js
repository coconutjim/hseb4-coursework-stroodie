$( document ).ready(function() {
    var $canvas = $('#viewport');
    canvasWidth = $canvas.width();
    canvasHeight = $canvas.height();
    $new_union_button = $('#editor_union_button_new');
    $reform_graph_button = $('#reform_graph_button');
    initGraph();

    resizeCanvas();
    $( window ).resize(function() {
        resizeCanvas();
    });
});

var canvasWidth = 0;
var canvasHeight = 0;
function resizeCanvas() {
    var canvas = $('#viewport').get(0);
    var ctx = canvas.getContext("2d");
    ctx.canvas.width = $('#cnv_wrap').width();
    canvasWidth = ctx.canvas.width;
    startGraph(false);
    setTimeout( function() { considerStopping(0); } , 1500);
    //console.log(canvasWidth);
    //console.log(canvasHeight);
}

var sys = null;
var edgeData = {'directed':true,'weight':2,'color':'#000000'};

var allMessages = {};
var nextMessages = {};
var unionMessages = {};
var allUnions = {};
var openedUnions = {};
var unionConnections = {};

var reforming = true;
var searchMode = false;

var $new_union_button;
var $reform_graph_button;

function initGraph() {
    sys = arbor.ParticleSystem(3000, 200, 0.6, true, 36, 0.02, 0.3);
    sys.renderer = Renderer("#viewport");

    //console.log(msJSON);

    var msData = JSON.parse(msJSON);
    var unions = msData['unions'];
    var messages = msData['messages'];
    var hasNotFixed = false;

    var i = 0;
    var node = null;
    var name = null;
    for (; i < unions.length; ++i) {
        var u = unions[i];
        name = u['uid'];
        node = sys.addNode(name, {'fixed':true,'color':'#483D8B','shape':'dot',
            'label':u['name'], 'first': false, 'tooltip':'Открыть объединение', 'objId':u['id'], 'selectedSearch':false,
            'unionId':u['unionId'], 'prevId':u['prevId'],
            'fixedPoint':u['fixed'], 'fixedX':u['x'], 'fixedY':u['y'], 'selectedUnion':false, 'isUnion':true, 'size':100});
        allUnions[name] = node;
        openedUnions[name] = false;
        unionMessages[name] = [];
        unionConnections[name] = {};
        unionConnections[name]['prev'] = [];
        unionConnections[name]['next'] = [];
        if (! u['fixed']) {
            hasNotFixed = true;
        }
    }

    i = 0;
    for (; i < messages.length; ++i) {
        var m = messages[i];
        name = m['uid'];
        var text = m['first'] ? '' : String.fromCharCode(parseInt(m['icon'].substr(1), 16));
        var size = m['first'] ? 50 : 25;
        node = sys.addNode(name, {'fixed':true,'color':m['color'],'shape':'dot',
            'label':text, 'first': m['first'], 'tooltip':m['name'], 'objId':m['id'], 'selectedSearch':false,
            'unionId':m['unionId'], 'prevId':m['prevId'],
            'fixedPoint':m['fixed'], 'fixedX':m['x'], 'fixedY':m['y'], 'selectedUnion':false, 'isUnion':false,
            'size':size});
        var unId = m['unionId'];
        if (unId != -1) {
            unionMessages[unId].push(node);
            if (m['prevId'] != -1) {
                unionConnections[unId]['prev'].push(m['prevId']);
            }
            if (m['next'].length > 0) {
                var ns = unionConnections[unId]['next'];
                unionConnections[unId]['next'] = ns.concat(m['next']);
            }
            sys.pruneNode(m['uid']);
        }
        allMessages[name] = node;
        nextMessages[name] = m['next'];
        if (! m['fixed'] && m['unionId'] == -1) {
            hasNotFixed = true;
        }
    }

    for (var id in allMessages) {
        if (! allMessages.hasOwnProperty(id)) {
            continue;
        }
        var message = allMessages[id];
        var prevId = message.data.prevId;
        if (prevId == -1) {
            continue;
        }
        var prevMessage = allMessages[prevId];
        var prevUnionId = prevMessage.data.unionId;
        if (prevUnionId != -1) {
            prevMessage = allUnions[prevUnionId];
        }

        var unionId = message.data.unionId;
        if (unionId != -1) {
            message = allUnions[unionId];
        }
        sys.addEdge(message, prevMessage, edgeData);
    }

    makeUnionConnectionsUnique();

    if (hasNotFixed) {
        $reform_graph_button.text('Стоп');
    }
}

function updateGraph(data) {
    restoreRectangleAndSelected();

    if (! reforming) {
        startGraph(false);
    }
    if (data != null && data.length != 0) {
        for (var i = 0; i < data.length; ++i) {
            var message = allMessages[data[i]];
            var union = null;
            var unionId = message.data.unionId;
            if (unionId != -1) {
                union = allUnions[unionId];
            }
            if (message) {
                message.data.selectedSearch = true;
            }
            if (union != null) {
                union.data.selectedSearch = true;
            }
        }
    }
    searchMode = true;
    considerStopping(1000);
}

function restoreGraph() {
    searchMode = false;
    startGraph(false);
    unionRectStart = null;
    unionRectEnd = null;
    restoreRectangleAndSelected();
    considerStopping(1000);
}

function fixGraph() {

    var $fixButton = $('#fix_graph_button');
    $fixButton.prop('disabled', true);

    var res = {};

    var $canvas = $('#viewport');
    var width = $canvas.width();
    var height = $canvas.height();

    sys.eachNode(function(node, pt){
        var p = {};
        var conv = sys.toScreen(node.p);
        p['x'] = conv.x / width;
        p['y'] = conv.y / height;
        res[node.name] = p;
    });

    // send a post request
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $.ajax({
        type: 'POST',
        url: window.location.href + '/fix',
        data: JSON.stringify(res),
        contentType: 'application/json',
        beforeSend: function(xhr){xhr.setRequestHeader(header, token);},
        complete: function (data) {
            var resp = data.responseText;
            if (resp == 'success') {
                window.location.reload(true);
            }
            else {
                window.location.href='/stroodie/400';
                $fixButton.prop('disabled', false);
            }
        }
    });
}

function reformGraph() {

    $reform_graph_button.prop('disabled', true);

    if (! reforming) {
        sys.parameters({repulsion:3000});

        var nodes = [];
        var edges = [];
        sys.eachNode(function(node, pt){
            node.data.fixedPoint = false;
            nodes.push(node);
        });
        sys.eachEdge(function(edge, pt1, pt2){
           edges.push(edge);
        });
        var i = 0;
        for (; i < nodes.length; ++i) {
            sys.pruneNode(nodes[i].name);
            nodes[i]._p = sys.fromScreen(arbor.Point(0.5 * canvasWidth, 0.5 * canvasHeight));
        }
        i = 0;
        for (; i < nodes.length; ++i) {
            sys.addNode(nodes[i].name, nodes[i].data);
        }
        i = 0;
        for (; i < edges.length; ++i) {
            sys.addEdge(sys.getNode(edges[i].source.name), sys.getNode(edges[i].target.name), edges[i].data);
        }
        reforming = true;
        restoreRectangleAndSelected();
    }
    else {
        sys.eachNode(function(node, pt){
            var conv = sys.toScreen(node._p);
            node.data.fixedX = conv.x / canvasWidth;
            node.data.fixedY = conv.y / canvasHeight;
            node.data.fixedPoint = true;
        });
        stopGraph(100);
    }

    $reform_graph_button.text(reforming ? 'Стоп' : 'Перестроить граф');
    $reform_graph_button.prop('disabled', false);
}

var selectedUnionM = [];
var selectedUnionU = [];
function selectNodesForUnion(rectStart, rectEnd) {
    selectedUnionM = [];
    selectedUnionU = [];
    sys.eachNode(function(node, pt){
        if (pt.x >= rectStart.x && pt.x <= rectEnd.x && pt.y >= rectStart.y && pt.y <= rectEnd.y) {
            node.data.selectedUnion = true;
            if (node.data.isUnion) {
                selectedUnionU.push(node.data.objId);
            }
            else {
                selectedUnionM.push(node.data.objId);
            }
        }
    });
    if ( (selectedUnionU.length + selectedUnionM.length) > 1) {
        if ($new_union_button.hasClass('hidden')) {
            $new_union_button.removeClass('hidden');
        }
    }
    else {
        if (! $new_union_button.hasClass('hidden')) {
            $new_union_button.addClass('hidden')
        }
    }
}

function restoreSelectedForUnion() {
    selectedUnionM = [];
    selectedUnionU = [];
    for (var id in allMessages) {
        if (! allMessages.hasOwnProperty(id)) {
            continue;
        }
        allMessages[id].data.selectedUnion = false;
    }
    for (var unId in allUnions) {
        if (! allUnions.hasOwnProperty(unId)) {
            continue;
        }
        allUnions[unId].data.selectedUnion = false;
    }
    sys.eachNode(function(node, pt){
        node.data.selectedUnion = false;
    });
    if (! $new_union_button.hasClass('hidden')) {
        $new_union_button.addClass('hidden')
    }
}

function restoreRectangleAndSelected() {
    unionRectStart = null;
    unionRectEnd = null;
    restoreSelectedForUnion();
}

function openUnion(unionId) {
    restoreRectangleAndSelected();

    sys.parameters({repulsion:10});

    sys.pruneNode(unionId);
    var unMessages = unionMessages[unionId];
    var i = 0;
    for (; i < unMessages.length; ++i) {
        var m = unMessages[i];
        sys.addNode(m.name, m.data);
    }
    i = 0;
    for (; i < unMessages.length; ++i) {
        var message = unMessages[i];
        var nodeId;
        var prevId = message.data.prevId;
        if (prevId != -1) {
            nodeId = prevId;
            var prevM = allMessages[prevId];
            var punId = prevM.data.unionId;
            if (punId != -1 && punId != unionId && ! openedUnions[punId]) {
                nodeId = punId;
            }
            sys.addEdge(sys.getNode(message.name), sys.getNode(nodeId), edgeData);
        }
        var nextMes = nextMessages[message.name];
        for (var j = 0; j < nextMes.length; ++j) {
            var nextM = allMessages[nextMes[j]];
            nodeId = nextM.name;
            var nunId = nextM.data.unionId;
            if (nunId != -1 && nunId != unionId && ! openedUnions[nunId]) {
                nodeId = nunId;
            }
            sys.addEdge(sys.getNode(nodeId), sys.getNode(message.name), edgeData);
        }
    }

    setTimeout( function() { considerStopping(0); } , 500);

    openedUnions[unionId] = true;

    var $div = $('<div class="small-gap-top">' + '</div>');
    var $closeBtn = $('<button class="btn btn-primary btn-xs" id="btn_close_union_' + unionId +
        '" onclick="closeUnion(' + unionId + ');">' +
        'Свернуть "' + allUnions[unionId].data.label + '"' + '</button>');
    $div.append($closeBtn);
    $('#close_unions_block').append($div);
}

function closeUnion(unionId) {
    restoreRectangleAndSelected();

    var $closeBtn = $('#btn_close_union_' + unionId);
    $closeBtn.prop('disabled', true);

    var unMessages = unionMessages[unionId];
    var i = 0;
    for (; i < unMessages.length; ++i) {
        sys.pruneNode(unMessages[i].name);
    }

    var un = allUnions[unionId];
    var union = sys.addNode(un.name, un.data);

    var message = null;
    var nodeId = null;

    var ps = unionConnections[unionId]['prev'];
    var conSet = {};
    i = 0;
    for (; i < ps.length; ++i) {
        message = allMessages[ps[i]];
        nodeId = message.name;
        var punId = message.data.unionId;
        if (punId != -1 && ( punId == unionId || ! openedUnions[punId] )) {
            nodeId = punId;
        }
        if (conSet[nodeId] !== 1) {
            sys.addEdge(union, sys.getNode(nodeId), edgeData);
            conSet[nodeId] = 1;
        }
    }

    var ns = unionConnections[unionId]['next'];
    conSet = {};
    i = 0;
    for (; i < ns.length; ++i) {
        message = allMessages[ns[i]];
        nodeId = message.name;
        var nunId = message.data.unionId;
        if (nunId != -1 && ( nunId == unionId || ! openedUnions[nunId] )) {
            nodeId = nunId;
        }
        if (conSet[nodeId] !== 1) {
            sys.addEdge(sys.getNode(nodeId), union, edgeData);
            conSet[nodeId] = 1;
        }
    }

    setTimeout( function() { considerStopping(0); } , 500);

    openedUnions[unionId] = false;

    $closeBtn.parent().remove();
}

function makeUnionConnectionsUnique() {
    for (var unId in unionConnections) {
        if (! unionConnections.hasOwnProperty(unId)) {
            continue;
        }
        unionConnections[unId]['prev'] = uniqueArr(unionConnections[unId]['prev']);
        unionConnections[unId]['next'] = uniqueArr(unionConnections[unId]['next']);
    }
}
function uniqueArr(arr) {
    var unArr = [];
    var testSet = {};
    var i = 0;
    for (; i < arr.length; ++i) {
        var elem = arr[i];
        if (testSet[elem] !== 1) {
            unArr.push(elem);
            testSet[elem] = 1;
        }
    }
    return unArr;
}

function union_crud(mode, elemId) {

    var $allButtons = $(':button');
    $allButtons.prop('disabled', true);

    // collecting data
    var res = {};
    res['mode'] = mode;
    res['id'] = mode == 'create' ? '-1' : elemId;
    if (mode != 'delete') {
        res['name'] = $('#union_name_' + elemId).val();
        if (mode == 'create') {
            res['mesIds'] = selectedUnionM;
            res['unionIds'] = selectedUnionU;
        }
    }

    // send a post request
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $.ajax({
        type: 'POST',
        url: window.location.href + '/union',
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
                        var $fb = $('#union_fb_' + elemId);
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

function considerStopping(timeout) {
    var hasNotFixed = false;
    sys.eachNode(function(node, pt){
        if (! node.data.fixedPoint) {
            hasNotFixed = true;
        }
    });
    if (! hasNotFixed) {
        stopGraph(timeout);
    }
    else {
        startGraph(true);
    }
}

function startGraph(changeLabel) {
    if (changeLabel) {
        $reform_graph_button.text('Стоп');
    }
    reforming = true;
    sys.start();
}

function stopGraph(timeout) {
    $reform_graph_button.text('Перестроить граф');
    reforming = false;
    setTimeout( function() { sys.stop(); } , timeout);
}
