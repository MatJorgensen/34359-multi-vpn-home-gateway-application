// js for sample app custom view
(function () {
    'use strict';

    // injected refs
    var $log, $scope, wss, ks;

    // constants
    var dataReq = 'sampleCustomDataRequest',
        dataResp = 'sampleCustomDataResponse';

    var dataReqCustom = 'toggleVlanRequest';

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
        var hostMac = document.getElementById('hostMac').value;
        var vlanId = document.getElementById('vlanId').value;
        console.log("hostMac: " + hostMac);
        console.log("vlanId: " + vlanId);
        var hostData = {"host": hostMac, "vlanId": vlanId};
        var rowId = hostMac + vlanId;
        if(document.getElementById(rowId) != null) {
            //element with id exists
            myDeleteFunction(rowId);
        } else {
            myCreateFunction(hostMac, vlanId)
        }
        wss.sendEvent(dataReqCustom, hostData);
    }

    function myCreateFunction(hostMac, vlanId) {
        var table = document.getElementById("myTable");
        var row = table.insertRow();
        var cell1 = row.insertCell(0);
        var cell2 = row.insertCell(1);
        cell1.innerHTML = hostMac;
        cell2.innerHTML = vlanId;
        row.id = (hostMac + vlanId);
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
