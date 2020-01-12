var unionRectStart = null;
var unionRectEnd = null;

(function(){

    Renderer = function(canvas){
        var canvas = $(canvas).get(0);
        var ctx = canvas.getContext("2d");
        var gfx = arbor.Graphics(canvas);
        var particleSystem = null;

        var that = {
            init:function(system){
                particleSystem = system;
                particleSystem.screenSize(canvasWidth, canvasHeight);
                particleSystem.screenPadding(40);

                that.initMouseHandling()
            },

            redraw:function(){
                if (!particleSystem) return;

                gfx.clear(); // convenience ƒ: clears the whole canvas rect

                // draw the nodes & save their bounds for edge drawing
                var nodeBoxes = {};
                particleSystem.eachNode(function(node, pt){
                    // node: {mass:#, p:{x,y}, name:"", data:{}}
                    // pt:   {x:#, y:#}  node position in screen coords

                    if (node.data.fixedPoint) {
                        pt = arbor.Point(canvasWidth * node.data.fixedX, canvasHeight * node.data.fixedY);
                        node._p = particleSystem.fromScreen(pt);
                    }

                    // determine the box size and round off the coords if we'll be
                    // drawing a text label (awful alignment jitter otherwise...)
                    var label = node.data.label||"";
                    //var w = ctx.measureText(""+label).width + 10
                    var w = node.data.size;
                    if (!(""+label).match(/^[ \t]*$/)){
                        pt.x = Math.floor(pt.x);
                        pt.y = Math.floor(pt.y);
                    }
                    else {
                        label = null;
                    }

                    // color
                    var color;
                    if (searchMode) {
                        if (node.data.selectedSearch) {
                            color = node.data.color ? node.data.color : "#000000";
                        }
                        else {
                            color = "#C0C0C0"
                        }
                    }
                    else {
                        color = node.data.color ? node.data.color : "#000000";
                    }
                    if (node.data.selectedUnion) {
                        color = "#000000";
                    }
                    ctx.fillStyle = color;

                    if (node.data.shape=='dot'){
                        gfx.oval(pt.x-w/2, pt.y-w/2, w,w, {fill:ctx.fillStyle});
                        nodeBoxes[node.name] = [pt.x-w/2, pt.y-w/2, w,w];
                    }else{
                        gfx.rect(pt.x-w/2, pt.y-10, w,20, 4, {fill:ctx.fillStyle});
                        nodeBoxes[node.name] = [pt.x-w/2, pt.y-11, w, 22];
                    }

                    // draw the text
                    if (label){
                        ctx.font = "15px FontAwesome";
                        ctx.textAlign = "center";
                        ctx.fillStyle = "white";
                        ctx.fillText(label||"", pt.x, pt.y+6);
                    }
                });


                // draw the edges
                particleSystem.eachEdge(function(edge, pt1, pt2){
                    // edge: {source:Node, target:Node, length:#, data:{}}
                    // pt1:  {x:#, y:#}  source position in screen coords
                    // pt2:  {x:#, y:#}  target position in screen coords

                    if (edge.source.data.fixedPoint) {
                        pt1 = arbor.Point(canvasWidth * edge.source.data.fixedX, canvasHeight * edge.source.data.fixedY);
                    }
                    if (edge.target.data.fixedPoint) {
                        pt2 = arbor.Point(canvasWidth * edge.target.data.fixedX, canvasHeight * edge.target.data.fixedY);
                    }

                    var weight = edge.data.weight;
                    var color = edge.data.color;

                    if (!color || (""+color).match(/^[ \t]*$/)) color = null;

                    // find the start point
                    var tail = intersect_line_box(pt1, pt2, nodeBoxes[edge.source.name]);
                    var head = intersect_line_box(tail, pt2, nodeBoxes[edge.target.name]);

                    ctx.save();
                    ctx.beginPath();
                    ctx.lineWidth = (!isNaN(weight)) ? parseFloat(weight) : 1;
                    ctx.strokeStyle = (color) ? color : "#cccccc";
                    ctx.fillStyle = null;

                    ctx.moveTo(tail.x, tail.y);
                    ctx.lineTo(head.x, head.y);
                    ctx.stroke();
                    ctx.restore();

                    // draw an arrowhead if this is a -> style edge
                    if (edge.data.directed){
                        ctx.save();
                        // move to the head position of the edge we just drew
                        var wt = !isNaN(weight) ? parseFloat(weight) : 1;
                        var arrowLength = 6 + wt;
                        var arrowWidth = 2 + wt;
                        ctx.fillStyle = (color) ? color : "#cccccc";
                        ctx.translate(head.x, head.y);
                        ctx.rotate(Math.atan2(head.y - tail.y, head.x - tail.x));

                        // delete some of the edge that's already there (so the point isn't hidden)
                        ctx.clearRect(-arrowLength/2,-wt/2, arrowLength/2,wt);

                        // draw the chevron
                        ctx.beginPath();
                        ctx.moveTo(-arrowLength, arrowWidth);
                        ctx.lineTo(0, 0);
                        ctx.lineTo(-arrowLength, -arrowWidth);
                        ctx.lineTo(-arrowLength * 0.8, -0);
                        ctx.closePath();
                        ctx.fill();
                        ctx.restore();
                    }
                });

                if (unionRectStart != null && unionRectEnd != null) {
                    ctx.rect(unionRectStart.x, unionRectStart.y,
                        unionRectEnd.x - unionRectStart.x, unionRectEnd.y - unionRectStart.y);
                    ctx.stroke();
                }

                //console.log('redraw'); // test

            },
            initMouseHandling:function(){
                // no-nonsense drag and drop (thanks springy.js)
                var _mouseP = null;
                var selected = null;
                var nearest = null;
                var dragged = null;
                var tooltipShown = false;
                var wasDragged = false;
                var lastModalId = null;
                var _mouseClicked = null;
                var oldmass = 1;

                // set up a handler object that will initially listen for mousedowns then
                // for moves and mouseups while dragging
                var handler = {
                    moved:function(e){
                        var pos = $(canvas).offset();
                        _mouseP = arbor.Point(e.pageX-pos.left, e.pageY-pos.top);
                        nearest = particleSystem.nearest(_mouseP);

                        if (!nearest.node) return false;

                        if (dragged) return false;

                        var dis = nearest.node.data.size / 2;
                        selected = (nearest.distance < dis) ? nearest : null;

                        if (selected) {
                            if (! tooltipShown) {
                                tooltipShown = true;
                            }
                            var $tooltip = $('#graph_tooltip');
                            $tooltip.css({top: e.pageY, left: e.pageX });
                            $tooltip.attr('title', nearest.node.data.tooltip).tooltip('fixTitle');
                            $tooltip.tooltip('show');
                        }
                        else {
                            if (tooltipShown) {
                                $('#graph_tooltip').tooltip('hide');
                                tooltipShown = false;
                            }
                        }

                        return false;
                    },
                    clicked:function(e){
                        if (tooltipShown) {
                            $('[data-toggle="tooltip"]').tooltip('hide');
                            tooltipShown = false;
                        }
                        var pos = $(canvas).offset();
                        _mouseP = arbor.Point(e.pageX-pos.left, e.pageY-pos.top);
                        nearest = dragged = particleSystem.nearest(_mouseP);

                        var dis = nearest.node.data.size / 2;
                        selected = (nearest.distance < dis) ? nearest : null;

                        if (selected) {
                            if (dragged.node !== null) {
                                dragged.node.fixed = true;
                            }
                            unionRectStart = unionRectEnd = null;
                        }
                        else {
                            unionRectEnd = null;
                            if (isDisMaster) {
                                unionRectStart = _mouseP;
                            }
                            selected = dragged = null;
                            if (! reforming) {
                                startGraph(false);
                            }
                        }
                        restoreSelectedForUnion();
                        $(canvas).bind('mousemove', handler.dragged);
                        $(window).bind('mouseup', handler.dropped);

                        return false;
                    },
                    dragged:function(e){
                        var old_nearest = nearest && nearest.node._id;
                        var pos = $(canvas).offset();
                        var s = arbor.Point(e.pageX-pos.left, e.pageY-pos.top);

                        if (nearest && dragged !== null && dragged.node !== null) {
                            if (! wasDragged) {
                                wasDragged = true;
                            }
                            var p = particleSystem.fromScreen(s);
                            dragged.node._p = p;
                            var conv = particleSystem.toScreen(dragged.node._p);
                            dragged.node.data.fixedX = conv.x / canvasWidth;
                            dragged.node.data.fixedY = conv.y / canvasHeight;
                            dragged.node.data.fixedPoint = true;
                        }
                        else {
                            if (isDisMaster && unionRectStart != null &&
                                s.x > unionRectStart.x && s.y > unionRectStart.y) {
                                unionRectEnd = s;
                            }
                        }

                        return false;
                    },

                    dropped:function(e){
                        if (dragged != null && dragged.node !== null) {
                            dragged.node.fixed = false;
                        }
                        var openedUnion = false;
                        if (dragged != null && dragged.node != null && ! wasDragged) {
                            if (dragged.node.data.isUnion) {
                                openUnion(dragged.node.name);
                                openedUnion = true;
                            }
                            else {
                                var objId = dragged.node.data.objId;
                                if (objId != lastModalId) {
                                    fillModalData(objId);
                                    lastModalId = objId;
                                }
                                $('#view_modal').modal('show');
                            }
                        }
                        if (unionRectStart != null && unionRectEnd != null) {
                            selectNodesForUnion(unionRectStart, unionRectEnd);
                        }
                        //dragged.node.tempMass = 50;
                        dragged = null;
                        selected = null;
                        wasDragged = false;
                        $(canvas).unbind('mousemove', handler.dragged);
                        $(window).unbind('mouseup', handler.dropped);
                        _mouseP = null;
                        if (! openedUnion) {
                            considerStopping(500);
                        }
                        return false;
                    },

                    left:function(e){
                        if (tooltipShown) {
                            $('[data-toggle="tooltip"]').tooltip('hide');
                            tooltipShown = false;
                        }
                        return false;
                    }
                };
                $(canvas).mousemove(handler.moved);
                $(canvas).mousedown(handler.clicked);
                $(canvas).mouseleave(handler.left);

            }

        };

        // helpers for figuring out where to draw arrows (thanks springy.js)
        var intersect_line_line = function(p1, p2, p3, p4)
        {
            var denom = ((p4.y - p3.y)*(p2.x - p1.x) - (p4.x - p3.x)*(p2.y - p1.y));
            if (denom === 0) return false; // lines are parallel
            var ua = ((p4.x - p3.x)*(p1.y - p3.y) - (p4.y - p3.y)*(p1.x - p3.x)) / denom;
            var ub = ((p2.x - p1.x)*(p1.y - p3.y) - (p2.y - p1.y)*(p1.x - p3.x)) / denom;

            if (ua < 0 || ua > 1 || ub < 0 || ub > 1)  return false;
            return arbor.Point(p1.x + ua * (p2.x - p1.x), p1.y + ua * (p2.y - p1.y));
        };

        var intersect_line_box = function(p1, p2, boxTuple)
        {
            var p3 = {x:boxTuple[0], y:boxTuple[1]},
                w = boxTuple[2],
                h = boxTuple[3];

            var tl = {x: p3.x, y: p3.y};
            var tr = {x: p3.x + w, y: p3.y};
            var bl = {x: p3.x, y: p3.y + h};
            var br = {x: p3.x + w, y: p3.y + h};

            return intersect_line_line(p1, p2, tl, tr) ||
                intersect_line_line(p1, p2, tr, br) ||
                intersect_line_line(p1, p2, br, bl) ||
                intersect_line_line(p1, p2, bl, tl) ||
                false
        };

        return that
    }

})();