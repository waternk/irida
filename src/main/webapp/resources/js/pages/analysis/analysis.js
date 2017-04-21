(function (angular, page, notifications) {
  /**
   * Controller to download the analysis.
   * @constructor
   */
  function FileDownloadController() {
    var vm = this;

    vm.getZipFile = function (id) {
      var iFrameId = 'hiddenDownloader';
      var hiddenIFrame = document.getElementById(iFrameId);
      if (hiddenIFrame === null) {
        hiddenIFrame = document.createElement('iframe');
        hiddenIFrame.id = iFrameId;
        hiddenIFrame.style.display = 'none';
        document.body.appendChild(hiddenIFrame);
      }
      hiddenIFrame.src = page.URLS.download + id + '?dandelionAssetFilterState=false';
    };
  }

  /**
   * Service for Analysis
   * @param $http
   * @param $interval
   * @returns {AnalysisService}
   * @constructor
   */
  function AnalysisService($http) {
    var svc = this;

    /**
     * Call the server to get the status for the current analysis.
     * 'page.URLS.status' is on the `_base.html` page for the analysis.
     * @returns {*}
     * @private
     */
    function _getState() {
      return $http.get(page.URLS.status).then(function (data) {
        return data.data;
      });
    }

    
    /**
     * Get the results from a SISTR analysis to preview in the browser.
     */
    svc.getSistrResults = function() {
      return $http.get(page.URLS.sistr).then(function(result) {
        return result.data;
      });
    }

    /**
     * Exported function to call the server for information about the current analysis.
     * @param fn Callback function with how to handle the results.
     */
    svc.getAnalysisState = function (fn) {
      _getState().then(function (data) {
        fn(data);
      });
    };

    /**
     * Call the server to update the shared status of the current analysis.
     */
    svc.updateProjectShare = function(project, shared) {
      var data = {project: project, shared: shared};
      return $http.post(page.URLS.share, data).then(function(response) {
        return response.data;
      });
    };

    /**
     * Call the server to get the shared status of project
     */
    svc.getSharedProjects = function() {
      return $http.get(page.URLS.share).then(function(response) {
        return response.data;
      });
    };

    return svc;
  }

  function ProjectShareController(AnalysisService, notifications) {
    var vm = this;

    vm.projects = {};

    function initialize() {
      AnalysisService.getSharedProjects().then(function(response){
        vm.projects = response;
        console.log(vm.projects);
      });
    }

    vm.updateShared = function(project) {
      AnalysisService.updateProjectShare(project.project.identifier, project.shared).then(function(response) {
        notifications.show({msg: response.message});
      });
    };

    initialize();
  }

  /**
   * Controls the state for the current analysis.
   * @param AnalysisService
   * @constructor
   */
  function StateController(AnalysisService) {
    var vm = this;
    vm.percentage = 0;

    /**
     * Initializes the sate of the analysis.
     */
    function initialize() {
      return AnalysisService.getAnalysisState(function (data) {
        vm.state = data.state;
        vm.stateLang = data.stateLang;
        vm.percentage = Math.round(parseFloat(data.percentComplete));
        vm.stateClass = _createClass(vm.state);
      });
    }

    /**
     * Dynamically creates the border color for the top of the sidebar depending on the state of the analysis.
     * @param state
     * @returns {string}
     * @private
     */
    function _createClass(state) {
      return 'analysis__alert--' + state.toLowerCase();
    }

    initialize();
  }

  function PreviewController() {
    var vm = this;
    vm.newick = page.NEWICK;
  }

  function SistrController(analysisService) {
    var vm = this;

    analysisService.getSistrResults().then(function(result) {
      if (result['parse_results_error']) {
        vm.parse_results_error = true;
      } else {
        var sample_information = {};
        sample_information['name'] = result['sample_name'];
        sample_information['qc_status'] = result['qc_status'];
        sample_information['qc_messages'] = result['qc_messages'].split("|");
        sample_information['qc_pass'] = (result['qc_status'] == 'PASS');

        var serotype_predictions_order = ['Serovar (overall)', 'Serovar (antigen)', 'Serovar (cgMLST)', 'Serogroup', 'H1', 'H2', 'O-antigen'];
        var serotype_predictions = {};
        serotype_predictions['Serovar (overall)'] = result['serovar'];
        serotype_predictions['Serovar (antigen)'] = result['serovar_antigen'];
        serotype_predictions['Serovar (cgMLST)'] = result['serovar_cgmlst'];
        serotype_predictions['Serogroup'] = result['serogroup'];
        serotype_predictions['H1'] = result['h1'];
        serotype_predictions['H2'] = result['h2'];
        serotype_predictions['O-antigen'] = result['o_antigen'];
  
        var cgMLST_predictions = {};
        var cgMLST_predictions_order = ['Subspecies', 'Matching genome name', 'Alleles matching genome', 'Percent matching', 'cgMLST Sequence Type'];
        cgMLST_predictions['Subspecies'] = result['cgmlst_subspecies'];
        cgMLST_predictions['Matching genome name'] = result['cgmlst_genome_match'];
        cgMLST_predictions['Alleles matching genome'] = result['cgmlst_matching_alleles']+'/330';
        cgMLST_predictions['Percent matching'] = parseFloat((1 - result['cgmlst_distance'])*100).toFixed(1)+"%";
        cgMLST_predictions['cgMLST Sequence Type'] = result['cgmlst_ST'];
  
        var mash_predictions = {};
        var mash_predictions_order = ['Subspecies', 'Serovar', 'Matching genome name', 'Percent shared k-mers'];
        mash_predictions['Subspecies'] = result['mash_subspecies'];
        mash_predictions['Serovar'] = result['mash_serovar'];
        mash_predictions['Matching genome name'] = result['mash_genome'];
        mash_predictions['Percent shared k-mers'] = parseFloat((1 - result['mash_distance'])*100).toFixed(1)+"%";
  
        vm.sample_information = sample_information;
        vm.serotype_predictions = serotype_predictions;
        vm.serotype_predictions_order = serotype_predictions_order;
        vm.cgMLST_predictions_order = cgMLST_predictions_order;
        vm.cgMLST_predictions = cgMLST_predictions;
        vm.mash_predictions_order = mash_predictions_order;
        vm.mash_predictions = mash_predictions;
        vm.parse_results_error = result['parse_results_error'];
      }
    });
  }

  angular.module('irida.analysis', ['ui.router', 'subnav', 'phylocanvas'])
    .config(['$stateProvider', function ($stateProvider) {
      $stateProvider
        .state("preview", {
          url        : "/preview",
          templateUrl: "preview.html"
        })
        .state("sistr", {
          url: "/sistr",
          templateUrl: "sistr.html",
          controllerAs: "sistrCtrl",
          controller: ["AnalysisService", SistrController]
        })
        .state("inputs", {
          url        : "/inputs",
          templateUrl: "inputs.html"
        })
        .state("provenance", {
          url        : "/provenance",
          templateUrl: "provenance.html"
        })
        .state("share", {
          url        : "/share",
          templateUrl: "share.html"
        })
      ;
    }])
    .service('AnalysisService', ['$http', AnalysisService])
    .controller('FileDownloadController', [FileDownloadController])
    .controller('StateController', ['AnalysisService', StateController])
    .controller('PreviewController', [PreviewController])
    .controller('ProjectShareController', ['AnalysisService', 'notifications', ProjectShareController])
  ;
})(window.angular, window.PAGE, window.notifications);
