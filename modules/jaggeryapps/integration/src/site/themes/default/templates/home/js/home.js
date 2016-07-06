// page initialization
$(document).ready(function() {
    // add upload app icon listener
    $("#change_app_icon").change(function(event) {
        submitChangeAppIcon(this);
    });
    initPageView();
    var nextVersion = generateNextPossibleVersion(application.versions);
    var uploadRevisionUrl = appCreationPageBaseUrl+"?appTypeName="+application.applicationType +
                            "&applicationName="+applicationName + "&encodedLabels="+encodedLabels + "&encodedEnvs="
                                    + encodedEnvs + "&newVersion=true&nextVersion=" + nextVersion + "&conSpecCpu=" + conSpecCpu + "&conSpecMemory=" + conSpecMemory;
    $('#upload-revision').attr("href", uploadRevisionUrl);

    if(selectedApplicationRevision.status=='inactive'){
        displayApplicationInactiveMessage();
    }
});

// wrapping functions
function initPageView() {
    loadAppIcon();
    var deploymentURL = selectedApplicationRevision.deploymentURL;
    var repoUrlHtml = generateLunchUrl(deploymentURL);
    $("#version-url-link").html(repoUrlHtml);
    $('#appVersionList li').click(function() {
        var newRevision = this.textContent;
        changeSelectedRevision(newRevision);
    });

    $('body').on('click', '#btn-launchApp', function() {
        var appUrl = $('#btn-launchApp').attr("url");
        var newWindow = window.open('','_blank');
        newWindow.location = appUrl;
    });

    $('#btn-dashboard').click(function() {
        var appUrl = $('#btn-dashboard').attr("url");
        var newWindow = window.open('','_blank');
        newWindow.location = appUrl;
    });

    listTags();
}
/**
 * This function is to display a message to user to inform that the application is stopped due to
 * being idle for longer period.
 */
function displayApplicationInactiveMessage() {
    jagg.message({
                     modalStatus: true,
                     type: 'warning',
                     timeout: 15000,
                     content: "<b>This application has been stopped due to inactivity for more than 24 hours</b></br>" +
                              "This is a limitation of free accounts in App Cloud.</br> To restart, click the <b>Start</b>. button.</br>" +
                              "Click the Support menu to contact us if you need any help."
                 });
}

function listTags(){
    var tags = selectedApplicationRevision.tags;
    var tagListLength;
    if(tags) {
        tagListLength = tags.length;
    }
    var tagString = '';
    for(var i = 0; i < tagListLength; i++){
        if(i >= 3){
            break;
        }
        tagString += tags[i].labelName + " : " + tags[i].labelValue + "</br>";
    }
    if(tagListLength > 3) {
        tagString += "</br><a class='view-tag' href='/integration/site/pages/tags.jag?applicationKey=" + applicationKey
                             + "&versionKey=" + selectedApplicationRevision.hashId + "'>View All Tags</a>";
    }

    $('#tag-list').html(tagString);
}

// Icon initialization
function loadAppIcon() {

    application = getIconDetail(application);

    var iconDiv;
    if(application.icon){
        iconDiv = '<img id="app-icon"  src="data:image/bmp;base64,' + application.icon + '" width="100px"/>'
    } else {
        iconDiv = '<div class="app-icon" style="background:' + application.uniqueColor + '">' +
                  '<i class="fw ' + application.appTypeIcon + ' fw-4x" data-toggle="tooltip" ></i>' +
                  '</div>';
    }

    $("#app-icon").html(iconDiv);
}

function changeSelectedRevision(newRevision){
    // change app description

    //Changing revision dropdown
    putSelectedRevisionToSession(applicationKey, newRevision);
    $('#selected-version').html(newRevision+" ");
    $("#selectedRevision").val(newRevision);
    selectedRevision = newRevision;
    selectedApplicationRevision = application.versions[newRevision];
    //Changing deploymentURL
    var deploymentURL = selectedApplicationRevision.deploymentURL;
    var repoUrlHtml = generateLunchUrl(deploymentURL);
    $("#version-url-link").html(repoUrlHtml);
    $('#btn-launchApp').attr({url:deploymentURL});

    var dashboardUrl = dashboardBaseUrl + applicationName + "-" + newRevision;
    $('#btn-dashboard').attr({url:dashboardUrl});

    //changing app description
    $("#app-description").text(application.description?application.description:'');

    //changing runtime
    $("#runtime").html(selectedApplicationRevision.runtimeName);

    //change icon
    loadAppIcon();

    // Change replica status
    $("#tableStatus").html(selectedApplicationRevision.status);

    //Change Env Variables
    $("#leftMenuEnvVars").attr('href',"/integration/site/pages/envs.jag?applicationKey=" + applicationKey + "&versionKey=" + selectedApplicationRevision.hashId);
    $("#envVars").attr('href',"/integration/site/pages/envs.jag?applicationKey=" + applicationKey + "&versionKey=" + selectedApplicationRevision.hashId);
    $("#envVarsAdd").attr('href',"/integration/site/pages/envs.jag?applicationKey=" + applicationKey + "&versionKey=" + selectedApplicationRevision.hashId);
    $("#runtimePropCount").text(selectedApplicationRevision.runtimeProperties.length.toString());

    //Change Tags
    $("#leftMenuTagSet").attr('href',"/integration/site/pages/tags.jag?applicationKey=" + applicationKey + "&versionKey=" + selectedApplicationRevision.hashId);
    $("#tagSet").attr('href',"/integration/site/pages/tags.jag?applicationKey=" + applicationKey + "&versionKey=" + selectedApplicationRevision.hashId);
    $("#tagSetAdd").attr('href',"/integration/site/pages/tags.jag?applicationKey=" + applicationKey + "&versionKey=" + selectedApplicationRevision.hashId);
    $("#tagCount").text(selectedApplicationRevision.tags.length.toString());
    listTags();

    // Change version status in UI
    if(selectedApplicationRevision.status == 'running'){

        $('#version-app-launch-block').empty();
        $('#version-app-launch-block').html('<button class="cu-btn cu-btn-md cu-btn-gr-dark btn-launch-app" id="btn-launchApp"' +
                       'url="' + deploymentURL + '">Launch App</button>' +
                       '<div class="btn-group ctrl-edit-button btn-edit-code"><a type="button" ' +
                       'class="btn cu-btn cu-btn-md cu-btn-red" onclick="stopApplication();">Stop' +
                       '<span id="stop-in-progress"><span></a></div><div class="btn-group ctrl-edit-button btn-edit-code">' +
                       '<a type="button" class="btn cu-btn cu-btn-md cu-btn-gray" onclick="redeployApplication();">' +
                       'Redeploy<span id="redeploy-in-progress"><span></a></div>');

        $('.block-replica').empty();
        $('.block-replica').html('<h3>Replicas</h3><div class="block-replicas"><figure class="node-cicle" ' +
                                 'data-percent="100"><figcaption>01</figcaption><svg width="200" height="200">' +
                                 '<circle class="outer" cx="95" cy="95" r="85" transform="rotate(-90, 95, 95)"/></svg>' +
                                 '<a href="/integration/site/pages/runtimeLogs.jag?applicationKey=' + applicationKey + '&selectedRevision=' + newRevision +
                                 '"><span class="view-log">View Logs</span></a></figure></div><div class="block-replicas">' +
                                 '<figure class="node-cicle"><figcaption><span class="fw-stack fw-lg ">' +
                                 '<i class="fw fw-ring fw-stack-2x"></i><i class="fw fw-add fw-stack-1x" ' +
                                 'data-toggle="tooltip" title="Adding replicas to your application will not support in this release."></i>' +
                                 '</span></figcaption></figure></div>');

    } else if(selectedApplicationRevision.status == 'stopped'){

        $('#version-app-launch-block').empty();
        $('#version-app-launch-block').html('<button class="cu-btn cu-btn-md cu-btn-gr-dark btn-launch-app" id="btn-launchApp"' +
                       'url="' + deploymentURL + '" disabled>Launch App</button>' +
                       '<div class="btn-group ctrl-edit-button btn-edit-code"><a type="button" ' +
                       'class="btn cu-btn cu-btn-md cu-btn-blue" onclick="startApplication();">Start</a></div>');

        $('.block-replica').empty();
        $('.block-replica').html('<h3>Replicas</h3><div class="block-replicas"><figure class="node-cicle" data-percent="100">' +
                                 '<figcaption>01</figcaption><svg width="200" height="200"><circle class="outer" ' +
                                 'style="stroke: #ACAFAD;" cx="95" cy="95" r="85" transform="rotate(-90, 95, 95)"/></svg>' +
                                 '</figure></div><div class="block-replicas"><figure class="node-cicle"><figcaption>' +
                                 '<span class="fw-stack fw-lg "><i class="fw fw-ring fw-stack-2x"></i>' +
                                 '<i class="fw fw-add fw-stack-1x" data-toggle="tooltip"' +
                                 ' title="Adding replicas to your application will not support in this release.">' +
                                 '</i></span></figcaption></figure></div>');
    } else {

        $('#version-app-launch-block').empty();
        $('#version-app-launch-block').html('<button class="cu-btn cu-btn-md cu-btn-gr-dark btn-launch-app" id="btn-launchApp" ' +
                       'url="' + deploymentURL + '" disabled>Launch App</button>' +
                       '<div class="btn-group ctrl-edit-button btn-edit-code"><a type="button" ' +
                       'class="btn cu-btn cu-btn-md cu-btn-red" href="#yourlink">Error has occurred.</a></div>');

    }

    // Set upload revision btn
    var uploadRevisionUrl = appCreationPageBaseUrl+"?appTypeName="+application.applicationType + //"&applicationName="+applicationName;
                            "&applicationName="+applicationName + "&encodedLabels="+encodedLabels + "&encodedEnvs="
                                    + encodedEnvs + "&newVersion=true&nextVersion=" + nextVersion;
    $('#upload-revision').attr("href", uploadRevisionUrl);

    changeRuntimeProps(selectedApplicationRevision);
    changeLabels(selectedApplicationRevision);
}

function generateLunchUrl(appURL) {
    var message = "";
    if(appURL) {
        message += "<a target='_blank' href='" + appURL + "' >";
        message += "<span>";
        message += "<b>URL : </b>";
        message += appURL;
        message += "</span>";
        message += "</a>";
    } else {
        message += "<i class='fw fw-deploy fw-1x'></i><span>Application is still deploying</span>";
    }
    return message;
}

function putSelectedRevisionToSession(applicationKey, selectedRevision){
    jagg.syncPost("../blocks/home/ajax/get.jag", {
        action: "putSelectedRevisionToSession",
        applicationKey: applicationKey,
        selectedRevision: selectedRevision
    });
}

function changeRuntimeProps(selectedApplicationRevision){
    $('#runtimePropCount').html(selectedApplicationRevision.runtimeProperties.length);
}

function changeLabels(selectedApplicationRevision){
    $('#labelCount').html(selectedApplicationRevision.tags.length);
}

// Uploading application icon
function submitChangeAppIcon(newIconObj) {
    var validated = validateIconImage(newIconObj.value, newIconObj.files[0].size);
    if(validated) {
        $('#changeAppIcon').submit();
    } else {
        jagg.message({content: "Invalid image selected for Application Icon - Select a valid image", type: 'error', id:'notification'});
    }
}

// check the file is an image file
function validateIconImage(filename, fileSize) {
    var ext = getFileExtension(filename);
    var extStatus = false;
    var fileSizeStatus = true;
    switch (ext.toLowerCase()) {
        case 'jpg':
        case 'jpeg':
        case 'gif':
        case 'bmp':
        case 'png':
            extStatus = true;
            break;
        default:
            jagg.message({content: "Invalid image selected for Application Icon - Select a valid image", type: 'error', id:'notification'});
            break;
    }

    if((fileSize/1024) > 51200 && extStatus == true) {
        fileSizeStatus = false;
        jagg.message({content: "Image file should be less than 5MB", type: 'error', id:'notification'});
    }
    if(extStatus == true && fileSizeStatus == true) {
        return true;
    }
    return false;
}

// Utility Functions Goes Here
// extract file extension
function getFileExtension(filename) {
    var parts = filename.split('.');
    return parts[parts.length - 1];
}

// Delete Application
function deleteApplication(){

    $('#app_creation_progress_modal').modal({ backdrop: 'static', keyboard: false});
    $("#app_creation_progress_modal").show();
    $("#modal-title").text("Deleting...");

    jagg.post("../blocks/application/application.jag", {
        action:"deleteVersion",
        versionKey:selectedApplicationRevision.hashId
    },function (result) {
        jagg.message({content: "Selected version deleted successfully", type: 'success', id:'view_log'});
        var versionCount = 0;
        for (var version in application.versions){
            versionCount++;
        }
        if(versionCount == 1){
            setTimeout(redirectAppListing, 2000);
        } else {
            setTimeout(redirectAppHome, 2000);
        }
    },function (jqXHR, textStatus, errorThrown) {
        jagg.message({content: "Error occurred while deleting the selected application version", type: 'error', id:'view_log'});
    });
}

// Delete integration
function deleteIntegration(){

    $('#app_creation_progress_modal').modal({ backdrop: 'static', keyboard: false});
    $("#app_creation_progress_modal").show();
    $("#modal-title").text("Deleting...");

    jagg.post("../blocks/application/application.jag", {
        action:"deleteApplication",
        applicationKey:applicationKey
    },function (result) {
        //TODO jagg.message({content: "Integration : " + applicationName + " deleted successfully", type: 'success', id:'view_log'});
        setTimeout(redirectAppListing, 2000);
    },function (jqXHR, textStatus, errorThrown) {
        jagg.message({content: "Error occurred while deleting integration : " + appName, type: 'error', id:'view_log'});
    });

}

function deleteApplicationPopUp(){
    jagg.popMessage({type:'confirm', modalStatus: true, title:'Delete Application Version',content:'Are you sure you want to delete this integration: ' + applicationName  + ' ?',
        okCallback:function(){
             deleteIntegration();
        }, cancelCallback:function(){}
    });
}

function redirectAppListing() {
    window.location.replace("index.jag");
}

function redirectAppHome() {
    window.location.replace("home.jag?applicationKey=" + applicationKey);
}
