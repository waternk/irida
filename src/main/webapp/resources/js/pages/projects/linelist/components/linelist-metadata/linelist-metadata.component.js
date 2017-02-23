import {EVENTS} from './../../constants';

/**
 * Controller for handling getting the name for a new template.
 * @param {array} templates of existing templates.
 * @param {object} $uibModalInstance handle on the current modal.
 */
function saveTemplateController(templates, $uibModalInstance) {
  this.templates = templates;
  this.template = {};

  this.cancel = () => {
    this.template.name = '';
    $uibModalInstance.dismiss();
  };

  this.save = () => {
    $uibModalInstance.close(this.template.name);
    this.template.name = '';
  };
}

saveTemplateController.$inject = [
  'templates',
  '$uibModalInstance'
];

/**
 * Controller for the ng-aside that allows the user to select
 * visible metadata fields
 * @param {array} fields metadata fields
 * @param {function} toggleColumnVisibility call to toggle the column within the Datatables.
 */
function showMetadataFieldSelectionsController(fields, toggleColumnVisibility) {
  this.fields = fields;

  this.toggleColumn = column => {
    toggleColumnVisibility(column);
  };
}

showMetadataFieldSelectionsController.$inject = ['fields', 'toggleColumnVisibility'];

/**
 * Controller for MetadataComponent. Handles displaying toggles
 * for hiding and showing metadata columns.
 *
 * @param {object} $scope angular DOM scope reference.
 * @param {object} $aside Reference to the angular-aside instance
 * @param {object} $uibModal Reference to the angular-bootstrap modal instance
 * @param {object} MetadataTemplateService service for handling metadata templates
 *
 * @description
 *
 */
function MetadataController($scope, $aside, $uibModal,
                            MetadataTemplateService) {
  const vm = this;
  let FIELD_ORDER;
  let FIELDS;
  vm.headers = [];

  vm.$onInit = () => {
    MetadataTemplateService.query(templates => {
      this.templates = templates;
      this.selectedTemplate = this.templates[0];
    });

    FIELDS = vm.fields
      .map(field => {
        return {label: field, visible: true};
      });
    FIELD_ORDER = vm.fields.map((x, i) => i);
  };

  vm.showMetadataTemplator = () => {
    $aside.open({
      templateUrl: 'metadata.aside.tmpl',
      openedClass: 'metadata-open',
      controllerAs: '$ctrl',
      controller: showMetadataFieldSelectionsController,
      resolve: {
        fields() {
          // Send in the appropriately ordered fields based
          // on the current table order.
          return FIELD_ORDER.map(col => {
            return FIELDS[col];
          });
        },
        toggleColumnVisibility() {
          return vm.parent.updateColumnVisibility;
        }
      },
      placement: 'left',
      size: 'sm'
    });
  };

  vm.saveTemplate = () => {
    $uibModal
      .open({
        templateUrl: `save-template.tmpl.html`,
        controllerAs: '$modal',
        controller: saveTemplateController,
        resolve: {
          templates: () => {
            return vm.templates;
          }
        }
      })
      .result
      .then(name => {
        saveTemplate(name);
      });
  };

  vm.templateSelected = () => {
    let fields;
    if (Number(vm.selectedTemplate.identifier) === 0) {
      // Make sure that all fields are visible
      FIELDS.forEach(field => {
        field.visible = true;
      });
      fields = Array.from(FIELDS);

      // Datatables does not fire an event when the table is reset to its
      // original state, so we will just reset the field order here.
      FIELD_ORDER = vm.fields.map((x, i) => i);
    } else {
      fields = Array.from(vm.selectedTemplate.fields);

      // Make sure only the headers visible are turned on.
      FIELDS.forEach(field => {
        const index = fields.findIndex(f => {
          return f.label === field.label;
        });
        field.visible = index > -1;
      });
    }
    vm.parent.templateSelected(fields);
  };

  /**
   * Save the currently visible fields as a MetadataTemplate
   * @param {string} name for the new template
   */
  function saveTemplate(name) {
    const fields = FIELD_ORDER
      .map(index => {
        // Need to put the fields in the order they are in the table
        return FIELDS[index];
      })
      .filter(field => field.visible)
      .map(field => field.label);
    const newTemplate = new MetadataTemplateService();
    newTemplate.name = name;
    newTemplate.fields = fields;
    newTemplate.$save();
    vm.templates.push(newTemplate);
    vm.selectedTemplate = newTemplate;
  }

  // Set up event listener for re-arranging the columns on the table.
  $scope.$on(EVENTS.TABLE.colReorder, (e, args) => {
    const newOrder = args.columns;
    if (newOrder) {
      FIELD_ORDER = newOrder;
    }
  });
}

MetadataController.$inject = [
  '$scope',
  '$aside',
  '$uibModal',
  'MetadataTemplateService'
];

export const MetadataComponent = {
  templateUrl: 'metadata.button.tmpl',
  require: {
    parent: '^^linelist'
  },
  bindings: {
    templates: '<',
    fields: '<'
  },
  controller: MetadataController
};
