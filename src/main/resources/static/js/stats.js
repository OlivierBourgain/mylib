Chart.defaults.global.defaultFontColor = 'white'
Chart.defaults.global.legend.display = false
Chart.defaults.scale.ticks.beginAtZero = true;
Chart.defaults.scale.ticks.autoSkip = false;
Chart.defaults.scale.ticks.callback = truncate;
Chart.defaults.scale.maxBarThickness = 40

Chart.defaults.global.tooltips.callbacks.title = function(items, data) {return data.labels[items[0].index]};
Chart.defaults.horizontalBar.tooltips.callbacks.title = Chart.defaults.global.tooltips.callbacks.title

var chartDetail;

$(function () {
    $(".fa-expand").click(function () {
        var parent = $(this).parent()
        showDetail(parent)
    });

    $(".fa-times").click(function () {
        $('#stat-extended').addClass('hidden')
    });

    // Check box show discarded
    $("#showDisc").click(function () {
        var checked = $(this)[0].checked
        var newUrl = URI(window.location.href)
        if (checked)
            newUrl.setSearch("showDisc", "true").toString()
        else
            newUrl.setSearch("showDisc", "").toString()
        window.location.href = newUrl
    });

    // Check box show discarded
    $("#year").change(function () {
        var newUrl = URI(window.location.href)
        newUrl.setSearch("year", this.value ).toString()
        window.location.href = newUrl
    });
});

// Show the stat detail.
// Parent is the stat that was clicked, and for which we want to show the detail
function showDetail(parent) {
    var stat = parent.attr("id")
    $('#statdetail').css('background', parent.css('background-image'));
    $('#statdetail>h4').text(parent.find('h4').text())
    $('#stat-extended').removeClass('hidden')
    var discarded= $("#showDisc")[0].checked
    var year = $("#year")[0].value

    console.log("Detail for " + stat + ", year=" + year);

    $.ajax({
        url: '/stat/' + stat + "?showDisc=" + discarded+ "&year=" + year,
        type: 'GET',
        dataType: 'json',
        success: function (chartData, status) {
            if (chartDetail) chartDetail.destroy();
            chartDetail = new Chart($("#detailChart"), {
                type: 'bar',
                data: chartData
            });
        },
        error: function (resultat, statut, erreur) {
            console.log("Error " + erreur)
        }
    });


}

function truncate(value) {
    if (value.length <= 20) return value;
    return value.substr(0, 18) + '\u2026'; // If truncated, add triple dot
}