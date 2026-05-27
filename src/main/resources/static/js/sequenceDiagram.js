var response;
var cantPacket =0;
var current_packet = 1;
var pivotTable;
//var deploymentUrl = "http://192.168.1.50:9999";
var deploymentUrl = "https://analytics.ws-paicbd.com";



$("#scroll1").on("scroll", function(){
    $(".scroll2").scrollLeft($(this).scrollLeft());
});
$(".scroll2").on("scroll", function(){
    $("#scroll1").scrollLeft($(this).scrollLeft());
});

window.addEventListener('scroll', function(e) {
    var data_table = document.getElementById("select");
    var posTable = data_table.offsetTop;
    var scroll = window.scrollY;
    if(posTable >= scroll){
        $("#pivot-table").css("overflow-x","auto");
    }else{
        $("#pivot-table").css("overflow-x","unset");
    }
});


$("div#diagram").on("click","g.signal", function(e){
    $("#right").removeClass('hide');
    $("#left").removeClass('hide');
    $('#exampleModalCenter').modal('show');
    current_packet = parseInt(this.getAttribute("cont"));
    $("#exampleModalCenter .modal-body").html(response[current_packet]);
    if(current_packet == cantPacket)  $("#right").addClass('hide');
    if(current_packet ==1) $("#left").addClass('hide');
});

$("#close-modal").on("click",function(){
    $('#exampleModalCenter').modal('hide');
});

function nextInfoPacket(){
    current_packet = current_packet + 1;
    if(current_packet == cantPacket ){
        $("#right").addClass('hide');
    }else{
        $("#right").removeClass('hide');
        $("#left").removeClass('hide');
    }

    $("#exampleModalCenter .modal-body").html(response[current_packet]);
}

function previousInfoPacket(){
    current_packet = current_packet -1;
    if(current_packet ==1)
    {
        $("#left").addClass('hide');
    }else{
        $("#right").removeClass('hide');
        $("#left").removeClass('hide');
    }

    $("#exampleModalCenter .modal-body").html(response[current_packet]);
}

function buildDiagram(startDate, endDate, imsi, endtoend_id, ip_src, ip_dst, filterLogic){
    current_packet = 1;
    document.getElementById("diagram").innerHTML = "";
    //var request = new XMLHttpRequest();

    $.ajax({
        url : deploymentUrl+'/DRA/'+startDate+'/'+endDate+'/'+imsi+'/'+endtoend_id+'/'+ip_src+'/'+ip_dst+'/'+filterLogic+'/',
        contentType: "application/json",
        type: 'GET',

        success: function (data) {

            pivotTable = data.pop();
            $("#pivot-table table").html(pivotTable);

            $("#scroll1 div").width($("table#select").width());

            var sequence = data[0];
            cantPacket = data.length-1;
            response = data;
            var diagram = Diagram.parse(sequence);
            diagram.drawSVG("diagram", {theme: 'simple'});
            $('#loadingModal').modal('hide');
            $('#download-pcap-btn').prop('disabled', false);
        }
    });

}

function updateDiagram(){
    var startDate = document.getElementById('start_date').value;
    var endDate = document.getElementById('end_date').value;
    var imsi = document.getElementById('imsi').value;
    var endtoend_id = document.getElementById('endtoend_id').value;
    var ip_src = document.getElementById('ip_src').value;
    var ip_dst = document.getElementById('ip_dst').value;
    var filterLogic = document.getElementById('filter_logic').value;

    if(startDate == ""){
        toastr.error('Please add an start date');
        document.getElementById('start_date').focus();
        return;
    }

    if(endDate == ""){
        toastr.error('Please add an end date');
        document.getElementById('end_date').focus();
        return;
    }

    $('#loadingModal').modal('show');

    if(imsi == "")
        imsi = "NA";

    if(endtoend_id == "")
        endtoend_id = "NA";

    if(ip_src == "")
        ip_src = "NA";

    if(ip_dst == "")
        ip_dst = "NA";

    if(filterLogic == "")
        filterLogic = "AND";

    console.log(startDate, endDate, imsi, endtoend_id, ip_src, ip_dst, filterLogic);
    buildDiagram(startDate, endDate, imsi, endtoend_id, ip_src, ip_dst, filterLogic);

    /*
    $("#pivot-table table tbody tr td input.generate_pcap").on("click",function (){
        $(this).toggleClass("generate_pcap");
    });*/
}

function buildPcap(){
    $('#loadingModal').modal('show');
    let json_array = [];
    let row_select = $("#pivot-table table tbody tr > td input.generate_pcap:checked").parent().parent();
    if(row_select.length > 0){
        for(let i=0;i<row_select.length;i++){
            json_array.push({json:row_select[i].querySelector(".raw_json").innerText});
        }
    }

    $.ajax({
        //url : "https://analytics.ws-paicbd.com/JSONtoPCAP",
        url : deploymentUrl+"/JSONtoPCAP",
        contentType: "application/json",
        type: 'POST',
        data: JSON.stringify(json_array),
        success: function (data) {
            //console.log(data);
            window.location.href = deploymentUrl+'/download';
            $('#loadingModal').modal('hide');
        }
    });

}


