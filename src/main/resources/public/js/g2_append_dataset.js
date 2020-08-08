Vue.component("dataset-samples-table", {
    props: ['dataset'],
    template: `
    <div class="card mb-3">
      <div class="card-header">
        <i>Dataset:</i> {{dataset.definition.name}}
      </div>
      <div class="card-body">
        <div class="table-responsive">
        <table class="table table-bordered" width="100%" cellspacing="0">
          <thead>
            <tr>
              <td v-for="attr in dataset.definition.attributes">
                <strong>{{attr.name}}</strong>
              </td>
            </tr>
          </thead>
          <tbody>
            <tr v-for="sample in dataset.samples">
              <td v-for="attr in dataset.definition.attributes">
                {{sample[attr.name].value}}
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>
    </div>
    `
});
Vue.component("dataset-row", {
    props: ['dataset','selection'],
    methods: {
      getdataset: function () {
        let location = this.dataset.location
        fetch(this.dataset.location, {
          method: "GET"
        })
        .then(response => response.json())
        .then((data) => {
          this.selection.dataset = data;
          this.selection.dataset.location = location;
        })        
      }
    },
    template: `
    <tr>
        <td>
            <button class="btn btn-link" v-on:click="getdataset()">{{dataset.name}}</button>
        </td>
        <td>{{dataset.location}}</td>
    </tr>
    `
});
Vue.component("dataset-sample-entry", {
    props: ['sample','attribute'],
    template: `
    <td>
      <div v-if="attribute.type === 'integer'">
        <input
            type="number"
            v-bind="{ max: attribute.bounds.max, min: attribute.bounds.min }"
            v-model.number="sample[attribute.name]"></input>
      </div>
      <div v-else-if="attribute.type === 'floating-point'">
        <input
            type="number"
            step="0.001"
            v-bind="{ max: attribute.bounds.max, min: attribute.bounds.min }"
            v-model.number="sample[attribute.name]"></input>
      </div>
      <div v-else-if="attribute.type === 'enumerated'">
        <select v-model="sample[attribute.name]"></input>
          <option disabled value="">Please select one</option>
          <option v-for="value in attribute.values">{{value}}</option>
        </select>
      </div>
      <div v-else-if="attribute.type === 'arbitrary'">
        <input v-model.number="sample[attribute.name]"></input>
      </div>
      <div v-else>
        No Clue
      </div>
    </td>
    `
});
Vue.component("dataset-sample-form", {
    props: ['dataset'],
    data: function () {
        return {sample: {}}
    },
    methods: {
        postsample: function() {
            var forSend = {}
            for (let attribute of this.dataset.definition.attributes) {
                forSend[attribute.name] = {
                    type: attribute.type,
                    value: this.sample[attribute.name]
                }
            }
            fetch(this.dataset.location, {
                body: JSON.stringify(forSend),
                method: "POST",
                headers: {
                  "Content-Type": "application/json",
                },
            })
            .then(response => response.json())
            .then(data => {
                this.dataset.samples = data.samples;
            });
        }
    },
    template: `
    <div class="card mb-3">
      <div class=card-header>
        Append a sample
      </div>
      <div class="card-body">
        <table>
          <thead>
            <tr>
              <td v-for="attribute in dataset.definition.attributes">{{attribute.name}}: {{attribute.type}}</td>
            </tr>
          </thead>
          <tbody>
            <tr>
              <dataset-sample-entry
                  v-for="attribute in dataset.definition.attributes"
                  v-bind:sample="sample"
                  v-bind:attribute="attribute"
                  v-bind:key="attribute.name">
              </dataset-sample-entry>
            </tr>
            <tr>
                <td>
                    <button v-on:click="postsample">Append</button>
                </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    `
});
const append_sample_component = new Vue({
    el: "#append-sample",
    data: {
        datasets: [],
        selection: { dataset: null }
    },
    methods: {
        reloadDataset: function () {
            let location = this.selection.dataset.location;
            fetch(location)
            .then(response => response.json())
            .then(data => {
                this.selection.dataset = data;
                this.selection.dataset.location = location;
            });
        },
    },
    mounted: function () {
        fetch("/api/datasets")
        .then(response => response.json())
        .then((data) => {
            this.datasets = data;
        });
    },
    template: `
    <div>
      <div class="card mb-3">
        <div class="card-header">
          <i class="fa fa-table"></i>Existing Datasets</div>
        <div class="card-body">
          <div class="table-responsive">        
            <table class="table table-bordered" id="dataTable" width="100%" cellspacing="0">
              <thead>
                <tr>
                  <th>Definition Name</th>
                  <th>Location</th>
                </tr>
              </thead>
              <tbody>
                <dataset-row
                  v-for="dataset in datasets"
                  v-bind:dataset="dataset"
                  v-bind:selection="selection"
                  v-bind:key="dataset.location"></dataset-row>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div v-if="selection.dataset !== null" class="card mb-3">
        <dataset-sample-form
          v-bind:dataset="selection.dataset"
          v-bind:key="selection.dataset.location">
        </dataset-sample-form>
      </div>
      <div v-if="selection.dataset !== null">
        <dataset-samples-table
          v-bind:dataset="selection.dataset"
          v-bind:key="selection.dataset.location">
        </dataset-samples-table>
      </div>
    </div>
    `
});
