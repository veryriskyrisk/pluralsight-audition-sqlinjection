$('#debugInfo').on('shown.bs.collapse', function () {
    $('html, body').animate({
        scrollTop: $("#debugInfo").offset().top
    }, 2000);
});
