/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

// page initialization
$(document).ready(function () {
    setExistingCustomUrl();
});

function setExistingCustomUrl() {
    if (defaultVersionName !== null && versions[defaultVersionName] != undefined) {
        var customUrl = versions[defaultVersionName].customtUrl;
        var hostUrl = stripedUrl(customUrl);
        $('#productionCustom').val(hostUrl);
        uiElementStateChange(true, true, true);
        showEditButton();
    } else {
        $('#updateCustomUrl').prop('disabled', true);
        showUpdateButton();
    }
}

function verifyCustomUrl() {
    var pointedUrl = $('#productionVersion').val();
    var customUrl = $('#productionCustom').val();
    var stripedPointedUrl = stripedUrl(pointedUrl);

    jagg.post("../blocks/urlmapper/urlmapper.jag", {
            action: "verifyCustomDomain",
            customUrl: customUrl,
            pointedUrl: stripedPointedUrl
        }, verifyCustomUrlSuccess,
        function (jqXHR, textStatus, errorThrown) {
            var errorMsg = "CNAME record does not exist for this config : \"" +
                customUrl + " CNAME " + stripedPointedUrl + "\". Please publish a CNAME record first."
            jagg.message({content: errorMsg, type: 'error', id: 'view_log'});
        });
}

function stripedUrl(url){
    var stripedUrl = url.replace(/.*?:\/\//g, "");
    return stripedUrl;
}

function verifyCustomUrlSuccess() {
    jagg.message({content: "The custom domain successfully added to the application.", type: 'success', id: 'view_log'});
    uiElementStateChange(true, true, true);
    showUpdateButton();
    $('#updateCustomUrl').prop('disabled', false);
}

function uiElementStateChange(prodVersion, prodCustom, verifyUrl) {
    $("#productionVersion").prop('disabled', prodVersion);
    $("#productionCustom").prop('disabled', prodCustom);
    $("#verifyUrl").prop('disabled', verifyUrl);
}

function showUpdateButton() {
    $('#editCustomUrl').hide();
    $('#updateCustomUrl').show();
}

function showEditButton() {
    $('#editCustomUrl').show();
    $('#updateCustomUrl').hide();
}

function updateCustomUrl() {
    var customUrl = $('#productionCustom').val();
    var versionName = $("#productionVersion option:selected").text();
    jagg.post("../blocks/urlmapper/urlmapper.jag", {
        action: "updateCustomUrl",
        customUrl: customUrl,
        applicationName: applicationName,
        versionName: versionName
    }, function (result) {
        jagg.message({content: "Custom domain successfully updated.", type: 'success', id: 'view_log'});
        showEditButton();
    }, function (jqXHR, textStatus, errorThrown) {
        jagg.message({content: "Error occurred while updating custom domain.", type: 'error', id: 'view_log'});

    });
}

function editCustomUrl() {
    uiElementStateChange(false, false, false);
}






