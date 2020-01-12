$( document ).ready(function() {
    var top_offset = 100;
    if ( ($(window).height() + top_offset) < $(document).height() ) {
        $('#go_top_element').removeClass('hidden').affix( {
            offset: { top: top_offset }
        });
    }
    initNotificationPopover();
});

function go_page_top() {
    $('html, body').animate( {scrollTop: 0 }, 'fast');
    return false;
}

var $notificationBlock;
function initNotificationPopover() {
    $notificationBlock = $("#notification_block");
    $notificationBlock.remove();
    $("#notification_popover").popover({
        placement: 'bottom',
        html : true,
        container: '#notification_popover',
        content: function() {
            return $notificationBlock.html();
        }
    });
}