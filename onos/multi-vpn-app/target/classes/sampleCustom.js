// js for sample app custom view
(function () {
    'use strict';

    // injected refs
    var $log, $scope, wss, ks;

    // constants
    var dataReq = 'sampleCustomDataRequest',
        dataResp = 'sampleCustomDataResponse';

    var dataReqCustom = 'macAndVlan';

    function addKeyBindings() {
        var map = {
            space: [getData, 'Fetch data from server'],

            _helpFormat: [
                ['space']
            ]
        };

        ks.keyBindings(map);
    }
    function getData() {
        console.log("inside getData function");
        var macHost = document.getElementById('macHost').value;
        var vlanTagId = document.getElementById('vlanTagId').value;
        console.log("macHost: " + macHost);
        console.log("vlan ID: " + vlanTagId);
        var hostData = {"host": macHost, "vlanId": vlanTagId};
        var rowId = macHost + vlanTagId;
        if(document.getElementById(rowId) != null) {
            //element with id exists
            myDeleteFunction(rowId);
        } else {
            myCreateFunction(macHost, vlanTagId)
        }
        wss.sendEvent(dataReqCustom, hostData);
    }

    function myCreateFunction(macHost, vlanTagId) {
        var table = document.getElementById("myTable");
        var row = table.insertRow();
        var cell1 = row.insertCell(0);
        var cell2 = row.insertCell(1);
        cell1.innerHTML = macHost;
        cell2.innerHTML = vlanTagId;
        row.id = (macHost+vlanTagId);
    }

    function myDeleteFunction(rowId) {
        var row = document.getElementById(rowId);
        row.remove();
    }

    function respDataCb(data) {
        //myCreateFunction()
        console.log(data);
        $scope.data = data;
        $scope.$apply();
    }


    angular.module('ovSampleCustom', [])
        .controller('OvSampleCustomCtrl',
            ['$log', '$scope', 'WebSocketService', 'KeyService',

                function (_$log_, _$scope_, _wss_, _ks_) {
                    $log = _$log_;
                    $scope = _$scope_;
                    wss = _wss_;
                    ks = _ks_;

                    var handlers = {};
                    $scope.data = {};

                    // data response handler
                    handlers[dataResp] = respDataCb;
                    wss.bindHandlers(handlers);

                    addKeyBindings();

                    // custom click handler
                    $scope.getData = getData;

                    // get data the first time...
                    getData();

                    // cleanup
                    $scope.$on('$destroy', function () {
                        wss.unbindHandlers(handlers);
                        ks.unbindKeys();
                        $log.log('OvSampleCustomCtrl has been destroyed');
                    });

                    $log.log('OvSampleCustomCtrl has been created');
                }]);

}());