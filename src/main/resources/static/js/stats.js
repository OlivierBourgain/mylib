Chart.defaults.global.defaultFontColor = 'white'
Chart.defaults.global.legend.display = false
Chart.defaults.scale.ticks.beginAtZero = true;
Chart.defaults.scale.maxBarThickness = 40

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
});

// Show the stat detail.
// Parent is the stat that was clicked, and for which we want to show the detail
function showDetail(parent) {
    var stat = parent.attr("id")
    $('#statdetail').css('background', parent.css('background-image'));
    $('#statdetail>h4').text(parent.find('h4').text())
    $('#stat-extended').removeClass('hidden')
    var discarded= $("#showDisc")[0].checked

    console.log("Detail for " + stat);

    $.ajax({
        url: '/stat/' + stat + "?showDisc=" + discarded,
        type: 'GET',
        dataType: 'json',
        success: function (chartData, status) {
            if (chartDetail) chartDetail.destroy();
            chartDetail = new Chart($("#detailChart"), {
                type: 'bar',
                data: chartData,
                options: {
                    scales: {
                        xAxes: [{
                            ticks: {
                                autoSkip: false,
                                callback: truncate,
                            }
                        }]
                    },
                    tooltips: {
                        enabled: true,
                        mode: 'label',
                        callbacks: {
                            title: function (tooltipItems, data) {
                                var idx = tooltipItems[0].index;
                                return data.labels[idx];//Use full label for tooltip
                            },
                        }
                    },
                }
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